package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.catalog.WallpaperCatalog
import com.example.data.model.LockScreenSetDoc
import com.example.data.model.UiStyleMode
import com.example.data.model.UserCollectionDoc
import com.example.data.model.Wallpaper
import com.example.ui.components.ClockDesignConfig
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.StudioSegmentedBar
import com.example.ui.components.TactileSurface
import com.example.ui.components.WallpaperAsyncImage
import com.example.ui.components.WallpaperGridCard
import com.example.ui.components.glassSpecularBorder
import com.example.ui.theme.AppMotion
import com.example.ui.theme.LocalReducedMotionFlag
import com.example.ui.theme.LocalUiStyleMode
import com.example.ui.theme.rememberStudioHaptics
import com.example.ui.viewmodel.SyncState

private enum class CollectionsTabSection(val label: String) {
    BOARDS("Boards"),
    SAVED_SETS("Saved Sets")
}

@Composable
fun FavoritesScreen(
    favoritesState: SyncState<List<Wallpaper>>,
    recentlyViewed: List<Wallpaper>,
    favoriteIds: Set<String>,
    columns: Int,
    onOpenWallpaper: (Wallpaper) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Favorites",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Your personal curated studio gallery",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (recentlyViewed.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Recently Viewed",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recentlyViewed, key = { it.id }) { wp ->
                            WallpaperGridCard(
                                wallpaper = wp,
                                isFavorite = wp.id in favoriteIds,
                                onClick = { onOpenWallpaper(wp) },
                                onFavoriteToggle = { onToggleFavorite(wp.id) },
                                aspectRatio = 0.68f,
                                modifier = Modifier.width(136.dp)
                            )
                        }
                    }
                }
            }
        }

        when (favoritesState) {
            SyncState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is SyncState.Error -> {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        EmptyStatePanel(
                            title = "Could not load favorites",
                            subtitle = favoritesState.message
                        )
                    }
                }
            }
            is SyncState.Ready -> {
                val list = favoritesState.data
                if (list.isEmpty()) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            EmptyStatePanel(
                                title = "No favorites saved yet",
                                subtitle = "Tap the heart icon on any artwork in Backdrops to build your personal studio gallery.",
                                actionLabel = "Explore Wallpapers",
                                onAction = onExploreClick
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "Saved Canvases (${list.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                    val rows = list.chunked(columns)
                    items(rows) { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { wp ->
                                WallpaperGridCard(
                                    wallpaper = wp,
                                    isFavorite = wp.id in favoriteIds,
                                    onClick = { onOpenWallpaper(wp) },
                                    onFavoriteToggle = { onToggleFavorite(wp.id) },
                                    aspectRatio = 0.65f,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(columns - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionsScreen(
    collectionsState: SyncState<List<UserCollectionDoc>>,
    lockScreenSetsState: SyncState<List<LockScreenSetDoc>> = SyncState.Ready(emptyList()),
    columns: Int = 1,
    onCreateCollection: (String, String) -> Unit,
    onRenameCollection: (UserCollectionDoc, String, String) -> Unit,
    onDeleteCollection: (UserCollectionDoc) -> Unit,
    onOpenCollectionDetail: (UserCollectionDoc) -> Unit,
    onOpenLockScreenSet: (LockScreenSetDoc) -> Unit = {},
    onToggleLockScreenSetFavorite: (String) -> Unit = {},
    onDeleteLockScreenSet: (LockScreenSetDoc) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingCollection by remember { mutableStateOf<UserCollectionDoc?>(null) }
    var activeSection by rememberSaveable { mutableStateOf(CollectionsTabSection.BOARDS) }

    val uiStyle = LocalUiStyleMode.current
    val reducedMotion = LocalReducedMotionFlag.current
    val isGlassUi = uiStyle == UiStyleMode.FROSTED_GLASS_OS
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = if (isGlassUi) Color.Transparent else MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (activeSection == CollectionsTabSection.BOARDS) {
                val fabMod = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = if (columns > 1) 8.dp else 84.dp)
                    .testTag("create_collection_fab")
                if (isGlassUi) {
                    Surface(
                        onClick = { showCreateDialog = true },
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.90f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(20.dp),
                        border = glassSpecularBorder(),
                        shadowElevation = 10.dp,
                        modifier = fabMod.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = "Create collection",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    FloatingActionButton(
                        onClick = { showCreateDialog = true },
                        modifier = fabMod
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Create collection")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (activeSection == CollectionsTabSection.BOARDS) "Collections" else "Lock-Screen Sets",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (activeSection == CollectionsTabSection.BOARDS) {
                            "Curate personal mood boards & seasonal setups"
                        } else {
                            "Saved Depth Studio compositions ready to re-open or apply"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StudioSegmentedBar(
                    options = CollectionsTabSection.entries,
                    selected = activeSection,
                    labelProvider = { it.label },
                    onSelect = { activeSection = it },
                    tagPrefix = "collections_section"
                )
            }

            AnimatedContent(
                targetState = activeSection,
                transitionSpec = {
                    AppMotion.screenEnter(uiStyle, reducedMotion) togetherWith
                        AppMotion.screenExit(uiStyle, reducedMotion)
                },
                label = "collections_section_transition",
                modifier = Modifier.weight(1f)
            ) { section ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (section == CollectionsTabSection.BOARDS) {
                        when (collectionsState) {
                            SyncState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                            is SyncState.Error -> {
                                item {
                                    EmptyStatePanel(
                                        title = "Could not load collections",
                                        subtitle = collectionsState.message
                                    )
                                }
                            }
                            is SyncState.Ready -> {
                                val cols = collectionsState.data
                                if (cols.isEmpty()) {
                                    item {
                                        EmptyStatePanel(
                                            title = "Create your first board",
                                            subtitle = "Group wallpapers by mood, phone case color, or workspace aesthetic.",
                                            actionLabel = "New Collection",
                                            onAction = { showCreateDialog = true }
                                        )
                                    }
                                } else {
                                    val rows = cols.chunked(columns.coerceAtLeast(1))
                                    items(rows) { rowCols ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            rowCols.forEach { collection ->
                                                CollectionBoardCard(
                                                    collection = collection,
                                                    onOpen = { onOpenCollectionDetail(collection) },
                                                    onEdit = { editingCollection = collection },
                                                    onDelete = { onDeleteCollection(collection) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            repeat(columns.coerceAtLeast(1) - rowCols.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        when (lockScreenSetsState) {
                            SyncState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                            is SyncState.Error -> {
                                item {
                                    EmptyStatePanel(
                                        title = "Could not load saved sets",
                                        subtitle = lockScreenSetsState.message
                                    )
                                }
                            }
                            is SyncState.Ready -> {
                                val sets = lockScreenSetsState.data
                                if (sets.isEmpty()) {
                                    item {
                                        EmptyStatePanel(
                                            title = "No Lock-Screen Sets yet",
                                            subtitle = "Design a clock & depth look in Depth Studio and tap Save Set to keep it here."
                                        )
                                    }
                                } else {
                                    val rows = sets.chunked(columns.coerceAtLeast(1))
                                    items(rows) { rowSets ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            rowSets.forEach { lockSet ->
                                                SavedLockScreenSetCard(
                                                    lockSet = lockSet,
                                                    onOpen = { onOpenLockScreenSet(lockSet) },
                                                    onToggleFavorite = { onToggleLockScreenSetFavorite(lockSet.id) },
                                                    onDelete = { onDeleteLockScreenSet(lockSet) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            repeat(columns.coerceAtLeast(1) - rowSets.size) {
                                                Spacer(modifier = Modifier.weight(1f))
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

    if (showCreateDialog) {
        CollectionEditorDialog(
            title = "New Collection",
            initialName = "",
            initialDescription = "",
            confirmLabel = "Create",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc ->
                onCreateCollection(name, desc)
                showCreateDialog = false
            }
        )
    }

    editingCollection?.let { target ->
        CollectionEditorDialog(
            title = "Edit Collection",
            initialName = target.name,
            initialDescription = target.description,
            confirmLabel = "Save",
            onDismiss = { editingCollection = null },
            onConfirm = { name, desc ->
                onRenameCollection(target, name, desc)
                editingCollection = null
            }
        )
    }
}

@Composable
private fun SavedLockScreenSetCard(
    lockSet: LockScreenSetDoc,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiStyle = LocalUiStyleMode.current
    val reducedMotion = LocalReducedMotionFlag.current
    val haptics = rememberStudioHaptics()
    val wallpaper = remember(lockSet.wallpaperId) {
        WallpaperCatalog.getById(lockSet.wallpaperId)
    }
    val parsedClock = remember(lockSet.clockConfigJson) {
        ClockDesignConfig.fromJsonString(lockSet.clockConfigJson) ?: ClockDesignConfig()
    }
    val bookmarkScale by animateFloatAsState(
        targetValue = if (lockSet.isFavorite && !reducedMotion) 1.18f else 1f,
        animationSpec = AppMotion.emphasisSpring(uiStyle, reducedMotion),
        label = "set_bookmark_scale"
    )

    TactileSurface(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onOpen)
            .testTag("lock_set_card_${lockSet.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 72.dp, height = 104.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                if (wallpaper != null) {
                    WallpaperAsyncImage(
                        wallpaper = wallpaper,
                        useFullRes = false,
                        contentDescription = lockSet.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.PhotoLibrary,
                        contentDescription = lockSet.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = lockSet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${parsedClock.face.label} • ${parsedClock.finish.label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val filterLabel = runCatching { CanvasLookFilter.valueOf(lockSet.lookFilterName).label }
                    .getOrDefault("Original")
                Text(
                    text = "${parsedClock.font.label} • $filterLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    FilledTonalButton(
                        onClick = onOpen,
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("lock_set_open_${lockSet.id}")
                    ) {
                        Icon(Icons.Rounded.Layers, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open Studio", style = MaterialTheme.typography.labelMedium)
                    }
                    IconButton(
                        onClick = {
                            haptics.performConfirm()
                            onToggleFavorite()
                        },
                        modifier = Modifier.testTag("lock_set_fav_${lockSet.id}")
                    ) {
                        Icon(
                            imageVector = if (lockSet.isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (lockSet.isFavorite) "Remove set bookmark" else "Bookmark set",
                            tint = if (lockSet.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.scale(bookmarkScale)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("lock_set_delete_${lockSet.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete saved set",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionBoardCard(
    collection: UserCollectionDoc,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val previewWallpapers = remember(collection.wallpaperIds) {
        collection.wallpaperIds.take(3).mapNotNull { WallpaperCatalog.getById(it) }
    }
    TactileSurface(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onOpen)
            .testTag("collection_card_${collection.id}")
    ) {
        Column {
            if (previewWallpapers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    previewWallpapers.forEach { wp ->
                        WallpaperAsyncImage(
                            wallpaper = wp,
                            useFullRes = false,
                            contentDescription = "Collection canvas",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp))
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Empty collection — save canvases from Detail",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val subtitle = buildString {
                        append("${collection.wallpaperIds.size} canvases")
                        if (collection.description.isNotBlank()) {
                            append(" • ${collection.description}")
                        }
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Rename collection")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete collection")
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionDetailScreen(
    collection: UserCollectionDoc,
    favoriteIds: Set<String>,
    columns: Int,
    onBack: () -> Unit,
    onOpenWallpaper: (Wallpaper) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRemoveFromCollection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val wallpapers = remember(collection.wallpaperIds) {
        collection.wallpaperIds.mapNotNull { WallpaperCatalog.getById(it) }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back to collections",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = collection.description.ifBlank { "${wallpapers.size} saved canvases" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (wallpapers.isEmpty()) {
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    EmptyStatePanel(
                        title = "Collection is empty",
                        subtitle = "Open any wallpaper and tap Save to Collection to add canvases here."
                    )
                }
            }
        } else {
            val rows = wallpapers.chunked(columns)
            items(rows) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { wp ->
                        Box(modifier = Modifier.weight(1f)) {
                            WallpaperGridCard(
                                wallpaper = wp,
                                isFavorite = wp.id in favoriteIds,
                                onClick = { onOpenWallpaper(wp) },
                                onFavoriteToggle = { onToggleFavorite(wp.id) },
                                aspectRatio = 0.65f,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Surface(
                                color = Color.Black.copy(alpha = 0.55f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                                    .size(36.dp)
                            ) {
                                IconButton(onClick = { onRemoveFromCollection(wp.id) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.RemoveCircleOutline,
                                        contentDescription = "Remove from collection",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionEditorDialog(
    title: String,
    initialName: String,
    initialDescription: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Collection Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), description.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
