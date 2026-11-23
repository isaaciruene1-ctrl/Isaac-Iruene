package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.FormatAlignLeft
import androidx.compose.material.icons.automirrored.rounded.FormatAlignRight
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FormatAlignCenter
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VerticalAlignTop
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import com.example.data.media.WallpaperTarget
import com.example.data.model.LockScreenSetDoc
import com.example.data.model.UiStyleMode
import com.example.data.model.Wallpaper
import com.example.engine.depth.ClockHorizontalAlign
import com.example.engine.depth.LabColorSpace
import com.example.ui.components.ClockBoundingOverlay
import com.example.ui.components.ClockDesignConfig
import com.example.ui.components.ClockSignatureMode
import com.example.ui.components.ClockSpatialMath
import com.example.ui.components.HyperOsClockFace
import com.example.ui.components.HyperOsClockFaceThumbnail
import com.example.ui.components.HyperOsClockFinish
import com.example.ui.components.HyperOsClockFont
import com.example.ui.components.HyperOsLockClock
import com.example.ui.components.LiquidGlassConfig
import com.example.ui.components.LiquidGlassPreset
import com.example.ui.components.StudioOptionPill
import com.example.ui.components.StudioSegmentedBar
import com.example.ui.components.TactileAmbientBackdrop
import com.example.ui.components.TactileHudIconButton
import com.example.ui.components.TactileSurface
import com.example.ui.components.WallpaperAsyncImage
import com.example.ui.components.glassSpecularBorder
import com.example.ui.theme.AppMotion
import com.example.ui.theme.LocalReducedMotionFlag
import com.example.ui.theme.LocalUiStyleMode
import com.example.ui.theme.rememberStudioHaptics
import com.example.ui.viewmodel.DepthStudioUiState
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

private enum class DepthSectionTab(val label: String) {
    SUBJECT("Depth"),
    CLOCK("Clock"),
    WALLPAPER("Canvas")
}

private enum class ClockSubSection(val label: String) {
    STYLE("Style"),
    TYPE_FINISH("Type & Surface"),
    COLOR_LAYOUT("Color & Layout")
}

private enum class CustomColorTarget(val label: String) {
    PRIMARY("Primary"),
    SECONDARY("Accent")
}

@Composable
fun DepthStudioScreen(
    initialWallpaper: Wallpaper,
    depthState: DepthStudioUiState,
    clockConfig: ClockDesignConfig,
    canUndo: Boolean,
    canRedo: Boolean,
    isApplying: Boolean,
    initialSet: LockScreenSetDoc? = null,
    onLoadWallpaper: (Wallpaper, Boolean) -> Unit,
    onPickCustomPhoto: (Uri, Boolean) -> Unit,
    onBeginClockGesture: (ClockDesignConfig) -> Unit,
    onPreviewClockConfig: (ClockDesignConfig) -> Unit,
    onEndClockGesture: () -> Unit,
    onCommitClockConfig: ((ClockDesignConfig) -> ClockDesignConfig) -> Unit,
    onUndoClock: () -> Unit,
    onRedoClock: () -> Unit,
    onResetProportions: () -> Unit,
    onCenterClock: () -> Unit,
    onAutoSnapClock: () -> Unit = {},
    onRestoreSetClock: ((ClockDesignConfig) -> Unit)? = null,
    onSaveLockScreenSet: ((LockScreenSetDoc) -> Unit)? = null,
    onBack: () -> Unit,
    onApplyWallpaper: (Wallpaper, WallpaperTarget, Boolean) -> Unit,
    onApplyCustomBitmapWallpaper: (Bitmap, WallpaperTarget, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeWallpaper by remember(initialWallpaper.id) { mutableStateOf(initialWallpaper) }
    var customPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    var showControls by remember { mutableStateOf(true) }
    var activeTab by remember { mutableStateOf(DepthSectionTab.SUBJECT) }
    var clockSubSection by remember { mutableStateOf(ClockSubSection.STYLE) }
    var showGlassOpticsDrawer by remember { mutableStateOf(false) }
    var showFormatAndSignatureDrawer by remember { mutableStateOf(false) }

    var showCustomColorPicker by remember { mutableStateOf(false) }
    var colorTarget by remember { mutableStateOf(CustomColorTarget.PRIMARY) }
    var pickerHue by remember { mutableFloatStateOf(210f) }
    var pickerSat by remember { mutableFloatStateOf(0.55f) }
    var pickerVal by remember { mutableFloatStateOf(0.95f) }

    var isClockEditing by remember { mutableStateOf(false) }
    var clockMeasuredSize by remember { mutableStateOf(IntSize.Zero) }

    var clockColorIndex by remember { mutableIntStateOf(0) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var dimFraction by remember { mutableFloatStateOf(0.0f) }
    var lookFilter by remember { mutableStateOf(CanvasLookFilter.ORIGINAL) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var showApplyDialog by remember { mutableStateOf(false) }
    var showSaveSetDialog by remember { mutableStateOf(false) }
    var lastAutoAppliedKey by rememberSaveable { mutableStateOf<String?>(null) }
    var skipNextAutoSnapForSet by remember { mutableStateOf(initialSet != null) }

    LaunchedEffect(initialSet?.id) {
        if (initialSet != null) {
            skipNextAutoSnapForSet = true
            val restoredClock = ClockDesignConfig.fromJsonString(initialSet.clockConfigJson)
            if (restoredClock != null) {
                onRestoreSetClock?.invoke(restoredClock)
            }
            zoomScale = initialSet.zoomScale.coerceIn(1f, 1.8f)
            panOffset = Offset(initialSet.panOffsetX, initialSet.panOffsetY)
            dimFraction = initialSet.dimFraction.coerceIn(0f, 0.45f)
            lookFilter = runCatching { CanvasLookFilter.valueOf(initialSet.lookFilterName) }
                .getOrDefault(CanvasLookFilter.ORIGINAL)
            if (initialSet.customPhotoUri.isNotBlank()) {
                customPhotoUri = runCatching { Uri.parse(initialSet.customPhotoUri) }.getOrNull()
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customPhotoUri = uri
            panOffset = Offset.Zero
            zoomScale = 1.0f
            onPickCustomPhoto(uri, false)
        }
    }

    LaunchedEffect(activeWallpaper.id, customPhotoUri) {
        val uri = customPhotoUri
        if (uri != null) {
            onPickCustomPhoto(uri, false)
        } else {
            onLoadWallpaper(activeWallpaper, false)
        }
    }

    val uiStyle = LocalUiStyleMode.current
    val reducedMotion = LocalReducedMotionFlag.current
    val haptics = rememberStudioHaptics()
    val isGlass = uiStyle == UiStyleMode.FROSTED_GLASS_OS
    val density = LocalDensity.current
    val appContext = LocalContext.current

    fun computeSmartWallpaperFraming(result: com.example.engine.depth.DepthDetectionResult): Pair<Float, Offset> {
        val targetZoom = result.recommendedWallpaperZoom.coerceIn(1.0f, 1.80f)
        val vw = canvasSize.width.toFloat().coerceAtLeast(1f)
        val vh = canvasSize.height.toFloat().coerceAtLeast(1f)
        val maxPanX = ((vw * (targetZoom - 1f)) * 0.5f).coerceAtLeast(180f)
        val maxPanY = ((vh * (targetZoom - 1f)) * 0.5f).coerceAtLeast(220f)
        val targetPan = Offset(
            x = (result.recommendedWallpaperPanXFraction * vw).coerceIn(-maxPanX, maxPanX),
            y = (result.recommendedWallpaperPanYFraction * vh).coerceIn(-maxPanY, maxPanY)
        )
        return targetZoom to targetPan
    }

    fun animateWallpaperToDepthFraming(result: com.example.engine.depth.DepthDetectionResult) {
        val (targetZoom, targetPan) = computeSmartWallpaperFraming(result)
        val startZoom = zoomScale
        val startPan = panOffset
        if (reducedMotion) {
            zoomScale = targetZoom
            panOffset = targetPan
            return
        }
        coroutineScope.launch {
            val zAnim = Animatable(startZoom)
            val panAnim = Animatable(startPan, Offset.VectorConverter)
            val springSpec = AppMotion.spatialSpring<Float>(uiStyle, false)
            val offsetSpringSpec = AppMotion.spatialSpring<Offset>(uiStyle, false)
            launch {
                zAnim.animateTo(targetZoom, animationSpec = springSpec) {
                    zoomScale = value
                }
            }
            launch {
                panAnim.animateTo(targetPan, animationSpec = offsetSpringSpec) {
                    panOffset = value
                }
            }
        }
    }

    LaunchedEffect(depthState) {
        if (depthState is DepthStudioUiState.Ready && depthState.key != lastAutoAppliedKey) {
            lastAutoAppliedKey = depthState.key
            if (skipNextAutoSnapForSet) {
                skipNextAutoSnapForSet = false
            } else {
                onPreviewClockConfig(
                    clockConfig.copy(
                        placementY = depthState.result.recommendedClockTopFraction,
                        alignment = depthState.result.recommendedAlign,
                        offsetXFraction = 0f,
                        widthScale = depthState.result.recommendedClockScale,
                        heightScale = depthState.result.recommendedClockScale
                    )
                )
                val (targetZoom, targetPan) = computeSmartWallpaperFraming(depthState.result)
                zoomScale = targetZoom
                panOffset = targetPan
                clockColorIndex = 0
            }
        }
    }

    val isHorizontallyOffCenter = remember(clockConfig.offsetXFraction, clockConfig.alignment) {
        ClockSpatialMath.isHorizontallyOffCenter(clockConfig)
    }

    val engineColor = remember(depthState) {
        val hex = (depthState as? DepthStudioUiState.Ready)?.result?.recommendedClockColorHex
        parseHexOrNull(hex) ?: Color(0xFFF4F6FA)
    }
    val paletteColors = remember(activeWallpaper.id, engineColor) {
        val engineArgb = engineColor.toArgb()
        val wallpaperPastels = activeWallpaper.dominantColors.map { rawArgb ->
            Color(LabColorSpace.harmonizeWallpaperAccent(rawArgb.toInt(), engineArgb))
        }
        val softNeutral = Color(LabColorSpace.harmonizeWallpaperAccent(0xFFF2F0EC.toInt(), engineArgb))
        (listOf(engineColor) + wallpaperPastels + listOf(softNeutral, Color(0xFFE7DEFF), Color(0xFFCDEEE2), Color(0xFFFFD8C8)))
            .distinct()
            .take(6)
    }
    val selectedClockColor = clockConfig.customPrimaryArgb?.let { Color(it.toInt()) }
        ?: paletteColors[clockColorIndex % paletteColors.size]
    val secondaryBlendColor = clockConfig.customSecondaryArgb?.let { Color(it.toInt()) }
        ?: activeWallpaper.dominantColors.firstOrNull()?.let { rawArgb ->
            Color(LabColorSpace.harmonizeWallpaperAccent(rawArgb.toInt(), selectedClockColor.toArgb()))
        }
        ?: MaterialTheme.colorScheme.primaryContainer

    val fullBitmap = when (depthState) {
        is DepthStudioUiState.ScanningSubject -> depthState.fullBitmap
        is DepthStudioUiState.Ready -> depthState.fullBitmap
        is DepthStudioUiState.EngineError -> depthState.fullBitmap
        else -> null
    }

    @Composable
    fun FloatingUndoRedoBar(modifier: Modifier = Modifier) {
        TactileSurface(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(50),
            tonalElevation = 4.dp,
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onUndoClock,
                    enabled = canUndo,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("clock_undo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Undo,
                        contentDescription = "Undo clock edit",
                        tint = if (canUndo) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onRedoClock,
                    enabled = canRedo,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("clock_redo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Redo,
                        contentDescription = "Redo clock edit",
                        tint = if (canRedo) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (clockConfig.hasCustomProportions) {
                    Spacer(modifier = Modifier.width(2.dp))
                    StudioOptionPill(
                        label = "Reset Size",
                        selected = false,
                        onClick = onResetProportions,
                        modifier = Modifier.testTag("clock_reset_size_pill")
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))
                StudioOptionPill(
                    label = if (isClockEditing) "Done" else "Resize",
                    selected = isClockEditing,
                    onClick = { isClockEditing = !isClockEditing },
                    modifier = Modifier.testTag("clock_toggle_handles_pill")
                )

                if (onSaveLockScreenSet != null) {
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(
                        onClick = { showSaveSetDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("depth_save_set_pill")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BookmarkAdd,
                            contentDescription = "Save Lock-Screen Set",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun DepthCanvasStage(stageModifier: Modifier, isCompactStage: Boolean) {
        val marginPx = with(density) { 10.dp.toPx() }
        val clockRect = if (canvasSize.width > 0 && canvasSize.height > 0) {
            ClockSpatialMath.computeClockBoxRect(
                unscaledWidthPx = clockMeasuredSize.width.toFloat(),
                unscaledHeightPx = clockMeasuredSize.height.toFloat(),
                viewportSize = canvasSize,
                config = clockConfig,
                minMarginPx = marginPx
            )
        } else null

        Box(
            modifier = stageModifier
                .background(Color(0xFF101218))
                .onSizeChanged { canvasSize = it }
        ) {
            // Layer 1: Full-resolution wallpaper bitmap (pan/zoom locked >= 1.0f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (isClockEditing) {
                            isClockEditing = false
                        } else if (!isCompactStage) {
                            showControls = !showControls
                        }
                    }
                    .pointerInput(zoomScale) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val maxPanX = ((canvasSize.width * (zoomScale - 1f)) * 0.5f).coerceAtLeast(180f)
                            val maxPanY = ((canvasSize.height * (zoomScale - 1f)) * 0.5f).coerceAtLeast(220f)
                            panOffset = Offset(
                                x = (panOffset.x + dragAmount.x).coerceIn(-maxPanX, maxPanX),
                                y = (panOffset.y + dragAmount.y).coerceIn(-maxPanY, maxPanY)
                            )
                        }
                    }
            ) {
                if (fullBitmap != null) {
                    Image(
                        bitmap = fullBitmap.asImageBitmap(),
                        contentDescription = "${activeWallpaper.category.displayName} background",
                        contentScale = ContentScale.Crop,
                        colorFilter = lookFilter.toComposeColorFilter(),
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = panOffset.x
                                translationY = panOffset.y
                            }
                    )
                } else {
                    WallpaperAsyncImage(
                        wallpaper = activeWallpaper,
                        useFullRes = true,
                        contentDescription = "${activeWallpaper.category.displayName} background",
                        contentScale = ContentScale.Crop,
                        colorFilter = lookFilter.toComposeColorFilter(),
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = panOffset.x
                                translationY = panOffset.y
                            }
                    )
                }

                if (dimFraction > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = dimFraction.coerceIn(0f, 0.5f)))
                    )
                }
            }

            // Layer 2: Customizable Lock Clock behind the foreground subject cutout
            // Placed via rect.centerXPx & rect.topPx with TransformOrigin(0.5f, 0f) so pixels align 1:1 with overlay handles
            val clockX = if (clockRect != null && clockMeasuredSize.width > 0) {
                (clockRect.centerXPx - clockMeasuredSize.width * 0.5f).roundToInt()
            } else {
                ((canvasSize.width - clockMeasuredSize.width) * 0.5f).roundToInt()
            }
            val clockY = if (clockRect != null) {
                clockRect.topPx.roundToInt()
            } else {
                (canvasSize.height * clockConfig.placementY).roundToInt()
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(x = clockX, y = clockY) }
            ) {
                HyperOsLockClock(
                    face = clockConfig.face,
                    font = clockConfig.font,
                    finish = clockConfig.finish,
                    primaryColor = selectedClockColor,
                    secondaryColor = secondaryBlendColor,
                    weightLevel = clockConfig.weightLevel,
                    scaleFactor = clockConfig.scale,
                    widthScale = clockConfig.widthScale,
                    heightScale = clockConfig.heightScale,
                    opacity = clockConfig.opacity,
                    alignment = clockConfig.alignment,
                    useLiveTime = clockConfig.useLiveTime,
                    is24HourFormat = clockConfig.is24HourFormat,
                    showDateAndWeather = clockConfig.showDateAndWeather,
                    signatureMode = clockConfig.signatureMode,
                    signatureText = clockConfig.signatureText,
                    liquidGlassConfig = clockConfig.liquidGlass,
                    backdropBitmap = fullBitmap,
                    viewportSize = canvasSize,
                    clockBoxRect = clockRect,
                    zoomScale = zoomScale,
                    panOffset = panOffset,
                    dimFraction = dimFraction,
                    lookFilter = lookFilter,
                    onMeasuredUnscaledSize = { measured ->
                        if (measured.width > 0 && measured.height > 0) {
                            clockMeasuredSize = measured
                        }
                    }
                )
            }

            // Layer 3: Pixel-accurate Depth Engine Foreground Subject Cutout
            val cutoutBmp = (depthState as? DepthStudioUiState.Ready)?.result?.cutoutBitmap
            if (clockConfig.depthOverlapEnabled && cutoutBmp != null) {
                Image(
                    bitmap = cutoutBmp.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = lookFilter.toComposeColorFilter(),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = zoomScale
                            scaleY = zoomScale
                            translationX = panOffset.x
                            translationY = panOffset.y
                        }
                )
            }

            // Layer 3.5: Tap-to-Reveal & Single-Grip Continuous Resize/Drag Overlay
            if (!isClockEditing && clockRect != null && clockMeasuredSize.width > 0) {
                val hitW = with(density) { clockRect.widthPx.toDp() }
                val hitH = with(density) { clockRect.heightPx.toDp() }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = clockRect.leftPx.roundToInt(),
                                y = clockRect.topPx.roundToInt()
                            )
                        }
                        .size(width = hitW, height = hitH)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isClockEditing = true }
                        .testTag("clock_tap_hit_box")
                )
            } else if (isClockEditing && canvasSize.width > 0 && clockMeasuredSize.width > 0) {
                ClockBoundingOverlay(
                    clockUnscaledWidthPx = clockMeasuredSize.width,
                    clockUnscaledHeightPx = clockMeasuredSize.height,
                    viewportSize = canvasSize,
                    config = clockConfig,
                    onGestureStart = onBeginClockGesture,
                    onUpdateConfig = onPreviewClockConfig,
                    onGestureEnd = onEndClockGesture,
                    onResetFrame = onResetProportions,
                    onToggleAspectLock = {
                        haptics.performTick(24L)
                        onCommitClockConfig { it.copy(aspectAdaptiveEnabled = !it.aspectAdaptiveEnabled) }
                    },
                    onDoneEditing = { isClockEditing = false }
                )
            }

            // Floating Top HUD inside Stage
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { if (!isCompactStage) it.statusBarsPadding() else it }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TactileHudIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack,
                    modifier = Modifier.testTag("depth_back_button")
                )

                val isRetryableError = depthState is DepthStudioUiState.EngineError || depthState is DepthStudioUiState.BitmapError
                TactileSurface(
                    containerColor = if (isRetryableError) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(50),
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = isRetryableError) {
                            val uri = customPhotoUri
                            if (uri != null) onPickCustomPhoto(uri, true)
                            else onLoadWallpaper(activeWallpaper, true)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (depthState) {
                            is DepthStudioUiState.LoadingFullBitmap -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Loading canvas...",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            is DepthStudioUiState.ScanningSubject -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Mapping Depth Contour...",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            is DepthStudioUiState.Ready -> {
                                Icon(
                                    imageVector = if (depthState.result.isDepthEligible) Icons.Rounded.AutoAwesome else Icons.Rounded.Layers,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (depthState.result.isDepthEligible) {
                                        val overlapPct = (depthState.result.recommendedClockOverlapFraction * 100f).roundToInt()
                                        if (overlapPct >= 4) {
                                            "Depth ${depthState.result.depthQualityScore}% • ${overlapPct}% tuck"
                                        } else {
                                            "Depth ${depthState.result.depthQualityScore}% • ${depthState.result.subjectLabel}"
                                        }
                                    } else {
                                        "Flat canvas"
                                    },
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            is DepthStudioUiState.EngineError, is DepthStudioUiState.BitmapError -> {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Scan failed • Retry",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TactileHudIconButton(
                        icon = Icons.Rounded.PhotoLibrary,
                        contentDescription = "Pick photo from gallery",
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    TactileHudIconButton(
                        icon = Icons.Rounded.Refresh,
                        contentDescription = "Re-scan depth subject",
                        onClick = {
                            val uri = customPhotoUri
                            if (uri != null) onPickCustomPhoto(uri, true)
                            else onLoadWallpaper(activeWallpaper, true)
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun DepthControlsBody(alwaysExpanded: Boolean) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .let { if (alwaysExpanded) it.fillMaxHeight() else it }
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (alwaysExpanded) {
                FloatingUndoRedoBar(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .let { mod -> if (!alwaysExpanded) mod.clickable { showControls = !showControls } else mod }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Depth Studio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (customPhotoUri != null) "Custom Gallery Photo" else "${activeWallpaper.category.displayName} • ${activeWallpaper.resolutionTier.badge}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!alwaysExpanded) {
                    Icon(
                        imageVector = if (showControls) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                        contentDescription = if (showControls) "Collapse controls" else "Expand controls",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = alwaysExpanded || showControls,
                enter = AppMotion.drawerEnter(uiStyle, reducedMotion),
                exit = AppMotion.drawerExit(uiStyle, reducedMotion),
                modifier = if (alwaysExpanded) Modifier.weight(1f) else Modifier
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = if (alwaysExpanded) Modifier.fillMaxHeight() else Modifier
                ) {
                    StudioSegmentedBar(
                        options = DepthSectionTab.entries,
                        selected = activeTab,
                        labelProvider = { it.label },
                        onSelect = { activeTab = it },
                        tagPrefix = "depth_tab"
                    )

                    val tabContentScrollModifier = if (alwaysExpanded) {
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    } else {
                        Modifier
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState())
                    }

                    Column(
                        modifier = tabContentScrollModifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when (activeTab) {
                            DepthSectionTab.SUBJECT -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Depth Engine Layer",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val subtitle = when (depthState) {
                                            is DepthStudioUiState.Ready -> if (depthState.result.isDepthEligible && depthState.result.cutoutBitmap != null) {
                                                val overlapPct = (depthState.result.recommendedClockOverlapFraction * 100f).roundToInt()
                                                val tuckLabel = if (overlapPct >= 4) " • ${overlapPct}% clock tuck" else ""
                                                "${depthState.result.subjectCategory.displayName}$tuckLabel • Score ${depthState.result.depthQualityScore}/100"
                                            } else {
                                                "No isolated foreground subject above horizon"
                                            }
                                            is DepthStudioUiState.ScanningSubject -> "Extracting geodesic subject contour..."
                                            is DepthStudioUiState.LoadingFullBitmap -> "Loading full-resolution image..."
                                            is DepthStudioUiState.EngineError -> depthState.message
                                            is DepthStudioUiState.BitmapError -> depthState.message
                                        }
                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (depthState is DepthStudioUiState.EngineError || depthState is DepthStudioUiState.BitmapError) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Switch(
                                        checked = clockConfig.depthOverlapEnabled,
                                        onCheckedChange = { enabled ->
                                            onCommitClockConfig { it.copy(depthOverlapEnabled = enabled) }
                                        },
                                        modifier = Modifier.testTag("depth_overlap_switch")
                                    )
                                }

                                if (depthState is DepthStudioUiState.EngineError || depthState is DepthStudioUiState.BitmapError) {
                                    OutlinedButton(
                                        onClick = {
                                            val uri = customPhotoUri
                                            if (uri != null) onPickCustomPhoto(uri, true)
                                            else onLoadWallpaper(activeWallpaper, true)
                                        },
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Retry Depth Scan")
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Clock Y",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    AdaptiveDesignSlider(
                                        value = clockConfig.placementY,
                                        onValueChange = { y ->
                                            onPreviewClockConfig(clockConfig.copy(placementY = y))
                                        },
                                        valueRange = 0.04f..0.84f,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("depth_clock_y_slider")
                                    )
                                    Text(
                                        text = "${(clockConfig.placementY * 100).roundToInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isHorizontallyOffCenter) {
                                        FilledTonalButton(
                                            onClick = {
                                                haptics.performTick(25L)
                                                onCenterClock()
                                            },
                                            shape = RoundedCornerShape(50),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("depth_center_clock_button")
                                        ) {
                                            Icon(
                                                Icons.Rounded.CenterFocusStrong,
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Center", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = onCenterClock,
                                            enabled = false,
                                            shape = RoundedCornerShape(50),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("depth_center_clock_button")
                                        ) {
                                            Icon(
                                                Icons.Rounded.CenterFocusStrong,
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Centered", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            haptics.performSnapDetent()
                                            onAutoSnapClock()
                                            (depthState as? DepthStudioUiState.Ready)?.result?.let { readyResult ->
                                                animateWallpaperToDepthFraming(readyResult)
                                            }
                                        },
                                        shape = RoundedCornerShape(50),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("depth_auto_snap_button")
                                    ) {
                                        Icon(
                                            Icons.Rounded.VerticalAlignTop,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Auto Snap", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }

                                    FilledTonalButton(
                                        onClick = { isClockEditing = !isClockEditing },
                                        shape = RoundedCornerShape(50),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("clock_toggle_handles_chip")
                                    ) {
                                        Icon(
                                            Icons.Rounded.OpenWith,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isClockEditing) "Done" else "Resize", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }

                            DepthSectionTab.CLOCK -> {
                                // Sub-pills keep the Clock workspace sleek and decluttered
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(ClockSubSection.entries, key = { it.name }) { sub ->
                                        StudioOptionPill(
                                            label = sub.label,
                                            selected = clockSubSection == sub,
                                            onClick = { clockSubSection = sub },
                                            modifier = Modifier.testTag("clock_subpill_${sub.name}")
                                        )
                                    }
                                }

                                when (clockSubSection) {
                                    ClockSubSection.STYLE -> {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            contentPadding = PaddingValues(end = 8.dp),
                                            modifier = Modifier.testTag("clock_face_strip")
                                        ) {
                                            items(HyperOsClockFace.entries, key = { it.name }) { face ->
                                                HyperOsClockFaceThumbnail(
                                                    face = face,
                                                    selected = clockConfig.face == face,
                                                    accentColor = secondaryBlendColor,
                                                    onClick = {
                                                        haptics.performSnapDetent()
                                                        onCommitClockConfig {
                                                            it.copy(
                                                                face = face,
                                                                font = face.recommendedFont,
                                                                weightLevel = if (face == HyperOsClockFace.POSTER_SPLIT || face == HyperOsClockFace.SPIRE_MONOLITH) 850 else 700,
                                                                scale = 1.0f,
                                                                widthScale = 1.0f,
                                                                heightScale = 1.0f,
                                                                baselineScale = 1.0f,
                                                                baselineWidthScale = 1.0f,
                                                                baselineHeightScale = 1.0f
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    ClockSubSection.TYPE_FINISH -> {
                                        // Direct curated font strip (each pill previewed in its own typeface; no category clutter)
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(end = 8.dp),
                                            modifier = Modifier.testTag("clock_font_strip")
                                        ) {
                                            items(HyperOsClockFont.entries, key = { it.name }) { fontItem ->
                                                val selectedFont = clockConfig.font == fontItem
                                                val pillBg = if (selectedFont) {
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isGlass) 0.94f else 1f)
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = if (isGlass) 0.84f else 1f)
                                                }
                                                val pillFg = if (selectedFont) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                                Surface(
                                                    color = pillBg,
                                                    contentColor = pillFg,
                                                    shape = RoundedCornerShape(50),
                                                    border = if (selectedFont) {
                                                        androidx.compose.foundation.BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary)
                                                    } else {
                                                        glassSpecularBorder()
                                                    },
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(50))
                                                        .clickable {
                                                            haptics.performTick(24L)
                                                            onCommitClockConfig { it.copy(font = fontItem) }
                                                        }
                                                        .testTag("clock_font_${fontItem.name}")
                                                ) {
                                                    Text(
                                                        text = fontItem.label,
                                                        fontFamily = fontItem.fontFamily,
                                                        fontWeight = if (selectedFont) FontWeight.Bold else FontWeight.Medium,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = pillFg,
                                                        maxLines = 1,
                                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                                    )
                                                }
                                            }
                                        }

                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(HyperOsClockFinish.entries, key = { it.name }) { fin ->
                                                StudioOptionPill(
                                                    label = fin.label,
                                                    selected = clockConfig.finish == fin,
                                                    onClick = {
                                                        val defaultAlpha = when (fin) {
                                                            HyperOsClockFinish.TRANSLUCENT -> 0.62f
                                                            HyperOsClockFinish.BLUR_GLASS -> 0.96f
                                                            else -> clockConfig.opacity.coerceAtLeast(0.88f)
                                                        }
                                                        onCommitClockConfig { it.copy(finish = fin, opacity = defaultAlpha) }
                                                    },
                                                    modifier = Modifier.testTag("clock_finish_${fin.name}")
                                                )
                                            }
                                        }

                                        StudioParamSlider(
                                            label = "Weight",
                                            valueText = "${clockConfig.weightLevel}",
                                            value = clockConfig.weightLevel.toFloat(),
                                            valueRange = 100f..900f,
                                            onValueChange = { w ->
                                                onPreviewClockConfig(clockConfig.copy(weightLevel = w.roundToInt()))
                                            },
                                            sliderTag = "clock_weight_slider"
                                        )
                                        StudioParamSlider(
                                            label = "Opacity",
                                            valueText = "${(clockConfig.opacity * 100).roundToInt()}%",
                                            value = clockConfig.opacity,
                                            valueRange = 0.20f..1.0f,
                                            onValueChange = { a ->
                                                onPreviewClockConfig(clockConfig.copy(opacity = a))
                                            },
                                            sliderTag = "clock_opacity_slider"
                                        )

                                        if (clockConfig.finish == HyperOsClockFinish.BLUR_GLASS) {
                                            val glassCfg = clockConfig.liquidGlass
                                            TactileSurface(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                                shape = RoundedCornerShape(18.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("glass_optics_drawer_card")
                                            ) {
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(18.dp))
                                                            .clickable { showGlassOpticsDrawer = !showGlassOpticsDrawer }
                                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                                            .testTag("glass_optics_drawer_toggle"),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.Opacity,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Column {
                                                                Text(
                                                                    text = "Glass Optics",
                                                                    style = MaterialTheme.typography.titleSmall,
                                                                    color = MaterialTheme.colorScheme.onSurface
                                                                )
                                                                Text(
                                                                    text = "Refraction ${glassCfg.refractionStrength.roundToInt()} • IOR ${String.format(Locale.US, "%.2f", glassCfg.ior)} • ${glassCfg.activePresetOrNull()?.label ?: "Custom Tint"}",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }
                                                        Icon(
                                                            imageVector = if (showGlassOpticsDrawer) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                                            contentDescription = if (showGlassOpticsDrawer) "Collapse Glass Optics" else "Expand Glass Optics",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }

                                                    AnimatedVisibility(
                                                        visible = showGlassOpticsDrawer,
                                                        enter = AppMotion.drawerEnter(uiStyle, reducedMotion),
                                                        exit = AppMotion.drawerExit(uiStyle, reducedMotion)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                items(LiquidGlassPreset.entries, key = { it.name }) { preset ->
                                                                    val isPresetSelected = (glassCfg.tintArgb and 0x00FFFFFFL) == (preset.tintArgb and 0x00FFFFFFL)
                                                                    StudioOptionPill(
                                                                        label = preset.label,
                                                                        selected = isPresetSelected,
                                                                        onClick = {
                                                                            onCommitClockConfig { cfg ->
                                                                                cfg.copy(
                                                                                    liquidGlass = cfg.liquidGlass.copy(
                                                                                        tintArgb = preset.tintArgb,
                                                                                        tintOpacity = preset.tintOpacity,
                                                                                        glassSaturation = preset.saturation,
                                                                                        chromaticAberration = preset.chromaticAberration
                                                                                    ).sanitized()
                                                                                )
                                                                            }
                                                                        }
                                                                    )
                                                                }
                                                            }

                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Column(modifier = Modifier.weight(1f)) {
                                                                    Text(
                                                                        text = "Adaptive Wallpaper Contrast",
                                                                        style = MaterialTheme.typography.labelMedium,
                                                                        color = MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                    Text(
                                                                        text = "Auto-tunes rim glow & depth shadows by sky brightness",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                }
                                                                Switch(
                                                                    checked = glassCfg.adaptiveContrast,
                                                                    onCheckedChange = { enabled ->
                                                                        onCommitClockConfig {
                                                                            it.copy(liquidGlass = it.liquidGlass.copy(adaptiveContrast = enabled))
                                                                        }
                                                                    },
                                                                    modifier = Modifier.testTag("glass_adaptive_contrast_switch")
                                                                )
                                                            }

                                                            StudioParamSlider(
                                                                label = "Refract",
                                                                valueText = String.format(Locale.US, "%.0f", glassCfg.refractionStrength),
                                                                value = glassCfg.refractionStrength,
                                                                valueRange = 0f..40f,
                                                                onValueChange = { v ->
                                                                    onPreviewClockConfig(clockConfig.copy(liquidGlass = glassCfg.copy(refractionStrength = v).sanitized()))
                                                                },
                                                                sliderTag = "glass_refraction_slider"
                                                            )
                                                            StudioParamSlider(
                                                                label = "IOR",
                                                                valueText = String.format(Locale.US, "%.2f", glassCfg.ior),
                                                                value = glassCfg.ior,
                                                                valueRange = 1.00f..1.80f,
                                                                onValueChange = { v ->
                                                                    onPreviewClockConfig(clockConfig.copy(liquidGlass = glassCfg.copy(ior = v).sanitized()))
                                                                },
                                                                sliderTag = "glass_ior_slider"
                                                            )
                                                            StudioParamSlider(
                                                                label = "Bevel",
                                                                valueText = "${glassCfg.bevelWidth.roundToInt()}px",
                                                                value = glassCfg.bevelWidth,
                                                                valueRange = 2f..24f,
                                                                onValueChange = { v ->
                                                                    onPreviewClockConfig(clockConfig.copy(liquidGlass = glassCfg.copy(bevelWidth = v).sanitized()))
                                                                },
                                                                sliderTag = "glass_bevel_slider"
                                                            )
                                                            StudioParamSlider(
                                                                label = "Prism",
                                                                valueText = "${(glassCfg.chromaticAberration * 100).roundToInt()}%",
                                                                value = glassCfg.chromaticAberration,
                                                                valueRange = 0f..1f,
                                                                onValueChange = { v ->
                                                                    onPreviewClockConfig(clockConfig.copy(liquidGlass = glassCfg.copy(chromaticAberration = v).sanitized()))
                                                                },
                                                                sliderTag = "glass_chromatic_slider"
                                                            )
                                                            StudioParamSlider(
                                                                label = "Frost",
                                                                valueText = "${glassCfg.frostBlur.roundToInt()}px",
                                                                value = glassCfg.frostBlur,
                                                                valueRange = 0f..24f,
                                                                onValueChange = { v ->
                                                                    onPreviewClockConfig(clockConfig.copy(liquidGlass = glassCfg.copy(frostBlur = v).sanitized()))
                                                                },
                                                                sliderTag = "glass_frost_slider"
                                                            )
                                                            StudioParamSlider(
                                                                label = "Vivid",
                                                                valueText = String.format(Locale.US, "%.2fx", glassCfg.glassSaturation),
                                                                value = glassCfg.glassSaturation,
                                                                valueRange = 1.0f..2.0f,
                                                                onValueChange = { v ->
                                                                    onPreviewClockConfig(clockConfig.copy(liquidGlass = glassCfg.copy(glassSaturation = v).sanitized()))
                                                                },
                                                                sliderTag = "glass_saturation_slider"
                                                            )
                                                            StudioParamSlider(
                                                                label = "Tint",
                                                                valueText = "${(glassCfg.tintOpacity * 100).roundToInt()}%",
                                                                value = glassCfg.tintOpacity,
                                                                valueRange = 0f..0.50f,
                                                                onValueChange = { v ->
                                                                    onPreviewClockConfig(clockConfig.copy(liquidGlass = glassCfg.copy(tintOpacity = v).sanitized()))
                                                                },
                                                                sliderTag = "glass_tint_opacity_slider"
                                                            )
                                                            StudioParamSlider(
                                                                label = "Gloss",
                                                                valueText = "${(glassCfg.specularIntensity * 100).roundToInt()}%",
                                                                value = glassCfg.specularIntensity,
                                                                valueRange = 0f..1.50f,
                                                                onValueChange = { v ->
                                                                    onPreviewClockConfig(clockConfig.copy(liquidGlass = glassCfg.copy(specularIntensity = v).sanitized()))
                                                                },
                                                                sliderTag = "glass_specular_slider"
                                                            )
                                                            StudioParamSlider(
                                                                label = "Light",
                                                                valueText = "${glassCfg.lightAngle.roundToInt()}°",
                                                                value = glassCfg.lightAngle,
                                                                valueRange = 0f..360f,
                                                                onValueChange = { v ->
                                                                    onPreviewClockConfig(clockConfig.copy(liquidGlass = glassCfg.copy(lightAngle = v).sanitized()))
                                                                },
                                                                sliderTag = "glass_light_angle_slider"
                                                            )
                                                            StudioParamSlider(
                                                                label = "Rim Dark",
                                                                valueText = "${(glassCfg.innerThicknessShadow * 100).roundToInt()}%",
                                                                value = glassCfg.innerThicknessShadow,
                                                                valueRange = 0f..0.60f,
                                                                onValueChange = { v ->
                                                                    onPreviewClockConfig(clockConfig.copy(liquidGlass = glassCfg.copy(innerThicknessShadow = v).sanitized()))
                                                                },
                                                                sliderTag = "glass_inner_shadow_slider"
                                                            )
                                                            StudioParamSlider(
                                                                label = "Depth",
                                                                valueText = "${(glassCfg.backdropDropShadow * 100).roundToInt()}%",
                                                                value = glassCfg.backdropDropShadow,
                                                                valueRange = 0f..1.0f,
                                                                onValueChange = { v ->
                                                                    onPreviewClockConfig(clockConfig.copy(liquidGlass = glassCfg.copy(backdropDropShadow = v).sanitized()))
                                                                },
                                                                sliderTag = "glass_drop_shadow_slider"
                                                            )

                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.End
                                                            ) {
                                                                OutlinedButton(
                                                                    onClick = {
                                                                        onCommitClockConfig { it.copy(liquidGlass = LiquidGlassConfig()) }
                                                                    },
                                                                    shape = RoundedCornerShape(50),
                                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                                                ) {
                                                                    Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                                                    Spacer(modifier = Modifier.width(4.dp))
                                                                    Text("Reset Optics", style = MaterialTheme.typography.labelSmall)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    ClockSubSection.COLOR_LAYOUT -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                paletteColors.forEachIndexed { idx, col ->
                                                    val chosen = clockConfig.customPrimaryArgb == null &&
                                                        clockColorIndex % paletteColors.size == idx
                                                    Box(
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .clip(CircleShape)
                                                            .background(col)
                                                            .border(
                                                                width = if (chosen) 2.5.dp else 1.dp,
                                                                color = if (chosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                                shape = CircleShape
                                                            )
                                                            .clickable {
                                                                haptics.performTick(24L)
                                                                clockColorIndex = idx
                                                                onCommitClockConfig {
                                                                    it.copy(customPrimaryArgb = null)
                                                                }
                                                            }
                                                    )
                                                }
                                                TactileSurface(
                                                    containerColor = if (showCustomColorPicker || clockConfig.customPrimaryArgb != null) {
                                                        MaterialTheme.colorScheme.primaryContainer
                                                    } else {
                                                        MaterialTheme.colorScheme.surfaceContainerHighest
                                                    },
                                                    shape = CircleShape,
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .clickable { showCustomColorPicker = !showCustomColorPicker }
                                                        .testTag("clock_custom_color_toggle")
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Palette,
                                                            contentDescription = "Custom color picker",
                                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            SingleChoiceSegmentedButtonRow {
                                                val aligns = listOf(
                                                    ClockHorizontalAlign.START to Icons.AutoMirrored.Rounded.FormatAlignLeft,
                                                    ClockHorizontalAlign.CENTER to Icons.Rounded.FormatAlignCenter,
                                                    ClockHorizontalAlign.END to Icons.AutoMirrored.Rounded.FormatAlignRight
                                                )
                                                aligns.forEachIndexed { idx, (al, icon) ->
                                                    SegmentedButton(
                                                        selected = clockConfig.alignment == al,
                                                        onClick = {
                                                            haptics.performTick(24L)
                                                            onCommitClockConfig { it.copy(alignment = al) }
                                                        },
                                                        colors = SegmentedButtonDefaults.colors(
                                                            activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                            activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                            inactiveContentColor = MaterialTheme.colorScheme.onSurface
                                                        ),
                                                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = aligns.size)
                                                    ) {
                                                        Icon(icon, contentDescription = al.name, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }

                                        AnimatedVisibility(
                                            visible = showCustomColorPicker,
                                            enter = AppMotion.drawerEnter(uiStyle, reducedMotion),
                                            exit = AppMotion.drawerExit(uiStyle, reducedMotion)
                                        ) {
                                            TactileSurface(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                                shape = RoundedCornerShape(18.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            CustomColorTarget.entries.forEach { tgt ->
                                                                StudioOptionPill(
                                                                    label = tgt.label,
                                                                    selected = colorTarget == tgt,
                                                                    onClick = { colorTarget = tgt }
                                                                )
                                                            }
                                                        }
                                                        TextButton(
                                                            onClick = {
                                                                onCommitClockConfig {
                                                                    it.copy(customPrimaryArgb = null, customSecondaryArgb = null)
                                                                }
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                                        ) {
                                                            Text("Adaptive")
                                                        }
                                                    }
                                                    val previewPicked = Color.hsv(pickerHue, pickerSat, pickerVal)
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(22.dp)
                                                                .clip(CircleShape)
                                                                .background(previewPicked)
                                                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(8.dp)
                                                                .clip(RoundedCornerShape(50))
                                                                .background(
                                                                    Brush.horizontalGradient(
                                                                        listOf(
                                                                            Color.Red, Color.Yellow, Color.Green,
                                                                            Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                                                        )
                                                                    )
                                                                )
                                                        )
                                                    }
                                                    AdaptiveDesignSlider(
                                                        value = pickerHue,
                                                        onValueChange = { h ->
                                                            pickerHue = h
                                                            val argb = Color.hsv(h, pickerSat, pickerVal).toArgb().toLong() and 0xFFFFFFFFL
                                                            onPreviewClockConfig(
                                                                if (colorTarget == CustomColorTarget.PRIMARY) {
                                                                    clockConfig.copy(customPrimaryArgb = argb)
                                                                } else {
                                                                    clockConfig.copy(customSecondaryArgb = argb)
                                                                }
                                                            )
                                                        },
                                                        valueRange = 0f..360f,
                                                        thumbColorOverride = previewPicked,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .testTag("clock_color_hue_slider")
                                                    )
                                                    StudioParamSlider(
                                                        label = "Tint",
                                                        valueText = "${(pickerSat * 100).roundToInt()}%",
                                                        value = pickerSat,
                                                        valueRange = 0f..1f,
                                                        onValueChange = { s ->
                                                            pickerSat = s
                                                            val argb = Color.hsv(pickerHue, s, pickerVal).toArgb().toLong() and 0xFFFFFFFFL
                                                            onPreviewClockConfig(
                                                                if (colorTarget == CustomColorTarget.PRIMARY) {
                                                                    clockConfig.copy(customPrimaryArgb = argb)
                                                                } else {
                                                                    clockConfig.copy(customSecondaryArgb = argb)
                                                                }
                                                            )
                                                        },
                                                        sliderTag = "clock_color_sat_slider"
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "W ${(clockConfig.widthScale * 100).roundToInt()}% • H ${(clockConfig.heightScale * 100).roundToInt()}%",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                StudioOptionPill(
                                                    label = if (clockConfig.aspectAdaptiveEnabled) "Auto-Fit" else "Locked",
                                                    selected = clockConfig.aspectAdaptiveEnabled,
                                                    onClick = {
                                                        onCommitClockConfig { it.copy(aspectAdaptiveEnabled = !it.aspectAdaptiveEnabled) }
                                                    },
                                                    modifier = Modifier.testTag("clock_color_aspect_lock_pill")
                                                )
                                                OutlinedButton(
                                                    onClick = {
                                                        haptics.performSnapDetent()
                                                        onResetProportions()
                                                    },
                                                    enabled = clockConfig.hasCustomProportions,
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    shape = RoundedCornerShape(50),
                                                    modifier = Modifier.testTag("clock_reset_proportions_button")
                                                ) {
                                                    Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Reset", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        }

                                        StudioParamSlider(
                                            label = "Width",
                                            valueText = "${(clockConfig.widthScale * 100).roundToInt()}%",
                                            value = clockConfig.widthScale,
                                            valueRange = ClockSpatialMath.MIN_WIDTH_SCALE..ClockSpatialMath.MAX_WIDTH_SCALE,
                                            onValueChange = { w ->
                                                val raw = clockConfig.copy(widthScale = w)
                                                val resolved = if (canvasSize.width > 0 && clockMeasuredSize.width > 0) {
                                                    ClockSpatialMath.applyAspectAdaptiveResolution(
                                                        updatedConfig = raw,
                                                        unscaledWidthPx = clockMeasuredSize.width.toFloat(),
                                                        unscaledHeightPx = clockMeasuredSize.height.toFloat(),
                                                        viewportSize = canvasSize
                                                    )
                                                } else raw
                                                if (resolved.face != clockConfig.face) {
                                                    haptics.performSnapDetent()
                                                }
                                                onPreviewClockConfig(resolved)
                                            },
                                            sliderTag = "clock_width_slider"
                                        )
                                        StudioParamSlider(
                                            label = "Height",
                                            valueText = "${(clockConfig.heightScale * 100).roundToInt()}%",
                                            value = clockConfig.heightScale,
                                            valueRange = ClockSpatialMath.MIN_HEIGHT_SCALE..ClockSpatialMath.MAX_HEIGHT_SCALE,
                                            onValueChange = { h ->
                                                val raw = clockConfig.copy(heightScale = h)
                                                val resolved = if (canvasSize.width > 0 && clockMeasuredSize.width > 0) {
                                                    ClockSpatialMath.applyAspectAdaptiveResolution(
                                                        updatedConfig = raw,
                                                        unscaledWidthPx = clockMeasuredSize.width.toFloat(),
                                                        unscaledHeightPx = clockMeasuredSize.height.toFloat(),
                                                        viewportSize = canvasSize
                                                    )
                                                } else raw
                                                if (resolved.face != clockConfig.face) {
                                                    haptics.performSnapDetent()
                                                }
                                                onPreviewClockConfig(resolved)
                                            },
                                            sliderTag = "clock_height_slider"
                                        )

                                        // Secondary toggles & signature tucked cleanly in a collapsible drawer
                                        TactileSurface(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                            shape = RoundedCornerShape(18.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(18.dp))
                                                        .clickable { showFormatAndSignatureDrawer = !showFormatAndSignatureDrawer }
                                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                                        .testTag("format_signature_drawer_toggle"),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Tune,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(17.dp)
                                                        )
                                                        Text(
                                                            text = "Format & Signature",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = if (showFormatAndSignatureDrawer) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                                        contentDescription = if (showFormatAndSignatureDrawer) "Collapse Format & Signature" else "Expand Format & Signature",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                AnimatedVisibility(
                                                    visible = showFormatAndSignatureDrawer,
                                                    enter = AppMotion.drawerEnter(uiStyle, reducedMotion),
                                                    exit = AppMotion.drawerExit(uiStyle, reducedMotion)
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            item {
                                                                StudioOptionPill(
                                                                    label = if (clockConfig.is24HourFormat) "24h" else "12h",
                                                                    selected = !clockConfig.is24HourFormat,
                                                                    onClick = {
                                                                        onCommitClockConfig { it.copy(is24HourFormat = !it.is24HourFormat) }
                                                                    },
                                                                    modifier = Modifier.testTag("clock_time_format_pill")
                                                                )
                                                            }
                                                            item {
                                                                StudioOptionPill(
                                                                    label = "Date & Weather",
                                                                    selected = clockConfig.showDateAndWeather,
                                                                    onClick = {
                                                                        onCommitClockConfig {
                                                                            val nextShow = !it.showDateAndWeather
                                                                            it.copy(
                                                                                showDateAndWeather = nextShow,
                                                                                signatureMode = if (!nextShow) ClockSignatureMode.PURE_TIME
                                                                                else if (it.signatureMode == ClockSignatureMode.PURE_TIME) ClockSignatureMode.DATE_WEATHER
                                                                                else it.signatureMode
                                                                            )
                                                                        }
                                                                    }
                                                                )
                                                            }
                                                            item {
                                                                StudioOptionPill(
                                                                    label = "Live Clock",
                                                                    selected = clockConfig.useLiveTime,
                                                                    onClick = {
                                                                        onCommitClockConfig { it.copy(useLiveTime = !it.useLiveTime) }
                                                                    }
                                                                )
                                                            }
                                                        }

                                                        LazyRow(
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            modifier = Modifier.testTag("clock_signature_mode_strip")
                                                        ) {
                                                            items(ClockSignatureMode.entries, key = { it.name }) { mode ->
                                                                StudioOptionPill(
                                                                    label = mode.label,
                                                                    selected = clockConfig.signatureMode == mode,
                                                                    onClick = {
                                                                        onCommitClockConfig {
                                                                            it.copy(
                                                                                signatureMode = mode,
                                                                                showDateAndWeather = mode != ClockSignatureMode.PURE_TIME
                                                                            )
                                                                        }
                                                                    },
                                                                    modifier = Modifier.testTag("clock_signature_mode_${mode.name}")
                                                                )
                                                            }
                                                        }

                                                        OutlinedTextField(
                                                            value = clockConfig.signatureText,
                                                            onValueChange = { txt ->
                                                                onPreviewClockConfig(clockConfig.copy(signatureText = txt.take(36)))
                                                            },
                                                            label = { Text("Custom Signature") },
                                                            placeholder = { Text("e.g. QUIET HORIZONS") },
                                                            singleLine = true,
                                                            colors = OutlinedTextFieldDefaults.colors(
                                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                            ),
                                                            trailingIcon = {
                                                                if (clockConfig.signatureText.isNotEmpty()) {
                                                                    IconButton(onClick = {
                                                                        onCommitClockConfig { it.copy(signatureText = "") }
                                                                    }) {
                                                                        Icon(Icons.Rounded.Close, contentDescription = "Clear signature")
                                                                    }
                                                                }
                                                            },
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .testTag("clock_signature_input")
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            DepthSectionTab.WALLPAPER -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Zoom",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    AdaptiveDesignSlider(
                                        value = zoomScale,
                                        onValueChange = { zoomScale = it },
                                        valueRange = 1.0f..1.80f,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("depth_zoom_slider")
                                    )
                                    TextButton(
                                        onClick = {
                                            zoomScale = 1.0f
                                            panOffset = Offset.Zero
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Text("${(zoomScale * 100).roundToInt()}%")
                                    }
                                }

                                StudioParamSlider(
                                    label = "Dim",
                                    valueText = "${(dimFraction * 100).roundToInt()}%",
                                    value = dimFraction,
                                    valueRange = 0f..0.45f,
                                    onValueChange = { dimFraction = it },
                                    sliderTag = "depth_dim_slider"
                                )

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(CanvasLookFilter.entries) { filter ->
                                        StudioOptionPill(
                                            label = filter.label,
                                            selected = lookFilter == filter,
                                            onClick = { lookFilter = filter }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { showApplyDialog = true },
                enabled = !isApplying,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("depth_apply_button")
            ) {
                if (isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Applying Wallpaper...")
                } else {
                    Icon(Icons.Rounded.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set Wallpaper")
                }
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight && maxWidth >= 520.dp

        if (isLandscape) {
            TactileAmbientBackdrop {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing.union(WindowInsets.displayCutout))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        color = Color(0xFF101218),
                        shape = RoundedCornerShape(28.dp),
                        border = glassSpecularBorder(),
                        shadowElevation = if (isGlass) 8.dp else 2.dp,
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(28.dp))
                    ) {
                        DepthCanvasStage(
                            stageModifier = Modifier.fillMaxSize(),
                            isCompactStage = true
                        )
                    }

                    TactileSurface(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(28.dp),
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .widthIn(min = 310.dp, max = 430.dp)
                            .weight(0.9f)
                            .fillMaxHeight()
                    ) {
                        DepthControlsBody(alwaysExpanded = true)
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF101218))) {
                DepthCanvasStage(
                    stageModifier = Modifier.fillMaxSize(),
                    isCompactStage = false
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingUndoRedoBar(
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TactileSurface(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DepthControlsBody(alwaysExpanded = false)
                    }
                }
            }
        }
    }

    if (showApplyDialog) {
        StudioWallpaperTargetDialog(
            title = if (customPhotoUri != null) "Custom Depth Photo" else "${activeWallpaper.category.displayName} Canvas",
            onDismiss = { showApplyDialog = false },
            onSelectTarget = { target, allowHomeParallax ->
                showApplyDialog = false
                val screenAspect = resolveDevicePortraitAspect(appContext)
                val parallaxWidth = allowHomeParallax && target != WallpaperTarget.LOCK
                if (fullBitmap != null) {
                    val bitmapToApply = renderEditedWallpaperBitmap(
                        source = fullBitmap,
                        zoomScale = zoomScale,
                        panOffset = panOffset,
                        viewportWidthPx = canvasSize.width.toFloat(),
                        viewportHeightPx = canvasSize.height.toFloat(),
                        dimFraction = dimFraction,
                        lookFilter = lookFilter,
                        targetScreenAspect = screenAspect,
                        allowParallaxWidth = parallaxWidth
                    )
                    onApplyCustomBitmapWallpaper(bitmapToApply, target, allowHomeParallax)
                } else {
                    onApplyWallpaper(activeWallpaper, target, allowHomeParallax)
                }
            }
        )
    }

    if (showSaveSetDialog && onSaveLockScreenSet != null) {
        val defaultTitle = remember(activeWallpaper.id, customPhotoUri) {
            if (customPhotoUri != null) "Custom Lock Setup" else "${activeWallpaper.category.displayName} Lock Setup"
        }
        var setNameInput by remember(showSaveSetDialog) { mutableStateOf(defaultTitle) }
        AlertDialog(
            onDismissRequest = { showSaveSetDialog = false },
            title = { Text("Save Lock-Screen Set") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Save your wallpaper crop, depth tuck, clock style, and glass optics locally to Collections.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = setNameInput,
                        onValueChange = { setNameInput = it.take(40) },
                        label = { Text("Set Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_set_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val safeName = setNameInput.trim().ifEmpty { defaultTitle }
                        val doc = LockScreenSetDoc(
                            id = initialSet?.id.orEmpty(),
                            name = safeName,
                            wallpaperId = activeWallpaper.id,
                            customPhotoUri = customPhotoUri?.toString().orEmpty(),
                            clockConfigJson = clockConfig.toJsonString(),
                            zoomScale = zoomScale,
                            panOffsetX = panOffset.x,
                            panOffsetY = panOffset.y,
                            dimFraction = dimFraction,
                            lookFilterName = lookFilter.name,
                            isFavorite = initialSet?.isFavorite ?: false
                        )
                        haptics.performConfirm()
                        onSaveLockScreenSet(doc)
                        showSaveSetDialog = false
                    },
                    modifier = Modifier.testTag("save_set_confirm_button")
                ) {
                    Text("Save Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveSetDialog = false }) {
                    Text("Cancel")
                }
            },
            modifier = Modifier.testTag("save_set_dialog")
        )
    }
}
