package com.agentx.android

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Locale

/**
 * Performs explicit user-requested UI actions after the user enables Agent X in
 * Accessibility settings. It does not inspect or act on unrelated screens.
 */
class DailyDayAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    fun execute(instruction: String): Boolean {
        val command = AccessibilityCommandParser.parse(instruction) ?: return false
        return when (command.action) {
            AccessibilityCommand.Action.CLICK -> clickTarget(command.target.orEmpty())
            AccessibilityCommand.Action.TYPE -> setText(command.text.orEmpty())
            AccessibilityCommand.Action.SWIPE -> swipe(
                command.startX ?: 0f,
                command.startY ?: 0f,
                command.endX ?: 0f,
                command.endY ?: 0f
            )
        }
    }

    private fun clickTarget(target: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val normalizedTarget = target.lowercase(Locale.getDefault()).trim()
        val node = findNode(root) { candidate ->
            val label = nodeLabel(candidate).lowercase(Locale.getDefault())
            label == normalizedTarget || label.contains(normalizedTarget)
        } ?: return false

        var clickable: AccessibilityNodeInfo? = node
        while (clickable != null && !clickable.isClickable) clickable = clickable.parent
        if (clickable?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) return true

        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        return tap(bounds.centerX().toFloat(), bounds.centerY().toFloat())
    }

    private fun setText(value: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        val target = focused ?: findNode(root) { it.isEditable }
        ?: return false
        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value)
        }
        return target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    private fun tap(x: Float, y: Float): Boolean {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 80))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    private fun swipe(startX: Float, startY: Float, endX: Float, endY: Float): Boolean {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 400))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    private fun findNode(
        node: AccessibilityNodeInfo,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): AccessibilityNodeInfo? {
        if (predicate(node)) return node
        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            val match = findNode(child, predicate)
            if (match != null) return match
        }
        return null
    }

    private fun nodeLabel(node: AccessibilityNodeInfo): String = listOfNotNull(
        node.text?.toString(),
        node.contentDescription?.toString(),
        node.hintText?.toString()
    ).joinToString(" ")

    companion object {
        @Volatile
        var instance: DailyDayAccessibilityService? = null
            private set
    }
}
