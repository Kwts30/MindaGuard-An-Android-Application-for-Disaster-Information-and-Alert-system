package com.mobiledev.mindaguard.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
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
import com.mobiledev.mindaguard.theme.*
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
    isRefreshing: Boolean = false,
    listenerError: String? = null,
    onRefresh: () -> Unit = {},
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
            .background(ScreenBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        HomeContent(
            greetingText     = greetingText,
            isAfternoon      = isAfternoon,
            isNight          = isNight,
            alerts           = alerts,
            isRefreshing     = isRefreshing,
            listenerError    = listenerError,
            onRefresh        = onRefresh,
            onAlertClick     = onAlertClick,
            onEmergencyClick = onEmergencyClick,
            onMapClick       = onMapClick
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
    isRefreshing: Boolean,
    listenerError: String?,
    onRefresh: () -> Unit,
    onAlertClick: () -> Unit,
    onEmergencyClick: () -> Unit,
    onMapClick: () -> Unit
) {
    // Spinning animation for the refresh icon
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing)),
        label = "spin"
    )

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

        // Hazard Map label + mini map tile
        item {
            Text(
                text = "Hazard Map",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
            Spacer(modifier = Modifier.height(8.dp))
            MiniMapTile(onClick = onMapClick)
        }

        // Community Alerts header with refresh button
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
                    color = DarkText,
                    letterSpacing = 0.5.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Refresh icon button — spins while loading
                    IconButton(
                        onClick  = onRefresh,
                        enabled  = !isRefreshing,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh alerts",
                            tint = if (isRefreshing) OrangeButton else Color.Gray,
                            modifier = Modifier
                                .size(18.dp)
                                .then(if (isRefreshing) Modifier.rotate(rotation) else Modifier)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go to Alert Updates",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(onClick = onAlertClick)
                    )
                }
            }
        }

        // Firestore error banner — shows when the listener fails
        if (listenerError != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚠ $listenerError",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB71C1C),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onRefresh) {
                            Text("Retry", color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Refreshing indicator row
        if (isRefreshing) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = OrangeButton
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Refreshing alerts…",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }

        // Alert cards
        items(alerts) { alert ->
            CommunityAlertItem(alert = alert, onReadMoreClick = onAlertClick)
        }

        if (alerts.isEmpty() && !isRefreshing) {
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
            colors = ButtonDefaults.buttonColors(containerColor = OrangeButton)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(8.dp))
            Text("ALERT\nUpdates", color = Color.White, fontWeight = FontWeight.SemiBold, lineHeight = 14.sp)
        }
        Button(
            modifier = Modifier.weight(1f).height(56.dp),
            onClick = onEmergencyClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmergencyBlue)
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
    ) {
        // Frozen map — no touch gestures, zoomed out to show Central Davao
        MapLibreMapView(
            modifier = Modifier.fillMaxSize(),
            layers = emptyList(),
            mapStyleJson = SATELLITE_STYLE_JSON,
            showEvacPins = false,
            showCritPins = false,
            frozen = true,
            initialLat = 7.0700,
            initialLng = 125.6120,
            initialZoom = 11.5
        )

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

        // Transparent click-interceptor overlay — sits on top of AndroidView
        // so taps are captured by Compose before MapLibre can consume them
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        )
    }
}

// ── Community alert card ──────────────────────────────────────────────────────

@Composable
private fun CommunityAlertItem(alert: AlertFeedItem, onReadMoreClick: () -> Unit = {}) {
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
                color = LightRedBg,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = RedWarning,
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
                    color = DarkText
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
                    Row(
                        modifier = Modifier.clickable(onClick = onReadMoreClick),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Read More",
                            fontSize = 11.sp,
                            color = BlueLink,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Go to Alert Updates",
                            tint = BlueLink,
                            modifier = Modifier.size(14.dp)
                        )
                    }
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