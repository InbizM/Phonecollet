package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConnectionType
import com.example.data.Device
import com.example.data.DeviceStatus
import com.example.ui.theme.*
import com.example.viewmodel.AdbViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AdbViewModel,
    onNavigateToControl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val hostLogs by viewModel.terminalLogs.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var addTypeByWifi by remember { mutableStateOf(false) }

    var wifiIp by remember { mutableStateOf("192.168.1.100") }
    var wifiPort by remember { mutableStateOf("5555") }

    var usbName by remember { mutableStateOf("Galaxy Z Fold 6") }
    var usbBrand by remember { mutableStateOf("Samsung") }

    var showLogsConsole by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Banner
        item {
            HeroHeader()
        }

        // Host Connection Console and Quick Stats
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Connection Controls",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { 
                                    addTypeByWifi = false
                                    showAddDialog = true 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_usb_btn")
                            ) {
                                Icon(Icons.Default.Usb, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("USB OTG", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { 
                                    addTypeByWifi = true
                                    showAddDialog = true 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_wifi_btn")
                            ) {
                                Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Wi-Fi ADB", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatPill(
                            label = "Total Devices",
                            count = devices.size.toString(),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatPill(
                            label = "Online",
                            count = devices.count { it.status == DeviceStatus.ONLINE }.toString(),
                            color = ColorConnected,
                            modifier = Modifier.weight(1f)
                        )
                        StatPill(
                            label = "Connecting",
                            count = devices.count { it.status == DeviceStatus.CONNECTING }.toString(),
                            color = ColorWarning,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Live Log Terminal Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgCardDark),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogsConsole = !showLogsConsole }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = null,
                                tint = ColorConnected,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Local ADB Server Host Console",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Icon(
                            if (showLogsConsole) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }

                    AnimatedVisibility(
                        visible = showLogsConsole,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                                .heightIn(max = 120.dp)
                                .background(BgMain, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            if (hostLogs.isEmpty()) {
                                Text(
                                    "No logs yet. Connection events will appear here...",
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(hostLogs) { log ->
                                        Text(
                                            log,
                                            color = if (log.contains("Executed") || log.contains("initialized")) ColorConnected else TextSecondary,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section header
        item {
            Text(
                text = "Connected Device List",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Devices items
        if (devices.isEmpty()) {
            item {
                EmptyDevicesState()
            }
        } else {
            items(devices, key = { it.id }) { device ->
                DeviceCard(
                    device = device,
                    groups = groups,
                    onNavigateToControl = onNavigateToControl,
                    onRemove = { viewModel.removeDevice(device.id) },
                    onPowerToggle = { viewModel.toggleDeviceScreen(device.id) },
                    onUpdateGroup = { group -> viewModel.updateDeviceGroup(device.id, group) }
                )
            }
        }
    }

    // Add Device Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = if (addTypeByWifi) "Connect via Wi-Fi ADB" else "Connect via USB OTG Emulator",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (addTypeByWifi) {
                        Text(
                            "Ensure the device is connected to the same local Wi-Fi and Wireless Debugging is active in Developer Options.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = wifiIp,
                            onValueChange = { wifiIp = it },
                            label = { Text("IP Address") },
                            leadingIcon = { Icon(Icons.Default.Lan, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("wifi_ip_input")
                        )
                        OutlinedTextField(
                            value = wifiPort,
                            onValueChange = { wifiPort = it },
                            label = { Text("Port") },
                            leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("wifi_port_input")
                        )
                    } else {
                        Text(
                            "Simulates plugging an OTG host adapter into your phone and hooking another Android device with a USB cable.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = usbBrand,
                            onValueChange = { usbBrand = it },
                            label = { Text("Device Brand") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("usb_brand_input")
                        )
                        OutlinedTextField(
                            value = usbName,
                            onValueChange = { usbName = it },
                            label = { Text("Device Model Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("usb_name_input")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addTypeByWifi) {
                            viewModel.addWifiDevice(wifiIp, wifiPort)
                        } else {
                            viewModel.connectOtgDevice(usbName, usbBrand)
                        }
                        showAddDialog = false
                    },
                    modifier = Modifier.testTag("confirm_add_device")
                ) {
                    Text("Connect Device")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        BgCard,
                        BgCardDark
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = "ADB COMMAND HUB",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorConnected,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Control center for USB OTG & Wi-Fi ADB devices",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 24.sp
                )
                Text(
                    text = "Extract screen streams, pipe bulk input commands, and link assets",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = 0.8f)
                )
            }
            Icon(
                Icons.Default.SettingsInputAntenna,
                contentDescription = null,
                tint = ColorConnected.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(64.dp)
                    .weight(0.5f)
            )
        }
    }
}

@Composable
fun StatPill(
    label: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun EmptyDevicesState() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.DevicesOther,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No devices connected yet",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Use the USB OTG or Wi-Fi ADB buttons at the top to simulate or scan connected phones.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceCard(
    device: Device,
    groups: List<String>,
    onNavigateToControl: (String) -> Unit,
    onRemove: () -> Unit,
    onPowerToggle: () -> Unit,
    onUpdateGroup: (String) -> Unit
) {
    var expandedGroupSelector by remember { mutableStateOf(false) }

    val statusColor by animateColorAsState(
        when (device.status) {
            DeviceStatus.ONLINE -> ColorConnected
            DeviceStatus.CONNECTING -> ColorWarning
            DeviceStatus.OFFLINE -> ColorOffline
        },
        label = "statusColor"
    )

    val cardBorder = if (device.connectionType == ConnectionType.USB_OTG) {
        BorderStroke(1.dp, ColorPurple)
    } else {
        BorderStroke(1.dp, WifiIndicator)
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        border = cardBorder,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("device_card_${device.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Brand & Name + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (device.connectionType == ConnectionType.USB_OTG) Icons.Default.Usb else Icons.Default.Wifi,
                        contentDescription = "Connection Type",
                        tint = if (device.connectionType == ConnectionType.USB_OTG) ColorPurple else WifiIndicator,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    device.groupName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                               )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (device.connectionType == ConnectionType.USB_OTG) ColorPurple.copy(alpha = 0.3f) else VineBadgeBg.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, if (device.connectionType == ConnectionType.USB_OTG) ColorPurple else ColorConnected),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    if (device.connectionType == ConnectionType.USB_OTG) "USB ('sub')" else "Vine (Wireless)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (device.connectionType == ConnectionType.USB_OTG) TextPrimary else ColorConnected,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${device.brand} • Android ${device.androidVersion} • Serial: ${device.serial}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = device.status.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Info rows: IP Address + Battery + Current screen app
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("HOST ATTACHMENT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    Text(device.ipAddress.ifBlank { "No IP Assigned" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("BATTERY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (device.batteryLevel == 100) Icons.Default.BatteryFull else Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery Status",
                            tint = if (device.batteryLevel > 20) ColorConnected else ColorOffline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${device.batteryLevel}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Tv,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Active App: ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${device.currentApp} (${device.appStateDescription})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Group assign dropdown trigger
                Box {
                    OutlinedButton(
                        onClick = { expandedGroupSelector = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Group", fontSize = 11.sp)
                    }

                    DropdownMenu(
                        expanded = expandedGroupSelector,
                        onDismissRequest = { expandedGroupSelector = false }
                    ) {
                        groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group) },
                                onClick = {
                                    onUpdateGroup(group)
                                    expandedGroupSelector = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onPowerToggle,
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.PowerSettingsNew,
                            contentDescription = "Toggle Screen Power",
                            tint = if (device.isScreenOn) ColorPurple else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Disconnect Device",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Button(
                        onClick = { onNavigateToControl(device.id) },
                        enabled = device.status == DeviceStatus.ONLINE,
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("control_device_${device.id}")
                    ) {
                        Text("Open Screen", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}
