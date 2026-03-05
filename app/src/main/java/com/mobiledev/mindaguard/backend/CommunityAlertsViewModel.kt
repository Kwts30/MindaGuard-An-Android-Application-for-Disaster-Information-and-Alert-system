package com.mobiledev.mindaguard.backend

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.mobiledev.mindaguard.ui.screens.CommunityReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// ── Data model ────────────────────────────────────────────────────────────────

data class AlertFeedItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val location: String,
    val description: String,
    val timeLabel: String = "Just now",
    val latitude: Double = 7.0644,
    val longitude: Double = 125.6079,
    val submittedBy: String = "",
    val submittedByName: String = "",
    val hazardType: String = "",
    val isUserSubmitted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false,
    val verifiedBy: String = "",
    val status: String = "unverified"   // "unverified" | "verified" | "rejected"
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class CommunityAlertsViewModel : ViewModel() {

    // Connect explicitly to the "default" named database (not the SDK default "(default)").
    // Our Firestore database was created with ID "default" (no parentheses).
    private val db   = FirebaseFirestore.getInstance(FirebaseApp.getInstance(), "default")
    private val auth = FirebaseAuth.getInstance()
    private val col  = db.collection("community_alerts")

    val alerts    = mutableStateListOf<AlertFeedItem>()
    val myReports = mutableStateListOf<AlertFeedItem>()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _submitError = MutableStateFlow<String?>(null)
    val submitError: StateFlow<String?> = _submitError

    /** Emits true briefly after a successful submit so the UI can navigate + toast */
    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    /** Exposes any Firestore listener error so the UI can show it (e.g. PERMISSION_DENIED) */
    private val _listenerError = MutableStateFlow<String?>(null)
    val listenerError: StateFlow<String?> = _listenerError

    private val _verifyError = MutableStateFlow<String?>(null)
    val verifyError: StateFlow<String?> = _verifyError

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError

    private var listenerReg: ListenerRegistration? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    init {
        // Start listening immediately so guests (unauthenticated) also see alerts.
        // The Firestore rules now allow public reads on community_alerts.
        startListening()

        // React to every sign-in / sign-out / account-switch automatically.
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // A user is now signed in — restart the feed listener with their UID context
                // and check whether this account has admin privileges.
                startListening()
                checkAdminRole()
            } else {
                // Signed out — restart listener as guest (public read), clear user-specific data.
                startListening()
                myReports.clear()
                _isAdmin.value = false
            }
        }
        authStateListener = listener
        auth.addAuthStateListener(listener)
    }

    // ── Check admin role via Auth ID-token custom claim ───────────────────────
    // Assign the claim with Firebase Admin SDK:
    //   admin.auth().setCustomUserClaims(uid, { role: 'admin' })

    private fun checkAdminRole() {
        val user = auth.currentUser ?: run { _isAdmin.value = false; return }
        viewModelScope.launch {
            try {
                // forceRefresh = true ensures we always get the latest token claims
                val result = user.getIdToken(true).await()
                _isAdmin.value = result.claims["role"] == "admin"
            } catch (_: Exception) {
                _isAdmin.value = false
            }
        }
    }

    // ── Real-time listener — ALL authenticated users see ALL alerts ───────────

    private fun stopListening() {
        listenerReg?.remove()
        listenerReg = null
    }

    private fun startListening() {
        // Remove any existing listener before attaching a new one
        stopListening()
        _isRefreshing.value = true

        listenerReg = col
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                _isRefreshing.value = false

                if (error != null) {
                    Log.e("CommunityAlerts", "Firestore listener error: ${error.message}", error)
                    _listenerError.value = error.message ?: "Unknown Firestore error"
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                // Success — clear any previous error
                _listenerError.value = null
                Log.d("CommunityAlerts", "Received ${snapshot.documents.size} alerts from Firestore")

                val currentUid = auth.currentUser?.uid ?: ""
                val incoming = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val verified = data["isVerified"] as? Boolean ?: false
                    AlertFeedItem(
                        id              = doc.id,
                        title           = (data["title"] as? String ?: "").uppercase(),
                        location        = data["location"] as? String ?: "",
                        description     = data["description"] as? String ?: "",
                        latitude        = (data["latitude"] as? Number)?.toDouble() ?: 7.0644,
                        longitude       = (data["longitude"] as? Number)?.toDouble() ?: 125.6079,
                        submittedBy     = data["submittedBy"] as? String ?: "",
                        submittedByName = data["submittedByName"] as? String ?: "Anonymous",
                        hazardType      = data["hazardType"] as? String ?: "",
                        isUserSubmitted = data["submittedBy"] == currentUid,
                        timestamp       = (data["timestamp"] as? Timestamp)
                                              ?.toDate()?.time ?: System.currentTimeMillis(),
                        timeLabel       = formatTimestamp(
                            (data["timestamp"] as? Timestamp)?.toDate()
                        ),
                        isVerified      = verified,
                        verifiedBy      = data["verifiedBy"] as? String ?: "",
                        status          = data["status"] as? String
                                              ?: if (verified) "verified" else "unverified"
                    )
                }

                alerts.clear()
                alerts.addAll(incoming)

                myReports.clear()
                myReports.addAll(incoming.filter { it.submittedBy == currentUid })
            }
    }

    /** Manually re-attach the Firestore listener to force a fresh load.
     *  Works for both authenticated users and guests. */
    fun refresh() {
        startListening()
    }

    // ── Submit new alert ──────────────────────────────────────────────────────

    fun addReport(report: CommunityReport) {
        val user = auth.currentUser
        if (user == null) {
            Log.e("CommunityAlerts", "addReport: currentUser is NULL — cannot submit")
            _submitError.value = "You must be signed in to submit an alert."
            return
        }
        val uid  = user.uid
        val displayName = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.takeIf { it.isNotBlank() }
            ?: "Anonymous"
        Log.d("CommunityAlerts", "addReport: uid=$uid, displayName=$displayName, title=${report.title}")
        viewModelScope.launch {
            _isSubmitting.value  = true
            _submitError.value   = null
            _submitSuccess.value = false
            try {
                val docData = hashMapOf(
                    "title"           to report.title.uppercase(),
                    "location"        to report.locationLabel.ifBlank { "Davao City" },
                    "description"     to report.description,
                    "latitude"        to report.latitude,
                    "longitude"       to report.longitude,
                    "submittedBy"     to uid,
                    "submittedByName" to displayName,
                    "hazardType"      to report.hazardType.name,
                    "timestamp"       to Timestamp.now(),
                    "isVerified"      to false,
                    "verifiedBy"      to "",
                    "status"          to "unverified"
                )
                Log.d("CommunityAlerts", "addReport: writing to Firestore...")
                val docRef = col.add(docData).await()
                Log.d("CommunityAlerts", "addReport: SUCCESS — doc id = ${docRef.id}")
                _submitSuccess.value = true
            } catch (e: Exception) {
                Log.e("CommunityAlerts", "addReport: FAILED — ${e.message}", e)
                _submitError.value = e.message ?: "Failed to submit alert."
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun clearSubmitSuccess() { _submitSuccess.value = false }

    /** Call this immediately before FirebaseAuth.signOut() to eagerly clear user-specific state.
     *  The listener is restarted as a guest listener so alerts remain visible after logout. */
    fun resetForLogout() {
        myReports.clear()
        _isAdmin.value       = false
        _isRefreshing.value  = false
        _listenerError.value = null
        _submitError.value   = null
        _submitSuccess.value = false
        _verifyError.value   = null
        _deleteError.value   = null
        // Restart as guest — community alerts are publicly readable
        startListening()
    }

    // ── Admin: verify / unverify ──────────────────────────────────────────────

    fun verifyAlert(alertId: String, verify: Boolean) {
        if (!_isAdmin.value) return
        val user = auth.currentUser ?: return
        val adminName = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.takeIf { it.isNotBlank() }
            ?: "Admin"
        viewModelScope.launch {
            _verifyError.value = null
            try {
                col.document(alertId).update(
                    mapOf(
                        "isVerified" to verify,
                        "verifiedBy" to if (verify) adminName else "",
                        "status"     to if (verify) "verified" else "unverified"
                    )
                ).await()
            } catch (e: Exception) {
                _verifyError.value = e.message ?: "Failed to update verification."
            }
        }
    }

    fun clearSubmitError() { _submitError.value = null }
    fun clearVerifyError() { _verifyError.value = null }
    fun clearDeleteError() { _deleteError.value = null }

    // ── Admin: delete (remove misinformation) ─────────────────────────────────

    fun deleteAlert(alertId: String) {
        if (!_isAdmin.value) return
        viewModelScope.launch {
            _deleteError.value = null
            try {
                col.document(alertId).delete().await()
            } catch (e: Exception) {
                _deleteError.value = e.message ?: "Failed to delete alert."
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
        authStateListener?.let { auth.removeAuthStateListener(it) }
        authStateListener = null
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun formatTimestamp(date: Date?): String {
        date ?: return "Just now"
        val diffMs  = System.currentTimeMillis() - date.time
        val minutes = diffMs / 60_000
        val hours   = diffMs / 3_600_000
        val days    = diffMs / 86_400_000
        return when {
            minutes < 1  -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours   < 24 -> "${hours}h ago"
            days    < 7  -> "${days}d ago"
            else         -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
        }
    }
}
