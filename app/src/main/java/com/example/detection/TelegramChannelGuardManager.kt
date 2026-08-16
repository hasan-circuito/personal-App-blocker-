package com.example.detection

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages Telegram channel context extraction, session-level restrictions,
 * and immediate re-entry prevention without application lockout.
 */
object TelegramChannelGuardManager {

    private const val TAG = "TelegramChannelGuard"
    private const val DEFAULT_RESTRICTION_DURATION_MS = 15 * 60 * 1000L // 15 minutes temporary restriction cooldown

    data class TelegramChannelContext(
        val channelIdentifier: String,
        val title: String,
        val username: String? = null,
        val identifierSource: String, // "ACCESSIBILITY_USERNAME", "ACCESSIBILITY_TEXT", "TEMPORARY_UI_CONTEXT"
        val detectedTimestamp: Long = System.currentTimeMillis()
    )

    data class RestrictedTelegramMediaContext(
        val channelIdentifier: String,
        val channelTitle: String,
        val username: String? = null,
        val detectionTimestamp: Long,
        val expiryTimestamp: Long,
        val reason: String = "Confirmed Visual Adult Content",
        val confidence: Float
    )

    private val _currentChannelContext = MutableStateFlow<TelegramChannelContext?>(null)
    val currentChannelContext: StateFlow<TelegramChannelContext?> = _currentChannelContext.asStateFlow()

    private val _activeRestrictions = MutableStateFlow<List<RestrictedTelegramMediaContext>>(emptyList())
    val activeRestrictions: StateFlow<List<RestrictedTelegramMediaContext>> = _activeRestrictions.asStateFlow()

    private val restrictionsMap = ConcurrentHashMap<String, RestrictedTelegramMediaContext>()
    private var lastReentryInterventionTime = 0L

    /**
     * Extracts Telegram channel title/username/identifier from AccessibilityNode hierarchy.
     */
    fun extractChannelContext(rootNode: AccessibilityNodeInfo?, targetPackage: String = "org.telegram.messenger"): TelegramChannelContext {
        if (rootNode == null) {
            val fallback = TelegramChannelContext(
                channelIdentifier = "UNAVAILABLE",
                title = "Telegram Media Context",
                username = null,
                identifierSource = "TEMPORARY_UI_CONTEXT"
            )
            _currentChannelContext.value = fallback
            return fallback
        }

        val textNodes = mutableListOf<String>()
        extractHeaderAndVisibleTexts(rootNode, textNodes)

        // 1. Search for public channel username (@example_channel)
        var foundUsername: String? = null
        for (text in textNodes) {
            val trimmed = text.trim()
            if (trimmed.startsWith("@") && trimmed.length >= 3 && !trimmed.contains(" ")) {
                foundUsername = trimmed
                break
            }
        }

        // 2. Search for prominent channel title
        var foundTitle: String? = null
        for (text in textNodes) {
            val trimmed = text.trim()
            if (trimmed.isNotBlank() &&
                !trimmed.startsWith("@") &&
                !trimmed.contains("messages", ignoreCase = true) &&
                !trimmed.contains("online", ignoreCase = true) &&
                !trimmed.contains("subscribers", ignoreCase = true) &&
                !trimmed.contains("members", ignoreCase = true) &&
                !trimmed.contains("views", ignoreCase = true) &&
                trimmed.length in 3..60
            ) {
                foundTitle = trimmed
                break
            }
        }

        val channelTitle = foundTitle ?: "Telegram Channel"
        val identifier: String
        val source: String

        when {
            foundUsername != null -> {
                identifier = foundUsername
                source = "ACCESSIBILITY_USERNAME"
            }
            foundTitle != null -> {
                identifier = "title:${foundTitle.lowercase().replace(" ", "_")}"
                source = "ACCESSIBILITY_TEXT"
            }
            else -> {
                val uiHash = Math.abs(textNodes.hashCode()).toString(16)
                identifier = "session_ui_$uiHash"
                source = "TEMPORARY_UI_CONTEXT"
            }
        }

        val result = TelegramChannelContext(
            channelIdentifier = identifier,
            title = channelTitle,
            username = foundUsername,
            identifierSource = source
        )

        _currentChannelContext.value = result

        Log.i(TAG, """
[CHANNEL_CONTEXT]
identifier=$identifier
title="$channelTitle"
username=${foundUsername ?: "none"}
source=$source
        """.trimIndent())

        return result
    }

    /**
     * Adds channel to temporary restricted list.
     */
    fun restrictChannelContext(
        channelContext: TelegramChannelContext,
        reason: String = "Confirmed Visual Adult Content",
        confidence: Float,
        durationMs: Long = DEFAULT_RESTRICTION_DURATION_MS
    ): RestrictedTelegramMediaContext {
        cleanExpiredRestrictions()

        val now = System.currentTimeMillis()
        val expiry = now + durationMs

        val restriction = RestrictedTelegramMediaContext(
            channelIdentifier = channelContext.channelIdentifier,
            channelTitle = channelContext.title,
            username = channelContext.username,
            detectionTimestamp = now,
            expiryTimestamp = expiry,
            reason = reason,
            confidence = confidence
        )

        restrictionsMap[channelContext.channelIdentifier] = restriction
        if (channelContext.username != null) {
            restrictionsMap[channelContext.username] = restriction
        }
        if (channelContext.title.isNotBlank()) {
            restrictionsMap["title:${channelContext.title.lowercase().replace(" ", "_")}"] = restriction
        }

        _activeRestrictions.value = restrictionsMap.values.distinctBy { it.channelIdentifier }

        val expiryStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(expiry))
        Log.i(TAG, """
[CHANNEL_RESTRICTION]
state=ACTIVE
identifier=${channelContext.channelIdentifier}
title="${channelContext.title}"
expiry=$expiryStr
reason=$reason
        """.trimIndent())

        VisualDiagnosticPipeline.logVisualGuard("""
[CHANNEL_RESTRICTION]
state=ACTIVE
identifier=${channelContext.channelIdentifier}
expiry=$expiryStr
        """.trimIndent())

        return restriction
    }

    /**
     * Checks if a channel identifier is under active temporary restriction.
     */
    fun isChannelRestricted(identifier: String): Boolean {
        cleanExpiredRestrictions()
        val restriction = restrictionsMap[identifier] ?: return false
        return System.currentTimeMillis() < restriction.expiryTimestamp
    }

    /**
     * Checks if current Telegram screen matches any restricted channel and immediately backs out.
     */
    fun checkAndEnforceReentryRestriction(
        service: AccessibilityService,
        rootNode: AccessibilityNodeInfo?,
        targetPackage: String
    ): Boolean {
        cleanExpiredRestrictions()
        if (restrictionsMap.isEmpty()) return false

        val channelContext = extractChannelContext(rootNode, targetPackage)
        val isRestricted = isChannelRestricted(channelContext.channelIdentifier) ||
                (channelContext.username != null && isChannelRestricted(channelContext.username)) ||
                isChannelRestricted("title:${channelContext.title.lowercase().replace(" ", "_")}")

        if (isRestricted) {
            val now = System.currentTimeMillis()
            if (now - lastReentryInterventionTime < 1000L) {
                // Throttled
                return true
            }
            lastReentryInterventionTime = now

            Log.w(TAG, """
[CHANNEL_REENTRY]
detected=true
identifier=${channelContext.channelIdentifier}
action=EXIT_CHANNEL (GLOBAL_ACTION_BACK)
            """.trimIndent())

            VisualDiagnosticPipeline.logVisualGuard("""
[CHANNEL_REENTRY]
detected=true
action=EXIT_CHANNEL (GLOBAL_ACTION_BACK)
            """.trimIndent())

            // Immediately exit restricted channel back to chats list
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            return true
        }

        return false
    }

    private fun cleanExpiredRestrictions() {
        val now = System.currentTimeMillis()
        var changed = false
        val iterator = restrictionsMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.expiryTimestamp <= now) {
                iterator.remove()
                changed = true
            }
        }
        if (changed) {
            _activeRestrictions.value = restrictionsMap.values.distinctBy { it.channelIdentifier }
        }
    }

    fun clearAllRestrictions() {
        restrictionsMap.clear()
        _activeRestrictions.value = emptyList()
        _currentChannelContext.value = null
        Log.i(TAG, "All Telegram channel restrictions cleared.")
    }

    private fun extractHeaderAndVisibleTexts(node: AccessibilityNodeInfo, textList: MutableList<String>) {
        node.text?.let {
            val s = it.toString().trim()
            if (s.isNotBlank() && !textList.contains(s)) textList.add(s)
        }
        node.contentDescription?.let {
            val s = it.toString().trim()
            if (s.isNotBlank() && !textList.contains(s)) textList.add(s)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            extractHeaderAndVisibleTexts(child, textList)
            child.recycle()
        }
    }
}
