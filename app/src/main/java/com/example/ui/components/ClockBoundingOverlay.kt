package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Interactive HyperOS-style bounding box overlay allowing direct edge-dragging for Width,
 * Height, Corner Scale, and Center Position right on the lock-screen canvas.
 */
@Composable
fun ClockBoundingOverlay(
    topOffsetDp: Dp,
    centerOffsetXPx: Float,
    clockWidthPx: Int,
    clockHeightPx: Int,
    viewportSize: IntSize,
    config: ClockDesignConfig,
    onUpdateConfig: (ClockDesignConfig) -> Unit,
    onResetFrame: () -> Unit,
    onDoneEditing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val latestConfig by rememberUpdatedState(config)
    val latestViewport by rememberUpdatedState(viewportSize)

    val boxW = if (clockWidthPx > 0) with(density) { (clockWidthPx * config.widthScale).toDp() }
        .coerceIn(180.dp, 360.dp) else 250.dp
    val boxH = if (clockHeightPx > 0) with(density) { (clockHeightPx * config.heightScale).toDp() }
        .coerceIn(110.dp, 320.dp) else 170.dp

    val topOffsetPx = with(density) { topOffsetDp.toPx() }
    val boxWPx = with(density) { boxW.toPx() }
    val boxHPx = with(density) { boxH.toPx() }
    val leftPx = ((viewportSize.width - boxWPx) * 0.5f + centerOffsetXPx)
        .coerceIn(12f, (viewportSize.width - boxWPx - 12f).coerceAtLeast(12f))

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Floating size readout & Reset / Done bar cleanly tucked above or below frame
        val toolbarY = if (topOffsetPx > with(density) { 104.dp.toPx() }) {
            (topOffsetPx - with(density) { 44.dp.toPx() }).roundToInt()
        } else {
            (topOffsetPx + boxHPx + with(density) { 8.dp.toPx() }).roundToInt()
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
            shape = RoundedCornerShape(50),
            tonalElevation = 4.dp,
            shadowElevation = 6.dp,
            modifier = Modifier
                .offset { IntOffset(x = leftPx.roundToInt(), y = toolbarY) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "W ${(config.widthScale * 100).roundToInt()}% • H ${(config.heightScale * 100).roundToInt()}% • S ${(config.scale * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    onClick = onResetFrame,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Surface(
                    onClick = onDoneEditing,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Interactive Clock Bounding Box + Edge/Corner Handles
        Box(
            modifier = Modifier
                .offset { IntOffset(x = leftPx.roundToInt(), y = topOffsetPx.roundToInt()) }
                .size(width = boxW, height = boxH)
                .clip(RoundedCornerShape(22.dp))
                .border(
                    width = 1.8.dp,
                    color = Color.White.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(22.dp)
                )
                .background(Color.White.copy(alpha = 0.05f))
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        val vw = latestViewport.width.coerceAtLeast(1).toFloat()
                        val vh = latestViewport.height.coerceAtLeast(1).toFloat()
                        onUpdateConfig(
                            latestConfig.copy(
                                offsetXFraction = (latestConfig.offsetXFraction + drag.x / vw)
                                    .coerceIn(-0.26f, 0.26f),
                                placementY = (latestConfig.placementY + drag.y / vh)
                                    .coerceIn(0.06f, 0.44f)
                            )
                        )
                    }
                }
                .testTag("clock_bounding_frame")
        ) {
            // Left edge width handle
            EdgePillHandle(
                isHorizontal = false,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .pointerInput("left") {
                        detectDragGestures { change, drag ->
                            change.consume()
                            val delta = -drag.x / 220f
                            onUpdateConfig(
                                latestConfig.copy(
                                    widthScale = (latestConfig.widthScale + delta).coerceIn(0.72f, 1.35f)
                                )
                            )
                        }
                    }
                    .testTag("clock_handle_width_left")
            )

            // Right edge width handle
            EdgePillHandle(
                isHorizontal = false,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .pointerInput("right") {
                        detectDragGestures { change, drag ->
                            change.consume()
                            val delta = drag.x / 220f
                            onUpdateConfig(
                                latestConfig.copy(
                                    widthScale = (latestConfig.widthScale + delta).coerceIn(0.72f, 1.35f)
                                )
                            )
                        }
                    }
                    .testTag("clock_handle_width_right")
            )

            // Top edge height handle
            EdgePillHandle(
                isHorizontal = true,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .pointerInput("top") {
                        detectDragGestures { change, drag ->
                            change.consume()
                            val delta = -drag.y / 240f
                            onUpdateConfig(
                                latestConfig.copy(
                                    heightScale = (latestConfig.heightScale + delta).coerceIn(0.70f, 1.55f)
                                )
                            )
                        }
                    }
                    .testTag("clock_handle_height_top")
            )

            // Bottom edge height handle
            EdgePillHandle(
                isHorizontal = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .pointerInput("bottom") {
                        detectDragGestures { change, drag ->
                            change.consume()
                            val delta = drag.y / 240f
                            onUpdateConfig(
                                latestConfig.copy(
                                    heightScale = (latestConfig.heightScale + delta).coerceIn(0.70f, 1.55f)
                                )
                            )
                        }
                    }
                    .testTag("clock_handle_height_bottom")
            )

            // 4 Corner Scale Handles
            CornerScaleHandle(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .pointerInput("br") {
                        detectDragGestures { change, drag ->
                            change.consume()
                            val delta = (drag.x + drag.y) / 360f
                            onUpdateConfig(
                                latestConfig.copy(
                                    scale = (latestConfig.scale + delta).coerceIn(0.70f, 1.45f)
                                )
                            )
                        }
                    }
                    .testTag("clock_handle_corner_br")
            )
            CornerScaleHandle(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .pointerInput("tr") {
                        detectDragGestures { change, drag ->
                            change.consume()
                            val delta = (drag.x - drag.y) / 360f
                            onUpdateConfig(
                                latestConfig.copy(
                                    scale = (latestConfig.scale + delta).coerceIn(0.70f, 1.45f)
                                )
                            )
                        }
                    }
            )
            CornerScaleHandle(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .pointerInput("bl") {
                        detectDragGestures { change, drag ->
                            change.consume()
                            val delta = (-drag.x + drag.y) / 360f
                            onUpdateConfig(
                                latestConfig.copy(
                                    scale = (latestConfig.scale + delta).coerceIn(0.70f, 1.45f)
                                )
                            )
                        }
                    }
            )
            CornerScaleHandle(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .pointerInput("tl") {
                        detectDragGestures { change, drag ->
                            change.consume()
                            val delta = (-drag.x - drag.y) / 360f
                            onUpdateConfig(
                                latestConfig.copy(
                                    scale = (latestConfig.scale + delta).coerceIn(0.70f, 1.45f)
                                )
                            )
                        }
                    }
            )
        }
    }
}

@Composable
private fun EdgePillHandle(
    isHorizontal: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = if (isHorizontal) 30.dp else 5.dp,
                    height = if (isHorizontal) 5.dp else 30.dp
                )
                .clip(RoundedCornerShape(50))
                .background(Color.White)
                .border(1.dp, Color.Black.copy(alpha = 0.25f), RoundedCornerShape(50))
        )
    }
}

@Composable
private fun CornerScaleHandle(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
    }
}
