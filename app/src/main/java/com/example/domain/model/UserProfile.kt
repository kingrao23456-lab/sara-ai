package com.example.domain.model

data class UserProfile(
    val userId: String = "guest_user",
    val name: String = "Alex",
    val email: String = "alex@sara.ai",
    val photoUrl: String? = null,
    val selectedPersonalityId: String = "zoya",
    val language: String = "English", // English, Hindi, Hinglish, Auto
    val theme: String = "AMOLED Black",
    val isGuest: Boolean = true,
    val voiceSpeed: Float = 1.0f,
    val voicePitch: Float = 1.0f,
    val isCloudSyncEnabled: Boolean = true
)
