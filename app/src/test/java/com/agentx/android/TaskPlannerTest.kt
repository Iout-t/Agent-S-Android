package com.agentx.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun youtubeInstructionProducesAppTaskInsteadOfGoogleSearch() {
        val plan = TaskPlanner.plan("Open YouTube and search for bass booster songs")

        assertEquals(TaskKind.APP_TASK, plan.kind)
        assertEquals("YouTube", plan.requestedApp)
        assertEquals("com.google.android.youtube", plan.preferredPackage)
        assertEquals("bass booster songs", plan.query)
        assertTrue(plan.url == null)
    }

    @Test
    fun otherInstalledAppRequestsUseTheSameRoutingPath() {
        val plan = TaskPlanner.plan("Launch Spotify and search for ambient focus music")

        assertEquals(TaskKind.APP_TASK, plan.kind)
        assertEquals("Spotify", plan.requestedApp)
        assertEquals("com.spotify.music", plan.preferredPackage)
        assertEquals("ambient focus music", plan.query)
    }

    @Test
    fun unknownLauncherAppsAreStillRecognizedByName() {
        val plan = TaskPlanner.plan("Open My Notes and create a new note")

        assertEquals(TaskKind.APP_TASK, plan.kind)
        assertEquals("My Notes", plan.requestedApp)
        assertEquals(null, plan.preferredPackage)
    }

    @Test
    fun signInInstructionRequestsEphemeralCredentials() {
        val plan = TaskPlanner.plan("Open YouTube and sign in with my username and password")

        assertEquals(TaskKind.APP_TASK, plan.kind)
        assertTrue(plan.requiresCredentials)
        assertFalse(plan.credentialReason.isNullOrBlank())
    }

    @Test
    fun ordinaryInstructionDelegatesToBackend() {
        val plan = TaskPlanner.plan("Enable dark mode across the phone")

        assertEquals(TaskKind.BACKEND_RUN, plan.kind)
        assertEquals(null, plan.url)
        assertTrue(plan.summary.contains("DailyDay"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankInstructionIsRejected() {
        TaskPlanner.plan("   ")
    }
}
