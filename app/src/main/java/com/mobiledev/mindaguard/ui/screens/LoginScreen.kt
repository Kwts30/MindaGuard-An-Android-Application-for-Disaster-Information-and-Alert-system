package com.mobiledev.mindaguard.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobiledev.mindaguard.R
import com.mobiledev.mindaguard.backend.ForgotPasswordUiState
import com.mobiledev.mindaguard.backend.LoginUiState
import com.mobiledev.mindaguard.backend.LoginViewModel
import com.mobiledev.mindaguard.ui.components.ErrorWithRetry
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showForgotPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var forgotPasswordEmail by rememberSaveable { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    val forgotUiState by viewModel.forgotPasswordUiState.collectAsState()

    val isLoading = uiState is LoginUiState.Loading
    val errorMessage = (uiState as? LoginUiState.Error)?.message
    val emailError = loginEmailError(email)
    val passwordError = loginPasswordError(password)
    val canSubmit = !isLoading && emailError == null && passwordError == null

    fun submitLogin() {
        if (!canSubmit) return
        // TODO: Call FirebaseAuth.signInWithEmailAndPassword(...) from your final auth layer.
        viewModel.login(email.trim(), password, onLoginSuccess)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bk),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Logo above the card
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_only),
                contentDescription = "MindaGuard logo",
                modifier = Modifier.height(120.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = painterResource(id = R.drawable.icon_text),
                contentDescription = "MindaGuard logo text",
                modifier = Modifier
                    .height(36.dp)
                    .padding(horizontal = 24.dp),
                contentScale = ContentScale.Fit
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 112.dp)
                .align(Alignment.Center),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LoginForm(
                    email = email,
                    onEmailChange = {
                        email = it
                        if (uiState is LoginUiState.Error) viewModel.resetState()
                    },
                    emailError = emailError,
                    password = password,
                    onPasswordChange = {
                        password = it
                        if (uiState is LoginUiState.Error) viewModel.resetState()
                    },
                    passwordError = passwordError,
                    passwordVisible = passwordVisible,
                    onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onRetry = ::submitLogin,
                    onLoginClick = ::submitLogin,
                    onForgotPasswordClick = {
                        forgotPasswordEmail = email
                        showForgotPasswordDialog = true
                        viewModel.resetForgotPasswordState()
                    },
                    onNavigateToRegister = onNavigateToRegister
                )
            }
        }

        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                email = forgotPasswordEmail,
                onEmailChange = {
                    forgotPasswordEmail = it
                    if (forgotUiState is ForgotPasswordUiState.Error || forgotUiState is ForgotPasswordUiState.Success) {
                        viewModel.resetForgotPasswordState()
                    }
                },
                state = forgotUiState,
                onDismiss = {
                    showForgotPasswordDialog = false
                    viewModel.resetForgotPasswordState()
                },
                onSendResetLink = {
                    // TODO: Call FirebaseAuth.sendPasswordResetEmail(...) from your auth backend layer.
                    viewModel.sendPasswordReset(forgotPasswordEmail)
                }
            )
        }
    }
}

@Composable
private fun LoginForm(
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Text(
        text = "Welcome Back!",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Email") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        singleLine = true,
        enabled = !isLoading,
        isError = emailError != null,
        supportingText = { if (emailError != null) Text(emailError) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisibility) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                )
            }
        },
        singleLine = true,
        enabled = !isLoading,
        isError = passwordError != null,
        supportingText = { if (passwordError != null) Text(passwordError) },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onLoginClick() })
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onForgotPasswordClick, enabled = !isLoading) {
            Icon(Icons.Default.LockOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Forgot Password?")
        }
    }

    if (errorMessage != null) {
        ErrorWithRetry(errorMessage = errorMessage, onRetry = onRetry)
    }

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = onLoginClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(999.dp),
        enabled = !isLoading && emailError == null && passwordError == null,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text("LOGIN", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    TextButton(onClick = onNavigateToRegister, enabled = !isLoading) {
        Text("New user? Register now", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ForgotPasswordDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    state: ForgotPasswordUiState,
    onDismiss: () -> Unit,
    onSendResetLink: () -> Unit
) {
    val emailError = if (email.isBlank()) "Email is required" else loginEmailError(email)
    val isLoading = state is ForgotPasswordUiState.Loading

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter your registered email to receive a password reset link.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    placeholder = { Text("name@example.com") },
                    singleLine = true,
                    enabled = !isLoading,
                    isError = emailError != null,
                    supportingText = { if (emailError != null) Text(emailError) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (emailError == null) onSendResetLink() }),
                    modifier = Modifier.fillMaxWidth()
                )

                when (state) {
                    is ForgotPasswordUiState.Success -> {
                        Text(text = state.message, color = MaterialTheme.colorScheme.primary)
                    }
                    is ForgotPasswordUiState.Error -> {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                    else -> Unit
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSendResetLink,
                enabled = !isLoading && emailError == null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Send Link")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Close")
            }
        }
    )
}

private fun loginEmailError(email: String): String? {
    if (email.isBlank()) return "Email is required"
    return if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
        "Enter a valid email address"
    } else {
        null
    }
}

private fun loginPasswordError(password: String): String? {
    return if (password.isBlank()) "Password is required" else null
}
