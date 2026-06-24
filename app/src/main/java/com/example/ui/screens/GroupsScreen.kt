package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun GroupsScreen(
    viewModel: AdbViewModel,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsState()
    val groups by viewModel.groups.collectAsState()

    var newGroupName by remember { mutableStateOf("") }
    var expandedGroupBulkCmdMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("groups_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Group Creator Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Create Device Group",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newGroupName,
                            onValueChange = { newGroupName = it },
                            placeholder = { Text("Group Name (e.g. Device Farm 1)") },
                            modifier = Modifier.weight(1f).testTag("group_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                if (newGroupName.isNotBlank()) {
                                    viewModel.addGroup(newGroupName)
                                    newGroupName = ""
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("create_group_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Create Group")
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Device Groups Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Group List items
        items(groups) { groupName ->
            val groupDevices = devices.filter { it.groupName == groupName }
            val onlineCount = groupDevices.count { it.status == DeviceStatus.ONLINE }
            val bulkCmd = expandedGroupBulkCmdMap[groupName] ?: ""

            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("group_card_$groupName")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Group Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.FolderShared,
                                contentDescription = null,
                                tint = ColorConnected,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    groupName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    "${groupDevices.size} devices assigned • $onlineCount online",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Play script icon indicators
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "GROUP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Group Members list
                    if (groupDevices.isEmpty()) {
                        Text(
                            "No devices in this group. Assign devices from the dashboard list.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            groupDevices.forEach { dev ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (dev.connectionType == ConnectionType.USB_OTG) Icons.Default.Usb else Icons.Default.Wifi,
                                            contentDescription = null,
                                            tint = if (dev.connectionType == ConnectionType.USB_OTG) ColorPurple else WifiIndicator,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            dev.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "(${dev.brand})",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            color = if (dev.connectionType == ConnectionType.USB_OTG) ColorPurple.copy(alpha = 0.2f) else VineBadgeBg.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(2.dp),
                                            border = BorderStroke(1.dp, if (dev.connectionType == ConnectionType.USB_OTG) ColorPurple.copy(alpha = 0.5f) else ColorConnected.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                if (dev.connectionType == ConnectionType.USB_OTG) "USB ('sub')" else "Vine",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (dev.connectionType == ConnectionType.USB_OTG) TextSecondary else ColorConnected,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (dev.status == DeviceStatus.ONLINE) ColorConnected else ColorOffline)
                                        )
                                        Text(
                                            dev.status.name,
                                            fontSize = 9.sp,
                                            color = if (dev.status == DeviceStatus.ONLINE) ColorConnected else ColorOffline,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Group Level Broadcaster
                    Text(
                        "GROUP ADB BULK CONTROLS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quick Home
                        OutlinedButton(
                            onClick = { viewModel.executeBulkCommand(groupName, "input keyevent 3") },
                            enabled = onlineCount > 0,
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Go Home", fontSize = 11.sp)
                        }

                        // Quick Power Wake
                        OutlinedButton(
                            onClick = { viewModel.executeBulkCommand(groupName, "input keyevent 26") },
                            enabled = onlineCount > 0,
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Power", fontSize = 11.sp)
                        }

                        // Open Settings Bulk
                        OutlinedButton(
                            onClick = { viewModel.executeBulkCommand(groupName, "am start -a android.settings.SETTINGS") },
                            enabled = onlineCount > 0,
                            modifier = Modifier.weight(1.2f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Settings", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bulk Terminal Box for this specific group
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = bulkCmd,
                            onValueChange = { text ->
                                expandedGroupBulkCmdMap = expandedGroupBulkCmdMap + (groupName to text)
                            },
                            placeholder = { Text("input text 'Group'", fontSize = 11.sp) },
                            singleLine = true,
                            enabled = onlineCount > 0,
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.5f).height(42.dp)
                        )
                        Button(
                            onClick = {
                                if (bulkCmd.isNotBlank()) {
                                    viewModel.executeBulkCommand(groupName, bulkCmd)
                                    expandedGroupBulkCmdMap = expandedGroupBulkCmdMap + (groupName to "")
                                }
                            },
                            enabled = onlineCount > 0 && bulkCmd.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(42.dp).testTag("broadcast_btn_$groupName")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Broadcast", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
