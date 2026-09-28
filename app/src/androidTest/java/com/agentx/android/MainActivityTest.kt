package com.agentx.android

import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun searchTaskIsPlannedAndBrowserSurfaceAppears() {
        activityRule.scenario.onActivity { activity ->
            val input = activity.findViewById<EditText>(R.id.task_input)
            val run = activity.findViewById<Button>(R.id.run_button)
            val status = activity.findViewById<TextView>(R.id.status_text)
            val results = activity.findViewById<WebView>(R.id.results_webview)

            input.setText("Search the web for Agent S GitHub")
            run.performClick()

            assertTrue(status.text.toString().contains("Agent S GitHub"))
            assertEquals(View.VISIBLE, results.visibility)
            assertTrue(results.url.orEmpty().contains("google.com/search"))
        }
    }
}
