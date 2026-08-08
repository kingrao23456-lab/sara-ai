package com.example.domain.model

data class AppNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "INFO", // INFO, MEMORY_SAVED, AI_INSIGHT
    val isRead: Boolean = false
)
