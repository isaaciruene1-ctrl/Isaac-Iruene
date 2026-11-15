package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.catalog.WallpaperCatalog
import com.example.data.model.ColorFamily
import com.example.data.model.DiscoveryCollection
import com.example.data.model.ResolutionTier
import com.example.data.model.SearchFilters
import com.example.data.model.SearchSort
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperCategory
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.WallpaperGridCard

@Composable
fun ExploreSearchScreen(
    filters: SearchFilters,
    results: List<Wallpaper>,
    recentSearches: List<String>,
    favoriteIds: Set<String>,
    columns: Int,
    onQueryChange: (String) -> Unit,
    onCommitSearch: (String) -> Unit,
    onClearHistory: () -> Unit,
    onSelectResolution: (ResolutionTier?) -> Unit,
    onToggleAmoled: () -> Unit,
    onTogglePortrait: () -> Unit,
    onToggleDepthStudio: () -> Unit,
    onSelectColor: (ColorFamily?) -> Unit,
    onSelectCategory: (WallpaperCategory?) -> Unit,
    onSelectCollection: (DiscoveryCollection?) -> Unit,
    onSelectSort: (SearchSort) -> Unit,
    onResetFilters: () -> Unit,
    onOpenWallpaper: (Wallpaper) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val hasActiveFilter = filters != SearchFilters()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Explore & Search",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${WallpaperCatalog.allWallpapers.size} curated canvases • Filter by palette, resolution, or style",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = filters.query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search titles, tags, colors, 4K, Kyoto...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (filters.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear search query"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onCommitSearch(filters.query)
                            focusManager.clearFocus()
                        }
                    ),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_search_input")
                )
            }
        }

        // Suggestions & Recent Searches
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (recentSearches.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Searches",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onClearHistory) {
                            Text("Clear")
                        }
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentSearches) { item ->
                            FilterChip(
                                selected = filters.query.equals(item, ignoreCase = true),
                                onClick = {
                                    onQueryChange(item)
                                    onCommitSearch(item)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(item) },
                                shape = RoundedCornerShape(50)
                            )
                        }
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(WallpaperCatalog.searchSuggestions) { suggestion ->
                        FilterChip(
                            selected = filters.query.equals(suggestion, ignoreCase = true),
                            onClick = {
                                onQueryChange(suggestion)
                                onCommitSearch(suggestion)
                            },
                            label = { Text(suggestion) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }
        }

        // Resolution & Mode Filter Chips (4K, QHD, Full HD, AMOLED, Portrait, Depth Studio, Recently Added, Most Popular)
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filters.resolutionTier == ResolutionTier.UHD_4K,
                        onClick = {
                            onSelectResolution(
                                if (filters.resolutionTier == ResolutionTier.UHD_4K) null else ResolutionTier.UHD_4K
                            )
                        },
                        label = { Text("4K") },
                        modifier = Modifier.testTag("filter_4k")
                    )
                }
                item {
                    FilterChip(
                        selected = filters.resolutionTier == ResolutionTier.QHD_1440,
                        onClick = {
                            onSelectResolution(
                                if (filters.resolutionTier == ResolutionTier.QHD_1440) null else ResolutionTier.QHD_1440
                            )
                        },
                        label = { Text("2K QHD") },
                        modifier = Modifier.testTag("filter_qhd")
                    )
                }
                item {
                    FilterChip(
                        selected = filters.resolutionTier == ResolutionTier.FHD_1080,
                        onClick = {
                            onSelectResolution(
                                if (filters.resolutionTier == ResolutionTier.FHD_1080) null else ResolutionTier.FHD_1080
                            )
                        },
                        label = { Text("Full HD") }
                    )
                }
                item {
                    FilterChip(
                        selected = filters.amoledOnly,
                        onClick = onToggleAmoled,
                        label = { Text("AMOLED") },
                        modifier = Modifier.testTag("filter_amoled")
                    )
                }
                item {
                    FilterChip(
                        selected = filters.portraitOnly,
                        onClick = onTogglePortrait,
                        label = { Text("Portrait") }
                    )
                }
                item {
                    FilterChip(
                        selected = filters.depthStudioOnly,
                        onClick = onToggleDepthStudio,
                        label = { Text("Depth Studio") },
                        modifier = Modifier.testTag("filter_depth")
                    )
                }
                item {
                    FilterChip(
                        selected = filters.sort == SearchSort.RECENTLY_ADDED,
                        onClick = {
                            onSelectSort(
                                if (filters.sort == SearchSort.RECENTLY_ADDED) SearchSort.RELEVANT
                                else SearchSort.RECENTLY_ADDED
                            )
                        },
                        label = { Text("Recently Added") }
                    )
                }
                item {
                    FilterChip(
                        selected = filters.sort == SearchSort.MOST_POPULAR,
                        onClick = {
                            onSelectSort(
                                if (filters.sort == SearchSort.MOST_POPULAR) SearchSort.RELEVANT
                                else SearchSort.MOST_POPULAR
                            )
                        },
                        label = { Text("Most Popular") }
                    )
                }
            }
        }

        // Intelligent Colour Discovery
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Colour Discovery",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(ColorFamily.entries) { colorFamily ->
                        val selected = filters.colorFamily == colorFamily
                        Surface(
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(20.dp),
                            tonalElevation = if (selected) 3.dp else 1.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    onSelectColor(if (selected) null else colorFamily)
                                }
                                .testTag("color_filter_${colorFamily.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorFamily.swatchHex))
                                )
                                Text(
                                    text = colorFamily.label,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Visual Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Browse Categories",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(WallpaperCategory.entries) { cat ->
                        val selected = filters.category == cat
                        Surface(
                            color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier
                                .width(152.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { onSelectCategory(if (selected) null else cat) }
                                .testTag("category_card_${cat.name}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = cat.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cat.tagline,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Filter Summary & Result Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val activeLabel = buildString {
                    append("${results.size} wallpapers")
                    filters.collection?.let { append(" • ${it.title}") }
                    filters.category?.let { append(" • ${it.displayName}") }
                    filters.colorFamily?.let { append(" • ${it.label}") }
                }
                Text(
                    text = activeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (hasActiveFilter) {
                    TextButton(
                        onClick = onResetFilters,
                        modifier = Modifier.testTag("reset_filters_button")
                    ) {
                        Text("Reset")
                    }
                }
            }
        }

        if (results.isEmpty()) {
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    EmptyStatePanel(
                        title = "No matching canvases",
                        subtitle = "Try clearing a resolution or color filter to explore all ${WallpaperCatalog.allWallpapers.size} wallpapers.",
                        actionLabel = "Reset all filters",
                        onAction = onResetFilters
                    )
                }
            }
        } else {
            val rows = results.chunked(columns)
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
