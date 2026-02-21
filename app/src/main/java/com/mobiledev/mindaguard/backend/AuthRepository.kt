package com.mobiledev.mindaguard.backend

import com.google.firebase.auth.FirebaseAuth
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

    /** Returns the currently signed-in Firebase user, or null. */
    @Suppress("unused")
    fun currentUser(): FirebaseUser? = auth.currentUser

    /** Sign out the current user. */
    @Suppress("unused")
    fun logout() {
        auth.signOut()
    }
}
