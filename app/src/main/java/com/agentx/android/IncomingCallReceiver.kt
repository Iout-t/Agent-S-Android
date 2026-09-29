package com.agentx.android

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import android.speech.tts.TextToSpeech
import java.util.Locale

class IncomingCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.intent.action.PHONE_STATE") return
        val state = intent.getStringExtra("state") ?: return
        if (state != "RINGING") return

        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        if (!preferences.getBoolean(KEY_AUTO_ANSWER, false)) return
        val instruction = preferences.getString(KEY_INSTRUCTION, "")?.trim().orEmpty()
        if (instruction.isEmpty()) return

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
            context.checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED
        ) {
            val telecom = context.getSystemService(TelecomManager::class.java)
            telecom?.acceptRingingCall()
        }

        lateinit var textToSpeech: TextToSpeech
        textToSpeech = TextToSpeech(context.applicationContext) { result ->
            if (result == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.getDefault()
                textToSpeech.speak(instruction, TextToSpeech.QUEUE_FLUSH, null, "dailyday-call-instruction")
            }
        }
    }

    companion object {
        const val PREFERENCES = "dailyday_call_settings"
        const val KEY_INSTRUCTION = "instruction"
        const val KEY_AUTO_ANSWER = "auto_answer"
    }
}
