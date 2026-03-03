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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    onBackClick: () -> Unit = {}
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

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
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
                if (locationError) ErrorText("Please pin a location")

                // Mock map tile
                MockMapTile(
                    pinDropped = pinDropped,
                    onGpsGrab = {
                        // Simulate GPS grab with Davao City center coords
                        mockLat = 7.0644 + (Math.random() * 0.01 - 0.005)
                        mockLng = 125.6078 + (Math.random() * 0.01 - 0.005)
                        locationLabel = "%.4f, %.4f (GPS)".format(mockLat, mockLng)
                        pinDropped = true
                        locationError = false
                    },
                    onMapTap = {
                        mockLat = 7.0644 + (Math.random() * 0.02 - 0.01)
                        mockLng = 125.6078 + (Math.random() * 0.02 - 0.01)
                        locationLabel = "%.4f, %.4f (Pinned)".format(mockLat, mockLng)
                        pinDropped = true
                        locationError = false
                    }
                )

                if (pinDropped) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = locationLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF1565C0)
                        )
                    }
                }

                // Optional human-readable location name
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = if (locationLabel.contains("GPS") || locationLabel.contains("Pinned")) "" else locationLabel,
                    onValueChange = { locationLabel = it },
                    placeholder = { Text("Describe location (e.g. Near SM Lanang)", color = Color(0xFFAAAAAA)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = outlinedFieldColors(),
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray)
                    }
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
                    titleError = title.isBlank()
                    hazardError = selectedHazard == null
                    descError = description.isBlank()
                    locationError = !pinDropped && locationLabel.isBlank()

                    if (!titleError && !hazardError && !descError && !locationError) {
                        showSuccess = true
                        onSubmit(
                            CommunityReport(
                                title = title.trim(),
                                hazardType = selectedHazard!!,
                                description = description.trim(),
                                locationLabel = locationLabel.trim(),
                                latitude = mockLat,
                                longitude = mockLng
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7043)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Submit Alert",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(100.dp))
        }
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

// ── Real Map Tile ─────────────────────────────────────────────────────────────

@Composable
private fun MockMapTile(
    pinDropped: Boolean,
    onGpsGrab: () -> Unit,
    onMapTap: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (pinDropped) Color(0xFF1565C0) else Color(0xFFDDDDDD),
        animationSpec = tween(300),
        label = "border"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onMapTap() }
    ) {
        // Real MapLibre satellite map
        com.mobiledev.mindaguard.ui.components.MapLibreMapView(
            modifier = Modifier.fillMaxSize(),
            layers = emptyList(),
            mapStyleJson = com.mobiledev.mindaguard.ui.components.SATELLITE_STYLE_JSON,
            showEvacPins = false,
            showCritPins = false,
            initialLat = 7.0644,
            initialLng = 125.6079,
            initialZoom = 14.0
        )

        // Pin icon overlay in centre
        val pinSize by animateDpAsState(
            targetValue = if (pinDropped) 40.dp else 32.dp,
            animationSpec = tween(300),
            label = "pin"
        )
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Pin location",
            tint = if (pinDropped) Color(0xFFD32F2F) else Color(0xFF888888),
            modifier = Modifier
                .align(Alignment.Center)
                .size(pinSize)
        )

        // "Tap to pin" hint when nothing pinned
        if (!pinDropped) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.55f)
            ) {
                Text(
                    text = "Tap map to drop pin",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Pinned confirmation chip
        if (pinDropped) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1565C0).copy(alpha = 0.90f)
            ) {
                Text(
                    text = "📍 Location pinned",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    // GPS grab button
    OutlinedButton(
        onClick = onGpsGrab,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1565C0)),
        colors = ButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1565C0),
            disabledContainerColor = Color.LightGray,
            disabledContentColor = Color.Gray
        )
    ) {
        Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Grab Current GPS Location", fontWeight = FontWeight.Medium)
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

// ── Extensions on HazardType ──────────────────────────────────────────────────

private val HazardType.accentColor: Color
    get() = when (this) {
        HazardType.FLOOD       -> Color(0xFF1565C0)
        HazardType.LANDSLIDE   -> Color(0xFF6D4C41)
        HazardType.EARTHQUAKE  -> Color(0xFF546E7A)
        HazardType.STORM_SURGE -> Color(0xFF00838F)
        HazardType.TYPHOON     -> Color(0xFF6A1B9A)
    }

private val HazardType.label: String
    get() = when (this) {
        HazardType.FLOOD       -> "Flood"
        HazardType.LANDSLIDE   -> "Landslide"
        HazardType.EARTHQUAKE  -> "Earthquake"
        HazardType.STORM_SURGE -> "Storm Surge"
        HazardType.TYPHOON     -> "Typhoon"
    }

private val HazardType.emoji: String
    get() = when (this) {
        HazardType.FLOOD       -> "🌊"
        HazardType.LANDSLIDE   -> "⛰️"
        HazardType.EARTHQUAKE  -> "🌍"
        HazardType.STORM_SURGE -> "🌀"
        HazardType.TYPHOON     -> "🌪️"
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
            color = Color(0xFF1C1E21)
        )
        if (required) {
            Text(" *", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFFD32F2F)
    )
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF1565C0),
    unfocusedBorderColor = Color(0xFFDDDDDD),
    errorBorderColor = Color(0xFFD32F2F),
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







