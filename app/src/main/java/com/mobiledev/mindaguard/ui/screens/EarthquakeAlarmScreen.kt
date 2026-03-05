package com.mobiledev.mindaguard.ui.screens

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledev.mindaguard.R
import com.mobiledev.mindaguard.theme.MindaGuardTheme
import kotlin.math.roundToInt

private val AlarmRed   = Color(0xFFEF5350)
private val AlarmDark  = Color(0xFF1A1A1A)
private val AlarmCard  = Color(0xFF212121)

@Composable
fun EarthquakeAlarmScreen(
    eventName: String = "TEST EARTHQUAKE",
    distanceKm: Float = 42.6f,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current

    // ── Looping alert sound at full volume ────────────────────────────────────
    DisposableEffect(Unit) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Save original volumes so we can restore them on dismiss
        val originalAlarmVol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val originalMusicVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxAlarmVol      = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        val maxMusicVol      = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        // Force both streams to maximum
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM, maxAlarmVol,
            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
        )
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC, maxMusicVol,
            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
        )

        // Create and start the looping player
        val resources = context.resources
        val rawId = resources.getIdentifier("earthquake_alert", "raw", context.packageName)
        val player: MediaPlayer? = if (rawId != 0) {
            MediaPlayer.create(context, rawId)?.apply {
                isLooping = true
                setVolume(1f, 1f)   // MediaPlayer internal volume also at max
                start()
            }
        } else null

        onDispose {
            // Stop & release player
            player?.stop()
            player?.release()
            // Restore original volumes
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM, originalAlarmVol,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
            )
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC, originalMusicVol,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
            )
        }
    }

    // ── Pulsing warning icon animation ────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // ── Swipe-to-dismiss state ────────────────────────────────────────────────
    val trackWidthDp = 280.dp
    val thumbSizeDp  = 52.dp
    val density      = LocalDensity.current
    val maxOffsetPx  = with(density) { (trackWidthDp - thumbSizeDp - 8.dp).toPx() }

    var offsetX    by remember { mutableFloatStateOf(0f) }
    var dismissed  by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue    = offsetX,
        animationSpec  = spring(stiffness = Spring.StiffnessMedium),
        label          = "thumb",
        finishedListener = {
            // Once the snap-to-end animation finishes, trigger the callback
            if (dismissed) onDismiss()
        }
    )
    val progress = (animatedOffset / maxOffsetPx).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AlarmRed)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // Warning icon — pulsing
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alert",
                tint = Color.Black,
                modifier = Modifier
                    .size(72.dp)
                    .scale(pulse)
            )

            Spacer(Modifier.height(20.dp))

            // Event name
            Text(
                text = eventName.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            // Distance
            Text(
                text = "${distanceKm} km Away",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(32.dp))

            // DROP / COVER / HOLD card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AlarmCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    AlarmStep(
                        label = "DROP",
                        iconResId = R.drawable.ic_eq_drop
                    )
                    AlarmDivider()
                    AlarmStep(
                        label = "COVER",
                        iconResId = R.drawable.ic_eq_cover
                    )
                    AlarmDivider()
                    AlarmStep(
                        label = "HOLD",
                        iconResId = R.drawable.ic_eq_hold
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Swipe-to-dismiss slider ───────────────────────────────────
            Box(
                modifier = Modifier
                    .width(trackWidthDp)
                    .height(thumbSizeDp + 8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Track label — fades as thumb advances
                Text(
                    text = "SWIPE TO DISMISS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = (1f - progress).coerceIn(0f, 1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = thumbSizeDp + 8.dp),
                    textAlign = TextAlign.Center
                )

                // Draggable thumb
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .size(thumbSizeDp)
                        .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    // Use raw offsetX (not animated) to check threshold
                                    if (offsetX >= maxOffsetPx * 0.75f) {
                                        // Snap thumb all the way to end, then exit via finishedListener
                                        offsetX   = maxOffsetPx
                                        dismissed = true
                                    } else {
                                        // Not far enough — snap back to start
                                        offsetX = 0f
                                    }
                                },
                                onDragCancel = { offsetX = 0f }
                            ) { _, dragAmount ->
                                offsetX = (offsetX + dragAmount).coerceIn(0f, maxOffsetPx)
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Dismiss",
                            tint = AlarmRed,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AlarmStep(label: String, iconResId: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 2.sp
        )
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = label,
            modifier = Modifier.size(72.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun AlarmDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .height(0.5.dp)
            .background(Color.White.copy(alpha = 0.15f))
    )
}

@Preview(showBackground = true)
@Composable
private fun EarthquakeAlarmPreview() {
    MindaGuardTheme(darkTheme = false) {
        EarthquakeAlarmScreen()
    }
}





