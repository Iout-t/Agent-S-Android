package com.agentx.android

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
            hint = "Try: Open YouTube and search for bass booster songs"
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

        val credentialPanel = LinearLayout(this).apply {
            id = R.id.credential_panel
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(0, 8, 0, 16)
        }
        val credentialTitle = TextView(this).apply {
            id = R.id.credential_title
            textSize = 18f
            setTextColor(Color.BLACK)
        }
        val credentialReason = TextView(this).apply {
            id = R.id.credential_reason
            setTextColor(Color.DKGRAY)
            setPadding(0, 4, 0, 8)
        }
        val username = EditText(this).apply {
            id = R.id.username_input
            hint = "Username or email"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val password = EditText(this).apply {
            id = R.id.password_input
            hint = "Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val continueButton = Button(this).apply {
            id = R.id.continue_button
            text = "Continue securely"
        }
        val cancelButton = Button(this).apply {
            id = R.id.cancel_button
            text = "Cancel"
        }
        credentialPanel.addView(credentialTitle)
        credentialPanel.addView(credentialReason)
        credentialPanel.addView(username)
        credentialPanel.addView(password)
        credentialPanel.addView(continueButton)
        credentialPanel.addView(cancelButton)

        val title = TextView(this).apply {
            text = "DailyDay"
            textSize = 28f
            setTextColor(Color.BLACK)
        }
        val subtitle = TextView(this).apply {
            text = "DailyDay autonomous agent"
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
            addView(credentialPanel)
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
                results.visibility = if (state.searchUrl == null) View.GONE else View.VISIBLE
                state.searchUrl?.let { url ->
                    results.loadUrl(url)
                }
                state.credentialRequest?.let { request ->
                    credentialPanel.visibility = View.VISIBLE
                    credentialTitle.text = "Sign in to ${request.appLabel}"
                    credentialReason.text = request.reason
                } ?: run {
                    credentialPanel.visibility = View.GONE
                    username.text?.clear()
                    password.text?.clear()
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
        continueButton.setOnClickListener {
            viewModel.continueWithCredentials(
                username = username.text.toString(),
                password = password.text.toString()
            )
        }
        cancelButton.setOnClickListener { viewModel.cancelCredentialRequest() }
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
