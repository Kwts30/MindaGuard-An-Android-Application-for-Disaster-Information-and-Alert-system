package com.mobiledev.mindaguard.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Shared model for anything we pin on the map
data class MapLocation(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val distanceText: String // e.g. "0.53 km"
)

// Sample / placeholder data for Evacuation Centers
val EvacCenters: List<MapLocation> = listOf(
    MapLocation(
        id = "evac1",
        name = "Ma-a Elementary School",
        address = "Barangay Ma-a, Talomo, Davao City",
        lat = 7.0713,
        lng = 125.6075,
        distanceText = "0.53 km"
    ),
    MapLocation(
        id = "evac2",
        name = "Ma-a Covered Court",
        address = "Barangay Ma-a, Talomo, Davao City",
        lat = 7.0721,
        lng = 125.6090,
        distanceText = "0.82 km"
    )
)

// Sample / placeholder data for Critical Facilities
val CriticalFacilities: List<MapLocation> = listOf(
    MapLocation(
        id = "crit1",
        name = "Barangay Ma-a Health Center",
        address = "Barangay Ma-a, Talomo, Davao City",
        lat = 7.0718,
        lng = 125.6068,
        distanceText = "0.34 km"
    ),
    MapLocation(
        id = "crit2",
        name = "Talomo District Hospital",
        address = "Talomo District, Davao City",
        lat = 7.0690,
        lng = 125.6030,
        distanceText = "1.12 km"
    )
)

/**
 * Reusable list item UI for both Evacuation Centers and Critical Facilities.
 * - Shows name, address, and distance with a small location pin icon.
 */
@Composable
fun LocationListItem(
    location: MapLocation,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = location.address,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Distance",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = location.distanceText,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}