package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.PersonalityRepository
import com.example.domain.model.AIPersonality
import com.example.domain.model.Gender
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PersonalitiesViewModel(
    private val personalityRepository: PersonalityRepository
) : ViewModel() {

    private val _genderFilter = MutableStateFlow<Gender?>(null) // null = ALL
    val genderFilter: StateFlow<Gender?> = _genderFilter.asStateFlow()

    val activePersonality = personalityRepository.activePersonality

    val displayedPersonalities: StateFlow<List<AIPersonality>> = _genderFilter.map { gender ->
        if (gender == null) personalityRepository.allPersonalities
        else personalityRepository.allPersonalities.filter { it.gender == gender }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = personalityRepository.allPersonalities
    )

    fun filterByGender(gender: Gender?) {
        _genderFilter.value = gender
    }

    fun selectPersonality(personality: AIPersonality) {
        viewModelScope.launch {
            personalityRepository.selectPersonality(personality.id)
        }
    }
}
