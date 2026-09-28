package com.agentx.android

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: AgentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val input = EditText(this).apply {
            id = R.id.task_input
            hint = "Try: Search the web for Agent S GitHub"
            setSingleLine(false)
            minLines = 2
        }
        val run = Button(this).apply {
            id = R.id.run_button
            text = "Run task"
        }
        val status = TextView(this).apply {
            id = R.id.status_text
            text = "Ready"
            setTextColor(Color.DKGRAY)
            setPadding(0, 16, 0, 16)
        }
        val results = WebView(this).apply {
            id = R.id.results_webview
            visibility = View.GONE
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setSupportZoom(true)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        val title = TextView(this).apply {
            text = "AgentX"
            textSize = 28f
            setTextColor(Color.BLACK)
        }
        val subtitle = TextView(this).apply {
            text = "Agent S-inspired task planner and browser executor"
            setTextColor(Color.DKGRAY)
            setPadding(0, 4, 0, 20)
        }
        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 16)
            addView(title)
            addView(subtitle)
            addView(input)
            addView(run)
            addView(status)
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(controls)
            addView(results)
        }
        setContentView(root)

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                status.text = state.message
                run.isEnabled = !state.running
                state.searchUrl?.let { url ->
                    results.visibility = View.VISIBLE
                    results.loadUrl(url)
                }
            }
        }

        run.setOnClickListener {
            val prompt = input.text.toString().trim()
            if (prompt.isEmpty()) {
                Snackbar.make(root, "Enter an instruction first", Snackbar.LENGTH_SHORT).show()
            } else {
                viewModel.run(prompt)
            }
        }
    }

    override fun onBackPressed() {
        val results = findViewById<WebView>(R.id.results_webview)
        if (results.visibility == View.VISIBLE && results.canGoBack()) {
            results.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
