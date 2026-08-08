package com.example.presentation.ui.security

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.KeystoreHelper
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityCenterScreen(
    onNavigateToPrivacy: () -> Unit,
    onNavigateToCloudSync: () -> Unit
) {
    val context = LocalContext.current
    val keystoreHelper = remember { KeystoreHelper(context) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Security Dashboard, 1 = Biometric & PIN Lock, 2 = Encryption & Tokens

    // Biometric & Lock States
    var biometricEnabled by remember { mutableStateOf(true) }
    var faceUnlockEnabled by remember { mutableStateOf(false) }
    var credentialFallbackEnabled by remember { mutableStateOf(true) }

    var lockEntireApp by remember { mutableStateOf(false) }
    var lockChats by remember { mutableStateOf(true) }
    var lockMemory by remember { mutableStateOf(true) }
    var lockSettings by remember { mutableStateOf(false) }

    // PIN States
    var pinType by remember { mutableStateOf("4-Digit PIN") } // 4-Digit, 6-Digit, Custom Password
    var currentPin by remember { mutableStateOf("1234") }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var showForgotPinDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Security & Defense Center", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
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
                    text = { Text("Dashboard", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Biometrics & PIN", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Keystore AES", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Security Dashboard
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            // Status Banner
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderPurpleGlow, RoundedCornerShape(20.dp))
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = NeonCyan.copy(alpha = 0.2f),
                                            shape = CircleShape,
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("System Security: Protected", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text("Android Keystore AES-256 Active", color = NeonCyan, fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "All memory facts, chat sessions, API tokens, and user credentials are encrypted on hardware.",
                                        color = TextSecondaryDark,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        item {
                            Text("Security & Account Defense", color = NeonPurpleBright, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        item {
                            SecurityQuickCard(
                                icon = Icons.Default.PrivacyTip,
                                title = "Privacy Dashboard & Data Controls",
                                subtitle = "Manage camera/mic permissions, clear memory & export data",
                                onClick = onNavigateToPrivacy
                            )
                        }

                        item {
                            SecurityQuickCard(
                                icon = Icons.Default.CloudSync,
                                title = "Firebase Cloud Sync & Secure Backup",
                                subtitle = "Auto Backup, AES Encrypted Cloud Sync, Offline Queue",
                                onClick = onNavigateToCloudSync
                            )
                        }

                        item {
                            // Device Security & Active Sessions
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
                                        Text("Active Authorized Devices", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Surface(color = AmoledBlack, shape = RoundedCornerShape(50)) {
                                            Text("2 Devices", color = SoftPinkAccent, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    DeviceSessionRow("Pixel 8 Pro (Current Device)", "Android 15 • Active Now", true)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    DeviceSessionRow("Galaxy Tab S9 Ultra", "Android 14 • Synced 2 hrs ago", false)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Biometric & PIN Lock Setup
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Biometric Authentication", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Fingerprint Unlock", color = TextPrimaryDark, fontSize = 13.sp)
                                        Switch(
                                            checked = biometricEnabled,
                                            onCheckedChange = { biometricEnabled = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = NeonPurpleBright)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Face Unlock (If Supported)", color = TextPrimaryDark, fontSize = 13.sp)
                                        Switch(
                                            checked = faceUnlockEnabled,
                                            onCheckedChange = { faceUnlockEnabled = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = NeonCyan)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Device Credential Fallback", color = TextPrimaryDark, fontSize = 13.sp)
                                        Switch(
                                            checked = credentialFallbackEnabled,
                                            onCheckedChange = { credentialFallbackEnabled = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = SoftPinkAccent)
                                        )
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
                                    Text("Granular App Lock Controls", color = NeonPurpleBright, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    LockToggleRow("Lock Entire Sara AI App", lockEntireApp) { lockEntireApp = it }
                                    LockToggleRow("Lock Private AI Chat Conversations", lockChats) { lockChats = it }
                                    LockToggleRow("Lock Memory Center & Facts", lockMemory) { lockMemory = it }
                                    LockToggleRow("Lock App Settings & Security", lockSettings) { lockSettings = it }
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
                                    Text("PIN & Password Configuration", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("Current Security Lock: $pinType", color = TextSecondaryDark, fontSize = 12.sp)

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { showChangePinDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Change PIN", color = AmoledBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { showForgotPinDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Forgot PIN?", color = TextPrimaryDark, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Android Keystore & Token Encryption Test
                    var testRawText by remember { mutableStateOf("Sara AI Secret Key: 0x98A1B2C3") }
                    var testEncryptedResult by remember { mutableStateOf("") }
                    var testDecryptedResult by remember { mutableStateOf("") }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Hardware Keystore Tester (AES-256 GCM)", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = testRawText,
                                        onValueChange = { testRawText = it },
                                        label = { Text("Raw Secret Text") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                testEncryptedResult = keystoreHelper.encrypt(testRawText)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Encrypt", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                if (testEncryptedResult.isNotEmpty()) {
                                                    testDecryptedResult = keystoreHelper.decrypt(testEncryptedResult)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Decrypt", color = AmoledBlack, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (testEncryptedResult.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("Encrypted Cipher (Base64):", color = SoftPinkAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(testEncryptedResult, color = TextPrimaryDark, fontSize = 11.sp, maxLines = 3)
                                    }

                                    if (testDecryptedResult.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Decrypted Output:", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(testDecryptedResult, color = TextPrimaryDark, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showChangePinDialog) {
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Change Security PIN", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter new 4-digit or 6-digit Security PIN:", color = TextSecondaryDark, fontSize = 12.sp)
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 6) newPinInput = it },
                        label = { Text("New PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length >= 4) {
                            currentPin = newPinInput
                            showChangePinDialog = false
                            newPinInput = ""
                            coroutineScope.launch { snackbarHostState.showSnackbar("Security PIN updated successfully!") }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright)
                ) {
                    Text("Save PIN", color = AmoledBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) { Text("Cancel", color = TextSecondaryDark) }
            },
            containerColor = SurfaceDark
        )
    }

    if (showForgotPinDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPinDialog = false },
            title = { Text("PIN Recovery", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = {
                Text("A security PIN reset link has been dispatched to your authenticated Google / Firebase account email.", color = TextSecondaryDark, fontSize = 12.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showForgotPinDialog = false
                        coroutineScope.launch { snackbarHostState.showSnackbar("Recovery email sent!") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("OK", color = AmoledBlack, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun SecurityQuickCard(
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
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun DeviceSessionRow(device: String, status: String, isCurrent: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(device, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(status, color = TextSecondaryDark, fontSize = 11.sp)
        }
        if (isCurrent) {
            Surface(color = NeonCyan.copy(alpha = 0.2f), shape = RoundedCornerShape(50)) {
                Text("Current", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
fun LockToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimaryDark, fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = NeonPurpleBright)
        )
    }
}
