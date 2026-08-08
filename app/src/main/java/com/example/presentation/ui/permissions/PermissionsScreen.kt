package com.example.presentation.ui.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    var isGranted: Boolean,
    val isRequired: Boolean = true,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen() {
    val context = LocalContext.current

    val permissionsList = remember {
        mutableStateListOf(
            PermissionItem("mic", "Microphone", "Required for continuous voice-to-voice mode and wake word.", Icons.Default.Mic, true, true, "Core AI"),
            PermissionItem("camera", "Camera & Vision", "Required for CameraX photo analysis and OCR text extraction.", Icons.Default.CameraAlt, true, true, "Core AI"),
            PermissionItem("location", "Location Context", "Required for Google Maps Grounding & local recommendations.", Icons.Default.Place, true, false, "Context"),
            PermissionItem("notifications", "System Notifications", "Required for daily AI briefings, reminders and quick replies.", Icons.Default.Notifications, true, false, "System"),
            PermissionItem("contacts", "Contacts", "Required to call or message contacts via voice commands.", Icons.Default.Contacts, true, false, "Communication"),
            PermissionItem("calendar", "Calendar", "Required to create, edit, and remind meeting events.", Icons.Default.CalendarToday, true, false, "Productivity"),
            PermissionItem("storage", "Storage & Photos", "Required to browse, save, and analyze files and images.", Icons.Default.Folder, true, false, "Media"),
            PermissionItem("bluetooth", "Bluetooth Devices", "Required to connect audio headsets and smart accessories.", Icons.Default.Bluetooth, true, false, "Hardware"),
            PermissionItem("overlay", "System Overlay Bubble", "Required for floating draggable assistant bubble over apps.", Icons.Default.Layers, true, false, "Assistant"),
            PermissionItem("accessibility", "Accessibility Service", "Optional service for screen reader and UI automated tasks.", Icons.Default.AccessibilityNew, false, false, "Accessibility"),
            PermissionItem("battery", "Disable Battery Optimization", "Required to keep background AI voice & wake word active.", Icons.Default.BatterySaver, true, false, "System"),
            PermissionItem("alarm", "Exact Alarms & Timers", "Required for precise countdown timers and morning alarms.", Icons.Default.Alarm, true, false, "Productivity")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permission Center", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "App Settings", tint = NeonPurpleBright)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Privacy & Permission Transparency", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Sara AI uses official Android SDK APIs to perform local device tasks. Permissions are requested only when needed. You can manage or revoke permissions anytime in system settings.",
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = AmoledBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Android App Settings", color = AmoledBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(permissionsList, key = { it.id }) { item ->
                PermissionCard(
                    permissionItem = item,
                    onToggle = {
                        val index = permissionsList.indexOfFirst { p -> p.id == item.id }
                        if (index != -1) {
                            permissionsList[index] = permissionsList[index].copy(isGranted = !permissionsList[index].isGranted)
                        }
                    },
                    testTag = "perm_${item.id}_card"
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    permissionItem: PermissionItem,
    onToggle: () -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (permissionItem.isGranted) BorderPurpleGlow else SoftPinkAccent.copy(alpha = 0.5f),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(permissionItem.icon, contentDescription = null, tint = if (permissionItem.isGranted) SoftPinkAccent else TextMuted, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(permissionItem.title, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = NeonCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(permissionItem.category, color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(permissionItem.description, color = TextSecondaryDark, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Switch(
                checked = permissionItem.isGranted,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AmoledBlack,
                    checkedTrackColor = NeonCyan,
                    uncheckedThumbColor = AmoledBlack,
                    uncheckedTrackColor = SurfaceVariantDark
                )
            )
        }
    }
}

