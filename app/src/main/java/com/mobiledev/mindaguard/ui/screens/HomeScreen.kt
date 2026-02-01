package com.mobiledev.mindaguard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledev.mindaguard.R
import com.mobiledev.mindaguard.theme.MindaGuardTheme
import java.time.LocalTime

// ---- Models ----

data class CommunityAlert(
    val title: String,
    val location: String,
    val description: String
)

val Color.Companion.mindaGuardGreen: Color
    get() = Color(0xFF00BFA5)

enum class MainDestination {
    HOME, MAP, MENU
}

// ---- Main page (light mode only, no bottom nav) ----

@Composable
fun MainPageScreen(
    currentTime: LocalTime = LocalTime.now(),
    onAlertClick: () -> Unit = {},
    onEmergencyClick: () -> Unit = {}
) {
    val isNight = currentTime.hour >= 18 || currentTime.hour < 6
    val isAfternoon = currentTime.hour in 12..17
    val greetingText = when {
        currentTime.hour in 5..11 -> "Good Morning!"
        currentTime.hour in 12..17 -> "Good Afternoon!"
        else -> "Good Evening!"
    }

    val alerts = remember {
        listOf(
            CommunityAlert(
                title = "Flood Warning at Quimpo St.",
                location = "Quimpo St., Davao City",
                description = "Heavy rain causing flash floods."
            ),
            CommunityAlert(
                title = "Flood Warning at UM Matina",
                location = "UM Matina, Davao City",
                description = "Heavy rains, possible river overflow."
            )
        )
    }

    // Always light background now
    val backgroundColor = Color(0xFFF5F6FA)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        HomeContent(
            greetingText = greetingText,
            isAfternoon = isAfternoon,
            isNight = isNight,
            alerts = alerts,
            onAlertClick = onAlertClick,
            onEmergencyClick = onEmergencyClick
        )
    }
}

// ---- Content of the HOME tab ----

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    greetingText: String,
    isAfternoon: Boolean,
    isNight: Boolean,
    alerts: List<CommunityAlert>,
    onAlertClick: () -> Unit,
    onEmergencyClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBar()

        HeroCard(
            greetingText = greetingText,
            isAfternoon = isAfternoon,
            isNight = isNight
        )

        AlertButtonsRow(
            onAlertClick = onAlertClick,
            onEmergencyClick = onEmergencyClick
        )

        Text(
            text = "Community Alerts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(alerts) { alert ->
                CommunityAlertItem(alert = alert)
            }
        }
    }
}

// ---- Top bar (logo centered) ----

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 35.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.icon_only),
            contentDescription = "MindaGuard logo",
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.icon_text),
            contentDescription = "MindaGuard text only",
            modifier = Modifier
                .height(20.dp)
                .align(Alignment.CenterVertically),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
    }
}

// ---- Hero image card with greeting ----

@Composable
private fun HeroCard(
    greetingText: String,
    isAfternoon: Boolean,
    isNight: Boolean
) {
    val heroResId = when {
        isNight -> R.drawable.home_night
        isAfternoon -> R.drawable.home_afternoon
        else -> R.drawable.home_morning
    }

    val textColor = if (!isAfternoon && !isNight) Color.Black else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(24.dp))
            .paint(
                painter = androidx.compose.ui.res.painterResource(id = heroResId),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = greetingText,
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Stay Safe and Alert Today.",
                color = textColor,
                fontSize = 14.sp
            )
        }
    }
}

// ---- Alert & Hotline buttons ----

@Composable
private fun AlertButtonsRow(
    onAlertClick: () -> Unit,
    onEmergencyClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            onClick = onAlertClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF7043)
            )
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                contentDescription = "Alert",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ALERT\nUpdates",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 14.sp
            )
        }

        Button(
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            onClick = onEmergencyClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF029BE5)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Phone,
                    contentDescription = "Phone",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EMERGENCY\nHotlines",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

// ---- Community alert list item ----

@Composable
private fun CommunityAlertItem(alert: CommunityAlert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = alert.title,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = alert.location,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = alert.description,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---- Previews (light only) ----

@Preview(showBackground = true, name = "Main Page - Morning")
@Composable
fun PreviewMainPageMorning() {
    MindaGuardTheme(darkTheme = false) {
        MainPageScreen(currentTime = LocalTime.of(8, 0))
    }
}

@Preview(showBackground = true, name = "Main Page - Afternoon")
@Composable
fun PreviewMainPageAfternoon() {
    MindaGuardTheme(darkTheme = false) {
        MainPageScreen(currentTime = LocalTime.of(15, 0))
    }
}

@Preview(showBackground = true, name = "Main Page - Night")
@Composable
fun PreviewMainPageNight() {
    MindaGuardTheme(darkTheme = false) {
        MainPageScreen(currentTime = LocalTime.of(21, 0))
    }
}