package com.mobiledev.mindaguard.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobiledev.mindaguard.R
import kotlinx.coroutines.delay
import com.mobiledev.mindaguard.backend.LayerDownloadState
import com.mobiledev.mindaguard.backend.MapLayerUiModel
import com.mobiledev.mindaguard.backend.MapLayerViewModel
import com.mobiledev.mindaguard.ui.components.CriticalFacilities
import com.mobiledev.mindaguard.ui.components.EvacCenters
import com.mobiledev.mindaguard.ui.components.MapLibreMapView
import com.mobiledev.mindaguard.ui.components.MapLocation
import com.mobiledev.mindaguard.ui.components.SATELLITE_STYLE_JSON
import com.mobiledev.mindaguard.ui.components.CLASSIC_STYLE_JSON
import com.mobiledev.mindaguard.ui.components.SearchableLocationSheet
import com.mobiledev.mindaguard.theme.GreenSuccess
import com.mobiledev.mindaguard.theme.CriticalRed
import com.mobiledev.mindaguard.theme.SearchBarBg

/* ─────────────────────────── Map Style ─────────────────────────── */

enum class MapStyleType(val label: String) {
    SATELLITE("Satellite"),
    CLASSIC("Classic")
}

/* ─────────────────────────── Tabs ─────────────────────────── */

enum class MapTab(val display: String) {
    EVACUATION("Evacuation"),
    HAZARD("Hazard Map"),
    CRITICAL("Critical")
}

/* ─────────────────────────── Main Screen ─────────────────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBackClick: () -> Unit = {},
    layerViewModel: MapLayerViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(MapTab.EVACUATION) }
    var searchQuery by remember { mutableStateOf("") }

    // Layer state from ViewModel
    val layers by layerViewModel.layers.collectAsState()
    val isLoadingLayers by layerViewModel.isLoadingLayers.collectAsState()
    val errorMsg by layerViewModel.errorMessage.collectAsState()

    // Show/hide the layers side-panel
    var showLayersPanel by remember { mutableStateOf(false) }

    // Current map base style (Satellite or Classic)
    var mapStyle by remember { mutableStateOf(MapStyleType.SATELLITE) }

    // Pin visibility toggles (shown in layers panel)
    var showEvacPins by remember { mutableStateOf(true) }
    var showCritPins by remember { mutableStateOf(true) }

    // Fly-to: set when a list item is tapped
    var flyToLocation by remember { mutableStateOf<MapLocation?>(null) }

    // Hold a reference to the live MapLibreMap for the compass reset
    var mapInstance by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }

    // Loading overlay — shown until map tiles are ready AND minimum delay has passed
    var mapReady by remember { mutableStateOf(false) }
    var delayDone by remember { mutableStateOf(false) }
    val showLoader = !mapReady || !delayDone

    LaunchedEffect(Unit) {
        delay(1800L)   // minimum display time for the loading screen
        delayDone = true
    }

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        confirmValueChange = { true }
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState)

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 140.dp,
        sheetContainerColor = Color.White,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent = {
            when (currentTab) {
                MapTab.EVACUATION -> SearchableLocationSheet(
                    title = "Evacuation Centers",
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    searchPlaceholder = "Search evacuation center",
                    locations = EvacCenters,
                    pinColor = GreenSuccess,
                    emptyMessage = "No matching evacuation center found",
                    onLocationClick = { loc -> flyToLocation = loc },
                    tabsContent = { MapTabs(currentTab = currentTab, onTabChange = { tab -> currentTab = tab; searchQuery = "" }) }
                )
                MapTab.HAZARD -> HazardSheetContent(
                    currentTab = currentTab,
                    onTabChange = { currentTab = it },
                    layers = layers,
                    isLoading = isLoadingLayers,
                    onToggleVisibility = { layerViewModel.toggleVisibility(it) },
                    onDownload = { layerViewModel.downloadLayer(it) },
                    onDelete = { layerViewModel.deleteLocalLayer(it) },
                    onRefresh = { layerViewModel.fetchLayers() }
                )
                MapTab.CRITICAL -> SearchableLocationSheet(
                    title = "Critical Facilities",
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    searchPlaceholder = "Search hospital or health center",
                    locations = CriticalFacilities,
                    pinColor = CriticalRed,
                    emptyMessage = "No matching facility found",
                    onLocationClick = { loc -> flyToLocation = loc },
                    tabsContent = { MapTabs(currentTab = currentTab, onTabChange = { tab -> currentTab = tab; searchQuery = "" }) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Real MapLibre satellite map ──────────────────────────────
            MapLibreMapView(
                modifier = Modifier.fillMaxSize(),
                layers = layers,
                mapStyleJson = if (mapStyle == MapStyleType.SATELLITE) SATELLITE_STYLE_JSON else CLASSIC_STYLE_JSON,
                showEvacPins = showEvacPins,
                showCritPins = showCritPins,
                flyToLocation = flyToLocation,
                onMapReady = { map ->
                    mapInstance = map
                    mapReady = true
                }
            )

            // ── MindaGuard logo loading overlay ──────────────────────────
            AnimatedVisibility(
                visible = showLoader,
                exit = fadeOut(animationSpec = tween(durationMillis = 500))
            ) {
                MapLoadingOverlay()
            }
            if (errorMsg != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp, start = 16.dp, end = 16.dp),
                    color = Color(0xFFB71C1C),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMsg ?: "",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { layerViewModel.fetchLayers() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = Color.White)
                        }
                    }
                }
            }

            // ── Top bar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onBackClick() },
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Title pill
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    shadowElevation = 4.dp
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Evacuation / Hazard Map",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right spacer — mirrors back button so title pill stays centred
                Spacer(modifier = Modifier.size(36.dp))
            }

            // ── Vertical button column — right side, below the top bar ───
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(top = 56.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Layers toggle
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
                            contentDescription = "Layers",
                            tint = if (showLayersPanel) Color.White else Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Compass / orientation reset
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            mapInstance?.let { map ->
                                map.animateCamera(
                                    org.maplibre.android.camera.CameraUpdateFactory.bearingTo(0.0)
                                )
                            }
                        },
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Explore,
                            contentDescription = "Compass",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // ── Floating Layers Panel — opens left of the button column ──
            AnimatedVisibility(
                visible = showLayersPanel,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(top = 56.dp, end = 64.dp)   // 64dp clears the 40dp btn + 16dp margin
            ) {
                LayersFloatingPanel(
                    mapStyle = mapStyle,
                    onMapStyleChange = { mapStyle = it },
                    showEvacPins = showEvacPins,
                    onToggleEvac = { showEvacPins = !showEvacPins },
                    showCritPins = showCritPins,
                    onToggleCrit = { showCritPins = !showCritPins },
                    onClose = { showLayersPanel = false }
                )
            }
        }
    }
}

/* ─────────────────────────── Floating Layers Panel ─────────────────────────── */

@Composable
private fun LayersFloatingPanel(
    mapStyle: MapStyleType,
    onMapStyleChange: (MapStyleType) -> Unit,
    showEvacPins: Boolean,
    onToggleEvac: () -> Unit,
    showCritPins: Boolean,
    onToggleCrit: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.width(260.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // ── Header ───────────────────────────────────────────────────
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

            // ── Map type switcher ─────────────────────────────────────────
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
                MapStyleType.entries.forEach { styleType ->
                    val selected = styleType == mapStyle
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onMapStyleChange(styleType) }
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                        else Color(0xFFDDDDDD),
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
                                    if (styleType == MapStyleType.SATELLITE)
                                        Color(0xFF2B4B6F)
                                    else
                                        Color(0xFFE8E0D4)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (styleType == MapStyleType.SATELLITE)
                                    Icons.Default.Satellite
                                else
                                    Icons.Default.Map,
                                contentDescription = styleType.label,
                                tint = if (styleType == MapStyleType.SATELLITE)
                                    Color.White
                                else
                                    Color(0xFF5C4A32),
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

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 0.5.dp,
                color = Color(0xFFEEEEEE)
            )

            // ── Overlays (pin layers) ─────────────────────────────────────
            Text(
                text = "OVERLAYS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Evacuation centers toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleEvac() }
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E7D32))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Evacuation Centers",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = showEvacPins,
                    onCheckedChange = { onToggleEvac() },
                    modifier = Modifier.size(20.dp)
                )
            }

            // Critical facilities toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleCrit() }
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFC62828))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Critical Facilities",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = showCritPins,
                    onCheckedChange = { onToggleCrit() },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/* ─────────────────────────── Layer Row Item ─────────────────────────── */

@Composable
private fun LayerRowItem(
    model: MapLayerUiModel,
    onToggleVisibility: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val isDownloaded  = model.downloadState is LayerDownloadState.Downloaded
    val isDownloading = model.downloadState is LayerDownloadState.Downloading
    val progress      = (model.downloadState as? LayerDownloadState.Downloading)?.progress ?: 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (model.isVisible && isDownloaded) Color(0xFFF5F5F5) else Color.Transparent)
            .clickable(enabled = isDownloaded) { onToggleVisibility() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color swatch dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(
                    runCatching {
                        Color(android.graphics.Color.parseColor(model.layer.color))
                    }.getOrDefault(Color(0xFFFF5722))
                )
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = model.layer.name,
                style    = MaterialTheme.typography.bodySmall,
                fontWeight = if (model.isVisible && isDownloaded) FontWeight.SemiBold else FontWeight.Normal,
                color    = if (model.isVisible && isDownloaded) Color(0xFF1A1A1A) else Color(0xFF555555),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isDownloading) {
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress    = { animatedProgress },
                    modifier    = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color       = MaterialTheme.colorScheme.primary,
                    trackColor  = Color(0xFFE0E0E0)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        when {
            isDownloading -> {
                CircularProgressIndicator(
                    modifier    = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.primary
                )
            }
            isDownloaded -> {
                // Switch toggle for visibility
                Switch(
                    checked         = model.isVisible,
                    onCheckedChange = { onToggleVisibility() },
                    modifier        = Modifier.height(24.dp),
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor   = Color.White,
                        checkedTrackColor   = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCCCCCC)
                    )
                )
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove layer",
                        modifier = Modifier.size(15.dp),
                        tint     = Color(0xFFBDBDBD)
                    )
                }
            }
            else -> {
                // Download button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onDownload() },
                    color    = Color(0xFFF0F4FF),
                    shape    = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Download",
                            modifier = Modifier.size(13.dp),
                            tint     = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Get",
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/* ─────────────────────────── Hazard Sheet (bottom sheet tab) ─────────────────────────── */

// Category icon mapping — Material icons, no emojis
@Composable
private fun categoryIcon(cat: String): androidx.compose.ui.graphics.vector.ImageVector = when (cat) {
    "flood"       -> Icons.Default.Water
    "storm_surge" -> Icons.Default.Air
    "earthquake"  -> Icons.Default.Terrain
    "landslide"   -> Icons.Default.Landscape
    else          -> Icons.Default.Layers
}

private fun categoryLabel(cat: String): String = when (cat) {
    "flood"       -> "Flood"
    "storm_surge" -> "Storm Surge"
    "earthquake"  -> "Earthquake Faults"
    "landslide"   -> "Landslide"
    else          -> cat.replaceFirstChar { it.uppercase() }
}

@Composable
private fun HazardSheetContent(
    currentTab: MapTab,
    onTabChange: (MapTab) -> Unit,
    layers: List<MapLayerUiModel>,
    isLoading: Boolean,
    onToggleVisibility: (String) -> Unit,
    onDownload: (String) -> Unit,
    onDelete: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        MapTabs(currentTab = currentTab, onTabChange = onTabChange)
        Spacer(Modifier.height(16.dp))

        // Header row
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Default.Layers,
                contentDescription = null,
                tint               = Color(0xFF444444),
                modifier           = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text       = "Hazard Layers",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF1A1A1A),
                modifier   = Modifier.weight(1f)
            )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                IconButton(
                    onClick  = onRefresh,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.size(16.dp),
                        tint     = Color(0xFF888888)
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text  = "Tap Get to download a layer, then toggle it on or off.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF888888)
        )
        Spacer(Modifier.height(14.dp))

        val grouped       = layers.groupBy { it.layer.category }
        val categoryOrder = listOf("flood", "storm_surge", "earthquake", "landslide")

        LazyColumn(
            modifier            = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            (categoryOrder + grouped.keys.filter { it !in categoryOrder }).forEach { cat ->
                val catLayers = grouped[cat] ?: return@forEach

                // Category header
                item {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector        = categoryIcon(cat),
                            contentDescription = null,
                            tint               = Color(0xFF666666),
                            modifier           = Modifier.size(14.dp)
                        )
                        Text(
                            text       = categoryLabel(cat).uppercase(),
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFF666666)
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                }

                items(catLayers, key = { it.layer.id }) { model ->
                    LayerRowItem(
                        model              = model,
                        onToggleVisibility = { onToggleVisibility(model.layer.id) },
                        onDownload         = { onDownload(model.layer.id) },
                        onDelete           = { onDelete(model.layer.id) }
                    )
                }
            }

            if (layers.isEmpty() && !isLoading) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Layers,
                                contentDescription = null,
                                tint     = Color(0xFFCCCCCC),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text  = "No layers available.\nCheck your connection.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFAAAAAA),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

/* ─────────────────────────── Map Loading Overlay ─────────────────────────── */

@Composable
private fun MapLoadingOverlay() {
    // Infinite pulse animation on the logo scale
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon_only),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(96.dp)
                .scale(pulse)
        )
    }
}


/* ─────────────────────────── Shared Tabs ─────────────────────────── */

@Composable
private fun MapTabs(
    currentTab: MapTab,
    onTabChange: (MapTab) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(SearchBarBg, RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MapTab.entries.forEach { tab ->
            val selected = tab == currentTab
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onTabChange(tab) },
                color = if (selected) Color.White else Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = tab.display,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}