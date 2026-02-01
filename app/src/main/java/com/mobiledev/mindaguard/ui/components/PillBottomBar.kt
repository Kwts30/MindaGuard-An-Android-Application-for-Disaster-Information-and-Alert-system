package com.mobiledev.mindaguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class PillTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun PillBottomBar(
    currentRoute: String?,
    tabs: List<PillTab>,
    onSelectTab: (route: String) -> Unit,
    modifier: Modifier = Modifier,
    barHeight: Dp = 60.dp,
    pillColor: Color = Color(0xFFE7E7E7),
    selectedTint: Color = MaterialTheme.colorScheme.primary,
    unselectedTint: Color = Color.Black
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(30.dp))
                .background(pillColor),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val tabModifier = Modifier
                .weight(1f)
                .fillMaxHeight()

            tabs.forEach { tab ->
                val isSelected = currentRoute == tab.route
                val tint = if (isSelected) selectedTint else unselectedTint

                Box(
                    modifier = tabModifier
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onSelectTab(tab.route) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(selectedTint.copy(alpha = 0.18f))
                        )
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) selectedTint else tint,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            color = tint,
                            style = MaterialTheme.typography.labelSmall,
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

/* Previews */

private val previewTabs = listOf(
    PillTab(route = "home", label = "Home", icon = Icons.Outlined.Home),
    PillTab(route = "map", label = "Map", icon = Icons.Outlined.Place),
    PillTab(route = "menu", label = "Menu", icon = Icons.Outlined.Menu)
)

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2, name = "PillBottomBar - Light")
@Composable
private fun PillBottomBarPreview_Light() {
    MaterialTheme {
        Surface {
            PillBottomBar(
                currentRoute = "home",
                tabs = previewTabs,
                onSelectTab = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}