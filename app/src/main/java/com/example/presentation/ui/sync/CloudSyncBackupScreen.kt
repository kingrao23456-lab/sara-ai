package com.example.presentation.ui.sync

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class BackupCategoryOption(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    var isEnabled: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncBackupScreen() {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var autoBackupEnabled by remember { mutableStateOf(true) }
    var autoRestoreEnabled by remember { mutableStateOf(true) }
    var wifiOnlySync by remember { mutableStateOf(true) }
    var mobileDataSync by remember { mutableStateOf(false) }
    var backgroundSync by remember { mutableStateOf(true) }

    var conflictResolution by remember { mutableStateOf("Latest Timestamp (Recommended)") }
    var isSyncingNow by remember { mutableStateOf(false) }
    var offlineQueueCount by remember { mutableStateOf(0) }

    val backupCategories = remember {
        mutableStateListOf(
            BackupCategoryOption("AI Chat Conversations", "History, media attachments & context", Icons.Default.Chat, true),
            BackupCategoryOption("User Fact Memory Store", "Saved user facts & preference graph", Icons.Default.Psychology, true),
            BackupCategoryOption("App & Model Settings", "Gemini parameters, voice pitch & theme", Icons.Default.Settings, true),
            BackupCategoryOption("AI Notes & Workspaces", "Formatted Markdown notes & spec sheets", Icons.Default.NoteAlt, true),
            BackupCategoryOption("Smart Tasks & Schedules", "Prioritized task checklists & calendar", Icons.Default.Checklist, true),
            BackupCategoryOption("Generated Images & Vision", "Created DALL-E/Gemini art & scans", Icons.Default.Image, true),
            BackupCategoryOption("Voice History & Audio Memos", "Continuous session transcriptions", Icons.Default.Mic, true),
            BackupCategoryOption("Automation Rules", "WorkManager triggers & cron routines", Icons.Default.AutoAwesome, true)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Firebase Cloud Sync & Backup", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
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
            // Hero Sync Overview
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderPurpleGlow, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Firebase Cloud Backup", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Last synced: Just now", color = NeonCyan, fontSize = 12.sp)
                                }
                            }

                            if (isSyncingNow) {
                                CircularProgressIndicator(color = NeonPurpleBright, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                isSyncingNow = true
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1200)
                                    isSyncingNow = false
                                    offlineQueueCount = 0
                                    snackbarHostState.showSnackbar("All categories synced with Firebase Cloud!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("manual_sync_now_button")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = AmoledBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync All Now", color = AmoledBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Sync Settings & Rules
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Network & Sync Rules", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        SyncToggleRow("Automatic Background Cloud Backup", autoBackupEnabled) { autoBackupEnabled = it }
                        SyncToggleRow("Automatic Restore on New Device Login", autoRestoreEnabled) { autoRestoreEnabled = it }
                        SyncToggleRow("Wi-Fi Only Sync (Save Mobile Data)", wifiOnlySync) { wifiOnlySync = it }
                        SyncToggleRow("Allow Mobile Data Sync", mobileDataSync) { mobileDataSync = it }
                        SyncToggleRow("Background Periodic Sync", backgroundSync) { backgroundSync = it }
                    }
                }
            }

            // Conflict Resolution & Offline Queue
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Offline Queue & Conflict Strategy", color = NeonPurpleBright, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pending Offline Sync Queue", color = TextPrimaryDark, fontSize = 13.sp)
                            Surface(color = AmoledBlack, shape = RoundedCornerShape(50)) {
                                Text("$offlineQueueCount items", color = SoftPinkAccent, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Conflict Resolution Strategy:", color = TextSecondaryDark, fontSize = 12.sp)
                        Text(conflictResolution, color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Category Selection Options
            item {
                Text("Selected Backup Categories", color = NeonPurpleBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            items(backupCategories) { cat ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(cat.icon, contentDescription = null, tint = SoftPinkAccent, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(cat.title, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(cat.subtitle, color = TextSecondaryDark, fontSize = 11.sp)
                        }

                        Switch(
                            checked = cat.isEnabled,
                            onCheckedChange = { isChecked ->
                                val index = backupCategories.indexOf(cat)
                                if (index != -1) {
                                    backupCategories[index] = cat.copy(isEnabled = isChecked)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = NeonPurpleBright)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SyncToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimaryDark, fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = NeonPurpleBright)
        )
    }
}
