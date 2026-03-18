package com.mobiledev.mindaguard.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.EmergencyShare
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Emergency contact data with icon support
 */
data class EmergencyContact(
    val agency: String,
    val number: String,
    val description: String,
    val icon: ImageVector
)

val defaultEmergencyContacts: List<EmergencyContact> = listOf(
    EmergencyContact(
        agency = "Central Emergency Hotline",
        number = "911",
        description = "Primary emergency response center for Davao City",
        icon = Icons.Filled.Phone
    ),
    EmergencyContact(
        agency = "Davao City Police Office",
        number = "221-6800",
        description = "Police assistance and crime reporting in Davao City",
        icon = Icons.Filled.LocalPolice
    ),
    EmergencyContact(
        agency = "Davao City Fire Department",
        number = "224-3206",
        description = "Fire suppression and rescue services in Davao City",
        icon = Icons.Filled.EmergencyShare
    ),
    EmergencyContact(
        agency = "Davao City Medical Emergency",
        number = "221-7000",
        description = "Ambulance and medical emergency services in Davao City",
        icon = Icons.Filled.LocalHospital
    ),
    EmergencyContact(
        agency = "Davao City DRRMO",
        number = "221-6555",
        description = "Disaster Risk Reduction Management Office for Davao City",
        icon = Icons.Filled.WarningAmber
    ),
    EmergencyContact(
        agency = "Philippine Red Cross - Davao",
        number = "222-1603",
        description = "Humanitarian assistance and disaster response in Davao City",
        icon = Icons.Filled.FavoriteBorder
    )
)

