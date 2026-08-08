package com.example.data.repository

import com.example.core.database.dao.UserDao
import com.example.core.database.entity.UserProfileEntity
import com.example.core.datastore.UserPreferencesRepository
import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val userDao: UserDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) {

    val currentUserProfile: Flow<UserProfile> = userDao.getUserProfile().map { entity ->
        if (entity != null) {
            UserProfile(
                userId = entity.userId,
                name = entity.name,
                email = entity.email,
                photoUrl = entity.photoUrl,
                selectedPersonalityId = entity.selectedPersonalityId,
                language = entity.language,
                theme = entity.theme,
                isGuest = entity.isGuest,
                voiceSpeed = entity.voiceSpeed,
                voicePitch = entity.voicePitch,
                isCloudSyncEnabled = entity.isCloudSyncEnabled
            )
        } else {
            UserProfile()
        }
    }

    val isLoggedIn: Flow<Boolean> = userPreferencesRepository.isLoggedIn

    suspend fun loginWithGuest() {
        var firebaseUid = "guest_" + System.currentTimeMillis()
        try {
            val result = firebaseRepository.signInAnonymously()
            val fbUser = result.getOrNull()
            if (fbUser != null) {
                firebaseUid = fbUser.uid
            }
        } catch (_: Exception) {}

        val userEntity = UserProfileEntity(
            userId = firebaseUid,
            name = "Guest User",
            email = "guest@sara.ai",
            photoUrl = null,
            selectedPersonalityId = "zoya",
            language = "English",
            theme = "AMOLED Black",
            isGuest = true,
            voiceSpeed = 1.0f,
            voicePitch = 1.0f,
            isCloudSyncEnabled = false
        )
        userDao.insertOrUpdateUser(userEntity)
        userPreferencesRepository.saveLoginState(
            isLoggedIn = true,
            userId = userEntity.userId,
            name = userEntity.name,
            email = userEntity.email,
            isGuest = true
        )
    }

    suspend fun signUpWithEmail(email: String, name: String, password: String) {
        val result = firebaseRepository.signUpWithEmail(email, password, name)
        val fbUser = result.getOrThrow()
            ?: throw IllegalStateException("Sign up failed: no account was created")

        val displayName = name.ifBlank { if (email.contains("@")) email.substringBefore("@") else "Alex" }

        val userEntity = UserProfileEntity(
            userId = fbUser.uid,
            name = displayName,
            email = email,
            photoUrl = null,
            selectedPersonalityId = "zoya",
            language = "English",
            theme = "AMOLED Black",
            isGuest = false,
            voiceSpeed = 1.0f,
            voicePitch = 1.0f,
            isCloudSyncEnabled = true
        )
        userDao.insertOrUpdateUser(userEntity)
        userPreferencesRepository.saveLoginState(
            isLoggedIn = true,
            userId = fbUser.uid,
            name = displayName,
            email = email,
            isGuest = false
        )
    }

    suspend fun signInWithEmail(email: String, password: String) {
        val result = firebaseRepository.signInWithEmail(email, password)
        val fbUser = result.getOrThrow()
            ?: throw IllegalStateException("Sign in failed: no account found")

        // Prefer the profile already stored in the cloud; if that read fails or is
        // blank, fall back to whatever is already saved locally for this account
        // (e.g. right after sign-up) before ever deriving a name from the email.
        val cloudProfile = try { firebaseRepository.getUserProfileFromFirestore(fbUser.uid) } catch (e: Exception) { null }
        val localProfile = try { userDao.getUserProfileOnce() } catch (e: Exception) { null }
        val displayName = cloudProfile?.name?.ifBlank { null }
            ?: localProfile?.takeIf { it.userId == fbUser.uid }?.name?.ifBlank { null }
            ?: (fbUser.email ?: email).substringBefore("@")

        val userEntity = UserProfileEntity(
            userId = fbUser.uid,
            name = displayName,
            email = email,
            photoUrl = cloudProfile?.photoUrl,
            selectedPersonalityId = cloudProfile?.selectedPersonalityId ?: "zoya",
            language = cloudProfile?.language ?: "English",
            theme = cloudProfile?.theme ?: "AMOLED Black",
            isGuest = false,
            voiceSpeed = cloudProfile?.voiceSpeed ?: 1.0f,
            voicePitch = cloudProfile?.voicePitch ?: 1.0f,
            isCloudSyncEnabled = true
        )
        userDao.insertOrUpdateUser(userEntity)
        userPreferencesRepository.saveLoginState(
            isLoggedIn = true,
            userId = fbUser.uid,
            name = displayName,
            email = email,
            isGuest = false
        )
    }

    /**
     * NOTE: Real "Sign in with Google" requires the Google Identity/Credential Manager
     * flow plus your app's SHA-1 fingerprint registered in the Firebase console. That's
     * a separate setup step (and needs a *stable* signing key, not one regenerated on
     * every CI build) — see FIXES.md for details. Calling this before that's done will
     * always fail with a clear error rather than silently pretending to succeed.
     */
    suspend fun loginWithGoogle(email: String, name: String, photoUrl: String?) {
        throw UnsupportedOperationException(
            "Google Sign-In isn't fully set up yet (needs a stable app signing key + SHA-1 registered in Firebase). Use email sign up/sign in or Continue as Guest for now."
        )
    }

    suspend fun updateUserProfile(updatedProfile: UserProfile) {
        val userEntity = UserProfileEntity(
            userId = updatedProfile.userId,
            name = updatedProfile.name,
            email = updatedProfile.email,
            photoUrl = updatedProfile.photoUrl,
            selectedPersonalityId = updatedProfile.selectedPersonalityId,
            language = updatedProfile.language,
            theme = updatedProfile.theme,
            isGuest = updatedProfile.isGuest,
            voiceSpeed = updatedProfile.voiceSpeed,
            voicePitch = updatedProfile.voicePitch,
            isCloudSyncEnabled = updatedProfile.isCloudSyncEnabled
        )
        userDao.insertOrUpdateUser(userEntity)
        userPreferencesRepository.setLanguage(updatedProfile.language)
        userPreferencesRepository.setVoiceSettings(updatedProfile.voicePitch, updatedProfile.voiceSpeed)

        if (updatedProfile.isCloudSyncEnabled) {
            try {
                firebaseRepository.saveUserProfileToFirestore(updatedProfile)
            } catch (_: Exception) {}
        }
    }

    suspend fun logout() {
        try {
            firebaseRepository.signOut()
        } catch (_: Exception) {}
        userPreferencesRepository.logout()
    }
}
