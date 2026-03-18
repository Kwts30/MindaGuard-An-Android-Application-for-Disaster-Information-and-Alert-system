package com.mobiledev.mindaguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledev.mindaguard.theme.MindaGuardTheme
import com.mobiledev.mindaguard.theme.ScreenBackground
import com.mobiledev.mindaguard.theme.EmergencyBlue
import com.mobiledev.mindaguard.theme.CriticalRed
import com.mobiledev.mindaguard.data.EmergencyContact
import com.mobiledev.mindaguard.data.defaultEmergencyContacts

@Composable
fun EmergencyScreen(
    contacts: List<EmergencyContact> = defaultEmergencyContacts,
    onCallClick: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmergencyBlue)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Emergency Hotlines",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Quick access to emergency services",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Alert Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "In Case of Life-Threatening Emergency",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5D4037)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Always call the nearest emergency service or local authorities immediately. Every second counts in an emergency.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6D4C41),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Emergency Contacts Title
            Text(
                text = "Emergency Services",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1E21),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Emergency Contacts List
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                contacts.forEach { contact ->
                    EmergencyCardDesign(
                        contact = contact,
                        onCallClick = { onCallClick(contact.number) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Additional Info Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Important Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoItem("Stay calm and provide accurate information to the operator")
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoItem("Tell them your location clearly")
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoItem("Describe the emergency situation briefly")
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoItem("Follow the operator's instructions carefully")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EmergencyCardDesign(
    contact: EmergencyContact,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCallClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Service Icon
            Icon(
                imageVector = contact.icon,
                contentDescription = contact.agency,
                modifier = Modifier.size(40.dp),
                tint = EmergencyBlue
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Contact Information
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = contact.agency,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1E21)
                )
                Text(
                    text = contact.number,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CriticalRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = contact.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Call Button
            Button(
                onClick = onCallClick,
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CriticalRed
                ),
                shape = RoundedCornerShape(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Call",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun InfoItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "[*]",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = EmergencyBlue
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF555555),
            lineHeight = 16.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmergencyScreenPreview() {
    MindaGuardTheme(darkTheme = false) {
        EmergencyScreen()
    }
}