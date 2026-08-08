package com.example.presentation.ui.personalities

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.AIPersonality
import com.example.domain.model.Gender
import com.example.presentation.viewmodel.PersonalitiesViewModel
import com.example.presentation.viewmodel.VoiceViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalitiesScreen(
    viewModel: PersonalitiesViewModel,
    voiceViewModel: VoiceViewModel? = null
) {
    val displayedPersonalities by viewModel.displayedPersonalities.collectAsState()
    val activePersonality by viewModel.activePersonality.collectAsState(initial = AIPersonality.ZOYA)
    val genderFilter by viewModel.genderFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Personalities Gallery (8)", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
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
        ) {
            // Gender Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = genderFilter == null,
                    onClick = { viewModel.filterByGender(null) },
                    label = { Text("All (8)", color = if (genderFilter == null) AmoledBlack else TextPrimaryDark) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonPurpleBright, containerColor = SurfaceDark)
                )
                FilterChip(
                    selected = genderFilter == Gender.FEMALE,
                    onClick = { viewModel.filterByGender(Gender.FEMALE) },
                    label = { Text("Female (4)", color = if (genderFilter == Gender.FEMALE) AmoledBlack else TextPrimaryDark) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SoftPinkAccent, containerColor = SurfaceDark)
                )
                FilterChip(
                    selected = genderFilter == Gender.MALE,
                    onClick = { viewModel.filterByGender(Gender.MALE) },
                    label = { Text("Male (4)", color = if (genderFilter == Gender.MALE) AmoledBlack else TextPrimaryDark) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonCyan, containerColor = SurfaceDark)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val context = androidx.compose.ui.platform.LocalContext.current
            // Personalities Cards List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedPersonalities, key = { it.id }) { personality ->
                    PersonalityCardItem(
                        personality = personality,
                        isActive = personality.id == activePersonality.id,
                        onSelect = { viewModel.selectPersonality(personality) },
                        onPreviewVoice = { voiceViewModel?.previewVoice(context, personality) }
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalityCardItem(
    personality: AIPersonality,
    isActive: Boolean,
    onSelect: () -> Unit,
    onPreviewVoice: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (isActive) CardBackgroundGlass else SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) NeonPurpleBright else BorderPurpleGlow,
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("personality_card_${personality.id}")
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariantDark),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sara_avatar_1785575842420),
                    contentDescription = personality.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = personality.name,
                            color = TextPrimaryDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = SurfaceVariantDark,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = personality.title,
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(onClick = onPreviewVoice, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Voice Preview", tint = SoftPinkAccent, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Mood: ${personality.mood} • Voice: ${personality.voiceName}",
                    color = SoftPinkAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "\"${personality.greeting}\"",
                    color = TextSecondaryDark,
                    fontSize = 12.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) NeonPurpleBright else SurfaceVariantDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    if (isActive) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = AmoledBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Active Personality", color = AmoledBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    } else {
                        Text("Switch to ${personality.name}", color = TextPrimaryDark, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

