package com.mobiledev.mindaguard.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledev.mindaguard.backend.AlertFeedItem
import com.mobiledev.mindaguard.theme.*
import com.mobiledev.mindaguard.ui.components.AlertDetailSheetContent
import com.mobiledev.mindaguard.ui.components.MapLibreMapView
import com.mobiledev.mindaguard.ui.components.MapLocation
import com.mobiledev.mindaguard.ui.components.SATELLITE_STYLE_JSON
import com.mobiledev.mindaguard.ui.components.CLASSIC_STYLE_JSON
import com.mobiledev.mindaguard.ui.components.VerifiedBadge
import com.mobiledev.mindaguard.ui.components.PendingBadge
import com.mobiledev.mindaguard.theme.MindaGuardTheme

// Convert AlertFeedItem → MapLocation so MapLibreMapView can fly to it
private fun AlertFeedItem.toMapLocation() = MapLocation(
    id      = id,
    name    = title,
    address = location,
    lat     = latitude,
    lng     = longitude
)

enum class AlertMapStyleType(val label: String) {
    SATELLITE("Satellite"),
    CLASSIC("Classic")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(
    alerts: List<AlertFeedItem> = emptyList(),
    isRefreshing: Boolean = false,
    listenerError: String? = null,
    onRefresh: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    // The alert the map is currently centred on
    var focusedAlert  by remember { mutableStateOf<AlertFeedItem?>(null) }
    // The alert whose detail card is expanded in the sheet
    var selectedAlert by remember { mutableStateOf<AlertFeedItem?>(null) }

    var searchQuery by remember { mutableStateOf("") }

    // Map style (Satellite / Classic) and layers panel visibility
    var mapStyle        by remember { mutableStateOf(AlertMapStyleType.SATELLITE) }
    var showLayersPanel by remember { mutableStateOf(false) }
    var mapInstance     by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }

    // Filter alerts by search query
    val filteredAlerts = remember(alerts, searchQuery) {
        if (searchQuery.isBlank()) alerts
        else alerts.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.location.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    // Fly-to location — updates when user taps a card
    val flyToLocation = focusedAlert?.toMapLocation()

    // Spinning animation for refresh icon
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing)),
        label         = "spin"
    )

    // Bottom sheet scaffold — mirrors MapScreen layout
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue        = SheetValue.PartiallyExpanded,
        confirmValueChange  = { true }
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState)

    BottomSheetScaffold(
        scaffoldState       = scaffoldState,
        sheetPeekHeight     = 200.dp,   // collapsed: shows search + first couple cards
        sheetContainerColor = Color.White,
        sheetShape          = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent        = {
            // ── Sheet header: drag handle + title + refresh ───────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(BorderGray, RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(12.dp))

                // Title row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Community Alerts",
                            style     = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color     = DarkText
                        )
                        Text(
                            "${filteredAlerts.size} alert${if (filteredAlerts.size != 1) "s" else ""} found",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    // Refresh button — spins while loading
                    IconButton(
                        onClick  = onRefresh,
                        enabled  = !isRefreshing,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = if (isRefreshing) OrangeButton else Color.Gray,
                            modifier = Modifier
                                .size(20.dp)
                                .then(if (isRefreshing) Modifier.rotate(rotation) else Modifier)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Search bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    color     = SearchBarBg,
                    shape     = RoundedCornerShape(23.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint     = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        TextField(
                            value         = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier      = Modifier.fillMaxWidth(),
                            placeholder   = { Text("Search alerts by name or location…", fontSize = 13.sp) },
                            colors        = TextFieldDefaults.colors(
                                focusedContainerColor   = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor   = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor             = Color.Black,
                                focusedTextColor        = Color.Black,
                                unfocusedTextColor      = Color.Black,
                                focusedPlaceholderColor   = Color.Gray,
                                unfocusedPlaceholderColor = Color.Gray
                            ),
                            singleLine  = true,
                            maxLines    = 1,
                            textStyle   = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Refreshing indicator
                if (isRefreshing) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color       = OrangeButton
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Refreshing…", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                // Firestore error banner
                if (listenerError != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "⚠ $listenerError",
                                style    = MaterialTheme.typography.bodySmall,
                                color    = Color(0xFFB71C1C),
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onRefresh) {
                                Text("Retry", color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── Alert list ────────────────────────────────────────────────
            if (filteredAlerts.isEmpty() && !isRefreshing) {
                Box(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment    = Alignment.Center
                ) {
                    Text(
                        text  = if (searchQuery.isBlank()) "No alerts yet." else "No results for \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier        = Modifier.fillMaxWidth(),
                    contentPadding  = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = 4.dp,
                        bottom = 120.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAlerts, key = { it.id }) { alert ->
                        AlertMapCard(
                            alert       = alert,
                            isSelected  = focusedAlert?.id == alert.id,
                            onClick     = {
                                focusedAlert  = alert
                                selectedAlert = alert
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Full-screen map ───────────────────────────────────────────
            MapLibreMapView(
                modifier       = Modifier.fillMaxSize(),
                layers         = emptyList(),
                mapStyleJson   = if (mapStyle == AlertMapStyleType.SATELLITE) SATELLITE_STYLE_JSON else CLASSIC_STYLE_JSON,
                showEvacPins   = false,
                showCritPins   = false,
                alertPins      = alerts.map { it.toMapLocation() },
                focusedAlertId = focusedAlert?.id,
                flyToLocation  = flyToLocation,
                initialLat     = 7.0644,
                initialLng     = 125.6079,
                initialZoom    = 12.0,
                onMapReady     = { map -> mapInstance = map }
            )

            // ── Top bar ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Back button — pinned to start
                Surface(
                    modifier        = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onBackClick() }
                        .align(Alignment.CenterStart),
                    color           = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }

                // Title pill — centred, wraps its own width
                Surface(
                    modifier        = Modifier
                        .wrapContentWidth()
                        .height(36.dp)
                        .align(Alignment.Center),
                    color           = Color.White,
                    shape           = RoundedCornerShape(18.dp),
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier         = Modifier.padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = "Alert Updates",
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ── Layers toggle + Compass — right side below top bar ────────
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(top = 56.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Layers toggle button
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { showLayersPanel = !showLayersPanel },
                    color = if (showLayersPanel) MaterialTheme.colorScheme.primary else Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = "Map Layers",
                            tint = if (showLayersPanel) Color.White else Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Compass / north-up reset
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            mapInstance?.animateCamera(
                                org.maplibre.android.camera.CameraUpdateFactory.bearingTo(0.0)
                            )
                        },
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Explore,
                            contentDescription = "Reset North",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // ── Floating layers panel ─────────────────────────────────────
            AnimatedVisibility(
                visible = showLayersPanel,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(top = 56.dp, end = 64.dp)
            ) {
                AlertLayersPanel(
                    mapStyle = mapStyle,
                    onMapStyleChange = { mapStyle = it },
                    onClose = { showLayersPanel = false }
                )
            }
        }
    }

    // ── Full detail bottom sheet — opens when a card is tapped ───────────────
    if (selectedAlert != null) {
        val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedAlert = null },
            sheetState       = detailSheetState,
            containerColor   = Color.White,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AlertDetailSheetContent(alert = selectedAlert!!, timePrefix = "Reported:")
        }
    }
}

// ── Alert Layers Floating Panel ───────────────────────────────────────────────

@Composable
private fun AlertLayersPanel(
    mapStyle: AlertMapStyleType,
    onMapStyleChange: (AlertMapStyleType) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.width(220.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Map Options",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "MAP TYPE",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlertMapStyleType.entries.forEach { styleType ->
                    val selected = styleType == mapStyle
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onMapStyleChange(styleType) }
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFDDDDDD),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (styleType == AlertMapStyleType.SATELLITE) Color(0xFF2B4B6F)
                                    else Color(0xFFE8E0D4)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (styleType == AlertMapStyleType.SATELLITE)
                                    Icons.Default.Satellite else Icons.Default.Map,
                                contentDescription = styleType.label,
                                tint = if (styleType == AlertMapStyleType.SATELLITE) Color.White
                                       else Color(0xFF5C4A32),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = styleType.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.DarkGray
                            )
                            if (selected) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Alert map card — compact card in the bottom sheet list ───────────────────

@Composable
private fun AlertMapCard(
    alert: AlertFeedItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) OrangeButton else Color.Transparent
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFF3E0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
        border    = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 0.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Warning icon
            Surface(
                shape    = CircleShape,
                color    = if (isSelected) Color(0xFFFFE0B2) else LightRedBg,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint     = if (isSelected) OrangeButton else RedWarning,
                    modifier = Modifier
                        .padding(9.dp)
                        .fillMaxSize()
                )
            }

            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Title + badge row
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        alert.title,
                        fontWeight  = FontWeight.Bold,
                        fontSize    = 13.sp,
                        maxLines    = 1,
                        overflow    = TextOverflow.Ellipsis,
                        color       = DarkText,
                        modifier    = Modifier.weight(1f, fill = false)
                    )
                    if (alert.isVerified) VerifiedBadge() else PendingBadge()
                }

                // Description
                Text(
                    alert.description,
                    fontSize  = 12.sp,
                    color     = Color.Gray,
                    maxLines  = 2,
                    overflow  = TextOverflow.Ellipsis
                )

                // Location + time row
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint     = if (isSelected) OrangeButton else Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        alert.location,
                        fontSize  = 11.sp,
                        color     = if (isSelected) OrangeButton else Color.Gray,
                        maxLines  = 1,
                        overflow  = TextOverflow.Ellipsis,
                        modifier  = Modifier.weight(1f)
                    )
                    Text(
                        alert.timeLabel,
                        fontSize = 11.sp,
                        color    = SubtleGray
                    )
                }

                // "Tap to view on map" hint when selected
                if (isSelected) {
                    Text(
                        "📍 Pinned on map  •  Tap again for full details",
                        fontSize   = 10.sp,
                        color      = OrangeButton,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun AlertScreenPreview() {
    MindaGuardTheme(darkTheme = false) {
        AlertScreen(
            alerts = listOf(
                AlertFeedItem(
                    title = "FLOOD WARNING AT QUIMPO ST.", location = "Quimpo St., Davao City",
                    description = "Heavy Rain, Causing flash floods.", timeLabel = "Just Now",
                    latitude = 7.0694, longitude = 125.6083
                ),
                AlertFeedItem(
                    title = "FLOOD WARNING AT UM MATINA", location = "UM Matina, Davao City",
                    description = "Heavy rains, possible river overflow.", timeLabel = "5m ago",
                    latitude = 7.0510, longitude = 125.5862, isVerified = true
                )
            )
        )
    }
}