package com.example.presentation.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.R
import com.example.domain.model.AIPersonality
import com.example.domain.model.ChatMessage
import com.example.domain.model.Sender
import com.example.presentation.viewmodel.ChatViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    activePersonality: AIPersonality,
    userLanguage: String,
    initialPrompt: String? = null,
    onNavigateToVision: () -> Unit
) {
    var inputText by remember { mutableStateOf(initialPrompt ?: "") }
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val searchGrounding by viewModel.searchGroundingEnabled.collectAsState()
    val mapsGrounding by viewModel.mapsGroundingEnabled.collectAsState()
    val replyingToMessage by viewModel.replyingToMessage.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val pinnedIds by viewModel.pinnedMessageIds.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadMessages()
        if (initialPrompt != null && initialPrompt.isNotBlank()) {
            viewModel.sendMessage(initialPrompt, activePersonality, userLanguage)
            inputText = ""
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NeonPurplePrimary)
                                .padding(1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.sara_avatar_1785575842420),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = activePersonality.name,
                                color = TextPrimaryDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${activePersonality.title} • Online",
                                color = NeonCyan,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val exported = viewModel.exportChatAsText()
                        clipboardManager.setText(AnnotatedString(exported))
                        coroutineScope.launch { snackbarHostState.showSnackbar("Chat history exported & copied to clipboard!") }
                    }) {
                        Icon(Icons.Default.IosShare, contentDescription = "Export Chat", tint = NeonCyan)
                    }
                    IconButton(onClick = { viewModel.toggleSearchGrounding() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Grounding",
                            tint = if (searchGrounding) NeonPurpleBright else TextMuted
                        )
                    }
                    IconButton(onClick = { viewModel.toggleMapsGrounding() }) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Maps Grounding",
                            tint = if (mapsGrounding) SoftPinkAccent else TextMuted
                        )
                    }
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = TextPrimaryDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Session Folder Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("default_session" to "General", "work_session" to "Work", "learning_session" to "Learning").forEach { (id, label) ->
                    val isSel = currentSessionId == id
                    Surface(
                        color = if (isSel) NeonPurplePrimary else AmoledBlack,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable { viewModel.setSession(id) }
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) AmoledBlack else TextSecondaryDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Grounding Badges Bar
            if (searchGrounding || mapsGrounding) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (searchGrounding) {
                        Text("🔍 Search Grounding Active", color = NeonPurpleBright, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (mapsGrounding) {
                        Text("📍 Maps Grounding Active", color = SoftPinkAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Messages Feed
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        activePersonality = activePersonality,
                        isPinned = pinnedIds.contains(msg.id),
                        onCopyText = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                        },
                        onReply = { viewModel.setReplyToMessage(msg) },
                        onPin = { viewModel.togglePinMessage(msg.id) },
                        onDelete = { id ->
                            viewModel.deleteMessage(id)
                        }
                    )
                }

                if (isGenerating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .background(SurfaceDark, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = NeonPurpleBright,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${activePersonality.name} is generating reply...",
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp
                                )
                            }
                            TextButton(onClick = { viewModel.stopGeneration() }) {
                                Text("Stop", color = SoftPinkAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Reply Preview Banner
            replyingToMessage?.let { replyMsg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Replying to ${replyMsg.sender.name}", color = NeonPurpleBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(replyMsg.text, color = TextSecondaryDark, fontSize = 12.sp, maxLines = 1)
                    }
                    IconButton(onClick = { viewModel.setReplyToMessage(null) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel Reply", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Input Bar
            Surface(
                color = SurfaceDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateToVision,
                        modifier = Modifier.testTag("vision_camera_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera Vision", tint = SoftPinkAccent)
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask Sara anything...", color = TextMuted, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        singleLine = false,
                        maxLines = 4
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val text = inputText
                                inputText = ""
                                viewModel.sendMessage(text, activePersonality, userLanguage)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isGenerating,
                        modifier = Modifier.testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) NeonPurpleBright else TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    activePersonality: AIPersonality,
    isPinned: Boolean = false,
    onCopyText: (String) -> Unit,
    onReply: () -> Unit = {},
    onPin: () -> Unit = {},
    onDelete: (String) -> Unit
) {
    val isUser = message.sender == Sender.USER

    Column(
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            color = if (isUser) SurfaceVariantDark else SurfaceDark,
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            border = if (isPinned) androidx.compose.foundation.BorderStroke(1.dp, NeonPurpleBright) else if (isUser) androidx.compose.foundation.BorderStroke(1.dp, BorderPurpleGlow) else null,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isUser) "You" else activePersonality.name,
                            color = if (isUser) SoftPinkAccent else NeonPurpleBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isPinned) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = NeonPurpleBright, modifier = Modifier.size(12.dp))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (!isUser) {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            IconButton(
                                onClick = {
                                    com.example.core.voice.AndroidVoiceManager.speak(context, message.text, activePersonality)
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Speak Aloud", tint = SoftPinkAccent, modifier = Modifier.size(14.dp))
                            }
                        }
                        IconButton(onClick = onReply, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Reply, contentDescription = "Reply", tint = TextMuted, modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = onPin, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.PushPin, contentDescription = "Pin", tint = if (isPinned) NeonPurpleBright else TextMuted, modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = { onCopyText(message.text) }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.text,
                    color = TextPrimaryDark,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
