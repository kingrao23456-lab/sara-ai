package com.example.presentation.ui.history

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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class DownloadFileItem(
    val name: String,
    val type: String,
    val size: String,
    val progress: Float,
    val date: String
)

data class HistoryRecord(
    val title: String,
    val category: String, // Chat, Voice, Image, OCR, Automation
    val timestamp: String,
    val details: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDownloadsScreen() {
    var selectedTab by remember { mutableStateOf(0) } // 0 = History Center, 1 = Download Center, 2 = Clipboard Manager

    val historyRecords = remember {
        mutableStateListOf(
            HistoryRecord("Gemini Live Voice Session", "Voice", "10 mins ago", "Duration: 4m 12s • 14 Messages exchanged"),
            HistoryRecord("Generated AI Art: Cyberpunk City", "Image", "1 hour ago", "Prompt: Highly detailed OLED neon city at night"),
            HistoryRecord("Document OCR Extraction", "OCR", "3 hours ago", "Scanned 2 pages • 450 words extracted"),
            HistoryRecord("Morning AI Briefing Executed", "Automation", "Today 8:00 AM", "Triggered WorkManager background routine successfully"),
            HistoryRecord("Chat Conversation: Android Architecture", "Chat", "Yesterday", "24 messages in chat session")
        )
    }

    val downloadsList = remember {
        mutableStateListOf(
            DownloadFileItem("Sara_AI_Spec_2028.pdf", "PDF", "2.4 MB", 1.0f, "Today"),
            DownloadFileItem("Cyberpunk_Neon_City_HD.png", "Image", "4.1 MB", 1.0f, "Today"),
            DownloadFileItem("Voice_Briefing_Audio.wav", "Audio", "1.8 MB", 0.85f, "Downloading..."),
            DownloadFileItem("Exported_Notes_Backup.json", "Data", "320 KB", 1.0f, "Yesterday")
        )
    }

    val clipboardHistory = remember {
        mutableStateListOf(
            "https://ai.studio/build/sara-ai",
            "Project Proposal 2028: Multi-modal generative reasoning",
            "+1 555-0199"
        )
    }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("History, Downloads & Clipboard", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
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
                    text = { Text("History", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Downloads", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Clipboard", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // History Center
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(historyRecords) { rec ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(rec.title, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Surface(color = AmoledBlack, shape = RoundedCornerShape(50)) {
                                            Text(rec.category, color = NeonCyan, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(rec.details, color = TextSecondaryDark, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(rec.timestamp, color = SoftPinkAccent, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Download Center
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(downloadsList) { file ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(file.name, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(file.size, color = TextSecondaryDark, fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    if (file.progress < 1.0f) {
                                        LinearProgressIndicator(
                                            progress = { file.progress },
                                            color = NeonCyan,
                                            trackColor = AmoledBlack,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        Surface(color = AmoledBlack, shape = RoundedCornerShape(50)) {
                                            Text("Completed (${file.date})", color = NeonPurpleBright, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Clipboard Manager
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(clipboardHistory) { item ->
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
                                    Text(item, color = TextPrimaryDark, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 2)

                                    IconButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(item))
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Copied to clipboard!") }
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan)
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
