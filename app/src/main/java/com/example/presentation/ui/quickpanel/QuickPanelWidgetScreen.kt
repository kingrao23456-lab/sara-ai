package com.example.presentation.ui.quickpanel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPanelWidgetScreen(
    onNavigate: (String) -> Unit
) {
    var floatingBubbleEnabled by remember { mutableStateOf(true) }
    var screenshotShortcutEnabled by remember { mutableStateOf(true) }
    var voiceShortcutEnabled by remember { mutableStateOf(true) }

    val quickActions = listOf(
        QuickActionItem("Chat AI", Icons.Default.Chat, "chat"),
        QuickActionItem("Live Voice", Icons.Default.Mic, "voice"),
        QuickActionItem("Live Camera", Icons.Default.CameraAlt, "vision"),
        QuickActionItem("OCR Extractor", Icons.Default.DocumentScanner, "qrocr"),
        QuickActionItem("Document AI", Icons.Default.Description, "documents"),
        QuickActionItem("AI Notes", Icons.Default.NoteAlt, "notestasks"),
        QuickActionItem("Scheduler", Icons.Default.CalendarToday, "planner"),
        QuickActionItem("Automations", Icons.Default.AutoAwesome, "automation")
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Quick Panel & Home Widgets", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Panel Shortcuts Grid
            item {
                Text("Quick Panel AI Shortcuts", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val rows = quickActions.chunked(4)
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { action ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onNavigate(action.route) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(action.icon, contentDescription = null, tint = NeonPurpleBright, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(action.title, color = TextPrimaryDark, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating Assistant Controls
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = NeonPurpleBright)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Floating Assistant Overlay Controls", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Always Available Overlay Bubble", color = TextPrimaryDark, fontSize = 13.sp)
                            Switch(
                                checked = floatingBubbleEnabled,
                                onCheckedChange = { floatingBubbleEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = NeonPurpleBright)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Floating Screenshot Shortcut", color = TextPrimaryDark, fontSize = 13.sp)
                            Switch(
                                checked = screenshotShortcutEnabled,
                                onCheckedChange = { screenshotShortcutEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = NeonCyan)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Floating Voice Assistant Shortcut", color = TextPrimaryDark, fontSize = 13.sp)
                            Switch(
                                checked = voiceShortcutEnabled,
                                onCheckedChange = { voiceShortcutEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = SoftPinkAccent)
                            )
                        }
                    }
                }
            }

            // Android Home Screen Widgets Config
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderPurpleGlow, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Android Home Screen Widgets", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Configure home screen widgets for fast 1-tap access to Sara AI features.", color = TextSecondaryDark, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { coroutineScope.launch { snackbarHostState.showSnackbar("Pinned AI Chat Widget to Home Screen!") } },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Pin Chat Widget", color = AmoledBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { coroutineScope.launch { snackbarHostState.showSnackbar("Pinned Voice Widget to Home Screen!") } },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Pin Voice Widget", color = AmoledBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
