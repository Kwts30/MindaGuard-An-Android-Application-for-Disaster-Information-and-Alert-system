package com.mobiledev.mindaguard.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledev.mindaguard.backend.AlertFeedItem
import com.mobiledev.mindaguard.theme.*

@Composable
fun AlertItemCard(
    alert: AlertFeedItem,
    onAction: () -> Unit,
    iconBgColor: Color = LightRedBg,
    iconTint: Color = RedWarning,
    actionLabel: String = "Read More"
) {
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
            Surface(shape = CircleShape, color = iconBgColor, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.Warning, contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.padding(9.dp).fillMaxSize()
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {

                // Title row + status badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        alert.title, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                        maxLines = 2, overflow = TextOverflow.Ellipsis, color = DarkText,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (alert.isVerified) {
                        VerifiedBadge()
                    } else if (alert.isUserSubmitted) {
                        PendingBadge()
                    }
                }

                Text(
                    alert.description, fontSize = 12.sp, color = Color.Gray,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn, contentDescription = null,
                        tint = if (actionLabel == "View Details") BlueLink else Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        alert.location, fontSize = 11.sp,
                        color = if (actionLabel == "View Details") BlueLink else Color.Gray,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(alert.timeLabel, fontSize = 11.sp, color = SubtleGray)
                    TextButton(onClick = onAction, contentPadding = PaddingValues(0.dp)) {
                        Text(actionLabel, fontSize = 11.sp, color = BlueLink, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Reusable unverified badge chip ───────────────────────────────────────────

@Composable
fun PendingBadge() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF8E1)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                Icons.Default.HourglassEmpty,
                contentDescription = "Unverified",
                tint = Color(0xFFF57F17),
                modifier = Modifier.size(11.dp)
            )
            Text(
                "Unverified",
                fontSize = 10.sp,
                color = Color(0xFFF57F17),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun VerifiedBadge(compact: Boolean = true) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFE8F5E9)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 6.dp else 10.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Verified",
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(if (compact) 11.dp else 14.dp)
            )
            Text(
                "Verified",
                fontSize = if (compact) 10.sp else 11.sp,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

