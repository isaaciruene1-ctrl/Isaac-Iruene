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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.model.DiscoveryCollection
import com.example.data.model.Wallpaper
import com.example.ui.components.BackdropWordmark
import com.example.ui.components.FavoriteHeartButton
import com.example.ui.components.WallpaperAsyncImage
import com.example.ui.components.WallpaperGridCard
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userDisplayName: String?,
    favoriteIds: Set<String>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSearchShortcut: () -> Unit,
    onProfileShortcut: () -> Unit,
    onOpenWallpaper: (Wallpaper) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenCollection: (DiscoveryCollection) -> Unit,
    onOpenDepthStudio: (Wallpaper) -> Unit,
    modifier: Modifier = Modifier
) {
    val featured = remember { WallpaperCatalog.getForCollection(DiscoveryCollection.FEATURED).take(6) }
    val trending = remember { WallpaperCatalog.getForCollection(DiscoveryCollection.TRENDING).take(10) }
    val editorsPicks = remember { WallpaperCatalog.getForCollection(DiscoveryCollection.EDITORS_PICKS).take(6) }
    val newArrivals = remember { WallpaperCatalog.getForCollection(DiscoveryCollection.NEW_ARRIVALS).take(10) }
    val depthStudio = remember { WallpaperCatalog.getForCollection(DiscoveryCollection.DEPTH_STUDIO).take(8) }
    val amoled = remember { WallpaperCatalog.getForCollection(DiscoveryCollection.AMOLED).take(10) }
    val ultraHd = remember { WallpaperCatalog.getForCollection(DiscoveryCollection.ULTRA_HD_4K).take(10) }

    val greeting = remember(userDisplayName) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeGreeting = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
        val first = userDisplayName?.trim()?.substringBefore(" ")?.takeIf { it.isNotBlank() }
        if (first != null) "$timeGreeting, $first" else timeGreeting
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Lightweight Branded Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BackdropWordmark(markSize = 28.dp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = CircleShape,
                                modifier = Modifier.size(48.dp)
                            ) {
                                IconButton(
                                    onClick = onSearchShortcut,
                                    modifier = Modifier.testTag("home_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = "Search wallpapers"
                                    )
                                }
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape,
                                modifier = Modifier.size(48.dp)
                            ) {
                                IconButton(
                                    onClick = onProfileShortcut,
                                    modifier = Modifier.testTag("home_profile_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = "Open profile and settings",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$greeting • YOUR SCREEN. YOUR CANVAS.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick 20-Collection Discovery Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DiscoveryCollection.entries) { collection ->
                        FilterChip(
                            selected = false,
                            onClick = { onOpenCollection(collection) },
                            label = { Text(collection.title) },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("home_collection_chip_${collection.name}")
                        )
                    }
                }
            }

            // Featured Carousel
            item {
                val pagerState = rememberPagerState(pageCount = { featured.size })
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = "Featured Canvases",
                        subtitle = "Hand-picked high-resolution studio drops",
                        onSeeAll = { onOpenCollection(DiscoveryCollection.FEATURED) }
                    )
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        pageSpacing = 14.dp
                    ) { page ->
                        val wp = featured[page]
                        FeaturedHeroCard(
                            wallpaper = wp,
                            isFavorite = wp.id in favoriteIds,
                            onClick = { onOpenWallpaper(wp) },
                            onFavoriteToggle = { onToggleFavorite(wp.id) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(featured.size) { index ->
                            val active = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .height(6.dp)
                                    .width(if (active) 22.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (active) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                            )
                        }
                    }
                }
            }

            // Depth Studio Signature Banner + Carousel
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .clickable {
                                depthStudio.firstOrNull()?.let(onOpenDepthStudio)
                                    ?: onOpenCollection(DiscoveryCollection.DEPTH_STUDIO)
                            }
                            .testTag("depth_studio_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Layers,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "DEPTH STUDIO",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Subject-separated canvases crafted for lock-screen clocks",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = "Launch Depth Studio",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    HorizontalWallpaperCarousel(
                        items = depthStudio,
                        favoriteIds = favoriteIds,
                        onOpenWallpaper = onOpenDepthStudio,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }

            // Editor's Picks — Asymmetric Editorial Bento Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = "Editor's Picks",
                        subtitle = "Soft pastel tones & architectural calm",
                        onSeeAll = { onOpenCollection(DiscoveryCollection.EDITORS_PICKS) }
                    )
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        editorsPicks.chunked(2).forEachIndexed { rowIdx, pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                pair.forEachIndexed { colIdx, wp ->
                                    val ratio = if ((rowIdx + colIdx) % 2 == 0) 0.68f else 0.78f
                                    WallpaperGridCard(
                                        wallpaper = wp,
                                        isFavorite = wp.id in favoriteIds,
                                        onClick = { onOpenWallpaper(wp) },
                                        onFavoriteToggle = { onToggleFavorite(wp.id) },
                                        aspectRatio = ratio,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Trending Carousel
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = "Trending Now",
                        subtitle = "Community favorites across 120+ artworks",
                        onSeeAll = { onOpenCollection(DiscoveryCollection.TRENDING) }
                    )
                    HorizontalWallpaperCarousel(
                        items = trending,
                        favoriteIds = favoriteIds,
                        onOpenWallpaper = onOpenWallpaper,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }

            // AMOLED Pitch-Black Showcase
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = "AMOLED Obsidian",
                        subtitle = "True-black contrast with luminous pastel edges",
                        onSeeAll = { onOpenCollection(DiscoveryCollection.AMOLED) }
                    )
                    HorizontalWallpaperCarousel(
                        items = amoled,
                        favoriteIds = favoriteIds,
                        onOpenWallpaper = onOpenWallpaper,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }

            // New Arrivals & 4K Ultra HD
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = "New Arrivals",
                        subtitle = "Freshly curated 4K & 2K QHD releases",
                        onSeeAll = { onOpenCollection(DiscoveryCollection.NEW_ARRIVALS) }
                    )
                    HorizontalWallpaperCarousel(
                        items = newArrivals,
                        favoriteIds = favoriteIds,
                        onOpenWallpaper = onOpenWallpaper,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = "4K Ultra HD Studio",
                        subtitle = "2160 × 3840 crisp detail on every pixel",
                        onSeeAll = { onOpenCollection(DiscoveryCollection.ULTRA_HD_4K) }
                    )
                    HorizontalWallpaperCarousel(
                        items = ultraHd,
                        favoriteIds = favoriteIds,
                        onOpenWallpaper = onOpenWallpaper,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        TextButton(onClick = onSeeAll) {
            Text("Explore")
        }
    }
}

@Composable
private fun FeaturedHeroCard(
    wallpaper: Wallpaper,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(272.dp)
            .clip(RoundedCornerShape(32.dp))
            .clickable(onClick = onClick)
            .testTag("featured_card_${wallpaper.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            WallpaperAsyncImage(
                wallpaper = wallpaper,
                useFullRes = false,
                contentDescription = "${wallpaper.title} featured wallpaper",
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.08f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.78f)
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "${wallpaper.resolutionTier.badge} • ${wallpaper.aspectRatio}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                FavoriteHeartButton(isFavorite = isFavorite, onToggle = onFavoriteToggle)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = wallpaper.category.displayName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = wallpaper.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = wallpaper.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.86f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HorizontalWallpaperCarousel(
    items: List<Wallpaper>,
    favoriteIds: Set<String>,
    onOpenWallpaper: (Wallpaper) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(items, key = { it.id }) { wp ->
            WallpaperGridCard(
                wallpaper = wp,
                isFavorite = wp.id in favoriteIds,
                onClick = { onOpenWallpaper(wp) },
                onFavoriteToggle = { onToggleFavorite(wp.id) },
                aspectRatio = 0.64f,
                modifier = Modifier.width(164.dp)
            )
        }
    }
}
