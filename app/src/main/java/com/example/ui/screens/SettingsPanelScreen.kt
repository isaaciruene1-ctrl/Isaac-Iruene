package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.data.model.ImageQualityPref
import com.example.data.model.ThemeMode
import com.example.data.model.UiStyleMode
import com.example.data.model.UserPreferencesDoc
import com.example.ui.components.BackdropLogoMark
import com.example.ui.components.TactileSurface
import com.example.ui.components.glassSpecularBorder
import com.example.ui.theme.AppMotion
import com.example.ui.theme.LocalReducedMotionFlag
import com.example.ui.theme.LocalUiStyleMode
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanelScreen(
    preferences: UserPreferencesDoc,
    cacheBytes: Long,
    onBack: () -> Unit,
    onUiStyleChange: (UiStyleMode) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAmoledDarkChange: (Boolean) -> Unit,
    onHomeCategoryChipsChange: (Boolean) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit = {},
    onReducedMotionChange: (Boolean) -> Unit = {},
    onGlassDepthIntensityChange: (Float) -> Unit = {},
    onImageQualityChange: (ImageQualityPref) -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit,
    onRefreshCacheSize: () -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGlass = LocalUiStyleMode.current == UiStyleMode.FROSTED_GLASS_OS

    LaunchedEffect(Unit) {
        onRefreshCacheSize()
    }

    val cacheLabel = remember(cacheBytes) {
        when {
            cacheBytes < 1024L -> "$cacheBytes B"
            cacheBytes < 1024L * 1024L -> "${cacheBytes / 1024L} KB"
            else -> String.format(Locale.US, "%.1f MB", cacheBytes / (1024f * 1024f))
        }
    }
    val totalCount = remember { WallpaperCatalog.allWallpapers.size }

    Scaffold(
        containerColor = if (isGlass) Color.Transparent else MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "Studio Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = preferences.parsedUiStyle.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_panel_screen")
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.displayCutout)),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWide = maxWidth >= 680.dp
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 980.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Interface Style Switcher: Material 3 vs Frosted Glass OS
                item {
                    InterfaceStyleSwitcherCard(
                        currentStyle = preferences.parsedUiStyle,
                        glassDepthIntensity = preferences.glassDepthIntensity,
                        onUiStyleChange = onUiStyleChange,
                        onGlassDepthIntensityChange = onGlassDepthIntensityChange
                    )
                }

                if (isWide) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(18.dp)
                            ) {
                                AppearanceLayoutCard(
                                    preferences = preferences,
                                    onThemeModeChange = onThemeModeChange,
                                    onAmoledDarkChange = onAmoledDarkChange,
                                    onHomeCategoryChipsChange = onHomeCategoryChipsChange,
                                    onHapticFeedbackChange = onHapticFeedbackChange,
                                    onReducedMotionChange = onReducedMotionChange
                                )
                                StorageAboutCard(
                                    cacheLabel = cacheLabel,
                                    totalCount = totalCount,
                                    onClearCache = onClearCache
                                )
                            }
                            DownloadsResolutionCard(
                                preferences = preferences,
                                isGlass = isGlass,
                                onImageQualityChange = onImageQualityChange,
                                onWifiOnlyChange = onWifiOnlyChange,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    item {
                        AppearanceLayoutCard(
                            preferences = preferences,
                            onThemeModeChange = onThemeModeChange,
                            onAmoledDarkChange = onAmoledDarkChange,
                            onHomeCategoryChipsChange = onHomeCategoryChipsChange,
                            onHapticFeedbackChange = onHapticFeedbackChange,
                            onReducedMotionChange = onReducedMotionChange
                        )
                    }
                    item {
                        DownloadsResolutionCard(
                            preferences = preferences,
                            isGlass = isGlass,
                            onImageQualityChange = onImageQualityChange,
                            onWifiOnlyChange = onWifiOnlyChange
                        )
                    }
                    item {
                        StorageAboutCard(
                            cacheLabel = cacheLabel,
                            totalCount = totalCount,
                            onClearCache = onClearCache
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InterfaceStyleSwitcherCard(
    currentStyle: UiStyleMode,
    glassDepthIntensity: Float,
    onUiStyleChange: (UiStyleMode) -> Unit,
    onGlassDepthIntensityChange: (Float) -> Unit
) {
    val reducedMotion = LocalReducedMotionFlag.current
    TactileSurface(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Layers,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Interface Design System",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Applies everywhere — Home, Detail, Preview Studio & Depth Studio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UiStyleMode.entries.forEach { mode ->
                    UiStyleOptionCard(
                        mode = mode,
                        selected = currentStyle == mode,
                        onClick = { onUiStyleChange(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            AnimatedVisibility(
                visible = currentStyle == UiStyleMode.FROSTED_GLASS_OS,
                enter = AppMotion.drawerEnter(currentStyle, reducedMotion),
                exit = AppMotion.drawerExit(currentStyle, reducedMotion)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Contextual Liquid Glass Tuning",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    StudioParamSlider(
                        label = "Glass",
                        valueText = "${(glassDepthIntensity * 100).roundToInt()}%",
                        value = glassDepthIntensity,
                        valueRange = 0.55f..1.45f,
                        onValueChange = onGlassDepthIntensityChange,
                        sliderTag = "settings_glass_depth_slider"
                    )
                }
            }
        }
    }
}

@Composable
private fun AppearanceLayoutCard(
    preferences: UserPreferencesDoc,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAmoledDarkChange: (Boolean) -> Unit,
    onHomeCategoryChipsChange: (Boolean) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onReducedMotionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    TactileSurface(
        shape = RoundedCornerShape(28.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Appearance & Layout",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val modes = listOf(
                    ThemeMode.SYSTEM to "System",
                    ThemeMode.LIGHT to "Light",
                    ThemeMode.DARK to "Dark"
                )
                modes.forEachIndexed { idx, (mode, label) ->
                    SegmentedButton(
                        selected = preferences.parsedThemeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = modes.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
                            inactiveContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("settings_theme_mode_${mode.name}")
                    ) {
                        Text(label)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.DarkMode, contentDescription = null)
                    Column {
                        Text("AMOLED Pitch Black", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "True #000000 backdrop when dark theme is active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = preferences.amoledDark,
                    onCheckedChange = onAmoledDarkChange,
                    modifier = Modifier.testTag("settings_amoled_switch")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.FilterList, contentDescription = null)
                    Column {
                        Text("Home Category Chips", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Pin category pills below the Home greeting",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = preferences.homeCategoryChipsEnabled,
                    onCheckedChange = onHomeCategoryChipsChange,
                    modifier = Modifier.testTag("settings_chips_switch")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.TouchApp, contentDescription = null)
                    Column {
                        Text("Tactile Haptics", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Subtle feedback on nav pill drag, sliders & depth snaps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = preferences.hapticFeedbackEnabled,
                    onCheckedChange = onHapticFeedbackChange,
                    modifier = Modifier.testTag("settings_haptics_switch")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Animation, contentDescription = null)
                    Column {
                        Text("Reduced Motion", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Calm, minimal transitions across tabs and studio drawers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = preferences.reducedMotionEnabled,
                    onCheckedChange = onReducedMotionChange,
                    modifier = Modifier.testTag("settings_reduced_motion_switch")
                )
            }
        }
    }
}

@Composable
private fun DownloadsResolutionCard(
    preferences: UserPreferencesDoc,
    isGlass: Boolean,
    onImageQualityChange: (ImageQualityPref) -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    TactileSurface(
        shape = RoundedCornerShape(28.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Downloads & Resolution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            ImageQualityPref.entries.forEach { pref ->
                val selected = preferences.parsedImageQuality == pref
                Surface(
                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = if (isGlass) 0.84f else 1f),
                    contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(18.dp),
                    border = glassSpecularBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onImageQualityChange(pref) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Rounded.HighQuality,
                            contentDescription = null,
                            tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                pref.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                pref.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Wifi, contentDescription = null)
                    Column {
                        Text("Wi-Fi Only Downloads", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Protect mobile data on 4K architecture packs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = preferences.wifiOnlyDownloads,
                    onCheckedChange = onWifiOnlyChange
                )
            }
        }
    }
}

@Composable
private fun StorageAboutCard(
    cacheLabel: String,
    totalCount: Int,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    TactileSurface(
        shape = RoundedCornerShape(28.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.CleaningServices, contentDescription = null)
                    Column {
                        Text("Artwork Preview Cache", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Using $cacheLabel on device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                OutlinedButton(
                    onClick = onClearCache,
                    modifier = Modifier.testTag("settings_clear_cache_button")
                ) {
                    Text("Clear")
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BackdropLogoMark(size = 28.dp)
                Column {
                    Text(
                        text = "Backdrops Studio • $totalCount Canvases",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$totalCount+ 4K photography & minimalist packs • On-device Depth Engine",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun UiStyleOptionCard(
    mode: UiStyleMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "ui_style_border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.84f)
        },
        label = "ui_style_bg"
    )
    val labelColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val subColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.84f) else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = bgColor,
        contentColor = labelColor,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag("ui_style_${mode.name}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (mode == UiStyleMode.FROSTED_GLASS_OS) {
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.42f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.36f)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                        }
                    )
                    .padding(8.dp)
            ) {
                if (mode == UiStyleMode.MATERIAL_3) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.72f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .height(18.dp)
                                .align(Alignment.Start)
                        ) {}
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .height(16.dp)
                        ) {}
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = labelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = mode.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = subColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
