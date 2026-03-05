package com.mobiledev.mindaguard.data

/**
 * Emergency contact data, extracted from EmergencyScreen.
 */
data class EmergencyContact(
    val agency: String,
    val number: String,
    val description: String
)

val defaultEmergencyContacts: List<EmergencyContact> = listOf(
    EmergencyContact(
        agency = "Local DRRMO",
        number = "123-4567",
        description = "Disaster response and rescue"
    ),
    EmergencyContact(
        agency = "Fire Department",
        number = "160",
        description = "Fire and rescue emergencies"
    ),
    EmergencyContact(
        agency = "Police",
        number = "166",
        description = "Police assistance"
    )
)

