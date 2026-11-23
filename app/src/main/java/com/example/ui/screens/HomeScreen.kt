package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.util.Random
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
import com.example.data.model.UiStyleMode
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperCategory
import com.example.ui.components.BackdropWordmark
import com.example.ui.components.FavoriteHeartButton
import com.example.ui.components.TactileHudIconButton
import com.example.ui.components.TactileSurface
import com.example.ui.components.WallpaperAsyncImage
import com.example.ui.components.WallpaperGridCard
import com.example.ui.components.glassSpecularBorder
import com.example.ui.theme.AppMotion
import com.example.ui.theme.LocalReducedMotionFlag
import com.example.ui.theme.LocalUiStyleMode
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    favoriteIds: Set<String>,
    isRefreshing: Boolean,
    showCategoryChips: Boolean,
    discoverySeed: Long = 0L,
    onRefresh: () -> Unit,
    onShuffleDiscovery: () -> Unit = {},
    onSearchShortcut: () -> Unit,
    onSettingsShortcut: () -> Unit,
    onProfileShortcut: () -> Unit,
    onOpenWallpaper: (Wallpaper) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSelectCategory: (WallpaperCategory) -> Unit,
    onOpenCollection: (DiscoveryCollection) -> Unit,
    onOpenDepthStudio: (Wallpaper) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiStyle = LocalUiStyleMode.current
    val reducedMotion = LocalReducedMotionFlag.current
    val isGlass = uiStyle == UiStyleMode.FROSTED_GLASS_OS
    val totalCatalogCount = remember { WallpaperCatalog.allWallpapers.size }

    val featured = remember(discoverySeed) {
        val rng = Random(discoverySeed)
        val featuredPool = WallpaperCatalog.getForCollection(DiscoveryCollection.FEATURED)
        val diverseByCategory = WallpaperCategory.entries.mapNotNull { cat ->
            featuredPool.filter { it.category == cat }.shuffled(rng).firstOrNull()
                ?: WallpaperCatalog.allWallpapers.filter { it.category == cat }.shuffled(rng).firstOrNull()
        }.shuffled(rng)
        diverseByCategory.take(6)
    }
    val trending = remember(discoverySeed) {
        WallpaperCatalog.getForCollection(DiscoveryCollection.TRENDING)
            .let { base ->
                val expanded = (base + WallpaperCatalog.allWallpapers.sortedByDescending { it.popularityScore }.take(36)).distinctBy { it.id }
                expanded.shuffled(Random(discoverySeed + 3L)).take(12)
            }
    }
    val editorsPicks = remember(discoverySeed) {
        WallpaperCatalog.getForCollection(DiscoveryCollection.EDITORS_PICKS)
            .shuffled(Random(discoverySeed + 2L))
            .take(6)
    }
    val newArrivals = remember(discoverySeed) {
        WallpaperCatalog.allWallpapers
            .sortedByDescending { it.addedOrder }
            .take(40)
            .shuffled(Random(discoverySeed + 5L))
            .take(12)
    }
    val depthStudio = remember(discoverySeed) {
        WallpaperCatalog.getForCollection(DiscoveryCollection.DEPTH_STUDIO)
            .shuffled(Random(discoverySeed + 1L))
            .take(10)
    }
    val amoled = remember(discoverySeed) {
        WallpaperCatalog.getForCollection(DiscoveryCollection.AMOLED)
            .shuffled(Random(discoverySeed + 4L))
            .take(12)
    }
    val ultraHd = remember(discoverySeed) {
        WallpaperCatalog.getForCollection(DiscoveryCollection.ULTRA_HD_4K)
            .shuffled(Random(discoverySeed + 6L))
            .take(12)
    }

    val greeting = remember(discoverySeed) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeGreeting = when (hour) {
            in 5..11 -> listOf("Good morning", "Morning light awaits", "Fresh dawn canvas", "Rise & sculpt")
            in 12..16 -> listOf("Good afternoon", "Midday focus", "Afternoon studio flow", "Sunlit horizon")
            in 17..21 -> listOf("Good evening", "Golden hour drop", "Evening calm", "Twilight palette")
            else -> listOf("Late-night studio", "Midnight OLED calm", "Quiet starfield hours", "After-dark canvas")
        }
        val creativeSalutes = listOf(
            "Curate your lock screen",
            "Depth & glass ready",
            "Dial in today's frame",
            "Spatial clock craft"
        )
        val pool = timeGreeting + creativeSalutes
        val idx = (((hour + discoverySeed) and 0x7FFFFFFFL) % pool.size).toInt()
        pool[idx]
    }

    val dynamicMoodLine = remember(discoverySeed) {
        val moods = listOf(
            "Freshly curated from $totalCatalogCount+ high-res canvases",
            "Stock drops, superhero crests & architectural calm",
            "33 adaptive lock-screen clocks & Depth Engine V2",
            "Architectural calm, OLED obsidian & natural depth"
        )
        val idx = ((discoverySeed and 0x7FFFFFFFL) % moods.size).toInt()
        moods[idx]
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
                    var overflowExpanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BackdropWordmark(markSize = 28.dp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                TactileHudIconButton(
                                    icon = Icons.Rounded.MoreVert,
                                    contentDescription = "Open menu",
                                    onClick = { overflowExpanded = true },
                                    active = overflowExpanded,
                                    modifier = Modifier.testTag("home_overflow_menu_button")
                                )

                                DropdownMenu(
                                    expanded = overflowExpanded,
                                    onDismissRequest = { overflowExpanded = false },
                                    shape = RoundedCornerShape(22.dp),
                                    containerColor = if (isGlass) {
                                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                    border = glassSpecularBorder(),
                                    tonalElevation = if (isGlass) 0.dp else 4.dp,
                                    shadowElevation = if (isGlass) 12.dp else 4.dp,
                                    modifier = Modifier.testTag("home_overflow_dropdown")
                                ) {
                                    val menuColors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.onSurface,
                                        leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Search Wallpapers") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Search,
                                                contentDescription = null
                                            )
                                        },
                                        colors = menuColors,
                                        onClick = {
                                            overflowExpanded = false
                                            onSearchShortcut()
                                        },
                                        modifier = Modifier.testTag("home_search_button")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Studio Settings") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Settings,
                                                contentDescription = null
                                            )
                                        },
                                        colors = menuColors,
                                        onClick = {
                                            overflowExpanded = false
                                            onSettingsShortcut()
                                        },
                                        modifier = Modifier.testTag("home_settings_button")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Profile & Library") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Person,
                                                contentDescription = null
                                            )
                                        },
                                        colors = menuColors,
                                        onClick = {
                                            overflowExpanded = false
                                            onProfileShortcut()
                                        },
                                        modifier = Modifier.testTag("home_profile_button")
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Refresh Feed") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Refresh,
                                                contentDescription = null
                                            )
                                        },
                                        colors = menuColors,
                                        onClick = {
                                            overflowExpanded = false
                                            onRefresh()
                                        },
                                        modifier = Modifier.testTag("home_refresh_menu_item")
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedContent(
                        targetState = "$greeting • $dynamicMoodLine",
                        transitionSpec = {
                            if (reducedMotion) {
                                fadeIn(tween(120)) togetherWith fadeOut(tween(100))
                            } else if (isGlass) {
                                AppMotion.screenEnter(uiStyle, reducedMotion = false) togetherWith
                                    AppMotion.screenExit(uiStyle, reducedMotion = false)
                            } else {
                                fadeIn(tween(240)) togetherWith fadeOut(tween(170))
                            }
                        },
                        label = "home_greeting_line"
                    ) { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Opt-in Category Filter Chips (disabled by default, enabled in Settings)
            if (showCategoryChips) {
                item(key = "home_category_chips_row") {
                    val homeCategories = remember {
                        listOf(
                            WallpaperCategory.ABSTRACT to "Abstract",
                            WallpaperCategory.NATURE to "Nature",
                            WallpaperCategory.ARCHITECTURE to "Architecture",
                            WallpaperCategory.MINIMALIST to "Minimal",
                            WallpaperCategory.AMOLED to "AMOLED",
                            WallpaperCategory.SPACE to "Space",
                            WallpaperCategory.DEPTH_STUDIO to "Depth Studio",
                            WallpaperCategory.FUTURISTIC to "Futuristic",
                            WallpaperCategory.AUTOMOTIVE to "Automotive",
                            WallpaperCategory.CINEMATIC to "Cinematic",
                            WallpaperCategory.ANIME_ARTISTIC to "Artistic"
                        )
                    }
                    AnimatedVisibility(
                        visible = showCategoryChips,
                        enter = AppMotion.drawerEnter(uiStyle, reducedMotion),
                        exit = AppMotion.drawerExit(uiStyle, reducedMotion)
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.testTag("home_category_chips_row")
                        ) {
                            items(homeCategories, key = { (cat, _) -> cat.name }) { (category, label) ->
                                FilterChip(
                                    selected = false,
                                    onClick = { onSelectCategory(category) },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = if (isGlass) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.86f)
                                        else MaterialTheme.colorScheme.surfaceContainerLow,
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.testTag("home_category_chip_${category.name}")
                                )
                            }
                        }
                    }
                }
            }

            // Featured Carousel
            item {
                val pagerState = rememberPagerState(pageCount = { featured.size.coerceAtLeast(1) })
                var previousSeed by remember { mutableStateOf(discoverySeed) }
                LaunchedEffect(discoverySeed) {
                    if (previousSeed != discoverySeed) {
                        previousSeed = discoverySeed
                        if (pagerState.currentPage != 0) {
                            pagerState.animateScrollToPage(
                                page = 0,
                                animationSpec = tween(durationMillis = 350)
                            )
                        }
                    }
                }
                LaunchedEffect(discoverySeed, pagerState.settledPage) {
                    if (featured.size > 1) {
                        delay(5600L)
                        val nextPage = (pagerState.settledPage + 1) % featured.size
                        pagerState.animateScrollToPage(
                            page = nextPage,
                            animationSpec = tween(durationMillis = 600)
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = "Featured Canvases",
                        subtitle = "Rotating high-resolution studio drops across categories",
                        onSeeAll = { onOpenCollection(DiscoveryCollection.FEATURED) }
                    )
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isWide = maxWidth >= 600.dp
                        HorizontalPager(
                            state = pagerState,
                            pageSize = if (isWide) PageSize.Fixed(390.dp) else PageSize.Fill,
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            pageSpacing = 14.dp
                        ) { page ->
                            featured.getOrNull(page)?.let { wp ->
                                FeaturedHeroCard(
                                    wallpaper = wp,
                                    isFavorite = wp.id in favoriteIds,
                                    onClick = { onOpenWallpaper(wp) },
                                    onFavoriteToggle = { onToggleFavorite(wp.id) }
                                )
                            }
                        }
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
                    TactileSurface(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
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
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val columns = if (maxWidth >= 680.dp) 3 else 2
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            editorsPicks.chunked(columns).forEachIndexed { rowIdx, rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEachIndexed { colIdx, wp ->
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
                                    repeat(columns - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
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
                        subtitle = "Community favorites across $totalCatalogCount+ artworks",
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
    val isGlass = LocalUiStyleMode.current == UiStyleMode.FROSTED_GLASS_OS
    Surface(
        shape = RoundedCornerShape(32.dp),
        border = glassSpecularBorder(),
        tonalElevation = if (isGlass) 0.dp else 4.dp,
        shadowElevation = if (isGlass) 8.dp else 0.dp,
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
                contentDescription = "${wallpaper.category.displayName} featured wallpaper",
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
                    color = MaterialTheme.colorScheme.surface.copy(alpha = if (isGlass) 0.72f else 0.85f),
                    border = glassSpecularBorder(),
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
                    text = wallpaper.category.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = wallpaper.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.88f),
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
