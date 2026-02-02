package com.mobiledev.mindaguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.mobiledev.mindaguard.ui.components.CriticalFacilities
import com.mobiledev.mindaguard.ui.components.EvacCenters
import com.mobiledev.mindaguard.ui.components.LocationListItem
import com.mobiledev.mindaguard.ui.components.MapLocation
import kotlinx.coroutines.delay

/* ----------------------------- MAP TABS & HAZARD ENUMS ----------------------------- */

enum class MapTab(val display: String) {
    EVACUATION("Evacuation Centers"),
    HAZARD("Hazard Map"),
    CRITICAL("Critical Facilities")
}

enum class StormSurgePeriod(val label: String) {
    YEAR_5("5 years"),
    YEAR_25("25 years"),
    YEAR_100("100 years")
}

/* ----------------------------- MAIN SCREEN ----------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBackClick: () -> Unit = {}
) {
    // Tabs
    var currentTab by remember { mutableStateOf(MapTab.EVACUATION) }

    // Search query (used in Evacuation & Critical tabs)
    var searchQuery by remember { mutableStateOf("") }

    // Hazard toggles
    var showFaultLine by remember { mutableStateOf(false) }
    var showStormSurge by remember { mutableStateOf(false) }
    var showLandslide by remember { mutableStateOf(false) }
    var stormSurgePeriod by remember { mutableStateOf(StormSurgePeriod.YEAR_25) }

    // Simulated map loading state
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Simulate loading time (e.g., fetching tiles)
        delay(1500) // 1.5 seconds
        isLoading = false
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
                MapTab.EVACUATION -> EvacuationSheetContent(
                    currentTab = currentTab,
                    onTabChange = { tab ->
                        currentTab = tab
                        searchQuery = ""
                    },
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    locations = EvacCenters,
                    onLocationClick = { /* TODO: move map later */ }
                )

                MapTab.HAZARD -> HazardSheetContent(
                    currentTab = currentTab,
                    onTabChange = { currentTab = it },
                    showFaultLine = showFaultLine,
                    onToggleFault = { showFaultLine = it },
                    showStormSurge = showStormSurge,
                    onToggleStorm = { showStormSurge = it },
                    showLandslide = showLandslide,
                    onToggleLandslide = { showLandslide = it },
                    stormSurgePeriod = stormSurgePeriod,
                    onStormSurgePeriodChange = { stormSurgePeriod = it }
                )

                MapTab.CRITICAL -> CriticalSheetContent(
                    currentTab = currentTab,
                    onTabChange = { tab ->
                        currentTab = tab
                        searchQuery = ""
                    },
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    locations = CriticalFacilities,
                    onLocationClick = { /* TODO: move map later */ }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Full-screen map background (placeholder color for now)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2F3B52)) // replace with real map later
            )

            // Loading overlay on top of the map
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Loading map…",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Top bar – padded below status bar / notch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onBackClick() },
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Evacuation/Hazard Map",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(36.dp)) // right spacer
            }
        }
    }
}

/* ----------------------------- Evacuation Sheet ----------------------------- */

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
        locations
            .filter { loc ->
                loc.name.contains(lowered, ignoreCase = true) ||
                        loc.address.contains(lowered, ignoreCase = true)
            }
            .minByOrNull { loc ->
                val nameIndex = loc.name.lowercase().indexOf(lowered).let { if (it == -1) Int.MAX_VALUE else it }
                val addressIndex = loc.address.lowercase().indexOf(lowered).let { if (it == -1) Int.MAX_VALUE else it }
                minOf(nameIndex, addressIndex)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        MapTabs(currentTab = currentTab, onTabChange = onTabChange)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Evacuation Centers",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            color = Color(0xFFF1F1F1),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search"
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search evacuation center") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
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

        // Results area: single source of truth, scrollable and keyboard-safe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding() // push content above the on-screen keyboard
                .heightIn(min = 0.dp, max = 260.dp) // constrain height so it can scroll
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (searchQuery.isBlank()) {
                    items(locations, key = { it.id }) { loc ->
                        LocationListItem(
                            location = loc,
                            onClick = { onLocationClick(loc) }
                        )
                    }
                } else {
                    if (bestMatch != null) {
                        item {
                            LocationListItem(
                                location = bestMatch,
                                onClick = { onLocationClick(bestMatch) }
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = "No matching evacuation center found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ----------------------------- Hazard Sheet ----------------------------- */

@Composable
private fun HazardSheetContent(
    currentTab: MapTab,
    onTabChange: (MapTab) -> Unit,
    showFaultLine: Boolean,
    onToggleFault: (Boolean) -> Unit,
    showStormSurge: Boolean,
    onToggleStorm: (Boolean) -> Unit,
    showLandslide: Boolean,
    onToggleLandslide: (Boolean) -> Unit,
    stormSurgePeriod: StormSurgePeriod,
    onStormSurgePeriodChange: (StormSurgePeriod) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        MapTabs(currentTab = currentTab, onTabChange = onTabChange)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Hazard Map",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = showFaultLine,
                onClick = { onToggleFault(!showFaultLine) },
                label = { Text("Fault Line") }
            )
            FilterChip(
                selected = showStormSurge,
                onClick = { onToggleStorm(!showStormSurge) },
                label = { Text("Storm Surge") }
            )
            FilterChip(
                selected = showLandslide,
                onClick = { onToggleLandslide(!showLandslide) },
                label = { Text("Landslide") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showStormSurge) {
            Text(
                text = "Storm Surge Period",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StormSurgeChip(
                    text = "5 years",
                    selected = stormSurgePeriod == StormSurgePeriod.YEAR_5,
                    onClick = { onStormSurgePeriodChange(StormSurgePeriod.YEAR_5) }
                )
                StormSurgeChip(
                    text = "25 years",
                    selected = stormSurgePeriod == StormSurgePeriod.YEAR_25,
                    onClick = { onStormSurgePeriodChange(StormSurgePeriod.YEAR_25) }
                )
                StormSurgeChip(
                    text = "100 years",
                    selected = stormSurgePeriod == StormSurgePeriod.YEAR_100,
                    onClick = { onStormSurgePeriodChange(StormSurgePeriod.YEAR_100) }
                )
            }
        }
    }
}

@Composable
private fun StormSurgeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color(0xFFF1F1F1)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Black,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

/* ----------------------------- Critical Facilities Sheet ----------------------------- */

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
        locations
            .filter { loc ->
                loc.name.contains(lowered, ignoreCase = true) ||
                        loc.address.contains(lowered, ignoreCase = true)
            }
            .minByOrNull { loc ->
                val nameIndex = loc.name.lowercase().indexOf(lowered).let { if (it == -1) Int.MAX_VALUE else it }
                val addressIndex = loc.address.lowercase().indexOf(lowered).let { if (it == -1) Int.MAX_VALUE else it }
                minOf(nameIndex, addressIndex)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        MapTabs(currentTab = currentTab, onTabChange = onTabChange)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Critical Facilities",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            color = Color(0xFFF1F1F1),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search"
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search hospital or health center") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
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

        // Results area: single source of truth, scrollable and keyboard-safe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .heightIn(min = 0.dp, max = 260.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (searchQuery.isBlank()) {
                    items(locations, key = { it.id }) { loc ->
                        LocationListItem(
                            location = loc,
                            onClick = { onLocationClick(loc) }
                        )
                    }
                } else {
                    if (bestMatch != null) {
                        item {
                            LocationListItem(
                                location = bestMatch,
                                onClick = { onLocationClick(bestMatch) }
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = "No matching facility found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ----------------------------- Shared Tabs ----------------------------- */

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
        MapTab.values().forEach { tab ->
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
                        text = when (tab) {
                            MapTab.EVACUATION -> "Evacuation Centers"
                            MapTab.HAZARD -> "Hazard Map"
                            MapTab.CRITICAL -> "Critical Facilities"
                        },
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