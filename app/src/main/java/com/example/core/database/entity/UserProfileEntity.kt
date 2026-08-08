package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val selectedPersonalityId: String,
    val language: String,
    val theme: String,
    val isGuest: Boolean,
    val voiceSpeed: Float,
    val voicePitch: Float,
    val isCloudSyncEnabled: Boolean
)
