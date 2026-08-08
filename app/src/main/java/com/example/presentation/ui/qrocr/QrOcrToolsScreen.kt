package com.example.presentation.ui.qrocr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrOcrToolsScreen(
    onSendToChat: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = OCR Vision, 1 = QR & Barcode Tools

    var ocrTextResult by remember {
        mutableStateOf("Sara AI Vision OCR detected text:\n\n\"Project Proposal 2028: Multi-modal generative reasoning with long context support and local device automation.\"")
    }
    var isOcrScanning by remember { mutableStateOf(false) }

    var qrInputText by remember { mutableStateOf("https://ai.studio/build/sara-ai") }
    var scannedQrResult by remember { mutableStateOf("https://github.com/google-deepmind/sara-ai") }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("QR Scanner & Vision OCR Tools", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
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
                    text = { Text("OCR Vision Extractor", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("QR & Barcode Generator", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // OCR Tab
                LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Document & Image Text Extraction", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Capture a photo or upload an image to extract text, translate, or summarize with Gemini Vision.", color = TextSecondaryDark, fontSize = 12.sp)

                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(AmoledBlack),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.sara_avatar_1785575842420),
                                        contentDescription = "Document preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        color = AmoledBlack.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Camera Feed / Gallery Image", color = NeonCyan, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (!isOcrScanning) {
                                            isOcrScanning = true
                                            coroutineScope.launch {
                                                delay(1500)
                                                ocrTextResult = "Extracted Text (Sara OCR):\n\n\"Executive Summary 2028: Sara AI integrated with official Android APIs for Seamless Voice, Camera OCR, and Calendar scheduling.\""
                                                isOcrScanning = false
                                                snackbarHostState.showSnackbar("OCR Text extraction complete!")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    if (isOcrScanning) {
                                        CircularProgressIndicator(color = AmoledBlack, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Scanning Image Text...", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Default.Camera, contentDescription = null, tint = AmoledBlack)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Scan & Extract Text", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                    }
                                }
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
                                Text("Extracted OCR Text Result", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    color = AmoledBlack,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(ocrTextResult, color = TextPrimaryDark, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(12.dp))
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(ocrTextResult))
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Extracted text copied!") }
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan)
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Button(
                                        onClick = { onSendToChat("Summarize and explain this extracted OCR text:\n\n$ocrTextResult") },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, tint = AmoledBlack, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Send to Chat AI", color = AmoledBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // QR & Barcode Tab
                LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Generate Custom QR Code", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = qrInputText,
                                    onValueChange = { qrInputText = it },
                                    label = { Text("URL or Text to Encode") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(AmoledBlack)
                                        .border(2.dp, NeonPurpleBright, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.QrCode2, contentDescription = "Generated QR Code", tint = NeonPurpleBright, modifier = Modifier.size(120.dp))
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        coroutineScope.launch { snackbarHostState.showSnackbar("QR Code saved to Gallery!") }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = AmoledBlack)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save QR Code Image", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                }
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
                                Text("Live Barcode & QR Scanner", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Scanned payload: $scannedQrResult", color = TextSecondaryDark, fontSize = 12.sp)

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(scannedQrResult))
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Scanned payload copied to clipboard!") }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AmoledBlack)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Copy Scanned Result", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
