package com.agentx.android

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/** User-enabled service boundary for UI automation; it never runs until enabled in Android settings. */
class DailyDayAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit
}
