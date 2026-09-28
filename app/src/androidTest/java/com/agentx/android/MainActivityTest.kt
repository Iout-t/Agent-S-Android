package com.agentx.android

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun searchTaskIsPlannedAndBrowserSurfaceAppears() {
        onView(withId(R.id.task_input))
            .perform(replaceText("Search the web for Agent S GitHub"))
        onView(withId(R.id.run_button)).perform(click())

        onView(withId(R.id.status_text))
            .check(matches(withText(containsString("Agent S GitHub"))))
        onView(withId(R.id.results_webview)).check(matches(isDisplayed()))
    }
}
