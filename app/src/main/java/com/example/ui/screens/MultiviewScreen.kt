package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConnectionType
import com.example.data.Device
import com.example.data.DeviceStatus
import com.example.ui.theme.*
import com.example.viewmodel.AdbViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiviewScreen(
    viewModel: AdbViewModel,
    onNavigateToControl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsState()
    val onlineDevices = devices.filter { it.status == DeviceStatus.ONLINE }
    val isMasterMirrorMode by viewModel.isMasterMirrorMode.collectAsState()

    var showMasterController by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("multiview_screen")
    ) {
        // Master Mirror Controller Panel at the top
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = BgCardDark),
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Devices,
                            contentDescription = null,
                            tint = if (isMasterMirrorMode) ColorConnected else ColorPurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Master Mirror Controller",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (isMasterMirrorMode) "MIRROR ON" else "MIRROR OFF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isMasterMirrorMode) ColorConnected else TextMuted
                        )
                        Switch(
                            checked = isMasterMirrorMode,
                            onCheckedChange = { viewModel.toggleMasterMirrorMode() },
                            modifier = Modifier.testTag("master_mirror_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isMasterMirrorMode) {
                    Text(
                        "Input mirroring is active. Taps on the Master Touchpad or clicks on Master physical keys below will stream to ALL online devices in parallel!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Master Touchpad Control Bezel
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .background(BgMain, RoundedCornerShape(12.dp))
                                .border(1.5.dp, ColorConnected, RoundedCornerShape(12.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        val xPercent = offset.x / size.width
                                        val yPercent = offset.y / size.height
                                        // Broadcast tap to all online devices via viewModel
                                        onlineDevices.forEach { dev ->
                                            viewModel.simulateScreenTap(dev.id, xPercent, yPercent)
                                        }
                                    }
                                }
                                .testTag("master_touchpad"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = ColorConnected, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "MASTER TOUCHPAD MOUSE",
                                    color = ColorConnected,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    "Tap here to click all screens",
                                    color = TextMuted,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Master keys
                        Column(
                            modifier = Modifier.weight(1.2f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.executeBulkCommandToAll("input keyevent 3") },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Home", fontSize = 10.sp)
                                }
                                Button(
                                    onClick = { viewModel.executeBulkCommandToAll("input keyevent 4") },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Back", fontSize = 10.sp)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.executeBulkCommandToAll("input keyevent 26") },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorPurple),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Power", fontSize = 10.sp)
                                }
                                Button(
                                    onClick = { viewModel.executeBulkCommandToAll("am start -a android.settings.SETTINGS") },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Settings", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "Mirror mode is inactive. Activate mirror mode above to unlock simultaneous control. Currently, screens are independent.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Dense Multiview Grid (${onlineDevices.size} Active)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Icon(Icons.Default.GridView, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }

        // Multiview grid - Tightly packed device screens
        if (onlineDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Default.NoCell, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No online devices to display.", fontWeight = FontWeight.Bold)
                    Text("Ensure your connected devices are powered ON and authorized in the dashboard.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2 screens side-by-side
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(onlineDevices, key = { it.id }) { dev ->
                    DenseGridDeviceItem(
                        device = dev,
                        onNavigateToControl = onNavigateToControl,
                        onScreenTap = { x, y ->
                            viewModel.simulateScreenTap(dev.id, x, y)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DenseGridDeviceItem(
    device: Device,
    onNavigateToControl: (String) -> Unit,
    onScreenTap: (Float, Float) -> Unit
) {
    val itemBorder = if (device.connectionType == ConnectionType.USB_OTG) {
        BorderStroke(1.dp, ColorPurple)
    } else {
        BorderStroke(1.dp, WifiIndicator)
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = itemBorder,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("multiview_item_${device.id}")
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tiny label header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        if (device.connectionType == ConnectionType.USB_OTG) Icons.Default.Usb else Icons.Default.Wifi,
                        contentDescription = null,
                        tint = if (device.connectionType == ConnectionType.USB_OTG) ColorPurple else ColorConnected,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        device.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                // tiny connection dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (device.isScreenOn) ColorConnected else ColorOffline)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Miniature Screen Container (Aspect ratio kept, width around 120dp)
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(230.dp)
                    .background(BgMain, RoundedCornerShape(14.dp))
                    .border(2.dp, BorderColor, RoundedCornerShape(14.dp))
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(11.dp))
                        .pointerInput(device.id) {
                            detectTapGestures { offset ->
                                val xPercent = offset.x / size.width
                                val yPercent = offset.y / size.height
                                onScreenTap(xPercent, yPercent)
                            }
                        }
                ) {
                    if (!device.isScreenOn) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(BgMain),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sleeping", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Dense render
                        when (device.currentApp) {
                            "Launcher" -> LauncherMini()
                            "Settings" -> SettingsMini()
                            "YouTube" -> YouTubeMini()
                            "Browser" -> BrowserMini()
                            "Camera" -> CameraMini()
                            else -> DefaultMini(device.currentApp)
                        }
                    }

                    // Tiny Notch Bezel
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(8.dp)
                            .align(Alignment.TopCenter)
                            .background(Color.Black, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Small button link to full controller
            TextButton(
                onClick = { onNavigateToControl(device.id) },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(24.dp)
            ) {
                Text("Open Controller", fontSize = 10.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(10.dp))
            }
        }
    }
}

// Miniatures for dense multiview layout

@Composable
fun LauncherMini() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF4C1D95))))
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("08:42", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(14.dp))
            // 4 tiny dots representing apps
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Box(modifier = Modifier.size(16.dp).background(Color(0xFF4B5563), CircleShape))
                Box(modifier = Modifier.size(16.dp).background(Color(0xFFEF4444), CircleShape))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Box(modifier = Modifier.size(16.dp).background(Color(0xFF10B981), CircleShape))
                Box(modifier = Modifier.size(16.dp).background(Color(0xFFF59E0B), CircleShape))
            }
        }
    }
}

@Composable
fun SettingsMini() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(4.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text("Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 8.sp)
        Spacer(modifier = Modifier.height(4.dp))
        // horizontal line boxes
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color.DarkGray, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color.DarkGray, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color.DarkGray, RoundedCornerShape(2.dp)))
    }
}

@Composable
fun YouTubeMini() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(2.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text("YouTube", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        // Mock Video
        Box(modifier = Modifier.fillMaxWidth().height(45.dp).background(Color.DarkGray, RoundedCornerShape(2.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color.Gray))
    }
}

@Composable
fun BrowserMini() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(4.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color(0xFFF1F3F4), RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.height(20.dp))
        Text("Google", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun CameraMini() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color(0xFF222222), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun DefaultMini(appName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E293B))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Android, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(appName, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}
