package com.example.data.catalog

import com.example.data.model.ColorFamily
import com.example.data.model.DepthSubjectHint
import com.example.data.model.Wallpaper

internal fun buildCatalogPartEleven(): List<Wallpaper> = buildThemedPartCatalog(
    partTag = "p11",
    baseOrderStart = 1860,
    themes = listOf(
        ThemeTemplate("01", "Porcelain", "Arch", "Clean cream travertine arches opening into serene dawn light.", ColorFamily.WARM, listOf(0xFFE3D7C8, 0xFFFAF7F2, 0xFF2E2A25), "Camille Laurent", true, DepthSubjectHint.TOP_ARCH),
        ThemeTemplate("02", "Nordic", "Fjord", "Deep slate-blue coastal waters calmed by drifting silver mist.", ColorFamily.BLUE, listOf(0xFF567AA8, 0xFFD4E0F0, 0xFF151F2E), "Soren Halvorsen", true, DepthSubjectHint.PEAK_HORIZON),
        ThemeTemplate("03", "Eucalyptus", "Mist", "Cool botanical silver-green foliage floating quietly against fog.", ColorFamily.GREEN, listOf(0xFF6B9480, 0xFFDCEBE3, 0xFF192620), "Clara Lindqvist", true, DepthSubjectHint.BOTANICAL_STEM),
        ThemeTemplate("04", "Honey", "Sands", "Silky golden-apricot dunes curving gracefully toward clear skies.", ColorFamily.ORANGE, listOf(0xFFE3A45C, 0xFFFAE6CC, 0xFF362410), "Tariq Mansour"),
        ThemeTemplate("05", "Lavender", "Cloud", "Whisper-soft pastel lavender waves diffused smoothly from edge to edge.", ColorFamily.PURPLE, listOf(0xFF9C84CC, 0xFFEDE6FA, 0xFF221C30), "Mina Park"),
        ThemeTemplate("06", "Titanium", "Edge", "Brushed silver-charcoal highlights trimmed cleanly for OLED screens.", ColorFamily.MONOCHROME, listOf(0xFF757A85, 0xFFEBEDF0, 0xFF191B1F), "Victor Ray", true, DepthSubjectHint.CENTER_PORTRAIT),
        ThemeTemplate("07", "Cyanotype", "Leaf", "Botanical Prussian-cyan silhouettes floating gently on frosted light.", ColorFamily.COOL, listOf(0xFF5292AB, 0xFFD4ECF5, 0xFF14252B), "Kenji Mori", true, DepthSubjectHint.BOTANICAL_STEM),
        ThemeTemplate("08", "Rust", "Canyon", "Warm sandstone folds sculpted by crisp late afternoon rays.", ColorFamily.WARM, listOf(0xFFD47E63, 0xFFF5D8CE, 0xFF331B13), "Elena Brooks"),
        ThemeTemplate("09", "Steel", "Wave", "Minimalist cool steel blue curves cascading cleanly toward midnight.", ColorFamily.BLUE, listOf(0xFF5C82B8, 0xFFD4E1F5, 0xFF162133), "Liam Cole"),
        ThemeTemplate("10", "Jade", "Pebble", "Smooth river-polished jade surfaces resting in tranquil shadows.", ColorFamily.GREEN, listOf(0xFF54997D, 0xFFCFEBE0, 0xFF142920), "Kenji Sato"),
        ThemeTemplate("11", "Tangerine", "Ray", "Diagonal warm amber rays slicing smoothly through hazy stillness.", ColorFamily.ORANGE, listOf(0xFFE89254, 0xFFFADCC5, 0xFF361F0E), "Dante Cross"),
        ThemeTemplate("12", "Violet", "Dusk", "Smooth violet-indigo transitions fading slowly over clean ridges.", ColorFamily.PURPLE, listOf(0xFF8361B0, 0xFFE2D4F5, 0xFF1D1429), "Luna Hayes", true, DepthSubjectHint.LOWER_SILHOUETTE),
        ThemeTemplate("13", "Slate", "Horizon", "Balanced matte grey tones carved smoothly into quiet coastal forms.", ColorFamily.MONOCHROME, listOf(0xFF6B7078, 0xFFE3E6EA, 0xFF181A1D), "Oskar Holm"),
        ThemeTemplate("14", "Iceberg", "Facet", "Crisp faceted arctic cyan planes catching clean northern daylight.", ColorFamily.COOL, listOf(0xFF6CB5C7, 0xFFE0F4F8, 0xFF182A30), "Freja Solberg", true, DepthSubjectHint.PEAK_HORIZON),
        ThemeTemplate("15", "Clay", "Ribbon", "Sculptural blush clay folds lit gently inside minimal white space.", ColorFamily.WARM, listOf(0xFFD99A8E, 0xFFF7E2DE, 0xFF33201C), "Lorenzo Bellini"),
        ThemeTemplate("16", "Periwinkle", "Glass", "Frosted periwinkle louvers refracting calm morning city glow.", ColorFamily.BLUE, listOf(0xFF6E8DD6, 0xFFE0E8FA, 0xFF192238), "Julian Meyer"),
        ThemeTemplate("17", "Verdant", "Canopy", "Layered pastel pine terraces resting peacefully under clear mist.", ColorFamily.GREEN, listOf(0xFF569471, 0xFFD1EBE0, 0xFF14261C), "Noah Bennett"),
        ThemeTemplate("18", "Apricot", "Satin", "Warm apricot satin drapes glowing softly around tranquil edges.", ColorFamily.ORANGE, listOf(0xFFE6A275, 0xFFFAE6D8, 0xFF362216), "Hana Kim"),
        ThemeTemplate("19", "Plum", "Quartz", "Deep velvety plum reflections polished smooth with gentle rim light.", ColorFamily.PURPLE, listOf(0xFF7D5899, 0xFFE0D1EB, 0xFF1D1324), "Vera Lin"),
        ThemeTemplate("20", "Alabaster", "Grain", "Clean photographic film grain resting softly on pale ivory stillness.", ColorFamily.MONOCHROME, listOf(0xFFC2C6CE, 0xFFF7F8FA, 0xFF26282B), "Arthur Vance"),
        ThemeTemplate("21", "Boreal", "Mist", "Cool mint-teal polar lights diffused smoothly across quiet night.", ColorFamily.COOL, listOf(0xFF54B0A8, 0xFFD1F2EE, 0xFF132926), "Mikkel Holm"),
        ThemeTemplate("22", "Desert", "Bloom", "Soft sun-bleached rose sandstone rising quietly out of still plains.", ColorFamily.WARM, listOf(0xFFE0B4A4, 0xFFFAF0EC, 0xFF332520), "Chloe Parker"),
        ThemeTemplate("23", "Cobalt", "Pillar", "Tall cobalt architectural pillars framing unobstructed lock clocks.", ColorFamily.BLUE, listOf(0xFF4773BA, 0xFFC7DCFA, 0xFF121E33), "Kuro Studio", true, DepthSubjectHint.CENTER_PORTRAIT)
    )
)
