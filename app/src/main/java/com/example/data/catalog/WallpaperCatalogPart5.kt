package com.example.data.catalog

import com.example.R
import com.example.data.model.ColorFamily
import com.example.data.model.DepthSubjectHint
import com.example.data.model.DiscoveryCollection
import com.example.data.model.ResolutionTier
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperCategory

internal data class SeedSpec(
    val suffix: String,
    val title: String,
    val description: String,
    val colorFamily: ColorFamily,
    val colors: List<Long>,
    val tags: List<String>,
    val creator: String,
    val supportsDepth: Boolean = false,
    val depthHint: DepthSubjectHint = DepthSubjectHint.LOWER_SILHOUETTE,
    val tier: ResolutionTier = ResolutionTier.UHD_4K,
    val aspect: String = "9:16"
)

internal fun seededEntry(
    id: String,
    title: String,
    description: String,
    category: WallpaperCategory,
    extraCollections: List<DiscoveryCollection>,
    resolutionTier: ResolutionTier,
    aspectRatio: String,
    dominantColors: List<Long>,
    colorFamily: ColorFamily,
    tags: List<String>,
    creatorName: String,
    supportsDepthEffect: Boolean = false,
    depthHint: DepthSubjectHint = DepthSubjectHint.LOWER_SILHOUETTE,
    popularityScore: Int = 86,
    addedOrder: Int = 300,
    grayscale: Boolean = false
): Wallpaper {
    val (w, h) = when (resolutionTier) {
        ResolutionTier.UHD_4K -> when (aspectRatio) {
            "9:16" -> 2160 to 3840
            "2:3" -> 2160 to 3240
            "16:9" -> 3840 to 2160
            else -> 2160 to 3840
        }
        ResolutionTier.QHD_1440 -> when (aspectRatio) {
            "9:16" -> 1440 to 2560
            "2:3" -> 1440 to 2160
            "16:9" -> 2560 to 1440
            else -> 1440 to 2560
        }
        ResolutionTier.FHD_1080 -> when (aspectRatio) {
            "9:16" -> 1080 to 1920
            "2:3" -> 1080 to 1620
            "16:9" -> 1920 to 1080
            else -> 1080 to 1920
        }
    }
    val fallbackResId = when (category) {
        WallpaperCategory.AMOLED, WallpaperCategory.SPACE, WallpaperCategory.FUTURISTIC -> R.drawable.img_fallback_amoled_horizon
        WallpaperCategory.DEPTH_STUDIO, WallpaperCategory.NATURE -> R.drawable.img_fallback_depth_botanical
        WallpaperCategory.ARCHITECTURE, WallpaperCategory.MINIMALIST -> R.drawable.img_auth_hero
        else -> R.drawable.img_fallback_pastel_abstract
    }
    val baseCollections = buildList {
        addAll(extraCollections)
        if (resolutionTier == ResolutionTier.UHD_4K) add(DiscoveryCollection.ULTRA_HD_4K)
        if (supportsDepthEffect) add(DiscoveryCollection.DEPTH_STUDIO)
        add(DiscoveryCollection.COLOUR_COLLECTIONS)
    }.distinct()
    val grayParam = if (grayscale) "&grayscale" else ""
    val thumbGray = if (grayscale) "?grayscale" else ""

    return Wallpaper(
        id = id,
        title = title,
        description = description,
        category = category,
        collections = baseCollections,
        thumbnailUrl = "https://picsum.photos/seed/backdrop_$id/720/1120$thumbGray",
        fullResUrl = "https://picsum.photos/seed/backdrop_$id/$w/$h?w=$w&h=$h$grayParam",
        fallbackResId = fallbackResId,
        width = w,
        height = h,
        resolutionTier = resolutionTier,
        aspectRatio = aspectRatio,
        dominantColors = dominantColors,
        colorFamily = colorFamily,
        tags = tags,
        creatorName = creatorName,
        creatorSource = "Backdrops Curated Pack",
        isDownloadable = true,
        supportsDepthEffect = supportsDepthEffect,
        depthSubjectHint = depthHint,
        popularityScore = popularityScore,
        addedOrder = addedOrder
    )
}

internal fun buildCategoryBatch(
    prefix: String,
    category: WallpaperCategory,
    baseOrder: Int,
    specs: List<SeedSpec>,
    grayscale: Boolean = false
): List<Wallpaper> = specs.mapIndexed { idx, spec ->
    val extras = buildList {
        add(DiscoveryCollection.NEW_ARRIVALS)
        if (idx % 5 == 0) add(DiscoveryCollection.FEATURED)
        if (idx % 4 == 1) add(DiscoveryCollection.EDITORS_PICKS)
        if (idx % 3 == 0) add(DiscoveryCollection.PHOTOGRAPHY)
        when (category) {
            WallpaperCategory.NATURE -> add(DiscoveryCollection.NATURE)
            WallpaperCategory.ABSTRACT -> add(DiscoveryCollection.ABSTRACT)
            WallpaperCategory.AMOLED -> add(DiscoveryCollection.AMOLED)
            WallpaperCategory.ARCHITECTURE -> add(DiscoveryCollection.ARCHITECTURE)
            WallpaperCategory.MINIMALIST -> add(DiscoveryCollection.MINIMALIST)
            WallpaperCategory.SPACE -> add(DiscoveryCollection.SPACE)
            WallpaperCategory.AUTOMOTIVE -> add(DiscoveryCollection.AUTOMOTIVE)
            WallpaperCategory.ANIME_ARTISTIC -> add(DiscoveryCollection.ANIME)
            WallpaperCategory.CINEMATIC -> add(DiscoveryCollection.CINEMATIC)
            WallpaperCategory.FUTURISTIC -> add(DiscoveryCollection.GAMING)
            WallpaperCategory.DEPTH_STUDIO -> add(DiscoveryCollection.DEPTH_STUDIO)
            WallpaperCategory.STOCK -> add(DiscoveryCollection.STOCK)
            WallpaperCategory.SUPERHERO -> add(DiscoveryCollection.SUPERHERO)
        }
    }
    seededEntry(
        id = "${prefix}_${spec.suffix}",
        title = spec.title,
        description = spec.description,
        category = category,
        extraCollections = extras,
        resolutionTier = spec.tier,
        aspectRatio = spec.aspect,
        dominantColors = spec.colors,
        colorFamily = spec.colorFamily,
        tags = spec.tags,
        creatorName = spec.creator,
        supportsDepthEffect = spec.supportsDepth,
        depthHint = spec.depthHint,
        popularityScore = 82 + ((idx * 7) % 17),
        addedOrder = baseOrder + idx,
        grayscale = grayscale
    )
}

internal fun buildCatalogPartFive(): List<Wallpaper> = buildList {
    // 1. NATURE (+32 -> total 52)
    addAll(
        buildCategoryBatch(
            prefix = "natp5",
            category = WallpaperCategory.NATURE,
            baseOrder = 300,
            specs = listOf(
                SeedSpec("01", "Dolomite Alpenglow Ridge", "First rose light brushing jagged limestone towers above misty alpine meadows.", ColorFamily.WARM, listOf(0xFFE09F87, 0xFF5D6B7D, 0xFF1F2733), listOf("nature", "mountains", "alpenglow", "alpine"), "Lukas Meier", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("02", "Emerald Fern Canopy", "Dew-laden rainforest fronds filtering soft emerald morning mist.", ColorFamily.GREEN, listOf(0xFF3E7058, 0xFF8DB89B, 0xFF172C22), listOf("nature", "fern", "forest", "botanical"), "Clara Lindqvist", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("03", "Glacial Fjord Mirror", "Still Arctic inlet reflecting snow-dusted cliffs under pale blue skies.", ColorFamily.BLUE, listOf(0xFF7CA4C4, 0xFFD9E6F2, 0xFF213547), listOf("nature", "fjord", "water", "nordic"), "Soren Halvorsen"),
                SeedSpec("04", "Sahara Dusk Ripple", "Wind-carved desert dunes glowing amber beneath violet twilight.", ColorFamily.ORANGE, listOf(0xFFD98A54, 0xFF8C5343, 0xFF291A1D), listOf("nature", "desert", "dunes", "amber"), "Nadia Mansour"),
                SeedSpec("05", "Kyoto Moss Sanctuary", "Velvet moss stones beside a quiet bamboo brook in dawn fog.", ColorFamily.GREEN, listOf(0xFF4F7A5A, 0xFFA4BF9E, 0xFF1A291F), listOf("nature", "kyoto", "moss", "zen"), "Kenji Sato"),
                SeedSpec("06", "Patagonia Granite Spire", "Wind-swept granite massif rising cleanly above glacial teal lagoons.", ColorFamily.COOL, listOf(0xFF69899E, 0xFFC5D5E0, 0xFF1F2B34), listOf("nature", "patagonia", "granite", "peak"), "Mateo Rojas", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("07", "Lavender Provence Drift", "Rows of blooming violet lavender receding toward golden hills.", ColorFamily.PURPLE, listOf(0xFF8F76B8, 0xFFCBBBE3, 0xFF2C233D), listOf("nature", "lavender", "pastel", "meadow"), "Camille Laurent"),
                SeedSpec("08", "Pacific Basalt Tide", "Long-exposure sea foam swirling around dark volcanic sea stacks.", ColorFamily.MONOCHROME, listOf(0xFF525861, 0xFFB8C0C8, 0xFF191C21), listOf("nature", "ocean", "basalt", "coast"), "Hannah Brooks", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("09", "Autumn Birch Mist", "Silver-bark birch trunks standing tall through amber forest haze.", ColorFamily.WARM, listOf(0xFFC9925B, 0xFFE8D1B5, 0xFF34271B), listOf("nature", "birch", "autumn", "forest"), "Jonas Lind"),
                SeedSpec("10", "Icelandic Black Sand", "Contrast of ivory surf sweeping over obsidian volcanic shorelines.", ColorFamily.MONOCHROME, listOf(0xFF34383E, 0xFFCFD4DA, 0xFF121417), listOf("nature", "iceland", "coast", "minimal"), "Freja Solberg"),
                SeedSpec("11", "Redwood Sunbeams", "Cathedral shafts of sunlight piercing coastal giant redwood mist.", ColorFamily.GREEN, listOf(0xFF47694D, 0xFFBACBA8, 0xFF1A261B), listOf("nature", "redwood", "sunbeams", "forest"), "Evan Porter"),
                SeedSpec("12", "Turquoise Cenote Light", "Limestone cavern pools illuminated by crisp zenith rays.", ColorFamily.BLUE, listOf(0xFF4C94A6, 0xFF9ED4DF, 0xFF142E36), listOf("nature", "water", "lagoon", "turquoise"), "Sofia Morales"),
                SeedSpec("13", "Misty Highlands Moor", "Heather slopes fading into drifting silver Highland cloud.", ColorFamily.PURPLE, listOf(0xFF7B708E, 0xFFC4BED1, 0xFF26222E), listOf("nature", "highlands", "mist", "calm"), "Callum Fraser"),
                SeedSpec("14", "Cherry Petal Breeze", "Soft sakura boughs framing a tranquil pastel spring sky.", ColorFamily.WARM, listOf(0xFFE5B3C1, 0xFFF7E4EA, 0xFF47313A), listOf("nature", "sakura", "spring", "pastel"), "Yui Takahashi", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("15", "Siberian Cedar Snow", "Powder-white pine needles glowing gently in crisp winter sun.", ColorFamily.COOL, listOf(0xFF9CB4C9, 0xFFEAF2F8, 0xFF243342), listOf("nature", "snow", "winter", "pine"), "Dmitri Volkov"),
                SeedSpec("16", "Coral Reef Shallows", "Translucent azure shallows drifting over sculpted sandbars.", ColorFamily.BLUE, listOf(0xFF53A3BF, 0xFFBDE3EF, 0xFF163A47), listOf("nature", "ocean", "coral", "azure"), "Maya Lin", tier = ResolutionTier.QHD_1440),
                SeedSpec("17", "Tuscany Cypress Dawn", "Rolling valley mist parted by slender sentinel cypress trees.", ColorFamily.WARM, listOf(0xFFCFA57C, 0xFF6E7D5E, 0xFF282B20), listOf("nature", "cypress", "dawn", "valley"), "Lorenzo Conti"),
                SeedSpec("18", "Yosemite Granite Dome", "Moonlit granite cliffs towering above quiet pine silhouettes.", ColorFamily.COOL, listOf(0xFF576C85, 0xFFB0C1D4, 0xFF19222E), listOf("nature", "yosemite", "cliff", "night"), "Noah Bennett", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("19", "Bamboo Rain Curtain", "Vertical jade bamboo stalks glistening after summer rain.", ColorFamily.GREEN, listOf(0xFF4F8763, 0xFFA5CFAC, 0xFF172D1E), listOf("nature", "bamboo", "rain", "jade"), "Hana Sato"),
                SeedSpec("20", "Cappadocia Sunrise Mist", "Sculpted tuff valleys warmed by golden dawn haze.", ColorFamily.ORANGE, listOf(0xFFE09768, 0xFFF2CBB0, 0xFF3D2518), listOf("nature", "valley", "sunrise", "warm"), "Emre Kaya"),
                SeedSpec("21", "Cascade Moss Waterfall", "Silky waterfall veil plunging over dark mossy canyon slate.", ColorFamily.GREEN, listOf(0xFF4A6E61, 0xFFC2D9D0, 0xFF16241F), listOf("nature", "waterfall", "slate", "moss"), "Liam OConnor"),
                SeedSpec("22", "Alpine Edelweiss Ridge", "Craggy summit ridge bathed in crisp high-altitude blue light.", ColorFamily.BLUE, listOf(0xFF6B8FB5, 0xFFD0E0EF, 0xFF1D2C3D), listOf("nature", "alpine", "summit", "blue"), "Anna Gruber"),
                SeedSpec("23", "Sonoran Saguaro Dusk", "Desert silhouettes rising against tangerine and plum evening clouds.", ColorFamily.ORANGE, listOf(0xFFD97E54, 0xFF7D5C78, 0xFF241926), listOf("nature", "desert", "sunset", "silhouette"), "Elena Ruiz", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("24", "Nordic Birch Creek", "Clear snowmelt stream winding over smooth mossy river stones.", ColorFamily.COOL, listOf(0xFF64828E, 0xFFBAD0D8, 0xFF1D292E), listOf("nature", "creek", "stones", "nordic"), "Mikkel Jensen"),
                SeedSpec("25", "Lotus Pond Dawn", "Muted sage lily pads floating on mirror-still dawn water.", ColorFamily.GREEN, listOf(0xFF62846E, 0xFFBDD1C3, 0xFF1F2E24), listOf("nature", "lotus", "pond", "calm"), "Mei Chen", tier = ResolutionTier.QHD_1440),
                SeedSpec("26", "Fjord Aurora Veil", "Soft mint ribbons shimmering gently above snow-capped peaks.", ColorFamily.GREEN, listOf(0xFF4CA88B, 0xFF284D63, 0xFF0E1D24), listOf("nature", "aurora", "nordic", "night"), "Astrid Nyman"),
                SeedSpec("27", "Golden Wheat Horizon", "Warm breeze rippling across endless sunlit pasture ridges.", ColorFamily.WARM, listOf(0xFFD9B26F, 0xFFF2DFB6, 0xFF3D3018), listOf("nature", "wheat", "gold", "horizon"), "Oliver Grant"),
                SeedSpec("28", "Atacama Salt Mirror", "Cloud reflections stretched across crystalline desert salt flats.", ColorFamily.COOL, listOf(0xFF93AFC7, 0xFFE8F0F7, 0xFF283440), listOf("nature", "saltflat", "clouds", "mirror"), "Diego Silva"),
                SeedSpec("29", "Sequoia Fog Canopy", "Silent towering trunks disappearing into coastal silver fog.", ColorFamily.MONOCHROME, listOf(0xFF5D6668, 0xFFB8C2C4, 0xFF1E2224), listOf("nature", "fog", "trees", "calm"), "Rachel Kim"),
                SeedSpec("30", "Amalfi Sea Cliffs", "Sun-drenched Mediterranean limestone plunging into deep sapphire surf.", ColorFamily.BLUE, listOf(0xFF4C82B0, 0xFFD9C7AE, 0xFF182F47), listOf("nature", "sea", "cliffs", "sapphire"), "Marco Bellini"),
                SeedSpec("31", "Cloud Forest Canopy", "High-altitude moss branches draped in drifting silver mist.", ColorFamily.GREEN, listOf(0xFF527360, 0xFFAEC4B8, 0xFF18241D), listOf("nature", "cloudforest", "mist", "green"), "Lucia Vargas"),
                SeedSpec("32", "Glacier Crevasse Blue", "Translucent cobalt ice facets sculpted by sub-zero wind.", ColorFamily.BLUE, listOf(0xFF5E96C7, 0xFFCBE4F7, 0xFF18314A), listOf("nature", "glacier", "ice", "blue"), "Henrik Holm")
            )
        )
    )

    // 2. ABSTRACT (+34 -> total 52)
    addAll(
        buildCategoryBatch(
            prefix = "absp5",
            category = WallpaperCategory.ABSTRACT,
            baseOrder = 340,
            specs = listOf(
                SeedSpec("01", "Silk Lilac Ribbon", "Smooth layered lilac fabric curves lit by soft studio rim light.", ColorFamily.PURPLE, listOf(0xFF9F87D4, 0xFFE6DCFA, 0xFF2E224A), listOf("abstract", "pastel", "silk", "lilac"), "Studio Aura", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("02", "Iridescent Prism Wave", "Refractive glass waves bending soft cyan and coral light.", ColorFamily.COOL, listOf(0xFF7EB4D9, 0xFFE5A4B4, 0xFF253245), listOf("abstract", "prism", "glass", "iridescent"), "Vera Novak"),
                SeedSpec("03", "Velvet Terracotta Fold", "Sculpted matte clay ribbons in warm sienna and cream gradients.", ColorFamily.WARM, listOf(0xFFD68D6F, 0xFFF2D6C8, 0xFF3E2219), listOf("abstract", "clay", "terracotta", "matte"), "Nora Lind"),
                SeedSpec("04", "Frosted Quartz Layers", "Translucent crystalline plates stacked with luminous edge highlights.", ColorFamily.BLUE, listOf(0xFF8FB8DE, 0xFFE4EFFA, 0xFF22364A), listOf("abstract", "quartz", "frosted", "glass"), "Julian Vance"),
                SeedSpec("05", "Peach Cloud Bloom", "Diffused apricot and rose atmospheric gradient study.", ColorFamily.WARM, listOf(0xFFE8A48E, 0xFFFCE4DA, 0xFF47271F), listOf("abstract", "peach", "gradient", "soft"), "Mila Hart"),
                SeedSpec("06", "Cobalt Fluid Contour", "High-gloss liquid sapphire swirls suspended in gentle motion.", ColorFamily.BLUE, listOf(0xFF4B7BB8, 0xFFA3C7EB, 0xFF162840), listOf("abstract", "fluid", "cobalt", "liquid"), "Theo Sterling"),
                SeedSpec("07", "Sage Ripple Plaster", "Tactile architectural plaster waves in calm eucalyptus green.", ColorFamily.GREEN, listOf(0xFF7CA68F, 0xFFD5E8DC, 0xFF21362A), listOf("abstract", "plaster", "sage", "texture"), "Klara Berg"),
                SeedSpec("08", "Opalescent Shell Curve", "Pearlescent spiral curves catching lavender and seafoam reflections.", ColorFamily.PURPLE, listOf(0xFFB5A3D9, 0xFFB8E0D8, 0xFF2C273D), listOf("abstract", "opal", "pearl", "pastel"), "Iris Vega"),
                SeedSpec("09", "Amber Ambergris Glow", "Molten honey and burnt sienna light streaks over smooth slate.", ColorFamily.ORANGE, listOf(0xFFE09052, 0xFFF5CCA6, 0xFF382012), listOf("abstract", "amber", "glow", "warm"), "Dante Cross"),
                SeedSpec("10", "Graphite Satin Pleats", "Monochrome silk drapery sculpted with razor-sharp edge lighting.", ColorFamily.MONOCHROME, listOf(0xFF6B6E75, 0xFFD1D4DA, 0xFF1B1C20), listOf("abstract", "graphite", "monochrome", "satin"), "Sven Erikson"),
                SeedSpec("11", "Periwinkle Foam Study", "Soft matte spheres and pastel shadows in serene studio equilibrium.", ColorFamily.BLUE, listOf(0xFF8E9EE0, 0xFFDCE2FA, 0xFF262D4D), listOf("abstract", "periwinkle", "spheres", "pastel"), "Ava Sterling", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("12", "Rosewater Mist Arc", "Minimal floating translucent arcs in blush quartz tones.", ColorFamily.WARM, listOf(0xFFE0A6B8, 0xFFF9E6EC, 0xFF452833), listOf("abstract", "rose", "arc", "pastel"), "Chloe Monet"),
                SeedSpec("13", "Mint Porcelain Relief", "Low-relief ceramic curves bathed in clean daylight shadow.", ColorFamily.GREEN, listOf(0xFF83C2B0, 0xFFDFF4EE, 0xFF1F3B33), listOf("abstract", "mint", "ceramic", "relief"), "Owen Park"),
                SeedSpec("14", "Tangerine Acrylic Wave", "Sweeping coral-orange acrylic brushstrokes frozen in macro clarity.", ColorFamily.ORANGE, listOf(0xFFE8865A, 0xFFF7C9B0, 0xFF422114), listOf("abstract", "acrylic", "orange", "macro"), "Lia Torres"),
                SeedSpec("15", "Twilight Indigo Silk", "Deep indigo gradient rippled like twilight wind across water.", ColorFamily.PURPLE, listOf(0xFF5D5C9E, 0xFFB4B2DF, 0xFF1B1A36), listOf("abstract", "indigo", "twilight", "silk"), "Kai Ito"),
                SeedSpec("16", "Chalk & Sand Relief", "Organic sandstone ridges carved in quiet natural relief.", ColorFamily.WARM, listOf(0xFFD9C7B0, 0xFFF5EFE6, 0xFF3A3227), listOf("abstract", "sandstone", "relief", "warm"), "Paula Meyer", tier = ResolutionTier.QHD_1440),
                SeedSpec("17", "Cerulean Glass Disc", "Floating cobalt glass disc refracting soft studio haze.", ColorFamily.BLUE, listOf(0xFF6294C9, 0xFFCFE2F5, 0xFF1A2F47), listOf("abstract", "glass", "disc", "cerulean"), "Leo Vance", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("18", "Plum Velvet Mist", "Rich plum and dusty rose gradient veil with velvety grain.", ColorFamily.PURPLE, listOf(0xFF875B8A, 0xFFD4B8D6, 0xFF2C192E), listOf("abstract", "plum", "velvet", "gradient"), "Nadia K"),
                SeedSpec("19", "Glacial Opal Foil", "Metallic frost foil reflecting soft mint and lavender light.", ColorFamily.COOL, listOf(0xFF9AC9CC, 0xFFE3DDF2, 0xFF24373B), listOf("abstract", "foil", "opal", "cool"), "Zoe Park"),
                SeedSpec("20", "Copper Foil Wave", "Brushed rose-copper metal waves glowing in studio side light.", ColorFamily.ORANGE, listOf(0xFFD68D72, 0xFFF0CABD, 0xFF3D2218), listOf("abstract", "copper", "metal", "wave"), "Samir Patel"),
                SeedSpec("21", "Eucalyptus Mist Fold", "Layered matte paper folds tinted in soft eucalyptus sage.", ColorFamily.GREEN, listOf(0xFF6FA38D, 0xFFD4EBE1, 0xFF1E362C), listOf("abstract", "paper", "sage", "fold"), "Linnea Holm"),
                SeedSpec("22", "Frost Sapphire Prism", "Clean diagonal refraction across frosted sapphire blocks.", ColorFamily.BLUE, listOf(0xFF5A82B5, 0xFFC2D7F0, 0xFF192A40), listOf("abstract", "sapphire", "prism", "diagonal"), "Max Weber"),
                SeedSpec("23", "Coral Sand Dunes", "Smooth mathematical sand contours glowing in soft coral light.", ColorFamily.WARM, listOf(0xFFE09E94, 0xFFF7DED9, 0xFF422723), listOf("abstract", "coral", "contours", "smooth"), "Hana Kim"),
                SeedSpec("24", "Silver Mercury Ripple", "Liquid chrome surface rippled into quiet concentric rings.", ColorFamily.MONOCHROME, listOf(0xFF848994, 0xFFDFE2E8, 0xFF212329), listOf("abstract", "mercury", "chrome", "ripple"), "Victor Ray"),
                SeedSpec("25", "Apricot Glass Ribbon", "Warm translucent glass ribbon hovering over cream linen.", ColorFamily.ORANGE, listOf(0xFFE8A174, 0xFFFCE2CF, 0xFF422818), listOf("abstract", "apricot", "ribbon", "glass"), "Elena Brooks", tier = ResolutionTier.QHD_1440),
                SeedSpec("26", "Violet Aurora Fold", "Luminous violet and sea-blue curtain drifting softly.", ColorFamily.PURPLE, listOf(0xFF8368C4, 0xFF7BB8C9, 0xFF201A3B), listOf("abstract", "violet", "aurora", "fold"), "Soren K"),
                SeedSpec("27", "Matte Alabaster Arc", "Minimal sculpted white plaster arches casting clean geometric shadow.", ColorFamily.MONOCHROME, listOf(0xFFD8D5CE, 0xFFF5F3EE, 0xFF383632), listOf("abstract", "alabaster", "plaster", "arc"), "Maren Lind", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("28", "Seafoam Silk Haze", "Pastel seafoam and sky gradient blended with fine film texture.", ColorFamily.GREEN, listOf(0xFF7BBCA8, 0xFFD6F0E8, 0xFF1E3830), listOf("abstract", "seafoam", "pastel", "haze"), "Tara Reid"),
                SeedSpec("29", "Indigo Glass Stack", "Stacked tinted indigo planes with crisp luminous edges.", ColorFamily.BLUE, listOf(0xFF566EB8, 0xFFBCC9F0, 0xFF182240), listOf("abstract", "indigo", "planes", "glass"), "Felix Roth"),
                SeedSpec("30", "Blush Clay Pebble", "Smooth river-worn pastel clay forms balanced on ivory ground.", ColorFamily.WARM, listOf(0xFFDDAFA8, 0xFFF7E7E4, 0xFF422D2A), listOf("abstract", "clay", "pebble", "pastel"), "Nina Frost"),
                SeedSpec("31", "Glacier Linen Sweep", "Soft ice-blue gradient draped smoothly over fine canvas grain.", ColorFamily.COOL, listOf(0xFF8CB4CF, 0xFFE3EFF7, 0xFF223340), listOf("abstract", "glacier", "linen", "cool"), "Caleb Stone"),
                SeedSpec("32", "Ochre Velvet Horizon", "Warm mustard and burnt honey abstract horizon gradients.", ColorFamily.ORANGE, listOf(0xFFD99E57, 0xFFF5DCBA, 0xFF3D2A14), listOf("abstract", "ochre", "velvet", "warm"), "Rhea Das"),
                SeedSpec("33", "Orchid Haze Prism", "Soft orchid pink refracted through frosted acrylic curves.", ColorFamily.PURPLE, listOf(0xFFC48AC4, 0xFFF0DCF0, 0xFF382238), listOf("abstract", "orchid", "acrylic", "pastel"), "Jude Ellis"),
                SeedSpec("34", "Slate Wave Sculpture", "Carved charcoal and silver layered topographic waves.", ColorFamily.MONOCHROME, listOf(0xFF60656E, 0xFFC5C9D1, 0xFF181A1E), listOf("abstract", "slate", "wave", "monochrome"), "Oskar Holm")
            )
        )
    )

    // 3. AMOLED (+33 -> total 52)
    addAll(
        buildCategoryBatch(
            prefix = "amop5",
            category = WallpaperCategory.AMOLED,
            baseOrder = 380,
            specs = listOf(
                SeedSpec("01", "Obsidian Cobalt Halo", "True-black center pierced by a razor-sharp glowing cobalt ring.", ColorFamily.BLUE, listOf(0xFF000000, 0xFF4C8BF5, 0xFF112240), listOf("amoled", "oled", "cobalt", "halo"), "Nocturne Lab", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("02", "Pitch Black Ember", "Deep obsidian darkness lit by a delicate warm copper horizon.", ColorFamily.ORANGE, listOf(0xFF000000, 0xFFE87E4D, 0xFF29140B), listOf("amoled", "ember", "black", "orange"), "Kuro Studio"),
                SeedSpec("03", "Velvet Black Violet", "True #000000 void framed by translucent violet light ribbons.", ColorFamily.PURPLE, listOf(0xFF000000, 0xFFA27BFF, 0xFF1E1338), listOf("amoled", "violet", "ribbon", "dark"), "Nyx Visual"),
                SeedSpec("04", "Midnight Emerald Edge", "Pitch-black matte slate rimmed with crisp bioluminescent emerald.", ColorFamily.GREEN, listOf(0xFF000000, 0xFF47C78E, 0xFF0E291D), listOf("amoled", "emerald", "oled", "edge"), "Aria Noir"),
                SeedSpec("05", "Monolith Black Ridge", "Sharp volcanic shadow silhouette cutting across dark starlit mist.", ColorFamily.MONOCHROME, listOf(0xFF000000, 0xFF5A5E66, 0xFF141518), listOf("amoled", "monolith", "ridge", "black"), "Taro Yamada", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("06", "Eclipse Rim Obsidian", "Minimal solar corona glowing thinly around a pitch-black disc.", ColorFamily.WARM, listOf(0xFF000000, 0xFFF0B27A, 0xFF2E1E12), listOf("amoled", "eclipse", "corona", "dark"), "Helios Art"),
                SeedSpec("07", "Laser Cyan Filament", "Delicate neon cyan glass threads suspended in total darkness.", ColorFamily.COOL, listOf(0xFF000000, 0xFF5CD6E5, 0xFF0F2B30), listOf("amoled", "cyan", "filament", "oled"), "Kira Sol"),
                SeedSpec("08", "Carbon Prism Facet", "Stealth black geometric crystal catching subtle silver highlights.", ColorFamily.MONOCHROME, listOf(0xFF000000, 0xFF7D828C, 0xFF181A1E), listOf("amoled", "carbon", "facet", "stealth"), "Dorian Gray"),
                SeedSpec("09", "Crimson Pulse Line", "Minimal crimson waveform glowing along an obsidian floor.", ColorFamily.WARM, listOf(0xFF000000, 0xFFE05A6D, 0xFF2E1015), listOf("amoled", "crimson", "pulse", "minimal"), "Riku Sato"),
                SeedSpec("10", "Indigo Void Petal", "Translucent dark-indigo blossom floating in pure OLED darkness.", ColorFamily.PURPLE, listOf(0xFF000000, 0xFF6B6CE0, 0xFF151633), listOf("amoled", "indigo", "petal", "oled"), "Mina Park", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("11", "Obsidian Gold Vein", "Hairline molten gold fracture sweeping across matte basalt.", ColorFamily.ORANGE, listOf(0xFF000000, 0xFFE5B45B, 0xFF2B200F), listOf("amoled", "gold", "vein", "obsidian"), "Sora Lin"),
                SeedSpec("12", "Arctic Night Horizon", "Deep pitch sky meeting a faint glacial mint aurora arc.", ColorFamily.GREEN, listOf(0xFF000000, 0xFF5CC9A7, 0xFF10261F), listOf("amoled", "aurora", "mint", "night"), "Bjorn V"),
                SeedSpec("13", "Shadow Slate Sphere", "Floating matte obsidian sphere edged with soft silver mist.", ColorFamily.MONOCHROME, listOf(0xFF000000, 0xFF8C929E, 0xFF181A1F), listOf("amoled", "sphere", "silver", "dark"), "Nero Minimal", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("14", "Sapphire Silk Thread", "Luminous blue fiber curves floating cleanly against true black.", ColorFamily.BLUE, listOf(0xFF000000, 0xFF5C9DF0, 0xFF102038), listOf("amoled", "sapphire", "silk", "blue"), "Evan Reed"),
                SeedSpec("15", "Nocturne Lavender Mist", "Subtle smoky lavender plume dissolving into endless pitch black.", ColorFamily.PURPLE, listOf(0xFF000000, 0xFFB08EE8, 0xFF211830), listOf("amoled", "lavender", "smoke", "oled"), "Lila Vance"),
                SeedSpec("16", "Tangerine Ring Eclipse", "Thin fiery tangerine neon ring centered in deep obsidian void.", ColorFamily.ORANGE, listOf(0xFF000000, 0xFFF28B50, 0xFF2E170B), listOf("amoled", "ring", "tangerine", "oled"), "Zane Cole", tier = ResolutionTier.QHD_1440),
                SeedSpec("17", "Matte Graphite Wave", "Low-contrast charcoal ripple engineered for zero battery drain.", ColorFamily.MONOCHROME, listOf(0xFF000000, 0xFF555961, 0xFF141517), listOf("amoled", "graphite", "wave", "battery"), "Onyx Craft"),
                SeedSpec("18", "Deep Sea Biolume", "Faint glowing teal jellyfish silhouette inside pitch abyssal black.", ColorFamily.COOL, listOf(0xFF000000, 0xFF45BFBA, 0xFF0D2625), listOf("amoled", "teal", "abyss", "glow"), "Nadia Sun", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("19", "Amethyst Shard Glow", "Single dark crystal catching delicate amethyst refraction.", ColorFamily.PURPLE, listOf(0xFF000000, 0xFFA46EE0, 0xFF1F1230), listOf("amoled", "amethyst", "crystal", "dark"), "Gideon K"),
                SeedSpec("20", "Void Peach Arc", "Warm minimalist peach crescent hovering in true-black space.", ColorFamily.WARM, listOf(0xFF000000, 0xFFF0A286, 0xFF2E1B14), listOf("amoled", "peach", "arc", "minimal"), "Cleo Hart"),
                SeedSpec("21", "Jade Laser Horizon", "Crisp emerald-jade beam slicing gently across matte night.", ColorFamily.GREEN, listOf(0xFF000000, 0xFF53C288, 0xFF0F261A), listOf("amoled", "jade", "beam", "oled"), "Kai Tanaka"),
                SeedSpec("22", "Steel Night Crescent", "Brushed dark titanium crescent rimmed with icy blue light.", ColorFamily.COOL, listOf(0xFF000000, 0xFF7CA4CC, 0xFF16202B), listOf("amoled", "steel", "crescent", "cool"), "Liam Vance"),
                SeedSpec("23", "Coral Nebula Spark", "Delicate coral dust wisps drifting inside pitch cosmos.", ColorFamily.WARM, listOf(0xFF000000, 0xFFE6837A, 0xFF2B1513), listOf("amoled", "coral", "cosmos", "dark"), "Iris Novak"),
                SeedSpec("24", "Onyx Feather Silhouette", "Minimal botanical plume rimmed with hairline silver glow.", ColorFamily.MONOCHROME, listOf(0xFF000000, 0xFFAAB0BA, 0xFF17181C), listOf("amoled", "onyx", "silver", "silhouette"), "Mila Ray", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("25", "Cobalt Ribbon Loop", "Continuous folded cobalt ribbon glowing over OLED black.", ColorFamily.BLUE, listOf(0xFF000000, 0xFF528AE6, 0xFF101D36), listOf("amoled", "ribbon", "cobalt", "loop"), "Theo K", tier = ResolutionTier.QHD_1440),
                SeedSpec("26", "Amber Neon Monolith", "Minimal vertical amber light slit glowing in total darkness.", ColorFamily.ORANGE, listOf(0xFF000000, 0xFFEB984E, 0xFF2B1A0B), listOf("amoled", "monolith", "amber", "slit"), "Soren Noir"),
                SeedSpec("27", "Plum Shadow Fold", "Deep velvet plum drapery merging seamlessly into pitch black.", ColorFamily.PURPLE, listOf(0xFF000000, 0xFF8C5A9E, 0xFF1B1021), listOf("amoled", "plum", "fold", "velvet"), "Elena S"),
                SeedSpec("28", "Mint Glass Shard", "Floating translucent mint prism edge in true-black void.", ColorFamily.GREEN, listOf(0xFF000000, 0xFF69D1AC, 0xFF122921), listOf("amoled", "mint", "glass", "shard"), "Noah Chen"),
                SeedSpec("29", "Charcoal Lunar Crater", "Moody high-contrast obsidian lunar rim in dark orbit.", ColorFamily.MONOCHROME, listOf(0xFF000000, 0xFF737882, 0xFF16171A), listOf("amoled", "lunar", "charcoal", "orbit"), "Vera Moon", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("30", "Cyan Eclipse Ring", "Glowing electric ice ring floating in deep pitch night.", ColorFamily.COOL, listOf(0xFF000000, 0xFF63D0E0, 0xFF11292E), listOf("amoled", "cyan", "eclipse", "oled"), "Max Reid"),
                SeedSpec("31", "Rose Gold Filament", "Fine rose-gold arc tracing softly across an obsidian backdrop.", ColorFamily.WARM, listOf(0xFF000000, 0xFFE3A69E, 0xFF2B1C1A), listOf("amoled", "rosegold", "arc", "dark"), "Nora Ellis"),
                SeedSpec("32", "Midnight Ultramarine", "Deep ultramarine ocean abyss fading directly to #000000.", ColorFamily.BLUE, listOf(0xFF000000, 0xFF4B70C9, 0xFF0F1830), listOf("amoled", "ultramarine", "abyss", "blue"), "Luca Meyer"),
                SeedSpec("33", "Obsidian Prism Edge", "Knife-sharp silver-violet prism line bisecting true black.", ColorFamily.PURPLE, listOf(0xFF000000, 0xFF9C84E0, 0xFF1A152B), listOf("amoled", "prism", "violet", "minimal"), "Kuro Lab")
            ),
            grayscale = true
        )
    )

    // 4. FUTURISTIC (+39 -> total 52)
    addAll(
        buildCategoryBatch(
            prefix = "futp5",
            category = WallpaperCategory.FUTURISTIC,
            baseOrder = 420,
            specs = listOf(
                SeedSpec("01", "Neo-Tokyo Rain Spire", "Towering holographic megastructures glowing softly through midnight rain.", ColorFamily.BLUE, listOf(0xFF4B84C9, 0xFF9CD4EB, 0xFF131D2E), listOf("futuristic", "cyber", "tokyo", "neon"), "Kenji Mori", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("02", "Orbital Ring Habitat", "Sleek titanium space ring hovering above sunlit cloud layers.", ColorFamily.COOL, listOf(0xFF76A4C4, 0xFFD9EDF7, 0xFF1B2B38), listOf("futuristic", "orbital", "scifi", "space"), "Vanguard 3D"),
                SeedSpec("03", "Cybernetic Glass Atrium", "Glowing cyan circuit ribs inside a futuristic botanical dome.", ColorFamily.GREEN, listOf(0xFF47B39C, 0xFFAFE8DB, 0xFF122B26), listOf("futuristic", "cybernetic", "atrium", "teal"), "Mina Cho"),
                SeedSpec("04", "Synthwave Violet Grid", "Luminous magenta-violet wireframe horizon under digital twilight.", ColorFamily.PURPLE, listOf(0xFF9E66D4, 0xFFE8B0EB, 0xFF251538), listOf("futuristic", "synthwave", "violet", "grid"), "Axel Nova"),
                SeedSpec("05", "Solar Sail Horizon", "Translucent golden spacecraft sails catching starlight in high orbit.", ColorFamily.ORANGE, listOf(0xFFE39E5B, 0xFFF7D5B0, 0xFF332011), listOf("futuristic", "solarsail", "gold", "scifi"), "Elena Vance"),
                SeedSpec("06", "Chrome Android Profile", "Sculpted liquid-chrome helmet illuminated by soft cyan studio light.", ColorFamily.MONOCHROME, listOf(0xFF788496, 0xFFD9E1EB, 0xFF1B1F26), listOf("futuristic", "chrome", "cyber", "portrait"), "Taro K", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("07", "Quantum Light Tunnel", "Receding geometric portal rings glowing with ice-blue lasers.", ColorFamily.BLUE, listOf(0xFF5499DE, 0xFFBBE0FA, 0xFF13253B), listOf("futuristic", "quantum", "portal", "tunnel"), "Nexus Visuals"),
                SeedSpec("08", "Martian Glass Colony", "Geodesic biosphere domes glowing warmly on red Martian plains.", ColorFamily.ORANGE, listOf(0xFFD67856, 0xFFF2BEAA, 0xFF361B12), listOf("futuristic", "mars", "colony", "dome"), "Leo Park"),
                SeedSpec("09", "Neon Rain Alley", "Wet asphalt reflecting pastel pink and electric blue kanji signage.", ColorFamily.PURPLE, listOf(0xFF8C6EC9, 0xFF69BCE0, 0xFF1D182E), listOf("futuristic", "neon", "rain", "cyberpunk"), "Hana Ito"),
                SeedSpec("10", "Sub-Zero Cryo Vault", "Minimal white-titanium corridor lit by crisp aquamarine strips.", ColorFamily.COOL, listOf(0xFF7EC2CE, 0xFFE6F4F7, 0xFF1C2D30), listOf("futuristic", "cryo", "corridor", "clean"), "Soren Frost"),
                SeedSpec("11", "Bioluminescent Synth", "Glowing cyber-botanical tendrils pulsing softly in dark labs.", ColorFamily.GREEN, listOf(0xFF4BC495, 0xFFA6EDCF, 0xFF112920), listOf("futuristic", "bioluminescent", "synth", "green"), "Aria Lin", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("12", "Plasma Core Horizon", "Suspended violet plasma sphere radiating geometric shockwaves.", ColorFamily.PURPLE, listOf(0xFF8968D4, 0xFFD3C4F7, 0xFF1F1636), listOf("futuristic", "plasma", "sphere", "scifi"), "Zane Gray"),
                SeedSpec("13", "Hyperloop Maglev Blur", "Sleek silver transit pod streaking through a glowing glass tube.", ColorFamily.COOL, listOf(0xFF7496BA, 0xFFD2E2F2, 0xFF1B2430), listOf("futuristic", "hyperloop", "speed", "silver"), "Max Meyer"),
                SeedSpec("14", "Hologram Lotus Core", "Translucent cyan wireframe blossom floating in dark studio space.", ColorFamily.BLUE, listOf(0xFF4EA8D9, 0xFFB5E3FA, 0xFF122836), listOf("futuristic", "hologram", "lotus", "cyan"), "Yui Chen", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("15", "Titanium Canyon Base", "Monumental brutalist sci-fi bunker nestled in misty basalt cliffs.", ColorFamily.MONOCHROME, listOf(0xFF636A73, 0xFFC2C8D1, 0xFF191B1F), listOf("futuristic", "bunker", "brutalist", "scifi"), "Dante Holm"),
                SeedSpec("16", "Amber Holo Terminal", "Warm amber holographic wireframe grid shimmering in dark air.", ColorFamily.ORANGE, listOf(0xFFE0924E, 0xFFF7CFA8, 0xFF331F0E), listOf("futuristic", "amber", "hologram", "grid"), "Kai Park", tier = ResolutionTier.QHD_1440),
                SeedSpec("17", "Skyline Drone Trails", "Aerial pastel light ribbons weaving between clean glass towers.", ColorFamily.WARM, listOf(0xFFE098A8, 0xFF80A5D4, 0xFF222233), listOf("futuristic", "drone", "skyline", "light"), "Chloe V"),
                SeedSpec("18", "Lunar Dome Observatory", "Sleek curved titanium observatory gazing at Earthrise.", ColorFamily.BLUE, listOf(0xFF5E8AC4, 0xFFC7DCF5, 0xFF182438), listOf("futuristic", "lunar", "observatory", "earthrise"), "Noah Reed"),
                SeedSpec("19", "Laser Grid Monolith", "Tall dark obelisk traced by pulsing electric lilac circuits.", ColorFamily.PURPLE, listOf(0xFF8C6CE0, 0xFFD6C7FA, 0xFF1E1636), listOf("futuristic", "monolith", "laser", "lilac"), "Vera S", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("20", "Aero Carbon Cockpit", "Clean minimalist sci-fi flight deck glowing with teal telemetry.", ColorFamily.COOL, listOf(0xFF56A8B8, 0xFFB8E4ED, 0xFF15292E), listOf("futuristic", "cockpit", "aero", "teal"), "Liam Fox"),
                SeedSpec("21", "Digital Glacier Shard", "Low-poly translucent ice prism hovering over chrome water.", ColorFamily.BLUE, listOf(0xFF6BA6D9, 0xFFD4EAF7, 0xFF1A2B3B), listOf("futuristic", "lowpoly", "glacier", "chrome"), "Felix R"),
                SeedSpec("22", "Solar Punk Terrace", "Curved white vertical gardens laced with clean solar glass.", ColorFamily.GREEN, listOf(0xFF5FA67F, 0xFFCFE8D9, 0xFF1B3025), listOf("futuristic", "solarpunk", "gardens", "green"), "Maya Ruiz"),
                SeedSpec("23", "Coral Cyber Wave", "Glowing neon coral vector ridges rolling into twilight fog.", ColorFamily.WARM, listOf(0xFFE6837A, 0xFFFAD1CD, 0xFF3B1E1C), listOf("futuristic", "coral", "vector", "neon"), "Jude K"),
                SeedSpec("24", "Deep Matrix Lattice", "Infinite emerald optical fiber grid disappearing into darkness.", ColorFamily.GREEN, listOf(0xFF47BA82, 0xFFAEEBD0, 0xFF10291D), listOf("futuristic", "lattice", "fiber", "matrix"), "Omar Ali"),
                SeedSpec("25", "Chrome Orbit Ring", "Polished liquid metal torus drifting in luminous pastel sky.", ColorFamily.MONOCHROME, listOf(0xFF8C929E, 0xFFE6E9F0, 0xFF212329), listOf("futuristic", "torus", "chrome", "sky"), "Nora Sol", tier = ResolutionTier.QHD_1440),
                SeedSpec("26", "Vapor Horizon Gate", "Minimalist neon arch framing a soft rose-purple digital sea.", ColorFamily.PURPLE, listOf(0xFFB57AD9, 0xFFF0CBF7, 0xFF2C1B38), listOf("futuristic", "vapor", "gate", "arch"), "Sven K", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("27", "Photon Bridge Dusk", "Suspended glass skybridge lit by warm amber plasma strips.", ColorFamily.ORANGE, listOf(0xFFE0925A, 0xFFF5D1B5, 0xFF331F12), listOf("futuristic", "bridge", "photon", "amber"), "Iris Reid"),
                SeedSpec("28", "Ion Storm Skyline", "Sleek dark spires silhouetted against cobalt atmospheric lightning.", ColorFamily.BLUE, listOf(0xFF4E7AC7, 0xFFB0C9F2, 0xFF152036), listOf("futuristic", "ion", "skyline", "lightning"), "Caleb M"),
                SeedSpec("29", "Ceramic Mech Silhouette", "Minimal matte porcelain sci-fi sculpture in soft studio haze.", ColorFamily.MONOCHROME, listOf(0xFFC8CDD4, 0xFFF0F2F5, 0xFF2B2E33), listOf("futuristic", "ceramic", "mech", "minimal"), "Riku Art", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("30", "Aurora Glass Spire", "Translucent crystalline tower refracting mint northern light.", ColorFamily.COOL, listOf(0xFF68BFA8, 0xFFD1F2E9, 0xFF18302A), listOf("futuristic", "spire", "crystal", "aurora"), "Astrid H"),
                SeedSpec("31", "Hyperion Sun Reactor", "Golden concentric rings suspended inside a minimalist white hall.", ColorFamily.WARM, listOf(0xFFDDA866, 0xFFF7E2C6, 0xFF382816), listOf("futuristic", "reactor", "gold", "rings"), "Lorenzo M"),
                SeedSpec("32", "Cyber Kyoto Lantern", "Holographic rain-slicked street bathed in teal and coral neon.", ColorFamily.COOL, listOf(0xFF56AEBF, 0xFFE38C95, 0xFF1A252E), listOf("futuristic", "kyoto", "neon", "rain"), "Kenji Mori"),
                SeedSpec("33", "Void Warp Corridor", "Symmetrical brushed obsidian tunnel receding into indigo glow.", ColorFamily.PURPLE, listOf(0xFF6663BF, 0xFFC0BFF0, 0xFF161530), listOf("futuristic", "corridor", "warp", "indigo"), "Victor Ray"),
                SeedSpec("34", "Zero-G Silk Pod", "Smooth white aerodynamic capsule drifting past sunlit clouds.", ColorFamily.MONOCHROME, listOf(0xFFDCE0E6, 0xFFF7F8FA, 0xFF2F3338), listOf("futuristic", "capsule", "aerodynamic", "clouds"), "Eva Park"),
                SeedSpec("35", "Prism Megacity Dusk", "Refractive glass towers catching magenta sunset clouds.", ColorFamily.PURPLE, listOf(0xFFA870C9, 0xFFEBC8F7, 0xFF2B1B36), listOf("futuristic", "megacity", "glass", "sunset"), "Theo N"),
                SeedSpec("36", "Cryo Emerald Core", "Glowing emerald geometric core floating inside frosted glass.", ColorFamily.GREEN, listOf(0xFF50B889, 0xFFB8EBCD, 0xFF142E22), listOf("futuristic", "core", "emerald", "frosted"), "Mina Lin"),
                SeedSpec("37", "Plasma Sunset Dunes", "Alien turquoise horizon over smooth glowing magenta sands.", ColorFamily.COOL, listOf(0xFF5AB8C4, 0xFFD987BA, 0xFF1B2B33), listOf("futuristic", "alien", "dunes", "plasma"), "Samir V"),
                SeedSpec("38", "Starport Glass Canopy", "Sweeping curved spaceport ribs opening onto twilight orbit.", ColorFamily.BLUE, listOf(0xFF5C86BF, 0xFFC2D8F2, 0xFF172236), listOf("futuristic", "starport", "canopy", "orbit"), "Dante C"),
                SeedSpec("39", "Holo Ribbon Monolith", "Floating pastel holographic ribbon circling a dark basalt slab.", ColorFamily.PURPLE, listOf(0xFF9E7EE0, 0xFF8CD1DE, 0xFF1E1A2E), listOf("futuristic", "monolith", "hologram", "pastel"), "Studio K")
            )
        )
    )

    // 5. ARCHITECTURE (+5 -> total 54)
    addAll(
        buildCategoryBatch(
            prefix = "arcp5",
            category = WallpaperCategory.ARCHITECTURE,
            baseOrder = 465,
            specs = listOf(
                SeedSpec("01", "Oslo Marble Opera", "Sloping white Carrara marble roof plunging cleanly into fjord waters.", ColorFamily.MONOCHROME, listOf(0xFFDEDFE3, 0xFF7B8591, 0xFF22262B), listOf("architecture", "marble", "oslo", "minimal"), "Henrik Solberg", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("02", "Valencia White Ribs", "Soaring futuristic white concrete ribs curving over clear blue pools.", ColorFamily.BLUE, listOf(0xFFECEEF2, 0xFF5E93C9, 0xFF1C3047), listOf("architecture", "calatrava", "white", "ribs"), "Mateo Navarro", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("03", "Kyoto Cedar Lattice", "Rhythmic vertical cedar slats casting razor-clean morning shadow.", ColorFamily.WARM, listOf(0xFFC29166, 0xFFEAD4BE, 0xFF332316), listOf("architecture", "kyoto", "cedar", "lattice"), "Kenji Sato"),
                SeedSpec("04", "Desert Concrete Villa", "Monolithic sand-colored concrete walls framing clear desert skies.", ColorFamily.ORANGE, listOf(0xFFD9A87E, 0xFF7A9CC2, 0xFF2E2218), listOf("architecture", "concrete", "desert", "villa"), "Sofia Vega"),
                SeedSpec("05", "Spiral Concrete Stair", "Monumental architectural spiral staircase photographed straight down.", ColorFamily.MONOCHROME, listOf(0xFFB5B8BF, 0xFFEFEFF2, 0xFF25272B), listOf("architecture", "spiral", "staircase", "geometry"), "Joel Filipe")
            )
        )
    )

    // 6. MINIMALIST (+17 -> total 52)
    addAll(
        buildCategoryBatch(
            prefix = "minp5",
            category = WallpaperCategory.MINIMALIST,
            baseOrder = 475,
            specs = listOf(
                SeedSpec("01", "Paper Horizon Fold", "Single crisp diagonal paper shadow dividing warm cream light.", ColorFamily.WARM, listOf(0xFFEFE8DC, 0xFFCBBBA4, 0xFF383128), listOf("minimalist", "paper", "shadow", "cream"), "Studio Maren"),
                SeedSpec("02", "Single Pebble Calm", "Smooth slate stone resting on quiet pale sand ripples.", ColorFamily.MONOCHROME, listOf(0xFFC8C5BE, 0xFF7A7873, 0xFF292826), listOf("minimalist", "pebble", "zen", "calm"), "Kenji S"),
                SeedSpec("03", "Skyline Wire Thread", "Solitary architectural mast silhouetted in vast pale blue negative space.", ColorFamily.BLUE, listOf(0xFF9EBCD9, 0xFFE6F0FA, 0xFF263647), listOf("minimalist", "sky", "negativespace", "blue"), "Clara Lind", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("04", "Sage Ceramic Vessel", "Matte sage stoneware vase in soft diffused window shadow.", ColorFamily.GREEN, listOf(0xFF92B39E, 0xFFE2ECE6, 0xFF24332A), listOf("minimalist", "ceramic", "sage", "stilllife"), "Nora Holm", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("05", "Blush Plaster Niche", "Quiet arched wall recess painted in warm pastel rose plaster.", ColorFamily.WARM, listOf(0xFFE3B9B3, 0xFFF7EAE8, 0xFF422E2B), listOf("minimalist", "arch", "blush", "plaster"), "Chloe Monet"),
                SeedSpec("06", "Lilac Mist Gradient", "Pure distraction-free pastel lilac to alabaster sky gradient.", ColorFamily.PURPLE, listOf(0xFFC2B2E6, 0xFFF2EEFA, 0xFF342C47), listOf("minimalist", "lilac", "gradient", "pastel"), "Ava Hart"),
                SeedSpec("07", "Sunlit Doorway Line", "One crisp beam of afternoon sunlight across matte stone tile.", ColorFamily.ORANGE, listOf(0xFFD9A679, 0xFFF2DFC9, 0xFF382719), listOf("minimalist", "sunlight", "beam", "stone"), "Jonas Reid"),
                SeedSpec("08", "Snowy Fence Post", "Lone timber marker standing in boundless untouched white snow.", ColorFamily.COOL, listOf(0xFFB5C4D1, 0xFFF2F6FA, 0xFF2B353E), listOf("minimalist", "snow", "winter", "calm"), "Soren V"),
                SeedSpec("09", "Obsidian Dot Balance", "Tiny matte black sphere resting cleanly on pale alabaster linen.", ColorFamily.MONOCHROME, listOf(0xFFDCDAD5, 0xFF292A2E, 0xFF75767A), listOf("minimalist", "sphere", "linen", "balance"), "Oskar N"),
                SeedSpec("10", "Seafoam Cloud Line", "Whisper-thin cloud streak across calm pastel seafoam skies.", ColorFamily.GREEN, listOf(0xFF94C4B6, 0xFFE3F5EF, 0xFF223831), listOf("minimalist", "seafoam", "cloud", "calm"), "Mila Lin"),
                SeedSpec("11", "Travertine Cube Shadow", "Clean geometric limestone cube casting crisp morning shadow.", ColorFamily.WARM, listOf(0xFFE0D2BE, 0xFFF7F1E8, 0xFF3B3328), listOf("minimalist", "travertine", "cube", "geometry"), "Avi Werde"),
                SeedSpec("12", "Periwinkle Dune Crest", "Clean curved sand ridge fading into pale twilight periwinkle.", ColorFamily.BLUE, listOf(0xFF8C9CD4, 0xFFDCE1F5, 0xFF262D47), listOf("minimalist", "dune", "periwinkle", "curve"), "Tara Brooks", tier = ResolutionTier.QHD_1440),
                SeedSpec("13", "Single Eucalyptus Twig", "Slender silver-dollar leaf stem on clean matte ivory backdrop.", ColorFamily.GREEN, listOf(0xFF8AA896, 0xFFF5F3ED, 0xFF29382F), listOf("minimalist", "botanical", "eucalyptus", "ivory"), "Linnea B", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("14", "Charcoal Hairline Grid", "Ultra-quiet architectural grid printed softly on dark slate paper.", ColorFamily.MONOCHROME, listOf(0xFF3B3E45, 0xFF6B707A, 0xFF141518), listOf("minimalist", "grid", "slate", "dark"), "Dorian K"),
                SeedSpec("15", "Apricot Horizon Glow", "Soft diffused apricot sunrise fading smoothly into pale mist.", ColorFamily.ORANGE, listOf(0xFFE8B08E, 0xFFFAEADF, 0xFF422C1F), listOf("minimalist", "apricot", "sunrise", "mist"), "Emma Frost"),
                SeedSpec("16", "Glass Water Drop", "Single translucent droplet resting on frosted matte glass.", ColorFamily.COOL, listOf(0xFFA8C4D4, 0xFFEDF5FA, 0xFF29363D), listOf("minimalist", "water", "glass", "macro"), "Noah Kim"),
                SeedSpec("17", "Slate Crescent Wall", "Sculpted matte crescent niche bathed in gentle indirect light.", ColorFamily.MONOCHROME, listOf(0xFF9EA2AB, 0xFFE6E8EC, 0xFF27292E), listOf("minimalist", "crescent", "sculpture", "matte"), "Vera M")
            )
        )
    )
}
