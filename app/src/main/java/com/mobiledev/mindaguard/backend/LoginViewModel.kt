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
    @Suppress("unused")
    val uiState: StateFlow<LoginUiState> = _uiState

    @Suppress("unused")
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
            } catch (_: FirebaseAuthInvalidUserException) {
                _uiState.value = LoginUiState.Error("No account found with this email.")
            } catch (_: FirebaseAuthInvalidCredentialsException) {
                _uiState.value = LoginUiState.Error("Incorrect email or password.")
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(
                    e.message ?: "Login failed. Please try again."
                )
            }
        }
    }

    @Suppress("unused")
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
