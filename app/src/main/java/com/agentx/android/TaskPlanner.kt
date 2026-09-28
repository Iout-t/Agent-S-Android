package com.agentx.android

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Android-native subset of Agent S's task loop: interpret a natural-language
 * instruction, choose an action, and produce a verifiable execution target.
 */
enum class TaskKind {
    WEB_SEARCH,
    BACKEND_RUN
}

data class TaskPlan(
    val kind: TaskKind,
    val instruction: String,
    val query: String? = null,
    val url: String? = null,
    val summary: String
)

object TaskPlanner {
    private val searchPrefix = Regex(
        "^(?:please\\s+)?(?:search|look\\s+up|find|google|browse)(?:\\s+the\\s+web)?(?:\\s+(?:for|about|on))?\\s*",
        RegexOption.IGNORE_CASE
    )

    fun plan(instruction: String): TaskPlan {
        val cleanInstruction = instruction.trim().replace(Regex("\\s+"), " ")
        require(cleanInstruction.isNotEmpty()) { "Instruction cannot be empty" }

        val lowered = cleanInstruction.lowercase()
        val isSearch = listOf("search", "look up", "find", "google", "browse the web", "web search")
            .any(lowered::contains)

        if (!isSearch) {
            return TaskPlan(
                kind = TaskKind.BACKEND_RUN,
                instruction = cleanInstruction,
                summary = "Delegating this task to the Agent X backend"
            )
        }

        val query = cleanInstruction
            .replace(searchPrefix, "")
            .replace(Regex("^web\\s+search\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^(?:the\\s+)?web\\s+(?:for|about)\\s+", RegexOption.IGNORE_CASE), "")
            .trim()
            .ifEmpty { cleanInstruction }

        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        return TaskPlan(
            kind = TaskKind.WEB_SEARCH,
            instruction = cleanInstruction,
            query = query,
            url = "https://www.google.com/search?q=$encodedQuery",
            summary = "Search the web for \"$query\""
        )
    }
}
