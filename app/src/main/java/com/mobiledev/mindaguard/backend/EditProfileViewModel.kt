package com.mobiledev.mindaguard.backend

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageException
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
    private val profilesRepository = ProfilesRepository(auth = auth, db = db)

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
                // Prefer active profile photo
                profilesRepository.ensureDefaultProfile()
                val profile = profilesRepository.getActiveProfile()
                _photoUrl.value = profile.photoUrl
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
                // Ensure an active profile exists (lazy migration from legacy /users/{uid})
                val activeProfileId = profilesRepository.ensureDefaultProfile()

                // Upload photo to a per-profile path
                val uploadedUrl: String? = if (pickedImageUri != null) {
                    try {
                        val ref = storage.reference
                            .child("profile_pictures/${user.uid}/${activeProfileId}.jpg")
                        ref.putFile(pickedImageUri).await()
                        ref.downloadUrl.await().toString()
                    } catch (e: Exception) {
                        val msg = when (e) {
                            is StorageException -> e.message ?: "Storage upload failed"
                            else -> e.message ?: "Storage upload failed"
                        }
                        throw IllegalStateException("Photo upload failed: $msg", e)
                    }
                } else null

                profilesRepository.updateActiveProfile(
                    firstName = firstName,
                    lastName = lastName,
                    mobile = mobile,
                    district = district,
                    barangay = barangay,
                    photoUrl = uploadedUrl
                )

                if (!uploadedUrl.isNullOrBlank()) {
                    _photoUrl.value = uploadedUrl
                }


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

