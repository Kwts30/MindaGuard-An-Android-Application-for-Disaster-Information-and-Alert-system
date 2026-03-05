package com.mobiledev.mindaguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledev.mindaguard.backend.AlertFeedItem
import com.mobiledev.mindaguard.backend.CommunityAlertsViewModel
import com.mobiledev.mindaguard.theme.*
import com.mobiledev.mindaguard.ui.components.AlertDetailSheetContent
import com.mobiledev.mindaguard.ui.components.VerifiedBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAlertsScreen(
    viewModel: CommunityAlertsViewModel,
    onBackClick: () -> Unit = {}
) {
    val alerts      = viewModel.alerts
    val verifyError by viewModel.verifyError.collectAsState()
    val deleteError by viewModel.deleteError.collectAsState()

    var selectedAlert by remember { mutableStateOf<AlertFeedItem?>(null) }
    var alertToDelete by remember { mutableStateOf<AlertFeedItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showOnlyUnverified by remember { mutableStateOf(false) }
    val displayed = if (showOnlyUnverified) alerts.filter { !it.isVerified } else alerts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Admin — Alert Moderation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${alerts.count { it.isVerified }} verified · ${alerts.count { !it.isVerified }} unverified",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !showOnlyUnverified,
                onClick  = { showOnlyUnverified = false },
                label    = { Text("All (${alerts.size})") }
            )
            FilterChip(
                selected = showOnlyUnverified,
                onClick  = { showOnlyUnverified = true },
                label    = { Text("Unverified (${alerts.count { !it.isVerified }})") },
                leadingIcon = {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )
        }

        // Verify error banner
        if (verifyError != null) {
            LaunchedEffect(verifyError) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearVerifyError()
            }
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFEBEE)
            ) {
                Text("⚠ $verifyError", modifier = Modifier.padding(12.dp),
                    color = Color(0xFFB71C1C), style = MaterialTheme.typography.bodySmall)
            }
        }

        // Delete error banner
        if (deleteError != null) {
            LaunchedEffect(deleteError) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearDeleteError()
            }
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFEBEE)
            ) {
                Text("⚠ $deleteError", modifier = Modifier.padding(12.dp),
                    color = Color(0xFFB71C1C), style = MaterialTheme.typography.bodySmall)
            }
        }

        // Alert list
        if (displayed.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No alerts to moderate.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayed, key = { it.id }) { alert ->
                    AdminAlertCard(
                        alert      = alert,
                        onVerify   = { viewModel.verifyAlert(alert.id, true) },
                        onUnverify = { viewModel.verifyAlert(alert.id, false) },
                        onDelete   = { alertToDelete = alert },
                        onTap      = { selectedAlert = alert }
                    )
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }

    // Delete confirmation dialog
    if (alertToDelete != null) {
        AlertDialog(
            onDismissRequest = { alertToDelete = null },
            icon  = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF5350)) },
            title = { Text("Delete Alert?", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "\"${alertToDelete!!.title}\" will be permanently removed as misinformation. " +
                    "This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAlert(alertToDelete!!.id)
                        if (selectedAlert?.id == alertToDelete!!.id) selectedAlert = null
                        alertToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { alertToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Detail bottom sheet
    if (selectedAlert != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedAlert = null },
            sheetState       = sheetState,
            containerColor   = Color.White,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AlertDetailSheetContent(alert = selectedAlert!!, timePrefix = "Reported:")
            val a = selectedAlert!!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (a.isVerified) viewModel.verifyAlert(a.id, false)
                        else viewModel.verifyAlert(a.id, true)
                        selectedAlert = null
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (a.isVerified) Color(0xFFEF5350) else Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        if (a.isVerified) Icons.Default.RemoveCircle else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (a.isVerified) "Unverify" else "Verify")
                }
                Button(
                    onClick = { alertToDelete = a; selectedAlert = null },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Delete")
                }
            }
        }
    }
}

// ── Admin alert card ──────────────────────────────────────────────────────────

@Composable
private fun AdminAlertCard(
    alert: AlertFeedItem,
    onVerify: () -> Unit,
    onUnverify: () -> Unit,
    onDelete: () -> Unit,
    onTap: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (alert.isVerified) Color(0xFFF1FBF1) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick   = onTap
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {

            // Title + verified badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    alert.title,
                    fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    color = DarkText,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (alert.isVerified) VerifiedBadge()
            }

            Text(
                alert.description,
                fontSize = 12.sp, color = Color.Gray,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )

            // Time + submitter + hazard type
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(alert.timeLabel, fontSize = 11.sp, color = SubtleGray, modifier = Modifier.weight(1f))
                if (alert.submittedByName.isNotBlank()) {
                    Text(
                        "\uD83D\uDC64 ${alert.submittedByName}",
                        fontSize = 10.sp,
                        color = Color(0xFF555555)
                    )
                }
                if (alert.hazardType.isNotBlank()) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFF3E0)) {
                        Text(
                            alert.hazardType,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 10.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (!alert.isVerified) {
                    Button(
                        onClick  = onVerify,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Verify", fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick  = onUnverify,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape    = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.RemoveCircle, contentDescription = null,
                            modifier = Modifier.size(14.dp), tint = Color(0xFFEF5350))
                        Spacer(Modifier.width(4.dp))
                        Text("Unverify", fontSize = 12.sp, color = Color(0xFFEF5350))
                    }
                }
                OutlinedButton(
                    onClick  = onTap,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape    = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Details", fontSize = 12.sp)
                }
                Button(
                    onClick  = onDelete,
                    modifier = Modifier.height(36.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
