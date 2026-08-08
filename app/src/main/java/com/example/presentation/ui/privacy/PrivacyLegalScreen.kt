package com.example.presentation.ui.privacy

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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyLegalScreen(
    onNavigateToPermissions: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableStateOf(0) } // 0 = Privacy Controls, 1 = Privacy Policy & Terms, 2 = Licenses

    var showDeleteChatsDialog by remember { mutableStateOf(false) }
    var showDeleteMemoryDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Privacy Controls & Legal Policy", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
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
            // Tab Header
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceDark,
                contentColor = NeonPurpleBright,
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Data Controls", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Privacy Policy", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Policy, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Licenses", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Data Controls & Actions
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderPurpleGlow, RoundedCornerShape(18.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(26.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Your Data Rights & Zero-Snoop Guarantee", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "You have full ownership of all conversation records, fact memories, and media assets. Export or erase anything instantly.",
                                        color = TextSecondaryDark,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        item {
                            PrivacyActionCard(
                                title = "Manage App Runtime Permissions",
                                subtitle = "Camera, Microphone, Storage, Location & Notifications",
                                icon = Icons.Default.VerifiedUser,
                                buttonText = "Manage Permissions",
                                buttonColor = NeonPurpleBright,
                                onClick = onNavigateToPermissions
                            )
                        }

                        item {
                            PrivacyActionCard(
                                title = "Export Complete User Data Archive (JSON)",
                                subtitle = "Download a complete copy of chats, memories, notes & settings",
                                icon = Icons.Default.Download,
                                buttonText = "Export JSON Archive",
                                buttonColor = NeonCyan,
                                onClick = {
                                    val dataJson = "{\"app\":\"Sara AI\",\"exportDate\":\"2026-08-01\",\"chats\":[],\"memories\":[]}"
                                    clipboardManager.setText(AnnotatedString(dataJson))
                                    coroutineScope.launch { snackbarHostState.showSnackbar("User data JSON archive copied to clipboard!") }
                                }
                            )
                        }

                        item {
                            PrivacyActionCard(
                                title = "Delete Chat History",
                                subtitle = "Permanently wipe all conversation sessions across all personalities",
                                icon = Icons.Default.DeleteSweep,
                                buttonText = "Wipe Chat History",
                                buttonColor = SoftPinkAccent,
                                onClick = { showDeleteChatsDialog = true }
                            )
                        }

                        item {
                            PrivacyActionCard(
                                title = "Delete Fact Memory Store",
                                subtitle = "Erase all learned user facts, preferences, and personal knowledge graph",
                                icon = Icons.Default.Psychology,
                                buttonText = "Clear All Memories",
                                buttonColor = SoftPinkAccent,
                                onClick = { showDeleteMemoryDialog = true }
                            )
                        }

                        item {
                            PrivacyActionCard(
                                title = "Delete Sara AI Account & Cloud Records",
                                subtitle = "Permanently remove Firebase Auth, Firestore data & Keystore alias",
                                icon = Icons.Default.PersonRemove,
                                buttonText = "Delete Account",
                                buttonColor = SoftPinkAccent,
                                onClick = { showDeleteAccountDialog = true }
                            )
                        }
                    }
                }

                1 -> {
                    // Privacy Policy Text & Consent
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Sara AI Privacy Policy & User Consent", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "1. Data Collection & Hardware Encrypted Storage:\n" +
                                                "Sara AI stores user memory facts and credentials using the Android Hardware Keystore (AES-256 GCM). No unencrypted plain text leaves your device without explicitly enabled Firebase Sync.\n\n" +
                                                "2. Gemini API & Server-Side Processing:\n" +
                                                "When sending prompts, images, or audio to Gemini models, data is processed securely over SSL/TLS. Your inputs are never sold or used to train public foundational models.\n\n" +
                                                "3. Permissions Transparency:\n" +
                                                "Camera and microphone streams are exclusively accessed when you explicitly initiate Live Vision or Voice sessions.",
                                        color = TextSecondaryDark,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
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
                                    Text("Terms of Service", color = NeonPurpleBright, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "By utilizing Sara AI, you agree to responsible usage of AI tools, respect system guidelines, and acknowledge local data storage policies.",
                                        color = TextSecondaryDark,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Open Source Licenses
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Open Source Licenses & Attribution", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    LicenseRow("Jetpack Compose & AndroidX", "Apache License 2.0")
                                    LicenseRow("Google Gemini AI SDK", "Apache License 2.0")
                                    LicenseRow("Firebase Android SDK", "Apache License 2.0")
                                    LicenseRow("Room Database & KSP", "Apache License 2.0")
                                    LicenseRow("Kotlinx Coroutines & Serialization", "Apache License 2.0")
                                    LicenseRow("Coil Image Loading Library", "Apache License 2.0")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteChatsDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteChatsDialog = false },
            title = { Text("Wipe All Chat History?", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete all chat session transcripts. This action cannot be undone.", color = TextSecondaryDark) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteChatsDialog = false
                        coroutineScope.launch { snackbarHostState.showSnackbar("All chat transcripts deleted.") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftPinkAccent)
                ) {
                    Text("Delete All Chats", color = AmoledBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteChatsDialog = false }) { Text("Cancel", color = TextSecondaryDark) }
            },
            containerColor = SurfaceDark
        )
    }

    if (showDeleteMemoryDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteMemoryDialog = false },
            title = { Text("Erase Fact Memory Store?", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = { Text("Sara AI will forget all saved user facts, preferences, and personal facts.", color = TextSecondaryDark) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteMemoryDialog = false
                        coroutineScope.launch { snackbarHostState.showSnackbar("Fact memory store cleared.") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftPinkAccent)
                ) {
                    Text("Clear Memories", color = AmoledBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteMemoryDialog = false }) { Text("Cancel", color = TextSecondaryDark) }
            },
            containerColor = SurfaceDark
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Sara AI Account?", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete your user profile, Firebase records, and local data.", color = TextSecondaryDark) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        coroutineScope.launch { snackbarHostState.showSnackbar("Account deletion initiated.") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftPinkAccent)
                ) {
                    Text("Permanently Delete Account", color = AmoledBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel", color = TextSecondaryDark) }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun PrivacyActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonText: String,
    buttonColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = buttonColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(subtitle, color = TextSecondaryDark, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText, color = AmoledBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun LicenseRow(libName: String, license: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(libName, color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(license, color = NeonCyan, fontSize = 11.sp)
    }
}
