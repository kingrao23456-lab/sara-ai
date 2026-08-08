package com.example.domain.model

data class AutomationTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val cronSchedule: String, // e.g. "Every Morning 8:00 AM"
    val isEnabled: Boolean = true,
    val actionType: String // "MORNING_BRIEF", "MEMORY_PRUNE", "REMINDER"
)
