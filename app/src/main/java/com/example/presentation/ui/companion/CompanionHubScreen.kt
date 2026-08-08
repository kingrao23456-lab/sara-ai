package com.example.presentation.ui.companion

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AIPersonality
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AvatarState { IDLE, SPEAKING, LISTENING, THINKING, SMILE, BLINK }

enum class RelationshipMode(val title: String, val description: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    FRIEND("Friend", "Casual, warm, and supportive everyday conversations", Icons.Default.Favorite),
    BEST_FRIEND("Best Friend", "Deep trust, fun banter, secret sharing & empathy", Icons.Default.SupervisedUserCircle),
    STUDY_PARTNER("Study Partner", "Exam prep, quiz questions, concept explanations & flashcards", Icons.Default.School),
    CODING_PARTNER("Coding Partner", "Code debugging, architecture reviews & pair programming", Icons.Default.Code),
    WORK_ASSISTANT("Work Assistant", "Email drafting, meeting summaries & task prioritization", Icons.Default.Work),
    CREATIVE_PARTNER("Creative Partner", "Brainstorming, story plots, design ideas & lyric writing", Icons.Default.Palette),
    FITNESS_COACH("Fitness Coach", "Workout routines, meal plans, motivation & habit tracking", Icons.Default.FitnessCenter),
    MENTOR("Mentor", "Career growth advice, life decisions & wisdom guide", Icons.Default.Psychology)
}

enum class UserEmotion(val label: String, val emoji: String, val color: Color, val empathyQuote: String) {
    HAPPY("Happy", "😊", Color(0xFF00E676), "Your joy brightens my whole day! Let's celebrate this great mood!"),
    SAD("Sad", "🥺", Color(0xFF29B6F6), "I'm right here with you. Take a soft breath — you don't have to carry it alone."),
    STRESSED("Stressed", "😰", Color(0xFFFFB74D), "Pause for a moment. Let's break things down step by step together."),
    EXCITED("Excited", "🎉", Color(0xFFFF4081), "That's incredible news! I'm beaming with excitement for you!"),
    TIRED("Tired", "😴", Color(0xFFAB47BC), "Rest your mind. You've worked hard today. I'm keeping everything safe."),
    CALM("Calm", "🧘", Color(0xFF26A69A), "A peaceful mind brings clear wisdom. Enjoy this quiet clarity."),
    ANGRI("Angry", "😤", Color(0xFFFF5252), "It's completely okay to feel frustrated. I'm listening to everything you need to vent.")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionHubScreen(
    onNavigateToChat: () -> Unit = {},
    onNavigateToVoice: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var activePersonality by remember { mutableStateOf(AIPersonality.ZOYA) }
    var selectedRelationshipMode by remember { mutableStateOf(RelationshipMode.BEST_FRIEND) }
    var selectedEmotion by remember { mutableStateOf(UserEmotion.HAPPY) }
    var avatarState by remember { mutableStateOf(AvatarState.IDLE) }

    // Dynamic Time Greeting
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greetingText = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Good Morning!"
            in 12..16 -> "Good Afternoon!"
            in 17..21 -> "Good Evening!"
            else -> "Welcome Back, Night Owl!"
        }
    }

    val dailyQuote = remember {
        listOf(
            "\"The secret of getting ahead is getting started.\" — Mark Twain",
            "\"Your future is created by what you do today, not tomorrow.\" — Robert Kiyosaki",
            "\"Small daily improvements over time lead to stunning results.\" — Robin Sharma",
            "\"Clarity comes from engagement, not from thought.\" — Marie Forleo"
        ).random()
    }

    // Avatar Animation Engine
    val transition = rememberInfiniteTransition(label = "AvatarHalo")
    val haloPulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "halo"
    )

    val waveOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
        label = "wave"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("AI Companion & Emotion Engine", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
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

            // Dynamic Greeting Banner
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderPurpleGlow, RoundedCornerShape(22.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(greetingText, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Text("Mode: ${selectedRelationshipMode.title} • Active: ${activePersonality.name}", color = TextSecondaryDark, fontSize = 12.sp)
                            }
                            Surface(
                                color = selectedEmotion.color.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedEmotion.emoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(selectedEmotion.label, color = selectedEmotion.color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(dailyQuote, color = TextPrimaryDark, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }

            // Animated Interactive Avatar Canvas & State Control
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Interactive Companion State (${avatarState.name})", color = TextSecondaryDark, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Animated Vector Canvas Avatar
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(180.dp)
                                .clickable {
                                    avatarState = AvatarState.values().random()
                                    coroutineScope.launch {
                                        delay(2500)
                                        avatarState = AvatarState.IDLE
                                    }
                                }
                                .testTag("interactive_avatar_canvas")
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2, size.height / 2)
                                val radius = (size.minDimension / 2.6f) * haloPulse

                                // Glowing Halo Circle
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            selectedEmotion.color.copy(alpha = 0.4f),
                                            NeonPurpleBright.copy(alpha = 0.2f),
                                            Color.Transparent
                                        ),
                                        center = center,
                                        radius = radius * 1.3f
                                    ),
                                    radius = radius * 1.3f,
                                    center = center
                                )

                                // Base Avatar Circle
                                drawCircle(
                                    color = SurfaceVariantDark,
                                    radius = radius,
                                    center = center
                                )

                                drawCircle(
                                    color = NeonPurpleBright,
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = 3.dp.toPx())
                                )

                                // Eye Expressions depending on AvatarState
                                val eyeOffset = 22.dp.toPx()
                                val eyeY = center.y - 12.dp.toPx()

                                if (avatarState == AvatarState.BLINK) {
                                    // Closed Eyes
                                    drawLine(
                                        color = NeonCyan,
                                        start = Offset(center.x - eyeOffset - 10.dp.toPx(), eyeY),
                                        end = Offset(center.x - eyeOffset + 10.dp.toPx(), eyeY),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                    drawLine(
                                        color = NeonCyan,
                                        start = Offset(center.x + eyeOffset - 10.dp.toPx(), eyeY),
                                        end = Offset(center.x + eyeOffset + 10.dp.toPx(), eyeY),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                } else {
                                    drawCircle(color = NeonCyan, radius = 6.dp.toPx(), center = Offset(center.x - eyeOffset, eyeY))
                                    drawCircle(color = NeonCyan, radius = 6.dp.toPx(), center = Offset(center.x + eyeOffset, eyeY))
                                }

                                // Smile Path
                                val smilePath = Path().apply {
                                    moveTo(center.x - 20.dp.toPx(), center.y + 12.dp.toPx())
                                    quadraticTo(
                                        center.x,
                                        if (avatarState == AvatarState.SMILE) center.y + 32.dp.toPx() else center.y + 22.dp.toPx(),
                                        center.x + 20.dp.toPx(),
                                        center.y + 12.dp.toPx()
                                    )
                                }
                                drawPath(path = smilePath, color = SoftPinkAccent, style = Stroke(width = 3.5f.dp.toPx()))

                                // Soundwave Animation if Speaking
                                if (avatarState == AvatarState.SPEAKING) {
                                    val barWidth = 4.dp.toPx()
                                    for (i in -2..2) {
                                        val height = (15..35).random().dp.toPx()
                                        drawRect(
                                            color = NeonCyan,
                                            topLeft = Offset(center.x + (i * 12.dp.toPx()), center.y + 38.dp.toPx()),
                                            size = androidx.compose.ui.geometry.Size(barWidth, height)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Tap Avatar to change state • Tap to converse", color = TextMuted, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Avatar State Action Quick Bar
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AvatarState.values().forEach { state ->
                                FilterChip(
                                    selected = avatarState == state,
                                    onClick = { avatarState = state },
                                    label = { Text(state.name, fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonCyan, selectedLabelColor = AmoledBlack)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onNavigateToChat,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = AmoledBlack)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Chat Now", color = AmoledBlack, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onNavigateToVoice,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = AmoledBlack)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Voice Call", color = AmoledBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Emotion Engine Selector
            item {
                Column {
                    Text("Emotion Engine — How are you feeling right now?", color = NeonPurpleBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(UserEmotion.values()) { emotion ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedEmotion == emotion) emotion.color.copy(alpha = 0.25f) else SurfaceDark
                                ),
                                modifier = Modifier
                                    .clickable {
                                        selectedEmotion = emotion
                                        avatarState = AvatarState.SMILE
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Sara detected you are feeling ${emotion.label}. Tone adjusted!")
                                        }
                                    }
                                    .border(
                                        width = if (selectedEmotion == emotion) 2.dp else 1.dp,
                                        color = if (selectedEmotion == emotion) emotion.color else SurfaceVariantDark,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(emotion.emoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(emotion.label, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Empathetic Message Banner
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = selectedEmotion.color, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(selectedEmotion.empathyQuote, color = TextPrimaryDark, fontSize = 12.sp, lineHeight = 17.sp)
                        }
                    }
                }
            }

            // Relationship Modes Selection
            item {
                Column {
                    Text("Relationship Modes — Tailor Sara's Mindset", color = NeonPurpleBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    RelationshipMode.values().forEach { mode ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedRelationshipMode == mode) CardBackgroundGlass else SurfaceDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedRelationshipMode = mode
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Active relationship mode updated to: ${mode.title}")
                                    }
                                }
                                .border(
                                    width = if (selectedRelationshipMode == mode) 1.5.dp else 0.5.dp,
                                    color = if (selectedRelationshipMode == mode) NeonCyan else SurfaceVariantDark,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(mode.icon, contentDescription = null, tint = if (selectedRelationshipMode == mode) NeonCyan else SoftPinkAccent, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(mode.title, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(mode.description, color = TextSecondaryDark, fontSize = 11.sp)
                                }
                                RadioButton(
                                    selected = selectedRelationshipMode == mode,
                                    onClick = { selectedRelationshipMode = mode },
                                    colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
