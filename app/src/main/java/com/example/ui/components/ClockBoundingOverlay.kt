package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.data.model.UiStyleMode
import com.example.ui.theme.AppMotion
import com.example.ui.theme.LocalReducedMotionFlag
import com.example.ui.theme.LocalUiStyleMode
import com.example.ui.theme.rememberStudioHaptics
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Interactive bounding box overlay for Depth Studio:
 * - Zero side or top drag bars: resizing is performed exclusively via the Bottom-Left and Bottom-Right corners
 * - Continuous infinite X/Y resizing in a single gesture with opposite top-corner anchoring
 * - Aspect-ratio adaptive clock face switching when unlocked (smooth morph + haptic detent)
 * - Design-language-distinct corner grips: flat Pixel L-brackets (Material 3) vs curved refractive glass arcs (Liquid Glass)
 */
@Composable
fun ClockBoundingOverlay(
    clockUnscaledWidthPx: Int,
    clockUnscaledHeightPx: Int,
    viewportSize: IntSize,
    config: ClockDesignConfig,
    onGestureStart: (ClockDesignConfig) -> Unit,
    onUpdateConfig: (ClockDesignConfig) -> Unit,
    onGestureEnd: () -> Unit,
    onToggleAspectLock: () -> Unit = {},
    onResetFrame: () -> Unit,
    onDoneEditing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val uiStyle = LocalUiStyleMode.current
    val reducedMotion = LocalReducedMotionFlag.current
    val haptics = rememberStudioHaptics()

    val latestConfig by rememberUpdatedState(config)
    val latestViewport by rememberUpdatedState(viewportSize)
    val latestUnscaledW by rememberUpdatedState(clockUnscaledWidthPx.toFloat())
    val latestUnscaledH by rememberUpdatedState(clockUnscaledHeightPx.toFloat())
    val latestHaptics by rememberUpdatedState(haptics)

    var activeGripTarget by remember { mutableStateOf<ClockGripTarget?>(null) }

    val marginPx = with(density) { 10.dp.toPx() }
    val cornerHitRadiusPx = with(density) { 36.dp.toPx() }
    val touchSlopPx = with(density) { 4.dp.toPx() }

    val rect = ClockSpatialMath.computeClockBoxRect(
        unscaledWidthPx = clockUnscaledWidthPx.toFloat(),
        unscaledHeightPx = clockUnscaledHeightPx.toFloat(),
        viewportSize = viewportSize,
        config = config,
        minMarginPx = marginPx
    )

    val boxW = with(density) { rect.widthPx.toDp() }
    val boxH = with(density) { rect.heightPx.toDp() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val pos = down.position
                    val currentCfg = latestConfig
                    val vp = latestViewport
                    val startRect = ClockSpatialMath.computeClockBoxRect(
                        unscaledWidthPx = latestUnscaledW,
                        unscaledHeightPx = latestUnscaledH,
                        viewportSize = vp,
                        config = currentCfg,
                        minMarginPx = marginPx
                    )

                    val target = hitTestGripTarget(
                        pos = pos,
                        rect = startRect,
                        cornerHitRadiusPx = cornerHitRadiusPx
                    )

                    if (target == null) {
                        var movedPastSlop = false
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if ((change.position - pos).getDistance() > touchSlopPx) {
                                movedPastSlop = true
                            }
                            if (!change.pressed) {
                                if (!movedPastSlop) {
                                    onDoneEditing()
                                }
                                break
                            }
                        }
                        return@awaitEachGesture
                    }

                    down.consume()
                    activeGripTarget = target
                    var beganUndoCapture = false
                    var lastEmittedConfig = currentCfg
                    var wasCenteredX = abs(currentCfg.offsetXFraction) < 0.015f

                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            activeGripTarget = null
                            if (beganUndoCapture) {
                                onGestureEnd()
                            }
                            break
                        }

                        val totalDelta = change.position - pos
                        if (!beganUndoCapture && totalDelta.getDistance() >= touchSlopPx) {
                            beganUndoCapture = true
                            onGestureStart(currentCfg)
                            latestHaptics.performTick(25L)
                        }

                        if (beganUndoCapture) {
                            change.consume()
                            val draggedCfg = ClockSpatialMath.applyDragDelta(
                                target = target,
                                startConfig = currentCfg,
                                startRect = startRect,
                                totalDx = totalDelta.x,
                                totalDy = totalDelta.y,
                                viewportSize = vp,
                                minMarginPx = marginPx
                            )

                            val resolvedCfg = if (target == ClockGripTarget.CORNER_BL || target == ClockGripTarget.CORNER_BR) {
                                val morphed = ClockSpatialMath.applyAspectAdaptiveResolution(
                                    updatedConfig = draggedCfg,
                                    unscaledWidthPx = latestUnscaledW,
                                    unscaledHeightPx = latestUnscaledH,
                                    viewportSize = vp,
                                    minMarginPx = marginPx
                                )
                                if (morphed.face != lastEmittedConfig.face) {
                                    latestHaptics.performSnapDetent()
                                } else {
                                    val hitEdge = (morphed.widthScale <= ClockSpatialMath.MIN_WIDTH_SCALE + 0.005f ||
                                        morphed.widthScale >= ClockSpatialMath.MAX_WIDTH_SCALE - 0.005f ||
                                        morphed.heightScale <= ClockSpatialMath.MIN_HEIGHT_SCALE + 0.005f ||
                                        morphed.heightScale >= ClockSpatialMath.MAX_HEIGHT_SCALE - 0.005f)
                                    val prevHitEdge = (lastEmittedConfig.widthScale <= ClockSpatialMath.MIN_WIDTH_SCALE + 0.005f ||
                                        lastEmittedConfig.widthScale >= ClockSpatialMath.MAX_WIDTH_SCALE - 0.005f ||
                                        lastEmittedConfig.heightScale <= ClockSpatialMath.MIN_HEIGHT_SCALE + 0.005f ||
                                        lastEmittedConfig.heightScale >= ClockSpatialMath.MAX_HEIGHT_SCALE - 0.005f)
                                    if (hitEdge && !prevHitEdge) {
                                        latestHaptics.performBoundaryBounce()
                                    }
                                }
                                morphed
                            } else {
                                val isCenteredNow = abs(draggedCfg.offsetXFraction) < 0.018f
                                if (isCenteredNow && !wasCenteredX) {
                                    latestHaptics.performTick(45L)
                                }
                                wasCenteredX = isCenteredNow
                                draggedCfg
                            }

                            lastEmittedConfig = resolvedCfg
                            onUpdateConfig(resolvedCfg)
                        }
                    }
                }
            }
    ) {
        val frameShape = RoundedCornerShape(22.dp)
        // Slim interactive frame with bottom-left & bottom-right corner grips ONLY (zero side/top bars)
        Box(
            modifier = Modifier
                .offset { IntOffset(x = rect.leftPx.roundToInt(), y = rect.topPx.roundToInt()) }
                .size(width = boxW, height = boxH)
                .clip(frameShape)
                .border(
                    width = if (uiStyle == UiStyleMode.FROSTED_GLASS_OS) 1.5.dp else 1.8.dp,
                    brush = if (uiStyle == UiStyleMode.FROSTED_GLASS_OS) {
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.92f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.52f),
                                Color.White.copy(alpha = 0.78f)
                            )
                        )
                    } else {
                        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.94f), Color.White.copy(alpha = 0.94f)))
                    },
                    shape = frameShape
                )
                .background(Color.White.copy(alpha = if (uiStyle == UiStyleMode.FROSTED_GLASS_OS) 0.05f else 0.04f))
                .testTag("clock_bounding_frame")
        ) {
            CornerResizeGripHandle(
                isBottomLeft = true,
                isPressed = activeGripTarget == ClockGripTarget.CORNER_BL,
                uiStyle = uiStyle,
                reducedMotion = reducedMotion,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .testTag("clock_handle_corner_bl")
            )
            CornerResizeGripHandle(
                isBottomLeft = false,
                isPressed = activeGripTarget == ClockGripTarget.CORNER_BR,
                uiStyle = uiStyle,
                reducedMotion = reducedMotion,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .testTag("clock_handle_corner_br")
            )
        }

        // Slim floating proportion readout + Auto-Adapt Lock + Reset + Done pill
        val toolbarHeightPx = with(density) { 44.dp.toPx() }
        val toolbarWidthPx = with(density) { 310.dp.toPx() }
        val toolbarY = if (rect.topPx > with(density) { 108.dp.toPx() }) {
            (rect.topPx - toolbarHeightPx - with(density) { 8.dp.toPx() })
        } else {
            (rect.bottomPx + with(density) { 8.dp.toPx() })
        }.coerceIn(marginPx, (viewportSize.height - toolbarHeightPx - marginPx).coerceAtLeast(marginPx))
        val safeToolbarX = (rect.centerXPx - toolbarWidthPx * 0.5f)
            .coerceIn(marginPx, (viewportSize.width - toolbarWidthPx - marginPx).coerceAtLeast(marginPx))

        TactileSurface(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(50),
            tonalElevation = 4.dp,
            modifier = Modifier
                .offset { IntOffset(x = safeToolbarX.roundToInt(), y = toolbarY.roundToInt()) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "W ${(config.widthScale * 100).roundToInt()}% • H ${(config.heightScale * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    onClick = {
                        haptics.performTick(25L)
                        onToggleAspectLock()
                    },
                    color = if (config.aspectAdaptiveEnabled) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = if (config.aspectAdaptiveEnabled) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("clock_aspect_lock_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = if (config.aspectAdaptiveEnabled) Icons.Rounded.AutoAwesome else Icons.Rounded.Lock,
                            contentDescription = if (config.aspectAdaptiveEnabled) "Aspect auto-adapt active" else "Clock face locked",
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (config.aspectAdaptiveEnabled) "Auto" else "Locked",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Surface(
                    onClick = {
                        haptics.performTick(25L)
                        onResetFrame()
                    },
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    onClick = {
                        haptics.performConfirm()
                        onDoneEditing()
                    },
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun hitTestGripTarget(
    pos: Offset,
    rect: ClockBoxRect,
    cornerHitRadiusPx: Float
): ClockGripTarget? {
    // Priority 1: Bottom-Right & Bottom-Left draggable corner grips ONLY
    val cornerCandidates = listOf(
        ClockGripTarget.CORNER_BR to Offset(rect.rightPx, rect.bottomPx),
        ClockGripTarget.CORNER_BL to Offset(rect.leftPx, rect.bottomPx)
    )
    var bestCorner: ClockGripTarget? = null
    var bestDist = cornerHitRadiusPx
    cornerCandidates.forEach { (target, vertex) ->
        val d = hypot(pos.x - vertex.x, pos.y - vertex.y)
        if (d <= bestDist) {
            bestDist = d
            bestCorner = target
        }
    }
    if (bestCorner != null) return bestCorner

    // Priority 2: Inside box body -> Unrestricted screen drag
    if (pos.x in (rect.leftPx - 8f)..(rect.rightPx + 8f) &&
        pos.y in (rect.topPx - 8f)..(rect.bottomPx + 8f)
    ) {
        return ClockGripTarget.BODY
    }
    return null
}

@Composable
private fun CornerResizeGripHandle(
    isBottomLeft: Boolean,
    isPressed: Boolean,
    uiStyle: UiStyleMode,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val customTokens = com.example.ui.theme.LocalThemeCustomization.current
    val activeScale by animateFloatAsState(
        targetValue = if (isPressed && !reducedMotion) {
            if (uiStyle == UiStyleMode.FROSTED_GLASS_OS && !customTokens.useClassicClockHandles) 1.24f else 1.12f
        } else 1.0f,
        animationSpec = AppMotion.emphasisSpring(uiStyle, reducedMotion, customTokens.m3MotionSpeed),
        label = "corner_grip_scale"
    )
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh

    val styleTag = when {
        customTokens.useClassicClockHandles -> "clock_grip_style_classic_${if (isBottomLeft) "bl" else "br"}"
        uiStyle == UiStyleMode.FROSTED_GLASS_OS -> "clock_grip_style_liquid_glass_${if (isBottomLeft) "bl" else "br"}"
        else -> "clock_grip_style_material3_${if (isBottomLeft) "bl" else "br"}"
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .scale(activeScale),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag(styleTag),
            contentAlignment = Alignment.Center
        ) {
            when {
                customTokens.useClassicClockHandles -> {
                    // Classic circular corner puck handle
                    Canvas(modifier = Modifier.size(22.dp)) {
                        drawCircle(color = surfaceColor, radius = size.minDimension * 0.44f)
                        drawCircle(
                            color = primaryColor,
                            radius = size.minDimension * 0.44f,
                            style = Stroke(width = 2.4.dp.toPx())
                        )
                        drawCircle(color = primaryColor, radius = size.minDimension * 0.16f)
                    }
                }
                uiStyle == UiStyleMode.FROSTED_GLASS_OS -> {
                    // Liquid Glass: Sculpted translucent curved glass arc hugging the rounded corner
                    Canvas(modifier = Modifier.size(34.dp)) {
                        val pad = 3.dp.toPx()
                        val arcSize = Size(size.width - pad * 2f, size.height - pad * 2f)
                        val startAngle = if (isBottomLeft) 90f else 0f
                        val sweepAngle = 90f

                        drawArc(
                            color = primaryColor.copy(alpha = 0.28f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(pad, pad),
                            size = arcSize,
                            style = Stroke(width = 8.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.94f),
                                    primaryColor.copy(alpha = 0.62f),
                                    Color.White.copy(alpha = 0.88f)
                                )
                            ),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(pad, pad),
                            size = arcSize,
                            style = Stroke(width = 5.2.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color.White.copy(alpha = 0.96f),
                            startAngle = startAngle + 6f,
                            sweepAngle = sweepAngle - 12f,
                            useCenter = false,
                            topLeft = Offset(pad + 0.5.dp.toPx(), pad + 0.5.dp.toPx()),
                            size = Size(arcSize.width - 1.dp.toPx(), arcSize.height - 1.dp.toPx()),
                            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
                else -> {
                    // Material 3: Flat, crisp Pixel-inspired geometric right-angle corner bracket
                    Canvas(modifier = Modifier.size(28.dp)) {
                        val m = 4.dp.toPx()
                        val arm = 13.dp.toPx()
                        val bracketPath = Path().apply {
                            if (isBottomLeft) {
                                moveTo(m, size.height - m - arm)
                                lineTo(m, size.height - m)
                                lineTo(m + arm, size.height - m)
                            } else {
                                moveTo(size.width - m - arm, size.height - m)
                                lineTo(size.width - m, size.height - m)
                                lineTo(size.width - m, size.height - m - arm)
                            }
                        }
                        drawPath(
                            path = bracketPath,
                            color = surfaceColor,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Square, join = StrokeJoin.Miter)
                        )
                        drawPath(
                            path = bracketPath,
                            color = primaryColor,
                            style = Stroke(width = 3.4.dp.toPx(), cap = StrokeCap.Square, join = StrokeJoin.Miter)
                        )
                        val dotPos = if (isBottomLeft) Offset(m, size.height - m) else Offset(size.width - m, size.height - m)
                        drawCircle(color = onPrimaryColor, radius = 2.dp.toPx(), center = dotPos)
                    }
                }
            }
        }
    }
}
