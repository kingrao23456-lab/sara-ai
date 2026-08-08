package com.example.presentation.ui.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.domain.model.MemoryItem
import com.example.presentation.viewmodel.MemoryViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel
) {
    val memoryItems by viewModel.memoryItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val askPermission by viewModel.askPermissionBeforeSaving.collectAsState()
    val perChatMemory by viewModel.perChatMemoryEnabled.collectAsState()
    val neverSaveSensitive by viewModel.neverSaveSensitive.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsCard by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Favorites") }

    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val categories = listOf(
        "All", "Personal Info", "Family", "Friends", "Work", "Study", "Coding",
        "Interests", "Goals", "Routine", "Favorites", "Health", "Notes"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Long-Term Memory Engine", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        val json = viewModel.exportBackupJson()
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(json))
                        coroutineScope.launch { snackbarHostState.showSnackbar("Memory database backed up & copied to clipboard!") }
                    }) {
                        Icon(Icons.Default.Backup, contentDescription = "Backup Memory", tint = NeonCyan)
                    }
                    IconButton(onClick = { showSettingsCard = !showSettingsCard }) {
                        Icon(Icons.Default.Tune, contentDescription = "Memory Settings", tint = NeonPurpleBright)
                    }
                    IconButton(onClick = { viewModel.clearAllMemory() }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Clear All Memory", tint = SoftPinkAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NeonPurplePrimary,
                contentColor = AmoledBlack,
                modifier = Modifier.testTag("add_memory_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Memory")
            }
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search stored facts, tags, preferences...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonPurpleBright) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurpleBright,
                    unfocusedBorderColor = BorderPurpleGlow,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("memory_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Smart Memory Controls Card
            if (showSettingsCard) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("🧠 Smart Memory Guard Rules", color = NeonPurpleBright, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ask before saving personal facts", color = TextPrimaryDark, fontSize = 12.sp)
                            Switch(
                                checked = askPermission,
                                onCheckedChange = { viewModel.toggleAskPermission() },
                                colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = NeonPurpleBright)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Per-Chat memory context active", color = TextPrimaryDark, fontSize = 12.sp)
                            Switch(
                                checked = perChatMemory,
                                onCheckedChange = { viewModel.togglePerChatMemory() },
                                colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = NeonCyan)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Never auto-save sensitive data", color = TextPrimaryDark, fontSize = 12.sp)
                            Switch(
                                checked = neverSaveSensitive,
                                onCheckedChange = { viewModel.toggleNeverSaveSensitive() },
                                colors = SwitchDefaults.colors(checkedThumbColor = AmoledBlack, checkedTrackColor = SoftPinkAccent)
                            )
                        }
                    }
                }
            }

            // Category Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { viewModel.setSelectedCategory(category) },
                        label = { Text(category, color = if (selectedCategory == category) AmoledBlack else TextPrimaryDark) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonPurpleBright,
                            containerColor = SurfaceDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == category,
                            borderColor = BorderPurpleGlow
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Keystore Encryption Badge
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderPurpleGlow, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Encrypted with Android Keystore AES-256 GCM",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Memory Items List
            if (memoryItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No memory items stored yet.", color = TextSecondaryDark, fontSize = 14.sp)
                        Text("Tap + to manually store key facts or preferences.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(memoryItems, key = { it.id }) { item ->
                        MemoryCardItem(
                            item = item,
                            onDelete = { id -> viewModel.deleteMemory(id) }
                        )
                    }
                }
            }
        }
    }

    // Add Memory Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Memory Item", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        label = { Text("Key Tag (e.g. Favorite Coffee)", color = TextSecondaryDark) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurpleBright,
                            unfocusedBorderColor = BorderPurpleGlow,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        )
                    )
                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        label = { Text("Memory Fact Content", color = TextSecondaryDark) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurpleBright,
                            unfocusedBorderColor = BorderPurpleGlow,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTag.isNotBlank() && newContent.isNotBlank()) {
                            viewModel.addMemory(newTag, newContent, newCategory)
                            newTag = ""
                            newContent = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurplePrimary)
                ) {
                    Text("Save Memory", color = AmoledBlack)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun MemoryCardItem(
    item: MemoryItem,
    onDelete: (String) -> Unit
) {
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
                Surface(
                    color = SurfaceVariantDark,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item.keyTag,
                        color = NeonPurpleBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = "Encrypted", tint = NeonCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = { onDelete(item.id) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.content,
                color = TextPrimaryDark,
                fontSize = 14.sp
            )
        }
    }
}
