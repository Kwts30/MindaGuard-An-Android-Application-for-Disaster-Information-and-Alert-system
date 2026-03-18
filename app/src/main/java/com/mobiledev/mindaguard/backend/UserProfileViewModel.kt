package com.mobiledev.mindaguard.backend

import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val mobile: String = "",
    val district: String = "",
    val barangay: String = "",
    val photoUrl: String = ""
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
    private val db = FirebaseFirestore.getInstance(FirebaseApp.getInstance(), "default")

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private var listenerReg: ListenerRegistration? = null

    init {
        loadProfile()
    }

    fun loadProfile() {
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = ProfileUiState.Error("Not logged in")
            return
        }

        // Cancel any existing listener
        listenerReg?.remove()

        _uiState.value = ProfileUiState.Loading

        // Real-time listener – updates UI whenever Firestore data changes
        listenerReg = db.collection("users").document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = ProfileUiState.Error(error.message ?: "Failed to load profile")
                    return@addSnapshotListener
                }

                // Fall back to Firebase Auth displayName if Firestore fields are missing
                val authName = user.displayName ?: ""
                val authParts = authName.trim().split(" ", limit = 2)
                val fallbackFirst = authParts.getOrElse(0) { "" }
                val fallbackLast  = authParts.getOrElse(1) { "" }

                val profile = UserProfile(
                    firstName = snapshot?.getString("firstName")?.takeIf { it.isNotBlank() } ?: fallbackFirst,
                    lastName  = snapshot?.getString("lastName")?.takeIf { it.isNotBlank() }  ?: fallbackLast,
                    email     = snapshot?.getString("email")     ?: user.email ?: "",
                    mobile    = snapshot?.getString("mobile")    ?: "",
                    district  = snapshot?.getString("district")  ?: "",
                    barangay  = snapshot?.getString("barangay")  ?: "",
                    photoUrl  = snapshot?.getString("photoUrl")  ?: ""
                )
                _uiState.value = ProfileUiState.Success(profile)
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerReg?.remove()
    }
}
