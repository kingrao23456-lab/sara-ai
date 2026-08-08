package com.example.presentation.ui.prompts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class PromptTemplate(
    val id: String,
    val title: String,
    val category: String, // Coding, Writing, Productivity, Study, Creative, Business
    val description: String,
    val promptText: String,
    var isFavorite: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptLibraryScreen(
    onSendToChat: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Coding") }
    var newPromptText by remember { mutableStateOf("") }

    val categories = listOf("All", "Favorites", "Coding", "Writing", "Productivity", "Study", "Creative", "Business")

    val promptsList = remember {
        mutableStateListOf(
            PromptTemplate(
                "1", "Senior Kotlin Code Review", "Coding",
                "Performs deep architectural code review with performance & safety tips.",
                "Act as a Senior Android Kotlin Engineer. Review the following code snippet for memory leaks, performance bottlenecks, and modern Jetpack Compose clean architecture best practices:\n\n[INSERT CODE HERE]",
                isFavorite = true
            ),
            PromptTemplate(
                "2", "High-Converting Email Copy", "Writing",
                "Crafts persuasive and professional email campaigns.",
                "Write a compelling sales outreach email for a new AI productivity software named Sara AI. Tone should be warm, professional, and highlight time savings and long-term memory capabilities.",
                isFavorite = false
            ),
            PromptTemplate(
                "3", "Feynman Technique Study Explainer", "Study",
                "Explains complex concepts simply using Feynman framework.",
                "Explain the concept of Quantum Entanglement using the Feynman Technique as if I am 12 years old. Use real-life analogies, simple language, and end with 3 quick quiz questions to test my understanding.",
                isFavorite = true
            ),
            PromptTemplate(
                "4", "Daily Eisenhower Matrix Planner", "Productivity",
                "Categorizes messy tasks into Urgent vs Important matrix.",
                "Organize the following raw task list into an Eisenhower Matrix (Do First, Schedule, Delegate, Don't Do):\n\n[INSERT TASKS HERE]",
                isFavorite = false
            ),
            PromptTemplate(
                "5", "Cyberpunk Sci-Fi Story Outline", "Creative",
                "Generates rich plot twists and worldbuilding for sci-fi stories.",
                "Write a detailed 5-chapter plot outline for a cyberpunk thriller set in Neo-Tokyo 2088 where an AI assistant develops human emotions and protects its creator.",
                isFavorite = false
            ),
            PromptTemplate(
                "6", "Executive Business Pitch Deck", "Business",
                "Structures a 10-slide VC pitch deck outline.",
                "Create a 10-slide pitch deck structure for an AI startup offering personalized memory engines for smartphones. Include problem statement, market size, value proposition, and traction metrics.",
                isFavorite = true
            )
        )
    }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val filteredPrompts = remember(searchQuery, selectedCategory, promptsList.toList()) {
        promptsList.filter { prompt ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Favorites" -> prompt.isFavorite
                else -> prompt.category.equals(selectedCategory, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isBlank() ||
                    prompt.title.contains(searchQuery, ignoreCase = true) ||
                    prompt.description.contains(searchQuery, ignoreCase = true) ||
                    prompt.promptText.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("AI Prompt Library & Templates", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        val json = promptsList.joinToString("\n---\n") { "${it.title} [${it.category}]: ${it.promptText}" }
                        clipboardManager.setText(AnnotatedString(json))
                        coroutineScope.launch { snackbarHostState.showSnackbar("Prompt library exported to clipboard!") }
                    }) {
                        Icon(Icons.Default.Backup, contentDescription = "Export Prompts", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NeonPurpleBright,
                contentColor = AmoledBlack,
                modifier = Modifier.testTag("add_prompt_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Prompt")
            }
        },
        containerColor = AmoledBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search 100+ AI prompts & templates...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonPurpleBright) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurpleBright,
                    unfocusedBorderColor = BorderPurpleGlow,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prompt_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonPurpleBright,
                            selectedLabelColor = AmoledBlack,
                            containerColor = SurfaceDark,
                            labelColor = TextSecondaryDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prompts List
            if (filteredPrompts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matching prompt templates found.", color = TextSecondaryDark, fontSize = 14.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredPrompts, key = { it.id }) { prompt ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(prompt.title, color = NeonPurpleBright, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = NeonCyan.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(prompt.category, color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = {
                                                val index = promptsList.indexOfFirst { it.id == prompt.id }
                                                if (index != -1) {
                                                    promptsList[index] = promptsList[index].copy(isFavorite = !promptsList[index].isFavorite)
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (prompt.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = SoftPinkAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(prompt.description, color = TextSecondaryDark, fontSize = 12.sp)

                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = AmoledBlack,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(prompt.promptText, color = TextPrimaryDark, fontSize = 12.sp, modifier = Modifier.padding(10.dp), maxLines = 3)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(prompt.promptText))
                                            coroutineScope.launch { snackbarHostState.showSnackbar("Copied prompt to clipboard!") }
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMuted, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy", color = TextMuted, fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { onSendToChat(prompt.promptText) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "Send", tint = AmoledBlack, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Use in Chat", color = AmoledBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Custom Prompt Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = SurfaceDark,
                title = { Text("Create Custom AI Prompt", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Prompt Title") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newPromptText,
                            onValueChange = { newPromptText = it },
                            label = { Text("Prompt Instruction Text") },
                            modifier = Modifier.height(100.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank() && newPromptText.isNotBlank()) {
                                promptsList.add(
                                    0,
                                    PromptTemplate(
                                        id = System.currentTimeMillis().toString(),
                                        title = newTitle,
                                        category = newCategory,
                                        description = "Custom user prompt template.",
                                        promptText = newPromptText,
                                        isFavorite = true
                                    )
                                )
                                newTitle = ""
                                newPromptText = ""
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleBright)
                    ) {
                        Text("Save Prompt", color = AmoledBlack)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", color = SoftPinkAccent)
                    }
                }
            )
        }
    }
}
