package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.AppDatabase
import com.example.core.datastore.UserPreferencesRepository
import com.example.core.security.KeystoreHelper
import com.example.data.repository.*
import com.example.domain.model.AIPersonality
import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val prefs = UserPreferencesRepository(application)
    private val keystoreHelper = KeystoreHelper(application)

    val memoryRepository = MemoryRepository(db.memoryDao(), keystoreHelper)
    val chatRepository = ChatRepository(db.chatDao(), memoryRepository)
    val personalityRepository = PersonalityRepository(prefs)
    val authRepository = AuthRepository(db.userDao(), prefs)
    val automationRepository = AutomationRepository(db.automationDao(), db.notificationDao())

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val currentUserProfile: StateFlow<UserProfile> = authRepository.currentUserProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    val activePersonality: StateFlow<AIPersonality> = personalityRepository.activePersonality.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AIPersonality.ZOYA
    )

    val unreadNotificationsCount: StateFlow<Int> = automationRepository.notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
}
