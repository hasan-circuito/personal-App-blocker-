package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val isBrowserOrSocial: Boolean
)

object AppHelper {

    private val commonBrowserPackages = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.microsoft.emmx",
        "com.brave.browser",
        "com.sec.android.app.sbrowser",
        "com.duckduckgo.mobile.android",
        "com.reddit.frontpage",
        "com.instagram.android",
        "com.twitter.android",
        "com.tiktok.m2",
        "com.facebook.katana"
    )

    fun getInstalledApps(context: Context): List<InstalledAppInfo> {
        val pm = context.packageManager
        val appsList = mutableListOf<InstalledAppInfo>()

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PackageManager.ApplicationInfoFlags.of(0)
        } else {
            0
        }

        try {
            val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledApplications(0)
            }

            for (appInfo in packages) {
                // Ignore our own package
                if (appInfo.packageName == context.packageName) continue

                val appName = pm.getApplicationLabel(appInfo).toString()
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val icon = try {
                    pm.getApplicationIcon(appInfo)
                } catch (e: Exception) {
                    null
                }

                val isBrowserOrSocial = commonBrowserPackages.contains(appInfo.packageName) ||
                        appName.lowercase().contains("browser") ||
                        appName.lowercase().contains("chrome") ||
                        appName.lowercase().contains("firefox")

                appsList.add(
                    InstalledAppInfo(
                        packageName = appInfo.packageName,
                        appName = appName,
                        icon = icon,
                        isSystemApp = isSystem,
                        isBrowserOrSocial = isBrowserOrSocial
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Sort: Common Browsers first, then alphabetically
        return appsList.sortedWith(
            compareByDescending<InstalledAppInfo> { it.isBrowserOrSocial }
                .thenBy { it.appName.lowercase() }
        )
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedService = "${context.packageName}/com.example.service.FocusGuardAccessibilityService"
        return try {
            val enabledServices = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            enabledServices.contains(context.packageName)
        } catch (e: Exception) {
            false
        }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openOverlaySettings(context: Context) {
        val intent = Intent(
            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
