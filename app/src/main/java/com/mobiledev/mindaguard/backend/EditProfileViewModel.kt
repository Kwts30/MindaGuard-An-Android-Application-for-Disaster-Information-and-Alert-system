package com.mobiledev.mindaguard.backend

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class EditProfileUiState {
    object Idle : EditProfileUiState()
    object Loading : EditProfileUiState()
    object Success : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
}

@Suppress("unused")
class EditProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance(FirebaseApp.getInstance(), "default")
    private val storage = FirebaseStorage.getInstance()

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Idle)
    @Suppress("unused")
    val uiState: StateFlow<EditProfileUiState> = _uiState

    private val _photoUrl = MutableStateFlow<String?>(null)
    @Suppress("unused")
    val photoUrl: StateFlow<String?> = _photoUrl

    @Suppress("unused")
    fun loadCurrentPhoto() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").document(uid).get().await()
                _photoUrl.value = snapshot.getString("photoUrl")
            } catch (_: Exception) { }
        }
    }

    @Suppress("unused")
    fun saveProfile(
        firstName: String,
        lastName: String,
        mobile: String,
        district: String,
        barangay: String,
        pickedImageUri: Uri?,
        onDone: () -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = EditProfileUiState.Error("Not logged in")
            return
        }

        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            try {
                // Upload profile picture if a new one was picked
                val uploadedUrl: String? = if (pickedImageUri != null) {
                    val ref = storage.reference
                        .child("profile_pictures/${user.uid}.jpg")
                    ref.putFile(pickedImageUri).await()
                    ref.downloadUrl.await().toString()
                } else null

                // Build update map
                val updates = mutableMapOf<String, Any>(
                    "firstName" to firstName.trim(),
                    "lastName"  to lastName.trim(),
                    "mobile"    to mobile.trim(),
                    "district"  to district,
                    "barangay"  to barangay
                )
                if (uploadedUrl != null) {
                    updates["photoUrl"] = uploadedUrl
                    _photoUrl.value = uploadedUrl
                }

                db.collection("users").document(user.uid)
                    .update(updates)
                    .await()

                _uiState.value = EditProfileUiState.Success
                onDone()
            } catch (e: Exception) {
                _uiState.value = EditProfileUiState.Error(e.message ?: "Failed to save profile")
            }
        }
    }

    @Suppress("unused")
    fun resetState() {
        _uiState.value = EditProfileUiState.Idle
    }
}

