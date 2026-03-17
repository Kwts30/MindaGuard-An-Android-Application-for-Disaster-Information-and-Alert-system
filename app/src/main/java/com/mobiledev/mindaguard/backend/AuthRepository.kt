package com.mobiledev.mindaguard.backend

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Repository that wraps Firebase Auth calls.
 * All methods are suspend functions — call them from a ViewModel coroutine.
 */
@Suppress("unused")
class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /** Sign in with email + password. Throws on failure. */
    @Suppress("unused")
    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    /** Register a new user with email + password. Throws on failure. */
    @Suppress("unused")
    suspend fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    /** Send a password reset email for an existing account. */
    @Suppress("unused")
    suspend fun sendPasswordResetEmail(email: String) {
        // TODO: Keep this call behind your final FirebaseAuth auth-flow decisions.
        auth.sendPasswordResetEmail(email).await()
    }

    /**
     * Change password for Email/Password accounts.
     * Firebase requires recent login: we re-authenticate using the current password.
     */
    @Suppress("unused")
    suspend fun reauthenticateAndUpdatePassword(
        email: String,
        currentPassword: String,
        newPassword: String
    ) {
        val user = auth.currentUser ?: throw IllegalStateException("Not logged in")
        if (email.isBlank()) throw IllegalArgumentException("Missing email")

        val credential = EmailAuthProvider.getCredential(email.trim(), currentPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
    }

    /** Returns the currently signed-in Firebase user, or null. */
    @Suppress("unused")
    fun currentUser(): FirebaseUser? = auth.currentUser

    /** Sign out the current user. */
    @Suppress("unused")
    fun logout() {
        auth.signOut()
    }
}

