package com.example.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sara_user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val SELECTED_PERSONALITY_ID = stringPreferencesKey("selected_personality_id")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME = stringPreferencesKey("theme")
        val VOICE_PITCH = floatPreferencesKey("voice_pitch")
        val VOICE_SPEED = floatPreferencesKey("voice_speed")
        val IS_GUEST = booleanPreferencesKey("is_guest")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_LOGGED_IN] ?: false
    }

    val selectedPersonalityId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_PERSONALITY_ID] ?: "zoya"
    }

    val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE] ?: "English"
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_NAME] ?: "Alex"
    }

    val userEmail: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_EMAIL] ?: "alex@sara.ai"
    }

    val isGuest: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_GUEST] ?: true
    }

    val voicePitch: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.VOICE_PITCH] ?: 1.0f
    }

    val voiceSpeed: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.VOICE_SPEED] ?: 1.0f
    }

    suspend fun saveLoginState(isLoggedIn: Boolean, userId: String, name: String, email: String, isGuest: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = isLoggedIn
            prefs[Keys.USER_ID] = userId
            prefs[Keys.USER_NAME] = name
            prefs[Keys.USER_EMAIL] = email
            prefs[Keys.IS_GUEST] = isGuest
        }
    }

    suspend fun setPersonalityId(personalityId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_PERSONALITY_ID] = personalityId
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language
        }
    }

    suspend fun setVoiceSettings(pitch: Float, speed: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.VOICE_PITCH] = pitch
            prefs[Keys.VOICE_SPEED] = speed
        }
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = false
            prefs[Keys.IS_GUEST] = true
        }
    }
}
