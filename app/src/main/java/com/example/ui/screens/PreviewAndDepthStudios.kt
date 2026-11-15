package com.example.ui.screens

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FormatAlignCenter
import androidx.compose.material.icons.rounded.FormatAlignLeft
import androidx.compose.material.icons.rounded.FormatAlignRight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.data.catalog.WallpaperCatalog
import com.example.data.media.WallpaperTarget
import com.example.data.model.DiscoveryCollection
import com.example.data.model.Wallpaper
import com.example.engine.depth.ClockHorizontalAlign
import com.example.ui.components.HyperOsClockFace
import com.example.ui.components.HyperOsClockFaceThumbnail
import com.example.ui.components.HyperOsClockFinish
import com.example.ui.components.HyperOsClockFont
import com.example.ui.components.HyperOsLockClock
import com.example.ui.components.WallpaperAsyncImage
import com.example.ui.viewmodel.DepthStudioUiState
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
    var clockFace by remember { mutableStateOf(HyperOsClockFace.MAGAZINE_STACKED) }
    var clockFont by remember { mutableStateOf(clockFace.recommendedFont) }
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
                            face = clockFace,
                            font = clockFont,
                            finish = HyperOsClockFinish.DUO_GRADIENT,
                            primaryColor = Color.White,
                            secondaryColor = secondaryTint,
                            scaleFactor = 0.85f
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
                                        selected = clockFace == face,
                                        onClick = {
                                            clockFace = face
                                            clockFont = face.recommendedFont
                                        },
                                        label = { Text(face.label) }
                                    )
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
    isApplying: Boolean,
    onLoadWallpaper: (Wallpaper, Boolean) -> Unit,
    onPickCustomPhoto: (Uri, Boolean) -> Unit,
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

    var depthOverlapEnabled by remember { mutableStateOf(true) }
    var clockFace by remember { mutableStateOf(HyperOsClockFace.MAGAZINE_STACKED) }
    var clockFont by remember { mutableStateOf(clockFace.recommendedFont) }
    var clockFinish by remember { mutableStateOf(HyperOsClockFinish.DUO_GRADIENT) }
    var clockPlacement by remember { mutableFloatStateOf(0.14f) }
    var clockScale by remember { mutableFloatStateOf(1.0f) }
    var clockAlignment by remember { mutableStateOf(ClockHorizontalAlign.CENTER) }
    var useLiveTime by remember { mutableStateOf(false) }
    var showDateAndWeather by remember { mutableStateOf(true) }

    var clockColorIndex by remember { mutableIntStateOf(0) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

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
            clockPlacement = depthState.result.recommendedClockTopFraction
            clockAlignment = depthState.result.recommendedAlign
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { canvasSize = it }
    ) {
        // Layer 1: Full-resolution wallpaper bitmap (or async full-res fallback during load)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showControls = !showControls }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffset = Offset(
                            x = (panOffset.x + dragAmount.x).coerceIn(-280f, 280f),
                            y = (panOffset.y + dragAmount.y).coerceIn(-340f, 340f)
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

        // Layer 2: Customizable Clock Layer behind the real subject cutout
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            val topPadding = (maxHeight * clockPlacement).coerceIn(52.dp, maxHeight * 0.44f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = topPadding)
            ) {
                HyperOsLockClock(
                    face = clockFace,
                    font = clockFont,
                    finish = clockFinish,
                    primaryColor = selectedClockColor,
                    secondaryColor = secondaryBlendColor,
                    scaleFactor = clockScale,
                    alignment = clockAlignment,
                    useLiveTime = useLiveTime,
                    showDateAndWeather = showDateAndWeather
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
                                        "Depth ${depthState.result.depthQualityScore}% • ${depthState.result.subjectLabel}"
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
                                                    "${depthState.result.subjectCategory.displayName} • ${depthState.result.processingTimeMs}ms • Score ${depthState.result.depthQualityScore}/100"
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
                                        Text("Clock Height", style = MaterialTheme.typography.labelMedium)
                                        Slider(
                                            value = clockPlacement,
                                            onValueChange = { clockPlacement = it },
                                            valueRange = 0.06f..0.44f,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (depthState is DepthStudioUiState.Ready) {
                                            TextButton(
                                                onClick = {
                                                    clockPlacement = depthState.result.recommendedClockTopFraction
                                                    clockAlignment = depthState.result.recommendedAlign
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
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(end = 8.dp)
                                    ) {
                                        items(HyperOsClockFace.entries) { face ->
                                            HyperOsClockFaceThumbnail(
                                                face = face,
                                                selected = clockFace == face,
                                                accentColor = secondaryBlendColor,
                                                onClick = {
                                                    clockFace = face
                                                    clockFont = face.recommendedFont
                                                }
                                            )
                                        }
                                    }

                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(HyperOsClockFont.entries) { fontItem ->
                                            FilterChip(
                                                selected = clockFont == fontItem,
                                                onClick = { clockFont = fontItem },
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

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            HyperOsClockFinish.entries.forEach { fin ->
                                                FilterChip(
                                                    selected = clockFinish == fin,
                                                    onClick = { clockFinish = fin },
                                                    label = { Text(fin.label, style = MaterialTheme.typography.labelSmall) }
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        SingleChoiceSegmentedButtonRow {
                                            val aligns = listOf(
                                                ClockHorizontalAlign.START to Icons.Rounded.FormatAlignLeft,
                                                ClockHorizontalAlign.CENTER to Icons.Rounded.FormatAlignCenter,
                                                ClockHorizontalAlign.END to Icons.Rounded.FormatAlignRight
                                            )
                                            aligns.forEachIndexed { idx, (al, icon) ->
                                                SegmentedButton(
                                                    selected = clockAlignment == al,
                                                    onClick = { clockAlignment = al },
                                                    shape = SegmentedButtonDefaults.itemShape(index = idx, count = aligns.size)
                                                ) {
                                                    Icon(icon, contentDescription = al.name, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                        Slider(
                                            value = clockScale,
                                            onValueChange = { clockScale = it },
                                            valueRange = 0.78f..1.25f,
                                            modifier = Modifier.weight(1f)
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
                                            valueRange = 1.0f..1.6f,
                                            modifier = Modifier.weight(1f)
                                        )
                                        FilterChip(
                                            selected = useLiveTime,
                                            onClick = { useLiveTime = !useLiveTime },
                                            label = { Text("Live", style = MaterialTheme.typography.labelSmall) }
                                        )
                                        FilterChip(
                                            selected = showDateAndWeather,
                                            onClick = { showDateAndWeather = !showDateAndWeather },
                                            label = { Text("Date", style = MaterialTheme.typography.labelSmall) }
                                        )
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
        }
    }

    if (showApplyDialog) {
        WallpaperTargetChooserDialog(
            title = if (customPhotoUri != null) "Custom Depth Photo" else activeWallpaper.title,
            onDismiss = { showApplyDialog = false },
            onSelectTarget = { target ->
                showApplyDialog = false
                val needsCrop = zoomScale > 1.01f || panOffset != Offset.Zero
                if (fullBitmap != null) {
                    val bitmapToApply = if (needsCrop) {
                        cropBitmapForViewport(
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

private fun cropBitmapForViewport(
    source: Bitmap,
    zoomScale: Float,
    panOffset: Offset,
    viewportWidthPx: Float,
    viewportHeightPx: Float
): Bitmap {
    if (viewportWidthPx <= 0f || viewportHeightPx <= 0f) return source
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

private fun parseHexOrNull(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return try {
        Color(android.graphics.Color.parseColor(hex.trim()))
    } catch (_: Exception) {
        null
    }
}
