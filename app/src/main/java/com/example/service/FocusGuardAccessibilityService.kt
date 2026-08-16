package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.example.data.model.ProtectionState
import com.example.data.repository.FocusGuardRepository
import com.example.detection.DetectionLevel
import com.example.detection.HeuristicContentDetector
import com.example.policy.BlockingPolicyManager
import com.example.policy.BlockingScope
import com.example.policy.InputProtectionEngine
import com.example.util.BrowserDomainExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class FocusGuardAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var activeInstance: FocusGuardAccessibilityService? = null
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var repository: FocusGuardRepository
    private val contentDetector = HeuristicContentDetector()

    private var lastAnalyzedPackage: String? = null
    private var lastAnalysisTime: Long = 0L

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        activeInstance = this
        repository = FocusGuardRepository.getInstance(applicationContext)
        serviceScope.launch(Dispatchers.IO) {
            repository.clearVisualLockouts()
        }
        Log.i("FocusGuardService", "FocusGuard Build: ${com.example.BuildConfig.APPLICATION_ID} v${com.example.BuildConfig.VERSION_NAME} (${com.example.BuildConfig.VERSION_CODE})")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        activeInstance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventPackage = event.packageName?.toString() ?: ""
        val activeRoot = rootInActiveWindow
        val activeWindowPackage = activeRoot?.packageName?.toString() ?: ""

        Log.d("FocusGuardService", "[FG-A11Y-EVENT] type=${event.eventType} pkg=$eventPackage class=${event.className} activeWinPkg=$activeWindowPackage")

        // EXCLUSION RULE: Never monitor or trigger block actions when FocusGuard itself is in focus or generating events
        if (eventPackage == applicationContext.packageName || activeWindowPackage == applicationContext.packageName) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activeRoot?.recycle()
            }
            return
        }

        // Ignore System UI or input methods
        if (eventPackage == "com.android.systemui" || activeWindowPackage == "com.android.systemui") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activeRoot?.recycle()
            }
            return
        }

        val targetPackage = if (activeWindowPackage.isNotBlank()) activeWindowPackage else eventPackage
        if (targetPackage.isBlank() || targetPackage == applicationContext.packageName) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activeRoot?.recycle()
            }
            return
        }

        // --- Telegram Channel Re-Entry Guard (Phase 6 & 7) ---
        if (BlockingPolicyManager.isTelegramApp(targetPackage)) {
            val wasReentryIntercepted = com.example.detection.TelegramChannelGuardManager.checkAndEnforceReentryRestriction(
                service = this,
                rootNode = activeRoot,
                targetPackage = targetPackage
            )
            if (wasReentryIntercepted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    activeRoot?.recycle()
                }
                return
            }
        }

        // --- On-Device Visual Content Classifier Pipeline Trigger ---
        if (com.example.detection.VisualDiagnosticPipeline.isDiagnosticActive.value) {
            com.example.detection.VisualDiagnosticPipeline.maybeSampleFrame(
                service = this,
                packageName = targetPackage,
                className = event.className?.toString() ?: "",
                rootNode = activeRoot
            )
        }

        serviceScope.launch(Dispatchers.IO) {
            try {
                // UNIVERSAL ACTIVE LOCKOUT CHECK: Enforce BlockOverlayScreen if package is actively locked out
                val activeLockout = repository.getActiveLockout(targetPackage)
                val now = System.currentTimeMillis()
                if (activeLockout != null && activeLockout.unlockTimestamp > now) {
                    val appName = getAppName(targetPackage)
                    BlockOverlayManager.showBlockScreen(
                        context = applicationContext,
                        packageName = targetPackage,
                        appName = appName,
                        lockoutReason = activeLockout.reason
                    )
                    return@launch
                }

                val scope = BlockingPolicyManager.getBlockingScope(targetPackage)

                if (scope == BlockingScope.ENTIRE_APPLICATION) {
                    // --- NON-BROWSER APP POLICY: Block Entire Application ---
                    val protectionState = repository.getProtectionStateSync()
                    if (protectionState == ProtectionState.PROTECTION_DISABLED) return@launch

                    val isMonitored = repository.isAppMonitored(targetPackage)
                    if (!isMonitored) return@launch

                    val nowTime = System.currentTimeMillis()
                    if (targetPackage == lastAnalyzedPackage && (nowTime - lastAnalysisTime) < 1500) {
                        return@launch
                    }
                    lastAnalyzedPackage = targetPackage
                    lastAnalysisTime = nowTime

                    val nodeTexts = mutableListOf<String>()
                    activeRoot?.let { root ->
                        extractNodeTexts(root, nodeTexts)
                    }
                    if (nodeTexts.isEmpty()) return@launch

                    val sensitivity = repository.getSensitivitySync()
                    val result = contentDetector.detectContent(nodeTexts, targetPackage, sensitivity)

                    if (result.level == DetectionLevel.ADULT) {
                        Log.w("FocusGuardService", "ADULT CONTENT DETECTED in $targetPackage. Triggering app lockout.")
                        val appName = getAppName(targetPackage)
                        val durationMinutes = repository.getLockoutDurationMinutesSync()

                        val lockout = repository.createLockout(
                            packageName = targetPackage,
                            appName = appName,
                            durationMinutes = durationMinutes,
                            reason = result.triggerReason,
                            confidence = result.confidence
                        )

                        BlockOverlayManager.showBlockScreen(
                            context = applicationContext,
                            packageName = targetPackage,
                            appName = appName,
                            lockoutReason = lockout.reason
                        )
                    }
                } else {
                    // --- INPUT-PROTECTED POLICY (Browsers & Telegram): Prevent/Remove Prohibited Input Only ---
                    val protectionState = repository.getProtectionStateSync()
                    if (protectionState == ProtectionState.PROTECTION_DISABLED) return@launch

                    val isMonitored = repository.isAppMonitored(targetPackage)
                    if (!isMonitored) return@launch

                    val nowTime = System.currentTimeMillis()
                    if (targetPackage == lastAnalyzedPackage && (nowTime - lastAnalysisTime) < 300) {
                        return@launch
                    }

                    val sensitivity = repository.getSensitivitySync()
                    val protectionResult = InputProtectionEngine.processInputProtection(
                        service = this@FocusGuardAccessibilityService,
                        rootNode = activeRoot,
                        packageName = targetPackage,
                        sensitivity = sensitivity,
                        detector = contentDetector
                    )

                    if (protectionResult.isHandled && protectionResult.detectionResult != null) {
                        lastAnalyzedPackage = targetPackage
                        lastAnalysisTime = nowTime

                        val appName = getAppName(targetPackage)
                        val actionDesc = if (protectionResult.wasTextModified) {
                            "Removed prohibited text input (${protectionResult.detectionResult.triggerReason})"
                        } else {
                            "Prevented prohibited search/input: ${protectionResult.detectionResult.triggerReason}"
                        }

                        repository.recordBlockEvent(
                            packageName = targetPackage,
                            appName = appName,
                            reason = actionDesc,
                            confidence = protectionResult.detectionResult.confidence
                        )

                        val toastMsg = if (protectionResult.wasTextModified) {
                            "FocusGuard: Prohibited text removed."
                        } else {
                            "FocusGuard: Prohibited search prevented."
                        }
                        showToast(toastMsg)
                    }
                }
            } catch (e: Exception) {
                Log.e("FocusGuardService", "Error during accessibility event processing", e)
            } finally {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    activeRoot?.recycle()
                }
            }
        }
    }

    private fun showToast(message: String) {
        mainHandler.post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun extractNodeTexts(node: AccessibilityNodeInfo, textList: MutableList<String>) {
        node.text?.let { text ->
            val str = text.toString().trim()
            if (str.isNotBlank()) {
                textList.add(str)
            }
        }

        node.contentDescription?.let { desc ->
            val str = desc.toString().trim()
            if (str.isNotBlank() && !textList.contains(str)) {
                textList.add(str)
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            extractNodeTexts(child, textList)
            child.recycle()
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    override fun onInterrupt() {
        Log.i("FocusGuardService", "Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activeInstance == this) {
            activeInstance = null
        }
        serviceScope.cancel()
    }
}

