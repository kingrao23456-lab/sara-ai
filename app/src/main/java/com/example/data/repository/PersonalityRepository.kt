package com.example.data.repository

import com.example.core.datastore.UserPreferencesRepository
import com.example.domain.model.AIPersonality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PersonalityRepository(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    val allPersonalities: List<AIPersonality> = AIPersonality.ALL_PERSONALITIES

    val activePersonality: Flow<AIPersonality> = userPreferencesRepository.selectedPersonalityId.map { id ->
        allPersonalities.find { it.id == id } ?: AIPersonality.ZOYA
    }

    suspend fun selectPersonality(id: String) {
        userPreferencesRepository.setPersonalityId(id)
    }

    fun getPersonalityById(id: String): AIPersonality {
        return allPersonalities.find { it.id == id } ?: AIPersonality.ZOYA
    }
}
