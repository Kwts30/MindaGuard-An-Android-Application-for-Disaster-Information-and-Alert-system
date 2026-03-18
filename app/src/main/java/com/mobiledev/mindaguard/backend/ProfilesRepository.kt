package com.mobiledev.mindaguard.backend

import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Data layout (backward compatible):
 *  - /users/{uid}                      (existing user doc)
 *  - /users/{uid}/profiles/{profileId} (new: multiple profiles)
 */
class ProfilesRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(FirebaseApp.getInstance(), "default")
) {

    data class Profile(
        val id: String = "",
        val firstName: String = "",
        val lastName: String = "",
        val mobile: String = "",
        val district: String = "",
        val barangay: String = "",
        val photoUrl: String = "",
        val createdAt: Timestamp? = null,
        val updatedAt: Timestamp? = null
    ) {
        val fullName: String get() = "$firstName $lastName".trim()
        val displayName: String get() = fullName
    }

    private fun requireUid(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("Not logged in")

    private fun userDoc(uid: String): DocumentReference = db.collection("users").document(uid)

    suspend fun ensureDefaultProfile(): String {
        val uid = requireUid()
        val userRef = userDoc(uid)
        val userSnap = userRef.get().await()

        // Some accounts may exist only in Firebase Auth and not yet have a /users/{uid} doc.
        // Make sure the root doc exists so subsequent reads/updates don't fail with NOT_FOUND.
        if (!userSnap.exists()) {
            userRef.set(
                mapOf(
                    "email" to (auth.currentUser?.email ?: "")
                ),
                SetOptions.merge()
            ).await()
        }

        val activeProfileId = userSnap.getString("activeProfileId")
        if (!activeProfileId.isNullOrBlank()) return activeProfileId

        // Create a default profile from existing /users/{uid} fields (lazy migration)
        val profileRef = userRef.collection("profiles").document()

        val firstName = userSnap.getString("firstName") ?: ""
        val lastName = userSnap.getString("lastName") ?: ""
        val mobile = userSnap.getString("mobile") ?: ""
        val district = userSnap.getString("district") ?: ""
        val barangay = userSnap.getString("barangay") ?: ""
        val photoUrl = userSnap.getString("photoUrl") ?: ""

        val now = FieldValue.serverTimestamp()
        profileRef.set(
            mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "mobile" to mobile,
                "district" to district,
                "barangay" to barangay,
                "photoUrl" to photoUrl,
                "createdAt" to now,
                "updatedAt" to now
            )
        ).await()

        // Use set(merge) instead of update to avoid NOT_FOUND when the root user doc is new.
        userRef.set(
            mapOf(
                "activeProfileId" to profileRef.id,
                "defaultProfileId" to profileRef.id
            ),
            SetOptions.merge()
        ).await()

        return profileRef.id
    }

    suspend fun getActiveProfile(): Profile {
        val uid = requireUid()
        val userRef = userDoc(uid)
        val userSnap = userRef.get().await()
        val activeId = userSnap.getString("activeProfileId") ?: ensureDefaultProfile()

        val profileSnap = userRef.collection("profiles").document(activeId).get().await()
        return Profile(
            id = profileSnap.id,
            firstName = profileSnap.getString("firstName") ?: "",
            lastName = profileSnap.getString("lastName") ?: "",
            mobile = profileSnap.getString("mobile") ?: "",
            district = profileSnap.getString("district") ?: "",
            barangay = profileSnap.getString("barangay") ?: "",
            photoUrl = profileSnap.getString("photoUrl") ?: "",
            createdAt = profileSnap.getTimestamp("createdAt"),
            updatedAt = profileSnap.getTimestamp("updatedAt")
        )
    }

    suspend fun updateActiveProfile(
        firstName: String,
        lastName: String,
        mobile: String,
        district: String,
        barangay: String,
        photoUrl: String?
    ) {
        val uid = requireUid()
        val userRef = userDoc(uid)
        val userSnap = userRef.get().await()
        val activeId = userSnap.getString("activeProfileId") ?: ensureDefaultProfile()

        // If an older/incorrect activeProfileId is stored but the doc is missing,
        // fall back to creating a default profile (prevents NOT_FOUND on update).
        val profileDocRef = userRef.collection("profiles").document(activeId)
        val profileExists = try {
            profileDocRef.get().await().exists()
        } catch (_: Exception) {
            false
        }

        val safeActiveId = if (profileExists) activeId else ensureDefaultProfile()

        val updates = mutableMapOf<String, Any>(
            "firstName" to firstName.trim(),
            "lastName" to lastName.trim(),
            "mobile" to mobile.trim(),
            "district" to district,
            "barangay" to barangay,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!photoUrl.isNullOrBlank()) updates["photoUrl"] = photoUrl

        // Update active profile
        userRef.collection("profiles").document(safeActiveId)
            .set(updates, SetOptions.merge()).await()

        // Keep legacy fields updated for screens that still read /users/{uid}
        val legacyUpdates = updates.toMutableMap().apply { remove("updatedAt") }
        // Use set(merge) instead of update to avoid NOT_FOUND when the root user doc
        // doesn't exist yet (some older accounts may only exist in Auth).
        userRef.set(legacyUpdates, SetOptions.merge()).await()
    }

    suspend fun createProfile(firstName: String, lastName: String): String {
        val uid = requireUid()
        val userRef = userDoc(uid)
        val profileRef = userRef.collection("profiles").document()

        val now = FieldValue.serverTimestamp()
        profileRef.set(
            mapOf(
                "firstName" to firstName.trim(),
                "lastName" to lastName.trim(),
                "mobile" to "",
                "district" to "",
                "barangay" to "",
                "photoUrl" to "",
                "createdAt" to now,
                "updatedAt" to now
            )
        ).await()

        return profileRef.id
    }

    suspend fun setActiveProfile(profileId: String) {
        val uid = requireUid()
        userDoc(uid).update("activeProfileId", profileId).await()

        // Best-effort: also copy profile into legacy root fields for compatibility
        val userRef = userDoc(uid)
        val snap = userRef.collection("profiles").document(profileId).get().await()
        if (snap.exists()) {
            userRef.update(
                mapOf(
                    "firstName" to (snap.getString("firstName") ?: ""),
                    "lastName" to (snap.getString("lastName") ?: ""),
                    "mobile" to (snap.getString("mobile") ?: ""),
                    "district" to (snap.getString("district") ?: ""),
                    "barangay" to (snap.getString("barangay") ?: ""),
                    "photoUrl" to (snap.getString("photoUrl") ?: "")
                )
            ).await()
        }
    }
}

