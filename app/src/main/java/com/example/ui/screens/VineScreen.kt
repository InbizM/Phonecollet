package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeviceStatus
import com.example.data.EmbeddedLink
import com.example.ui.theme.*
import com.example.viewmodel.AdbViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VineScreen(
    viewModel: AdbViewModel,
    modifier: Modifier = Modifier
) {
    val links by viewModel.links.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val activeDevices = devices.filter { it.status == DeviceStatus.ONLINE }

    val context = LocalContext.current

    var showAddLinkSection by remember { mutableStateOf(false) }
    var linkTitle by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }
    var linkCategory by remember { mutableStateOf("Documentation") }
    var linkDesc by remember { mutableStateOf("") }

    var selectedDeviceToPush by remember { mutableStateOf<String?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    // Auto select first active device if any
    LaunchedEffect(activeDevices) {
        if (selectedDeviceToPush == null || activeDevices.none { it.id == selectedDeviceToPush }) {
            selectedDeviceToPush = activeDevices.firstOrNull()?.id
        }
    }

    val categories = listOf("All") + links.map { it.category }.distinct()
    val filteredLinks = if (selectedCategoryFilter == "All") links else links.filter { it.category == selectedCategoryFilter }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("vine_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header for "Vine Connections & Links" Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgCardDark),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(VineBadgeBg, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Wifi,
                                contentDescription = null,
                                tint = ColorConnected,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Vine Connection Hub",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                "Wireless & USB Debugging Console",
                                style = MaterialTheme.typography.labelSmall,
                                color = ColorConnected,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Manage helpful embedded resource links, browser directories, or push target websites directly to connected devices with a single click.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dual Connection Info Cards (Vine Wireless vs USB Sub-app)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BgCard),
                            border = BorderStroke(1.dp, VineIndicator.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.SettingsInputAntenna,
                                        contentDescription = null,
                                        tint = VineIndicator,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Vine Connection",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "A high-speed, wireless connection type equivalent to standard ADB over Wi-Fi.",
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = BgCard),
                            border = BorderStroke(1.dp, ColorPurple.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Usb,
                                        contentDescription = null,
                                        tint = ColorPurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "USB Connection",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Physical wired connection. Treated as a separate, hardware-linked module.",
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Custom Embedded Link Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgCardDark),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Add Custom Embedded Link",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        IconButton(
                            onClick = { showAddLinkSection = !showAddLinkSection },
                            modifier = Modifier.testTag("add_link_toggle")
                        ) {
                            Icon(
                                if (showAddLinkSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    }

                    AnimatedVisibility(visible = showAddLinkSection) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = linkTitle,
                                onValueChange = { linkTitle = it },
                                label = { Text("Title") },
                                modifier = Modifier.fillMaxWidth().testTag("link_title_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = linkUrl,
                                onValueChange = { linkUrl = it },
                                label = { Text("URL Link (e.g. https://...)") },
                                modifier = Modifier.fillMaxWidth().testTag("link_url_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = linkDesc,
                                onValueChange = { linkDesc = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth().testTag("link_desc_input"),
                                singleLine = true
                            )

                            // Category Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Documentation", "ADB Tools", "Automation").forEach { cat ->
                                        FilterChip(
                                            selected = linkCategory == cat,
                                            onClick = { linkCategory = cat },
                                            label = { Text(cat, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (linkTitle.isNotBlank() && linkUrl.isNotBlank()) {
                                        viewModel.addEmbeddedLink(linkTitle, linkUrl, linkCategory, linkDesc)
                                        linkTitle = ""
                                        linkUrl = ""
                                        linkDesc = ""
                                        showAddLinkSection = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("save_link_btn")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Embedded Link")
                            }
                        }
                    }
                }
            }
        }

        // Category Filter Row
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat) }
                    )
                }
            }
        }

        // Active Device Destination Indicator for Pushing Links
        if (activeDevices.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "ADB Push Target Phone Browser",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Send link directly to:", fontSize = 12.sp)
                            var dropdownExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { dropdownExpanded = true },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    val currentDeviceName = activeDevices.find { it.id == selectedDeviceToPush }?.name ?: "Select Phone"
                                    Text(currentDeviceName, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    activeDevices.forEach { dev ->
                                        DropdownMenuItem(
                                            text = { Text(dev.name) },
                                            onClick = {
                                                selectedDeviceToPush = dev.id
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Embedded links cards
        if (filteredLinks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No links found in this category.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            items(filteredLinks, key = { it.id }) { link ->
                VineLinkCard(
                    link = link,
                    selectedDeviceToPush = selectedDeviceToPush,
                    onFavoriteToggle = { viewModel.toggleFavoriteLink(link.id) },
                    onDelete = { viewModel.removeEmbeddedLink(link.id) },
                    onBrowserLaunch = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                        context.startActivity(browserIntent)
                    },
                    onPushToDevice = { targetDeviceId ->
                        // adb command to launch url: am start -a android.intent.action.VIEW -d "URL"
                        val adbCommand = "am start -a android.intent.action.VIEW -d \"${link.url}\""
                        viewModel.executeCustomCommand(targetDeviceId, adbCommand)
                    }
                )
            }
        }
    }
}

@Composable
fun VineLinkCard(
    link: EmbeddedLink,
    selectedDeviceToPush: String?,
    onFavoriteToggle: () -> Unit,
    onDelete: () -> Unit,
    onBrowserLaunch: () -> Unit,
    onPushToDevice: (String) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, VineIndicator),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().testTag("vine_card_${link.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            tint = VineIndicator,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            link.title,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        link.url,
                        color = VineIndicator,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { onBrowserLaunch() }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(28.dp)) {
                        Icon(
                            if (link.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (link.isFavorite) Color(0xFFFFB300) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Link",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                link.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = VineBadgeBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        link.category.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onBrowserLaunch,
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Open Web", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { selectedDeviceToPush?.let { onPushToDevice(it) } },
                        enabled = selectedDeviceToPush != null,
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp).testTag("push_link_btn_${link.id}")
                    ) {
                        Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Push to Phone", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
