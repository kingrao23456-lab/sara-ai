package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isAuthSuccess = MutableStateFlow(false)
    val isAuthSuccess: StateFlow<Boolean> = _isAuthSuccess.asStateFlow()

    fun loginWithGuest() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                authRepository.loginWithGuest()
                _isAuthSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Guest login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(email: String, name: String, password: String) {
        if (name.isBlank()) {
            _errorMessage.value = "Please enter your name"
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }
        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                authRepository.signUpWithEmail(email, name, password)
                _isAuthSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = mapAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Please enter your email address"
            return
        }
        if (password.isBlank()) {
            _errorMessage.value = "Please enter your password"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                authRepository.signInWithEmail(email, password)
                _isAuthSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = mapAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginWithGoogle(email: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                authRepository.loginWithGoogle(email, name, null)
                _isAuthSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Google sign-in isn't available yet"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun mapAuthError(e: Exception): String {
        val msg = e.message ?: return "Something went wrong. Please try again."
        return when {
            msg.contains("email address is already in use", true) ->
                "An account with this email already exists. Try signing in instead."
            msg.contains("password is invalid", true) || msg.contains("no user record", true) ||
                msg.contains("INVALID_LOGIN_CREDENTIALS", true) ->
                "Incorrect email or password."
            msg.contains("badly formatted", true) -> "Please enter a valid email address."
            msg.contains("weak password", true) -> "Password is too weak. Use at least 6 characters."
            msg.contains("network error", true) -> "Network error — check your internet connection."
            msg.contains("Firebase Auth not configured", true) -> "Sign-in isn't configured for this build."
            else -> msg
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetAuthSuccess() {
        _isAuthSuccess.value = false
    }
}
