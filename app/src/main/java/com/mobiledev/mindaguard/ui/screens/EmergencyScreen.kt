package com.mobiledev.mindaguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobiledev.mindaguard.theme.MindaGuardTheme

data class EmergencyContact(
    val agency: String,
    val number: String,
    val description: String
)

@Composable
fun EmergencyScreen(
    contacts: List<EmergencyContact> = sampleContacts
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
                text = "Emergency Hotlines",
                style = MaterialTheme.typography.headlineSmall
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts) { contact ->
                    EmergencyCard(contact)
                }
            }
        }
    }
}

@Composable
private fun EmergencyCard(contact: EmergencyContact) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Phone",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(text = contact.agency, style = MaterialTheme.typography.bodyLarge)
                Text(text = contact.number, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                Text(text = contact.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

private val sampleContacts = listOf(
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

@Preview(showBackground = true)
@Composable
private fun EmergencyScreenPreview() {
    MindaGuardTheme(darkTheme = false) {
        EmergencyScreen()
    }
}