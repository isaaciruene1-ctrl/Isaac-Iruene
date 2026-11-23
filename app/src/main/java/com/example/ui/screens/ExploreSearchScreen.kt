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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.FilterChipDefaults
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
import com.example.data.model.UiStyleMode
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperCategory
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.TactileSurface
import com.example.ui.components.WallpaperGridCard
import com.example.ui.components.glassSpecularBorder
import com.example.ui.theme.LocalUiStyleMode

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
    val isGlass = LocalUiStyleMode.current == UiStyleMode.FROSTED_GLASS_OS
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = if (isGlass) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.86f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        labelColor = MaterialTheme.colorScheme.onSurface,
        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
    )

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
                TactileSurface(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                ) {
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
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("explore_search_input")
                    )
                }
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
                                colors = chipColors,
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
                            colors = chipColors,
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }
        }

        // Resolution & Mode Filter Chips
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
                        colors = chipColors,
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
                        colors = chipColors,
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
                        label = { Text("Full HD") },
                        colors = chipColors
                    )
                }
                item {
                    FilterChip(
                        selected = filters.amoledOnly,
                        onClick = onToggleAmoled,
                        label = { Text("AMOLED") },
                        colors = chipColors,
                        modifier = Modifier.testTag("filter_amoled")
                    )
                }
                item {
                    FilterChip(
                        selected = filters.portraitOnly,
                        onClick = onTogglePortrait,
                        label = { Text("Portrait") },
                        colors = chipColors
                    )
                }
                item {
                    FilterChip(
                        selected = filters.depthStudioOnly,
                        onClick = onToggleDepthStudio,
                        label = { Text("Depth Studio") },
                        colors = chipColors,
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
                        label = { Text("Recently Added") },
                        colors = chipColors
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
                        label = { Text("Most Popular") },
                        colors = chipColors
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
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(ColorFamily.entries) { colorFamily ->
                        val selected = filters.colorFamily == colorFamily
                        val swatchText = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        Surface(
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = if (isGlass) 0.86f else 1f),
                            contentColor = swatchText,
                            shape = RoundedCornerShape(20.dp),
                            border = glassSpecularBorder(),
                            tonalElevation = if (isGlass) 0.dp else if (selected) 3.dp else 1.dp,
                            shadowElevation = if (isGlass) 4.dp else 0.dp,
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
                                    style = MaterialTheme.typography.labelLarge,
                                    color = swatchText
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
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(WallpaperCategory.entries) { cat ->
                        val selected = filters.category == cat
                        val catTitleColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                        val catSubColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                        Surface(
                            color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = if (isGlass) 0.86f else 1f),
                            contentColor = catTitleColor,
                            shape = RoundedCornerShape(22.dp),
                            border = glassSpecularBorder(),
                            modifier = Modifier
                                .width(156.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { onSelectCategory(if (selected) null else cat) }
                                .testTag("category_card_${cat.name}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = cat.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = catTitleColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cat.tagline,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = catSubColor,
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
                        subtitle = if (filters.query.isNotBlank()) {
                            "No artwork matched \"${filters.query.trim()}\". Clear the query or reset filters to browse all ${WallpaperCatalog.allWallpapers.size} canvases."
                        } else {
                            "Try clearing a resolution or color filter to explore all ${WallpaperCatalog.allWallpapers.size} wallpapers."
                        },
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
