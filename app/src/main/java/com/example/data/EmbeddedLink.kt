package com.example.data

data class EmbeddedLink(
    val id: String,
    val title: String,
    val url: String,
    val category: String,
    val description: String,
    val isFavorite: Boolean = false
) {
    companion object {
        val DEFAULT_LINKS = listOf(
            EmbeddedLink(
                id = "1",
                title = "scrcpy on Github",
                url = "https://github.com/Genymobile/scrcpy",
                category = "ADB Tools",
                description = "Display and control Android devices over USB or TCP/IP on a desktop."
            ),
            EmbeddedLink(
                id = "2",
                title = "Android ADB Guide",
                url = "https://developer.android.com/studio/command-line/adb",
                category = "Documentation",
                description = "Official Google developer documentation for the Android Debug Bridge CLI."
            ),
            EmbeddedLink(
                id = "3",
                title = "Awesome ADB Scripts",
                url = "https://github.com/mzlogin/awesome-adb",
                category = "Automation",
                description = "A curated collection of extremely useful ADB commands and scripts for automation."
            ),
            EmbeddedLink(
                id = "4",
                title = "Wireless Debugging Help",
                url = "https://developer.android.com/tools/adb#wireless",
                category = "Tutorial",
                description = "Learn how to connect to a device over Wi-Fi for debugging and remote control."
            )
        )
    }
}
