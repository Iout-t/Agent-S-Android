package com.agentx.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityCommandTest {
    @Test
    fun parsesTapTargetAndRemovesButtonSuffix() {
        val command = AccessibilityCommandParser.parse("Use accessibility to tap the Confirm button")

        assertNotNull(command)
        assertEquals(AccessibilityCommand.Action.CLICK, command?.action)
        assertEquals("Confirm", command?.target)
    }

    @Test
    fun parsesQuotedTextEntry() {
        val command = AccessibilityCommandParser.parse("Type 'Alone by Marshmello' in the search field")

        assertNotNull(command)
        assertEquals(AccessibilityCommand.Action.TYPE, command?.action)
        assertEquals("Alone by Marshmello", command?.text)
    }

    @Test
    fun parsesDirectionalSwipe() {
        val command = AccessibilityCommandParser.parse("Swipe up")

        assertNotNull(command)
        assertEquals(AccessibilityCommand.Action.SWIPE, command?.action)
        assertTrue((command?.endY ?: 0f) < (command?.startY ?: 0f))
    }

    @Test
    fun ignoresUnrecognizedInstructions() {
        assertNull(AccessibilityCommandParser.parse("Open Spotify and search for music"))
    }
}
