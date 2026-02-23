package com.mobiledev.mindaguard.backend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val mobile: String = "",
    val district: String = "",
    val barangay: String = ""
) {
    val fullName: String get() = "$firstName $lastName".trim()
    val displayName: String get() = if (fullName.isNotBlank()) fullName else email
}

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class UserProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = ProfileUiState.Error("Not logged in")
            return
        }

        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                // Try server first, fall back to cache if offline
                val doc = try {
                    db.collection("users").document(user.uid).get(Source.SERVER).await()
                } catch (_: Exception) {
                    db.collection("users").document(user.uid).get(Source.CACHE).await()
                }
                val profile = UserProfile(
                    firstName  = doc.getString("firstName")  ?: "",
                    lastName   = doc.getString("lastName")   ?: "",
                    email      = doc.getString("email")      ?: user.email ?: "",
                    mobile     = doc.getString("mobile")     ?: "",
                    district   = doc.getString("district")   ?: "",
                    barangay   = doc.getString("barangay")   ?: ""
                )
                _uiState.value = ProfileUiState.Success(profile)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }
}

