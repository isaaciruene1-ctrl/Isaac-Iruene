package com.example.data.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

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
    DEPTH_STUDIO("Depth Studio", "Foreground subjects sculpted for lock-screen clocks"),
    STOCK("Stock", "Original OS-inspired minimalist & geometric drops"),
    SUPERHERO("Superhero", "Bold heroic emblems, armored light & stylized comics")
}

enum class DiscoveryCollection(val title: String, val subtitle: String) {
    FEATURED("Featured", "Curated hero canvases for your screen"),
    TRENDING("Trending", "Most loved across Backdrops this week"),
    EDITORS_PICKS("Editor's Picks", "Hand-selected editorial compositions"),
    NEW_ARRIVALS("New Arrivals", "Fresh high-resolution drops"),
    ULTRA_HD_4K("4K Ultra HD", "Razor-sharp 2160 × 3840+ detail"),
    AMOLED("AMOLED", "Deep obsidian contrast for OLED panels"),
    DEPTH_STUDIO("Depth Studio", "Layered subjects built for clock overlap"),
    STOCK("Stock", "Clean OS-inspired minimal & geometric canvases"),
    SUPERHERO("Superhero", "Armored emblems, neon capes & cosmic crests"),
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

enum class DockLayoutMode(val label: String) {
    AUTOMATIC("Automatic"),
    BOTTOM("Bottom"),
    SIDE("Side")
}

enum class MaterialNavStyle(val label: String) {
    PILL_TONAL("Tonal Pill"),
    COMPACT_BAR("Compact Bar"),
    FLOATING_CAPSULE("Floating Rail")
}

enum class AppLanguageOption(
    val code: String,
    val nativeLabel: String,
    val englishLabel: String,
    val isRtl: Boolean = false
) {
    ENGLISH("en", "English", "English", false),
    SPANISH("es", "Español", "Spanish", false),
    FRENCH("fr", "Français", "French", false),
    GERMAN("de", "Deutsch", "German", false),
    PORTUGUESE("pt", "Português", "Portuguese", false),
    KOREAN("ko", "한국어", "Korean", false),
    ARABIC("ar", "العربية", "Arabic (RTL)", true),
    JAPANESE("ja", "日本語", "Japanese", false),
    HINDI("hi", "हिन्दी", "Hindi", false);

    companion object {
        fun fromCode(code: String?): AppLanguageOption =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
    }
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

data class FavoriteDoc(
    val id: String = "",
    val userId: String = "",
    val wallpaperId: String = "",
    val createdAt: Long? = null
)

data class UserCollectionDoc(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val wallpaperIds: List<String> = emptyList(),
    val coverUrl: String = "",
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)

data class RecentlyViewedDoc(
    val id: String = "",
    val userId: String = "",
    val wallpaperId: String = "",
    val viewedAt: Long? = null
)

data class UserPreferencesDoc(
    val id: String = "",
    val userId: String = "",
    val themeMode: String = ThemeMode.SYSTEM.name,
    val amoledDark: Boolean = false,
    val imageQuality: String = ImageQualityPref.ORIGINAL.name,
    val wifiOnlyDownloads: Boolean = false,
    val recentSearches: List<String> = emptyList(),
    val homeCategoryChipsEnabled: Boolean = false,
    val uiStyle: String = UiStyleMode.MATERIAL_3.name,
    val hapticFeedbackEnabled: Boolean = true,
    val reducedMotionEnabled: Boolean = false,
    val glassDepthIntensity: Float = 1.0f,
    // Backdrop 1.1 Localization, Onboarding & Deep Theme Customization
    val languageCode: String = AppLanguageOption.ENGLISH.code,
    val onboardingCompleted: Boolean = false,
    val dynamicThemingEnabled: Boolean = true,
    val customAccentHex: Long = 0xFF6B57A8,
    val m3SurfaceDepth: Float = 1.0f,
    val m3CornerRadiusScale: Float = 1.0f,
    val m3ElevationScale: Float = 1.0f,
    val m3DensityScale: Float = 1.0f,
    val m3NavStyle: String = MaterialNavStyle.PILL_TONAL.name,
    val m3TypographyScale: Float = 1.0f,
    val m3MotionSpeed: Float = 1.0f,
    val glassBlurLevel: Float = 1.0f,
    val glassRefractionLevel: Float = 1.0f,
    val glassTransparencyLevel: Float = 1.0f,
    val glassLightResponse: Float = 1.0f,
    val glassSpecularHighlights: Float = 1.0f,
    val glassBorderSoftness: Float = 1.0f,
    val glassAdaptiveContrast: Float = 1.0f,
    val dockLayoutMode: String = DockLayoutMode.AUTOMATIC.name,
    val useClassicClockHandles: Boolean = false,
    val weatherCity: String = "",
    val weatherUseLocation: Boolean = false,
    val lockScreenOverlayEnabled: Boolean = false,
    val activeOverlaySetId: String = "",
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) {
    val parsedThemeMode: ThemeMode
        get() = runCatching { ThemeMode.valueOf(themeMode) }.getOrDefault(ThemeMode.SYSTEM)

    val parsedImageQuality: ImageQualityPref
        get() = runCatching { ImageQualityPref.valueOf(imageQuality) }.getOrDefault(ImageQualityPref.ORIGINAL)

    val parsedUiStyle: UiStyleMode
        get() = runCatching { UiStyleMode.valueOf(uiStyle) }.getOrDefault(UiStyleMode.MATERIAL_3)

    val parsedDockLayoutMode: DockLayoutMode
        get() = runCatching { DockLayoutMode.valueOf(dockLayoutMode) }.getOrDefault(DockLayoutMode.AUTOMATIC)

    val parsedMaterialNavStyle: MaterialNavStyle
        get() = runCatching { MaterialNavStyle.valueOf(m3NavStyle) }.getOrDefault(MaterialNavStyle.PILL_TONAL)

    val parsedLanguage: AppLanguageOption
        get() = AppLanguageOption.fromCode(languageCode)
}

@Immutable
data class LockScreenSetDoc(
    val id: String = "",
    val name: String = "",
    val wallpaperId: String = "",
    val customPhotoUri: String = "",
    val clockConfigJson: String = "",
    val zoomScale: Float = 1.0f,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f,
    val rotationDegrees: Float = 0f,
    val dimFraction: Float = 0f,
    val lookFilterName: String = "ORIGINAL",
    val isFavorite: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

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
