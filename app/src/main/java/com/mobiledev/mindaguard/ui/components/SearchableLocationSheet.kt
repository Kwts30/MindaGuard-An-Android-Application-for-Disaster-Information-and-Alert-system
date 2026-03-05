package com.mobiledev.mindaguard.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobiledev.mindaguard.theme.SearchBarBg

/**
 * Shared searchable location list used by both EvacuationSheetContent and CriticalSheetContent.
 * Extracts the duplicated search bar + best-match logic into a single composable.
 */
@Composable
fun SearchableLocationSheet(
    title: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    searchPlaceholder: String,
    locations: List<MapLocation>,
    pinColor: Color,
    emptyMessage: String,
    onLocationClick: (MapLocation) -> Unit,
    tabsContent: @Composable () -> Unit
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
        tabsContent()
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        // Search bar
        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            color = SearchBarBg,
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
                    placeholder = { Text(searchPlaceholder) },
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

        // Location list
        Box(modifier = Modifier.fillMaxWidth().imePadding().heightIn(min = 0.dp, max = 260.dp)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (searchQuery.isBlank()) {
                    items(locations, key = { it.id }) { loc ->
                        LocationListItem(location = loc, pinColor = pinColor, onClick = { onLocationClick(loc) })
                    }
                } else {
                    if (bestMatch != null) {
                        item { LocationListItem(location = bestMatch, pinColor = pinColor, onClick = { onLocationClick(bestMatch) }) }
                    } else {
                        item { Text(emptyMessage, style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
                    }
                }
            }
        }
    }
}



