package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.model.UiStyleMode
import com.example.ui.theme.AppMotion
import com.example.ui.theme.LocalAmoledDarkFlag
import com.example.ui.theme.LocalDarkThemeFlag
import com.example.ui.theme.LocalGlassDepthIntensity
import com.example.ui.theme.LocalReducedMotionFlag
import com.example.ui.theme.LocalUiStyleMode
import com.example.ui.theme.rememberStudioHaptics
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Pure slot geometry helper for the draggable Liquid Glass bottom navigation pill.
 */
object LiquidNavPillMath {
    fun slotForPosition(xPx: Float, totalWidthPx: Float, slotCount: Int): Int {
        if (slotCount <= 1 || totalWidthPx <= 0f) return 0
        val slotW = totalWidthPx / slotCount.toFloat()
        return (xPx / slotW).toInt().coerceIn(0, slotCount - 1)
    }

    fun resolveReleaseRoute(startIndex: Int, releaseIndex: Int, routes: List<String>): String? {
        if (routes.isEmpty()) return null
        val safeRelease = releaseIndex.coerceIn(0, routes.lastIndex)
        return if (safeRelease != startIndex) routes[safeRelease] else null
    }
}

@Composable
fun TactileAmbientBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val uiStyle = LocalUiStyleMode.current
    val isDark = LocalDarkThemeFlag.current
    val isAmoled = LocalAmoledDarkFlag.current
    val glassDepth = LocalGlassDepthIntensity.current
    val baseBg = MaterialTheme.colorScheme.background
    val primaryGlow = MaterialTheme.colorScheme.primary
    val secondaryGlow = MaterialTheme.colorScheme.secondary
    val tertiaryGlow = MaterialTheme.colorScheme.tertiary

    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(baseBg)
                .drawBehind {
                    if (uiStyle == UiStyleMode.FROSTED_GLASS_OS && !isAmoled) {
                        val baseAlpha = if (isDark) 0.18f else 0.16f
                        val alphaMul = (baseAlpha * glassDepth).coerceIn(0.08f, 0.28f)
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
}

@Composable
fun glassSpecularBorder(accent: Color = MaterialTheme.colorScheme.primary): BorderStroke? {
    val uiStyle = LocalUiStyleMode.current
    if (uiStyle != UiStyleMode.FROSTED_GLASS_OS) return null
    val isDark = LocalDarkThemeFlag.current
    val glassDepth = LocalGlassDepthIntensity.current
    val highlight = if (isDark) {
        Color.White.copy(alpha = (0.38f * glassDepth).coerceIn(0.24f, 0.52f))
    } else {
        Color.White.copy(alpha = 0.92f)
    }
    val mid = accent.copy(alpha = if (isDark) 0.44f else 0.28f)
    val rim = if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.10f)
    return BorderStroke(
        width = 1.15.dp,
        brush = Brush.linearGradient(colors = listOf(highlight, mid, rim))
    )
}

@Composable
fun TactileSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = Color.Unspecified,
    glassTintAlpha: Float? = null,
    tonalElevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val uiStyle = LocalUiStyleMode.current
    val isDark = LocalDarkThemeFlag.current
    val isAmoled = LocalAmoledDarkFlag.current
    val glassDepth = LocalGlassDepthIntensity.current
    val isGlass = uiStyle == UiStyleMode.FROSTED_GLASS_OS

    val resolvedColor = if (isGlass) {
        val defaultAlpha = when {
            isAmoled -> 0.93f
            isDark -> 0.89f
            else -> 0.84f
        }
        val tunedAlpha = glassTintAlpha ?: (defaultAlpha + (glassDepth - 1f) * 0.08f)
        containerColor.copy(alpha = tunedAlpha.coerceIn(if (isDark) 0.82f else 0.68f, 0.97f))
    } else {
        containerColor
    }
    val resolvedContentColor = contentColor.takeOrElse {
        MaterialTheme.colorScheme.contentColorFor(containerColor)
            .takeOrElse { MaterialTheme.colorScheme.onSurface }
    }

    Surface(
        color = resolvedColor,
        contentColor = resolvedContentColor,
        shape = shape,
        border = glassSpecularBorder(),
        tonalElevation = if (isGlass) 0.dp else tonalElevation,
        shadowElevation = if (isGlass) 8.dp else 0.dp,
        modifier = modifier,
        content = content
    )
}

/**
 * Tactile circular HUD button adapting cleanly between Liquid Glass translucent pills
 * and Material 3 tonal buttons with high-contrast Light/Dark/AMOLED visibility.
 */
@Composable
fun TactileHudIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    active: Boolean = false
) {
    val uiStyle = LocalUiStyleMode.current
    val reducedMotion = LocalReducedMotionFlag.current
    val isGlass = uiStyle == UiStyleMode.FROSTED_GLASS_OS
    val isDark = LocalDarkThemeFlag.current

    val scale by animateFloatAsState(
        targetValue = if (active) 1.07f else 1f,
        animationSpec = AppMotion.spatialSpring(uiStyle, reducedMotion),
        label = "hud_btn_scale"
    )
    val resolvedBg = when {
        active -> MaterialTheme.colorScheme.primary.copy(alpha = if (isGlass) 0.92f else 1f)
        isGlass -> containerColor.copy(alpha = if (isDark) 0.84f else 0.86f)
        else -> containerColor
    }
    val resolvedTint = if (active) MaterialTheme.colorScheme.onPrimary else contentColor

    Surface(
        color = resolvedBg,
        contentColor = resolvedTint,
        shape = CircleShape,
        border = glassSpecularBorder(),
        tonalElevation = if (isGlass) 0.dp else 3.dp,
        shadowElevation = if (isGlass) 6.dp else 0.dp,
        modifier = modifier
            .size(48.dp)
            .scale(scale)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = resolvedTint
            )
        }
    }
}

/**
 * Wide sliding-capsule segmented tab bar for Studio workspaces.
 * Material 3 renders a clean Pixel tonal indicator; Liquid Glass uses a fluid specular glass lens.
 */
@Composable
fun <T> StudioSegmentedBar(
    options: List<T>,
    selected: T,
    labelProvider: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    tagPrefix: String = "studio_tab"
) {
    val uiStyle = LocalUiStyleMode.current
    val reducedMotion = LocalReducedMotionFlag.current
    val haptics = rememberStudioHaptics()
    val isGlass = uiStyle == UiStyleMode.FROSTED_GLASS_OS
    val isDark = LocalDarkThemeFlag.current

    val trackColor = if (isGlass) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = if (isDark) 0.88f else 0.80f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)
    val count = options.size.coerceAtLeast(1)
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = AppMotion.spatialSpring(uiStyle, reducedMotion),
        label = "seg_slide_idx"
    )

    Surface(
        color = trackColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(50),
        border = if (isGlass) glassSpecularBorder() else null,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp)
        ) {
            val density = LocalDensity.current
            val totalW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
            val slotW = totalW / count.toFloat()
            val capsuleX = (animatedIndex * slotW).coerceIn(0f, (totalW - slotW).coerceAtLeast(0f))
            val capsuleWidthDp = with(density) { slotW.toDp() }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset { IntOffset(x = capsuleX.roundToInt(), y = 0) }
                    .width(capsuleWidthDp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        brush = if (isGlass) {
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.96f),
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (isDark) 0.86f else 0.90f)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        }
                    )
                    .let { mod ->
                        if (isGlass) {
                            mod.border(
                                width = 1.6.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = if (isDark) 0.56f else 0.95f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
                                    )
                                ),
                                shape = RoundedCornerShape(50)
                            )
                        } else {
                            mod
                        }
                    }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                options.forEach { option ->
                    val isSelected = option == selected
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            if (isGlass) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        animationSpec = AppMotion.colorSpec(uiStyle, reducedMotion),
                        label = "seg_fg"
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = {
                                    if (option != selected) {
                                        haptics.performTick(30L)
                                        onSelect(option)
                                    }
                                }
                            )
                            .minimumInteractiveComponentSize()
                            .testTag("${tagPrefix}_${labelProvider(option)}")
                    ) {
                        Text(
                            text = labelProvider(option),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tactile pill chip adapting between Material 3 tonal chip motion and Liquid Glass bouncy pill motion.
 */
@Composable
fun StudioOptionPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    val uiStyle = LocalUiStyleMode.current
    val reducedMotion = LocalReducedMotionFlag.current
    val haptics = rememberStudioHaptics()
    val isGlass = uiStyle == UiStyleMode.FROSTED_GLASS_OS
    val isDark = LocalDarkThemeFlag.current

    val pillScale by animateFloatAsState(
        targetValue = if (selected && isGlass && !reducedMotion) 1.045f else 1f,
        animationSpec = AppMotion.spatialSpring(uiStyle, reducedMotion),
        label = "option_pill_scale"
    )
    val targetBg = when {
        !enabled -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.45f)
        selected && isGlass -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.94f)
        selected -> MaterialTheme.colorScheme.secondaryContainer
        isGlass -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = if (isDark) 0.80f else 0.78f)
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val bgColor by animateColorAsState(
        targetValue = targetBg,
        animationSpec = AppMotion.colorSpec(uiStyle, reducedMotion),
        label = "option_pill_bg"
    )
    val fgColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        selected && isGlass -> MaterialTheme.colorScheme.onPrimaryContainer
        selected -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    Surface(
        color = bgColor,
        contentColor = fgColor,
        shape = if (isGlass) RoundedCornerShape(50) else RoundedCornerShape(14.dp),
        border = when {
            selected && isGlass -> BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))
            selected -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
            isGlass -> glassSpecularBorder()
            else -> BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        },
        modifier = modifier
            .scale(pillScale)
            .clip(if (isGlass) RoundedCornerShape(50) else RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) {
                haptics.performTick(24L)
                onClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = fgColor,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = fgColor,
                maxLines = 1
            )
        }
    }
}

data class DockDestinationSpec(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Conventional Pixel-inspired Material 3 bottom NavigationBar:
 * crisp tonal surface, Material 3 indicator pill, immediate taps, zero Liquid Glass styling or drag pill.
 */
@Composable
fun PixelMaterialBottomBar(
    destinations: List<DockDestinationSpec>,
    currentRoute: String,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val reducedMotion = LocalReducedMotionFlag.current
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 4.dp,
        modifier = modifier.testTag("bottom_navigation_bar")
    ) {
        destinations.forEach { dest ->
            val selected = currentRoute == dest.route
            val iconScale by animateFloatAsState(
                targetValue = if (selected && !reducedMotion) 1.05f else 1.0f,
                animationSpec = AppMotion.spatialSpring(UiStyleMode.MATERIAL_3, reducedMotion),
                label = "m3_nav_icon_scale"
            )
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectRoute(dest.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                        contentDescription = dest.label,
                        modifier = Modifier.scale(iconScale)
                    )
                },
                label = {
                    Text(
                        text = dest.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_${dest.route}")
            )
        }
    }
}

/**
 * Liquid Glass Floating Draggable Bottom Pill Navigation Bar:
 * - Immediate quick tap on any destination icon/tab switches immediately.
 * - Long-pressing the active capsule or bar enters fluid pill-drag mode:
 *   • Tracks finger smoothly across destinations with elastic stretch & specular shimmer.
 *   • Highlights the targeted destination without switching sections mid-drag.
 *   • Commits navigation ONLY on finger release (or reverts cleanly if released over start slot / canceled).
 *   • Throttled target haptic ticks + distinct lift & commit haptic feedback.
 */
@Composable
fun FrostedFloatingNavDock(
    destinations: List<DockDestinationSpec>,
    currentRoute: String,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalDarkThemeFlag.current
    val isAmoled = LocalAmoledDarkFlag.current
    val reducedMotion = LocalReducedMotionFlag.current
    val glassDepth = LocalGlassDepthIntensity.current
    val haptics = rememberStudioHaptics()

    val count = destinations.size.coerceAtLeast(1)
    val settledIndex = destinations.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    var isDraggingPill by remember { mutableStateOf(false) }
    var dragFingerX by remember { mutableFloatStateOf(0f) }
    var dragVelocityX by remember { mutableFloatStateOf(0f) }
    var dragStartIndex by remember { mutableIntStateOf(settledIndex) }
    var hoveredIndex by remember { mutableIntStateOf(settledIndex) }
    var lastHapticSlot by remember { mutableIntStateOf(settledIndex) }

    val latestDestinations by rememberUpdatedState(destinations)
    val latestSettledIndex by rememberUpdatedState(settledIndex)
    val latestOnSelectRoute by rememberUpdatedState(onSelectRoute)
    val latestHaptics by rememberUpdatedState(haptics)

    val leftSlotAnim = remember { Animatable(settledIndex.toFloat()) }
    val rightSlotAnim = remember { Animatable(settledIndex.toFloat() + 1f) }
    var previousAnimTarget by remember { mutableIntStateOf(settledIndex) }

    val activeVisualIndex = if (isDraggingPill) hoveredIndex else settledIndex

    LaunchedEffect(settledIndex, isDraggingPill, dragFingerX) {
        if (isDraggingPill) return@LaunchedEffect
        val target = settledIndex
        val movingRight = target >= previousAnimTarget
        previousAnimTarget = target

        val fastSpec = if (reducedMotion) {
            AppMotion.spatialSpring<Float>(UiStyleMode.FROSTED_GLASS_OS, reducedMotion = true)
        } else {
            androidx.compose.animation.core.spring(dampingRatio = 0.62f, stiffness = 430f)
        }
        val tailSpec = if (reducedMotion) {
            AppMotion.spatialSpring<Float>(UiStyleMode.FROSTED_GLASS_OS, reducedMotion = true)
        } else {
            androidx.compose.animation.core.spring(dampingRatio = 0.54f, stiffness = 250f)
        }

        coroutineScope {
            launch {
                leftSlotAnim.animateTo(
                    targetValue = target.toFloat(),
                    animationSpec = if (movingRight) tailSpec else fastSpec
                )
            }
            launch {
                rightSlotAnim.animateTo(
                    targetValue = target.toFloat() + 1f,
                    animationSpec = if (movingRight) fastSpec else tailSpec
                )
            }
        }
    }

    val containerBg = when {
        isAmoled -> Color(0xFF14141D).copy(alpha = (0.88f + (glassDepth - 1f) * 0.06f).coerceIn(0.80f, 0.96f))
        isDark -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = (0.86f + (glassDepth - 1f) * 0.08f).coerceIn(0.78f, 0.95f))
        else -> Color.White.copy(alpha = (0.84f + (glassDepth - 1f) * 0.08f).coerceIn(0.74f, 0.94f))
    }

    val liftScale by animateFloatAsState(
        targetValue = if (isDraggingPill && !reducedMotion) 1.07f else 1.0f,
        animationSpec = AppMotion.spatialSpring(UiStyleMode.FROSTED_GLASS_OS, reducedMotion),
        label = "pill_drag_lift"
    )
    val dragStretchFactor by animateFloatAsState(
        targetValue = if (isDraggingPill && !reducedMotion) {
            (1.0f + (abs(dragVelocityX) / 48f).coerceIn(0f, 0.14f))
        } else {
            1.0f
        },
        animationSpec = AppMotion.spatialSpring(UiStyleMode.FROSTED_GLASS_OS, reducedMotion),
        label = "pill_drag_stretch"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = containerBg,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(
                width = 1.25.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = if (isDark) 0.34f else 0.92f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                        MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.24f else 0.16f)
                    )
                )
            ),
            tonalElevation = 0.dp,
            shadowElevation = if (isDraggingPill) 18.dp else 14.dp,
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .testTag("bottom_navigation_bar")
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .pointerInput(count) {
                        val totalW = size.width.toFloat().coerceAtLeast(1f)
                        val slotW = totalW / count.toFloat()
                        detectDragGesturesAfterLongPress(
                            onDragStart = { startOffset ->
                                val startIdx = latestSettledIndex
                                val touchSlot = LiquidNavPillMath.slotForPosition(startOffset.x, totalW, count)
                                isDraggingPill = true
                                dragStartIndex = startIdx
                                hoveredIndex = touchSlot
                                lastHapticSlot = touchSlot
                                dragFingerX = startOffset.x.coerceIn(slotW * 0.5f, totalW - slotW * 0.5f)
                                dragVelocityX = 0f
                                latestHaptics.performConfirm()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val nextX = (dragFingerX + dragAmount.x).coerceIn(slotW * 0.5f, totalW - slotW * 0.5f)
                                dragFingerX = nextX
                                dragVelocityX = dragAmount.x
                                val nextSlot = LiquidNavPillMath.slotForPosition(nextX, totalW, count)
                                if (nextSlot != hoveredIndex) {
                                    hoveredIndex = nextSlot
                                    if (nextSlot != lastHapticSlot) {
                                        lastHapticSlot = nextSlot
                                        latestHaptics.performTick(minIntervalMs = 55L)
                                    }
                                }
                            },
                            onDragEnd = {
                                val startIdx = dragStartIndex
                                val releaseSlot = hoveredIndex
                                val routes = latestDestinations.map { it.route }
                                val targetRoute = LiquidNavPillMath.resolveReleaseRoute(startIdx, releaseSlot, routes)
                                // Sync slot spring start to release position so pill glides fluidly into rest slot
                                val slotPos = ((dragFingerX - slotW * 0.5f) / slotW).coerceIn(0f, (count - 1).toFloat())
                                isDraggingPill = false
                                dragVelocityX = 0f
                                previousAnimTarget = releaseSlot
                                if (targetRoute != null) {
                                    latestHaptics.performConfirm()
                                    latestOnSelectRoute(targetRoute)
                                } else if (abs(slotPos - startIdx.toFloat()) > 0.05f) {
                                    latestHaptics.performTick(25L)
                                }
                            },
                            onDragCancel = {
                                isDraggingPill = false
                                dragVelocityX = 0f
                                hoveredIndex = latestSettledIndex
                            }
                        )
                    }
            ) {
                val density = LocalDensity.current
                val totalW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                val slotW = totalW / count.toFloat()

                // Snap leftSlotAnim / rightSlotAnim immediately during live drag so release settles smoothly from finger spot
                LaunchedEffect(isDraggingPill, dragFingerX, slotW) {
                    if (isDraggingPill) {
                        val fingerSlotLeft = ((dragFingerX - slotW * 0.5f) / slotW).coerceIn(0f, (count - 1).toFloat())
                        leftSlotAnim.snapTo(fingerSlotLeft)
                        rightSlotAnim.snapTo(fingerSlotLeft + 1f)
                    }
                }

                val minLeftSlot = min(leftSlotAnim.value, rightSlotAnim.value - 0.55f)
                val maxRightSlot = max(rightSlotAnim.value, leftSlotAnim.value + 0.55f)
                val travelStretch = abs((rightSlotAnim.value - leftSlotAnim.value) - 1f).coerceIn(0f, 1.5f)

                val capsuleX = if (isDraggingPill) {
                    (dragFingerX - slotW * 0.5f).coerceIn(0f, (totalW - slotW).coerceAtLeast(0f))
                } else {
                    (minLeftSlot * slotW).coerceIn(0f, (totalW - slotW * 0.55f).coerceAtLeast(0f))
                }
                val capsuleW = if (isDraggingPill) {
                    slotW
                } else {
                    ((maxRightSlot - minLeftSlot) * slotW).coerceIn(slotW * 0.72f, (totalW - capsuleX).coerceAtLeast(slotW * 0.72f))
                }
                val capsuleWidthDp = with(density) { capsuleW.toDp() }
                val baseHeight by animateDpAsState(
                    targetValue = if (isDraggingPill) 56.dp else (54.dp - (travelStretch * 2.2f).coerceAtMost(4f).dp),
                    animationSpec = AppMotion.spatialSpring(UiStyleMode.FROSTED_GLASS_OS, reducedMotion),
                    label = "liquid_capsule_h"
                )

                val pillBgStart = MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = if (isDraggingPill) 0.80f else if (isDark) 0.92f else 0.94f
                )
                val pillBgEnd = MaterialTheme.colorScheme.secondaryContainer.copy(
                    alpha = if (isDraggingPill) 0.74f else if (isDark) 0.82f else 0.88f
                )
                val shimmerBias = (dragFingerX / totalW).coerceIn(0f, 1f)
                val shimmerStartColor = Color.White.copy(alpha = if (isDraggingPill) 0.78f else if (isDark) 0.40f else 0.86f)
                val shimmerAccent = MaterialTheme.colorScheme.primary.copy(alpha = if (isDraggingPill) 0.85f else 0.45f)

                // Floating Stretchy Liquid Glass Capsule Lens
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset { IntOffset(x = capsuleX.roundToInt(), y = 0) }
                        .width(capsuleWidthDp)
                        .height(baseHeight)
                        .graphicsLayer {
                            scaleX = liftScale * dragStretchFactor
                            scaleY = liftScale / max(1f, (dragStretchFactor - 1f) * 0.35f + 1f)
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(pillBgStart, pillBgEnd),
                                start = Offset(shimmerBias * 140f, 0f),
                                end = Offset(280f + shimmerBias * 120f, 140f)
                            )
                        )
                        .border(
                            width = if (isDraggingPill) 1.5.dp else 1.dp,
                            brush = Brush.linearGradient(
                                colors = if (shimmerBias > 0.5f) {
                                    listOf(shimmerAccent, shimmerStartColor)
                                } else {
                                    listOf(shimmerStartColor, shimmerAccent)
                                }
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .testTag(if (isDraggingPill) "liquid_nav_dragging_capsule" else "liquid_nav_active_capsule")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    destinations.forEachIndexed { idx, dest ->
                        val isTarget = idx == activeVisualIndex
                        val iconTint by animateColorAsState(
                            targetValue = if (isTarget) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.84f else 0.72f)
                            },
                            animationSpec = AppMotion.colorSpec(UiStyleMode.FROSTED_GLASS_OS, reducedMotion),
                            label = "dock_tint"
                        )
                        val iconPopScale by animateFloatAsState(
                            targetValue = when {
                                isDraggingPill && isTarget -> 1.24f
                                isTarget && !reducedMotion -> 1.15f
                                else -> 1.0f
                            },
                            animationSpec = AppMotion.emphasisSpring(UiStyleMode.FROSTED_GLASS_OS, reducedMotion),
                            label = "dock_icon_pop"
                        )
                        val labelPopScale by animateFloatAsState(
                            targetValue = if (isTarget && !reducedMotion) 1.05f else 0.96f,
                            animationSpec = AppMotion.spatialSpring(UiStyleMode.FROSTED_GLASS_OS, reducedMotion),
                            label = "dock_label_pop"
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(24.dp))
                                .semantics {
                                    role = Role.Tab
                                    selected = currentRoute == dest.route
                                }
                                .clickable(
                                    enabled = !isDraggingPill,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = {
                                        if (currentRoute != dest.route) {
                                            haptics.performTick(25L)
                                        }
                                        onSelectRoute(dest.route)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 7.dp)
                                .testTag("nav_${dest.route}")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTarget) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = dest.label,
                                    tint = iconTint,
                                    modifier = Modifier
                                        .size(21.dp)
                                        .scale(iconPopScale)
                                )
                                Text(
                                    text = dest.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isTarget) FontWeight.Bold else FontWeight.Medium,
                                    color = iconTint,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.scale(labelPopScale)
                                )
                            }
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
    val isAmoled = LocalAmoledDarkFlag.current
    val reducedMotion = LocalReducedMotionFlag.current
    val glassDepth = LocalGlassDepthIntensity.current
    val haptics = rememberStudioHaptics()

    val glassBg = when {
        isAmoled -> Color(0xFF14141D).copy(alpha = (0.88f + (glassDepth - 1f) * 0.06f).coerceIn(0.80f, 0.96f))
        isDark -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = (0.86f + (glassDepth - 1f) * 0.08f).coerceIn(0.78f, 0.95f))
        else -> Color.White.copy(alpha = (0.84f + (glassDepth - 1f) * 0.08f).coerceIn(0.74f, 0.94f))
    }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp)
    ) {
        Surface(
            color = glassBg,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(30.dp),
            border = glassSpecularBorder(),
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxHeight()
                .testTag("frosted_nav_rail")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BackdropLogoMark(size = 32.dp)
                Spacer(modifier = Modifier.height(4.dp))
                destinations.forEach { dest ->
                    val selected = currentRoute == dest.route
                    val itemScale by animateFloatAsState(
                        targetValue = if (selected && !reducedMotion) 1.12f else 1.0f,
                        animationSpec = AppMotion.emphasisSpring(UiStyleMode.FROSTED_GLASS_OS, reducedMotion),
                        label = "rail_pop"
                    )
                    val itemBg by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                        else Color.Transparent,
                        animationSpec = AppMotion.colorSpec(UiStyleMode.FROSTED_GLASS_OS, reducedMotion),
                        label = "rail_bg"
                    )
                    val tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.84f else 0.74f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(itemBg)
                            .clickable {
                                if (currentRoute != dest.route) haptics.performTick(25L)
                                onSelectRoute(dest.route)
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("nav_${dest.route}")
                    ) {
                        Icon(
                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                            contentDescription = dest.label,
                            tint = tint,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(itemScale)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dest.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = tint,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
