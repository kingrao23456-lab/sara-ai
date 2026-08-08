package com.example.presentation.ui.help

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpAboutScreen() {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Help Center & Guides, 1 = Response Feedback, 2 = About & Changelog

    // Feedback State
    var ratingStars by remember { mutableIntStateOf(5) }
    var feedbackCategory by remember { mutableStateOf("AI Voice Accuracy") }
    var feedbackComment by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Help Center, Feedback & About", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
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
                    text = { Text("Help & Guides", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Help, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("AI Feedback", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("About & Log", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Help Center & Guides
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            FaqExpandableCard("How does Sara AI store my private memories?", "Sara AI utilizes Room Database with local AES-256 Android Keystore encryption. Facts remain strictly on your device unless Firebase Cloud Sync is explicitly turned on.")
                        }
                        item {
                            FaqExpandableCard("How do I activate hands-free Voice Calls?", "Go to Settings > Voice & Speech Engine and turn on 'Hands-Free Wake Word'. You can say 'Hey Sara' anytime to trigger continuous conversation.")
                        }
                        item {
                            FaqExpandableCard("What are Relationship Modes?", "Relationship Modes adjust Sara's prompt engineering, empathy tone, and suggestion priorities. Choose from Friend, Best Friend, Study Partner, Coding Partner, Work Assistant, Creative Partner, Fitness Coach, or Mentor.")
                        }
                        item {
                            FaqExpandableCard("How do WorkManager Automations work?", "Automation tasks run in background WorkManager threads. You can schedule morning news digests, nightly habit reminders, or automatic memory backups.")
                        }
                    }
                }

                1 -> {
                    // AI Response Feedback System
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text("Rate Sara AI Responses & Submit Feedback", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("Overall Rating:", color = TextPrimaryDark, fontSize = 12.sp)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        (1..5).forEach { star ->
                                            Icon(
                                                imageVector = if (star <= ratingStars) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = null,
                                                tint = if (star <= ratingStars) SoftPinkAccent else TextMuted,
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clickable { ratingStars = star }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Feedback Category:", color = TextPrimaryDark, fontSize = 12.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                        listOf("AI Accuracy", "Voice Quality", "UI Design", "Bug Report").forEach { cat ->
                                            FilterChip(
                                                selected = feedbackCategory == cat,
                                                onClick = { feedbackCategory = cat },
                                                label = { Text(cat, fontSize = 10.sp) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonPurpleBright, selectedLabelColor = AmoledBlack)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = feedbackComment,
                                        onValueChange = { feedbackComment = it },
                                        placeholder = { Text("Tell us how we can make Sara AI even better...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            feedbackComment = ""
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Thank you! Your feedback has been recorded.")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("submit_feedback_button")
                                    ) {
                                        Text("Submit Feedback", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // About & Release Changelog
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderPurpleGlow, RoundedCornerShape(18.dp))
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Sara AI — Ultimate Companion", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                            Text("Version 3.2.0 Pro Edition (Build 2026)", color = NeonCyan, fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Engineered with Google Gemini AI models, Android Keystore Security, Firebase Sync, WorkManager Automations, and Jetpack Compose M3.", color = TextSecondaryDark, fontSize = 12.sp)
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
                                    Text("Release Changelog — v3.2.0", color = SoftPinkAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    ChangelogItem("• Security Center with Hardware Keystore & Biometric Lock")
                                    ChangelogItem("• Firebase Cloud Sync, Auto Backup & Offline Queue")
                                    ChangelogItem("• Relationship Modes (8 Roles) & Emotion Engine")
                                    ChangelogItem("• Animated Vector Canvas Avatar Visualizer (60FPS)")
                                    ChangelogItem("• Usage Streaks & Achievement Badges System")
                                    ChangelogItem("• Granular Data Erasure & Privacy Rights Controls")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FaqExpandableCard(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(question, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = NeonCyan
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(answer, color = TextSecondaryDark, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

@Composable
fun ChangelogItem(text: String) {
    Text(text, color = TextPrimaryDark, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
}
