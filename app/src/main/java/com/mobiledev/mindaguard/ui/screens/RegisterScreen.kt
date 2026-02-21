package com.mobiledev.mindaguard.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobiledev.mindaguard.R
import com.mobiledev.mindaguard.backend.RegisterUiState
import com.mobiledev.mindaguard.backend.RegisterViewModel

// Davao City districts and their barangays
private val davaoDistricts = mapOf(
    "District 1" to listOf(
        "Agdao", "Buhangin", "Bunawan", "Callawa", "Communal", "Indangan",
        "Lacson", "Lapu-lapu", "Mahayag", "Mandug", "New Valencia", "Panacan",
        "Salapawan", "Sasa", "Tigatto", "Vicente Hizon Sr.", "Waan"
    ),
    "District 2" to listOf(
        "Alambre", "Atan-Awe", "Biao Escuela", "Biao Guianga", "Biao Joaquin",
        "Cabantian", "Calinan", "Cawayan", "Dacudao", "Dalagdag", "Dominga",
        "Dumoy", "Fatima", "Kinuskusan", "Lacson", "Langub", "Lizada",
        "Lubogan", "Mabuhay", "Malabog", "Mapula", "Marapangi", "Marilog",
        "Matina Aplaya", "Matina Crossing", "Matina Pangi", "Mintal",
        "Mudiang", "Mulig", "New Carmen", "New Visayas", "Paradise Embak",
        "Riverside", "Sirib", "Suawan", "Subasta", "Talandang", "Taliñes",
        "Tamayong", "Tamugan", "Tapak", "Tawan-Tawan", "Tibuloy",
        "Tibungco", "Tigatto", "Toril", "Tugbok", "Ula"
    ),
    "District 3" to listOf(
        "Baliok", "Binugao", "Bucana", "Catalunan Grande", "Catalunan Pequeño",
        "Colosas", "Daliao", "Daliaon Plantation", "Eden", "Fatima",
        "Generoso", "Gov. Paciano Bangoy", "Gov. Vicente Duterte",
        "Gumalang", "Inawayan", "Kilate", "Lantic", "Malagos",
        "Maligatong", "Mandug", "Manuel Guianga", "Mapula", "Marapangi",
        "Matina Aplaya", "Mintal", "Mulig", "New Carmen", "Panacan",
        "Pangyan", "Riverside", "Santo Niño", "Tacunan", "Tagakpan",
        "Talandang", "Tamugan", "Toril", "Tungkalan", "Ula"
    ),
    "District 4" to listOf(
        "Angalan", "Bago Aplaya", "Bago Gallera", "Bago Oshiro",
        "Baliok", "Bangkas Heights", "Baracatan", "Barangay 1-40 (Poblacion)",
        "Bucana", "Bunawan", "Cadalian", "Centro (San Juan)",
        "Dacoville", "Diamond Heights", "Dumoy", "Fatima",
        "Granville", "Ilang", "Km. 11", "Lacson", "Lanang",
        "Lapanday", "Leon Garcia Sr.", "Light Industry and Science Park",
        "Maa", "Magtuod", "Matina Aplaya", "Mintal", "Pampanga",
        "Pañabo", "Riverside", "Sirawan", "Talomo Proper", "Tibungco",
        "Toril", "Tugbok", "Uyanguren", "Wilfredo Aquino"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: RegisterViewModel = viewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Address
    var selectedDistrict by remember { mutableStateOf("") }
    var selectedBarangay by remember { mutableStateOf("") }
    var districtExpanded by remember { mutableStateOf(false) }
    var barangayExpanded by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Success) {
            viewModel.resetState()
            onRegisterSuccess()
        }
    }

    val isLoading = uiState is RegisterUiState.Loading
    val errorMessage = (uiState as? RegisterUiState.Error)?.message
    val barangayList = davaoDistricts[selectedDistrict] ?: emptyList()

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
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
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

                // First Name
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it; viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("First Name") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Last Name
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it; viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Last Name") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mobile
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it; viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Mobile Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Address (Davao City)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // District Dropdown
                ExposedDropdownMenuBox(
                    expanded = districtExpanded,
                    onExpandedChange = { if (!isLoading) districtExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedDistrict,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        label = { Text("District") },
                        leadingIcon = { Icon(Icons.Default.Home, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                        enabled = !isLoading
                    )
                    ExposedDropdownMenu(
                        expanded = districtExpanded,
                        onDismissRequest = { districtExpanded = false }
                    ) {
                        davaoDistricts.keys.forEach { district ->
                            DropdownMenuItem(
                                text = { Text(district) },
                                onClick = {
                                    selectedDistrict = district
                                    selectedBarangay = ""
                                    districtExpanded = false
                                    viewModel.resetState()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Barangay Dropdown
                ExposedDropdownMenuBox(
                    expanded = barangayExpanded,
                    onExpandedChange = { if (!isLoading && selectedDistrict.isNotBlank()) barangayExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedBarangay,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        label = { Text("Barangay") },
                        leadingIcon = { Icon(Icons.Default.Home, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = barangayExpanded) },
                        enabled = !isLoading && selectedDistrict.isNotBlank(),
                        placeholder = { if (selectedDistrict.isBlank()) Text("Select a district first") }
                    )
                    ExposedDropdownMenu(
                        expanded = barangayExpanded,
                        onDismissRequest = { barangayExpanded = false }
                    ) {
                        barangayList.forEach { barangay ->
                            DropdownMenuItem(
                                text = { Text(barangay) },
                                onClick = {
                                    selectedBarangay = barangay
                                    barangayExpanded = false
                                    viewModel.resetState()
                                }
                            )
                        }
                    }
                }

                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.register(
                            email = email,
                            password = password,
                            confirmPassword = confirmPassword,
                            firstName = firstName,
                            lastName = lastName,
                            mobile = mobile,
                            barangay = selectedBarangay,
                            district = selectedDistrict
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(999.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
