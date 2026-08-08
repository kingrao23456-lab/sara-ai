package com.example.presentation.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class NoteItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class PromptTemplate(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val promptText: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    onSendPromptToChat: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Scratchpad & Notes, 1: Templates & Quick Actions, 2: File Tools
    val clipboardManager = LocalClipboardManager.current

    // Local Notes State
    val notes = remember {
        mutableStateListOf(
            NoteItem(
                title = "Kotlin Clean Architecture Notes",
                content = "Remember to use UseCases and constructor injection. StateFlow for ViewStates.",
                category = "Coding"
            ),
            NoteItem(
                title = "Sara AI Daily Routine Plan",
                content = "Morning briefing at 8 AM, workout reminder at 6 PM.",
                category = "Routine"
            ),
            NoteItem(
                title = "Gemini Vision Prompt Template",
                content = "Describe what you see in this image and list key details.",
                category = "Prompts"
            )
        )
    }

    var scratchpadText by remember { mutableStateOf("") }
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    // Sample Templates
    val templates = remember {
        listOf(
            PromptTemplate("1", "Summarize Text", Icons.Default.Summarize, "Summarize the following text in bullet points with key takeaways:\n\n", "Quick Action"),
            PromptTemplate("2", "Improve Writing", Icons.Default.EditNote, "Rewrite and polish this paragraph for maximum clarity and professional tone:\n\n", "Quick Action"),
            PromptTemplate("3", "Generate Code", Icons.Default.Code, "Write a production-ready Jetpack Compose component in Kotlin for:\n\n", "Coding"),
            PromptTemplate("4", "Fix Bug & Refactor", Icons.Default.BugReport, "Find bugs, explain issues, and fix this code block:\n\n", "Coding"),
            PromptTemplate("5", "Create Markdown Table", Icons.Default.TableChart, "Organize the following information into a structured Markdown table:\n\n", "Format"),
            PromptTemplate("6", "Draft Professional Email", Icons.Default.Email, "Draft a polite and concise professional email regarding:\n\n", "Writing"),
            PromptTemplate("7", "Translate to Spanish", Icons.Default.Translate, "Translate the following message into fluent Spanish:\n\n", "Translation"),
            PromptTemplate("8", "Explain Simply", Icons.Default.Help, "Explain this complex topic as if I am 10 years old:\n\n", "Explanation")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Workspace & Tools", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
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
            // Workspace Tab Switcher
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceDark,
                contentColor = NeonPurpleBright,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Notes & Scratch", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Quick Templates", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("File Explainer", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // Notes & Scratchpad View
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            // Quick Scratchpad
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderPurpleGlow, RoundedCornerShape(20.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("⚡ Quick Scratchpad", color = NeonPurpleBright, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.getText()?.let {
                                                        scratchpadText += it.text
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = NeonCyan, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    if (scratchpadText.isNotBlank()) {
                                                        onSendPromptToChat(scratchpadText)
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Send, contentDescription = "Send to Chat", tint = SoftPinkAccent, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = scratchpadText,
                                        onValueChange = { scratchpadText = it },
                                        placeholder = { Text("Type quick thoughts or paste text to summarize/edit...", color = TextMuted, fontSize = 13.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonPurpleBright,
                                            unfocusedBorderColor = BorderPurpleGlow,
                                            focusedTextColor = TextPrimaryDark,
                                            unfocusedTextColor = TextPrimaryDark
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .testTag("scratchpad_input")
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📝 Saved AI Notes (${notes.size})", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Button(
                                    onClick = { showAddNoteDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurplePrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("add_note_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = AmoledBlack, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("New Note", color = AmoledBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        items(notes, key = { it.id }) { note ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(note.title, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Surface(
                                            color = NeonPurplePrimary.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                note.category,
                                                color = NeonPurpleBright,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(note.content, color = TextSecondaryDark, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        TextButton(
                                            onClick = { onSendPromptToChat("Explain and elaborate on this note:\n${note.content}") }
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Ask AI", color = NeonCyan, fontSize = 11.sp)
                                        }
                                        IconButton(
                                            onClick = { notes.remove(note) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SoftPinkAccent, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Quick Action Templates View
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text("⚡ AI Quick Action Prompts", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Tap any template to launch directly into Gemini chat", color = TextSecondaryDark, fontSize = 12.sp)
                        }

                        items(templates, key = { it.id }) { template ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSendPromptToChat(template.promptText) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(NeonPurplePrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(template.icon, contentDescription = null, tint = NeonPurpleBright)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(template.title, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(template.promptText, color = TextSecondaryDark, fontSize = 11.sp, maxLines = 1)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // File Explainer View
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = SoftPinkAccent, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Summarize & Explain Any File", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Supports PDF, DOCX, TXT, CSV, JSON, and Markdown files", color = TextSecondaryDark, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        onSendPromptToChat("Please analyze and summarize the attached document content, highlighting core insights, key metrics, and action items.")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurplePrimary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = AmoledBlack)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pick & Analyze File", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text("Recent File Analysis History", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = SoftPinkAccent)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Project_Architecture_Doc.pdf", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Processed • 3 pages summarized", color = NeonCyan, fontSize = 11.sp)
                                }
                                TextButton(onClick = { onSendPromptToChat("Summarize Project_Architecture_Doc.pdf") }) {
                                    Text("Re-analyze", color = NeonPurpleBright, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // New Note Dialog
            if (showAddNoteDialog) {
                AlertDialog(
                    onDismissRequest = { showAddNoteDialog = false },
                    title = { Text("Create AI Note", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = newNoteTitle,
                                onValueChange = { newNoteTitle = it },
                                label = { Text("Note Title", color = TextSecondaryDark) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonPurpleBright,
                                    unfocusedBorderColor = BorderPurpleGlow,
                                    focusedTextColor = TextPrimaryDark,
                                    unfocusedTextColor = TextPrimaryDark
                                )
                            )
                            OutlinedTextField(
                                value = newNoteContent,
                                onValueChange = { newNoteContent = it },
                                label = { Text("Note Content", color = TextSecondaryDark) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonPurpleBright,
                                    unfocusedBorderColor = BorderPurpleGlow,
                                    focusedTextColor = TextPrimaryDark,
                                    unfocusedTextColor = TextPrimaryDark
                                ),
                                modifier = Modifier.height(100.dp)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newNoteTitle.isNotBlank() && newNoteContent.isNotBlank()) {
                                    notes.add(0, NoteItem(title = newNoteTitle, content = newNoteContent, category = "Personal"))
                                    newNoteTitle = ""
                                    newNoteContent = ""
                                    showAddNoteDialog = false
                                }
                            }
                        ) {
                            Text("Save Note", color = NeonPurpleBright, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddNoteDialog = false }) {
                            Text("Cancel", color = TextMuted)
                        }
                    },
                    containerColor = SurfaceDark
                )
            }
        }
    }
}
