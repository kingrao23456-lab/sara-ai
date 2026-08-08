package com.example.presentation.ui.planner

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class RoutineItem(
    val title: String,
    val time: String,
    val category: String,
    val description: String,
    val isCompleted: Boolean = false
)

data class TimeBlockItem(
    val timeSlot: String,
    val activity: String,
    val priority: String,
    val isDone: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerRoutinesScreen(
    onSendToChat: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Planner & Time Blocking, 1 = Smart Routines, 2 = Reminders

    val timeBlocks = remember {
        mutableStateListOf(
            TimeBlockItem("08:00 - 09:00", "Morning AI Briefing & Fitness Routine", "HIGH"),
            TimeBlockItem("09:30 - 11:30", "Deep Work: Jetpack Compose & Android AI", "URGENT"),
            TimeBlockItem("12:00 - 13:00", "Lunch & Mindfulness Meditation", "MEDIUM"),
            TimeBlockItem("14:00 - 16:00", "Sara AI System Testing & Code Review", "HIGH"),
            TimeBlockItem("17:00 - 18:00", "Study Routine & Technical Research", "LOW")
        )
    }

    val routinesList = listOf(
        RoutineItem("Morning Routine", "7:00 AM", "Morning", "Stretch, Drink Water, Sara AI Morning Briefing, Review Daily Tasks"),
        RoutineItem("Study Routine", "10:00 AM", "Study", "Pomodoro Focus (25m), Note Synthesis, Quiz Review with Sara AI"),
        RoutineItem("Work Routine", "1:00 PM", "Work", "Check Priority Emails, Time Blocking, GitHub Sync"),
        RoutineItem("Fitness Routine", "5:30 PM", "Fitness", "30 Min Cardio & Core, Log Workout Stats"),
        RoutineItem("Sleep Routine", "10:30 PM", "Sleep", "Dim Lights, Sara AI Sleep Wind-Down Story, Alarm Set")
    )

    var newReminderTitle by remember { mutableStateOf("") }
    var newReminderTime by remember { mutableStateOf("08:00 AM") }
    val remindersList = remember {
        mutableStateListOf(
            "Water Hydration Goal (2L)" to "09:00 AM",
            "Team Sprint Sync on Google Meet" to "02:00 PM",
            "Evening Memory Consolidation with Sara" to "09:00 PM"
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("AI Scheduler & Routine Planner", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceDark,
                contentColor = NeonPurpleBright,
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Time Blocking", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Smart Routines", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Reminders", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // Time Blocking & Smart Planner
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderPurpleGlow, RoundedCornerShape(18.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AI Schedule Assistant Suggestions", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Sara suggests inserting a 15-min break between 11:30 AM and 12:00 PM for optimal focus.", color = TextSecondaryDark, fontSize = 12.sp)

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { onSendToChat("Optimize my daily schedule for maximum productivity and balance.") },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Ask Sara to Re-Balance Schedule", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        items(timeBlocks) { block ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = AmoledBlack,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(block.timeSlot, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(block.activity, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Surface(
                                            color = when (block.priority) {
                                                "URGENT" -> SoftPinkAccent.copy(alpha = 0.2f)
                                                "HIGH" -> NeonPurpleBright.copy(alpha = 0.2f)
                                                else -> SurfaceVariantDark
                                            },
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(block.priority, color = TextPrimaryDark, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Smart Routines
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(routinesList) { routine ->
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
                                        Text(routine.title, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Surface(color = AmoledBlack, shape = RoundedCornerShape(50)) {
                                            Text(routine.time, color = SoftPinkAccent, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(routine.description, color = TextSecondaryDark, fontSize = 12.sp, lineHeight = 16.sp)

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            coroutineScope.launch { snackbarHostState.showSnackbar("Activated ${routine.title}!") }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AmoledBlack)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Start Routine", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Reminders
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Add Smart Reminder", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = newReminderTitle,
                                        onValueChange = { newReminderTitle = it },
                                        label = { Text("Reminder Task") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            if (newReminderTitle.isNotBlank()) {
                                                remindersList.add(newReminderTitle to newReminderTime)
                                                newReminderTitle = ""
                                                coroutineScope.launch { snackbarHostState.showSnackbar("Reminder added!") }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Save Reminder", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        items(remindersList) { reminder ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(reminder.first, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(reminder.second, color = SoftPinkAccent, fontSize = 11.sp)
                                    }

                                    IconButton(onClick = {
                                        remindersList.remove(reminder)
                                    }) {
                                        Icon(Icons.Default.Check, contentDescription = "Complete", tint = NeonCyan)
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
