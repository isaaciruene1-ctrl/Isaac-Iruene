package com.example.data.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class WallpaperCategory(val displayName: String, val tagline: String) {
    NATURE("Nature", "Alpine peaks, misty forests & tranquil waters"),
    ABSTRACT("Abstract", "Sculptural forms & pastel light studies"),
    AMOLED("AMOLED", "True-black obsidian canvases with luminous accents"),
    FUTURISTIC("Futuristic", "Sci-fi horizons, neon architecture & cybernetics"),
    ARCHITECTURE("Architecture", "Brutalist concrete, arches & sunlit geometry"),
    AUTOMOTIVE("Automotive", "Studio silhouettes, classic curves & motion"),
    SPACE("Space", "Deep nebulae, planetary eclipses & starfields"),
    MINIMALIST("Minimalist", "Quiet negative space & deliberate restraint"),
    ANIME_ARTISTIC("Anime & Artistic", "Painterly illustrations & atmospheric worlds"),
    CINEMATIC("Cinematic", "Moody film frames & dramatic golden hour"),
    DEPTH_STUDIO("Depth Studio", "Foreground subjects sculpted for lock-screen clocks")
}

enum class DiscoveryCollection(val title: String, val subtitle: String) {
    FEATURED("Featured", "Curated hero canvases for your screen"),
    TRENDING("Trending", "Most loved across BACKDROP this week"),
    EDITORS_PICKS("Editor's Picks", "Hand-selected editorial compositions"),
    NEW_ARRIVALS("New Arrivals", "Fresh high-resolution drops"),
    ULTRA_HD_4K("4K Ultra HD", "Razor-sharp 2160 × 3840+ detail"),
    AMOLED("AMOLED", "Deep obsidian contrast for OLED panels"),
    DEPTH_STUDIO("Depth Studio", "Layered subjects built for clock overlap"),
    NATURE("Nature", "Organic landscapes & coastal calm"),
    ABSTRACT("Abstract", "Fluid glass, ribbons & soft gradients"),
    ARCHITECTURE("Architecture", "Light, shadow & monumental symmetry"),
    AUTOMOTIVE("Automotive", "Precision machines & nocturnal reflections"),
    MINIMALIST("Minimalist", "Breathe-easy calm & negative space"),
    SPACE("Space", "Cosmic horizons & orbital serenity"),
    ANIME("Anime", "Dreamlike illustrated atmospheres"),
    GAMING("Gaming", "Atmospheric worlds & cyber vistas"),
    ANIMALS("Animals", "Intimate wildlife portraits"),
    PHOTOGRAPHY("Photography", "Editorial 35mm & medium-format captures"),
    ARTISTIC("Artistic", "Fine art textures & digital paintings"),
    CINEMATIC("Cinematic", "Widescreen mood & dramatic lighting"),
    COLOUR_COLLECTIONS("Colour Collections", "Harmonic palettes tuned to your mood")
}

enum class ColorFamily(val label: String, val swatchHex: Long) {
    BLUE("Blue", 0xFF5D89BA),
    PURPLE("Purple", 0xFF8B74BD),
    GREEN("Green", 0xFF5A8F7B),
    ORANGE("Orange", 0xFFD9845B),
    MONOCHROME("Monochrome", 0xFF4A4852),
    WARM("Warm-tone", 0xFFD4937E),
    COOL("Cool-tone", 0xFF729BB8)
}

enum class ResolutionTier(val badge: String, val minWidth: Int) {
    UHD_4K("4K UHD", 2160),
    QHD_1440("2K QHD", 1440),
    FHD_1080("1080p Full HD", 1080)
}

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class UiStyleMode(val label: String, val subtitle: String) {
    MATERIAL_3("Material 3", "Expressive tonal surfaces & anchored navigation bar"),
    FROSTED_GLASS_OS("Frosted Glass OS", "Translucent tactile panels, specular rims & floating pill dock")
}

enum class ImageQualityPref(val label: String, val description: String) {
    ORIGINAL("Original 4K/QHD", "Always download and preview full resolution"),
    HIGH("High Efficiency", "Balanced 1440p previews with original downloads"),
    BALANCED("Data Saver", "Compressed thumbnails, full resolution on demand")
}

enum class DepthSubjectHint {
    TOP_ARCH,
    LOWER_SILHOUETTE,
    CENTER_PORTRAIT,
    BOTANICAL_STEM,
    PEAK_HORIZON
}

@Immutable
data class Wallpaper(
    val id: String,
    val title: String,
    val description: String,
    val category: WallpaperCategory,
    val collections: List<DiscoveryCollection>,
    val thumbnailUrl: String,
    val fullResUrl: String,
    @param:DrawableRes val fallbackResId: Int,
    val width: Int,
    val height: Int,
    val resolutionTier: ResolutionTier,
    val aspectRatio: String,
    val dominantColors: List<Long>,
    val colorFamily: ColorFamily,
    val tags: List<String>,
    val creatorName: String,
    val creatorSource: String,
    val isDownloadable: Boolean = true,
    val supportsDepthEffect: Boolean = false,
    val depthSubjectHint: DepthSubjectHint = DepthSubjectHint.LOWER_SILHOUETTE,
    val popularityScore: Int = 80,
    val addedOrder: Int = 1,
    val isBundledAsset: Boolean = false
) {
    val resolutionText: String get() = "$width × $height"
}

@IgnoreExtraProperties
data class FavoriteDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val wallpaperId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserCollectionDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val wallpaperIds: List<String> = emptyList(),
    val coverUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RecentlyViewedDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val wallpaperId: String = "",
    @ServerTimestamp val viewedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserPreferencesDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val themeMode: String = ThemeMode.SYSTEM.name,
    val amoledDark: Boolean = false,
    val imageQuality: String = ImageQualityPref.ORIGINAL.name,
    val wifiOnlyDownloads: Boolean = false,
    val recentSearches: List<String> = emptyList(),
    val homeCategoryChipsEnabled: Boolean = false,
    val uiStyle: String = UiStyleMode.MATERIAL_3.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val parsedThemeMode: ThemeMode
        get() = runCatching { ThemeMode.valueOf(themeMode) }.getOrDefault(ThemeMode.SYSTEM)

    val parsedImageQuality: ImageQualityPref
        get() = runCatching { ImageQualityPref.valueOf(imageQuality) }.getOrDefault(ImageQualityPref.ORIGINAL)

    val parsedUiStyle: UiStyleMode
        get() = runCatching { UiStyleMode.valueOf(uiStyle) }.getOrDefault(UiStyleMode.MATERIAL_3)
}

enum class SearchSort {
    RELEVANT, RECENTLY_ADDED, MOST_POPULAR
}

data class SearchFilters(
    val query: String = "",
    val resolutionTier: ResolutionTier? = null,
    val amoledOnly: Boolean = false,
    val portraitOnly: Boolean = false,
    val depthStudioOnly: Boolean = false,
    val colorFamily: ColorFamily? = null,
    val category: WallpaperCategory? = null,
    val collection: DiscoveryCollection? = null,
    val sort: SearchSort = SearchSort.RELEVANT
)
