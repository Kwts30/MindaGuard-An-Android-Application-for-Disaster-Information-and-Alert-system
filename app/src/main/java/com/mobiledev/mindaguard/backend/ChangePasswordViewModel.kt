package com.mobiledev.mindaguard.backend

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ChangePasswordUiState {
    object Idle : ChangePasswordUiState()
    object Loading : ChangePasswordUiState()
    data class Success(val message: String) : ChangePasswordUiState()
    data class Error(val message: String) : ChangePasswordUiState()
}

@Suppress("unused")
class ChangePasswordViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChangePasswordUiState>(ChangePasswordUiState.Idle)
    val uiState: StateFlow<ChangePasswordUiState> = _uiState

    @Suppress("unused")
    fun reset() {
        _uiState.value = ChangePasswordUiState.Idle
    }

    @Suppress("unused")
    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.orEmpty()

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = ChangePasswordUiState.Error("No email found for this account.")
            return
        }
        if (currentPassword.isBlank()) {
            _uiState.value = ChangePasswordUiState.Error("Enter your current password")
            return
        }
        if (newPassword.length < 6) {
            _uiState.value = ChangePasswordUiState.Error("New password must be at least 6 characters")
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.value = ChangePasswordUiState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = ChangePasswordUiState.Loading
            try {
                // TODO: If you add other login providers (Google, etc.), handle them here.
                repository.reauthenticateAndUpdatePassword(
                    email = email,
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
                _uiState.value = ChangePasswordUiState.Success("Password updated successfully")
            } catch (_: FirebaseAuthRecentLoginRequiredException) {
                // Should be rare because we re-auth above, but keep a friendly message.
                _uiState.value = ChangePasswordUiState.Error("Please log in again and try.")
            } catch (e: Exception) {
                val msg = e.message.orEmpty()
                _uiState.value = ChangePasswordUiState.Error(
                    if (msg.contains("network", ignoreCase = true) || msg.contains("timeout", ignoreCase = true)) {
                        "Poor connection. Please try again."
                    } else msg.ifBlank { "Unable to change password. Please try again." }
                )
            }
        }
    }
}


