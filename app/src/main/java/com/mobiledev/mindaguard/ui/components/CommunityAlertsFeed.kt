package com.mobiledev.mindaguard.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledev.mindaguard.model.DisasterAlert
import com.mobiledev.mindaguard.model.HazardType
import com.mobiledev.mindaguard.model.mockDavaoAlerts
import com.mobiledev.mindaguard.theme.MindaGuardTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ── Colour + label helpers ────────────────────────────────────────────────────

private val HazardType.accentColor: Color
    get() = when (this) {
        HazardType.FLOOD        -> Color(0xFF1565C0)
        HazardType.LANDSLIDE    -> Color(0xFF6D4C41)
        HazardType.EARTHQUAKE   -> Color(0xFF546E7A)
        HazardType.STORM_SURGE  -> Color(0xFF00838F)
        HazardType.TYPHOON      -> Color(0xFF6A1B9A)
    }

private val HazardType.badgeLabel: String
    get() = when (this) {
        HazardType.FLOOD        -> "FLOOD"
        HazardType.LANDSLIDE    -> "LANDSLIDE"
        HazardType.EARTHQUAKE   -> "EARTHQUAKE"
        HazardType.STORM_SURGE  -> "STORM SURGE"
        HazardType.TYPHOON      -> "TYPHOON"
    }

private fun LocalDateTime.toRelativeLabel(): String {
    val now     = LocalDateTime.now()
    val minutes = ChronoUnit.MINUTES.between(this, now)
    val hours   = ChronoUnit.HOURS.between(this, now)
    val days    = ChronoUnit.DAYS.between(this, now)
    return when {
        minutes < 1  -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours   < 24 -> "${hours}h ago"
        days    < 7  -> "${days}d ago"
        else         -> DateTimeFormatter.ofPattern("MMM d, yyyy").format(this)
    }
}

// ── Public feed composable ────────────────────────────────────────────────────

/**
 * Vertically scrollable feed of [DisasterAlert] cards.
 *
 * @param alerts        List of alerts to render.
 * @param onAlertTapped Integration bridge — called when the user taps a card.
 *                      Wire this to the map module so it can pan to the hazard location.
 * @param modifier      Optional modifier applied to the [LazyColumn].
 * @param contentPadding Padding around the list content (e.g. to clear a bottom nav bar).
 */
@Composable
fun CommunityAlertsFeed(
    alerts: List<DisasterAlert>,
    onAlertTapped: (DisasterAlert) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 100.dp)
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = contentPadding
    ) {
        items(items = alerts, key = { it.id }) { alert ->
            DisasterAlertCard(alert = alert, onTapped = { onAlertTapped(alert) })
        }
    }
}

// ── Individual card ───────────────────────────────────────────────────────────

@Composable
private fun DisasterAlertCard(
    alert: DisasterAlert,
    onTapped: () -> Unit
) {
    val accent = alert.hazardType.accentColor

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTapped),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Header: badge + relative timestamp
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HazardBadge(hazardType = alert.hazardType, accentColor = accent)
                Text(
                    text  = alert.timestamp.toRelativeLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9E9E9E)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Title
            Text(
                text       = alert.title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF1C1E21),
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            // Location row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.Warning,
                    contentDescription = null,
                    tint               = accent,
                    modifier           = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text     = alert.location,
                    style    = MaterialTheme.typography.labelMedium,
                    color    = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(6.dp))

            // Description
            Text(
                text       = alert.description,
                style      = MaterialTheme.typography.bodySmall,
                color      = Color(0xFF555555),
                maxLines   = 3,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(10.dp))

            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            Spacer(Modifier.height(6.dp))

            // Tap hint
            Text(
                text  = "Tap to view on map →",
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
        }
    }
}

// ── Hazard badge ──────────────────────────────────────────────────────────────

@Composable
private fun HazardBadge(hazardType: HazardType, accentColor: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = accentColor.copy(alpha = 0.12f)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Default.Warning,
                contentDescription = null,
                tint               = accentColor,
                modifier           = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text       = hazardType.badgeLabel,
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color      = accentColor,
                fontSize   = 10.sp
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun CommunityAlertsFeedPreview() {
    MindaGuardTheme(darkTheme = false) {
        CommunityAlertsFeed(
            alerts        = mockDavaoAlerts,
            onAlertTapped = { /* preview no-op */ },
            modifier      = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}



