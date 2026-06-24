package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Device
import com.example.data.QuickCommand
import com.example.ui.theme.*
import com.example.viewmodel.AdbViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(
    deviceId: String,
    viewModel: AdbViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsState()
    val device = devices.find { it.id == deviceId }
    val commands by viewModel.commands.collectAsState()
    val simulatedInputTexts by viewModel.simulatedInputTexts.collectAsState()
    val currentInputText = simulatedInputTexts[deviceId] ?: ""

    var customCommandText by remember { mutableStateOf("") }
    var keyboardInputText by remember { mutableStateOf("") }

    if (device == null) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Device not found or disconnected.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateBack) {
                Text("Back to Dashboard")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(device.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Serial: ${device.serial} • ADB Online", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = if (device.batteryLevel > 20) ColorConnected else ColorOffline, modifier = Modifier.size(16.dp))
                        Text("${device.batteryLevel}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize().testTag("control_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Screen streaming view at the top or side-by-side depending on size
            // Let's create a scrollable column of controls, with the device frame taking 320dp height
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Device Screen Bezel Box
                DeviceInteractiveFrame(
                    device = device,
                    onScreenTap = { x, y ->
                        viewModel.simulateScreenTap(device.id, x, y)
                    }
                )

                // Virtual Physical Bar (Power, Volume, Rotate)
                VirtualPhysicalButtonsBar(
                    device = device,
                    onPowerToggle = { viewModel.toggleDeviceScreen(device.id) },
                    onVolUp = { viewModel.executeCustomCommand(device.id, "input keyevent 24") },
                    onVolDown = { viewModel.executeCustomCommand(device.id, "input keyevent 25") },
                    onMute = { viewModel.executeCustomCommand(device.id, "input keyevent 164") }
                )

                // Navigation Bar (Back, Home, Recents)
                VirtualNavBar(
                    onBack = { viewModel.executeCustomCommand(device.id, "input keyevent 4") },
                    onHome = { viewModel.executeCustomCommand(device.id, "input keyevent 3") },
                    onRecents = { viewModel.executeCustomCommand(device.id, "input keyevent 187") }
                )

                // Keyboard Input Transmitter Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "ADB Keyboard Transmitter",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = keyboardInputText,
                                onValueChange = { keyboardInputText = it },
                                placeholder = { Text("Type text to send...") },
                                modifier = Modifier.weight(1f).testTag("keyboard_text_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Button(
                                onClick = {
                                    viewModel.simulateKeyboardInput(device.id, keyboardInputText)
                                    keyboardInputText = ""
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier.testTag("send_keyboard_btn")
                            ) {
                                Icon(Icons.Default.KeyboardTab, contentDescription = "Send text")
                            }
                        }
                        if (currentInputText.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Simulated Text: \"$currentInputText\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(
                                    onClick = { viewModel.clearSimulatedInputText(device.id) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                // Quick Macros Panel
                Text(
                    "Quick ADB Script Macros",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(commands) { cmd ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { viewModel.executeCommand(device.id, cmd) }
                                .testTag("macro_btn_${cmd.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val icon = when (cmd.iconName) {
                                    "home" -> Icons.Default.Home
                                    "arrow_back" -> Icons.Default.ArrowBack
                                    "menu" -> Icons.Default.Menu
                                    "power_settings_new" -> Icons.Default.PowerSettingsNew
                                    "screenshot" -> Icons.Default.Screenshot
                                    "info" -> Icons.Default.Info
                                    "settings" -> Icons.Default.Settings
                                    "swipe" -> Icons.Default.SwipeUp
                                    else -> Icons.Default.Terminal
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(cmd.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Custom Script Terminal Executor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    colors = CardDefaults.cardColors(containerColor = BgCardDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = ColorConnected, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Shell Script Terminal Runner",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "adb shell",
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            OutlinedTextField(
                                value = customCommandText,
                                onValueChange = { customCommandText = it },
                                placeholder = { Text("ls -la /sdcard", color = TextMuted) },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1.5f).testTag("terminal_text_input")
                            )
                            Button(
                                onClick = {
                                    if (customCommandText.isNotBlank()) {
                                        viewModel.executeCustomCommand(device.id, customCommandText)
                                        customCommandText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorGreen, contentColor = TextPrimary),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("execute_shell_btn")
                            ) {
                                Text("RUN", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Selected Device Specific Console Logs
                        Text(
                            "DEVICE STDOUT LOGS:",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(BgMain, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            if (device.recentLogs.isEmpty()) {
                                Text(
                                    "No device terminal activity yet...",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(device.recentLogs) { log ->
                                        Text(
                                            log,
                                            color = if (log.contains("adb shell")) ColorConnected else if (log.contains("stdout")) ColorConnected.copy(alpha = 0.8f) else TextSecondary,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceInteractiveFrame(
    device: Device,
    onScreenTap: (Float, Float) -> Unit
) {
    // Standard aspect ratio for typical phone: 9:19.5 (around 160 x 340 dp)
    Box(
        modifier = Modifier
            .width(180.dp)
            .height(370.dp)
            .background(BgMain, RoundedCornerShape(28.dp))
            .border(4.dp, BorderColor, RoundedCornerShape(28.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Phone Screen Surface
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(22.dp))
                .pointerInput(device.id) {
                    detectTapGestures { offset ->
                        val xPercent = offset.x / size.width
                        val yPercent = offset.y / size.height
                        onScreenTap(xPercent, yPercent)
                    }
                }
                .testTag("device_screen_touch")
        ) {
            if (!device.isScreenOn) {
                // Sleep screen state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Device Sleeping",
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                // Active application drawing
                when (device.currentApp) {
                    "Launcher" -> LauncherMockup()
                    "Settings" -> SettingsMockup()
                    "YouTube" -> YouTubeMockup(device.appStateDescription)
                    "Browser" -> BrowserMockup()
                    "Camera" -> CameraMockup()
                    else -> DefaultAppMockup(device.currentApp, device.appStateDescription)
                }
            }

            // Small Notch bezel at the top center
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(14.dp)
                    .align(Alignment.TopCenter)
                    .background(Color.Black, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            )
        }
    }
}

@Composable
fun LauncherMockup() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ColorPurple, BgMain))) // Gorgeous gradient wallpaper
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(18.dp))
            // Clock & Date Widget
            Text(
                "08:42",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                "Wed, Jun 24",
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // App Icons Grid (Interactive!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AppLauncherIcon(Icons.Default.Settings, "Settings", Color(0xFF4B5563))
                AppLauncherIcon(Icons.Default.PlayArrow, "YouTube", Color(0xFFEF4444))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AppLauncherIcon(Icons.Default.Language, "Browser", Color(0xFF10B981))
                AppLauncherIcon(Icons.Default.CameraAlt, "Camera", Color(0xFFF59E0B))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Hotbar Mockup
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Icon(Icons.Default.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Icon(Icons.Default.ContactPhone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun AppLauncherIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(55.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            label,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun SettingsMockup() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCardDark) // Dark Material settings style
            .padding(6.dp)
    ) {
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Settings items list
        SettingsListRow(Icons.Default.Wifi, "Wi-Fi Connection", "On (AdbHub_Net)", Color(0xFF3B82F6))
        SettingsListRow(Icons.Default.Bluetooth, "Bluetooth Hub", "Disconnected", Color(0xFF6366F1))
        SettingsListRow(Icons.Default.DisplaySettings, "Display & Brightness", "Auto Dark Mode", Color(0xFFEC4899))
        SettingsListRow(Icons.Default.SettingsSuggest, "Developer Options", "ADB Debugging ON", Color(0xFF10B981))
        SettingsListRow(Icons.Default.SystemUpdate, "About phone", "Android 14 (API 34)", Color(0xFF8B5CF6))
    }
}

@Composable
fun SettingsListRow(icon: androidx.compose.ui.graphics.vector.ImageVector, name: String, subtitle: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(name, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.LightGray, fontSize = 7.sp)
        }
    }
}

@Composable
fun YouTubeMockup(description: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgMain)
            .padding(4.dp)
    ) {
        Spacer(modifier = Modifier.height(18.dp))
        // YouTube Red Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text("YouTube", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = (-0.5).sp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Video Player Box (Simulated Video)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
                .background(Color.DarkGray, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    if (description.contains("Paused")) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    if (description.contains("Paused")) "PAUSED" else "PLAYING LIVE SCREEN",
                    color = Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title and channel info
        Text(
            "Android Developer Stream: Wireless ADB Controls & Pipeline Hacks",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            "Android Devs • 500K Views • 12h ago",
            color = Color.LightGray,
            fontSize = 7.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Likes & share bar mockup
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(11.dp))
            Icon(Icons.Default.ThumbDown, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(11.dp))
            Icon(Icons.Default.Share, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(11.dp))
            Icon(Icons.Default.Download, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(11.dp))
        }
    }
}

@Composable
fun BrowserMockup() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(4.dp)
    ) {
        Spacer(modifier = Modifier.height(18.dp))
        // Browser URL Address bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F3F4), RoundedCornerShape(12.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(8.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("google.com", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Google visual logo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("e", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, Color.LightGray, RoundedCornerShape(14.dp))
                .padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Search or type URL...", color = Color.LightGray, fontSize = 8.sp)
            }
        }
    }
}

@Composable
fun CameraMockup() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(18.dp))
            // Live capture viewport mockup (custom drawing)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(BgCardDark, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                    Text("LENS ACTIVE - ADB PREVIEW", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Trigger action controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Cached, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.White, CircleShape)
                        .border(1.5.dp, Color.Black, CircleShape)
                )
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun DefaultAppMockup(appName: String, stateDescription: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgMain)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Android, contentDescription = null, tint = ColorConnected, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(appName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stateDescription,
            color = Color.LightGray,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}

@Composable
fun VirtualPhysicalButtonsBar(
    device: Device,
    onPowerToggle: () -> Unit,
    onVolUp: () -> Unit,
    onVolDown: () -> Unit,
    onMute: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPowerToggle,
                modifier = Modifier.testTag("power_btn")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PowerSettingsNew,
                        contentDescription = "Power",
                        tint = if (device.isScreenOn) ColorPurple else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Text("Power", fontSize = 9.sp)
                }
            }

            IconButton(
                onClick = onVolUp,
                modifier = Modifier.testTag("vol_up_btn")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Vol Up", modifier = Modifier.size(20.dp))
                    Text("Vol+", fontSize = 9.sp)
                }
            }

            IconButton(
                onClick = onVolDown,
                modifier = Modifier.testTag("vol_down_btn")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.VolumeDown, contentDescription = "Vol Down", modifier = Modifier.size(20.dp))
                    Text("Vol-", fontSize = 9.sp)
                }
            }

            IconButton(
                onClick = onMute,
                modifier = Modifier.testTag("vol_mute_btn")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.VolumeMute, contentDescription = "Mute", modifier = Modifier.size(20.dp))
                    Text("Mute", fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun VirtualNavBar(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onRecents: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BgCardDark),
        modifier = Modifier
            .width(220.dp)
            .height(44.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("nav_back_btn")) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back Keyevent", tint = Color.White)
            }
            IconButton(onClick = onHome, modifier = Modifier.testTag("nav_home_btn")) {
                Icon(Icons.Default.Circle, contentDescription = "Home Keyevent", tint = Color.White, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onRecents, modifier = Modifier.testTag("nav_recents_btn")) {
                Icon(Icons.Default.CropSquare, contentDescription = "Recents Keyevent", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}
