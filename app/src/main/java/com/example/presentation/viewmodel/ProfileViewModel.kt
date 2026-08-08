package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val userProfile: StateFlow<UserProfile> = authRepository.currentUserProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    fun updateName(name: String) {
        viewModelScope.launch {
            val current = userProfile.value
            authRepository.updateUserProfile(current.copy(name = name))
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            val current = userProfile.value
            authRepository.updateUserProfile(current.copy(language = language))
        }
    }

    fun updateVoiceSettings(speed: Float, pitch: Float) {
        viewModelScope.launch {
            val current = userProfile.value
            authRepository.updateUserProfile(current.copy(voiceSpeed = speed, voicePitch = pitch))
        }
    }

    fun toggleCloudSync(enabled: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value
            authRepository.updateUserProfile(current.copy(isCloudSyncEnabled = enabled))
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
