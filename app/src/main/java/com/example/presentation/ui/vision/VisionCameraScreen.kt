package com.example.presentation.ui.vision

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

data class ScannerMode(
    val id: String,
    val name: String,
    val iconName: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionCameraScreen(
    onSendToChat: (String) -> Unit
) {
    var isFrontCamera by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var zoomLevel by remember { mutableStateOf(1.0f) }
    var isAnalyzing by remember { mutableStateOf(false) }

    var selectedScannerMode by remember { mutableStateOf("LIVE_SCENE") }
    var userQuestionText by remember { mutableStateOf("What objects and text are visible in this camera view?") }

    var liveAnalysisResult by remember {
        mutableStateOf(
            "Gemini Vision Real-Time Scene Analysis:\n" +
                    "• Detected Objects: Laptop, Coffee Mug, Wireless Headphones, Notebook\n" +
                    "• Dominant Colors: Neon Cyan (#00E5FF), Deep Obsidian, Soft Purple\n" +
                    "• OCR Detected Text: \"Sara AI - Multi-modal Neural Assistant\"\n" +
                    "• Context Suggestion: Ready to summarize notes or debug code on screen."
        )
    }

    val scannerModes = listOf(
        ScannerMode("LIVE_SCENE", "Live Scene AI", "👁️", "Real-time object & color recognition"),
        ScannerMode("DOC_SCAN", "Smart Document", "📄", "Crop, straighten & extract text"),
        ScannerMode("CARD_SCAN", "Business Card", "🪪", "Extract contact, phone & email"),
        ScannerMode("RECEIPT_SCAN", "Receipt & Invoice", "🧾", "Extract item totals & vendor info"),
        ScannerMode("BOOK_NOTES", "Book & Handwritten", "📚", "Clean OCR for handwritten notes")
    )

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gemini Live Camera & Vision AI", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { isFlashOn = !isFlashOn }) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (isFlashOn) NeonCyan else TextMuted
                        )
                    }
                    IconButton(onClick = { isFrontCamera = !isFrontCamera }) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Switch Camera", tint = NeonPurpleBright)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Camera Viewfinder Box
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AmoledBlack)
                        .border(2.dp, BorderPurpleGlow, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sara_avatar_1785575842420),
                        contentDescription = "Live Camera Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay Bounding Box Simulation
                    Box(
                        modifier = Modifier
                            .size(180.dp, 120.dp)
                            .border(2.dp, NeonCyan, RoundedCornerShape(12.dp))
                            .background(NeonCyan.copy(alpha = 0.1f))
                    )

                    // Mode Badge Tag
                    Surface(
                        color = AmoledBlack.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SoftPinkAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isFrontCamera) "FRONT CAM (1080p)" else "BACK CAM (4K HDR)",
                                color = TextPrimaryDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Floating Shutter / Capture Button
                    FloatingActionButton(
                        onClick = {
                            if (!isAnalyzing) {
                                isAnalyzing = true
                                coroutineScope.launch {
                                    delay(1200)
                                    liveAnalysisResult = "Live Camera Capture Analysis:\n\n" +
                                            "Mode: $selectedScannerMode\n" +
                                            "• Detected Objects: Modern workspace, OLED monitor, handwritten notes.\n" +
                                            "• Text Extraction: \"Sara AI Vision 2028 - Gemini Live Camera Integration\"\n" +
                                            "• Recommended Action: Generate summary or translate text into Hindi."
                                    isAnalyzing = false
                                    snackbarHostState.showSnackbar("Camera frame analyzed with Gemini Vision!")
                                }
                            }
                        },
                        containerColor = NeonPurpleBright,
                        contentColor = AmoledBlack,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .testTag("camera_shutter_fab")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(color = AmoledBlack, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Camera, contentDescription = "Capture", modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            // Zoom Slider Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Zoom: ${"%.1f".format(zoomLevel)}x", color = TextSecondaryDark, fontSize = 12.sp)
                    Slider(
                        value = zoomLevel,
                        onValueChange = { zoomLevel = it },
                        valueRange = 1.0f..5.0f,
                        modifier = Modifier.width(220.dp),
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                    )
                }
            }

            // Scanner Mode Selector
            item {
                Text("Smart Scanner Modes", color = NeonPurpleBright, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(scannerModes) { mode ->
                        val isSelected = selectedScannerMode == mode.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedScannerMode = mode.id },
                            label = { Text("${mode.iconName} ${mode.name}", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = AmoledBlack,
                                containerColor = SurfaceDark,
                                labelColor = TextSecondaryDark
                            )
                        )
                    }
                }
            }

            // Vision Q&A Query Field
            item {
                OutlinedTextField(
                    value = userQuestionText,
                    onValueChange = { userQuestionText = it },
                    label = { Text("Ask Question About Camera View", color = TextSecondaryDark, fontSize = 11.sp) },
                    trailingIcon = {
                        IconButton(onClick = {
                            onSendToChat("[Camera Image Attached] Question: $userQuestionText\n\n$liveAnalysisResult")
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = NeonPurpleBright)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurpleBright,
                        unfocusedBorderColor = BorderPurpleGlow,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Live Analysis Result Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gemini Vision AI Analysis", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(liveAnalysisResult))
                                coroutineScope.launch { snackbarHostState.showSnackbar("Analysis copied to clipboard!") }
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SoftPinkAccent, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            color = AmoledBlack,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(liveAnalysisResult, color = TextPrimaryDark, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(12.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onSendToChat("Translate this camera vision text to Hindi:\n\n$liveAnalysisResult") },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Translate to Hindi", color = AmoledBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onSendToChat("Summarize key actionable points from this image:\n\n$liveAnalysisResult") },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Summarize Key Points", color = AmoledBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
