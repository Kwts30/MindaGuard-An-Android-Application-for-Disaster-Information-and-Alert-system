package com.mobiledev.mindaguard.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobiledev.mindaguard.backend.isNetworkError

/**
 * Reusable error section with optional retry for network errors.
 * Used in LoginScreen and RegisterScreen.
 */
@Composable
fun ErrorWithRetry(
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (errorMessage == null) return

    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = errorMessage,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier.fillMaxWidth()
    )
    if (isNetworkError(errorMessage)) {
        Spacer(modifier = Modifier.height(4.dp))
        TextButton(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tap to try again", color = MaterialTheme.colorScheme.primary)
        }
    }
}

