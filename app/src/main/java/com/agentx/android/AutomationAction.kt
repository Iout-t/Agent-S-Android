package com.agentx.android

enum class AutomationAction(val label: String, private val terms: List<String>) {
    ANSWER_CALL("answer incoming calls", listOf("answer call", "answer incoming", "pick up call")),
    REJECT_CALL("reject incoming calls", listOf("reject call", "decline call")),
    SILENCE_CALL("silence incoming calls", listOf("silence call", "mute incoming call")),
    END_CALL("end calls", listOf("end call", "hang up")),
    ROUTE_CALL_AUDIO("route call audio", listOf("speakerphone", "bluetooth headset", "route audio")),
    PRIORITY_RING("maximize priority ring volume", listOf("priority contact", "maximize ring")),
    SEND_MESSAGE("send an automated message", listOf("send sms", "send text", "auto reply", "reply while driving")),
    READ_MESSAGES("read messages aloud", listOf("read text aloud", "read incoming messages", "read messages")),
    FORWARD_MESSAGES("forward messages or notifications", listOf("forward missed", "forward notification")),
    WEBHOOK("fetch web data or call a webhook", listOf("webhook", "http request", "fetch json", "update api")),
    WIFI("open Wi-Fi controls", listOf("toggle wi-fi", "turn on wi-fi", "turn off wi-fi")),
    MOBILE_DATA("open mobile-data controls", listOf("mobile data", "cellular data")),
    BLUETOOTH("open Bluetooth controls", listOf("toggle bluetooth", "turn on bluetooth", "turn off bluetooth")),
    GPS("open location controls", listOf("toggle gps", "turn on gps", "turn off gps")),
    NFC("open NFC controls", listOf("toggle nfc", "turn on nfc", "turn off nfc")),
    AIRPLANE_MODE("open airplane-mode controls", listOf("airplane mode", "flight mode")),
    HOTSPOT("open hotspot controls", listOf("turn on hotspot", "mobile hotspot")),
    DOWNLOAD("download or sync a file", listOf("download file", "sync document", "download media")),
    BRIGHTNESS("change screen brightness", listOf("screen brightness", "set brightness")),
    SCREEN_TIMEOUT("change screen timeout", listOf("screen timeout", "display timeout")),
    ROTATION("toggle auto-rotate", listOf("auto-rotate", "autorotate", "screen rotation")),
    DARK_MODE("change dark or light theme", listOf("dark mode", "light mode", "dark theme")),
    VOLUME("adjust audio volume", listOf("media volume", "ring volume", "notification volume", "alarm volume", "force mute")),
    BATTERY_SAVER("enable battery saver", listOf("battery saver", "power saving")),
    CHARGING("react to charging state", listOf("when phone is plugged", "when charging", "stop charging notification")),
    LAUNCH_APP("launch an app", listOf("launch app", "open app")),
    CLOSE_APP("close an app", listOf("close app", "stop app")),
    UI_AUTOMATION("interact with an app UI", listOf("accessibility", "simulate tap", "tap ", "swipe", "text input", "click button", "skip youtube ads")),
    APP_LOCK("protect an app", listOf("app lock", "lock this app", "biometric lock")),
    FILE_OPERATION("manage files", listOf("copy file", "move file", "delete file", "zip file", "unzip file")),
    BACKUP("back up data", listOf("backup", "back up", "sync folder")),
    GEOFENCE("run a location-triggered task", listOf("geofence", "when i arrive", "when i leave")),
    SENSOR("react to a device sensor", listOf("shake the device", "face down", "proximity", "flip the phone")),
    SCHEDULE("schedule a routine task", listOf("schedule", "every day", "at sunrise", "at sunset", "every morning", "every evening"));

    companion object {
        fun from(instruction: String): AutomationAction? {
            val lowered = instruction.lowercase()
            return entries.firstOrNull { action -> action.terms.any(lowered::contains) }
        }
    }
}
