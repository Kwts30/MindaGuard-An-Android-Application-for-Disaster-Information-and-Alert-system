package com.mobiledev.mindaguard.backend

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

/**
 * Repository that wraps all Supabase Auth calls.
 * All methods are suspend functions to be called from a ViewModel coroutine.
 */
class AuthRepository {

    private val auth = SupabaseClient.client.auth

    /**
     * Sign in with email + password.
     * Throws an exception (message surfaced to UI) on failure.
     */
    suspend fun login(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * Register a new user with email + password.
     * Throws an exception on failure (e.g. email already taken, weak password).
     */
    suspend fun register(email: String, password: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /** Returns the currently signed-in user, or null if not logged in. */
    fun currentUser() = auth.currentUserOrNull()

    /** Sign out the current user. */
    suspend fun logout() {
        auth.signOut()
    }
}

