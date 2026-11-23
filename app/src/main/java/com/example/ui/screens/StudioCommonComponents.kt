package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.ColorMatrix as AndroidColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint as AndroidPaint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.example.data.media.WallpaperTarget
import com.example.data.model.UiStyleMode
import com.example.ui.components.TactileSurface
import com.example.ui.theme.AppMotion
import com.example.ui.theme.LocalDarkThemeFlag
import com.example.ui.theme.LocalReducedMotionFlag
import com.example.ui.theme.LocalUiStyleMode
import com.example.ui.theme.rememberStudioHaptics
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Local real-time hardware-accelerated canvas filters (no external AI required)
 * that match visually between Compose preview and final applied Bitmap.
 */
enum class CanvasLookFilter(val label: String) {
    ORIGINAL("Original"),
    WARM_FILM("Warm Film"),
    COOL_MIST("Cool Mist"),
    CRISP_MONO("Noir Mono"),
    VELVET_MATTE("Velvet Matte");

    fun toColorMatrixArray(): FloatArray? = when (this) {
        ORIGINAL -> null
        WARM_FILM -> floatArrayOf(
            1.06f, 0.04f, 0f, 0f, 10f,
            0f, 1.01f, 0f, 0f, 4f,
            0f, 0f, 0.90f, 0f, -6f,
            0f, 0f, 0f, 1f, 0f
        )
        COOL_MIST -> floatArrayOf(
            0.92f, 0f, 0.04f, 0f, -4f,
            0f, 1.00f, 0.04f, 0f, 2f,
            0.03f, 0.04f, 1.08f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        )
        CRISP_MONO -> floatArrayOf(
            0.32f, 0.62f, 0.12f, 0f, -6f,
            0.32f, 0.62f, 0.12f, 0f, -6f,
            0.32f, 0.62f, 0.12f, 0f, -6f,
            0f, 0f, 0f, 1f, 0f
        )
        VELVET_MATTE -> floatArrayOf(
            0.88f, 0f, 0f, 0f, 14f,
            0f, 0.88f, 0f, 0f, 14f,
            0f, 0f, 0.90f, 0f, 16f,
            0f, 0f, 0f, 1f, 0f
        )
    }

    fun toComposeColorFilter(): ColorFilter? =
        toColorMatrixArray()?.let { ColorFilter.colorMatrix(ColorMatrix(it)) }
}

data class NormalizedCropRect(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int
)

/**
 * Pure geometry solver for 1:1 Lock Screen centered framing and optional wider Home parallax crop.
 * Prevents Android WallpaperManager from drifting off-center when aspect ratios differ.
 */
internal fun computeCenteredViewportCropRect(
    sourceWidth: Int,
    sourceHeight: Int,
    zoomScale: Float,
    panOffsetX: Float,
    panOffsetY: Float,
    viewportWidthPx: Float,
    viewportHeightPx: Float,
    targetScreenAspect: Float,
    allowParallaxWidth: Boolean = false
): NormalizedCropRect {
    val safeW = sourceWidth.coerceAtLeast(1)
    val safeH = sourceHeight.coerceAtLeast(1)
    val srcW = safeW.toFloat()
    val srcH = safeH.toFloat()
    val srcAspect = srcW / srcH

    val clampedTargetAspect = targetScreenAspect.coerceIn(0.36f, 2.2f)
    val fitAspect = if (allowParallaxWidth) {
        (clampedTargetAspect * 1.24f).coerceAtMost(max(srcAspect, clampedTargetAspect))
    } else {
        clampedTargetAspect
    }

    val safeZoom = zoomScale.coerceIn(1f, 4f)
    val baseBoxW: Float
    val baseBoxH: Float
    if (srcAspect > fitAspect) {
        baseBoxH = srcH
        baseBoxW = (srcH * fitAspect).coerceAtMost(srcW)
    } else {
        baseBoxW = srcW
        baseBoxH = (srcW / fitAspect).coerceAtMost(srcH)
    }

    val cropW = (baseBoxW / safeZoom).coerceIn(min(64f, srcW), srcW)
    val cropH = (baseBoxH / safeZoom).coerceIn(min(64f, srcH), srcH)

    val effectiveScale = if (viewportWidthPx > 0f && viewportHeightPx > 0f) {
        max(viewportWidthPx / srcW, viewportHeightPx / srcH) * safeZoom
    } else {
        1f
    }

    val rawCenterX = if (viewportWidthPx > 0f && effectiveScale > 0f) {
        srcW * 0.5f - (panOffsetX / effectiveScale)
    } else {
        srcW * 0.5f
    }
    val rawCenterY = if (viewportHeightPx > 0f && effectiveScale > 0f) {
        srcH * 0.5f - (panOffsetY / effectiveScale)
    } else {
        srcH * 0.5f
    }

    val minCenterX = cropW * 0.5f
    val maxCenterX = (srcW - cropW * 0.5f).coerceAtLeast(minCenterX)
    val minCenterY = cropH * 0.5f
    val maxCenterY = (srcH - cropH * 0.5f).coerceAtLeast(minCenterY)

    val clampedCenterX = rawCenterX.coerceIn(minCenterX, maxCenterX)
    val clampedCenterY = rawCenterY.coerceIn(minCenterY, maxCenterY)

    val intW = cropW.roundToInt().coerceIn(1, safeW)
    val intH = cropH.roundToInt().coerceIn(1, safeH)
    val maxLeft = (safeW - intW).coerceAtLeast(0)
    val maxTop = (safeH - intH).coerceAtLeast(0)
    val left = (clampedCenterX - intW * 0.5f).roundToInt().coerceIn(0, maxLeft)
    val top = (clampedCenterY - intH * 0.5f).roundToInt().coerceIn(0, maxTop)

    return NormalizedCropRect(
        left = left,
        top = top,
        width = intW,
        height = intH
    )
}

internal fun resolveDevicePortraitAspect(context: Context): Float {
    val metrics = context.resources.displayMetrics
    val minDim = min(metrics.widthPixels, metrics.heightPixels).coerceAtLeast(1)
    val maxDim = max(metrics.widthPixels, metrics.heightPixels).coerceAtLeast(1)
    return (minDim.toFloat() / maxDim.toFloat()).coerceIn(0.40f, 0.78f)
}

@Composable
internal fun StudioWallpaperTargetDialog(
    title: String,
    onDismiss: () -> Unit,
    onSelectTarget: (WallpaperTarget, Boolean) -> Unit
) {
    var allowHomeParallax by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        TactileSurface(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Set Wallpaper",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Choose where to apply \"$title\":",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "Home Screen Parallax",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Off keeps exact 1:1 centered lock framing",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = allowHomeParallax,
                        onCheckedChange = { allowHomeParallax = it },
                        modifier = Modifier.testTag("home_parallax_switch")
                    )
                }
                Button(
                    onClick = { onSelectTarget(WallpaperTarget.HOME, allowHomeParallax) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Home Screen")
                }
                FilledTonalButton(
                    onClick = { onSelectTarget(WallpaperTarget.LOCK, false) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lock Screen")
                }
                OutlinedButton(
                    onClick = { onSelectTarget(WallpaperTarget.BOTH, allowHomeParallax) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Home & Lock Screens")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
internal fun AdaptiveDesignSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    thumbColorOverride: Color? = null,
    onValueChangeFinished: (() -> Unit)? = null
) {
    val uiStyle = LocalUiStyleMode.current
    if (uiStyle == UiStyleMode.FROSTED_GLASS_OS) {
        LiquidGlassSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            thumbColorOverride = thumbColorOverride,
            onValueChangeFinished = onValueChangeFinished,
            modifier = modifier
        )
    } else {
        val haptics = rememberStudioHaptics()
        val span = (valueRange.endInclusive - valueRange.start).coerceAtLeast(1e-4f)
        val fraction = ((value - valueRange.start) / span).coerceIn(0f, 1f)
        var lastDetentBucket by remember { mutableIntStateOf((fraction * 10f).roundToInt()) }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = { next ->
                val f = ((next - valueRange.start) / span).coerceIn(0f, 1f)
                val bucket = (f * 10f).roundToInt()
                if (bucket != lastDetentBucket) {
                    lastDetentBucket = bucket
                    haptics.performTick(45L)
                }
                onValueChange(next)
            },
            valueRange = valueRange,
            onValueChangeFinished = onValueChangeFinished,
            colors = if (thumbColorOverride != null) {
                SliderDefaults.colors(thumbColor = thumbColorOverride)
            } else {
                SliderDefaults.colors()
            },
            modifier = modifier
        )
    }
}

/**
 * Tactile optical capsule slider unique to Liquid Glass mode:
 * - Translucent glass capsule track that gently swells vertically while actively dragged.
 * - Refracted gradient active span + specular top rim.
 * - Morphing frosted droplet thumb with subtle stretch & haptic detents across notches.
 */
@Composable
internal fun LiquidGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    thumbColorOverride: Color? = null,
    onValueChangeFinished: (() -> Unit)? = null
) {
    val isDark = LocalDarkThemeFlag.current
    val reducedMotion = LocalReducedMotionFlag.current
    val haptics = rememberStudioHaptics()

    val span = (valueRange.endInclusive - valueRange.start).coerceAtLeast(1e-4f)
    val fraction = ((value - valueRange.start) / span).coerceIn(0f, 1f)

    var isPressedOrDragging by remember { mutableStateOf(false) }
    var lastDetentBucket by remember { mutableIntStateOf((fraction * 10f).roundToInt()) }

    val latestOnValueChange by rememberUpdatedState(onValueChange)
    val latestOnFinished by rememberUpdatedState(onValueChangeFinished)
    val latestValueRange by rememberUpdatedState(valueRange)
    val latestHaptics by rememberUpdatedState(haptics)

    val trackHeight by animateDpAsState(
        targetValue = if (isPressedOrDragging) 14.dp else 10.dp,
        animationSpec = AppMotion.spatialSpring(UiStyleMode.FROSTED_GLASS_OS, reducedMotion),
        label = "liquid_slider_track_h"
    )
    val dropletScaleX by animateFloatAsState(
        targetValue = if (isPressedOrDragging && !reducedMotion) 1.24f else 1.0f,
        animationSpec = AppMotion.emphasisSpring(UiStyleMode.FROSTED_GLASS_OS, reducedMotion),
        label = "liquid_slider_droplet_sx"
    )
    val dropletScaleY by animateFloatAsState(
        targetValue = if (isPressedOrDragging && !reducedMotion) 1.12f else 1.0f,
        animationSpec = AppMotion.emphasisSpring(UiStyleMode.FROSTED_GLASS_OS, reducedMotion),
        label = "liquid_slider_droplet_sy"
    )

    val trackBg = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.72f)
    } else {
        Color.White.copy(alpha = 0.68f)
    }
    val rimBrush = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.45f else 0.94f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.36f)
        )
    )
    val activeFillBrush = Brush.horizontalGradient(
        listOf(
            (thumbColorOverride ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.88f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.92f)
        )
    )

    BoxWithConstraints(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .height(34.dp)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = value.coerceIn(valueRange.start, valueRange.endInclusive),
                    range = valueRange
                )
                setProgress { targetVal ->
                    val clamped = targetVal.coerceIn(valueRange.start, valueRange.endInclusive)
                    onValueChange(clamped)
                    true
                }
            }
            .pointerInput(valueRange) {
                val widthPx = size.width.toFloat().coerceAtLeast(1f)
                val thumbRadiusPx = 11.dp.toPx()
                val usableW = (widthPx - thumbRadiusPx * 2f).coerceAtLeast(1f)

                fun dispatchX(xPx: Float) {
                    val f = ((xPx - thumbRadiusPx) / usableW).coerceIn(0f, 1f)
                    val rng = latestValueRange
                    val newVal = (rng.start + (rng.endInclusive - rng.start) * f)
                        .coerceIn(rng.start, rng.endInclusive)
                    val bucket = (f * 10f).roundToInt()
                    if (bucket != lastDetentBucket) {
                        lastDetentBucket = bucket
                        latestHaptics.performTick(45L)
                    }
                    latestOnValueChange(newVal)
                }

                detectTapGestures { offset ->
                    isPressedOrDragging = true
                    dispatchX(offset.x)
                    isPressedOrDragging = false
                    latestOnFinished?.invoke()
                }
            }
            .pointerInput(valueRange) {
                val widthPx = size.width.toFloat().coerceAtLeast(1f)
                val thumbRadiusPx = 11.dp.toPx()
                val usableW = (widthPx - thumbRadiusPx * 2f).coerceAtLeast(1f)

                fun dispatchX(xPx: Float) {
                    val f = ((xPx - thumbRadiusPx) / usableW).coerceIn(0f, 1f)
                    val rng = latestValueRange
                    val newVal = (rng.start + (rng.endInclusive - rng.start) * f)
                        .coerceIn(rng.start, rng.endInclusive)
                    val bucket = (f * 10f).roundToInt()
                    if (bucket != lastDetentBucket) {
                        lastDetentBucket = bucket
                        latestHaptics.performTick(45L)
                    }
                    latestOnValueChange(newVal)
                }

                detectDragGestures(
                    onDragStart = { startOffset ->
                        isPressedOrDragging = true
                        dispatchX(startOffset.x)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        dispatchX(change.position.x)
                    },
                    onDragEnd = {
                        isPressedOrDragging = false
                        latestOnFinished?.invoke()
                    },
                    onDragCancel = {
                        isPressedOrDragging = false
                    }
                )
            }
    ) {
        val density = LocalDensity.current
        val totalPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val thumbSizeDp = 22.dp
        val thumbPx = with(density) { thumbSizeDp.toPx() }
        val usablePx = (totalPx - thumbPx).coerceAtLeast(0f)
        val thumbX = (usablePx * fraction).roundToInt()
        val fillWidthDp = with(density) { ((usablePx * fraction) + thumbPx * 0.5f).coerceIn(8f, totalPx).toDp() }

        // Outer glass capsule track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(50))
                .background(trackBg)
                .border(BorderStroke(1.dp, rimBrush), RoundedCornerShape(50))
                .testTag("liquid_glass_slider_track")
        ) {
            // Active refracted span
            Box(
                modifier = Modifier
                    .width(fillWidthDp)
                    .height(trackHeight)
                    .clip(RoundedCornerShape(50))
                    .background(activeFillBrush)
            )
        }

        // Morphing frosted droplet thumb
        Surface(
            color = (thumbColorOverride ?: Color.White).copy(alpha = if (isPressedOrDragging) 0.96f else 0.92f),
            shape = CircleShape,
            border = BorderStroke(
                width = 1.4.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White,
                        (thumbColorOverride ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.72f)
                    )
                )
            ),
            shadowElevation = if (isPressedOrDragging) 10.dp else 5.dp,
            modifier = Modifier
                .offset { IntOffset(x = thumbX, y = 0) }
                .size(thumbSizeDp)
                .graphicsLayer {
                    scaleX = dropletScaleX
                    scaleY = dropletScaleY
                }
        ) {}
    }
}

@Composable
internal fun StudioParamSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    sliderTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(56.dp)
        )
        AdaptiveDesignSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
                .weight(1f)
                .testTag(sliderTag)
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(42.dp)
        )
    }
}

internal fun parseHexOrNull(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return try {
        Color(hex.trim().toColorInt())
    } catch (_: Exception) {
        null
    }
}

/**
 * Crops the source bitmap with exact centered screen aspect (or optional parallax width)
 * matching Compose viewport pan/zoom with zero off-center drift, and bakes dim/filter look.
 */
internal fun renderEditedWallpaperBitmap(
    source: Bitmap,
    zoomScale: Float,
    panOffset: Offset,
    viewportWidthPx: Float,
    viewportHeightPx: Float,
    dimFraction: Float = 0f,
    lookFilter: CanvasLookFilter = CanvasLookFilter.ORIGINAL,
    targetScreenAspect: Float = if (viewportWidthPx > 0f && viewportHeightPx > 0f) {
        (viewportWidthPx / viewportHeightPx).coerceIn(0.40f, 0.78f)
    } else {
        9f / 19.5f
    },
    allowParallaxWidth: Boolean = false,
    rotationDegrees: Float = 0f
): Bitmap {
    val crop = computeCenteredViewportCropRect(
        sourceWidth = source.width,
        sourceHeight = source.height,
        zoomScale = zoomScale,
        panOffsetX = panOffset.x,
        panOffsetY = panOffset.y,
        viewportWidthPx = viewportWidthPx,
        viewportHeightPx = viewportHeightPx,
        targetScreenAspect = targetScreenAspect,
        allowParallaxWidth = allowParallaxWidth
    )

    val normRot = ((rotationDegrees % 360f) + 360f) % 360f
    val hasRotation = normRot > 0.25f && normRot < 359.75f

    val cropped = if (!hasRotation && crop.left == 0 && crop.top == 0 && crop.width == source.width && crop.height == source.height) {
        source
    } else if (!hasRotation) {
        Bitmap.createBitmap(source, crop.left, crop.top, crop.width, crop.height)
    } else {
        // Rotate around output center matching Compose Canvas framing
        val out = createBitmap(crop.width, crop.height)
        val canvas = AndroidCanvas(out)
        val matrix = android.graphics.Matrix()
        val scale = max(crop.width.toFloat() / source.width.toFloat(), crop.height.toFloat() / source.height.toFloat()) *
            zoomScale.coerceIn(1f, 4f)
        val panRatioX = if (viewportWidthPx > 0f) panOffset.x * (crop.width / viewportWidthPx) else 0f
        val panRatioY = if (viewportHeightPx > 0f) panOffset.y * (crop.height / viewportHeightPx) else 0f
        matrix.postTranslate(-source.width * 0.5f, -source.height * 0.5f)
        matrix.postScale(scale, scale)
        matrix.postRotate(rotationDegrees)
        matrix.postTranslate(crop.width * 0.5f + panRatioX, crop.height * 0.5f + panRatioY)
        val rotPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(source, matrix, rotPaint)
        out
    }

    val matrixValues = lookFilter.toColorMatrixArray()
    val safeDim = dimFraction.coerceIn(0f, 0.6f)
    if (matrixValues == null && safeDim <= 0.01f) {
        return cropped
    }

    val out = createBitmap(cropped.width, cropped.height)
    val canvas = AndroidCanvas(out)
    val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.FILTER_BITMAP_FLAG)
    if (matrixValues != null) {
        paint.colorFilter = ColorMatrixColorFilter(AndroidColorMatrix(matrixValues))
    }
    canvas.drawBitmap(cropped, 0f, 0f, paint)
    if (safeDim > 0.01f) {
        canvas.drawColor(android.graphics.Color.argb((safeDim * 255f).roundToInt().coerceIn(0, 200), 0, 0, 0))
    }
    return out
}
