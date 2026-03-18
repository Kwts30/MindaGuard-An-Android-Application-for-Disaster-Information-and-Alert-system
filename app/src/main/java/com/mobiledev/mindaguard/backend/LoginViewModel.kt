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

sealed class ForgotPasswordUiState {
    object Idle : ForgotPasswordUiState()
    object Loading : ForgotPasswordUiState()
    data class Success(val message: String) : ForgotPasswordUiState()
    data class Error(val message: String) : ForgotPasswordUiState()
}

@Suppress("unused")
class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _forgotPasswordUiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val forgotPasswordUiState: StateFlow<ForgotPasswordUiState> = _forgotPasswordUiState

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
                _uiState.value = LoginUiState.Error(
                    if (isNetworkError(msg)) "Poor connection. Tap to try again."
                    else msg.ifBlank { "Login failed. Please try again." }
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    fun sendPasswordReset(email: String) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _forgotPasswordUiState.value = ForgotPasswordUiState.Error("Enter a valid email address")
            return
        }

        viewModelScope.launch {
            _forgotPasswordUiState.value = ForgotPasswordUiState.Loading
            try {
                // TODO: Call FirebaseAuth.sendPasswordResetEmail(email) in final auth wiring.
                repository.sendPasswordResetEmail(email.trim())
                _forgotPasswordUiState.value = ForgotPasswordUiState.Success(
                    "Password reset link sent. Check your email."
                )
            } catch (e: Exception) {
                val msg = e.message ?: ""
                _forgotPasswordUiState.value = ForgotPasswordUiState.Error(
                    if (isNetworkError(msg)) "Poor connection. Tap to try again."
                    else msg.ifBlank { "Unable to send reset link. Please try again." }
                )
            }
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordUiState.value = ForgotPasswordUiState.Idle
    }
}
