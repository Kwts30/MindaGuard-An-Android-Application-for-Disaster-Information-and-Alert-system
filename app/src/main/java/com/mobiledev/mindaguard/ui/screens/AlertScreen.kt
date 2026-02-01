package com.mobiledev.mindaguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    alerts: List<AlertItem> = sampleAlerts
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Alert Updates",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alerts) { alert ->
                    AlertCard(alert)
                }
            }
        }
    }
}

@Composable
private fun AlertCard(alert: AlertItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = alert.title, fontWeight = FontWeight.SemiBold)
            Text(
                text = alert.location,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = alert.time,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

private val sampleAlerts = listOf(
    AlertItem(
        title = "Flood Warning at Quimpo St.",
        location = "Quimpo St., Davao City",
        description = "Heavy rain causing flash floods.",
        time = "Today • 7:30 PM"
    ),
    AlertItem(
        title = "Flood Warning at UM Matina",
        location = "UM Matina, Davao City",
        description = "Heavy rains, possible river overflow.",
        time = "Today • 6:45 PM"
    )
)

@Preview(showBackground = true)
@Composable
private fun AlertScreenPreview() {
    MindaGuardTheme(darkTheme = false) {
        AlertScreen()
    }
}