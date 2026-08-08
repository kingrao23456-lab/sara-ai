package com.example.presentation.ui.voice

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.presentation.viewmodel.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(
    viewModel: VoiceViewModel,
    activePersonality: AIPersonality,
    userLanguage: String,
    onSelectPersonality: (AIPersonality) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val voiceState by viewModel.voiceState.collectAsState()
    val micPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening(
                context = context,
                personality = activePersonality,
                language = userLanguage
            )
        }
    }
    val voiceMode by viewModel.voiceMode.collectAsState()
    val isLiveCallActive by viewModel.isLiveCallActive.collectAsState()
    val liveCallError by viewModel.liveCallError.collectAsState()

    DisposableEffect(Unit) {
        onDispose { viewModel.stopLiveCall() }
    }
    val relationshipMode by viewModel.relationshipMode.collectAsState()
    val emotionTone by viewModel.currentEmotion.collectAsState()
    val lastTranscript by viewModel.lastTranscript.collectAsState()
    val speed by viewModel.voiceSpeed.collectAsState()
    val pitch by viewModel.voicePitch.collectAsState()
    val isContinuous by viewModel.isContinuousConversation.collectAsState()
    val wakeWordEnabled by viewModel.isWakeWordEnabled.collectAsState()
    val userSpokenText by viewModel.userSpokenText.collectAsState()

    var showAdvancedSettings by remember { mutableStateOf(false) }

    val audioWaveHeights = remember(voiceState) {
        if (voiceState == VoiceState.SPEAKING || voiceState == VoiceState.LISTENING) {
            listOf(0.4f, 0.7f, 0.9f, 0.5f, 0.95f, 0.8f, 0.6f, 0.9f, 1.0f, 0.7f, 0.5f, 0.8f, 0.95f, 0.6f, 0.4f, 0.7f)
        } else {
            listOf(0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "orbPulse")
    val orbScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (voiceState == VoiceState.SPEAKING || voiceState == VoiceState.LISTENING) 1.15f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbScale"
    )

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Voice Engine Live", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${activePersonality.name} (${activePersonality.title}) • ${relationshipMode.name.replace("_", " ")}", color = NeonCyan, fontSize = 11.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.previewVoice(context, activePersonality) }) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Preview Voice", tint = SoftPinkAccent)
                    }
                    IconButton(onClick = { showAdvancedSettings = !showAdvancedSettings }) {
                        Icon(Icons.Default.Tune, contentDescription = "Voice Settings", tint = NeonPurpleBright)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // AI Model Selector Row (8 Personalities)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Active AI Model / Voice:",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AIPersonality.VOICE_PERSONAS) { model ->
                        val isSelected = model.id == activePersonality.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onSelectPersonality(model)
                                viewModel.previewVoice(context, model)
                            },
                            label = {
                                Text(
                                    text = "${model.name} (${if (model.gender == com.example.domain.model.Gender.MALE) "Male" else "Female"})",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (model.gender == com.example.domain.model.Gender.MALE) NeonCyan else SoftPinkAccent,
                                selectedLabelColor = AmoledBlack,
                                containerColor = SurfaceDark,
                                labelColor = TextPrimaryDark
                            ),
                            modifier = Modifier.testTag("voice_model_chip_${model.id}")
                        )
                    }
                }
            }

            // Status Indicator & Interrupt Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (voiceState) {
                                        VoiceState.SPEAKING -> NeonPurpleBright
                                        VoiceState.LISTENING -> SoftPinkAccent
                                        VoiceState.PROCESSING -> NeonCyan
                                        VoiceState.IDLE -> TextMuted
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (voiceState) {
                                VoiceState.SPEAKING -> "Speaking..."
                                VoiceState.LISTENING -> userSpokenText
                                VoiceState.PROCESSING -> "Thinking..."
                                VoiceState.IDLE -> "Tap orb or say \"Hey ${activePersonality.name}\""
                            },
                            color = TextPrimaryDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (voiceState == VoiceState.SPEAKING) {
                    Button(
                        onClick = { viewModel.interruptAi() },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftPinkAccent),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Interrupt", tint = AmoledBlack, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Interrupt", color = AmoledBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Giant Glowing Animated AI Avatar Orb
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(orbScale)
                        .blur(36.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    when (voiceState) {
                                        VoiceState.SPEAKING -> NeonPurplePrimary
                                        VoiceState.LISTENING -> SoftPinkAccent
                                        VoiceState.PROCESSING -> NeonCyan
                                        VoiceState.IDLE -> BorderPurpleGlow
                                    },
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(orbScale)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    NeonPurpleBright,
                                    SoftPinkAccent,
                                    NeonCyan,
                                    NeonPurpleBright
                                )
                            )
                        )
                        .clickable {
                            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.RECORD_AUDIO
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                            if (!hasPermission) {
                                micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                return@clickable
                            }

                            if (voiceMode == VoiceMode.VOICE_TO_VOICE) {
                                // Real, continuous, duplex Gemini Live call — tap once to start, tap again to hang up.
                                if (isLiveCallActive) {
                                    viewModel.stopLiveCall()
                                } else {
                                    viewModel.startLiveCall(
                                        context = context,
                                        personality = activePersonality,
                                        language = userLanguage
                                    )
                                }
                            } else if (voiceState == VoiceState.IDLE) {
                                viewModel.startListening(
                                    context = context,
                                    personality = activePersonality,
                                    language = userLanguage
                                )
                            } else if (voiceState == VoiceState.LISTENING) {
                                viewModel.stopListeningAndProcess(
                                    context = context,
                                    personality = activePersonality,
                                    language = userLanguage
                                )
                            } else if (voiceState == VoiceState.SPEAKING) {
                                viewModel.interruptAi()
                            }
                        }
                        .testTag("voice_orb_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(118.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sara_avatar_1785575842420),
                            contentDescription = activePersonality.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(118.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

            // 16-Bar Equalizer Audio Wave
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                audioWaveHeights.forEach { heightFactor ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(heightFactor.coerceIn(0.1f, 1.0f))
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(NeonPurpleBright, SoftPinkAccent)
                                )
                            )
                    )
                }
            }

            // Real-Time Transcript Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderPurpleGlow, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transcript (${activePersonality.name})",
                            color = NeonPurpleBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lang: $userLanguage",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lastTranscript,
                        color = TextPrimaryDark,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Advanced Voice Controls Card
            if (showAdvancedSettings) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("🔊 Voice Engine Fine-Tuning", color = NeonPurpleBright, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Emotion Tone Selector
                        Text("Emotion Tone: ${emotionTone.name}", color = TextSecondaryDark, fontSize = 11.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(EmotionTone.values()) { tone ->
                                FilterChip(
                                    selected = emotionTone == tone,
                                    onClick = { viewModel.setEmotion(tone) },
                                    label = { Text(tone.name, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SoftPinkAccent, selectedLabelColor = AmoledBlack)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Voice Speed Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Speed: ${"%.1f".format(speed)}x", color = TextSecondaryDark, fontSize = 12.sp)
                            Slider(
                                value = speed,
                                onValueChange = { viewModel.setVoiceSpeed(it) },
                                valueRange = 0.5f..2.0f,
                                modifier = Modifier.width(180.dp),
                                colors = SliderDefaults.colors(thumbColor = SoftPinkAccent, activeTrackColor = SoftPinkAccent)
                            )
                        }

                        // Voice Pitch Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pitch: ${"%.1f".format(pitch)}x", color = TextSecondaryDark, fontSize = 12.sp)
                            Slider(
                                value = pitch,
                                onValueChange = { viewModel.setVoicePitch(it) },
                                valueRange = 0.5f..1.5f,
                                modifier = Modifier.width(180.dp),
                                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                            )
                        }

                        // Toggles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Continuous Conversation", color = TextPrimaryDark, fontSize = 12.sp)
                            Switch(
                                checked = isContinuous,
                                onCheckedChange = { viewModel.toggleContinuousConversation() },
                                colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = NeonPurpleBright)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Wake Word (\"Hey ${activePersonality.name}\")", color = TextPrimaryDark, fontSize = 12.sp)
                            Switch(
                                checked = wakeWordEnabled,
                                onCheckedChange = { viewModel.toggleWakeWord() },
                                colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = NeonCyan)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
