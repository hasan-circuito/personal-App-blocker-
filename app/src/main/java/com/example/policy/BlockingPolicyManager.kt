package com.example.policy

enum class BlockingScope {
    CURRENT_WEBSITE,
    INPUT_ONLY,
    ENTIRE_APPLICATION
}

object BlockingPolicyManager {

    private val KNOWN_BROWSER_PACKAGES = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.microsoft.emmx",
        "com.brave.browser",
        "com.sec.android.app.sbrowser",
        "com.duckduckgo.mobile.android",
        "com.vivaldi.browser",
        "com.opera.mini.native"
    )

    private val KNOWN_TELEGRAM_PACKAGES = setOf(
        "org.telegram.messenger",
        "org.telegram.messenger.web",
        "org.telegram.messenger.beta",
        "org.telegram.plus",
        "org.telegram.Bifogram",
        "org.telegram.vidogram"
    )

    fun isBrowserApp(packageName: String): Boolean {
        val lowerPkg = packageName.lowercase()
        return KNOWN_BROWSER_PACKAGES.contains(lowerPkg) ||
                lowerPkg.contains("browser") ||
                lowerPkg.contains("chrome") ||
                lowerPkg.contains("firefox")
    }

    fun isTelegramApp(packageName: String): Boolean {
        val lowerPkg = packageName.lowercase()
        return KNOWN_TELEGRAM_PACKAGES.contains(lowerPkg) ||
                lowerPkg.contains("telegram")
    }

    fun isInputProtectedApp(packageName: String): Boolean {
        return isBrowserApp(packageName) || isTelegramApp(packageName)
    }

    fun getBlockingScope(packageName: String): BlockingScope {
        return when {
            isBrowserApp(packageName) -> BlockingScope.CURRENT_WEBSITE
            isTelegramApp(packageName) -> BlockingScope.INPUT_ONLY
            else -> BlockingScope.ENTIRE_APPLICATION
        }
    }
}
