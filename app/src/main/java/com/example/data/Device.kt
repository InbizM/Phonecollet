package com.example.data

enum class ConnectionType {
    USB_OTG,
    WIFI_ADB
}

enum class DeviceStatus {
    ONLINE,
    CONNECTING,
    OFFLINE
}

data class Device(
    val id: String,
    val name: String,
    val brand: String,
    val serial: String,
    val connectionType: ConnectionType,
    val status: DeviceStatus,
    val batteryLevel: Int = 100,
    val androidVersion: String = "14",
    val ipAddress: String = "",
    val groupName: String = "Unassigned",
    val currentApp: String = "Launcher",
    val isScreenOn: Boolean = true,
    val screenRotation: Int = 0, // 0, 90, 180, 270
    val recentLogs: List<String> = emptyList(),
    val appStateDescription: String = "Home screen active"
)
