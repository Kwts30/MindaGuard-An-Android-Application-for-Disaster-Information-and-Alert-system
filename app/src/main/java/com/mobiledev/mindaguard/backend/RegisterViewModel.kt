package com.mobiledev.mindaguard.backend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
        district: String,
        onSuccess: () -> Unit
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

                // Navigate immediately — account is created
                onSuccess()

                // Save profile in background (best-effort)
                val currentUser = FirebaseAuth.getInstance().currentUser
                val uid = currentUser?.uid
                if (uid != null) {
                    val fullName = "${firstName.trim()} ${lastName.trim()}".trim()
                    try {
                        currentUser.updateProfile(
                            UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
                                .build()
                        ).await()
                    } catch (_: Exception) {}
                    try {
                        FirebaseFirestore.getInstance(FirebaseApp.getInstance(), "default")
                            .collection("users").document(uid)
                            .set(mapOf(
                                "firstName" to firstName.trim(),
                                "lastName"  to lastName.trim(),
                                "email"     to email.trim(),
                                "mobile"    to mobile.trim(),
                                "district"  to district,
                                "barangay"  to barangay,
                                // Ensure profile editing has a consistent starting point.
                                // The actual profile doc will be created lazily by ProfilesRepository.
                                "activeProfileId" to "",
                                "defaultProfileId" to ""
                            )).await()
                    } catch (_: Exception) {}
                }

                _uiState.value = RegisterUiState.Success

            } catch (_: FirebaseAuthUserCollisionException) {
                _uiState.value = RegisterUiState.Error("This email is already registered. Please log in.")
            } catch (_: FirebaseAuthWeakPasswordException) {
                _uiState.value = RegisterUiState.Error("Password is too weak. Use at least 6 characters.")
            } catch (e: Exception) {
                val msg = e.message ?: ""
                _uiState.value = RegisterUiState.Error(
                    if (isNetworkError(msg)) "Poor connection. Tap to try again."
                    else msg.ifBlank { "Registration failed. Please try again." }
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}
