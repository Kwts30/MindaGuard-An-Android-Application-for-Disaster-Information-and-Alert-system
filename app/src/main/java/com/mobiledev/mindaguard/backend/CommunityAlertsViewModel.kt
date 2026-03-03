package com.mobiledev.mindaguard.backend

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.mobiledev.mindaguard.ui.screens.CommunityReport

data class AlertFeedItem(
    val title: String,
    val location: String,
    val description: String,
    val timeLabel: String = "Just now",
    val latitude: Double = 7.0644,
    val longitude: Double = 125.6079,
    val isUserSubmitted: Boolean = false
)

class CommunityAlertsViewModel : ViewModel() {

    val alerts = mutableStateListOf(
        AlertFeedItem(
            title = "FLOOD WARNING AT QUIMPO ST.",
            location = "Quimpo St., Davao City",
            description = "Heavy Rain, Causing flash floods. Residents near low-lying areas are advised to evacuate immediately.",
            timeLabel = "Just Now",
            latitude = 7.0694,
            longitude = 125.6083
        ),
        AlertFeedItem(
            title = "FLOOD WARNING AT UM MATINA",
            location = "UM Matina, Davao City",
            description = "Heavy rains, possible river overflow. Flooding reported near Matina Crossing.",
            timeLabel = "Just Now",
            latitude = 7.0510,
            longitude = 125.5862
        )
    )

    // Only alerts submitted by the current user this session
    val myReports = mutableStateListOf<AlertFeedItem>()

    fun addReport(report: CommunityReport) {
        val item = AlertFeedItem(
            title = report.title.uppercase(),
            location = report.locationLabel.ifBlank { "Davao City" },
            description = report.description,
            timeLabel = "Just Now",
            latitude = report.latitude,
            longitude = report.longitude,
            isUserSubmitted = true
        )
        alerts.add(0, item)
        myReports.add(0, item)
    }
}



