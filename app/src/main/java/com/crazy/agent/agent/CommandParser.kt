package com.crazy.agent.agent

import javax.inject.Inject
import javax.inject.Singleton

/** Parses natural language commands to extract entities like app names, contacts, and actions */
@Singleton
class CommandParser @Inject constructor() {

    // Common app name mappings to package names
    private val appPackages =
            mapOf(
                    "whatsapp" to "com.whatsapp",
                    "youtube" to "com.google.android.youtube",
                    "google chat" to "com.google.android.apps.dynamite",
                    "gmail" to "com.google.android.gm",
                    "chrome" to "com.android.chrome",
                    "maps" to "com.google.android.apps.maps",
                    "instagram" to "com.instagram.android",
                    "facebook" to "com.facebook.katana",
                    "twitter" to "com.twitter.android",
                    "telegram" to "org.telegram.messenger",
                    "spotify" to "com.spotify.music",
                    "netflix" to "com.netflix.mediaclient"
            )

    /** Extract app name from command */
    fun extractAppName(command: String): String? {
        val lowerCommand = command.lowercase()

        // Try to find app name in the command
        for ((appName, _) in appPackages) {
            if (lowerCommand.contains(appName)) {
                return appName
            }
        }

        return null
    }

    /** Get package name for an app */
    fun getPackageName(appName: String): String? {
        return appPackages[appName.lowercase()]
    }

    /**
     * Extract contact/person name from command Looks for patterns like "to [name]", "find [name]",
     * "message [name]"
     */
    fun extractContactName(command: String): String? {
        val patterns =
                listOf(
                        Regex(
                                "(?:to|find|message|contact)\\s+([A-Za-z]+)",
                                RegexOption.IGNORE_CASE
                        ),
                        Regex(
                                "send\\s+(?:message\\s+)?(?:to\\s+)?([A-Za-z]+)",
                                RegexOption.IGNORE_CASE
                        )
                )

        for (pattern in patterns) {
            val match = pattern.find(command)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1]
            }
        }

        return null
    }

    /**
     * Extract message content from command Looks for patterns like "send [message]", "message
     * [content]"
     */
    fun extractMessageContent(command: String): String? {
        // Try to find quoted text first
        val quotedPattern = Regex("['\"]([^'\"]+)['\"]")
        val quotedMatch = quotedPattern.find(command)
        if (quotedMatch != null && quotedMatch.groupValues.size > 1) {
            return quotedMatch.groupValues[1]
        }

        // Try patterns like "send him/her [message]"
        val patterns =
                listOf(
                        Regex("send\\s+(?:him|her|them)\\s+(.+)", RegexOption.IGNORE_CASE),
                        Regex("message\\s+(?:saying|with)?\\s*(.+)", RegexOption.IGNORE_CASE)
                )

        for (pattern in patterns) {
            val match = pattern.find(command)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1].trim()
            }
        }

        return null
    }

    /**
     * Extract search query from command Looks for patterns like "search [query]", "find [query]",
     * "play [query]"
     */
    fun extractSearchQuery(command: String): String? {
        // Try to find quoted text first
        val quotedPattern = Regex("['\"]([^'\"]+)['\"]")
        val quotedMatch = quotedPattern.find(command)
        if (quotedMatch != null && quotedMatch.groupValues.size > 1) {
            return quotedMatch.groupValues[1]
        }

        // Try patterns
        val patterns =
                listOf(
                        Regex(
                                "(?:search|find|play|look for)\\s+(?:for\\s+)?(.+?)(?:\\s+(?:on|and|in)\\s+\\w+)?$",
                                RegexOption.IGNORE_CASE
                        ),
                        Regex("(?:search|find|play)\\s+(.+)", RegexOption.IGNORE_CASE)
                )

        for (pattern in patterns) {
            val match = pattern.find(command)
            if (match != null && match.groupValues.size > 1) {
                var query = match.groupValues[1].trim()
                // Remove trailing "and play", "and open", etc.
                query = query.replace(Regex("\\s+and\\s+\\w+$", RegexOption.IGNORE_CASE), "")
                return query
            }
        }

        return null
    }

    /** Detect if command involves sending a message */
    fun isSendMessageCommand(command: String): Boolean {
        val lowerCommand = command.lowercase()
        return lowerCommand.contains("send") ||
                lowerCommand.contains("message") ||
                lowerCommand.contains("text")
    }

    /** Detect if command involves searching */
    fun isSearchCommand(command: String): Boolean {
        val lowerCommand = command.lowercase()
        return lowerCommand.contains("search") ||
                lowerCommand.contains("find") ||
                lowerCommand.contains("play") ||
                lowerCommand.contains("look for")
    }
}
