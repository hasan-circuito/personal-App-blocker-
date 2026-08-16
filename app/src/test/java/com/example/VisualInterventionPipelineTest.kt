package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ProtectionState
import com.example.data.repository.FocusGuardRepository
import com.example.detection.VisualClassification
import com.example.detection.VisualDiagnosticPipeline
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VisualInterventionPipelineTest {

    private lateinit var context: Context
    private lateinit var repository: FocusGuardRepository
    private val telegramPkg = "org.telegram.messenger"

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        repository = FocusGuardRepository.getInstance(context)
        repository.clearAllLockouts()
        repository.clearBlockHistory()
        repository.setProtectionEnabled(true)
        repository.setAppMonitored(telegramPkg, "Telegram", true)
        VisualDiagnosticPipeline.setDiagnosticActive(true)
        VisualDiagnosticPipeline.resetRiskConfirmationState()
        com.example.detection.TelegramChannelGuardManager.clearAllRestrictions()
    }

    @After
    fun tearDown() = runBlocking {
        repository.clearAllLockouts()
        repository.clearBlockHistory()
        VisualDiagnosticPipeline.resetRiskConfirmationState()
        com.example.detection.TelegramChannelGuardManager.clearAllRestrictions()
    }

    @Test
    fun `TEST 1 - Telegram normal safe video frames do not trigger confirmation or interception`() = runBlocking {
        val now = System.currentTimeMillis()

        // 4 consecutive SAFE frames
        val res1 = VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.SAFE, 0.95f, now)
        assertFalse("Frame 1 SAFE should not confirm", res1.isConfirmed)
        assertEquals(0, res1.highRiskCount)

        val res2 = VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.SAFE, 0.96f, now + 2000)
        assertFalse("Frame 2 SAFE should not confirm", res2.isConfirmed)
        assertEquals(0, res2.highRiskCount)

        val res3 = VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.SAFE, 0.98f, now + 4000)
        assertFalse("Frame 3 SAFE should not confirm", res3.isConfirmed)
        assertEquals(0, res3.highRiskCount)

        val res4 = VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.SAFE, 0.99f, now + 6000)
        assertFalse("Frame 4 SAFE should not confirm", res4.isConfirmed)
        assertEquals(0, res4.highRiskCount)

        val lockout = repository.getActiveLockout(telegramPkg)
        assertNull("No lockout should be created for safe frames", lockout)
    }

    @Test
    fun `TEST 2 - High-risk Telegram video in VISUAL_TEST_MODE confirms risk and intercepts media without app lockout`() = runBlocking {
        val now = System.currentTimeMillis()

        // Frame 1: HIGH_RISK
        val r1 = VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.90f, now)
        assertFalse(r1.isConfirmed)
        assertEquals(1, r1.highRiskCount)

        // Frame 2: HIGH_RISK
        val r2 = VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.92f, now + 2000)
        assertFalse(r2.isConfirmed)
        assertEquals(2, r2.highRiskCount)

        // Frame 3: SAFE
        val r3 = VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.SAFE, 0.95f, now + 4000)
        assertFalse(r3.isConfirmed)
        assertEquals(2, r3.highRiskCount)

        // Frame 4: HIGH_RISK (Total 3 of last 4 within 8 seconds)
        val r4 = VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.94f, now + 6000)
        assertTrue("3 of 4 high risk frames within 8s must be confirmed", r4.isConfirmed)
        assertEquals(3, r4.highRiskCount)

        // Execute intervention in VISUAL_TEST_MODE
        VisualDiagnosticPipeline.executeVisualIntervention(context, telegramPkg, r4.averageConfidence)

        // Verify: Telegram itself remains completely unlocked (NO lockout entity created)
        val lockout = repository.getActiveLockout(telegramPkg)
        assertNull("In VISUAL_TEST_MODE, no 2-hour Telegram lockout should be created", lockout)
        assertTrue(VisualDiagnosticPipeline.interventionStatus.value.startsWith("INTERCEPTED"))
    }

    @Test
    fun `TEST 3 - Single HIGH_RISK frame followed by SAFE frames does not confirm risk`() = runBlocking {
        val now = System.currentTimeMillis()

        // Frame 1: HIGH_RISK
        val r1 = VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.91f, now)
        assertFalse(r1.isConfirmed)

        // Frame 2: SAFE
        val r2 = VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.SAFE, 0.96f, now + 2000)
        assertFalse(r2.isConfirmed)

        // Frame 3: SAFE
        val r3 = VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.SAFE, 0.97f, now + 4000)
        assertFalse(r3.isConfirmed)

        // Frame 4: SAFE
        val r4 = VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.SAFE, 0.98f, now + 6000)
        assertFalse("Single candidate must not confirm risk", r4.isConfirmed)
        assertEquals(1, r4.highRiskCount)

        val lockout = repository.getActiveLockout(telegramPkg)
        assertNull("No lockout should occur for single false positive frame", lockout)
    }

    @Test
    fun `TEST 4 - Multiple high-risk detections in VISUAL_TEST_MODE never lock Telegram`() = runBlocking {
        VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.95f)
        VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.95f)
        VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.95f)

        // Session 1: Interception
        VisualDiagnosticPipeline.executeVisualIntervention(context, telegramPkg, 0.95f)
        var lockout = repository.getActiveLockout(telegramPkg)
        assertNull("Telegram must not be locked after session 1", lockout)

        // Session 2: Reset & re-trigger
        VisualDiagnosticPipeline.resetRiskConfirmationState()
        VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.96f)
        VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.96f)
        VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.96f)
        VisualDiagnosticPipeline.executeVisualIntervention(context, telegramPkg, 0.96f)

        lockout = repository.getActiveLockout(telegramPkg)
        assertNull("Telegram must still not be locked after session 2", lockout)
    }

    @Test
    fun `TEST 5 - Protection OFF prevents intervention action even if visual risk is confirmed`() = runBlocking {
        repository.setProtectionEnabled(false)

        VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.95f)
        VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.95f)
        val r = VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.95f)
        assertTrue(r.isConfirmed)

        VisualDiagnosticPipeline.executeVisualIntervention(context, telegramPkg, r.averageConfidence)

        val lockout = repository.getActiveLockout(telegramPkg)
        assertNull("No lockout should be created when protection is OFF", lockout)
        assertEquals("DISABLED (Protection Inactive)", VisualDiagnosticPipeline.interventionStatus.value)
    }

    @Test
    fun `TEST 6 - Target app not monitored prevents intervention action`() = runBlocking {
        repository.setAppMonitored(telegramPkg, "Telegram", false)

        val r = VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.95f)
        VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.95f)
        VisualDiagnosticPipeline.evaluateTemporalRisk(VisualClassification.HIGH_RISK, 0.95f)

        VisualDiagnosticPipeline.executeVisualIntervention(context, telegramPkg, 0.95f)

        val lockout = repository.getActiveLockout(telegramPkg)
        assertNull("No lockout should be created when target app is unmonitored", lockout)
        assertEquals("WAITING (App Not Monitored)", VisualDiagnosticPipeline.interventionStatus.value)
    }

    @Test
    fun `TEST 7 - clearVisualLockouts resets visual-triggered lockouts while preserving text-based lockouts`() = runBlocking {
        // Create 1 visual lockout and 1 text lockout
        repository.createLockout(
            packageName = telegramPkg,
            appName = "Telegram",
            durationMinutes = 120,
            reason = "Visual Adult Content Detected (On-Device Classifier)",
            confidence = 0.95f
        )
        val textPkg = "com.android.chrome"
        repository.createLockout(
            packageName = textPkg,
            appName = "Chrome",
            durationMinutes = 120,
            reason = "Prohibited Adult Search Keyword",
            confidence = 1.0f
        )

        assertNotNull("Visual lockout should exist prior to clearing", repository.getActiveLockout(telegramPkg))
        assertNotNull("Text lockout should exist prior to clearing", repository.getActiveLockout(textPkg))

        // Clear visual lockouts
        repository.clearVisualLockouts()

        // Telegram visual lockout cleared
        assertNull("Telegram visual lockout must be cleared", repository.getActiveLockout(telegramPkg))
        // Chrome text lockout preserved
        assertNotNull("Chrome text lockout must remain active", repository.getActiveLockout(textPkg))
    }

    @Test
    fun `TEST 8 - TelegramChannelGuardManager restricts channel session and prevents re-entry`() = runBlocking {
        val channelContext = com.example.detection.TelegramChannelGuardManager.TelegramChannelContext(
            title = "Test Channel",
            username = "@testchannel",
            channelIdentifier = "@testchannel",
            identifierSource = "ACCESSIBILITY_USERNAME"
        )

        // Restrict channel
        val restriction = com.example.detection.TelegramChannelGuardManager.restrictChannelContext(
            channelContext = channelContext,
            reason = "Adult Content Confirmed",
            confidence = 0.92f,
            durationMs = 15 * 60 * 1000L
        )

        assertNotNull(restriction)
        assertTrue(com.example.detection.TelegramChannelGuardManager.isChannelRestricted(channelContext.channelIdentifier))
        assertEquals(1, com.example.detection.TelegramChannelGuardManager.activeRestrictions.value.size)

        // Clear restrictions
        com.example.detection.TelegramChannelGuardManager.clearAllRestrictions()
        assertFalse(com.example.detection.TelegramChannelGuardManager.isChannelRestricted(channelContext.channelIdentifier))
        assertEquals(0, com.example.detection.TelegramChannelGuardManager.activeRestrictions.value.size)
    }
}
