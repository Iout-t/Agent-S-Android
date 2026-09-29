package com.agentx.android

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

enum class TaskKind {
    APP_TASK,
    DEVICE_TASK,
    FORM_TASK,
    WEB_SEARCH,
    BACKEND_RUN
}

data class TaskPlan(
    val kind: TaskKind,
    val instruction: String,
    val query: String? = null,
    val url: String? = null,
    val requestedApp: String? = null,
    val preferredPackage: String? = null,
    val actionDescription: String = instruction,
    val requiresCredentials: Boolean = false,
    val credentialReason: String? = null,
    val automationAction: AutomationAction? = null,
    val summary: String
)

object TaskPlanner {
    private val googleFormLinkPattern = Regex(
        "https?://(?:docs\\.google\\.com/forms|forms\\.gle)[^\\s]+",
        RegexOption.IGNORE_CASE
    )
    private val appSearchPattern = Regex(
        "^(?:please\\s+)?(?:open|launch|use|go\\s+to|visit)\\s+(.+?)\\s+(?:and\\s+)?(?:search|find|look\\s+up)\\s+(?:for\\s+)?(.+)$",
        RegexOption.IGNORE_CASE
    )
    private val searchInAppPattern = Regex(
        "^(?:please\\s+)?(?:search|find|look\\s+up)\\s+(?:for\\s+)?(.+?)\\s+(?:on|in)\\s+([A-Za-z0-9][A-Za-z0-9 ._-]*)$",
        RegexOption.IGNORE_CASE
    )
    private val appPlayPattern = Regex(
        "^(?:please\\s+)?(?:open|launch|use|go\\s+to|visit)\\s+(.+?)\\s+(?:and\\s+)?play\\s+(.+)$",
        RegexOption.IGNORE_CASE
    )
    private val appActionPattern = Regex(
        "^(?:please\\s+)?(?:open|launch|use|go\\s+to|visit)\\s+(.+?)\\s+and\\s+(.+)$",
        RegexOption.IGNORE_CASE
    )
    private val appOnlyPattern = Regex(
        "^(?:please\\s+)?(?:open|launch|use|go\\s+to|visit)\\s+([A-Za-z0-9][A-Za-z0-9 ._-]*)$",
        RegexOption.IGNORE_CASE
    )
    private val searchPrefix = Regex(
        "^(?:please\\s+)?(?:search|look\\s+up|find|google|browse)(?:\\s+the\\s+web)?(?:\\s+(?:for|about|on))?\\s*",
        RegexOption.IGNORE_CASE
    )
    private val knownPackages = mapOf(
        "youtube" to "com.google.android.youtube",
        "chrome" to "com.android.chrome",
        "google chrome" to "com.android.chrome",
        "maps" to "com.google.android.apps.maps",
        "google maps" to "com.google.android.apps.maps",
        "gmail" to "com.google.android.gm",
        "spotify" to "com.spotify.music",
        "whatsapp" to "com.whatsapp",
        "telegram" to "org.telegram.messenger",
        "instagram" to "com.instagram.android",
        "facebook" to "com.facebook.katana",
        "netflix" to "com.netflix.mediaclient",
        "amazon" to "com.amazon.mShop.android.shopping",
        "slack" to "com.Slack",
        "zoom" to "us.zoom.videomeetings"
    )

    fun plan(instruction: String): TaskPlan {
        val cleanInstruction = instruction.trim().replace(Regex("\\s+"), " ")
        require(cleanInstruction.isNotEmpty()) { "Instruction cannot be empty" }

        googleFormLinkPattern.find(cleanInstruction)?.value?.let { rawLink ->
            val link = rawLink.trimEnd('.', ',', ')', ']')
            return TaskPlan(
                kind = TaskKind.FORM_TASK,
                instruction = cleanInstruction,
                url = link,
                actionDescription = "open the Google Form",
                summary = "Google Form ready for guided completion"
            )
        }

        AutomationAction.from(cleanInstruction)?.let { action ->
            return TaskPlan(
                kind = TaskKind.DEVICE_TASK,
                instruction = cleanInstruction,
                actionDescription = action.label,
                automationAction = action,
                summary = "Device automation ready: ${action.label}"
            )
        }

        searchInAppPattern.matchEntire(cleanInstruction)?.let { match ->
            val query = match.groupValues[1].trim()
            val app = match.groupValues[2].trim()
            return appTask(cleanInstruction, app, query, "search for $query")
        }

        appPlayPattern.matchEntire(cleanInstruction)?.let { match ->
            val app = match.groupValues[1].trim()
            val query = match.groupValues[2]
                .trim()
                .replace("'", "")
                .replace("\"", "")
                .removeSuffix(".")
                .trim()
            return appTask(cleanInstruction, app, query, "play $query")
        }

        appSearchPattern.matchEntire(cleanInstruction)?.let { match ->
            val app = match.groupValues[1].trim()
            val query = match.groupValues[2].trim()
            return appTask(cleanInstruction, app, query, "search for $query")
        }

        appActionPattern.matchEntire(cleanInstruction)?.let { match ->
            val app = match.groupValues[1].trim()
            val action = match.groupValues[2].trim()
            return appTask(cleanInstruction, app, null, action)
        }

        appOnlyPattern.matchEntire(cleanInstruction)?.let { match ->
            val app = match.groupValues[1].trim()
            return appTask(cleanInstruction, app, null, "open $app")
        }

        val lowered = cleanInstruction.lowercase()
        val isSearch = listOf("search", "look up", "find", "google", "browse the web", "web search")
            .any(lowered::contains)
        if (isSearch) {
            val query = cleanInstruction
                .replace(searchPrefix, "")
                .replace(Regex("^web\\s+search\\s*", RegexOption.IGNORE_CASE), "")
                .replace(Regex("^(?:the\\s+)?web\\s+(?:for|about)\\s+", RegexOption.IGNORE_CASE), "")
                .trim()
                .ifEmpty { cleanInstruction }
            return TaskPlan(
                kind = TaskKind.WEB_SEARCH,
                instruction = cleanInstruction,
                query = query,
                url = googleUrl(query),
                actionDescription = "Search the web for $query",
                summary = "Web search ready for \"$query\""
            )
        }

        return TaskPlan(
            kind = TaskKind.BACKEND_RUN,
            instruction = cleanInstruction,
            summary = "Delegating this task to the Agent X backend"
        )
    }

    private fun appTask(instruction: String, app: String, query: String?, action: String): TaskPlan {
        val credentialReason = credentialReason(instruction)
        return TaskPlan(
            kind = TaskKind.APP_TASK,
            instruction = instruction,
            query = query,
            requestedApp = app,
            preferredPackage = knownPackages[app.lowercase()],
            actionDescription = action,
            requiresCredentials = credentialReason != null,
            credentialReason = credentialReason,
            summary = if (query == null) "App task ready: $app → $action" else "App task ready: $app → $query"
        )
    }

    fun credentialReason(instruction: String): String? {
        val lowered = instruction.lowercase()
        return when {
            listOf("username", "password", "log in", "login", "sign in", "sign-in", "credentials")
                .any(lowered::contains) -> "This task may require a sign-in. Agent X will ask for credentials only for this run."
            else -> null
        }
    }

    fun googleUrl(query: String): String {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        return "https://www.google.com/search?q=$encodedQuery"
    }

    fun appWebUrl(app: String?, query: String): String? {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        return when (app?.lowercase()) {
            "youtube" -> "https://www.youtube.com/results?search_query=$encodedQuery"
            "spotify" -> "https://open.spotify.com/search/$encodedQuery"
            "maps", "google maps" -> "https://www.google.com/maps/search/?api=1&query=$encodedQuery"
            "amazon" -> "https://www.amazon.com/s?k=$encodedQuery"
            else -> null
        }
    }
}
