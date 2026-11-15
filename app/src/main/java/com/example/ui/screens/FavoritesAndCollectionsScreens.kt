package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.catalog.WallpaperCatalog
import com.example.data.model.UserCollectionDoc
import com.example.data.model.Wallpaper
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.WallpaperAsyncImage
import com.example.ui.components.WallpaperGridCard
import com.example.ui.viewmodel.SyncState

@Composable
fun FavoritesScreen(
    favoritesState: SyncState<List<Wallpaper>>,
    recentlyViewed: List<Wallpaper>,
    favoriteIds: Set<String>,
    columns: Int,
    onOpenWallpaper: (Wallpaper) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onExploreClick: () -> Unit,
    isGuest: Boolean = false,
    onSignInClick: (() -> Unit)? = null,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isGuest) "Saved locally on this device (Guest Mode)" else "Synced across your Google Studio account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (isGuest && onSignInClick != null) {
                        TextButton(onClick = onSignInClick) {
                            Text("Sign in to sync")
                        }
                    }
                }
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
                                subtitle = "Tap the heart icon on any artwork in BACKDROP to build your personal studio gallery.",
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
    onCreateCollection: (String, String) -> Unit,
    onRenameCollection: (UserCollectionDoc, String, String) -> Unit,
    onDeleteCollection: (UserCollectionDoc) -> Unit,
    onOpenCollectionDetail: (UserCollectionDoc) -> Unit,
    isGuest: Boolean = false,
    onSignInClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingCollection by remember { mutableStateOf<UserCollectionDoc?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .padding(bottom = 84.dp)
                    .testTag("create_collection_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Create collection")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Collections",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isGuest) "Local mood boards (Guest Mode)" else "Curate personal mood boards & seasonal setups",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        if (isGuest && onSignInClick != null) {
                            TextButton(onClick = onSignInClick) {
                                Text("Sign in")
                            }
                        }
                    }
                }
            }

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
                        items(cols, key = { it.id }) { collection ->
                            val previewWallpapers = remember(collection.wallpaperIds) {
                                collection.wallpaperIds.take(3).mapNotNull { WallpaperCatalog.getById(it) }
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = RoundedCornerShape(26.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(26.dp))
                                    .clickable { onOpenCollectionDetail(collection) }
                                    .testTag("collection_card_${collection.id}")
                            ) {
                                Column {
                                    // Visual Preview Mosaic
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
                                                    contentDescription = wp.title,
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
                                            IconButton(onClick = { editingCollection = collection }) {
                                                Icon(Icons.Rounded.Edit, contentDescription = "Rename collection")
                                            }
                                            IconButton(onClick = { onDeleteCollection(collection) }) {
                                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete collection")
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
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to collections")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.headlineMedium,
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
