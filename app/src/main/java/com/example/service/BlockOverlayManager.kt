package com.example.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.example.ui.BlockActivity

object BlockOverlayManager {

    fun showBlockScreen(
        context: Context,
        packageName: String,
        appName: String? = null,
        lockoutReason: String? = null
    ) {
        val resolvedAppName = appName ?: getAppName(context, packageName)

        val intent = Intent(context, BlockActivity::class.java).apply {
            putExtra(BlockActivity.EXTRA_PACKAGE_NAME, packageName)
            putExtra(BlockActivity.EXTRA_APP_NAME, resolvedAppName)
            putExtra(BlockActivity.EXTRA_REASON, lockoutReason ?: "Adult content detected")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        context.startActivity(intent)
    }

    private fun getAppName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
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

    fun canDrawOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }
}
