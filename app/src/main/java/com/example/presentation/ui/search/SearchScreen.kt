package com.example.presentation.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ChatMessage
import com.example.domain.model.MemoryItem
import com.example.presentation.viewmodel.ChatViewModel
import com.example.presentation.viewmodel.MemoryViewModel
import com.example.ui.theme.*

data class SearchResult(
    val id: String,
    val title: String,
    val snippet: String,
    val category: String, // "Message", "Memory", "Note"
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    chatViewModel: ChatViewModel,
    memoryViewModel: MemoryViewModel,
    onNavigateToChat: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // All, Messages, Memories

    val chatMessages by chatViewModel.messages.collectAsState()
    val memoryItems by memoryViewModel.memoryItems.collectAsState()

    val filteredResults = remember(searchQuery, selectedFilter, chatMessages, memoryItems) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val list = mutableListOf<SearchResult>()
            if (selectedFilter == "All" || selectedFilter == "Messages") {
                chatMessages.filter { it.text.contains(searchQuery, ignoreCase = true) }.forEach {
                    list.add(
                        SearchResult(
                            id = it.id,
                            title = "Message (${it.sender})",
                            snippet = it.text,
                            category = "Message",
                            timestamp = it.timestamp
                        )
                    )
                }
            }
            if (selectedFilter == "All" || selectedFilter == "Memories") {
                memoryItems.filter {
                    it.keyTag.contains(searchQuery, ignoreCase = true) || it.content.contains(searchQuery, ignoreCase = true)
                }.forEach {
                    list.add(
                        SearchResult(
                            id = it.id,
                            title = "Memory: ${it.keyTag}",
                            snippet = it.content,
                            category = "Memory",
                            timestamp = it.timestamp
                        )
                    )
                }
            }
            list
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Intelligence Search", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
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
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search messages, memory, tags, notes...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonPurpleBright) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = SoftPinkAccent)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurpleBright,
                    unfocusedBorderColor = BorderPurpleGlow,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("global_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Messages", "Memories").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonPurpleBright,
                            selectedLabelColor = AmoledBlack,
                            containerColor = SurfaceDark,
                            labelColor = TextSecondaryDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (searchQuery.isBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SavedSearch, contentDescription = null, tint = TextMuted, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Search across chat logs, memories, and facts", color = TextSecondaryDark, fontSize = 13.sp)
                    }
                }
            } else if (filteredResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matching results found for \"$searchQuery\"", color = TextSecondaryDark, fontSize = 14.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredResults, key = { it.id }) { item ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (item.category == "Message") {
                                        onNavigateToChat(item.snippet)
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(item.title, color = NeonPurpleBright, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Surface(
                                        color = if (item.category == "Memory") NeonCyan.copy(alpha = 0.2f) else SoftPinkAccent.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            item.category,
                                            color = if (item.category == "Memory") NeonCyan else SoftPinkAccent,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.snippet, color = TextPrimaryDark, fontSize = 13.sp, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
    }
}
