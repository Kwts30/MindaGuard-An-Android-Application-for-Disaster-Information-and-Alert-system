package com.mobiledev.mindaguard.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mobiledev.mindaguard.theme.OrangeButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

// ── Result data class ─────────────────────────────────────────────────────────

data class PickedLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String
)

// ── Triangle shape for bubble pointer ────────────────────────────────────────

private object TriangleShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
        Outline.Generic(Path().apply {
            moveTo(size.width / 2f, size.height)
            lineTo(0f, 0f)
            lineTo(size.width, 0f)
            close()
        })
}

// ── Full-screen Location Picker Dialog ────────────────────────────────────────

@SuppressLint("MissingPermission")
@Composable
fun LocationPickerDialog(
    initialLat: Double = 7.0700,
    initialLng: Double = 125.6120,
    onDismiss: () -> Unit,
    onConfirm: (PickedLocation) -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var centerLat    by remember { mutableDoubleStateOf(initialLat) }
    var centerLng    by remember { mutableDoubleStateOf(initialLng) }
    var mapRef       by remember { mutableStateOf<MapLibreMap?>(null) }
    var isGpsLoading by remember { mutableStateOf(false) }

    // Pin drop animation — bounces when coordinates change
    var pinDropped by remember { mutableStateOf(false) }
    val pinTranslateY by animateFloatAsState(
        targetValue = if (pinDropped) -8f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "pin_y"
    )

    val coordLabel = "%.5f, %.5f".format(centerLat, centerLng)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress      = true,
            dismissOnClickOutside   = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {

            // ── Full interactive map ──────────────────────────────────────────
            MapLibreMapView(
                modifier     = Modifier.fillMaxSize(),
                layers       = emptyList(),
                mapStyleJson = CLASSIC_STYLE_JSON,
                showEvacPins = false,
                showCritPins = false,
                frozen       = false,
                initialLat   = initialLat,
                initialLng   = initialLng,
                initialZoom  = 16.0,
                onMapReady   = { map ->
                    mapRef = map
                    map.addOnCameraIdleListener {
                        val target = map.cameraPosition.target
                        if (target != null) {
                            centerLat  = target.latitude
                            centerLng  = target.longitude
                            pinDropped = true
                        }
                    }
                }
            )

            // ── Fixed center pin + "Your address is here" bubble ─────────────
            Box(
                modifier        = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(y = (-36).dp + pinTranslateY.dp)
                ) {
                    // Bubble label
                    Surface(
                        shape        = RoundedCornerShape(20.dp),
                        color        = OrangeButton,
                        shadowElevation = 6.dp
                    ) {
                        Text(
                            text     = "Your address is here",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            color    = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                    // Downward triangle pointer
                    Box(
                        modifier = Modifier
                            .size(width = 14.dp, height = 8.dp)
                            .background(color = OrangeButton, shape = TriangleShape)
                    )
                    // Pin icon
                    Icon(
                        imageVector     = Icons.Default.LocationOn,
                        contentDescription = "Pin",
                        tint            = OrangeButton,
                        modifier        = Modifier.size(36.dp)
                    )
                }
            }

            // ── Top bar ───────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Back row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick  = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector     = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint            = OrangeButton
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text     = "Pin Location",
                        style    = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color    = Color.Black,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.88f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                // Coordinates card
                Surface(
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(12.dp),
                    color           = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector     = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint            = OrangeButton,
                            modifier        = Modifier.size(20.dp)
                        )
                        Text(
                            text  = coordLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF333333),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── GPS recenter FAB ──────────────────────────────────────────────
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        isGpsLoading = true
                        try {
                            val client = LocationServices.getFusedLocationProviderClient(context)
                            val loc    = client.getCurrentLocation(
                                Priority.PRIORITY_HIGH_ACCURACY, null
                            ).await()
                            if (loc != null) {
                                centerLat = loc.latitude
                                centerLng = loc.longitude
                                mapRef?.animateCamera(
                                    org.maplibre.android.camera.CameraUpdateFactory
                                        .newLatLngZoom(LatLng(loc.latitude, loc.longitude), 17.0),
                                    800
                                )
                            }
                        } catch (_: Exception) {
                        } finally {
                            isGpsLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 100.dp)
                    .size(46.dp),
                containerColor = Color.White,
                contentColor   = OrangeButton,
                shape          = CircleShape,
                elevation      = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                if (isGpsLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = OrangeButton,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector     = Icons.Default.MyLocation,
                        contentDescription = "My location",
                        modifier        = Modifier.size(22.dp)
                    )
                }
            }

            // ── Confirm button ────────────────────────────────────────────────
            Button(
                onClick = {
                    onConfirm(
                        PickedLocation(
                            latitude  = centerLat,
                            longitude = centerLng,
                            label     = coordLabel
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(54.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeButton)
            ) {
                Text(
                    text       = "Confirm",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )
            }
        }
    }
}


