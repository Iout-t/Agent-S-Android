package com.agentx.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskPlannerTest {
    @Test
    fun searchInstructionProducesGoogleSearchPlan() {
        val plan = TaskPlanner.plan("Search the web for Agent S GitHub")

        assertEquals(TaskKind.WEB_SEARCH, plan.kind)
        assertEquals("Agent S GitHub", plan.query)
        assertEquals("https://www.google.com/search?q=Agent+S+GitHub", plan.url)
    }

    @Test
    fun ordinaryInstructionDelegatesToBackend() {
        val plan = TaskPlanner.plan("Open the settings app and enable dark mode")

        assertEquals(TaskKind.BACKEND_RUN, plan.kind)
        assertEquals(null, plan.url)
        assertTrue(plan.summary.contains("backend"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankInstructionIsRejected() {
        TaskPlanner.plan("   ")
    }
}
