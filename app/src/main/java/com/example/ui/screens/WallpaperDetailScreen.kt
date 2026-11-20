package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOutMap
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.catalog.WallpaperCatalog
import com.example.data.media.DownloadProgress
import com.example.data.media.WallpaperTarget
import com.example.data.model.UserCollectionDoc
import com.example.data.model.Wallpaper
import com.example.data.repository.AiCapabilities
import com.example.ui.components.FavoriteHeartButton
import com.example.ui.components.WallpaperAsyncImage
import com.example.ui.viewmodel.SyncState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WallpaperDetailScreen(
    wallpaper: Wallpaper,
    favoriteIds: Set<String>,
    collectionsState: SyncState<List<UserCollectionDoc>>,
    downloadProgress: DownloadProgress?,
    isApplying: Boolean,
    onBack: () -> Unit,
    onRecordViewed: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDownload: (Wallpaper) -> Unit,
    onClearDownloadStatus: () -> Unit,
    onApplyWallpaper: (Wallpaper, WallpaperTarget) -> Unit,
    onShareWallpaper: (Wallpaper) -> Unit,
    onAddToCollection: (UserCollectionDoc, Wallpaper) -> Unit,
    onCreateCollectionWithWallpaper: (String, String, Wallpaper) -> Unit,
    onOpenPreviewStudio: (Wallpaper) -> Unit,
    onOpenDepthStudio: (Wallpaper) -> Unit,
    onOpenAiRemix: (Wallpaper) -> Unit,
    onOpenAiAnalyze: (Wallpaper) -> Unit,
    aiCapabilities: AiCapabilities = AiCapabilities(),
    modifier: Modifier = Modifier
) {
    val pagerItems = remember(wallpaper.id) {
        listOf(wallpaper) + WallpaperCatalog.getRelated(wallpaper, limit = 8)
    }
    val pagerState = rememberPagerState(pageCount = { pagerItems.size })
    val currentWallpaper = pagerItems.getOrElse(pagerState.currentPage) { wallpaper }

    var showHud by remember { mutableStateOf(true) }
    var zoomMode by remember { mutableStateOf(false) }
    var scale by remember(currentWallpaper.id) { mutableFloatStateOf(1f) }
    var offset by remember(currentWallpaper.id) { mutableStateOf(Offset.Zero) }

    var showMetadataSheet by remember { mutableStateOf(false) }
    var showApplyDialog by remember { mutableStateOf(false) }
    var showCollectionSheet by remember { mutableStateOf(false) }

    LaunchedEffect(currentWallpaper.id) {
        onRecordViewed(currentWallpaper.id)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !zoomMode && scale <= 1.02f,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = pagerItems[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(zoomMode, item.id) {
                        if (zoomMode) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 3.5f)
                                scale = newScale
                                if (newScale > 1.05f) {
                                    offset = Offset(
                                        x = (offset.x + pan.x).coerceIn(-600f, 600f),
                                        y = (offset.y + pan.y).coerceIn(-900f, 900f)
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                        }
                    }
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) {
                        if (!zoomMode) showHud = !showHud
                    }
            ) {
                WallpaperAsyncImage(
                    wallpaper = item,
                    useFullRes = true,
                    contentDescription = "${item.title} full-resolution preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (page == pagerState.currentPage) {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                        }
                )
            }
        }

        // Top HUD
        AnimatedVisibility(
            visible = showHud,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.45f),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("detail_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Navigate back",
                                tint = Color.White
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = if (zoomMode) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.45f),
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    zoomMode = !zoomMode
                                    if (!zoomMode) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    }
                                },
                                modifier = Modifier.testTag("detail_zoom_button")
                            ) {
                                Icon(
                                    imageVector = if (zoomMode) Icons.Rounded.ZoomOutMap else Icons.Rounded.ZoomIn,
                                    contentDescription = if (zoomMode) "Exit zoom mode" else "Pinch and pan zoom",
                                    tint = if (zoomMode) MaterialTheme.colorScheme.onPrimary else Color.White
                                )
                            }
                        }
                        Surface(
                            color = Color.Black.copy(alpha = 0.45f),
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            IconButton(
                                onClick = { showMetadataSheet = true },
                                modifier = Modifier.testTag("detail_info_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = "Wallpaper info and attribution",
                                    tint = Color.White
                                )
                            }
                        }
                        FavoriteHeartButton(
                            isFavorite = currentWallpaper.id in favoriteIds,
                            onToggle = { onToggleFavorite(currentWallpaper.id) },
                            containerColor = Color.Black.copy(alpha = 0.45f),
                            modifier = Modifier.testTag("detail_favorite_button")
                        )
                    }
                }
            }
        }

        // Bottom HUD
        AnimatedVisibility(
            visible = showHud,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.72f),
                                Color.Black.copy(alpha = 0.90f)
                            )
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = currentWallpaper.category.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = currentWallpaper.resolutionText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentWallpaper.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentWallpaper.creatorName} • Swipe sideways for related",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                    }
                }

                // Download progress pill if active
                if (downloadProgress != null && downloadProgress !is DownloadProgress.Failed) {
                    Surface(
                        color = Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            when (downloadProgress) {
                                DownloadProgress.Starting -> {
                                    Text(
                                        "Preparing full-resolution download...",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                                is DownloadProgress.InProgress -> {
                                    val pct = (downloadProgress.fraction * 100).toInt()
                                    Text(
                                        "Downloading $pct%",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    LinearProgressIndicator(
                                        progress = { downloadProgress.fraction },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                is DownloadProgress.Completed -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF9DF0C3),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Saved to Pictures/BACKDROP",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        TextButton(onClick = onClearDownloadStatus) {
                                            Text("Dismiss", color = Color.White)
                                        }
                                    }
                                }
                                else -> Unit
                            }
                        }
                    }
                }

                // Studio & AI Action Pills
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onOpenPreviewStudio(currentWallpaper) },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("open_preview_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhoneAndroid,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Preview Studio")
                    }

                    FilledTonalButton(
                        onClick = { onOpenDepthStudio(currentWallpaper) },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("open_depth_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Layers,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Depth Studio")
                    }

                    if (aiCapabilities.imageGenAvailable) {
                        AssistChip(
                            onClick = { onOpenAiRemix(currentWallpaper) },
                            label = { Text("AI Remix", color = Color.White) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFE7DEFF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    if (aiCapabilities.textAndVisionAvailable) {
                        AssistChip(
                            onClick = { onOpenAiAnalyze(currentWallpaper) },
                            label = { Text("Analyze", color = Color.White) }
                        )
                    }
                }

                // Primary Bottom Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        IconButton(
                            onClick = { onDownload(currentWallpaper) },
                            modifier = Modifier.testTag("detail_download_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = "Download wallpaper",
                                tint = Color.White
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        IconButton(
                            onClick = { showCollectionSheet = true },
                            modifier = Modifier.testTag("detail_collection_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BookmarkAdd,
                                contentDescription = "Save to collection",
                                tint = Color.White
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        IconButton(
                            onClick = { onShareWallpaper(currentWallpaper) },
                            modifier = Modifier.testTag("detail_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Share wallpaper",
                                tint = Color.White
                            )
                        }
                    }

                    Button(
                        onClick = { showApplyDialog = true },
                        enabled = !isApplying,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_apply_button")
                    ) {
                        if (isApplying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Wallpaper,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Set Wallpaper")
                        }
                    }
                }
            }
        }
    }

    if (showApplyDialog) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = { Text("Set Wallpaper") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Choose where to apply \"${currentWallpaper.title}\":",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            showApplyDialog = false
                            onApplyWallpaper(currentWallpaper, WallpaperTarget.HOME)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Home Screen")
                    }
                    FilledTonalButton(
                        onClick = {
                            showApplyDialog = false
                            onApplyWallpaper(currentWallpaper, WallpaperTarget.LOCK)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Lock Screen")
                    }
                    OutlinedButton(
                        onClick = {
                            showApplyDialog = false
                            onApplyWallpaper(currentWallpaper, WallpaperTarget.BOTH)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Home & Lock Screens")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showApplyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMetadataSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMetadataSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = currentWallpaper.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentWallpaper.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetadataPill(
                        label = "Resolution",
                        value = "${currentWallpaper.resolutionText} (${currentWallpaper.resolutionTier.badge})",
                        modifier = Modifier.weight(1f)
                    )
                    MetadataPill(
                        label = "Aspect Ratio",
                        value = currentWallpaper.aspectRatio,
                        modifier = Modifier.weight(1f)
                    )
                }

                MetadataPill(
                    label = "Creator & Attribution",
                    value = "${currentWallpaper.creatorName} • ${currentWallpaper.creatorSource}",
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Color Palette",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    currentWallpaper.dominantColors.forEach { hexLong ->
                        val hexString = "#%06X".format(0xFFFFFF and hexLong.toInt())
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(hexLong))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(hexString, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleSmall
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    currentWallpaper.tags.forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCollectionSheet) {
        SaveToCollectionSheet(
            wallpaper = currentWallpaper,
            collectionsState = collectionsState,
            onDismiss = { showCollectionSheet = false },
            onSelectCollection = { col ->
                onAddToCollection(col, currentWallpaper)
                showCollectionSheet = false
            },
            onCreateCollection = { name, desc ->
                onCreateCollectionWithWallpaper(name, desc, currentWallpaper)
                showCollectionSheet = false
            }
        )
    }
}

@Composable
private fun MetadataPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaveToCollectionSheet(
    wallpaper: Wallpaper,
    collectionsState: SyncState<List<UserCollectionDoc>>,
    onDismiss: () -> Unit,
    onSelectCollection: (UserCollectionDoc) -> Unit,
    onCreateCollection: (String, String) -> Unit
) {
    var creatingNew by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Save to Collection",
                style = MaterialTheme.typography.headlineSmall
            )

            if (creatingNew) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Collection Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newDesc,
                    onValueChange = { newDesc = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { creatingNew = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back")
                    }
                    Button(
                        onClick = {
                            if (newName.isNotBlank()) {
                                onCreateCollection(newName.trim(), newDesc.trim())
                            }
                        },
                        enabled = newName.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create & Save")
                    }
                }
            } else {
                FilledTonalButton(
                    onClick = { creatingNew = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Create New Collection")
                }

                when (collectionsState) {
                    SyncState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is SyncState.Error -> {
                        Text(
                            text = collectionsState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    is SyncState.Ready -> {
                        if (collectionsState.data.isEmpty()) {
                            Text(
                                text = "You haven't created any collections yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            collectionsState.data.forEach { col ->
                                val alreadyIn = wallpaper.id in col.wallpaperIds
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable { onSelectCollection(col) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(col.name, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                "${col.wallpaperIds.size} wallpapers",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (alreadyIn) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = "Already saved",
                                                tint = MaterialTheme.colorScheme.primary
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
    }
}
