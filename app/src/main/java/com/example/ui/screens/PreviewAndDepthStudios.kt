package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FormatAlignCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.example.data.catalog.WallpaperCatalog
import com.example.data.media.WallpaperTarget
import com.example.data.model.DiscoveryCollection
import com.example.data.model.Wallpaper
import com.example.engine.depth.ClockHorizontalAlign
import com.example.engine.depth.DepthGenerativeFillEngine
import com.example.engine.depth.GenerativeFillMood
import com.example.ui.components.ClockBoundingOverlay
import com.example.ui.components.ClockDesignConfig
import com.example.ui.components.HyperOsClockCategory
import com.example.ui.components.HyperOsClockFace
import com.example.ui.components.HyperOsClockFaceThumbnail
import com.example.ui.components.HyperOsClockFinish
import com.example.ui.components.HyperOsClockFont
import com.example.ui.components.HyperOsLockClock
import com.example.ui.components.WallpaperAsyncImage
import com.example.ui.viewmodel.DepthStudioUiState
import com.example.ui.viewmodel.GenerativeFillUiState
import kotlin.math.roundToInt

enum class PreviewMode(val label: String) {
    LOCK_SCREEN("Lock Screen"),
    HOME_SCREEN("Home Screen")
}

private enum class DepthSectionTab(val label: String) {
    SUBJECT("Depth"),
    CLOCK("Clock"),
    WALLPAPER("Canvas")
}

@Composable
fun PreviewStudioScreen(
    wallpaper: Wallpaper,
    isApplying: Boolean,
    onBack: () -> Unit,
    onApplyWallpaper: (Wallpaper, WallpaperTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(PreviewMode.HOME_SCREEN) }
    var clockConfig by remember { mutableStateOf(ClockDesignConfig(scale = 0.85f)) }
    var showWidgets by remember { mutableStateOf(true) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var showApplyDialog by remember { mutableStateOf(false) }

    val secondaryTint = wallpaper.dominantColors.firstOrNull()?.let { Color(it) }
        ?: MaterialTheme.colorScheme.primaryContainer

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // Wallpaper Canvas with drag-to-frame
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                contentDescription = "${wallpaper.title} live device preview",
                contentScale = ContentScale.Crop,
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

        // System Status Bar + Material 3 Preview Overlay + Clean Control Card
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            val isCompactHeight = maxHeight < 640.dp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
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
                        FilledTonalIconButton(
                            onClick = onBack,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("preview_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                    }
                }

                // Device Mock Overlay (Material 3 Home vs Lock screen)
                if (mode == PreviewMode.HOME_SCREEN) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 10.dp else 16.dp)
                    ) {
                        if (showWidgets) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = RoundedCornerShape(24.dp),
                                tonalElevation = 3.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
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

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(50),
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(18.dp),
                                        tonalElevation = 2.dp,
                                        modifier = Modifier.size(50.dp)
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

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
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
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = CircleShape,
                                        modifier = Modifier.size(44.dp)
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
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color.White)
                        HyperOsLockClock(
                            face = clockConfig.face,
                            font = clockConfig.font,
                            finish = clockConfig.finish,
                            primaryColor = Color.White,
                            secondaryColor = secondaryTint,
                            weightLevel = clockConfig.weightLevel,
                            scaleFactor = clockConfig.scale,
                            widthScale = clockConfig.widthScale,
                            heightScale = clockConfig.heightScale,
                            opacity = clockConfig.opacity
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                                        "BACKDROP Studio",
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

                // Clean Material 3 Bottom Sheet Panel
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    tonalElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            PreviewMode.entries.forEachIndexed { idx, entry ->
                                SegmentedButton(
                                    selected = mode == entry,
                                    onClick = { mode = entry },
                                    shape = SegmentedButtonDefaults.itemShape(index = idx, count = PreviewMode.entries.size),
                                    modifier = Modifier.testTag("preview_mode_${entry.name}")
                                ) {
                                    Text(entry.label)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Zoom", style = MaterialTheme.typography.labelLarge)
                            Slider(
                                value = zoomScale,
                                onValueChange = { zoomScale = it },
                                valueRange = 1f..1.8f,
                                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                            )
                            Text("${(zoomScale * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                        }

                        if (mode == PreviewMode.HOME_SCREEN) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show Home Widgets", style = MaterialTheme.typography.bodyMedium)
                                Switch(checked = showWidgets, onCheckedChange = { showWidgets = it })
                            }
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(HyperOsClockFace.entries) { face ->
                                    FilterChip(
                                        selected = clockConfig.face == face,
                                        onClick = {
                                            clockConfig = clockConfig.copy(
                                                face = face,
                                                font = face.recommendedFont
                                            )
                                        },
                                        label = { Text(face.label) }
                                    )
                                }
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(HyperOsClockFinish.entries) { fin ->
                                    FilterChip(
                                        selected = clockConfig.finish == fin,
                                        onClick = { clockConfig = clockConfig.copy(finish = fin) },
                                        label = { Text(fin.label, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Opacity", style = MaterialTheme.typography.labelMedium)
                                Slider(
                                    value = clockConfig.opacity,
                                    onValueChange = { clockConfig = clockConfig.copy(opacity = it) },
                                    valueRange = 0.25f..1.0f,
                                    modifier = Modifier.weight(1f)
                                )
                                Text("${(clockConfig.opacity * 100).roundToInt()}%", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Button(
                            onClick = { showApplyDialog = true },
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
            }
        }
    }

    if (showApplyDialog) {
        WallpaperTargetChooserDialog(
            title = wallpaper.title,
            onDismiss = { showApplyDialog = false },
            onSelectTarget = { target ->
                showApplyDialog = false
                onApplyWallpaper(wallpaper, target)
            }
        )
    }
}

@Composable
fun DepthStudioScreen(
    initialWallpaper: Wallpaper,
    depthState: DepthStudioUiState,
    fillState: GenerativeFillUiState,
    framingResetVersion: Int,
    isApplying: Boolean,
    allowGenerativeFill: Boolean = true,
    onLoadWallpaper: (Wallpaper, Boolean) -> Unit,
    onPickCustomPhoto: (Uri, Boolean) -> Unit,
    onApplyGenerativeFill: (Float, Float, Float, Float, Float, GenerativeFillMood, String) -> Unit,
    onUndoGenerativeFill: () -> Unit,
    onDismissFillError: () -> Unit,
    onBack: () -> Unit,
    onApplyWallpaper: (Wallpaper, WallpaperTarget) -> Unit,
    onApplyCustomBitmapWallpaper: (Bitmap, WallpaperTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    val depthWallpapers = remember(initialWallpaper.id) {
        val list = WallpaperCatalog.getForCollection(DiscoveryCollection.DEPTH_STUDIO)
        if (list.any { it.id == initialWallpaper.id }) list else listOf(initialWallpaper) + list
    }

    var activeWallpaper by remember(initialWallpaper.id) { mutableStateOf(initialWallpaper) }
    var customPhotoUri by remember { mutableStateOf<Uri?>(null) }

    var showControls by remember { mutableStateOf(true) }
    var activeTab by remember { mutableStateOf(DepthSectionTab.SUBJECT) }
    var selectedClockCategory by remember { mutableStateOf<HyperOsClockCategory?>(null) }

    var depthOverlapEnabled by remember { mutableStateOf(true) }
    var clockConfig by remember { mutableStateOf(ClockDesignConfig()) }
    var isClockEditing by remember { mutableStateOf(false) }
    var clockMeasuredSize by remember { mutableStateOf(IntSize.Zero) }

    var clockColorIndex by remember { mutableIntStateOf(0) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var selectedFillMood by remember { mutableStateOf(GenerativeFillMood.SEAMLESS_SCENE) }
    var customFillCue by remember { mutableStateOf("") }

    var showApplyDialog by remember { mutableStateOf(false) }
    var lastAutoAppliedKey by remember { mutableStateOf<String?>(null) }

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

    LaunchedEffect(framingResetVersion) {
        if (framingResetVersion > 0) {
            panOffset = Offset.Zero
            zoomScale = 1.0f
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

    LaunchedEffect(depthState) {
        if (depthState is DepthStudioUiState.Ready && depthState.key != lastAutoAppliedKey) {
            lastAutoAppliedKey = depthState.key
            clockConfig = clockConfig.copy(
                placementY = depthState.result.recommendedClockTopFraction,
                alignment = depthState.result.recommendedAlign,
                offsetXFraction = 0f
            )
            clockColorIndex = 0
        }
    }

    val engineColor = remember(depthState) {
        val hex = (depthState as? DepthStudioUiState.Ready)?.result?.recommendedClockColorHex
        parseHexOrNull(hex) ?: Color.White
    }
    val paletteColors = remember(activeWallpaper.id, engineColor) {
        val wallpaperPastels = activeWallpaper.dominantColors.map { Color(it) }
        (listOf(engineColor, Color.White, Color(0xFFE7DEFF), Color(0xFFCDEEE2), Color(0xFFFFD8C8)) + wallpaperPastels)
            .distinct()
            .take(6)
    }
    val selectedClockColor = paletteColors[clockColorIndex % paletteColors.size]
    val secondaryBlendColor = activeWallpaper.dominantColors.firstOrNull()?.let { Color(it) }
        ?: MaterialTheme.colorScheme.primaryContainer

    val fullBitmap = when (depthState) {
        is DepthStudioUiState.ScanningSubject -> depthState.fullBitmap
        is DepthStudioUiState.Ready -> depthState.fullBitmap
        is DepthStudioUiState.EngineError -> depthState.fullBitmap
        else -> null
    }

    val frameSpec = remember(fullBitmap, canvasSize, zoomScale, panOffset) {
        val bmp = fullBitmap ?: return@remember null
        if (canvasSize.width <= 0 || canvasSize.height <= 0) return@remember null
        DepthGenerativeFillEngine.computeFrameSpec(
            sourceWidth = bmp.width,
            sourceHeight = bmp.height,
            viewportWidth = canvasSize.width.toFloat(),
            viewportHeight = canvasSize.height.toFloat(),
            zoomScale = zoomScale,
            panOffsetX = panOffset.x,
            panOffsetY = panOffset.y
        )
    }
    val hasUncoveredBorders = allowGenerativeFill && frameSpec?.hasUncoveredBorders == true
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF101218))
            .onSizeChanged { canvasSize = it }
    ) {
        // Subtle canvas grid shown beneath uncovered margins when zoomed out or panned past edge
        if (hasUncoveredBorders) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cell = 24.dp.toPx()
                val darkTile = Color(0xFF171A22)
                val lightTile = Color(0xFF1E222D)
                val cols = (size.width / cell).toInt() + 1
                val rows = (size.height / cell).toInt() + 1
                for (r in 0..rows) {
                    for (c in 0..cols) {
                        val color = if ((r + c) % 2 == 0) darkTile else lightTile
                        drawRect(
                            color = color,
                            topLeft = Offset(c * cell, r * cell),
                            size = Size(cell, cell)
                        )
                    }
                }
            }
        }

        // Layer 1: Full-resolution wallpaper bitmap (supports edge panning; tapping outside clock hides handles)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (isClockEditing) {
                        isClockEditing = false
                    } else {
                        showControls = !showControls
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffset = Offset(
                            x = (panOffset.x + dragAmount.x).coerceIn(-520f, 520f),
                            y = (panOffset.y + dragAmount.y).coerceIn(-680f, 680f)
                        )
                    }
                }
        ) {
            if (fullBitmap != null) {
                Image(
                    bitmap = fullBitmap.asImageBitmap(),
                    contentDescription = "${activeWallpaper.title} background",
                    contentScale = ContentScale.Crop,
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
                    contentDescription = "${activeWallpaper.title} background",
                    contentScale = ContentScale.Crop,
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
        }

        // Compute aligned full-bleed clock vertical & horizontal offset once for Layer 2 & Layer 3.5 overlay
        val viewportHeightDp = with(density) { canvasSize.height.toDp() }
        val minClockTop = (statusTop + 16.dp).coerceAtLeast(52.dp)
        val maxClockTop = if (viewportHeightDp > minClockTop) {
            (viewportHeightDp * 0.44f).coerceAtLeast(minClockTop)
        } else {
            minClockTop
        }
        val clockTopPadding = if (viewportHeightDp > 0.dp) {
            (viewportHeightDp * clockConfig.placementY).coerceIn(minClockTop, maxClockTop)
        } else {
            minClockTop
        }
        val centerOffsetXPx = canvasSize.width * clockConfig.offsetXFraction

        // Layer 2: Customizable HyperOS Clock Layer behind the real subject cutout
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = clockTopPadding)
                    .graphicsLayer {
                        translationX = centerOffsetXPx
                    }
                    .onSizeChanged { clockMeasuredSize = it }
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
                    showDateAndWeather = clockConfig.showDateAndWeather
                )
            }
        }

        // Layer 3: Pixel-accurate Depth Engine Foreground Subject Cutout (exact same transform as Layer 1)
        val cutoutBmp = (depthState as? DepthStudioUiState.Ready)?.result?.cutoutBitmap
        if (depthOverlapEnabled && cutoutBmp != null) {
            Image(
                bitmap = cutoutBmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
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

        // Layer 3.5: Tap-to-Reveal Clock Bounding Frame (handles stay above subject cutout so edges are always draggable)
        if (!isClockEditing && canvasSize.width > 0) {
            val hitW = if (clockMeasuredSize.width > 0) {
                with(density) { (clockMeasuredSize.width * clockConfig.widthScale).toDp() }.coerceIn(170.dp, 350.dp)
            } else 240.dp
            val hitH = if (clockMeasuredSize.height > 0) {
                with(density) { (clockMeasuredSize.height * clockConfig.heightScale).toDp() }.coerceIn(100.dp, 300.dp)
            } else 160.dp
            val hitLeftPx = ((canvasSize.width - with(density) { hitW.toPx() }) * 0.5f + centerOffsetXPx)
                .coerceIn(0f, (canvasSize.width - with(density) { hitW.toPx() }).coerceAtLeast(0f))
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = hitLeftPx.roundToInt(),
                            y = with(density) { clockTopPadding.toPx() }.roundToInt()
                        )
                    }
                    .size(width = hitW, height = hitH)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isClockEditing = true }
                    .testTag("clock_tap_hit_box")
            )
        } else if (isClockEditing && canvasSize.width > 0) {
            ClockBoundingOverlay(
                topOffsetDp = clockTopPadding,
                centerOffsetXPx = centerOffsetXPx,
                clockWidthPx = clockMeasuredSize.width,
                clockHeightPx = clockMeasuredSize.height,
                viewportSize = canvasSize,
                config = clockConfig,
                onUpdateConfig = { clockConfig = it },
                onResetFrame = {
                    val readyResult = (depthState as? DepthStudioUiState.Ready)?.result
                    clockConfig = clockConfig.copy(
                        scale = 1.0f,
                        widthScale = 1.0f,
                        heightScale = 1.0f,
                        offsetXFraction = 0f,
                        placementY = readyResult?.recommendedClockTopFraction ?: 0.14f,
                        alignment = readyResult?.recommendedAlign ?: ClockHorizontalAlign.CENTER
                    )
                },
                onDoneEditing = { isClockEditing = false }
            )
        }

        // Floating Canvas Progress Pill while Gemini AI extends uncovered borders
        AnimatedVisibility(
            visible = fillState is GenerativeFillUiState.Filling,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.96f),
                shape = RoundedCornerShape(50),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Extending canvas edges with Gemini AI...",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Layer 4: Clean Material 3 Chrome (Top Bar + Compact Bottom Sheet)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar — clean M3 tonal icon buttons & status pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("depth_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }

                val isRetryableError = depthState is DepthStudioUiState.EngineError || depthState is DepthStudioUiState.BitmapError
                Surface(
                    onClick = {
                        if (isRetryableError) {
                            val uri = customPhotoUri
                            if (uri != null) onPickCustomPhoto(uri, true)
                            else onLoadWallpaper(activeWallpaper, true)
                        }
                    },
                    enabled = isRetryableError,
                    color = if (isRetryableError) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(50),
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = 8.dp)
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
                                            "Depth ${depthState.result.depthQualityScore}% • ${overlapPct}% clock tuck"
                                        } else {
                                            "Depth ${depthState.result.depthQualityScore}% • ${depthState.result.subjectLabel}"
                                        }
                                    } else {
                                        "Flat canvas (no overlap)"
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
                    FilledTonalIconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = "Pick photo from gallery")
                    }
                    FilledTonalIconButton(
                        onClick = {
                            val uri = customPhotoUri
                            if (uri != null) onPickCustomPhoto(uri, true)
                            else onLoadWallpaper(activeWallpaper, true)
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Re-scan depth subject")
                    }
                }
            }

            // Bottom Control Sheet
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sheet Header + Collapse Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showControls = !showControls }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Depth Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (customPhotoUri != null) "Custom Gallery Photo" else "${activeWallpaper.title} • ${activeWallpaper.resolutionTier.badge}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = if (showControls) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                            contentDescription = if (showControls) "Collapse controls" else "Expand controls",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(
                        visible = showControls,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                DepthSectionTab.entries.forEachIndexed { idx, tab ->
                                    SegmentedButton(
                                        selected = activeTab == tab,
                                        onClick = { activeTab = tab },
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = idx,
                                            count = DepthSectionTab.entries.size
                                        )
                                    ) {
                                        Text(tab.label, maxLines = 1)
                                    }
                                }
                            }

                            when (activeTab) {
                                DepthSectionTab.SUBJECT -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Depth Engine Layer", style = MaterialTheme.typography.titleSmall)
                                            val subtitle = when (depthState) {
                                                is DepthStudioUiState.Ready -> if (depthState.result.isDepthEligible && depthState.result.cutoutBitmap != null) {
                                                    val overlapPct = (depthState.result.recommendedClockOverlapFraction * 100f).roundToInt()
                                                    val tuckLabel = if (overlapPct >= 4) " • ${overlapPct}% clock tuck" else ""
                                                    "${depthState.result.subjectCategory.displayName}$tuckLabel • ${depthState.result.processingTimeMs}ms • Score ${depthState.result.depthQualityScore}/100"
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
                                            checked = depthOverlapEnabled,
                                            onCheckedChange = { depthOverlapEnabled = it },
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
                                        Text("Clock Height Y", style = MaterialTheme.typography.labelMedium)
                                        Slider(
                                            value = clockConfig.placementY,
                                            onValueChange = { clockConfig = clockConfig.copy(placementY = it) },
                                            valueRange = 0.06f..0.44f,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (depthState is DepthStudioUiState.Ready) {
                                            TextButton(
                                                onClick = {
                                                    clockConfig = clockConfig.copy(
                                                        placementY = depthState.result.recommendedClockTopFraction,
                                                        alignment = depthState.result.recommendedAlign,
                                                        offsetXFraction = 0f
                                                    )
                                                    clockColorIndex = 0
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Text("Auto Snap")
                                            }
                                        }
                                    }
                                }

                                DepthSectionTab.CLOCK -> {
                                    val filteredFaces = remember(selectedClockCategory) {
                                        if (selectedClockCategory == null) HyperOsClockFace.entries
                                        else HyperOsClockFace.entries.filter { it.category == selectedClockCategory }
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 310.dp)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Clock Family Filter + Tap-Edge Edit Toggle
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            item {
                                                FilterChip(
                                                    selected = isClockEditing,
                                                    onClick = { isClockEditing = !isClockEditing },
                                                    label = {
                                                        Text(
                                                            if (isClockEditing) "Hide Handles" else "Drag Edges",
                                                            style = MaterialTheme.typography.labelSmall
                                                        )
                                                    },
                                                    modifier = Modifier.testTag("clock_toggle_handles_chip")
                                                )
                                            }
                                            item {
                                                FilterChip(
                                                    selected = selectedClockCategory == null,
                                                    onClick = { selectedClockCategory = null },
                                                    label = { Text("All (11)", style = MaterialTheme.typography.labelSmall) }
                                                )
                                            }
                                            items(HyperOsClockCategory.entries) { cat ->
                                                FilterChip(
                                                    selected = selectedClockCategory == cat,
                                                    onClick = { selectedClockCategory = cat },
                                                    label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) }
                                                )
                                            }
                                        }

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            contentPadding = PaddingValues(end = 8.dp)
                                        ) {
                                            items(filteredFaces, key = { it.name }) { face ->
                                                HyperOsClockFaceThumbnail(
                                                    face = face,
                                                    selected = clockConfig.face == face,
                                                    accentColor = secondaryBlendColor,
                                                    onClick = {
                                                        clockConfig = clockConfig.copy(
                                                            face = face,
                                                            font = face.recommendedFont
                                                        )
                                                    }
                                                )
                                            }
                                        }

                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(HyperOsClockFont.entries, key = { it.name }) { fontItem ->
                                                FilterChip(
                                                    selected = clockConfig.font == fontItem,
                                                    onClick = { clockConfig = clockConfig.copy(font = fontItem) },
                                                    label = {
                                                        Text(
                                                            text = fontItem.label,
                                                            fontFamily = fontItem.family,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                )
                                            }
                                        }

                                        // 5 Finish Modes: Solid | Translucent | Blend | Outline | Blur Glass
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(HyperOsClockFinish.entries, key = { it.name }) { fin ->
                                                FilterChip(
                                                    selected = clockConfig.finish == fin,
                                                    onClick = {
                                                        val defaultAlpha = when (fin) {
                                                            HyperOsClockFinish.TRANSLUCENT -> 0.62f
                                                            HyperOsClockFinish.BLUR_GLASS -> 0.88f
                                                            else -> clockConfig.opacity.coerceAtLeast(0.88f)
                                                        }
                                                        clockConfig = clockConfig.copy(finish = fin, opacity = defaultAlpha)
                                                    },
                                                    label = { Text(fin.label, style = MaterialTheme.typography.labelSmall) },
                                                    modifier = Modifier.testTag("clock_finish_${fin.name}")
                                                )
                                            }
                                        }

                                        // Swatches + Alignment
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                paletteColors.forEachIndexed { idx, col ->
                                                    val chosen = clockColorIndex % paletteColors.size == idx
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
                                                            .clickable { clockColorIndex = idx }
                                                    )
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
                                                        onClick = { clockConfig = clockConfig.copy(alignment = al) },
                                                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = aligns.size)
                                                    ) {
                                                        Icon(icon, contentDescription = al.name, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }

                                        // Weight, Opacity, Height, Width & Size Sliders
                                        ClockParamSlider(
                                            label = "Weight",
                                            valueText = "${clockConfig.weightLevel}",
                                            value = clockConfig.weightLevel.toFloat(),
                                            valueRange = 100f..900f,
                                            onValueChange = {
                                                clockConfig = clockConfig.copy(weightLevel = it.roundToInt())
                                            },
                                            sliderTag = "clock_weight_slider"
                                        )
                                        ClockParamSlider(
                                            label = "Opacity",
                                            valueText = "${(clockConfig.opacity * 100).roundToInt()}%",
                                            value = clockConfig.opacity,
                                            valueRange = 0.20f..1.0f,
                                            onValueChange = { clockConfig = clockConfig.copy(opacity = it) },
                                            sliderTag = "clock_opacity_slider"
                                        )
                                        ClockParamSlider(
                                            label = "Height",
                                            valueText = "${(clockConfig.heightScale * 100).roundToInt()}%",
                                            value = clockConfig.heightScale,
                                            valueRange = 0.70f..1.55f,
                                            onValueChange = { clockConfig = clockConfig.copy(heightScale = it) },
                                            sliderTag = "clock_height_slider"
                                        )
                                        ClockParamSlider(
                                            label = "Width",
                                            valueText = "${(clockConfig.widthScale * 100).roundToInt()}%",
                                            value = clockConfig.widthScale,
                                            valueRange = 0.72f..1.35f,
                                            onValueChange = { clockConfig = clockConfig.copy(widthScale = it) },
                                            sliderTag = "clock_width_slider"
                                        )
                                        ClockParamSlider(
                                            label = "Scale",
                                            valueText = "${(clockConfig.scale * 100).roundToInt()}%",
                                            value = clockConfig.scale,
                                            valueRange = 0.70f..1.45f,
                                            onValueChange = { clockConfig = clockConfig.copy(scale = it) },
                                            sliderTag = "clock_scale_slider"
                                        )
                                    }
                                }

                                DepthSectionTab.WALLPAPER -> {
                                    LazyRow(
                                        contentPadding = PaddingValues(end = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(depthWallpapers, key = { it.id }) { wp ->
                                            val selected = customPhotoUri == null && wp.id == activeWallpaper.id
                                            Surface(
                                                onClick = {
                                                    customPhotoUri = null
                                                    activeWallpaper = wp
                                                    panOffset = Offset.Zero
                                                    zoomScale = 1.0f
                                                },
                                                shape = RoundedCornerShape(16.dp),
                                                border = BorderStroke(
                                                    width = if (selected) 2.dp else 1.dp,
                                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                                ),
                                                color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                else MaterialTheme.colorScheme.surfaceContainerHigh
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    WallpaperAsyncImage(
                                                        wallpaper = wp,
                                                        useFullRes = false,
                                                        contentDescription = wp.title,
                                                        modifier = Modifier
                                                            .size(38.dp)
                                                            .clip(RoundedCornerShape(10.dp))
                                                    )
                                                    Text(
                                                        text = wp.title,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        modifier = Modifier.padding(end = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Zoom", style = MaterialTheme.typography.labelMedium)
                                        Slider(
                                            value = zoomScale,
                                            onValueChange = { zoomScale = it },
                                            valueRange = if (allowGenerativeFill) 0.55f..1.80f else 1.0f..1.80f,
                                            modifier = Modifier.weight(1f)
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

                                    if (allowGenerativeFill) {
                                        // Gemini Generative Fill (Wallpaper Extend) controls
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(GenerativeFillMood.entries) { mood ->
                                                FilterChip(
                                                    selected = selectedFillMood == mood,
                                                    onClick = { selectedFillMood = mood },
                                                    label = { Text(mood.label, style = MaterialTheme.typography.labelSmall) }
                                                )
                                            }
                                        }

                                        if (selectedFillMood == GenerativeFillMood.CUSTOM) {
                                            OutlinedTextField(
                                                value = customFillCue,
                                                onValueChange = { customFillCue = it },
                                                placeholder = { Text("e.g. soft twilight cirrus sky & pastel mist") },
                                                singleLine = true,
                                                textStyle = MaterialTheme.typography.bodySmall,
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        if (fillState is GenerativeFillUiState.Error) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.errorContainer,
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = fillState.message,
                                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    TextButton(onClick = onDismissFillError) {
                                                        Text("OK")
                                                    }
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            FilledTonalButton(
                                                onClick = {
                                                    onApplyGenerativeFill(
                                                        canvasSize.width.toFloat(),
                                                        canvasSize.height.toFloat(),
                                                        zoomScale,
                                                        panOffset.x,
                                                        panOffset.y,
                                                        selectedFillMood,
                                                        customFillCue
                                                    )
                                                },
                                                enabled = hasUncoveredBorders &&
                                                    fullBitmap != null &&
                                                    fillState !is GenerativeFillUiState.Filling,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("depth_generative_fill_button")
                                            ) {
                                                if (fillState is GenerativeFillUiState.Filling) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        strokeWidth = 2.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Extending Edges...", maxLines = 1)
                                                } else {
                                                    Icon(
                                                        Icons.Rounded.AutoFixHigh,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        if (hasUncoveredBorders) "Generative Fill" else "Zoom Out / Drag to Fill",
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            if (fillState is GenerativeFillUiState.Applied) {
                                                OutlinedButton(
                                                    onClick = onUndoGenerativeFill,
                                                    modifier = Modifier.testTag("depth_undo_fill_button")
                                                ) {
                                                    Icon(
                                                        Icons.AutoMirrored.Rounded.Undo,
                                                        contentDescription = "Undo Fill",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Undo")
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = clockConfig.useLiveTime,
                                            onClick = { clockConfig = clockConfig.copy(useLiveTime = !clockConfig.useLiveTime) },
                                            label = { Text("Live Time", style = MaterialTheme.typography.labelSmall) }
                                        )
                                        FilterChip(
                                            selected = clockConfig.showDateAndWeather,
                                            onClick = { clockConfig = clockConfig.copy(showDateAndWeather = !clockConfig.showDateAndWeather) },
                                            label = { Text("Date & Weather", style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Contextual quick Generative Fill banner when user pans/zooms past borders from any tab
                    AnimatedVisibility(
                        visible = allowGenerativeFill && hasUncoveredBorders && (activeTab != DepthSectionTab.WALLPAPER || !showControls) && fillState !is GenerativeFillUiState.Filling
                    ) {
                        FilledTonalButton(
                            onClick = {
                                activeTab = DepthSectionTab.WALLPAPER
                                showControls = true
                                onApplyGenerativeFill(
                                    canvasSize.width.toFloat(),
                                    canvasSize.height.toFloat(),
                                    zoomScale,
                                    panOffset.x,
                                    panOffset.y,
                                    selectedFillMood,
                                    customFillCue
                                )
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fill Uncovered Borders with Gemini AI")
                        }
                    }

                    if (fillState is GenerativeFillUiState.Filling && activeTab != DepthSectionTab.WALLPAPER) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Extending canvas borders & refreshing depth contour...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showApplyDialog = true },
                        enabled = !isApplying && fillState !is GenerativeFillUiState.Filling,
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
        }
    }

    if (showApplyDialog) {
        WallpaperTargetChooserDialog(
            title = if (customPhotoUri != null) "Custom Depth Photo" else activeWallpaper.title,
            onDismiss = { showApplyDialog = false },
            onSelectTarget = { target ->
                showApplyDialog = false
                val needsTransform = kotlin.math.abs(zoomScale - 1.0f) > 0.01f || panOffset != Offset.Zero
                if (fullBitmap != null) {
                    val bitmapToApply = if (needsTransform) {
                        renderTransformedViewportBitmap(
                            source = fullBitmap,
                            zoomScale = zoomScale,
                            panOffset = panOffset,
                            viewportWidthPx = canvasSize.width.toFloat(),
                            viewportHeightPx = canvasSize.height.toFloat()
                        )
                    } else {
                        fullBitmap
                    }
                    onApplyCustomBitmapWallpaper(bitmapToApply, target)
                } else {
                    onApplyWallpaper(activeWallpaper, target)
                }
            }
        )
    }
}

@Composable
private fun WallpaperTargetChooserDialog(
    title: String,
    onDismiss: () -> Unit,
    onSelectTarget: (WallpaperTarget) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Wallpaper") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Choose where to apply \"$title\":",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = { onSelectTarget(WallpaperTarget.HOME) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Home Screen")
                }
                FilledTonalButton(
                    onClick = { onSelectTarget(WallpaperTarget.LOCK) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lock Screen")
                }
                OutlinedButton(
                    onClick = { onSelectTarget(WallpaperTarget.BOTH) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Home & Lock Screens")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun renderTransformedViewportBitmap(
    source: Bitmap,
    zoomScale: Float,
    panOffset: Offset,
    viewportWidthPx: Float,
    viewportHeightPx: Float
): Bitmap {
    if (viewportWidthPx <= 0f || viewportHeightPx <= 0f) return source
    val spec = DepthGenerativeFillEngine.computeFrameSpec(
        sourceWidth = source.width,
        sourceHeight = source.height,
        viewportWidth = viewportWidthPx,
        viewportHeight = viewportHeightPx,
        zoomScale = zoomScale,
        panOffsetX = panOffset.x,
        panOffsetY = panOffset.y
    )
    if (!spec.hasUncoveredBorders) {
        val srcW = source.width.toFloat()
        val srcH = source.height.toFloat()
        val baseScale = maxOf(viewportWidthPx / srcW, viewportHeightPx / srcH)
        val effectiveScale = baseScale * zoomScale.coerceAtLeast(1f)

        val centerX = viewportWidthPx / 2f + panOffset.x
        val centerY = viewportHeightPx / 2f + panOffset.y

        val left = ((0f - centerX) / effectiveScale + srcW / 2f).coerceIn(0f, srcW - 1f)
        val top = ((0f - centerY) / effectiveScale + srcH / 2f).coerceIn(0f, srcH - 1f)
        val right = ((viewportWidthPx - centerX) / effectiveScale + srcW / 2f).coerceIn(left + 1f, srcW)
        val bottom = ((viewportHeightPx - centerY) / effectiveScale + srcH / 2f).coerceIn(top + 1f, srcH)

        val cropX = left.roundToInt().coerceIn(0, source.width - 1)
        val cropY = top.roundToInt().coerceIn(0, source.height - 1)
        val cropW = (right - left).roundToInt().coerceIn(1, source.width - cropX)
        val cropH = (bottom - top).roundToInt().coerceIn(1, source.height - cropY)
        return Bitmap.createBitmap(source, cropX, cropY, cropW, cropH)
    }

    val output = createBitmap(spec.canvasWidth, spec.canvasHeight)
    val canvas = AndroidCanvas(output)
    canvas.drawColor(android.graphics.Color.rgb(16, 18, 24))
    val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.FILTER_BITMAP_FLAG)
    canvas.drawBitmap(source, null, spec.placedRect, paint)
    return output
}

private fun parseHexOrNull(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return try {
        Color(hex.trim().toColorInt())
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun ClockParamSlider(
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
            modifier = Modifier.width(54.dp)
        )
        Slider(
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
            modifier = Modifier.width(38.dp)
        )
    }
}
