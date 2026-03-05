package com.mobiledev.mindaguard.backend

/**
 * Shared utility to detect network-related error messages.
 * Used by LoginViewModel, RegisterViewModel, and their screens.
 */
fun isNetworkError(message: String): Boolean {
    val keywords = listOf("network", "timeout", "timed out", "connect", "resolve", "socket", "failed", "poor")
    val lower = message.lowercase()
    return keywords.any { lower.contains(it) }
}

