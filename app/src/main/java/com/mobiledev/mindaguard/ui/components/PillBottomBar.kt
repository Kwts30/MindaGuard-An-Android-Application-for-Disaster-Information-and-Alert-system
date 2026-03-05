package com.mobiledev.mindaguard.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PillTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private const val ANIM_DURATION = 220

// Spring spec used for scale/offset — gives a subtle bounce feel
private val tabSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness    = Spring.StiffnessMedium
)

/**
 * Floating pill-style bottom nav bar.
 * Selected tab gets a solid filled inner pill with animated color, scale, and icon transitions.
 */
@Composable
fun PillBottomBar(
    currentRoute: String?,
    tabs: List<PillTab>,
    onSelectTab: (route: String) -> Unit,
    modifier: Modifier = Modifier,
    barHeight: Dp = 64.dp,
    pillColor: Color = Color(0xF0FFFFFF),
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = Color.White,
    unselectedTint: Color = Color(0xFF555555)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp)
            .padding(bottom = 12.dp)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(barHeight),
            shape = RoundedCornerShape(50.dp),
            color = pillColor,
            shadowElevation = 12.dp,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val isSelected = currentRoute == tab.route
                    val interactionSource = remember { MutableInteractionSource() }

                    // ── Animated background color of the inner pill ───────────
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) selectedColor else Color.Transparent,
                        animationSpec = tween(
                            durationMillis = ANIM_DURATION,
                            easing = FastOutSlowInEasing
                        ),
                        label = "tab_bg_${tab.route}"
                    )

                    // ── Animated icon + label tint ────────────────────────────
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) selectedContentColor else unselectedTint,
                        animationSpec = tween(
                            durationMillis = ANIM_DURATION,
                            easing = FastOutSlowInEasing
                        ),
                        label = "tab_tint_${tab.route}"
                    )

                    // ── Scale bounce: selected tab pops up slightly ───────────
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.10f else 1.0f,
                        animationSpec = tabSpring,
                        label = "tab_scale_${tab.route}"
                    )

                    // ── Icon vertical lift: selected icon nudges up ───────────
                    val iconOffsetY by animateFloatAsState(
                        targetValue = if (isSelected) -2f else 0f,
                        animationSpec = tabSpring,
                        label = "tab_icon_y_${tab.route}"
                    )

                    // ── Font weight driven by animated float ──────────────────
                    val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(44.dp))
                            .background(bgColor)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onSelectTab(tab.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .scale(scale)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = contentColor,
                                modifier = Modifier
                                    .size(22.dp)
                                    .offset(y = iconOffsetY.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.label,
                                color = contentColor,
                                fontSize = 10.sp,
                                fontWeight = fontWeight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ── Preview ─────────────────────────────────────────────────────────────── */

private val previewTabs = listOf(
    PillTab(route = "home",         label = "Home",         icon = Icons.Outlined.Home),
    PillTab(route = "map",          label = "Map",          icon = Icons.Outlined.Place),
    PillTab(route = "create_alert", label = "Create Alert", icon = Icons.Outlined.AddAlert),
    PillTab(route = "menu",         label = "Menu",         icon = Icons.Outlined.Menu)
)

@Preview(showBackground = true, backgroundColor = 0xFF1A6B3C, name = "PillBottomBar")
@Composable
private fun PillBottomBarPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFF1A6B3C))
        ) {
            PillBottomBar(
                currentRoute = "home",
                tabs = previewTabs,
                onSelectTab = {},
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
