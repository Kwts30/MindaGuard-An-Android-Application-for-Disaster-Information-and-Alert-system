package com.mobiledev.mindaguard.backend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@Suppress("unused")
class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Please enter your email and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                repository.login(email.trim(), password)
                onSuccess()
                _uiState.value = LoginUiState.Success
            } catch (_: FirebaseAuthInvalidUserException) {
                _uiState.value = LoginUiState.Error("No account found with this email.")
            } catch (_: FirebaseAuthInvalidCredentialsException) {
                _uiState.value = LoginUiState.Error("Incorrect email or password.")
            } catch (e: Exception) {
                val msg = e.message ?: ""
                val isNetwork = msg.contains("network", ignoreCase = true) ||
                        msg.contains("timeout", ignoreCase = true) ||
                        msg.contains("timed out", ignoreCase = true) ||
                        msg.contains("connect", ignoreCase = true) ||
                        msg.contains("resolve", ignoreCase = true) ||
                        msg.contains("socket", ignoreCase = true)
                _uiState.value = LoginUiState.Error(
                    if (isNetwork) "Poor connection. Tap to try again."
                    else msg.ifBlank { "Login failed. Please try again." }
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
