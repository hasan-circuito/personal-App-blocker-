package com.example.util

import android.view.accessibility.AccessibilityNodeInfo
import java.net.URI
import java.util.Locale

object BrowserDomainExtractor {

    // Common URL bar resource ID endings across popular Android browsers
    private val URL_BAR_ID_PATTERNS = listOf(
        ":id/url_bar",
        ":id/url_bar_title",
        ":id/location_bar_edit_text",
        ":id/search_box",
        ":id/address_bar",
        ":id/mozac_browser_toolbar_edit_text",
        ":id/url_field"
    )

    fun extractDomainFromBrowser(
        rootNode: AccessibilityNodeInfo?,
        allTexts: List<String>
    ): String {
        if (rootNode != null) {
            val urlFromBar = findUrlBarText(rootNode)
            if (!urlFromBar.isNullOrBlank()) {
                val domain = sanitizeDomain(urlFromBar)
                if (domain.isNotBlank()) return domain
            }
        }

        // Fallback: search extracted text nodes for domain patterns
        for (text in allTexts) {
            if (text.contains(".") && !text.contains(" ")) {
                val cleaned = sanitizeDomain(text)
                if (cleaned.isNotBlank()) return cleaned
            }
        }

        return "prohibited-webpage"
    }

    private fun findUrlBarText(node: AccessibilityNodeInfo): String? {
        val viewId = node.viewIdResourceName
        if (viewId != null) {
            val lowerId = viewId.lowercase(Locale.ROOT)
            if (URL_BAR_ID_PATTERNS.any { lowerId.endsWith(it) }) {
                val text = node.text?.toString() ?: node.contentDescription?.toString()
                if (!text.isNullOrBlank()) return text
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findUrlBarText(child)
            child.recycle()
            if (result != null) return result
        }

        return null
    }

    fun sanitizeDomain(input: String): String {
        var str = input.trim().lowercase(Locale.ROOT)
        if (!str.startsWith("http://") && !str.startsWith("https://")) {
            str = "https://$str"
        }
        return try {
            val uri = URI(str)
            val host = uri.host ?: str.removePrefix("https://").removePrefix("http://").split("/").firstOrNull() ?: ""
            host.removePrefix("www.")
        } catch (e: Exception) {
            input.lowercase(Locale.ROOT)
                .removePrefix("https://")
                .removePrefix("http://")
                .removePrefix("www.")
                .split("/")
                .firstOrNull() ?: input
        }
    }
}
