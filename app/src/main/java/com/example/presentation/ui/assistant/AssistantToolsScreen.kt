package com.example.presentation.ui.assistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantToolsScreen() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var overlayBubbleEnabled by remember { mutableStateOf(true) }
    var backgroundServiceEnabled by remember { mutableStateOf(true) }
    var quickReplyEnabled by remember { mutableStateOf(true) }
    var accessibilityEnabled by remember { mutableStateOf(false) }

    var smsPhoneNumber by remember { mutableStateOf("+1 555-0199") }
    var smsMessageText by remember { mutableStateOf("Hey! Meeting you at 5 PM.") }

    var alarmHour by remember { mutableStateOf("7") }
    var alarmMinute by remember { mutableStateOf("00") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Android Assistant Center", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
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
            // Floating Overlay Assistant Bubble Settings
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderPurpleGlow, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = NeonPurpleBright)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Floating Overlay Assistant Bubble", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Draggable floating icon over all apps with quick screenshot, voice button, and floating chat.", color = TextSecondaryDark, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Enable Floating Overlay Bubble", color = TextPrimaryDark, fontSize = 13.sp)
                            Switch(
                                checked = overlayBubbleEnabled,
                                onCheckedChange = { overlayBubbleEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = NeonPurpleBright)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Persistent Background Service", color = TextPrimaryDark, fontSize = 13.sp)
                            Switch(
                                checked = backgroundServiceEnabled,
                                onCheckedChange = { backgroundServiceEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = NeonCyan)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Accessibility Assistant Service", color = TextPrimaryDark, fontSize = 13.sp)
                            Switch(
                                checked = accessibilityEnabled,
                                onCheckedChange = {
                                    accessibilityEnabled = it
                                    if (it) {
                                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                        context.startActivity(intent)
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = SoftPinkAccent)
                            )
                        }
                    }
                }
            }

            // Quick Device Control System Actions
            item {
                Text("System Device Control Shortcuts", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistantShortcutCard(
                        icon = Icons.Default.Wifi,
                        title = "Wi-Fi Settings",
                        onClick = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
                        modifier = Modifier.weight(1f)
                    )
                    AssistantShortcutCard(
                        icon = Icons.Default.Bluetooth,
                        title = "Bluetooth",
                        onClick = { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
                        modifier = Modifier.weight(1f)
                    )
                    AssistantShortcutCard(
                        icon = Icons.Default.DisplaySettings,
                        title = "Display",
                        onClick = { context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Communication & Share Intents
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Communication & Share Intents", color = NeonPurpleBright, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = smsPhoneNumber,
                            onValueChange = { smsPhoneNumber = it },
                            label = { Text("Phone Number", color = TextSecondaryDark, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = smsMessageText,
                            onValueChange = { smsMessageText = it },
                            label = { Text("SMS / Social Message Content", color = TextSecondaryDark, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$smsPhoneNumber"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = AmoledBlack, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Dialer", color = AmoledBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$smsPhoneNumber")).apply {
                                        putExtra("sms_body", smsMessageText)
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Sms, contentDescription = null, tint = AmoledBlack, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Send SMS", color = AmoledBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, smsMessageText)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share via Sara AI"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftPinkAccent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = AmoledBlack, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", color = AmoledBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Calendar & Alarms Actions
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Calendar Events & Morning Alarm Tools", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_INSERT).apply {
                                        data = CalendarContract.Events.CONTENT_URI
                                        putExtra(CalendarContract.Events.TITLE, "Sara AI Scheduled Session")
                                        putExtra(CalendarContract.Events.DESCRIPTION, "AI reminder session created automatically.")
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AmoledBlack, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Calendar Event", color = AmoledBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                                        putExtra(AlarmClock.EXTRA_MESSAGE, "Morning Briefing with Sara AI")
                                        putExtra(AlarmClock.EXTRA_HOUR, 7)
                                        putExtra(AlarmClock.EXTRA_MINUTES, 0)
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = AmoledBlack, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Set 7 AM Alarm", color = AmoledBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Clipboard Manager Card
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
                            Text("Clipboard Manager", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            TextButton(onClick = {
                                clipboardManager.setText(AnnotatedString(""))
                                coroutineScope.launch { snackbarHostState.showSnackbar("Clipboard cleared!") }
                            }) {
                                Text("Clear", color = SoftPinkAccent, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val currentText = clipboardManager.getText()?.text ?: "Clipboard is currently empty"
                        Surface(
                            color = AmoledBlack,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(currentText, color = TextSecondaryDark, fontSize = 12.sp, modifier = Modifier.padding(10.dp), maxLines = 2)
                        }
                    }
                }
            }

            // Google Maps Launcher
            item {
                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=nearby+coffee+shops")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = AmoledBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Google Maps Navigation", color = AmoledBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AssistantShortcutCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = NeonPurpleBright, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, color = TextPrimaryDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
