package com.mobiledev.mindaguard.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobiledev.mindaguard.R
import com.mobiledev.mindaguard.backend.ChangePasswordUiState
import com.mobiledev.mindaguard.backend.ChangePasswordViewModel
import com.mobiledev.mindaguard.theme.MenuCardBg
import com.mobiledev.mindaguard.theme.OrangeButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit = {},
    viewModel: ChangePasswordViewModel = viewModel()
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var currentVisible by rememberSaveable { mutableStateOf(false) }
    var newVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }

    val state by viewModel.uiState.collectAsState()

    val isLoading = state is ChangePasswordUiState.Loading
    val errorMessage = (state as? ChangePasswordUiState.Error)?.message
    val successMessage = (state as? ChangePasswordUiState.Success)?.message

    LaunchedEffect(state) {
        if (state is ChangePasswordUiState.Success) {
            // Let success text render briefly then return to Menu.
            delay(650)
            onSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .paint(
                painter = painterResource(id = R.drawable.bk),
                contentScale = ContentScale.Crop
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Change Password", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                ),
                modifier = Modifier.statusBarsPadding()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MenuCardBg.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "For security, enter your current password to set a new one.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )

                    PasswordField(
                        label = "Current password",
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            if (state !is ChangePasswordUiState.Idle) viewModel.reset()
                        },
                        visible = currentVisible,
                        onToggleVisible = { currentVisible = !currentVisible },
                        enabled = !isLoading
                    )

                    PasswordField(
                        label = "New password",
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            if (state !is ChangePasswordUiState.Idle) viewModel.reset()
                        },
                        visible = newVisible,
                        onToggleVisible = { newVisible = !newVisible },
                        enabled = !isLoading
                    )

                    PasswordField(
                        label = "Confirm new password",
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (state !is ChangePasswordUiState.Idle) viewModel.reset()
                        },
                        visible = confirmVisible,
                        onToggleVisible = { confirmVisible = !confirmVisible },
                        enabled = !isLoading
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (!successMessage.isNullOrBlank()) {
                        Text(
                            text = successMessage,
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.changePassword(
                                currentPassword = currentPassword,
                                newPassword = newPassword,
                                confirmPassword = confirmPassword
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeButton,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Color.White,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Text("Updating…")
                        } else {
                            Icon(Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                            Text("Update Password")
                        }
                    }

                    TextButton(
                        onClick = {
                            // Optionally clear fields
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                            viewModel.reset()
                        },
                        enabled = !isLoading
                    ) {
                        Text("Clear")
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // small footer logo (optional, consistent with your screens)
            Image(
                painter = painterResource(id = R.drawable.mmcm_logo),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(56.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onToggleVisible, enabled = enabled) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) "Hide" else "Show"
                )
            }
        },
        enabled = enabled,
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation()
    )
}



