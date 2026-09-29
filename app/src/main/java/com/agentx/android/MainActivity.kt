package com.agentx.android
import android.Manifest
import android.app.role.RoleManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.CheckBox
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
        val assistantButton = Button(this).apply {
            id = R.id.assistant_button
            text = "Set DailyDay as default assistant"
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
        val saveCredentials = CheckBox(this).apply {
            id = R.id.save_credentials
            text = "Save encrypted credentials for this app on this phone"
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
        credentialPanel.addView(saveCredentials)
        credentialPanel.addView(continueButton)
        credentialPanel.addView(cancelButton)

        val callHeading = TextView(this).apply {
            text = "Incoming calls"
            textSize = 20f
            setTextColor(Color.BLACK)
            setPadding(0, 24, 0, 4)
        }
        val callInstruction = EditText(this).apply {
            id = R.id.call_instruction_input
            hint = "What DailyDay should say after answering"
            setSingleLine(false)
            minLines = 2
        }
        val autoAnswer = CheckBox(this).apply {
            id = R.id.auto_answer_calls
            text = "Enable automatic answer and speak this instruction"
        }
        val saveCallSettings = Button(this).apply {
            id = R.id.save_call_settings
            text = "Save call settings"
        }
        val callHelp = TextView(this).apply {
            text = "When enabled, DailyDay can answer an incoming call and speak your instruction. Android will ask for phone permissions before this can work."
            setTextColor(Color.DKGRAY)
            setPadding(0, 4, 0, 8)
        }
        val callPreferences = getSharedPreferences(IncomingCallReceiver.PREFERENCES, MODE_PRIVATE)
        callInstruction.setText(callPreferences.getString(IncomingCallReceiver.KEY_INSTRUCTION, ""))
        autoAnswer.isChecked = callPreferences.getBoolean(IncomingCallReceiver.KEY_AUTO_ANSWER, false)

        val callSection = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.rgb(240, 244, 248))
            addView(callHeading)
            addView(callHelp)
            addView(callInstruction)
            addView(autoAnswer)
            addView(saveCallSettings)
        }

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
            addView(assistantButton)
            addView(status)
            addView(credentialPanel)
            addView(callSection)
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
                    credentialReason.text = if (request.savedAvailable) {
                        "Encrypted credentials are available for this app. ${request.reason}"
                    } else {
                        request.reason
                    }
                } ?: run {
                    credentialPanel.visibility = View.GONE
                    username.text?.clear()
                    password.text?.clear()
                    saveCredentials.isChecked = false
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
                password = password.text.toString(),
                saveForFuture = saveCredentials.isChecked
            )
        }
        cancelButton.setOnClickListener { viewModel.cancelCredentialRequest() }
        assistantButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(RoleManager::class.java)
                when {
                    roleManager == null || !roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT) ->
                        Snackbar.make(root, "This Android build does not expose the assistant role", Snackbar.LENGTH_LONG).show()
                    roleManager.isRoleHeld(RoleManager.ROLE_ASSISTANT) ->
                        Snackbar.make(root, "DailyDay is already the default assistant", Snackbar.LENGTH_SHORT).show()
                    else -> startActivityForResult(
                        roleManager.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT),
                        ASSISTANT_ROLE_REQUEST
                    )
                }
            } else {
                Snackbar.make(root, "Default assistant selection requires Android 10 or newer", Snackbar.LENGTH_LONG).show()
            }
        }
        saveCallSettings.setOnClickListener {
            if (autoAnswer.isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ANSWER_PHONE_CALLS),
                    CALL_PERMISSION_REQUEST
                )
            }
            callPreferences.edit()
                .putString(IncomingCallReceiver.KEY_INSTRUCTION, callInstruction.text.toString().trim())
                .putBoolean(IncomingCallReceiver.KEY_AUTO_ANSWER, autoAnswer.isChecked)
                .apply()
            Snackbar.make(root, "Incoming-call settings saved", Snackbar.LENGTH_SHORT).show()
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

    private companion object {
        const val ASSISTANT_ROLE_REQUEST = 4101
        const val CALL_PERMISSION_REQUEST = 4102
    }
}
