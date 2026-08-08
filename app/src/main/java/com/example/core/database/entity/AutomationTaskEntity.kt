package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_tasks")
data class AutomationTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val cronSchedule: String,
    val isEnabled: Boolean,
    val actionType: String
)
