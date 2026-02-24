package com.mobiledev.mindaguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobiledev.mindaguard.theme.MindaGuardTheme

data class AlertItem(
    val title: String,
    val location: String,
    val description: String,
    val time: String
)

@Composable
fun AlertScreen(
    alerts: List<AlertItem> = sampleAlerts,
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
    ) {
        // Top bar with back button
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
            Text(
                text = "Alert Updates",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(alerts) { alert ->
                AlertCard(alert)
            }
        }
    }
}

@Composable
private fun AlertCard(alert: AlertItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = alert.title, fontWeight = FontWeight.SemiBold)
            Text(text = alert.location, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(text = alert.description, style = MaterialTheme.typography.bodySmall)
            Text(text = alert.time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

private val sampleAlerts = listOf(
    AlertItem("Flood Warning at Quimpo St.", "Quimpo St., Davao City", "Heavy rain causing flash floods.", "Today • 7:30 PM"),
    AlertItem("Flood Warning at UM Matina", "UM Matina, Davao City", "Heavy rains, possible river overflow.", "Today • 6:45 PM")
)

@Preview(showBackground = true)
@Composable
private fun AlertScreenPreview() {
    MindaGuardTheme(darkTheme = false) {
        AlertScreen()
    }
}