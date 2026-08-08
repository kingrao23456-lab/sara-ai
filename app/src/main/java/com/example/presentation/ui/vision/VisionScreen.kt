package com.example.presentation.ui.vision

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Psychology
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
import com.example.presentation.viewmodel.ChatViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionScreen(
    chatViewModel: ChatViewModel,
    activePersonality: AIPersonality,
    userLanguage: String,
    onNavigateToChat: () -> Unit
) {
    var promptText by remember { mutableStateOf("Describe what you see in this image and give actionable recommendations.") }
    var isAnalyzing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gemini CameraX Vision", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Camera Viewfinder Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
                    .border(2.dp, BorderPurpleGlow, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sara_app_icon_1785575828281),
                    contentDescription = "Sample Vision Frame",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(160.dp)
                )

                Surface(
                    color = AmoledBlack.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Camera Live Scanner Ready",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Prompt Box
            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                label = { Text("Vision Query Prompt", color = TextSecondaryDark) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurpleBright,
                    unfocusedBorderColor = BorderPurpleGlow,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vision_prompt_input")
            )

            // Analysis Action Button
            Button(
                onClick = {
                    isAnalyzing = true
                    chatViewModel.sendMessage(promptText, activePersonality, userLanguage)
                    onNavigateToChat()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurplePrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("analyze_vision_button")
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = AmoledBlack)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Analyze Image with Gemini", color = AmoledBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
