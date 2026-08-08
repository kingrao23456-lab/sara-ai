package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val sender: String,
    val text: String,
    val timestamp: Long,
    val imagePath: String? = null,
    val personalityId: String,
    val isVoiceMessage: Boolean,
    val groundedSourcesJson: String = "[]"
)
