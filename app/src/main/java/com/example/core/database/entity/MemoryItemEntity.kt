package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_items")
data class MemoryItemEntity(
    @PrimaryKey val id: String,
    val keyTag: String,
    val content: String,
    val category: String,
    val timestamp: Long,
    val isEncrypted: Boolean
)
