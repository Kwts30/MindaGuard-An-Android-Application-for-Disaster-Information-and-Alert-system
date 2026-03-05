package com.mobiledev.mindaguard.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledev.mindaguard.model.HazardType
import com.mobiledev.mindaguard.theme.MindaGuardTheme
import com.mobiledev.mindaguard.theme.*
import com.mobiledev.mindaguard.ui.components.LocationPickerDialog
import com.mobiledev.mindaguard.ui.components.PickedLocation

// ── Submitted report data class ───────────────────────────────────────────────

data class CommunityReport(
    val title: String,
    val hazardType: HazardType,
    val description: String,
    val locationLabel: String,
    val latitude: Double,
    val longitude: Double
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun CreateAlertScreen(
    onSubmit: (CommunityReport) -> Unit = {},
    onBackClick: () -> Unit = {},
    isSubmitting: Boolean = false,
    submitError: String? = null,
    onClearError: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var selectedHazard by remember { mutableStateOf<HazardType?>(null) }
    var description by remember { mutableStateOf("") }
    var locationLabel by remember { mutableStateOf("") }
    var pinDropped by remember { mutableStateOf(false) }
    // Mock coordinates – will be replaced by real GPS / map pick
    var mockLat by remember { mutableDoubleStateOf(7.0644) }
    var mockLng by remember { mutableDoubleStateOf(125.6078) }

    var showSuccess by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    var hazardError by remember { mutableStateOf(false) }
    var descError by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Create Alert",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // ── Scrollable form ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(Modifier.height(4.dp))

            // ── Section: Incident Title ───────────────────────────────────────
            SectionCard {
                SectionLabel(text = "Incident Title", required = true)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    placeholder = { Text("e.g. Road blocked at Quimpo Blvd", color = Color(0xFFAAAAAA)) },
                    isError = titleError,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = outlinedFieldColors()
                )
                if (titleError) ErrorText("Please enter an incident title")
            }

            // ── Section: Hazard Type ──────────────────────────────────────────
            SectionCard {
                SectionLabel(text = "Hazard Type", required = true)
                if (hazardError) ErrorText("Please select a hazard type")
                HazardTypeSelector(
                    selected = selectedHazard,
                    onSelect = { selectedHazard = it; hazardError = false }
                )
            }

            // ── Section: Location Pin ─────────────────────────────────────────
            SectionCard {
                SectionLabel(text = "Pin Location", required = true)
                if (locationError) ErrorText("Please pin a location on the map")

                LocationThumbnail(
                    pinDropped   = pinDropped,
                    lat          = mockLat,
                    lng          = mockLng,
                    locationLabel= locationLabel,
                    onTap        = { showLocationPicker = true }
                )
            }

            // ── Section: Description ──────────────────────────────────────────
            SectionCard {
                SectionLabel(text = "Description", required = true)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; descError = false },
                    placeholder = {
                        Text(
                            "Describe the situation in detail. Include what you see, how severe it is, and who might be affected.",
                            color = Color(0xFFAAAAAA)
                        )
                    },
                    isError = descError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 6,
                    colors = outlinedFieldColors()
                )
                if (descError) ErrorText("Please describe the incident")
                Text(
                    text = "${description.length}/500",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }

            // ── Disclaimer ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF3E0))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF6F00),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Only report real, verified incidents. False reports may delay emergency response.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF7A4000),
                    lineHeight = 16.sp
                )
            }

            // ── Submit button ─────────────────────────────────────────────────
            Button(
                onClick = {
                    titleError    = title.isBlank()
                    hazardError   = selectedHazard == null
                    descError     = description.isBlank()
                    locationError = !pinDropped && locationLabel.isBlank()

                    if (!titleError && !hazardError && !descError && !locationError) {
                        showSuccess = true
                        onSubmit(
                            CommunityReport(
                                title         = title.trim(),
                                hazardType    = selectedHazard!!,
                                description   = description.trim(),
                                locationLabel = locationLabel.trim(),
                                latitude      = mockLat,
                                longitude     = mockLng
                            )
                        )
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeButton,
                    disabledContainerColor = OrangeButton.copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Submitting…", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Alert", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // ── Firebase submit error ──────────────────────────────────────────
            if (submitError != null) {
                LaunchedEffect(submitError) {
                    kotlinx.coroutines.delay(4000)
                    onClearError()
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFB71C1C).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "⚠ $submitError",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB71C1C)
                    )
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }

    // ── Full-screen location picker ───────────────────────────────────────────
    if (showLocationPicker) {
        LocationPickerDialog(
            initialLat = mockLat,
            initialLng = mockLng,
            onDismiss  = { showLocationPicker = false },
            onConfirm  = { picked: PickedLocation ->
                mockLat       = picked.latitude
                mockLng       = picked.longitude
                locationLabel = picked.label
                pinDropped    = true
                locationError = false
                showLocationPicker = false
            }
        )
    }

    // ── Success snackbar / toast overlay ─────────────────────────────────────
    if (showSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showSuccess = false
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF2E7D32),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Text("Alert submitted!", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Location thumbnail (tap → opens full-screen picker) ──────────────────────

@Composable
private fun LocationThumbnail(
    pinDropped: Boolean,
    lat: Double,
    lng: Double,
    locationLabel: String,
    onTap: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (pinDropped) BlueLink else Color(0xFFDDDDDD),
        animationSpec = tween(300),
        label = "border"
    )

    // Thumbnail map preview
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        com.mobiledev.mindaguard.ui.components.MapLibreMapView(
            modifier  = Modifier.fillMaxSize(),
            layers    = emptyList(),
            mapStyleJson = com.mobiledev.mindaguard.ui.components.CLASSIC_STYLE_JSON,
            showEvacPins = false,
            showCritPins = false,
            frozen    = true,
            initialLat = lat,
            initialLng = lng,
            initialZoom = 15.5
        )

        // Center pin
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = if (pinDropped) OrangeButton else Color(0xFF888888),
            modifier = Modifier
                .align(Alignment.Center)
                .size(if (pinDropped) 40.dp else 32.dp)
                .offset(y = (-16).dp)
        )

        // "Tap to pick location" / "Tap to change" hint at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (pinDropped) "📍 Tap to change location" else "Tap to pick location on map",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }

        // Transparent clickable overlay on top
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onTap)
        )
    }

    // Show confirmed coordinates below thumbnail
    if (pinDropped) {
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = BlueLink,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = locationLabel,
                style = MaterialTheme.typography.labelSmall,
                color = BlueLink,
                maxLines = 1
            )
        }
    }
}

// ── Hazard type chip selector ─────────────────────────────────────────────────

@Composable
private fun HazardTypeSelector(
    selected: HazardType?,
    onSelect: (HazardType) -> Unit
) {
    val types = HazardType.entries.toList()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Two rows of chips
        val rows = types.chunked(3)
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { type ->
                    val isSelected = selected == type
                    val chipColor = if (isSelected) type.accentColor else Color.White
                    val textColor = if (isSelected) Color.White else Color(0xFF444444)
                    val borderColor = if (isSelected) type.accentColor else Color(0xFFDDDDDD)

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(type) }
                            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp)),
                        color = chipColor,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = type.emoji,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
                // Fill remaining cells if row is not full
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
private fun SectionLabel(text: String, required: Boolean = false) {
    Row {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = DarkText
        )
        if (required) {
            Text(" *", color = RedWarning, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelSmall,
        color = RedWarning
    )
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BlueLink,
    unfocusedBorderColor = BorderGray,
    errorBorderColor = RedWarning,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White
)

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun CreateAlertScreenPreview() {
    MindaGuardTheme(darkTheme = false) {
        CreateAlertScreen()
    }
}







