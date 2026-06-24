package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ConnectionType
import com.example.data.Device
import com.example.data.DeviceStatus
import com.example.data.EmbeddedLink
import com.example.data.QuickCommand
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdbViewModel : ViewModel() {

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices.asStateFlow()

    private val _selectedDeviceId = MutableStateFlow<String?>(null)
    val selectedDeviceId: StateFlow<String?> = _selectedDeviceId.asStateFlow()

    private val _groups = MutableStateFlow<List<String>>(listOf("Unassigned", "Testing Suite", "Social Farming", "Kiosk Mode"))
    val groups: StateFlow<List<String>> = _groups.asStateFlow()

    private val _commands = MutableStateFlow<List<QuickCommand>>(QuickCommand.DEFAULT_COMMANDS)
    val commands: StateFlow<List<QuickCommand>> = _commands.asStateFlow()

    private val _links = MutableStateFlow<List<EmbeddedLink>>(EmbeddedLink.DEFAULT_LINKS)
    val links: StateFlow<List<EmbeddedLink>> = _links.asStateFlow()

    private val _isMasterMirrorMode = MutableStateFlow(false)
    val isMasterMirrorMode: StateFlow<Boolean> = _isMasterMirrorMode.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<String>>(emptyList())
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    // Simulator input text states
    private val _simulatedInputTexts = MutableStateFlow<Map<String, String>>(emptyMap())
    val simulatedInputTexts: StateFlow<Map<String, String>> = _simulatedInputTexts.asStateFlow()

    init {
        // Load initial highly detailed simulated devices for immediate exploration
        _devices.value = listOf(
            Device(
                id = "pixel_8",
                name = "Pixel 8 Pro",
                brand = "Google",
                serial = "2C181FDFE0203A",
                connectionType = ConnectionType.USB_OTG,
                status = DeviceStatus.ONLINE,
                batteryLevel = 92,
                androidVersion = "14",
                ipAddress = "USB Host /dev/bus/usb/001/002",
                groupName = "Testing Suite",
                currentApp = "Launcher",
                recentLogs = listOf("ADB server recognized Pixel 8 Pro", "shell settings put system screen_brightness 128"),
                appStateDescription = "Homescreen - Icons: Settings, YouTube, Browser"
            ),
            Device(
                id = "s24_ultra",
                name = "Galaxy S24 Ultra",
                brand = "Samsung",
                serial = "3E228CDFB2249B",
                connectionType = ConnectionType.WIFI_ADB,
                status = DeviceStatus.ONLINE,
                batteryLevel = 78,
                androidVersion = "14",
                ipAddress = "192.168.1.15:5555",
                groupName = "Social Farming",
                currentApp = "YouTube",
                recentLogs = listOf("Connected to 192.168.1.15:5555", "shell am start -n com.google.android.youtube/.."),
                appStateDescription = "Watching: 'Jetpack Compose Tutorial' at 4k"
            ),
            Device(
                id = "xiaomi_14",
                name = "Xiaomi 14",
                brand = "Xiaomi",
                serial = "XM44199CD31102",
                connectionType = ConnectionType.USB_OTG,
                status = DeviceStatus.ONLINE,
                batteryLevel = 100,
                androidVersion = "14",
                ipAddress = "USB Host /dev/bus/usb/001/003",
                groupName = "Testing Suite",
                currentApp = "Settings",
                recentLogs = listOf("USB OTG node mounted", "shell am start -a android.settings.SETTINGS"),
                appStateDescription = "System Settings - Display & Brightness"
            ),
            Device(
                id = "oneplus_12",
                name = "OnePlus 12",
                brand = "OnePlus",
                serial = "OP9928374AA823",
                connectionType = ConnectionType.WIFI_ADB,
                status = DeviceStatus.CONNECTING,
                batteryLevel = 45,
                androidVersion = "13",
                ipAddress = "192.168.1.92:5555",
                groupName = "Unassigned",
                currentApp = "Camera",
                recentLogs = listOf("Initiating connection to 192.168.1.92:5555..."),
                appStateDescription = "Camera app active - Live Preview"
            )
        )
        _selectedDeviceId.value = "pixel_8"
        addHostLog("ADB OTG Engine initialized. Ready for USB connection & Wireless TCP ports.")
    }

    private fun addHostLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _terminalLogs.value = listOf("[$timestamp] $message") + _terminalLogs.value.take(49)
    }

    fun selectDevice(id: String?) {
        _selectedDeviceId.value = id
        id?.let { addHostLog("Selected target device: $it") }
    }

    // ADB WiFi Device Addition
    fun addWifiDevice(ip: String, port: String) {
        if (ip.isBlank() || port.isBlank()) return
        val fullIp = "$ip:$port"
        val deviceId = "wifi_" + ip.replace(".", "_") + "_" + port
        
        viewModelScope.launch {
            addHostLog("adb connect $fullIp ...")
            // Create a pending device
            val newDevice = Device(
                id = deviceId,
                name = "Device @ $ip",
                brand = "Generic Android",
                serial = "WIFI-$port-$deviceId",
                connectionType = ConnectionType.WIFI_ADB,
                status = DeviceStatus.CONNECTING,
                batteryLevel = 100,
                ipAddress = fullIp,
                recentLogs = listOf("Connecting to $fullIp")
            )
            _devices.value = _devices.value + newDevice
            
            delay(1500) // Simulated connection latency
            
            _devices.value = _devices.value.map { dev ->
                if (dev.id == deviceId) {
                    dev.copy(
                        status = DeviceStatus.ONLINE,
                        brand = "Xiaomi",
                        name = "Redmi Note (Wireless)",
                        batteryLevel = 89,
                        recentLogs = listOf("Connected successfully to $fullIp", "shell getprop ro.product.model")
                    )
                } else dev
            }
            addHostLog("Connected to wireless device: $fullIp")
        }
    }

    // USB OTG Device Addition
    fun connectOtgDevice(name: String, brand: String) {
        val deviceId = "usb_" + name.lowercase().replace(" ", "_") + "_" + (100..999).random()
        val randomSerial = (1000000000..9999999999).random().toString(16).uppercase()
        val newDevice = Device(
            id = deviceId,
            name = name,
            brand = brand,
            serial = randomSerial,
            connectionType = ConnectionType.USB_OTG,
            status = DeviceStatus.ONLINE,
            batteryLevel = 100,
            ipAddress = "USB Host /dev/bus/usb/002/${(4..99).random()}",
            groupName = "Unassigned",
            recentLogs = listOf("OTG Handshake success", "Permissions acquired for bulk transfers", "ADB handshake OK")
        )
        _devices.value = _devices.value + newDevice
        addHostLog("USB Device connected: $brand $name via OTG")
    }

    fun removeDevice(id: String) {
        _devices.value = _devices.value.filterNot { it.id == id }
        if (_selectedDeviceId.value == id) {
            _selectedDeviceId.value = _devices.value.firstOrNull()?.id
        }
        addHostLog("Device disconnected: $id")
    }

    // Command executions
    fun executeCommand(deviceId: String, command: QuickCommand) {
        executeCustomCommand(deviceId, command.commandText)
    }

    fun executeCustomCommand(deviceId: String, commandText: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                val newLogs = listOf("[$timestamp] $ adb shell $commandText", "[$timestamp] stdout: command executed successfully") + dev.recentLogs.take(19)
                
                // Parse specific action simulation to show interactive screen changes
                var nextApp = dev.currentApp
                var appDesc = dev.appStateDescription
                var isScreen = dev.isScreenOn
                
                val lowercaseCmd = commandText.lowercase()
                when {
                    "input keyevent 3" in lowercaseCmd -> {
                        nextApp = "Launcher"
                        appDesc = "Homescreen - Icons: Settings, YouTube, Browser"
                    }
                    "input keyevent 4" in lowercaseCmd -> {
                        if (dev.currentApp != "Launcher") {
                            nextApp = "Launcher"
                            appDesc = "Returned to Homescreen"
                        }
                    }
                    "am start" in lowercaseCmd && "settings" in lowercaseCmd -> {
                        nextApp = "Settings"
                        appDesc = "System Settings - Display & Brightness"
                    }
                    "am start" in lowercaseCmd && "youtube" in lowercaseCmd -> {
                        nextApp = "YouTube"
                        appDesc = "Watching: 'Jetpack Compose Tutorial' at 4k"
                    }
                    "input keyevent 26" in lowercaseCmd -> {
                        isScreen = !dev.isScreenOn
                        appDesc = if (isScreen) "Homescreen Active" else "Screen is turned OFF (ADB Sleep)"
                    }
                    "screencap" in lowercaseCmd -> {
                        appDesc = "$nextApp - Captured screenshot to local memory"
                    }
                }
                
                dev.copy(
                    recentLogs = newLogs,
                    currentApp = nextApp,
                    appStateDescription = appDesc,
                    isScreenOn = isScreen
                )
            } else dev
        }
        addHostLog("Executed: adb -s $deviceId shell '$commandText'")
    }

    // Bulk actions
    fun executeBulkCommand(groupName: String, commandText: String) {
        viewModelScope.launch {
            addHostLog("Executing group bulk command [$groupName]: '$commandText'")
            _devices.value.filter { it.groupName == groupName && it.status == DeviceStatus.ONLINE }.forEach { dev ->
                executeCustomCommand(dev.id, commandText)
                delay(200)
            }
        }
    }

    fun executeBulkCommandToAll(commandText: String) {
        viewModelScope.launch {
            addHostLog("Executing broadcast bulk command: '$commandText'")
            _devices.value.filter { it.status == DeviceStatus.ONLINE }.forEach { dev ->
                executeCustomCommand(dev.id, commandText)
                delay(150)
            }
        }
    }

    fun toggleMasterMirrorMode() {
        _isMasterMirrorMode.value = !_isMasterMirrorMode.value
        addHostLog("Master Mirror Mode: ${_isMasterMirrorMode.value.toString().uppercase()}")
    }

    // Grouping
    fun addGroup(groupName: String) {
        if (groupName.isNotBlank() && !_groups.value.contains(groupName)) {
            _groups.value = _groups.value + groupName
            addHostLog("Created device group: $groupName")
        }
    }

    fun updateDeviceGroup(deviceId: String, groupName: String) {
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                dev.copy(groupName = groupName)
            } else dev
        }
        addHostLog("Assigned device $deviceId to group $groupName")
    }

    // Tap coordinate simulation to navigate screen state
    fun simulateScreenTap(deviceId: String, xPercent: Float, yPercent: Float) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId && dev.isScreenOn) {
                val commandSim = "input tap ${(xPercent * 1080).toInt()} ${(yPercent * 2400).toInt()}"
                val newLogs = listOf("[$timestamp] $ adb shell $commandSim", "[$timestamp] MotionEvent: ACTION_DOWN -> ACTION_UP") + dev.recentLogs.take(19)
                
                var nextApp = dev.currentApp
                var appDesc = dev.appStateDescription
                
                if (dev.currentApp == "Launcher") {
                    when {
                        // Settings icon tap (upper-left region)
                        xPercent < 0.5f && yPercent > 0.2f && yPercent < 0.4f -> {
                            nextApp = "Settings"
                            appDesc = "System Settings - Display & Brightness"
                        }
                        // YouTube icon tap (upper-right region)
                        xPercent >= 0.5f && yPercent > 0.2f && yPercent < 0.4f -> {
                            nextApp = "YouTube"
                            appDesc = "Watching: 'Jetpack Compose Tutorial' at 4k"
                        }
                        // Browser icon tap (mid region)
                        yPercent >= 0.4f && yPercent < 0.6f -> {
                            nextApp = "Browser"
                            appDesc = "Browsing: 'Google.com' on Chrome Mobile"
                        }
                    }
                } else {
                    // Tap on top region of other apps usually returns or does action
                    if (yPercent < 0.1f) {
                        nextApp = "Launcher"
                        appDesc = "Returned to Homescreen"
                    } else if (dev.currentApp == "YouTube") {
                        // Tapping pause or play
                        if (xPercent > 0.4f && xPercent < 0.6f && yPercent > 0.3f && yPercent < 0.5f) {
                            appDesc = if (appDesc.contains("Paused")) "Watching: 'Jetpack Compose Tutorial' at 4k" else "Paused: 'Jetpack Compose Tutorial' at 4k"
                        }
                    } else if (dev.currentApp == "Settings") {
                        if (yPercent > 0.3f && yPercent < 0.5f) {
                            appDesc = "Settings -> Developer Options Enabled!"
                        }
                    }
                }
                
                dev.copy(
                    recentLogs = newLogs,
                    currentApp = nextApp,
                    appStateDescription = appDesc
                )
            } else dev
        }
        
        // Propagate tap to other devices if master mirror mode is ON
        if (_isMasterMirrorMode.value) {
            _devices.value.filter { it.id != deviceId && it.status == DeviceStatus.ONLINE }.forEach { dev ->
                viewModelScope.launch {
                    delay(50)
                    simulateScreenTap(dev.id, xPercent, yPercent)
                }
            }
        }
    }

    // Keyboard transmitter simulation
    fun simulateKeyboardInput(deviceId: String, text: String) {
        if (text.isEmpty()) return
        val currentInput = _simulatedInputTexts.value[deviceId] ?: ""
        val updatedInput = currentInput + text
        _simulatedInputTexts.value = _simulatedInputTexts.value + (deviceId to updatedInput)
        
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                val commandSim = "input text \"$text\""
                val newLogs = listOf("[$timestamp] $ adb shell $commandSim") + dev.recentLogs.take(19)
                dev.copy(
                    recentLogs = newLogs,
                    appStateDescription = "Transmitted: '$updatedInput' to active input field"
                )
            } else dev
        }
        addHostLog("Keyboard send to $deviceId: '$text'")
    }

    fun clearSimulatedInputText(deviceId: String) {
        _simulatedInputTexts.value = _simulatedInputTexts.value + (deviceId to "")
    }

    fun toggleDeviceScreen(deviceId: String) {
        executeCustomCommand(deviceId, "input keyevent 26")
    }

    // Embedded links ("bine") actions
    fun addEmbeddedLink(title: String, url: String, category: String, description: String) {
        if (title.isBlank() || url.isBlank()) return
        val newId = "link_" + (1000..9999).random()
        val newLink = EmbeddedLink(
            id = newId,
            title = title,
            url = url,
            category = category,
            description = description
        )
        _links.value = _links.value + newLink
        addHostLog("Added Embedded Link: $title")
    }

    fun removeEmbeddedLink(id: String) {
        _links.value = _links.value.filterNot { it.id == id }
        addHostLog("Removed Embedded Link: $id")
    }

    fun toggleFavoriteLink(id: String) {
        _links.value = _links.value.map { link ->
            if (link.id == id) {
                link.copy(isFavorite = !link.isFavorite)
            } else link
        }
    }
}
