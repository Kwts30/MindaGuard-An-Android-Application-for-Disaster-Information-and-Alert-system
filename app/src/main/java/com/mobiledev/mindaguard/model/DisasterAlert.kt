package com.mobiledev.mindaguard.model

import java.time.LocalDateTime

// ── Hazard type enum ─────────────────────────────────────────────────────────
enum class HazardType {
    FLOOD,
    LANDSLIDE,
    EARTHQUAKE,
    STORM_SURGE,
    TYPHOON
}

// ── Core data class ──────────────────────────────────────────────────────────
data class DisasterAlert(
    val id: String,
    val title: String,
    val location: String,
    val description: String,
    val timestamp: LocalDateTime,
    val hazardType: HazardType
)

// ── Mock data – Davao City localized ─────────────────────────────────────────
@Suppress("unused")
val mockDavaoAlerts: List<DisasterAlert> = listOf(
    DisasterAlert(
        id = "alert_001",
        title = "Flood Warning at Quimpo St.",
        location = "Quimpo St., Poblacion District, Davao City",
        description = "Heavy rain causing flash floods. Residents near low-lying areas are advised to evacuate immediately.",
        timestamp = LocalDateTime.now().minusMinutes(30),
        hazardType = HazardType.FLOOD
    ),
    DisasterAlert(
        id = "alert_002",
        title = "Flood Warning at UM Matina",
        location = "UM Matina, Matina, Davao City",
        description = "Heavy rains, possible river overflow. Flooding reported near Matina Crossing.",
        timestamp = LocalDateTime.now().minusMinutes(75),
        hazardType = HazardType.FLOOD
    ),
    DisasterAlert(
        id = "alert_003",
        title = "Landslide Alert – Mt. Apo Slopes",
        location = "Calinan District, Davao City",
        description = "Sustained rainfall has destabilized slopes along the Mt. Apo foothills. Avoid mountain roads.",
        timestamp = LocalDateTime.now().minusHours(2),
        hazardType = HazardType.LANDSLIDE
    ),
    DisasterAlert(
        id = "alert_004",
        title = "Storm Surge Watch – Davao Gulf",
        location = "Sasa & Panacan Coastal Barangays, Davao City",
        description = "PAGASA has issued a storm surge watch for coastal barangays along Davao Gulf. Wave heights may reach 1.5 m.",
        timestamp = LocalDateTime.now().minusHours(3),
        hazardType = HazardType.STORM_SURGE
    ),
    DisasterAlert(
        id = "alert_005",
        title = "Typhoon Advisory – Davao Region",
        location = "Davao City Metro Area",
        description = "Typhoon signal No. 1 raised over Davao Region. Expect strong winds and heavy rains in the next 36 hours.",
        timestamp = LocalDateTime.now().minusHours(5),
        hazardType = HazardType.TYPHOON
    ),
    DisasterAlert(
        id = "alert_006",
        title = "Seismic Activity – Magnitude 4.2",
        location = "Toril District, Davao City",
        description = "A magnitude 4.2 earthquake was felt in Toril and nearby areas. No tsunami threat. Inspect structures for damage.",
        timestamp = LocalDateTime.now().minusHours(8),
        hazardType = HazardType.EARTHQUAKE
    )
)



