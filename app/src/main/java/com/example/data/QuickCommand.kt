package com.example.data

data class QuickCommand(
    val id: String,
    val title: String,
    val commandText: String,
    val category: String,
    val iconName: String
) {
    companion object {
        val DEFAULT_COMMANDS = listOf(
            QuickCommand("1", "Go Home", "input keyevent 3", "Navigation", "home"),
            QuickCommand("2", "Back Press", "input keyevent 4", "Navigation", "arrow_back"),
            QuickCommand("3", "Show Recents", "input keyevent 187", "Navigation", "menu"),
            QuickCommand("4", "Lock/Unlock", "input keyevent 26", "Power", "power_settings_new"),
            QuickCommand("5", "Take Screenshot", "screencap -p /sdcard/screenshot.png", "Utility", "screenshot"),
            QuickCommand("6", "Dump Sysinfo", "dumpsys battery", "System", "info"),
            QuickCommand("7", "Open Settings", "am start -a android.settings.SETTINGS", "Utility", "settings"),
            QuickCommand("8", "Simulate Swipe", "input swipe 300 1000 300 200 500", "Automation", "swipe"),
            QuickCommand("9", "Clear Logcat", "logcat -c", "Developer", "delete")
        )
    }
}
