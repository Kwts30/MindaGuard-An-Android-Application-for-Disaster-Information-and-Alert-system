package com.mobiledev.mindaguard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledev.mindaguard.R
import com.mobiledev.mindaguard.backend.AlertFeedItem
import com.mobiledev.mindaguard.theme.MindaGuardTheme
import com.mobiledev.mindaguard.ui.components.MapLibreMapView
import com.mobiledev.mindaguard.ui.components.SATELLITE_STYLE_JSON
import java.time.LocalTime

val Color.Companion.mindaGuardGreen: Color
    get() = Color(0xFF00BFA5)

enum class MainDestination { HOME, MAP, MENU }

// ── Main page ─────────────────────────────────────────────────────────────────

@Composable
fun MainPageScreen(
    currentTime: LocalTime = LocalTime.now(),
    alerts: List<AlertFeedItem> = emptyList(),
    onAlertClick: () -> Unit = {},
    onEmergencyClick: () -> Unit = {},
    onMapClick: () -> Unit = {}
) {
    val isNight     = currentTime.hour >= 18 || currentTime.hour < 6
    val isAfternoon = currentTime.hour in 12..17
    val greetingText = when {
        currentTime.hour in 5..11  -> "Good Morning!"
        currentTime.hour in 12..17 -> "Good Afternoon!"
        else                       -> "Good Evening!"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        HomeContent(
            greetingText    = greetingText,
            isAfternoon     = isAfternoon,
            isNight         = isNight,
            alerts          = alerts,
            onAlertClick    = onAlertClick,
            onEmergencyClick = onEmergencyClick,
            onMapClick      = onMapClick
        )
    }
}

// ── Home content ──────────────────────────────────────────────────────────────

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    greetingText: String,
    isAfternoon: Boolean,
    isNight: Boolean,
    alerts: List<AlertFeedItem>,
    onAlertClick: () -> Unit,
    onEmergencyClick: () -> Unit,
    onMapClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item { TopBar() }

        item {
            HeroCard(
                greetingText = greetingText,
                isAfternoon  = isAfternoon,
                isNight      = isNight
            )
        }

        item {
            AlertButtonsRow(
                onAlertClick     = onAlertClick,
                onEmergencyClick = onEmergencyClick
            )
        }

        // ── Mini map tile ─────────────────────────────────────────────────────
        item {
            MiniMapTile(onClick = onMapClick)
        }

        // ── Community Alerts header ───────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMMUNITY ALERTS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1E21),
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ── Alert cards ───────────────────────────────────────────────────────
        items(alerts) { alert ->
            CommunityAlertItem(alert = alert)
        }

        if (alerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No alerts yet. Be the first to report!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.icon_only),
            contentDescription = "MindaGuard logo",
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.icon_text),
            contentDescription = "MindaGuard text only",
            modifier = Modifier
                .height(20.dp)
                .align(Alignment.CenterVertically),
            contentScale = ContentScale.Fit
        )
    }
}

// ── Hero card ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroCard(greetingText: String, isAfternoon: Boolean, isNight: Boolean) {
    val heroResId = when {
        isNight     -> R.drawable.home_night
        isAfternoon -> R.drawable.home_afternoon
        else        -> R.drawable.home_morning
    }
    val textColor = if (!isAfternoon && !isNight) Color.Black else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(24.dp))
            .paint(painter = painterResource(id = heroResId), contentScale = ContentScale.Crop)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = greetingText, color = textColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = "Stay Safe and Alert Today.", color = textColor, fontSize = 14.sp)
        }
    }
}

// ── Alert & Emergency buttons ─────────────────────────────────────────────────

@Composable
private fun AlertButtonsRow(onAlertClick: () -> Unit, onEmergencyClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            modifier = Modifier.weight(1f).height(56.dp),
            onClick = onAlertClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(8.dp))
            Text("ALERT\nUpdates", color = Color.White, fontWeight = FontWeight.SemiBold, lineHeight = 14.sp)
        }
        Button(
            modifier = Modifier.weight(1f).height(56.dp),
            onClick = onEmergencyClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF029BE5))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(8.dp))
                Text("EMERGENCY\nHotlines", color = Color.White, fontWeight = FontWeight.SemiBold, lineHeight = 14.sp)
            }
        }
    }
}

// ── Mini map tile ─────────────────────────────────────────────────────────────

@Composable
private fun MiniMapTile(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        MapLibreMapView(
            modifier = Modifier.fillMaxSize(),
            layers = emptyList(),
            mapStyleJson = SATELLITE_STYLE_JSON,
            showEvacPins = false,
            showCritPins = false,
            initialLat = 7.0644,
            initialLng = 125.6079,
            initialZoom = 13.0
        )

        // "School / University" label chip — mimics the screenshot
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White.copy(alpha = 0.90f),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF029BE5),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Davao City",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1C1E21),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Tap overlay hint at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.25f))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Tap to open full map",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Community alert card ──────────────────────────────────────────────────────

@Composable
private fun CommunityAlertItem(alert: AlertFeedItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Red warning icon
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFEBEE),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = alert.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF1C1E21)
                )
                Text(
                    text = alert.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(text = alert.timeLabel, fontSize = 11.sp, color = Color.Gray)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "Read More",
                        fontSize = 11.sp,
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

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