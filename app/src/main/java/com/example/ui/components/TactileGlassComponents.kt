package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.UiStyleMode
import com.example.ui.theme.LocalAmoledDarkFlag
import com.example.ui.theme.LocalDarkThemeFlag
import com.example.ui.theme.LocalUiStyleMode

@Composable
fun TactileAmbientBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val uiStyle = LocalUiStyleMode.current
    val isDark = LocalDarkThemeFlag.current
    val isAmoled = LocalAmoledDarkFlag.current
    val baseBg = MaterialTheme.colorScheme.background
    val primaryGlow = MaterialTheme.colorScheme.primary
    val secondaryGlow = MaterialTheme.colorScheme.secondary
    val tertiaryGlow = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBg)
            .drawBehind {
                if (uiStyle == UiStyleMode.FROSTED_GLASS_OS && !isAmoled) {
                    val alphaMul = if (isDark) 0.22f else 0.16f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryGlow.copy(alpha = alphaMul), Color.Transparent),
                            center = Offset(size.width * 0.15f, size.height * 0.12f),
                            radius = size.maxDimension * 0.55f
                        ),
                        radius = size.maxDimension * 0.55f,
                        center = Offset(size.width * 0.15f, size.height * 0.12f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(secondaryGlow.copy(alpha = alphaMul * 0.9f), Color.Transparent),
                            center = Offset(size.width * 0.88f, size.height * 0.48f),
                            radius = size.maxDimension * 0.50f
                        ),
                        radius = size.maxDimension * 0.50f,
                        center = Offset(size.width * 0.88f, size.height * 0.48f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(tertiaryGlow.copy(alpha = alphaMul * 0.7f), Color.Transparent),
                            center = Offset(size.width * 0.28f, size.height * 0.88f),
                            radius = size.maxDimension * 0.45f
                        ),
                        radius = size.maxDimension * 0.45f,
                        center = Offset(size.width * 0.28f, size.height * 0.88f)
                    )
                }
            }
    ) {
        content()
    }
}

@Composable
fun glassSpecularBorder(accent: Color = MaterialTheme.colorScheme.primary): BorderStroke? {
    val uiStyle = LocalUiStyleMode.current
    if (uiStyle != UiStyleMode.FROSTED_GLASS_OS) return null
    val isDark = LocalDarkThemeFlag.current
    val highlight = if (isDark) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.78f)
    val mid = accent.copy(alpha = if (isDark) 0.24f else 0.20f)
    val rim = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
    return BorderStroke(
        width = 1.dp,
        brush = Brush.linearGradient(colors = listOf(highlight, mid, rim))
    )
}

@Composable
fun TactileSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    glassTintAlpha: Float? = null,
    tonalElevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val uiStyle = LocalUiStyleMode.current
    val isDark = LocalDarkThemeFlag.current
    val isGlass = uiStyle == UiStyleMode.FROSTED_GLASS_OS
    val resolvedColor = if (isGlass) {
        val alpha = glassTintAlpha ?: if (isDark) 0.70f else 0.78f
        containerColor.copy(alpha = alpha)
    } else {
        containerColor
    }
    Surface(
        color = resolvedColor,
        shape = shape,
        border = glassSpecularBorder(),
        tonalElevation = if (isGlass) 0.dp else tonalElevation,
        shadowElevation = if (isGlass) 6.dp else 0.dp,
        modifier = modifier,
        content = content
    )
}

data class DockDestinationSpec(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun FrostedFloatingNavDock(
    destinations: List<DockDestinationSpec>,
    currentRoute: String,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalDarkThemeFlag.current
    val glassBg = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f)
    } else {
        Color.White.copy(alpha = 0.80f)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = glassBg,
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = if (isDark) 0.28f else 0.90f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.26f),
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
                    )
                )
            ),
            shadowElevation = 14.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bottom_navigation_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                destinations.forEach { dest ->
                    val selected = currentRoute == dest.route
                    val pillColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.88f else 0.92f)
                        } else {
                            Color.Transparent
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dock_pill"
                    )
                    val iconTint by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        label = "dock_tint"
                    )
                    val padH by animateDpAsState(
                        targetValue = if (selected) 14.dp else 10.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dock_pad"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(pillColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { onSelectRoute(dest.route) }
                            )
                            .padding(horizontal = padH, vertical = 6.dp)
                            .testTag("nav_${dest.route}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.label,
                                tint = iconTint,
                                modifier = Modifier.size(21.dp)
                            )
                            Text(
                                text = dest.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = iconTint,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FrostedFloatingNavRail(
    destinations: List<DockDestinationSpec>,
    currentRoute: String,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalDarkThemeFlag.current
    val glassBg = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.76f)
    } else {
        Color.White.copy(alpha = 0.80f)
    }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Surface(
            color = glassBg,
            shape = RoundedCornerShape(30.dp),
            border = glassSpecularBorder(),
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxHeight()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BackdropLogoMark(size = 34.dp)
                Spacer(modifier = Modifier.height(8.dp))
                destinations.forEach { dest ->
                    val selected = currentRoute == dest.route
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                                else Color.Transparent
                            )
                            .clickable { onSelectRoute(dest.route) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                            contentDescription = dest.label,
                            tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dest.label,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FrostedGlassStudioPillFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalDarkThemeFlag.current
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.84f else 0.88f),
        shape = CircleShape,
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = if (isDark) 0.40f else 0.92f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                )
            )
        ),
        shadowElevation = 10.dp,
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "AI Studio",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
