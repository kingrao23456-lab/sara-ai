package com.example.presentation.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.presentation.viewmodel.ProfileViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutComplete: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()

    var nameInput by remember(userProfile.name) { mutableStateOf(userProfile.name) }
    var expandedLangDropdown by remember { mutableStateOf(false) }

    val languages = listOf("English", "Hindi", "Hinglish", "Auto Language Detection")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile & Preferences", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // User Header Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderPurpleGlow, RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(NeonPurplePrimary)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sara_app_icon_1785575828281),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(66.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = userProfile.name,
                            color = TextPrimaryDark,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = userProfile.email,
                            color = TextSecondaryDark,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = if (userProfile.isGuest) SoftPinkAccent.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (userProfile.isGuest) "Guest Session" else "Cloud Account Verified",
                                color = if (userProfile.isGuest) SoftPinkAccent else NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Edit Profile Form
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Account Settings", color = NeonPurpleBright, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it; viewModel.updateName(it) },
                        label = { Text("Display Name", color = TextSecondaryDark) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonPurpleBright) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurpleBright,
                            unfocusedBorderColor = BorderPurpleGlow,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Language Selector Dropdown
                    Box {
                        OutlinedTextField(
                            value = userProfile.language,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("AI Communication Language", color = TextSecondaryDark) },
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = SoftPinkAccent) },
                            trailingIcon = {
                                IconButton(onClick = { expandedLangDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextPrimaryDark)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonPurpleBright,
                                unfocusedBorderColor = BorderPurpleGlow,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = expandedLangDropdown,
                            onDismissRequest = { expandedLangDropdown = false },
                            modifier = Modifier.background(SurfaceDark)
                        ) {
                            languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang, color = TextPrimaryDark) },
                                    onClick = {
                                        viewModel.updateLanguage(lang)
                                        expandedLangDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Cloud Sync Switch
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Firestore Cloud Memory Sync", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Keep memory & chat history synced across devices", color = TextSecondaryDark, fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = userProfile.isCloudSyncEnabled,
                        onCheckedChange = { viewModel.toggleCloudSync(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonPurpleBright, checkedTrackColor = SurfaceVariantDark)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            Button(
                onClick = {
                    viewModel.logout()
                    onLogoutComplete()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoftPinkAccent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_button")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = AmoledBlack)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", color = AmoledBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
