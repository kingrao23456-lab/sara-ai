package com.example.presentation.ui.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class BadgeMilestone(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen() {
    val currentStreakDays by remember { mutableIntStateOf(14) }
    val totalChatsCount by remember { mutableIntStateOf(128) }
    val factsSavedCount by remember { mutableIntStateOf(42) }
    val voiceMinutesCount by remember { mutableIntStateOf(95) }
    val codeSnippetsCount by remember { mutableIntStateOf(34) }

    val milestones = remember {
        listOf(
            BadgeMilestone("Sara's Bestie", "Interacted for 7 consecutive days", Icons.Default.Favorite, true, SoftPinkAccent),
            BadgeMilestone("Code Wizard", "Solved 25+ coding bugs with Nova & Ethan", Icons.Default.Code, true, NeonCyan),
            BadgeMilestone("Fact Master", "Stored 30+ facts in User Memory", Icons.Default.Psychology, true, NeonPurpleBright),
            BadgeMilestone("Voice Pioneer", "Spent over 60 voice calling minutes", Icons.Default.Mic, true, Color(0xFFFFB74D)),
            BadgeMilestone("Early Riser", "Had 5 morning conversations before 7 AM", Icons.Default.WbSunny, true, Color(0xFFFFD54F)),
            BadgeMilestone("Night Owl", "Had 10 late-night brainstorming sessions", Icons.Default.NightsStay, true, Color(0xFFAB47BC)),
            BadgeMilestone("Automation Pro", "Created 5 WorkManager routines", Icons.Default.AutoAwesome, false, Color(0xFF81C784)),
            BadgeMilestone("Multi-Lingual", "Chatted in English & Hindi", Icons.Default.Translate, true, Color(0xFF4FC3F7))
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements & Productivity Stats", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Usage Streak Banner
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderPurpleGlow, RoundedCornerShape(22.dp))
                        .testTag("streak_banner_card")
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFFF6D00).copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF6D00), modifier = Modifier.size(32.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("$currentStreakDays Days Usage Streak!", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("You're on fire! Keep connecting with Sara daily.", color = TextSecondaryDark, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Productivity Metrics Grid
            item {
                Text("AI Productivity Stats", color = NeonPurpleBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard("Total Chats", "$totalChatsCount", Icons.Default.Chat, NeonPurpleBright, Modifier.weight(1f))
                    StatMetricCard("Memory Facts", "$factsSavedCount", Icons.Default.Psychology, NeonCyan, Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard("Voice Mins", "${voiceMinutesCount}m", Icons.Default.Mic, SoftPinkAccent, Modifier.weight(1f))
                    StatMetricCard("Code Solved", "$codeSnippetsCount", Icons.Default.Code, Color(0xFFFFB74D), Modifier.weight(1f))
                }
            }

            // Unlocked Badges & Milestones
            item {
                Text("Unlocked Milestones & Badges (${milestones.count { it.isUnlocked }}/${milestones.size})", color = NeonPurpleBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    milestones.forEach { badge ->
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
                                    color = if (badge.isUnlocked) badge.color.copy(alpha = 0.2f) else SurfaceVariantDark,
                                    shape = CircleShape,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            badge.icon,
                                            contentDescription = null,
                                            tint = if (badge.isUnlocked) badge.color else TextMuted,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(badge.title, color = if (badge.isUnlocked) TextPrimaryDark else TextMuted, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(badge.description, color = TextSecondaryDark, fontSize = 11.sp)
                                }

                                if (badge.isUnlocked) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(title, color = TextSecondaryDark, fontSize = 11.sp)
        }
    }
}
