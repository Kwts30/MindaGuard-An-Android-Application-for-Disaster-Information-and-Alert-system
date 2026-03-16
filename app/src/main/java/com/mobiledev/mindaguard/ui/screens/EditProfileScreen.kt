package com.mobiledev.mindaguard.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mobiledev.mindaguard.R
import com.mobiledev.mindaguard.backend.EditProfileUiState
import com.mobiledev.mindaguard.backend.EditProfileViewModel
import com.mobiledev.mindaguard.backend.ProfileUiState
import com.mobiledev.mindaguard.backend.UserProfileViewModel

@Composable
fun EditProfileScreen(
	onBackClick: () -> Unit,
	profileViewModel: UserProfileViewModel = viewModel(),
	editViewModel: EditProfileViewModel = viewModel()
) {
	val profileState by profileViewModel.uiState.collectAsState()
	val editState by editViewModel.uiState.collectAsState()
	val uploadedPhotoUrl by editViewModel.photoUrl.collectAsState()

	var firstName by rememberSaveable { mutableStateOf("") }
	var lastName by rememberSaveable { mutableStateOf("") }
	var mobile by rememberSaveable { mutableStateOf("") }
	var district by rememberSaveable { mutableStateOf("") }
	var barangay by rememberSaveable { mutableStateOf("") }
	var pickedImageUriString by rememberSaveable { mutableStateOf<String?>(null) }
	var currentPhotoUrl by rememberSaveable { mutableStateOf("") }

	val imagePicker = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.GetContent()
	) { uri: Uri? ->
		pickedImageUriString = uri?.toString()
	}

	LaunchedEffect(Unit) {
		profileViewModel.loadProfile()
		editViewModel.loadCurrentPhoto()
	}

	LaunchedEffect(profileState) {
		val profile = (profileState as? ProfileUiState.Success)?.profile ?: return@LaunchedEffect
		val isFormBlank = firstName.isBlank() &&
			lastName.isBlank() &&
			mobile.isBlank() &&
			district.isBlank() &&
			barangay.isBlank() &&
			pickedImageUriString == null

		if (isFormBlank) {
			firstName = profile.firstName
			lastName = profile.lastName
			mobile = profile.mobile
			district = profile.district
			barangay = profile.barangay
			currentPhotoUrl = profile.photoUrl
		}
	}

	LaunchedEffect(uploadedPhotoUrl) {
		if (!uploadedPhotoUrl.isNullOrBlank()) {
			currentPhotoUrl = uploadedPhotoUrl.orEmpty()
		}
	}

	Box(modifier = Modifier.fillMaxSize()) {
		Image(
			painter = painterResource(id = R.drawable.bk),
			contentDescription = null,
			modifier = Modifier.fillMaxSize(),
			contentScale = ContentScale.Crop
		)

		Column(modifier = Modifier.fillMaxSize()) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 4.dp, vertical = 8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(onClick = onBackClick) {
					Icon(
						imageVector = Icons.AutoMirrored.Filled.ArrowBack,
						contentDescription = "Back",
						tint = Color.Black
					)
				}
				Text(
					text = "Edit Profile",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Medium,
					color = Color.Black
				)
			}

			Card(
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 16.dp, vertical = 8.dp),
				shape = RoundedCornerShape(20.dp),
				colors = CardDefaults.cardColors(containerColor = Color(0xEEE8E4DF)),
				elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
			) {
				Column(
					modifier = Modifier
						.fillMaxSize()
						.verticalScroll(rememberScrollState())
						.padding(16.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Top
				) {
					val previewModel = remember(pickedImageUriString, currentPhotoUrl) {
						pickedImageUriString ?: currentPhotoUrl.takeIf { it.isNotBlank() }
					}

					Box(
						modifier = Modifier
							.size(110.dp)
							.clip(CircleShape)
							.background(Color(0xFFD8D4CF)),
						contentAlignment = Alignment.Center
					) {
						if (previewModel != null) {
							AsyncImage(
								model = previewModel,
								contentDescription = "Profile photo",
								modifier = Modifier.fillMaxSize(),
								contentScale = ContentScale.Crop
							)
						} else {
							Icon(
								imageVector = Icons.Default.Person,
								contentDescription = "Default avatar",
								tint = Color(0xFF4A4A4A),
								modifier = Modifier.size(64.dp)
							)
						}
					}

					Spacer(modifier = Modifier.height(12.dp))

					Button(
						onClick = { imagePicker.launch("image/*") },
						shape = RoundedCornerShape(999.dp),
						colors = ButtonDefaults.buttonColors(
							containerColor = Color(0xFFD6D1CC),
							contentColor = Color(0xFF444444)
						)
					) {
						Text("Choose Profile Picture")
					}

					Spacer(modifier = Modifier.height(16.dp))

					OutlinedTextField(
						value = firstName,
						onValueChange = { firstName = it },
						modifier = Modifier.fillMaxWidth(),
						label = { Text("First Name") },
						singleLine = true
					)

					Spacer(modifier = Modifier.height(10.dp))

					OutlinedTextField(
						value = lastName,
						onValueChange = { lastName = it },
						modifier = Modifier.fillMaxWidth(),
						label = { Text("Last Name") },
						singleLine = true
					)

					Spacer(modifier = Modifier.height(10.dp))

					OutlinedTextField(
						value = mobile,
						onValueChange = { mobile = it },
						modifier = Modifier.fillMaxWidth(),
						label = { Text("Mobile Number") },
						singleLine = true
					)

					Spacer(modifier = Modifier.height(10.dp))

					OutlinedTextField(
						value = district,
						onValueChange = { district = it },
						modifier = Modifier.fillMaxWidth(),
						label = { Text("District") },
						singleLine = true
					)

					Spacer(modifier = Modifier.height(10.dp))

					OutlinedTextField(
						value = barangay,
						onValueChange = { barangay = it },
						modifier = Modifier.fillMaxWidth(),
						label = { Text("Barangay") },
						singleLine = true
					)

					Spacer(modifier = Modifier.height(18.dp))

					val isSaving = editState is EditProfileUiState.Loading
					Button(
						onClick = {
							editViewModel.saveProfile(
								firstName = firstName,
								lastName = lastName,
								mobile = mobile,
								district = district,
								barangay = barangay,
								pickedImageUri = pickedImageUriString?.let(Uri::parse),
								onDone = onBackClick
							)
						},
						enabled = !isSaving,
						modifier = Modifier
							.fillMaxWidth()
							.height(50.dp),
						shape = RoundedCornerShape(999.dp)
					) {
						if (isSaving) {
							CircularProgressIndicator(
								modifier = Modifier.size(20.dp),
								strokeWidth = 2.dp,
								color = MaterialTheme.colorScheme.onPrimary
							)
						} else {
							Text("Save Changes")
						}
					}

					if (editState is EditProfileUiState.Error) {
						Spacer(modifier = Modifier.height(10.dp))
						Text(
							text = (editState as EditProfileUiState.Error).message,
							color = MaterialTheme.colorScheme.error,
							style = MaterialTheme.typography.bodyMedium
						)
					}
				}
			}
		}
	}
}


