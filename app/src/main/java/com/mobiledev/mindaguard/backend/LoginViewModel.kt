package com.mobiledev.mindaguard.backend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Please enter your email and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                repository.login(email.trim(), password)
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(
                    e.message?.let { parseSupabaseError(it) } ?: "Login failed. Please try again."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    private fun parseSupabaseError(raw: String): String = when {
        raw.contains("Invalid login credentials", ignoreCase = true) ->
            "Incorrect email or password."
        raw.contains("Email not confirmed", ignoreCase = true) ->
            "Please verify your email first."
        raw.contains("network", ignoreCase = true) ||
                raw.contains("Unable to resolve host", ignoreCase = true) ->
            "No internet connection. Please check your network."
        else -> "Login failed: $raw"
    }
}

