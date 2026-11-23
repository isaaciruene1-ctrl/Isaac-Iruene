package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.data.media.WallpaperTarget
import com.example.data.model.UiStyleMode
import com.example.data.model.Wallpaper
import com.example.ui.components.ClockDesignConfig
import com.example.ui.components.HyperOsClockFace
import com.example.ui.components.HyperOsClockFinish
import com.example.ui.components.HyperOsLockClock
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
import kotlin.math.roundToInt

enum class PreviewMode(val label: String) {
    LOCK_SCREEN("Lock Screen"),
    HOME_SCREEN("Home Screen")
}

@Composable
fun PreviewStudioScreen(
    wallpaper: Wallpaper,
    isApplying: Boolean,
    onBack: () -> Unit,
    onApplyWallpaper: (Wallpaper, WallpaperTarget, Boolean) -> Unit,
    onApplyEditedWallpaper: ((Wallpaper, WallpaperTarget, Boolean, (android.graphics.Bitmap) -> android.graphics.Bitmap) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(PreviewMode.HOME_SCREEN) }
    var clockConfig by remember { mutableStateOf(ClockDesignConfig(scale = 0.85f)) }
    var showWidgets by remember { mutableStateOf(true) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var dimFraction by remember { mutableFloatStateOf(0.0f) }
    var lookFilter by remember { mutableStateOf(CanvasLookFilter.ORIGINAL) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var showApplyDialog by remember { mutableStateOf(false) }

    val secondaryTint = wallpaper.dominantColors.firstOrNull()?.let { Color(it) }
        ?: MaterialTheme.colorScheme.primaryContainer
    val uiStyle = LocalUiStyleMode.current
    val reducedMotion = LocalReducedMotionFlag.current
    val haptics = rememberStudioHaptics()
    val isGlass = uiStyle == UiStyleMode.FROSTED_GLASS_OS

    val hasTransform = zoomScale > 1.01f || panOffset != Offset.Zero || dimFraction > 0.01f || lookFilter != CanvasLookFilter.ORIGINAL

    @Composable
    fun PreviewViewport(viewportModifier: Modifier, compactOverlay: Boolean) {
        Box(
            modifier = viewportModifier
                .background(Color.Black)
                .onSizeChanged { viewportSize = it }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffset = Offset(
                            x = (panOffset.x + dragAmount.x).coerceIn(-360f, 360f),
                            y = (panOffset.y + dragAmount.y).coerceIn(-480f, 480f)
                        )
                    }
                }
        ) {
            WallpaperAsyncImage(
                wallpaper = wallpaper,
                useFullRes = true,
                contentDescription = "${wallpaper.category.displayName} live device preview",
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
            if (dimFraction > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = dimFraction.coerceIn(0f, 0.5f)))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .let { if (!compactOverlay) it.statusBarsPadding() else it },
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Mock Status Bar + Controls
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = if (compactOverlay) 12.dp else 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("9:41", color = Color.White, style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Rounded.Wifi, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Icon(Icons.Rounded.BatteryFull, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TactileHudIconButton(
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            onClick = onBack,
                            modifier = Modifier.testTag("preview_back_button")
                        )

                        TactileSurface(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(50),
                            tonalElevation = 2.dp
                        ) {
                            Text(
                                text = "${wallpaper.resolutionTier.badge} • Drag to frame",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }

                        if (hasTransform) {
                            TactileHudIconButton(
                                icon = Icons.Rounded.Refresh,
                                contentDescription = "Reset preview framing & filter",
                                onClick = {
                                    zoomScale = 1.0f
                                    panOffset = Offset.Zero
                                    dimFraction = 0.0f
                                    lookFilter = CanvasLookFilter.ORIGINAL
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.size(48.dp))
                        }
                    }
                }

                // Device Mock Overlay (adapts cleanly with distinct M3 vs Liquid Glass transitions)
                AnimatedContent(
                    targetState = mode,
                    transitionSpec = {
                        AppMotion.screenEnter(uiStyle, reducedMotion) togetherWith
                            AppMotion.screenExit(uiStyle, reducedMotion)
                    },
                    label = "preview_mode_overlay"
                ) { activeMode ->
                    if (activeMode == PreviewMode.HOME_SCREEN) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(if (compactOverlay) 10.dp else 14.dp)
                        ) {
                            AnimatedVisibility(
                                visible = showWidgets,
                                enter = AppMotion.drawerEnter(uiStyle, reducedMotion),
                                exit = AppMotion.drawerExit(uiStyle, reducedMotion)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(if (compactOverlay) 10.dp else 14.dp)) {
                                    TactileSurface(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        glassTintAlpha = 0.68f,
                                        shape = RoundedCornerShape(24.dp),
                                        tonalElevation = 3.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Friday, Nov 15",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                                Text(
                                                    text = "21° Mostly Clear",
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Rounded.WbSunny,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    TactileSurface(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        glassTintAlpha = 0.66f,
                                        shape = RoundedCornerShape(50),
                                        tonalElevation = 2.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.Search,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                "Search apps & web",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }

                            val appIcons = listOf(
                                Icons.Rounded.CalendarToday to "Calendar",
                                Icons.Rounded.CameraAlt to "Camera",
                                Icons.Rounded.MusicNote to "Music",
                                Icons.Rounded.Palette to "Studio"
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                appIcons.forEach { (icon, label) ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        TactileSurface(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            glassTintAlpha = 0.78f,
                                            shape = RoundedCornerShape(18.dp),
                                            tonalElevation = 2.dp,
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            TactileSurface(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                glassTintAlpha = 0.70f,
                                shape = RoundedCornerShape(28.dp),
                                tonalElevation = 3.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    listOf(
                                        Icons.Rounded.Phone,
                                        Icons.Rounded.Mail,
                                        Icons.Rounded.Home,
                                        Icons.Rounded.Settings
                                    ).forEach { dockIcon ->
                                        TactileSurface(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            glassTintAlpha = 0.84f,
                                            shape = CircleShape,
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    dockIcon,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color.White)
                            HyperOsLockClock(
                                face = clockConfig.face,
                                font = clockConfig.font,
                                finish = clockConfig.finish,
                                primaryColor = Color(0xFFF4F6FA),
                                secondaryColor = secondaryTint,
                                weightLevel = clockConfig.weightLevel,
                                scaleFactor = clockConfig.scale,
                                widthScale = clockConfig.widthScale,
                                heightScale = clockConfig.heightScale,
                                opacity = clockConfig.opacity,
                                liquidGlassConfig = clockConfig.liquidGlass,
                                viewportSize = viewportSize,
                                zoomScale = zoomScale,
                                panOffset = panOffset,
                                dimFraction = dimFraction,
                                lookFilter = lookFilter
                            )
                            TactileSurface(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                glassTintAlpha = 0.72f,
                                shape = RoundedCornerShape(20.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Notifications,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            "Backdrops Studio",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        Text(
                                            "Lock screen preview",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ControlsContent(scrollable: Boolean) {
        Column(
            modifier = Modifier
                .let { if (scrollable) it.verticalScroll(rememberScrollState()) else it }
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StudioSegmentedBar(
                options = PreviewMode.entries,
                selected = mode,
                labelProvider = { it.label },
                onSelect = { mode = it },
                tagPrefix = "preview_mode"
            )

            StudioParamSlider(
                label = "Zoom",
                valueText = "${(zoomScale * 100).roundToInt()}%",
                value = zoomScale,
                valueRange = 1f..1.8f,
                onValueChange = { zoomScale = it },
                sliderTag = "preview_zoom_slider"
            )

            StudioParamSlider(
                label = "Dim",
                valueText = "${(dimFraction * 100).roundToInt()}%",
                value = dimFraction,
                valueRange = 0f..0.45f,
                onValueChange = { dimFraction = it },
                sliderTag = "preview_dim_slider"
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

            if (mode == PreviewMode.HOME_SCREEN) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Show Home Widgets",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(checked = showWidgets, onCheckedChange = { showWidgets = it })
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HyperOsClockFace.entries) { face ->
                        StudioOptionPill(
                            label = face.label,
                            selected = clockConfig.face == face,
                            onClick = {
                                clockConfig = clockConfig.copy(
                                    face = face,
                                    font = face.recommendedFont
                                )
                            }
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(HyperOsClockFinish.entries) { fin ->
                        StudioOptionPill(
                            label = fin.label,
                            selected = clockConfig.finish == fin,
                            onClick = { clockConfig = clockConfig.copy(finish = fin) }
                        )
                    }
                }
                StudioParamSlider(
                    label = "Clock α",
                    valueText = "${(clockConfig.opacity * 100).roundToInt()}%",
                    value = clockConfig.opacity,
                    valueRange = 0.25f..1.0f,
                    onValueChange = { clockConfig = clockConfig.copy(opacity = it) },
                    sliderTag = "preview_clock_opacity_slider"
                )
            }

            Button(
                onClick = {
                    haptics.performConfirm()
                    showApplyDialog = true
                },
                enabled = !isApplying,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("preview_apply_button")
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
                        color = Color.Black,
                        shape = RoundedCornerShape(28.dp),
                        border = glassSpecularBorder(),
                        shadowElevation = if (isGlass) 8.dp else 2.dp,
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(28.dp))
                    ) {
                        PreviewViewport(
                            viewportModifier = Modifier.fillMaxSize(),
                            compactOverlay = true
                        )
                    }

                    TactileSurface(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(28.dp),
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .widthIn(min = 300.dp, max = 420.dp)
                            .weight(0.9f)
                            .fillMaxHeight()
                    ) {
                        ControlsContent(scrollable = true)
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    PreviewViewport(
                        viewportModifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        compactOverlay = false
                    )
                    TactileSurface(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        ControlsContent(scrollable = true)
                    }
                }
            }
        }
    }

    val appContext = LocalContext.current
    if (showApplyDialog) {
        StudioWallpaperTargetDialog(
            title = "${wallpaper.category.displayName} Canvas",
            onDismiss = { showApplyDialog = false },
            onSelectTarget = { target, allowHomeParallax ->
                showApplyDialog = false
                val screenAspect = resolveDevicePortraitAspect(appContext)
                if (onApplyEditedWallpaper != null) {
                    val capturedZoom = zoomScale
                    val capturedPan = panOffset
                    val capturedW = viewportSize.width.toFloat()
                    val capturedH = viewportSize.height.toFloat()
                    val capturedDim = dimFraction
                    val capturedFilter = lookFilter
                    val parallaxWidth = allowHomeParallax && target != WallpaperTarget.LOCK
                    onApplyEditedWallpaper(wallpaper, target, allowHomeParallax) { sourceBitmap ->
                        renderEditedWallpaperBitmap(
                            source = sourceBitmap,
                            zoomScale = capturedZoom,
                            panOffset = capturedPan,
                            viewportWidthPx = capturedW,
                            viewportHeightPx = capturedH,
                            dimFraction = capturedDim,
                            lookFilter = capturedFilter,
                            targetScreenAspect = screenAspect,
                            allowParallaxWidth = parallaxWidth
                        )
                    }
                } else {
                    onApplyWallpaper(wallpaper, target, allowHomeParallax)
                }
            }
        )
    }
}
