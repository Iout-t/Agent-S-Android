package com.agentx.android

/** Parsed, user-requested UI action for the optional accessibility service. */
data class AccessibilityCommand(
    val action: Action,
    val target: String? = null,
    val text: String? = null,
    val startX: Float? = null,
    val startY: Float? = null,
    val endX: Float? = null,
    val endY: Float? = null
) {
    enum class Action { CLICK, SWIPE, TYPE }
}

object AccessibilityCommandParser {
    private val coordinateSwipe = Regex(
        "(?:swipe|drag)\\s+(?:from\\s+)?(\\d+(?:\\.\\d+)?)\\s*[, ]\\s*(\\d+(?:\\.\\d+)?)\\s+(?:to|toward)\\s+(\\d+(?:\\.\\d+)?)\\s*[, ]\\s*(\\d+(?:\\.\\d+)?)",
        RegexOption.IGNORE_CASE
    )
    private val quotedText = Regex(
        "(?:type|enter|write|input)\\s+(?:text\\s+)?[\\\"'](.+?)[\\\"']",
        RegexOption.IGNORE_CASE
    )

    fun parse(instruction: String): AccessibilityCommand? {
        val normalized = instruction.trim().replace(Regex("\\s+"), " ")
        coordinateSwipe.find(normalized)?.let { match ->
            return AccessibilityCommand(
                action = AccessibilityCommand.Action.SWIPE,
                startX = match.groupValues[1].toFloat(),
                startY = match.groupValues[2].toFloat(),
                endX = match.groupValues[3].toFloat(),
                endY = match.groupValues[4].toFloat()
            )
        }

        val lower = normalized.lowercase()
        if (lower.contains("swipe up")) return directionalSwipe(0f, -1f)
        if (lower.contains("swipe down")) return directionalSwipe(0f, 1f)
        if (lower.contains("swipe left")) return directionalSwipe(-1f, 0f)
        if (lower.contains("swipe right")) return directionalSwipe(1f, 0f)

        quotedText.find(normalized)?.let { match ->
            return AccessibilityCommand(
                action = AccessibilityCommand.Action.TYPE,
                text = match.groupValues[1]
            )
        }

        Regex("(?:type|enter|write|input)\\s+(?:text\\s+)?(.+)$", RegexOption.IGNORE_CASE)
            .find(normalized)?.let { match ->
                return AccessibilityCommand(
                    action = AccessibilityCommand.Action.TYPE,
                    text = match.groupValues[1].trim().trimEnd('.')
                )
            }

        Regex("(?:tap|click|press|select)\\s+(?:on\\s+)?(?:the\\s+)?(.+)$", RegexOption.IGNORE_CASE)
            .find(normalized)?.let { match ->
                val target = match.groupValues[1]
                    .trim()
                    .trimEnd('.', '!', '?')
                    .replace(Regex("\\s+button$", RegexOption.IGNORE_CASE), "")
                    .trim()
                return AccessibilityCommand(
                    action = AccessibilityCommand.Action.CLICK,
                    target = target
                )
            }

        return null
    }

    private fun directionalSwipe(horizontal: Float, vertical: Float): AccessibilityCommand =
        AccessibilityCommand(
            action = AccessibilityCommand.Action.SWIPE,
            startX = 540f - horizontal * 260f,
            startY = 1200f - vertical * 500f,
            endX = 540f + horizontal * 260f,
            endY = 1200f + vertical * 500f
        )
}
