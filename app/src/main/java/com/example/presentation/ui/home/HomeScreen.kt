package com.example.presentation.ui.home

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.AIPersonality
import com.example.domain.model.ChatMessage
import com.example.domain.model.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userProfile: UserProfile,
    activePersonality: AIPersonality,
    recentMessages: List<ChatMessage>,
    memoryCount: Int,
    unreadNotificationsCount: Int,
    onOpenDrawer: () -> Unit,
    onNavigateToChat: (initialPrompt: String?) -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToPersonalities: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.sara_app_icon_1785575828281),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sara AI",
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("open_drawer_button")) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Drawer", tint = TextPrimaryDark)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications, modifier = Modifier.testTag("notifications_button")) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(containerColor = SoftPinkAccent) {
                                        Text(unreadNotificationsCount.toString(), color = AmoledBlack)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextPrimaryDark)
                        }
                    }
                },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Banner Card with Sara Avatar
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderPurpleGlow, RoundedCornerShape(28.dp))
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = SurfaceVariantDark,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(NeonCyan)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${activePersonality.name} • ${activePersonality.title}",
                                            color = NeonCyan,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Text(
                                    text = "Hello, ${userProfile.name}!",
                                    color = TextPrimaryDark,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = activePersonality.greeting,
                                    color = TextSecondaryDark,
                                    fontSize = 13.sp,
                                    maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { onNavigateToChat(null) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurplePrimary),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.testTag("start_chat_button")
                                    ) {
                                        Icon(Icons.Default.ChatBubble, contentDescription = null, tint = AmoledBlack, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Chat", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = onNavigateToVoice,
                                        shape = RoundedCornerShape(14.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderPurpleGlow),
                                        modifier = Modifier.testTag("start_voice_button")
                                    ) {
                                        Icon(Icons.Default.Mic, contentDescription = null, tint = SoftPinkAccent, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Voice Live", color = TextPrimaryDark)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Avatar Image
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Brush.linearGradient(listOf(NeonPurpleBright, SoftPinkAccent)), CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.sara_avatar_1785575842420),
                                    contentDescription = "Personality Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // Quick Prompt Ideas
            item {
                Column {
                    Text(
                        text = "Quick AI Inspirations",
                        color = TextPrimaryDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val quickPrompts = listOf(
                        "⚡ Write clean Kotlin Compose snippet",
                        "☕ Morning productivity roadmap",
                        "🧠 Search memory about my preferences",
                        "🌌 Explain Quantum Computing simply"
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(quickPrompts) { prompt ->
                            SuggestionChip(
                                onClick = { onNavigateToChat(prompt) },
                                label = { Text(prompt, color = TextPrimaryDark, fontSize = 13.sp) },
                                shape = RoundedCornerShape(12.dp),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = BorderPurpleGlow
                                ),
                                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = SurfaceDark)
                            )
                        }
                    }
                }
            }

            // Long-Term Memory Stats & AI Personalities Shortcuts
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Memory Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToMemory() }
                            .border(1.dp, BorderPurpleGlow, RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = SoftPinkAccent, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "$memoryCount Encrypted Facts",
                                color = TextPrimaryDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Long-Term Memory",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Personality Switcher Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToPersonalities() }
                            .border(1.dp, BorderPurpleGlow, RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Face, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "8 AI Personalities",
                                color = TextPrimaryDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Active: ${activePersonality.name}",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Recent Messages Preview
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Conversation",
                            color = TextPrimaryDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { onNavigateToChat(null) }) {
                            Text("View All", color = NeonPurpleBright)
                        }
                    }

                    if (recentMessages.isEmpty()) {
                        Surface(
                            color = SurfaceDark,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No recent messages yet. Say hello to Sara!",
                                    color = TextSecondaryDark,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        recentMessages.takeLast(3).forEach { msg ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onNavigateToChat(null) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (msg.sender == com.example.domain.model.Sender.USER) Icons.Default.Person else Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = if (msg.sender == com.example.domain.model.Sender.USER) SoftPinkAccent else NeonPurpleBright,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = msg.text,
                                        color = TextPrimaryDark,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
