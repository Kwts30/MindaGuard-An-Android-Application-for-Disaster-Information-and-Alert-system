package com.mobiledev.mindaguard.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobiledev.mindaguard.backend.LayerDownloadState
import com.mobiledev.mindaguard.backend.MapLayerUiModel
import com.mobiledev.mindaguard.backend.MapLayerViewModel
import com.mobiledev.mindaguard.ui.components.CriticalFacilities
import com.mobiledev.mindaguard.ui.components.EvacCenters
import com.mobiledev.mindaguard.ui.components.LocationListItem
import com.mobiledev.mindaguard.ui.components.MapLibreMapView
import com.mobiledev.mindaguard.ui.components.MapLocation
import com.mobiledev.mindaguard.ui.components.SATELLITE_STYLE_JSON
import com.mobiledev.mindaguard.ui.components.CLASSIC_STYLE_JSON

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
    var flyToLocation by remember { mutableStateOf<com.mobiledev.mindaguard.ui.components.MapLocation?>(null) }

    // Hold a reference to the live MapLibreMap for the compass reset
    var mapInstance by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }

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
                MapTab.EVACUATION -> EvacuationSheetContent(
                    currentTab = currentTab,
                    onTabChange = { tab -> currentTab = tab; searchQuery = "" },
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    locations = EvacCenters,
                    onLocationClick = { loc -> flyToLocation = loc }
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
                MapTab.CRITICAL -> CriticalSheetContent(
                    currentTab = currentTab,
                    onTabChange = { tab -> currentTab = tab; searchQuery = "" },
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    locations = CriticalFacilities,
                    onLocationClick = { loc -> flyToLocation = loc }
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
                onMapReady = { map -> mapInstance = map }
            )

            // ── Error snackbar ───────────────────────────────────────────
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
    val isDownloaded = model.downloadState is LayerDownloadState.Downloaded
    val isDownloading = model.downloadState is LayerDownloadState.Downloading
    val progress = (model.downloadState as? LayerDownloadState.Downloading)?.progress ?: 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = isDownloaded) { onToggleVisibility() }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colour swatch
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(
                    runCatching {
                        Color(android.graphics.Color.parseColor(model.layer.color))
                    }.getOrDefault(Color(0xFFFF5722))
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.layer.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isDownloading) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .padding(top = 2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color(0xFFE0E0E0)
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        when {
            isDownloading -> {
                // show spinner only, no tap action
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            }
            isDownloaded -> {
                // visibility toggle checkbox
                Checkbox(
                    checked = model.isVisible,
                    onCheckedChange = { onToggleVisibility() },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                // delete cached file
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove layer",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFE53935)
                    )
                }
            }
            else -> {
                // download button
                IconButton(onClick = onDownload, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download layer",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/* ─────────────────────────── Hazard Sheet (bottom sheet tab) ─────────────────────────── */

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
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Hazard Layers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                TextButton(onClick = onRefresh, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Refresh", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Download layers to view them on the map. Tap the checkbox to toggle visibility.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))

        val grouped = layers.groupBy { it.layer.category }
        val categoryOrder = listOf("flood", "storm_surge", "earthquake", "landslide")
        val categoryLabels = mapOf(
            "flood"        to "🌊  Flood",
            "storm_surge"  to "🌀  Storm Surge",
            "earthquake"   to "⚡  Earthquake Faults",
            "landslide"    to "🏔  Landslide"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (categoryOrder + grouped.keys.filter { it !in categoryOrder }).forEach { cat ->
                val catLayers = grouped[cat] ?: return@forEach
                item {
                    Text(
                        text = categoryLabels[cat] ?: cat,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF444444),
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }
                items(catLayers, key = { it.layer.id }) { model ->
                    LayerRowItem(
                        model = model,
                        onToggleVisibility = { onToggleVisibility(model.layer.id) },
                        onDownload = { onDownload(model.layer.id) },
                        onDelete = { onDelete(model.layer.id) }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                }
            }

            if (layers.isEmpty() && !isLoading) {
                item {
                    Text(
                        text = "No layers found. Check your internet connection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}

/* ─────────────────────────── Evacuation Sheet ─────────────────────────── */

@Composable
private fun EvacuationSheetContent(
    currentTab: MapTab,
    onTabChange: (MapTab) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    locations: List<MapLocation>,
    onLocationClick: (MapLocation) -> Unit
) {
    val bestMatch: MapLocation? = remember(searchQuery, locations) {
        if (searchQuery.isBlank()) return@remember null
        val lowered = searchQuery.trim().lowercase()
        locations.filter {
            it.name.contains(lowered, ignoreCase = true) ||
                    it.address.contains(lowered, ignoreCase = true)
        }.minByOrNull { loc ->
            val ni = loc.name.lowercase().indexOf(lowered).let { if (it == -1) Int.MAX_VALUE else it }
            val ai = loc.address.lowercase().indexOf(lowered).let { if (it == -1) Int.MAX_VALUE else it }
            minOf(ni, ai)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        MapTabs(currentTab = currentTab, onTabChange = onTabChange)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Evacuation Centers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            color = Color(0xFFF1F1F1),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                Icon(Icons.Outlined.Search, contentDescription = "Search")
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search evacuation center") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray
                    ),
                    singleLine = true,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().imePadding().heightIn(min = 0.dp, max = 260.dp)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (searchQuery.isBlank()) {
                    items(locations, key = { it.id }) { loc ->
                        LocationListItem(location = loc, pinColor = Color(0xFF2E7D32), onClick = { onLocationClick(loc) })
                    }
                } else {
                    if (bestMatch != null) {
                        item { LocationListItem(location = bestMatch, pinColor = Color(0xFF2E7D32), onClick = { onLocationClick(bestMatch) }) }
                    } else {
                        item { Text("No matching evacuation center found", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
                    }
                }
            }
        }
    }
}

/* ─────────────────────────── Critical Facilities Sheet ─────────────────────────── */

@Composable
private fun CriticalSheetContent(
    currentTab: MapTab,
    onTabChange: (MapTab) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    locations: List<MapLocation>,
    onLocationClick: (MapLocation) -> Unit
) {
    val bestMatch: MapLocation? = remember(searchQuery, locations) {
        if (searchQuery.isBlank()) return@remember null
        val lowered = searchQuery.trim().lowercase()
        locations.filter {
            it.name.contains(lowered, ignoreCase = true) ||
                    it.address.contains(lowered, ignoreCase = true)
        }.minByOrNull { loc ->
            val ni = loc.name.lowercase().indexOf(lowered).let { if (it == -1) Int.MAX_VALUE else it }
            val ai = loc.address.lowercase().indexOf(lowered).let { if (it == -1) Int.MAX_VALUE else it }
            minOf(ni, ai)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        MapTabs(currentTab = currentTab, onTabChange = onTabChange)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Critical Facilities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            color = Color(0xFFF1F1F1),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                Icon(Icons.Outlined.Search, contentDescription = "Search")
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search hospital or health center") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray
                    ),
                    singleLine = true,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().imePadding().heightIn(min = 0.dp, max = 260.dp)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (searchQuery.isBlank()) {
                    items(locations, key = { it.id }) { loc ->
                        LocationListItem(location = loc, pinColor = Color(0xFFC62828), onClick = { onLocationClick(loc) })
                    }
                } else {
                    if (bestMatch != null) {
                        item { LocationListItem(location = bestMatch, pinColor = Color(0xFFC62828), onClick = { onLocationClick(bestMatch) }) }
                    } else {
                        item { Text("No matching facility found", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
                    }
                }
            }
        }
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
            .background(Color(0xFFF1F1F1), RoundedCornerShape(24.dp))
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