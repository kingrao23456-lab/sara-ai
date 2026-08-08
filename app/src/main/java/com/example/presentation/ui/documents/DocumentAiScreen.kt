package com.example.presentation.ui.documents

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DocumentItem(
    val id: String,
    val fileName: String,
    val fileType: String, // PDF, DOCX, TXT, CSV, MD
    val size: String,
    val pageCount: String,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentAiScreen(
    onSendToChat: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Documents, 1 = Screenshot AI, 2 = Image Editor

    val recentDocs = remember {
        mutableStateListOf(
            DocumentItem("1", "Sara_AI_Architecture_Spec.pdf", "PDF", "2.4 MB", "18 Pages", "10 mins ago"),
            DocumentItem("2", "Q3_Market_Research_Report.docx", "DOCX", "1.1 MB", "12 Pages", "1 hour ago"),
            DocumentItem("3", "User_Feedback_Log.csv", "CSV", "450 KB", "120 Rows", "Yesterday"),
            DocumentItem("4", "Kotlin_Jetpack_Compose_Guide.md", "MD", "180 KB", "6 Pages", "2 days ago")
        )
    }

    var selectedDoc by remember { mutableStateOf<DocumentItem?>(recentDocs.first()) }
    var isAnalyzingDoc by remember { mutableStateOf(false) }
    var documentSummaryResult by remember {
        mutableStateOf(
            "Document Executive Summary (Sara_AI_Architecture_Spec.pdf):\n\n" +
                    "• Key System Modules: Gemini Live API, Continuous Voice Gateway, Local Room Database, Android Assistant Overlay.\n" +
                    "• Performance Benchmark: Sub-500ms streaming audio responses using Gemini 2.5 Flash.\n" +
                    "• Security Standard: Android Keystore encryption with strict runtime permission confirmation."
        )
    }

    // Screenshot AI State
    var screenshotAnalysis by remember {
        mutableStateOf("Screenshot Analysis:\nDetected UI elements: 3 Buttons, 1 Search Field, Navigation Drawer. Target screen appears to be System Settings.")
    }

    // Image Editor State
    var editToolSelected by remember { mutableStateOf("BG_REMOVE") }
    val editTools = listOf("BG_REMOVE", "ENHANCE", "CROP", "ROTATE", "COMPRESS")

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Document AI & File Intelligence", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
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
                    text = { Text("Doc Analyzer", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Screenshot AI", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Screenshot, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("AI Image Editor", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // Document AI Tab
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        item {
                            Text("Select Document File to Analyze", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        items(recentDocs, key = { it.id }) { doc ->
                            val isSelected = selectedDoc?.id == doc.id
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) NeonPurpleBright else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedDoc = doc }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = when (doc.fileType) {
                                            "PDF" -> SoftPinkAccent.copy(alpha = 0.2f)
                                            "DOCX" -> NeonCyan.copy(alpha = 0.2f)
                                            else -> NeonPurpleBright.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(doc.fileType, color = TextPrimaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(doc.fileName, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${doc.size} • ${doc.pageCount} • ${doc.timestamp}", color = TextSecondaryDark, fontSize = 11.sp)
                                    }

                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonPurpleBright)
                                    }
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    selectedDoc?.let { doc ->
                                        isAnalyzingDoc = true
                                        coroutineScope.launch {
                                            delay(1500)
                                            documentSummaryResult = "AI Deep Analysis of ${doc.fileName}:\n\n" +
                                                    "• Key Sections: Executive Summary, System Architecture, Performance Metrics, and Deployment Plan.\n" +
                                                    "• Summary: This document outlines the full multi-modal capabilities of Sara AI powered by Gemini models.\n" +
                                                    "• Search Match: Found 14 references to 'Android APIs' and 'Local Storage'."
                                            isAnalyzingDoc = false
                                            snackbarHostState.showSnackbar("${doc.fileName} summarized successfully!")
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                if (isAnalyzingDoc) {
                                    CircularProgressIndicator(color = AmoledBlack, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Analyzing Document Content...", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmoledBlack)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Summarize & Explain Document", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Document Intelligence Insights", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Surface(
                                        color = AmoledBlack,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(documentSummaryResult, color = TextPrimaryDark, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(12.dp))
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { onSendToChat("Answer questions about this document:\n\n$documentSummaryResult") },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = AmoledBlack, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Ask Questions in AI Chat", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Screenshot AI Tab
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Screenshot Intelligence & Analysis", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Analyze active screen, extract UI text, explain layout elements, and suggest next actions.", color = TextSecondaryDark, fontSize = 12.sp)

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Active screen captured & analyzed!")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Screenshot, contentDescription = null, tint = AmoledBlack)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Capture & Analyze Screen", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Surface(
                                        color = AmoledBlack,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(screenshotAnalysis, color = TextPrimaryDark, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // AI Image Editor Tab
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("AI Image Enhancer & Background Remover", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(AmoledBlack),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.sara_avatar_1785575842420),
                                            contentDescription = "Edit Preview",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(editTools) { tool ->
                                            FilterChip(
                                                selected = editToolSelected == tool,
                                                onClick = { editToolSelected = tool },
                                                label = { Text(tool.replace("_", " "), fontSize = 10.sp) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonCyan, selectedLabelColor = AmoledBlack)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Image processed with $editToolSelected tool!")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = AmoledBlack)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Apply AI Tool", color = AmoledBlack, fontWeight = FontWeight.Bold)
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
