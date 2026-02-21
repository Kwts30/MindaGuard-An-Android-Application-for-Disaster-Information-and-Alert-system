package com.mobiledev.mindaguard.backend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String,
        mobile: String,
        barangay: String,
        district: String
    ) {
        when {
            firstName.isBlank() || lastName.isBlank() || mobile.isBlank() ||
                    email.isBlank() || password.isBlank() || confirmPassword.isBlank() ||
                    barangay.isBlank() || district.isBlank() -> {
                _uiState.value = RegisterUiState.Error("Please fill in all fields")
                return
            }
            password != confirmPassword -> {
                _uiState.value = RegisterUiState.Error("Passwords do not match")
                return
            }
            password.length < 6 -> {
                _uiState.value = RegisterUiState.Error("Password must be at least 6 characters")
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> {
                _uiState.value = RegisterUiState.Error("Please enter a valid email address")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                repository.register(email.trim(), password)
                _uiState.value = RegisterUiState.Success
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error(
                    e.message?.let { parseSupabaseError(it) } ?: "Registration failed. Please try again."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }

    private fun parseSupabaseError(raw: String): String = when {
        raw.contains("already registered", ignoreCase = true) ||
                raw.contains("already been registered", ignoreCase = true) ->
            "This email is already registered. Please log in."
        raw.contains("password", ignoreCase = true) && raw.contains("short", ignoreCase = true) ->
            "Password is too short. Use at least 6 characters."
        raw.contains("network", ignoreCase = true) ||
                raw.contains("Unable to resolve host", ignoreCase = true) ->
            "No internet connection. Please check your network."
        else -> "Registration failed: $raw"
    }
}

