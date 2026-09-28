package com.agentx.android

import android.os.Bundle
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
        val input = EditText(this).apply { hint = "Tell AgentX what to do" }
        val run = Button(this).apply { text = "Run automation" }
        val status = TextView(this).apply { text = "Connecting…" }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
            addView(TextView(context).apply { text = "AgentX"; textSize = 28f })
            addView(input)
            addView(run)
            addView(status)
        }
        setContentView(root)
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                status.text = state.message
                run.isEnabled = !state.running
            }
        }
        run.setOnClickListener {
            val prompt = input.text.toString().trim()
            if (prompt.isEmpty()) Snackbar.make(root, "Enter an instruction first", Snackbar.LENGTH_SHORT).show()
            else viewModel.run(prompt)
        }
    }
}
