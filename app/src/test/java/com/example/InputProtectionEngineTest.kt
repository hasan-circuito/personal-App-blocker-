package com.example

import com.example.policy.BlockingPolicyManager
import com.example.policy.BlockingScope
import com.example.policy.InputProtectionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputProtectionEngineTest {

    @Test
    fun `test sanitizeText removes prohibited keyword from middle of sentence`() {
        val input = "hello porn world"
        val clean = InputProtectionEngine.sanitizeText(input, listOf("porn"))
        assertEquals("hello world", clean)
    }

    @Test
    fun `test sanitizeText removes single prohibited word`() {
        val input = "porn"
        val clean = InputProtectionEngine.sanitizeText(input, listOf("porn"))
        assertEquals("", clean)
    }

    @Test
    fun `test sanitizeText preserves innocent words containing substring`() {
        val input = "This is an important support passport"
        val clean = InputProtectionEngine.sanitizeText(input, listOf("porn"))
        assertEquals("This is an important support passport", clean)
    }

    @Test
    fun `test sanitizeText removes pasted adult keyword`() {
        val input = "check xhamster now"
        val clean = InputProtectionEngine.sanitizeText(input, listOf("xhamster"))
        assertEquals("check now", clean)
    }

    @Test
    fun `test sanitizeText removes Bangla prohibited keyword and preserves rest of sentence`() {
        val input = "হ্যালো চোদা বিশ্ব"
        val clean = InputProtectionEngine.sanitizeText(input, listOf("চোদা"))
        assertEquals("হ্যালো বিশ্ব", clean)
    }

    @Test
    fun `test sanitizeText removes single Bangla prohibited word`() {
        val input = "চোদা"
        val clean = InputProtectionEngine.sanitizeText(input, listOf("চোদা"))
        assertEquals("", clean)
    }

    @Test
    fun `test sanitizeText removes Banglish prohibited word in sentence`() {
        val input = "check choda video link"
        val clean = InputProtectionEngine.sanitizeText(input, listOf("choda"))
        assertEquals("check video link", clean)
    }

    @Test
    fun `test Telegram package classification`() {
        val telegramPkg = "org.telegram.messenger"
        assertTrue(BlockingPolicyManager.isTelegramApp(telegramPkg))
        assertTrue(BlockingPolicyManager.isInputProtectedApp(telegramPkg))
        assertFalse(BlockingPolicyManager.isBrowserApp(telegramPkg))
        assertEquals(BlockingScope.INPUT_ONLY, BlockingPolicyManager.getBlockingScope(telegramPkg))
    }

    @Test
    fun `test Chrome package classification is preserved`() {
        val chromePkg = "com.android.chrome"
        assertTrue(BlockingPolicyManager.isBrowserApp(chromePkg))
        assertTrue(BlockingPolicyManager.isInputProtectedApp(chromePkg))
        assertFalse(BlockingPolicyManager.isTelegramApp(chromePkg))
        assertEquals(BlockingScope.CURRENT_WEBSITE, BlockingPolicyManager.getBlockingScope(chromePkg))
    }
}
