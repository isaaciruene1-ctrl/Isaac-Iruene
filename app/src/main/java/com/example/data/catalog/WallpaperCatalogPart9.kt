package com.example.data.catalog

import com.example.data.model.ColorFamily
import com.example.data.model.DepthSubjectHint
import com.example.data.model.ResolutionTier
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperCategory

internal data class ThemeTemplate(
    val suffix: String,
    val adjective: String,
    val motif: String,
    val description: String,
    val colorFamily: ColorFamily,
    val colors: List<Long>,
    val creator: String,
    val depth: Boolean = false,
    val depthHint: DepthSubjectHint = DepthSubjectHint.LOWER_SILHOUETTE,
    val tier: ResolutionTier = ResolutionTier.UHD_4K
)

internal fun buildThemedPartCatalog(
    partTag: String,
    baseOrderStart: Int,
    themes: List<ThemeTemplate>
): List<Wallpaper> = buildList {
    WallpaperCategory.entries.forEachIndexed { catIdx, category ->
        val (catPrefix, catNoun, catTags) = when (category) {
            WallpaperCategory.NATURE -> Triple("nat", "Horizon", listOf("nature", "landscape", "scenic"))
            WallpaperCategory.ABSTRACT -> Triple("abs", "Ribbon", listOf("abstract", "organic", "gradient"))
            WallpaperCategory.AMOLED -> Triple("amo", "Void", listOf("amoled", "oled", "obsidian"))
            WallpaperCategory.ARCHITECTURE -> Triple("arc", "Facade", listOf("architecture", "minimal", "form"))
            WallpaperCategory.MINIMALIST -> Triple("min", "Silence", listOf("minimal", "clean", "calm"))
            WallpaperCategory.SPACE -> Triple("spa", "Nebula", listOf("space", "cosmos", "orbital"))
            WallpaperCategory.AUTOMOTIVE -> Triple("aut", "Silhouette", listOf("automotive", "gt", "studio"))
            WallpaperCategory.ANIME_ARTISTIC -> Triple("ani", "Atelier", listOf("anime", "artistic", "illustration"))
            WallpaperCategory.FUTURISTIC -> Triple("fut", "Prism", listOf("futuristic", "kinetic", "synth"))
            WallpaperCategory.CINEMATIC -> Triple("cin", "Frame", listOf("cinematic", "film", "moody"))
            WallpaperCategory.DEPTH_STUDIO -> Triple("dep", "Relief", listOf("depth", "portrait", "layered"))
        }

        val specs = themes.mapIndexed { idx, theme ->
            val rotatedColorFamily = if (category == WallpaperCategory.AMOLED && idx % 3 == 0) {
                ColorFamily.MONOCHROME
            } else {
                theme.colorFamily
            }
            val palette = if (category == WallpaperCategory.AMOLED) {
                listOf(0xFF000000L, theme.colors[0], theme.colors[1])
            } else {
                theme.colors
            }
            val enableDepth = category == WallpaperCategory.DEPTH_STUDIO || theme.depth
            val tier = when {
                enableDepth -> if (idx % 4 == 0) ResolutionTier.QHD_1440 else ResolutionTier.UHD_4K
                idx % 7 == 0 -> ResolutionTier.FHD_1080
                idx % 3 == 0 -> ResolutionTier.QHD_1440
                else -> theme.tier
            }
            SeedSpec(
                suffix = theme.suffix,
                title = "${theme.adjective} ${theme.motif} $catNoun",
                description = "${theme.description} Curated for ${category.displayName} displays.",
                colorFamily = rotatedColorFamily,
                colors = palette,
                tags = catTags + listOf(theme.adjective.lowercase(), theme.motif.lowercase(), partTag),
                creator = theme.creator,
                supportsDepth = enableDepth,
                depthHint = theme.depthHint,
                tier = tier,
                aspect = "9:16"
            )
        }

        addAll(
            buildCategoryBatch(
                prefix = "${catPrefix}${partTag}",
                category = category,
                baseOrder = baseOrderStart + (catIdx * 25),
                specs = specs,
                grayscale = category == WallpaperCategory.MINIMALIST && partTag == "p11"
            )
        )
    }
}

internal fun buildCatalogPartNine(): List<Wallpaper> = buildThemedPartCatalog(
    partTag = "p9",
    baseOrderStart = 1260,
    themes = listOf(
        ThemeTemplate("01", "Alabaster", "Dune", "Softly sculpted pale highlights balanced against calm horizon shadows.", ColorFamily.WARM, listOf(0xFFE0D5C8, 0xFFFAF6F0, 0xFF2B2723), "Camille Laurent", true, DepthSubjectHint.TOP_ARCH),
        ThemeTemplate("02", "Cobalt", "Glacier", "Translucent arctic blue planes catching crisp early morning light.", ColorFamily.BLUE, listOf(0xFF4F82C4, 0xFFD4E6FA, 0xFF142136), "Soren Halvorsen", true, DepthSubjectHint.PEAK_HORIZON),
        ThemeTemplate("03", "Sage", "Moss", "Misty botanical sage layers settled cleanly in studio equilibrium.", ColorFamily.GREEN, listOf(0xFF749982, 0xFFE2F0E7, 0xFF1C2921), "Clara Lindqvist", true, DepthSubjectHint.BOTANICAL_STEM),
        ThemeTemplate("04", "Apricot", "Vesper", "Warm apricot twilight mist fading evenly into velvety sandstone.", ColorFamily.ORANGE, listOf(0xFFE0976C, 0xFFF7DEC8, 0xFF332016), "Mateo Navarro"),
        ThemeTemplate("05", "Iris", "Velvet", "Muted iris and soft lavender transitions smoothed to matte silence.", ColorFamily.PURPLE, listOf(0xFF8C74C4, 0xFFEAE2FA, 0xFF1E1730), "Mina Park", true, DepthSubjectHint.CENTER_PORTRAIT),
        ThemeTemplate("06", "Obsidian", "Silk", "Deep satin charcoal ripples catching hairline silver specular light.", ColorFamily.MONOCHROME, listOf(0xFF565B66, 0xFFE6E8EC, 0xFF14161A), "Victor Ray"),
        ThemeTemplate("07", "Cyan", "Drift", "Weightless glacier mist drifting gently across clean slate planes.", ColorFamily.COOL, listOf(0xFF5CAEC2, 0xFFD6F3FA, 0xFF15292E), "Freja Solberg", true, DepthSubjectHint.PEAK_HORIZON),
        ThemeTemplate("08", "Terracotta", "Vault", "Sun-warmed terracotta geometry bathed cleanly in Mediterranean calm.", ColorFamily.WARM, listOf(0xFFD6886D, 0xFFF7DDD4, 0xFF331D15), "Lorenzo Bellini"),
        ThemeTemplate("09", "Sapphire", "Tide", "Still sapphire depth ripples glowing peacefully along clear shores.", ColorFamily.BLUE, listOf(0xFF4773B8, 0xFFC4DAFA, 0xFF121E33), "Chloe Parker"),
        ThemeTemplate("10", "Celadon", "Grove", "Pale celadon porcelain surfaces reflecting soft diffused daylight.", ColorFamily.GREEN, listOf(0xFF66B092, 0xFFD6F2E7, 0xFF162B22), "Kenji Sato"),
        ThemeTemplate("11", "Sienna", "Ridge", "Amber canyon ridges layered softly under clear golden sky.", ColorFamily.ORANGE, listOf(0xFFDE8B52, 0xFFF7D9C2, 0xFF361F0E), "Elena Brooks", true, DepthSubjectHint.PEAK_HORIZON),
        ThemeTemplate("12", "Orchid", "Haze", "Airy pastel orchid contours blooming quietly inside diffused cloud.", ColorFamily.PURPLE, listOf(0xFF9F7BD6, 0xFFEDE2FA, 0xFF231936), "Iris Reid"),
        ThemeTemplate("13", "Graphite", "Mono", "High-contrast brushed titanium tones framed with architectural poise.", ColorFamily.MONOCHROME, listOf(0xFF7D828C, 0xFFEDF0F5, 0xFF1B1D21), "Oskar Holm", true, DepthSubjectHint.LOWER_SILHOUETTE),
        ThemeTemplate("14", "Teal", "Lagoon", "Tranquil coastal teal depth shifting softly toward misty dawn.", ColorFamily.COOL, listOf(0xFF54A8B2, 0xFFD1F0F4, 0xFF14292B), "Noah Bennett"),
        ThemeTemplate("15", "Rose", "Quartz", "Blush-tinted frosted quartz curves diffused through clean daylight.", ColorFamily.WARM, listOf(0xFFE0A2AA, 0xFFFAF0F2, 0xFF332023), "Vera Lin"),
        ThemeTemplate("16", "Periwinkle", "Mist", "Cool periwinkle morning fog wrapped gently over quiet silhouettes.", ColorFamily.BLUE, listOf(0xFF6B8AD4, 0xFFDCE6FA, 0xFF182236), "Julian Meyer", true, DepthSubjectHint.LOWER_SILHOUETTE),
        ThemeTemplate("17", "Mint", "Reed", "Fresh spring mint gradients sweeping cleanly past delicate stems.", ColorFamily.GREEN, listOf(0xFF5FC49D, 0xFFD4F7EA, 0xFF142E24), "Sora Takahashi", true, DepthSubjectHint.BOTANICAL_STEM),
        ThemeTemplate("18", "Copper", "Ember", "Warm burnished copper reflections softened to an editorial glow.", ColorFamily.ORANGE, listOf(0xFFD67F54, 0xFFF5D1C0, 0xFF331B10), "Dante Cross"),
        ThemeTemplate("19", "Plum", "Dusk", "Deep twilight plum transitions fading cleanly toward calm midnight.", ColorFamily.PURPLE, listOf(0xFF7B599E, 0xFFDCCCEB, 0xFF1D1326), "Luna Hayes"),
        ThemeTemplate("20", "Silver", "Frost", "Crisp silver rime frost textures balanced with whisper-quiet contrast.", ColorFamily.MONOCHROME, listOf(0xFFA6ACB8, 0xFFF2F4F7, 0xFF23262B), "Arthur Vance"),
        ThemeTemplate("21", "Arctic", "Breeze", "Cool pale turquoise light gliding cleanly across frosted glass.", ColorFamily.COOL, listOf(0xFF72B8C9, 0xFFE3F5FA, 0xFF192B30), "Mikkel Holm"),
        ThemeTemplate("22", "Sandstone", "Bloom", "Earthy pastel limestone shapes bathed evenly in calm afternoon sun.", ColorFamily.WARM, listOf(0xFFD9BE9C, 0xFFF7ECE0, 0xFF332B21), "Liam Cole"),
        ThemeTemplate("23", "Indigo", "Zenith", "Clean deep indigo gradients opening upward for crisp clock legibility.", ColorFamily.BLUE, listOf(0xFF4769B0, 0xFFCADBF7, 0xFF111C30), "Kuro Studio", true, DepthSubjectHint.LOWER_SILHOUETTE)
    )
)
