package com.mobiledev.mindaguard.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobiledev.mindaguard.R
import com.mobiledev.mindaguard.backend.RegisterUiState
import com.mobiledev.mindaguard.backend.RegisterViewModel
import com.mobiledev.mindaguard.data.davaoDistricts
import com.mobiledev.mindaguard.ui.components.ErrorWithRetry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: RegisterViewModel = viewModel()
) {
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var mobile by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var selectedDistrict by rememberSaveable { mutableStateOf("") }
    var selectedBarangay by rememberSaveable { mutableStateOf("") }
    var districtExpanded by rememberSaveable { mutableStateOf(false) }
    var barangayExpanded by rememberSaveable { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is RegisterUiState.Loading
    val errorMessage = (uiState as? RegisterUiState.Error)?.message
    val barangayList = davaoDistricts[selectedDistrict] ?: emptyList()

    val firstNameError = requiredFieldError(firstName, "First name")
    val lastNameError = requiredFieldError(lastName, "Last name")
    val mobileError = mobileError(mobile)
    val emailError = registerEmailError(email)
    val passwordError = registerPasswordError(password)
    val confirmPasswordError = confirmPasswordError(password, confirmPassword)
    val districtError = requiredFieldError(selectedDistrict, "District")
    val barangayError = requiredFieldError(selectedBarangay, "Barangay")

    val canSubmit = !isLoading && listOf(
        firstNameError,
        lastNameError,
        mobileError,
        emailError,
        passwordError,
        confirmPasswordError,
        districtError,
        barangayError
    ).all { it == null }

    fun submitRegistration() {
        if (!canSubmit) return
        // TODO: Call FirebaseAuth.createUserWithEmailAndPassword(...) from your final auth layer.
        viewModel.register(
            email = email.trim(),
            password = password,
            confirmPassword = confirmPassword,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            mobile = mobile.trim(),
            barangay = selectedBarangay,
            district = selectedDistrict,
            onSuccess = onRegisterSuccess
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bk),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick, enabled = !isLoading) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create New Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                RegisterForm(
                    firstName = firstName,
                    onFirstNameChange = {
                        firstName = it
                        if (uiState is RegisterUiState.Error) viewModel.resetState()
                    },
                    firstNameError = firstNameError,
                    lastName = lastName,
                    onLastNameChange = {
                        lastName = it
                        if (uiState is RegisterUiState.Error) viewModel.resetState()
                    },
                    lastNameError = lastNameError,
                    mobile = mobile,
                    onMobileChange = {
                        mobile = it.filter(Char::isDigit).take(11)
                        if (uiState is RegisterUiState.Error) viewModel.resetState()
                    },
                    mobileError = mobileError,
                    email = email,
                    onEmailChange = {
                        email = it
                        if (uiState is RegisterUiState.Error) viewModel.resetState()
                    },
                    emailError = emailError,
                    password = password,
                    onPasswordChange = {
                        password = it
                        if (uiState is RegisterUiState.Error) viewModel.resetState()
                    },
                    passwordError = passwordError,
                    passwordVisible = passwordVisible,
                    onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = {
                        confirmPassword = it
                        if (uiState is RegisterUiState.Error) viewModel.resetState()
                    },
                    confirmPasswordError = confirmPasswordError,
                    confirmPasswordVisible = confirmPasswordVisible,
                    onToggleConfirmPasswordVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                    selectedDistrict = selectedDistrict,
                    selectedBarangay = selectedBarangay,
                    districtExpanded = districtExpanded,
                    barangayExpanded = barangayExpanded,
                    barangayList = barangayList,
                    onDistrictExpandedChange = { if (!isLoading) districtExpanded = it },
                    onBarangayExpandedChange = { if (!isLoading && selectedDistrict.isNotBlank()) barangayExpanded = it },
                    onDistrictSelect = { district ->
                        selectedDistrict = district
                        selectedBarangay = ""
                        districtExpanded = false
                        if (uiState is RegisterUiState.Error) viewModel.resetState()
                    },
                    onBarangaySelect = { barangay ->
                        selectedBarangay = barangay
                        barangayExpanded = false
                        if (uiState is RegisterUiState.Error) viewModel.resetState()
                    },
                    districtError = districtError,
                    barangayError = barangayError,
                    isLoading = isLoading,
                    onSubmit = ::submitRegistration
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ErrorWithRetry(
                        errorMessage = errorMessage,
                        onRetry = ::submitRegistration
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterForm(
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    firstNameError: String?,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    lastNameError: String?,
    mobile: String,
    onMobileChange: (String) -> Unit,
    mobileError: String?,
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    confirmPasswordError: String?,
    confirmPasswordVisible: Boolean,
    onToggleConfirmPasswordVisibility: () -> Unit,
    selectedDistrict: String,
    selectedBarangay: String,
    districtExpanded: Boolean,
    barangayExpanded: Boolean,
    barangayList: List<String>,
    onDistrictExpandedChange: (Boolean) -> Unit,
    onBarangayExpandedChange: (Boolean) -> Unit,
    onDistrictSelect: (String) -> Unit,
    onBarangaySelect: (String) -> Unit,
    districtError: String?,
    barangayError: String?,
    isLoading: Boolean,
    onSubmit: () -> Unit
) {
    val canSubmit = listOf(
        firstNameError,
        lastNameError,
        mobileError,
        emailError,
        passwordError,
        confirmPasswordError,
        districtError,
        barangayError
    ).all { it == null }

    OutlinedTextField(
        value = firstName,
        onValueChange = onFirstNameChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("First Name") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
        singleLine = true,
        enabled = !isLoading,
        isError = firstNameError != null,
        supportingText = { if (firstNameError != null) Text(firstNameError) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
        shape = RoundedCornerShape(8.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = lastName,
        onValueChange = onLastNameChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Last Name") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
        singleLine = true,
        enabled = !isLoading,
        isError = lastNameError != null,
        supportingText = { if (lastNameError != null) Text(lastNameError) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
        shape = RoundedCornerShape(8.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = mobile,
        onValueChange = onMobileChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Mobile Number") },
        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
        singleLine = true,
        enabled = !isLoading,
        isError = mobileError != null,
        supportingText = { if (mobileError != null) Text(mobileError) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
        shape = RoundedCornerShape(8.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Email Address") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        singleLine = true,
        enabled = !isLoading,
        isError = emailError != null,
        supportingText = { if (emailError != null) Text(emailError) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        shape = RoundedCornerShape(8.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisibility, enabled = !isLoading) {
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
        shape = RoundedCornerShape(8.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Confirm Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onToggleConfirmPasswordVisibility, enabled = !isLoading) {
                Icon(
                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                )
            }
        },
        singleLine = true,
        enabled = !isLoading,
        isError = confirmPasswordError != null,
        supportingText = { if (confirmPasswordError != null) Text(confirmPasswordError) },
        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        shape = RoundedCornerShape(8.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Address (Davao City)",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(8.dp))

    ExposedDropdownMenuBox(
        expanded = districtExpanded,
        onExpandedChange = onDistrictExpandedChange
    ) {
        OutlinedTextField(
            value = selectedDistrict,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("District") },
            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
            enabled = !isLoading,
            isError = districtError != null,
            supportingText = { if (districtError != null) Text(districtError) },
            shape = RoundedCornerShape(8.dp)
        )
        ExposedDropdownMenu(
            expanded = districtExpanded,
            onDismissRequest = { onDistrictExpandedChange(false) }
        ) {
            davaoDistricts.keys.forEach { district ->
                DropdownMenuItem(
                    text = { Text(district) },
                    onClick = { onDistrictSelect(district) }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    ExposedDropdownMenuBox(
        expanded = barangayExpanded,
        onExpandedChange = onBarangayExpandedChange
    ) {
        OutlinedTextField(
            value = selectedBarangay,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Barangay") },
            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = barangayExpanded) },
            enabled = !isLoading && selectedDistrict.isNotBlank(),
            isError = barangayError != null,
            placeholder = { if (selectedDistrict.isBlank()) Text("Select a district first") },
            supportingText = { if (barangayError != null) Text(barangayError) },
            shape = RoundedCornerShape(8.dp)
        )
        ExposedDropdownMenu(
            expanded = barangayExpanded,
            onDismissRequest = { onBarangayExpandedChange(false) }
        ) {
            barangayList.forEach { barangay ->
                DropdownMenuItem(
                    text = { Text(barangay) },
                    onClick = { onBarangaySelect(barangay) }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(8.dp),
        enabled = !isLoading && canSubmit
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Creating...")
        } else {
            Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun requiredFieldError(value: String, label: String): String? {
    return if (value.isBlank()) "$label is required" else null
}

private fun mobileError(value: String): String? {
    if (value.isBlank()) return "Mobile number is required"
    if (!value.all { it.isDigit() }) return "Mobile number must contain only digits"
    return if (value.length != 11) "Mobile number must be exactly 11 digits (e.g., 09XXXXXXXXX)" else null
}

private fun registerEmailError(value: String): String? {
    if (value.isBlank()) return "Email address is required"
    return if (!android.util.Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches()) {
        "Please enter a valid email address"
    } else null
}

private fun registerPasswordError(value: String): String? {
    if (value.isBlank()) return "Password is required"
    return if (value.length < 6) "Password must be at least 6 characters" else null
}

private fun confirmPasswordError(password: String, confirmPassword: String): String? {
    if (confirmPassword.isBlank()) return "Please confirm your password"
    return if (password != confirmPassword) "Passwords do not match" else null
}
