package com.mobiledev.mindaguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobiledev.mindaguard.backend.AlertFeedItem
import com.mobiledev.mindaguard.theme.*
import com.mobiledev.mindaguard.ui.components.AlertDetailSheetContent
import com.mobiledev.mindaguard.ui.components.AlertItemCard

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
            .background(ScreenBackground)
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
                    Icon(Icons.Default.Warning, contentDescription = null, tint = BorderGray, modifier = Modifier.size(64.dp))
                    Text("You haven't submitted any alerts yet.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text("Use 'Create Alert' to report an incident.", style = MaterialTheme.typography.labelSmall, color = PlaceholderGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
            ) {
                items(reports, key = { it.id }) { report ->
                    AlertItemCard(
                        alert = report,
                        onAction = { selectedAlert = report },
                        iconBgColor = LightGreenBg,
                        iconTint = GreenSuccess,
                        actionLabel = "View Details"
                    )
                }
            }
        }
    }

    // Detail bottom sheet
    if (selectedAlert != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedAlert = null },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AlertDetailSheetContent(
                alert = selectedAlert!!,
                timePrefix = "Submitted:",
                badgeContent = {
                    Surface(shape = RoundedCornerShape(50), color = LightGreenBg) {
                        Text(
                            text = "✅  Your Report",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyReportsScreenPreview() {
    MindaGuardTheme(darkTheme = false) {
        MyReportsScreen(
            reports = listOf(
                AlertFeedItem(title = "ROAD BLOCKED AT QUIMPO", location = "Quimpo Blvd, Davao City",
                    description = "Large tree fell blocking the road.", timeLabel = "Just Now",
                    latitude = 7.0694, longitude = 125.6083, isUserSubmitted = true)
            )
        )
    }
}
