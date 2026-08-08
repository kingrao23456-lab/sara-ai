package com.example.presentation.ui.automation

import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AutomationTask
import com.example.presentation.viewmodel.AutomationViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(
    viewModel: AutomationViewModel
) {
    val tasks by viewModel.tasks.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Active Automations, 1 = Execution History

    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var selectedTrigger by remember { mutableStateOf("Time (Daily at 8 AM)") }
    var selectedAction by remember { mutableStateOf("AI Summarize Briefing") }

    val triggers = listOf(
        "Time (Daily at 8 AM)",
        "Battery Level (< 20%)",
        "Charging Connected",
        "Wi-Fi Connected",
        "Bluetooth Device Connected",
        "Headphones Connected",
        "Location Arrival (Office)",
        "Device Boot Completed",
        "Notification Received",
        "Manual Trigger"
    )

    val actions = listOf(
        "AI Summarize Briefing",
        "AI Translate Message",
        "AI Explain Screen Context",
        "Open App",
        "Open Website",
        "Open Camera",
        "Create Note",
        "Set Alarm (7:00 AM)",
        "Calendar Event",
        "Play Music"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Automation Hub & Background Routines", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = NeonPurpleBright,
                contentColor = AmoledBlack,
                modifier = Modifier.testTag("add_automation_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Automation")
            }
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tab Header
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceDark,
                contentColor = NeonPurpleBright,
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active Automations (${tasks.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Execution History", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            // Hero Manual Trigger Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderPurpleGlow, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonPurpleBright, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("WorkManager Intelligent Background Engine", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Trigger manual briefings or customize triggers & actions.", color = TextSecondaryDark, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.triggerManualBriefing() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurplePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trigger_briefing_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AmoledBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Trigger Morning Briefing Now", color = AmoledBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (selectedTab == 0) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(tasks, key = { it.id }) { task ->
                        AutomationTaskCard(
                            task = task,
                            onToggle = { isChecked -> viewModel.toggleTask(task.id, isChecked) },
                            onRunNow = { viewModel.runAutomationNow(task) },
                            onDelete = { viewModel.deleteAutomation(task.id) }
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val historyItems = notifications.filter { it.type == "AUTOMATION" || it.type == "AI_INSIGHT" }
                    if (historyItems.isEmpty()) {
                        item {
                            Text("No execution history logs yet. Trigger an automation above!", color = TextSecondaryDark, fontSize = 13.sp)
                        }
                    } else {
                        items(historyItems, key = { it.id }) { item ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(item.title, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(item.type, color = SoftPinkAccent, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.message, color = TextPrimaryDark, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Custom Automation Rule", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Automation Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Select Trigger Condition:", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(triggers) { trig ->
                            FilterChip(
                                selected = selectedTrigger == trig,
                                onClick = { selectedTrigger = trig },
                                label = { Text(trig, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonPurpleBright, selectedLabelColor = AmoledBlack)
                            )
                        }
                    }

                    Text("Select Action:", color = NeonPurpleBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(actions) { act ->
                            FilterChip(
                                selected = selectedAction == act,
                                onClick = { selectedAction = act },
                                label = { Text(act, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonCyan, selectedLabelColor = AmoledBlack)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.addAutomation(newTitle, newDescription.ifBlank { "Custom user rule" }, selectedTrigger, selectedAction)
                            showCreateDialog = false
                            newTitle = ""
                            newDescription = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright)
                ) {
                    Text("Save Rule", color = AmoledBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun AutomationTaskCard(
    task: AutomationTask,
    onToggle: (Boolean) -> Unit,
    onRunNow: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(task.title, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(task.description, color = TextSecondaryDark, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(color = SurfaceVariantDark, shape = RoundedCornerShape(50)) {
                        Text(task.cronSchedule, color = SoftPinkAccent, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Switch(
                    checked = task.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = NeonPurpleBright, checkedTrackColor = SurfaceVariantDark)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRunNow) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run Now", tint = NeonCyan)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SoftPinkAccent)
                }
            }
        }
    }
}
