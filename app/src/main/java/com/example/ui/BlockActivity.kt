package com.example.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.repository.FocusGuardRepository
import com.example.ui.screens.BlockOverlayScreen
import com.example.ui.theme.FocusGuardTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlockActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_REASON = "extra_reason"
    }

    private lateinit var repository: FocusGuardRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = FocusGuardRepository.getInstance(applicationContext)
        processLockoutIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processLockoutIntent(intent)
    }

    private fun processLockoutIntent(intent: Intent?) {
        val packageName = intent?.getStringExtra(EXTRA_PACKAGE_NAME) ?: run {
            finish()
            return
        }
        val fallbackAppName = intent.getStringExtra(EXTRA_APP_NAME) ?: "Monitored Application"
        val fallbackReason = intent.getStringExtra(EXTRA_REASON) ?: "Content policy lockout"

        CoroutineScope(Dispatchers.Main).launch {
            val lockout = repository.getActiveLockout(packageName)
            val now = System.currentTimeMillis()

            if (lockout == null || lockout.unlockTimestamp <= now) {
                // No active lockout exists for this package. Finish BlockActivity so app is usable.
                finish()
                return@launch
            }

            val appName = lockout.appName.ifBlank { fallbackAppName }
            val reason = lockout.reason.ifBlank { fallbackReason }

            setContent {
                FocusGuardTheme {
                    BlockOverlayScreen(
                        packageName = packageName,
                        appName = appName,
                        reason = reason,
                        unlockTimestamp = lockout.unlockTimestamp,
                        onCloseClick = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Direct to Home Screen on back press to enforce lockout
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}
