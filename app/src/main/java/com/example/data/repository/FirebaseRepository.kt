package com.example.data.repository

import android.util.Log
import com.example.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    companion object {
        private const val TAG = "FirebaseRepository"
        private const val USERS_COLLECTION = "users"
        private const val MEMORY_COLLECTION = "user_memories"
        private const val CHATS_COLLECTION = "chat_sessions"
        private const val AUTOMATIONS_COLLECTION = "automations"
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth initialization notice: ${e.message}")
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore initialization notice: ${e.message}")
            null
        }
    }

    val currentUser: FirebaseUser?
        get() = try { auth?.currentUser } catch (e: Exception) { null }

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val firebaseAuth = auth
        if (firebaseAuth != null) {
            val listener = FirebaseAuth.AuthStateListener { fa ->
                trySend(fa.currentUser)
            }
            try {
                firebaseAuth.addAuthStateListener(listener)
            } catch (_: Exception) {}
            awaitClose {
                try { firebaseAuth.removeAuthStateListener(listener) } catch (_: Exception) {}
            }
        } else {
            trySend(null)
            awaitClose {}
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseUser?> {
        val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth not configured"))
        return try {
            val result = firebaseAuth.signInAnonymously().await()
            Log.d(TAG, "Anonymous sign-in successful: ${result.user?.uid}")
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e(TAG, "Anonymous sign-in notice", e)
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<FirebaseUser?> {
        val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth not configured"))
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                val profile = UserProfile(
                    userId = user.uid,
                    name = name.ifBlank { email.substringBefore("@") },
                    email = email,
                    isGuest = false,
                    isCloudSyncEnabled = true
                )
                saveUserProfileToFirestore(profile)
            }
            Log.d(TAG, "Sign-up successful: ${user?.uid}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-up failed", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser?> {
        val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth not configured"))
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Sign-in successful: ${authResult.user?.uid}")
            Result.success(authResult.user)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in failed", e)
            Result.failure(e)
        }
    }

    suspend fun saveUserProfileToFirestore(userProfile: UserProfile): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore not configured"))
        return try {
            val uid = auth?.currentUser?.uid ?: userProfile.userId
            val userMap = hashMapOf(
                "userId" to uid,
                "name" to userProfile.name,
                "email" to userProfile.email,
                "photoUrl" to (userProfile.photoUrl ?: ""),
                "selectedPersonalityId" to userProfile.selectedPersonalityId,
                "language" to userProfile.language,
                "theme" to userProfile.theme,
                "isGuest" to userProfile.isGuest,
                "voiceSpeed" to userProfile.voiceSpeed,
                "voicePitch" to userProfile.voicePitch,
                "isCloudSyncEnabled" to userProfile.isCloudSyncEnabled,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(USERS_COLLECTION)
                .document(uid)
                .set(userMap, SetOptions.merge())
                .await()
            Log.d(TAG, "User profile saved to Firestore for $uid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save profile to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun getUserProfileFromFirestore(userId: String): UserProfile? {
        val db = firestore ?: return null
        return try {
            val snapshot = db.collection(USERS_COLLECTION).document(userId).get().await()
            if (snapshot.exists()) {
                UserProfile(
                    userId = snapshot.getString("userId") ?: userId,
                    name = snapshot.getString("name") ?: "Alex",
                    email = snapshot.getString("email") ?: "alex@sara.ai",
                    photoUrl = snapshot.getString("photoUrl"),
                    selectedPersonalityId = snapshot.getString("selectedPersonalityId") ?: "zoya",
                    language = snapshot.getString("language") ?: "English",
                    theme = snapshot.getString("theme") ?: "AMOLED Black",
                    isGuest = snapshot.getBoolean("isGuest") ?: false,
                    voiceSpeed = (snapshot.getDouble("voiceSpeed") ?: 1.0).toFloat(),
                    voicePitch = (snapshot.getDouble("voicePitch") ?: 1.0).toFloat(),
                    isCloudSyncEnabled = snapshot.getBoolean("isCloudSyncEnabled") ?: true
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch user profile from Firestore", e)
            null
        }
    }

    suspend fun syncMemoryToFirestore(userId: String, memoryKey: String, memoryValue: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore not configured"))
        return try {
            val data = hashMapOf(
                "key" to memoryKey,
                "value" to memoryValue,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(MEMORY_COLLECTION)
                .document(memoryKey)
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync memory to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun syncChatSessionToFirestore(userId: String, sessionId: String, messages: List<Map<String, Any>>): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore not configured"))
        return try {
            val data = hashMapOf(
                "sessionId" to sessionId,
                "messages" to messages,
                "lastUpdated" to System.currentTimeMillis()
            )
            db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(CHATS_COLLECTION)
                .document(sessionId)
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync chat session to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun syncAutomationToFirestore(userId: String, taskId: String, taskData: Map<String, Any>): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore not configured"))
        return try {
            db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(AUTOMATIONS_COLLECTION)
                .document(taskId)
                .set(taskData, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync automation task to Firestore", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out", e)
        }
    }
}
