package com.example.presentation.ui.notes

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

data class NoteItem(
    val id: String,
    val title: String,
    val body: String,
    val folder: String,
    val isPinned: Boolean = false,
    val type: String = "TEXT" // TEXT, CHECKLIST, VOICE, IMAGE
)

data class TaskItem(
    val id: String,
    val title: String,
    val priority: String, // HIGH, MEDIUM, LOW
    val dueDate: String,
    val label: String,
    var isDone: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTasksScreen(
    onSendToChat: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = AI Notes, 1 = Task Manager

    val notesList = remember {
        mutableStateListOf(
            NoteItem("1", "Sara AI System Spec 2028", "Multi-modal vision, continuous voice stream, local Room database, and encrypted Keystore.", "Work", true, "TEXT"),
            NoteItem("2", "Grocery Checklist", "[x] Almond Milk\n[ ] Protein Bar\n[ ] Greek Yogurt", "Personal", false, "CHECKLIST"),
            NoteItem("3", "Voice Memo: Architecture Ideas", "Recorded 2 mins ago: Focus on zero latency response using Gemini 2.5 Flash model.", "Voice Notes", true, "VOICE")
        )
    }

    val tasksList = remember {
        mutableStateListOf(
            TaskItem("1", "Finalize Android Assistant Overlay layout", "HIGH", "Today", "Development"),
            TaskItem("2", "Test WorkManager background execution", "HIGH", "Tomorrow", "QA"),
            TaskItem("3", "Review privacy & runtime permissions", "MEDIUM", "Aug 5", "Security")
        )
    }

    var noteSearchQuery by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf("All") }
    val folders = listOf("All", "Work", "Personal", "Voice Notes", "Archived")

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteBody by remember { mutableStateOf("") }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskPriority by remember { mutableStateOf("HIGH") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("AI Notes & Smart Task Manager", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddNoteDialog = true else showAddTaskDialog = true
                },
                containerColor = NeonPurpleBright,
                contentColor = AmoledBlack,
                modifier = Modifier.testTag("add_note_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
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
                    text = { Text("AI Notes (${notesList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.NoteAlt, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Task Manager (${tasksList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // Notes Section
                OutlinedTextField(
                    value = noteSearchQuery,
                    onValueChange = { noteSearchQuery = it },
                    placeholder = { Text("Search notes by title or content...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(folders) { folder ->
                        FilterChip(
                            selected = selectedFolder == folder,
                            onClick = { selectedFolder = folder },
                            label = { Text(folder, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonPurpleBright, selectedLabelColor = AmoledBlack)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val filteredNotes = notesList.filter {
                    (selectedFolder == "All" || it.folder == selectedFolder) &&
                            (noteSearchQuery.isBlank() || it.title.contains(noteSearchQuery, ignoreCase = true) || it.body.contains(noteSearchQuery, ignoreCase = true))
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredNotes, key = { it.id }) { note ->
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (note.isPinned) {
                                            Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = SoftPinkAccent, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(note.title, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    Surface(color = AmoledBlack, shape = RoundedCornerShape(50)) {
                                        Text(note.folder, color = NeonCyan, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(note.body, color = TextSecondaryDark, fontSize = 12.sp, maxLines = 3)

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { onSendToChat("Summarize and elaborate on this note:\nTitle: ${note.title}\nContent: ${note.body}") },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("AI Summarize Note", color = AmoledBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Task Manager Section
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
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonPurpleBright)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sara Task Prioritization AI", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Suggested next task: Finalize Android Assistant Overlay layout (High Priority)", color = TextSecondaryDark, fontSize = 12.sp)
                            }
                        }
                    }

                    items(tasksList, key = { it.id }) { task ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isDone,
                                    onCheckedChange = { isChecked ->
                                        val index = tasksList.indexOf(task)
                                        if (index != -1) {
                                            tasksList[index] = task.copy(isDone = isChecked)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = NeonPurpleBright)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        task.title,
                                        color = if (task.isDone) TextMuted else TextPrimaryDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(color = AmoledBlack, shape = RoundedCornerShape(50)) {
                                            Text("Due: ${task.dueDate}", color = SoftPinkAccent, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Surface(color = SurfaceVariantDark, shape = RoundedCornerShape(50)) {
                                            Text(task.label, color = NeonCyan, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }

                                IconButton(onClick = { tasksList.remove(task) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = SoftPinkAccent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Create AI Note", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newNoteTitle,
                        onValueChange = { newNoteTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newNoteBody,
                        onValueChange = { newNoteBody = it },
                        label = { Text("Content / Note Body") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNoteTitle.isNotBlank()) {
                            notesList.add(0, NoteItem(java.util.UUID.randomUUID().toString(), newNoteTitle, newNoteBody, "Work"))
                            showAddNoteDialog = false
                            newNoteTitle = ""
                            newNoteBody = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright)
                ) {
                    Text("Save Note", color = AmoledBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) { Text("Cancel", color = TextSecondaryDark) }
            },
            containerColor = SurfaceDark
        )
    }

    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add New Task", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Task Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            tasksList.add(0, TaskItem(java.util.UUID.randomUUID().toString(), newTaskTitle, newTaskPriority, "Today", "General"))
                            showAddTaskDialog = false
                            newTaskTitle = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Add Task", color = AmoledBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancel", color = TextSecondaryDark) }
            },
            containerColor = SurfaceDark
        )
    }
}
