package com.example.domain.model

enum class Sender { USER, AI, SYSTEM }

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sessionId: String = "default_session",
    val sender: Sender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null,
    val personalityId: String = "zoya",
    val isVoiceMessage: Boolean = false,
    val groundedSources: List<String> = emptyList()
)
