package com.example.domain.model

data class MemoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val keyTag: String, // e.g., "Favorite Coffee", "Work Goal", "Birthday", "Pet Name"
    val content: String,
    val category: String = "General", // "Preferences", "Personal", "Work", "Ideas"
    val timestamp: Long = System.currentTimeMillis(),
    val isEncrypted: Boolean = true
)
