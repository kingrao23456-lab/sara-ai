package com.example.presentation.ui.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.AIPersonality
import com.example.domain.model.UserProfile
import com.example.ui.theme.*

@Composable
fun SaraDrawerContent(
    userProfile: UserProfile,
    activePersonality: AIPersonality,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = SurfaceDark,
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackgroundGlass)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.sara_app_icon_1785575828281),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(userProfile.name, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(userProfile.email, color = TextSecondaryDark, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Active: ${activePersonality.name}", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Items
            DrawerMenuItem(icon = Icons.Default.Person, label = "User Profile", onClick = { onNavigate("profile") }, testTag = "drawer_item_profile")
            DrawerMenuItem(icon = Icons.Default.Chat, label = "Conversations", onClick = { onNavigate("chat") }, testTag = "drawer_item_chat")
            DrawerMenuItem(icon = Icons.Default.CameraAlt, label = "Gemini Live Camera & Vision", onClick = { onNavigate("livevision") }, testTag = "drawer_item_vision")
            DrawerMenuItem(icon = Icons.Default.Description, label = "Document AI & Screenshot", onClick = { onNavigate("documents") }, testTag = "drawer_item_documents")
            DrawerMenuItem(icon = Icons.Default.SmartToy, label = "Android Assistant Center", onClick = { onNavigate("assistant") }, testTag = "drawer_item_assistant")
            DrawerMenuItem(icon = Icons.Default.QrCodeScanner, label = "QR & OCR Vision Tools", onClick = { onNavigate("qrocr") }, testTag = "drawer_item_qrocr")
            DrawerMenuItem(icon = Icons.Default.Tune, label = "AI Models & Thinking Modes", onClick = { onNavigate("models") }, testTag = "drawer_item_models")
            DrawerMenuItem(icon = Icons.Default.Brush, label = "AI Art & Image Generator", onClick = { onNavigate("imagegen") }, testTag = "drawer_item_imagegen")
            DrawerMenuItem(icon = Icons.Default.FolderSpecial, label = "Prompt Library & Templates", onClick = { onNavigate("prompts") }, testTag = "drawer_item_prompts")
            DrawerMenuItem(icon = Icons.Default.Workspaces, label = "AI Workspace & Notes", onClick = { onNavigate("workspace") }, testTag = "drawer_item_workspace")
            DrawerMenuItem(icon = Icons.Default.Search, label = "Global Intelligence Search", onClick = { onNavigate("search") }, testTag = "drawer_item_search")
            DrawerMenuItem(icon = Icons.Default.Face, label = "AI Personalities (8)", onClick = { onNavigate("personalities") }, testTag = "drawer_item_personalities")
            DrawerMenuItem(icon = Icons.Default.CalendarToday, label = "AI Scheduler & Routines", onClick = { onNavigate("planner") }, testTag = "drawer_item_planner")
            DrawerMenuItem(icon = Icons.Default.NoteAlt, label = "AI Notes & Tasks", onClick = { onNavigate("notestasks") }, testTag = "drawer_item_notestasks")
            DrawerMenuItem(icon = Icons.Default.History, label = "History, Downloads & Clipboard", onClick = { onNavigate("history") }, testTag = "drawer_item_history")
            DrawerMenuItem(icon = Icons.Default.GridView, label = "Quick Panel & Widgets", onClick = { onNavigate("quickpanel") }, testTag = "drawer_item_quickpanel")
            DrawerMenuItem(icon = Icons.Default.AutoAwesome, label = "Automation Routines", onClick = { onNavigate("automation") }, testTag = "drawer_item_automation")
            DrawerMenuItem(icon = Icons.Default.Notifications, label = "Notifications", onClick = { onNavigate("notifications") }, testTag = "drawer_item_notifications")
            DrawerMenuItem(icon = Icons.Default.VerifiedUser, label = "App Permissions", onClick = { onNavigate("permissions") }, testTag = "drawer_item_permissions")
            DrawerMenuItem(icon = Icons.Default.Favorite, label = "AI Companion & Emotion Engine", onClick = { onNavigate("companion") }, testTag = "drawer_item_companion")
            DrawerMenuItem(icon = Icons.Default.EmojiEvents, label = "Achievements & Stats", onClick = { onNavigate("achievements") }, testTag = "drawer_item_achievements")
            DrawerMenuItem(icon = Icons.Default.Speed, label = "Performance & Release Verification", onClick = { onNavigate("performance") }, testTag = "drawer_item_performance")
            DrawerMenuItem(icon = Icons.Default.Help, label = "Help Center & About", onClick = { onNavigate("help") }, testTag = "drawer_item_help")
            DrawerMenuItem(icon = Icons.Default.Security, label = "Security & Defense Center", onClick = { onNavigate("security") }, testTag = "drawer_item_security")
            DrawerMenuItem(icon = Icons.Default.CloudSync, label = "Cloud Sync & Backup", onClick = { onNavigate("cloudsync") }, testTag = "drawer_item_cloudsync")
            DrawerMenuItem(icon = Icons.Default.PrivacyTip, label = "Privacy Controls & Policy", onClick = { onNavigate("privacy") }, testTag = "drawer_item_privacy")
            DrawerMenuItem(icon = Icons.Default.Settings, label = "Settings & Preferences", onClick = { onNavigate("settings") }, testTag = "drawer_item_settings")

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(color = BorderPurpleGlow)

            Spacer(modifier = Modifier.height(12.dp))

            DrawerMenuItem(
                icon = Icons.Default.Logout,
                label = "Sign Out",
                onClick = onLogout,
                testTag = "drawer_item_logout",
                textColor = SoftPinkAccent
            )
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String,
    textColor: androidx.compose.ui.graphics.Color = TextPrimaryDark
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, color = textColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
