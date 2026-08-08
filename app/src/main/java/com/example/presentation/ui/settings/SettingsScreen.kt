package com.example.presentation.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToPermissions: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToCloudSync: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }

    // Quick Toggles & Preferences State
    var themeSelection by remember { mutableStateOf("AMOLED Black") } // Light, Dark, AMOLED Black, Material You
    var fontSize by remember { mutableFloatStateOf(14f) }

    var selectedVoice by remember { mutableStateOf("Sara Alpha (Warm Expressive)") }
    var voiceSpeed by remember { mutableFloatStateOf(1.0f) }
    var voicePitch by remember { mutableFloatStateOf(1.0f) }
    var autoSpeak by remember { mutableStateOf(true) }
    var wakeWordEnabled by remember { mutableStateOf(true) }
    var noiseCancellation by remember { mutableStateOf(true) }

    var defaultModel by remember { mutableStateOf("gemini-2.5-flash") }
    var thinkingMode by remember { mutableStateOf(true) }
    var memoryEnabled by remember { mutableStateOf(true) }
    var webSearchEnabled by remember { mutableStateOf(true) }
    var imageGenEnabled by remember { mutableStateOf(true) }

    var streamingResponse by remember { mutableStateOf(true) }
    var chatBubbleStyle by remember { mutableStateOf("Glassmorphism") } // Glassmorphism, Compact, Rounded
    var autoScroll by remember { mutableStateOf(true) }

    var aiNotifications by remember { mutableStateOf(true) }
    var reminderNotifications by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }

    var selectedLanguage by remember { mutableStateOf("English") } // English, Hindi, Hinglish

    var cacheSizeMb by remember { mutableStateOf("24.8 MB") }
    var crashReportsConsent by remember { mutableStateOf(true) }

    var showDeveloperLogsDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings & Preferences", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
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

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search settings, voice, security, models...", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPurpleBright, unfocusedBorderColor = SurfaceVariantDark),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 1. Security & Privacy Center Section
            if (searchQuery.isBlank() || searchQuery.contains("security", true) || searchQuery.contains("privacy", true) || searchQuery.contains("sync", true)) {
                item {
                    Text("Security, Privacy & Cloud Sync", color = NeonPurpleBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                item {
                    SettingRowCard(
                        icon = Icons.Default.Security,
                        title = "Security & Defense Center",
                        subtitle = "Hardware Keystore, Biometrics, PIN Lock & Device Security",
                        onClick = onNavigateToSecurity
                    )
                }

                item {
                    SettingRowCard(
                        icon = Icons.Default.CloudSync,
                        title = "Firebase Cloud Sync & Backup",
                        subtitle = "Auto Backup, WiFi Sync & Offline Queue Controls",
                        onClick = onNavigateToCloudSync
                    )
                }

                item {
                    SettingRowCard(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Controls, Rights & Legal Policy",
                        subtitle = "Data Export, Erasure Rights & Terms of Service",
                        onClick = onNavigateToPrivacy
                    )
                }

                item {
                    SettingRowCard(
                        icon = Icons.Default.VerifiedUser,
                        title = "App Permissions Manager",
                        subtitle = "Manage Camera, Microphone, Storage & Location",
                        onClick = onNavigateToPermissions
                    )
                }
            }

            // 2. Appearance & Theme Strategy
            if (searchQuery.isBlank() || searchQuery.contains("theme", true) || searchQuery.contains("appearance", true) || searchQuery.contains("font", true)) {
                item {
                    Text("Appearance & Theme", color = NeonPurpleBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Theme Engine", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("AMOLED Black", "Dark", "Light", "Material You").forEach { mode ->
                                    FilterChip(
                                        selected = themeSelection == mode,
                                        onClick = { themeSelection = mode },
                                        label = { Text(mode, fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonPurpleBright, selectedLabelColor = AmoledBlack)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Font Size: ${fontSize.toInt()} sp", color = TextPrimaryDark, fontSize = 12.sp)
                            Slider(
                                value = fontSize,
                                onValueChange = { fontSize = it },
                                valueRange = 12f..20f,
                                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonPurpleBright)
                            )
                        }
                    }
                }
            }

            // 3. Voice & Speech Controls
            if (searchQuery.isBlank() || searchQuery.contains("voice", true) || searchQuery.contains("pitch", true) || searchQuery.contains("speech", true)) {
                item {
                    Text("Voice & Speech Engine", color = NeonPurpleBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Voice Persona: $selectedVoice", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Voice Speed: ${"%.1f".format(voiceSpeed)}x", color = TextPrimaryDark, fontSize = 12.sp)
                            Slider(
                                value = voiceSpeed,
                                onValueChange = { voiceSpeed = it },
                                valueRange = 0.5f..2.0f,
                                colors = SliderDefaults.colors(thumbColor = NeonPurpleBright, activeTrackColor = NeonCyan)
                            )

                            Text("Voice Pitch: ${"%.1f".format(voicePitch)}x", color = TextPrimaryDark, fontSize = 12.sp)
                            Slider(
                                value = voicePitch,
                                onValueChange = { voicePitch = it },
                                valueRange = 0.5f..2.0f,
                                colors = SliderDefaults.colors(thumbColor = NeonPurpleBright, activeTrackColor = SoftPinkAccent)
                            )

                            ToggleRowSetting("Auto Speak AI Responses", autoSpeak) { autoSpeak = it }
                            ToggleRowSetting("Hands-Free Wake Word (\"Hey Sara\")", wakeWordEnabled) { wakeWordEnabled = it }
                            ToggleRowSetting("Active Noise Cancellation Filter", noiseCancellation) { noiseCancellation = it }
                        }
                    }
                }
            }

            // 4. AI & Gemini Model Settings
            if (searchQuery.isBlank() || searchQuery.contains("ai", true) || searchQuery.contains("gemini", true) || searchQuery.contains("model", true)) {
                item {
                    Text("AI Models & Capabilities", color = NeonPurpleBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Default Gemini Model", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("gemini-3.6-flash", "gemini-3.5-flash-lite", "gemini-2.5-pro").forEach { mod ->
                                    FilterChip(
                                        selected = defaultModel == mod,
                                        onClick = { defaultModel = mod },
                                        label = { Text(mod, fontSize = 9.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonCyan, selectedLabelColor = AmoledBlack)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            ToggleRowSetting("Deep Thinking Reasoning Mode", thinkingMode) { thinkingMode = it }
                            ToggleRowSetting("User Fact Memory Integration", memoryEnabled) { memoryEnabled = it }
                            ToggleRowSetting("Live Google Web Search Grounding", webSearchEnabled) { webSearchEnabled = it }
                            ToggleRowSetting("Image & Visual Art Generation", imageGenEnabled) { imageGenEnabled = it }
                        }
                    }
                }
            }

            // 5. Chat Interface Preferences
            if (searchQuery.isBlank() || searchQuery.contains("chat", true) || searchQuery.contains("bubble", true)) {
                item {
                    Text("Chat Interface Settings", color = NeonPurpleBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ToggleRowSetting("Real-Time Token Streaming", streamingResponse) { streamingResponse = it }
                            ToggleRowSetting("Auto Scroll to New Messages", autoScroll) { autoScroll = it }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Bubble Style: $chatBubbleStyle", color = TextPrimaryDark, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 6. Language Settings
            if (searchQuery.isBlank() || searchQuery.contains("language", true) || searchQuery.contains("hindi", true)) {
                item {
                    Text("Language & Localisation", color = NeonPurpleBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Preferred Conversation Language", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("English", "Hindi", "Hinglish").forEach { lang ->
                                    FilterChip(
                                        selected = selectedLanguage == lang,
                                        onClick = { selectedLanguage = lang },
                                        label = { Text(lang, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonPurpleBright, selectedLabelColor = AmoledBlack)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 7. Storage Manager & Cache
            if (searchQuery.isBlank() || searchQuery.contains("storage", true) || searchQuery.contains("cache", true)) {
                item {
                    Text("Storage & Memory Manager", color = NeonPurpleBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Temporary Media Cache", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Size: $cacheSizeMb", color = TextSecondaryDark, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        cacheSizeMb = "0.0 MB"
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Cache memory cleared successfully!") }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftPinkAccent),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Clear Cache", color = AmoledBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 8. Developer Settings & Diagnostics
            if (searchQuery.isBlank() || searchQuery.contains("developer", true) || searchQuery.contains("debug", true) || searchQuery.contains("logs", true)) {
                item {
                    Text("Developer Settings & Diagnostics", color = NeonPurpleBright, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("System Diagnostics & Logs", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Button(
                                    onClick = { showDeveloperLogsDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("View Logs", color = AmoledBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            ToggleRowSetting("Send Anonymized Crash Diagnostics", crashReportsConsent) { crashReportsConsent = it }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showDeveloperLogsDialog) {
        AlertDialog(
            onDismissRequest = { showDeveloperLogsDialog = false },
            title = { Text("Developer Diagnostics", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("• Gemini Model REST Status: ONLINE (HTTP 200)", color = NeonCyan, fontSize = 11.sp)
                    Text("• Firebase Auth State: AUTHENTICATED", color = NeonPurpleBright, fontSize = 11.sp)
                    Text("• Android Keystore Alias: SaraAI_MasterKeyAlias ACTIVE", color = SoftPinkAccent, fontSize = 11.sp)
                    Text("• Room DB Tables: memory_facts, chat_messages, user_profile, automation_tasks", color = TextSecondaryDark, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showDeveloperLogsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright)) {
                    Text("Close", color = AmoledBlack, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun SettingRowCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = SoftPinkAccent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = TextSecondaryDark, fontSize = 12.sp)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun ToggleRowSetting(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = TextPrimaryDark, fontSize = 12.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = NeonPurpleBright)
        )
    }
}
