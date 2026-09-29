package com.agentx.android

import android.os.Bundle
import android.content.Intent
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService

class DailyDayVoiceSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle): VoiceInteractionSession =
        object : VoiceInteractionSession(this) {
            override fun onShow(args: Bundle?, showFlags: Int) {
                super.onShow(args, showFlags)
                startActivity(
                    Intent(this@DailyDayVoiceSessionService, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                hide()
            }
        }
}
