package com.mobiledev.mindaguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledev.mindaguard.backend.AlertFeedItem
import com.mobiledev.mindaguard.theme.*

/**
 * Shared bottom-sheet detail content for alerts.
 * Used by AlertScreen (AlertDetailSheet) and MyReportsScreen (MyReportDetailSheet).
 *
 * @param alert          the alert to display
 * @param badgeContent   optional composable shown above the title row (e.g. "Your Report" badge)
 * @param timePrefix     prefix for the time label (e.g. "Reported:" or "Submitted:")
 */
@Composable
fun AlertDetailSheetContent(
    alert: AlertFeedItem,
    badgeContent: @Composable (() -> Unit)? = null,
    timePrefix: String = "Reported:"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(BorderGray, RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        // Optional badge (e.g. "Your Report")
        badgeContent?.invoke()

        // ── Verified banner ───────────────────────────────────────────────────
        if (alert.isVerified) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VerifiedBadge(compact = false)
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFF8E1)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⏳", fontSize = 14.sp)
                    Text(
                        text = "Pending admin verification — treat with caution.",
                        fontSize = 11.sp,
                        color = Color(0xFFF57F17)
                    )
                }
            }
        }

        // Badge + title
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = CircleShape, color = LightRedBg, modifier = Modifier.size(44.dp)) {
                Icon(
                    Icons.Default.Warning, contentDescription = null,
                    tint = RedWarning,
                    modifier = Modifier.padding(10.dp).fillMaxSize()
                )
            }
            Column {
                Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText)
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BlueLink, modifier = Modifier.size(14.dp))
                    Text(alert.location, fontSize = 12.sp, color = BlueLink)
                }
            }
        }

        HorizontalDivider(color = DividerColor)

        // Description
        Text("Description", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MutedLabel)
        Text(alert.description, style = MaterialTheme.typography.bodyMedium, color = DarkText, lineHeight = 22.sp)

        HorizontalDivider(color = DividerColor)


        // Time + reporter
        Text(
            text = "$timePrefix ${alert.timeLabel}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        if (alert.submittedByName.isNotBlank()) {
            Text(
                text = "Reported by: ${alert.submittedByName}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF555555)
            )
        }
    }
}

