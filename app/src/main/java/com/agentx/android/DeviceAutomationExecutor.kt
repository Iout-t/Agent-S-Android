package com.agentx.android

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings

class DeviceAutomationExecutor(context: Context) {
    private val appContext = context.applicationContext

    fun execute(plan: TaskPlan): AppExecutionResult {
        val action = plan.automationAction
            ?: return AppExecutionResult(false, message = "No device automation action was recognized.")
        if (action == AutomationAction.UI_AUTOMATION) {
            val service = DailyDayAccessibilityService.instance
                ?: return AppExecutionResult(
                    launched = false,
                    message = "Enable Agent X in Android Accessibility settings before running tap, swipe, or text actions."
                )
            val executed = service.execute(plan.instruction)
            return AppExecutionResult(
                launched = executed,
                message = if (executed) {
                    "Accessibility action completed."
                } else {
                    "Accessibility could not find the requested target or action."
                }
            )
        }
        val intent = settingsIntent(action)
        return try {
            if (action == AutomationAction.VOLUME) {
                val audio = appContext.getSystemService(AudioManager::class.java)
                audio?.setStreamVolume(AudioManager.STREAM_MUSIC, audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
            }
            if (intent != null) {
                appContext.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            AppExecutionResult(
                launched = intent != null || action == AutomationAction.VOLUME,
                message = if (intent != null) {
                    "Opened Android controls for ${action.label}. Some changes require your confirmation."
                } else {
                    "Prepared ${action.label}. This action may require a permission or an enabled automation service."
                }
            )
        } catch (_: Exception) {
            AppExecutionResult(
                launched = false,
                message = "Android could not perform ${action.label}; check the required permission or system role."
            )
        }
    }

    private fun settingsIntent(action: AutomationAction): Intent? = when (action) {
        AutomationAction.WIFI -> Intent(Settings.ACTION_WIFI_SETTINGS)
        AutomationAction.MOBILE_DATA -> Intent(Settings.ACTION_WIRELESS_SETTINGS)
        AutomationAction.BLUETOOTH -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        AutomationAction.GPS -> Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        AutomationAction.NFC -> Intent(Settings.ACTION_NFC_SETTINGS)
        AutomationAction.AIRPLANE_MODE -> Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
        AutomationAction.HOTSPOT -> Intent(Settings.ACTION_WIRELESS_SETTINGS)
        AutomationAction.BRIGHTNESS -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
        AutomationAction.SCREEN_TIMEOUT -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
        AutomationAction.ROTATION -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
        AutomationAction.DARK_MODE -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
        AutomationAction.BATTERY_SAVER -> Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
        AutomationAction.APP_LOCK -> Intent(Settings.ACTION_SECURITY_SETTINGS)
        AutomationAction.UI_AUTOMATION -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        AutomationAction.FILE_OPERATION, AutomationAction.BACKUP ->
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        AutomationAction.SEND_MESSAGE ->
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
        AutomationAction.WEBHOOK, AutomationAction.DOWNLOAD -> null
        AutomationAction.SCHEDULE -> Intent(Settings.ACTION_DATE_SETTINGS)
        AutomationAction.ROUTE_CALL_AUDIO, AutomationAction.PRIORITY_RING,
        AutomationAction.READ_MESSAGES, AutomationAction.FORWARD_MESSAGES,
        AutomationAction.CHARGING, AutomationAction.LAUNCH_APP,
        AutomationAction.CLOSE_APP, AutomationAction.GEOFENCE,
        AutomationAction.SENSOR, AutomationAction.ANSWER_CALL,
        AutomationAction.REJECT_CALL, AutomationAction.SILENCE_CALL,
        AutomationAction.END_CALL -> null
        AutomationAction.VOLUME -> null
    }

}
