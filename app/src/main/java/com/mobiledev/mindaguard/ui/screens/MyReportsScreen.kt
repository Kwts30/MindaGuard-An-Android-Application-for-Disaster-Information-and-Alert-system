package com.mobiledev.mindaguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledev.mindaguard.backend.AlertFeedItem
import com.mobiledev.mindaguard.theme.MindaGuardTheme
import com.mobiledev.mindaguard.ui.components.MapLibreMapView
import com.mobiledev.mindaguard.ui.components.SATELLITE_STYLE_JSON

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    reports: List<AlertFeedItem> = emptyList(),
    onBackClick: () -> Unit = {}
) {
    var selectedAlert by remember { mutableStateOf<AlertFeedItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = "My Reports",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${reports.size} alert${if (reports.size != 1) "s" else ""} submitted",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }

        if (reports.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFDDDDDD),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "You haven't submitted any alerts yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Use 'Create Alert' to report an incident.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFAAAAAA)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
            ) {
                items(reports) { report ->
                    MyReportCard(report = report, onViewDetails = { selectedAlert = report })
                }
            }
        }
    }

    // Detail bottom sheet (reuses same design as AlertScreen)
    if (selectedAlert != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedAlert = null },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            MyReportDetailSheet(alert = selectedAlert!!)
        }
    }
}

// ── Detail sheet ──────────────────────────────────────────────────────────────

@Composable
private fun MyReportDetailSheet(alert: AlertFeedItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .width(40.dp).height(4.dp)
                .background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        // "My Report" badge
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFE8F5E9)
        ) {
            Text(
                text = "✅  Your Report",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = Color(0xFFFFEBEE), modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null,
                    tint = Color(0xFFD32F2F), modifier = Modifier.padding(10.dp).fillMaxSize())
            }
            Column {
                Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1C1E21))
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF1565C0), modifier = Modifier.size(14.dp))
                    Text(alert.location, fontSize = 12.sp, color = Color(0xFF1565C0))
                }
            }
        }

        HorizontalDivider(color = Color(0xFFEEEEEE))

        Text("Description", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Color(0xFF555555))
        Text(alert.description, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1C1E21), lineHeight = 22.sp)

        HorizontalDivider(color = Color(0xFFEEEEEE))

        Text("Pinned Location", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Color(0xFF555555))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F4FF), RoundedCornerShape(10.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF1565C0), modifier = Modifier.size(18.dp))
            Text(
                text = "%.4f, %.4f".format(alert.latitude, alert.longitude),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1565C0),
                fontWeight = FontWeight.Medium
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth().height(200.dp)
                .background(Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
        ) {
            MapLibreMapView(
                modifier = Modifier.fillMaxSize(),
                layers = emptyList(),
                mapStyleJson = SATELLITE_STYLE_JSON,
                showEvacPins = false,
                showCritPins = false,
                initialLat = alert.latitude,
                initialLng = alert.longitude,
                initialZoom = 15.0
            )
            Icon(Icons.Default.LocationOn, null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(36.dp).align(Alignment.Center))
        }

        Text("Submitted: ${alert.timeLabel}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

// ── Report card ───────────────────────────────────────────────────────────────

@Composable
private fun MyReportCard(report: AlertFeedItem, onViewDetails: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(shape = CircleShape, color = Color(0xFFE8F5E9), modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.padding(9.dp).fillMaxSize())
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(report.title, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color(0xFF1C1E21))
                Text(report.description, fontSize = 12.sp, color = Color.Gray,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF1565C0), modifier = Modifier.size(12.dp))
                    Text(report.location, fontSize = 11.sp, color = Color(0xFF1565C0),
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(report.timeLabel, fontSize = 11.sp, color = Color(0xFF9E9E9E))
                    TextButton(onClick = onViewDetails, contentPadding = PaddingValues(0.dp)) {
                        Text("View Details", fontSize = 11.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyReportsScreenPreview() {
    MindaGuardTheme(darkTheme = false) {
        MyReportsScreen(
            reports = listOf(
                AlertFeedItem("ROAD BLOCKED AT QUIMPO", "Quimpo Blvd, Davao City",
                    "Large tree fell blocking the road.", "Just Now", 7.0694, 125.6083, true)
            )
        )
    }
}

