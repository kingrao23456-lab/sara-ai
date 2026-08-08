package com.example.presentation.ui.models

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.viewmodel.ChatViewModel
import com.example.ui.theme.*

data class AiModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val isDefault: Boolean = false,
    val speedRating: String,
    val reasoningRating: String,
    val contextWindow: String,
    val badge: String
)

data class ThinkingModeInfo(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorScreen(
    chatViewModel: ChatViewModel
) {
    val currentModel by chatViewModel.selectedModel.collectAsState()
    val currentThinkingMode by chatViewModel.selectedThinkingMode.collectAsState()

    val availableModels = remember {
        listOf(
            AiModelInfo(
                id = "gemini-3.6-flash",
                name = "Gemini 3.6 Flash",
                description = "Latest flagship flash model — fast, agentic, great for everyday conversations.",
                isDefault = true,
                speedRating = "⚡⚡⚡⚡⚡",
                reasoningRating = "🧠🧠🧠🧠",
                contextWindow = "1M Tokens",
                badge = "RECOMMENDED"
            ),
            AiModelInfo(
                id = "gemini-2.5-pro",
                name = "Gemini 2.5 Pro",
                description = "Maximum intelligence model for complex coding, math, logic, and deep analysis.",
                speedRating = "⚡⚡⚡",
                reasoningRating = "🧠🧠🧠🧠🧠",
                contextWindow = "2M Tokens",
                badge = "PRO REASONING"
            ),
            AiModelInfo(
                id = "gemini-3.5-flash-lite",
                name = "Gemini 3.5 Flash-Lite",
                description = "Lowest latency, most cost-effective model — ideal for quick replies.",
                speedRating = "⚡⚡⚡⚡⚡",
                reasoningRating = "🧠🧠🧠",
                contextWindow = "1M Tokens",
                badge = "LIGHTNING SPEED"
            ),
            AiModelInfo(
                id = "gemini-2.5-flash",
                name = "Gemini 2.5 Flash",
                description = "Previous-generation flash model, kept as a fallback option.",
                speedRating = "⚡⚡⚡⚡",
                reasoningRating = "🧠🧠🧠🧠",
                contextWindow = "1M Tokens",
                badge = "LEGACY"
            )
        )
    }

    val thinkingModes = remember {
        listOf(
            ThinkingModeInfo("FAST", "Fast & Direct", "Concise, immediate answers without delay", "⚡"),
            ThinkingModeInfo("BALANCED", "Balanced", "Well-rounded responses with appropriate context", "⚖️"),
            ThinkingModeInfo("SMART", "Smart Context", "Deep contextual understanding & follow-up insights", "💡"),
            ThinkingModeInfo("DEEP_THINKING", "Deep Thinking", "Step-by-step chain of thought decomposition", "🧠"),
            ThinkingModeInfo("CREATIVE", "Creative Spark", "Imaginative, expressive, and detailed output", "🎨"),
            ThinkingModeInfo("CODING", "Software Architect", "Clean code, unit tests, and performance optimization", "💻"),
            ThinkingModeInfo("RESEARCH", "In-Depth Research", "Citations, structured analysis, and evidence synthesis", "🔍")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Model & Thinking Selector", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Select Gemini AI Engine Model", color = NeonPurpleBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Choose the neural model powering your Sara AI conversations:", color = TextSecondaryDark, fontSize = 12.sp)
            }

            items(availableModels, key = { it.id }) { model ->
                val isSelected = currentModel == model.id
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) SurfaceDark else AmoledBlack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) NeonPurpleBright else BorderPurpleGlow,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { chatViewModel.setSelectedModel(model.id) }
                        .testTag("model_card_${model.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { chatViewModel.setSelectedModel(model.id) },
                                    colors = RadioButtonDefaults.colors(selectedColor = NeonPurpleBright)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(model.name, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Surface(
                                color = if (isSelected) NeonPurpleBright else SurfaceVariantDark,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    model.badge,
                                    color = if (isSelected) AmoledBlack else NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(model.description, color = TextSecondaryDark, fontSize = 13.sp, lineHeight = 18.sp)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Speed: ${model.speedRating}", color = SoftPinkAccent, fontSize = 11.sp)
                            Text("Reasoning: ${model.reasoningRating}", color = NeonCyan, fontSize = 11.sp)
                            Text("Context: ${model.contextWindow}", color = TextPrimaryDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Select Thinking Mode Preset", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Adjust how Sara AI structures its chain-of-thought processing:", color = TextSecondaryDark, fontSize = 12.sp)
            }

            items(thinkingModes, key = { it.id }) { mode ->
                val isSelected = currentThinkingMode == mode.id
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) NeonCyan else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { chatViewModel.setSelectedThinkingMode(mode.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(mode.iconName, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mode.name, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(mode.description, color = TextSecondaryDark, fontSize = 12.sp)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = NeonCyan)
                        }
                    }
                }
            }
        }
    }
}
