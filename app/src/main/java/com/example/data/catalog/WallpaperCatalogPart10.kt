package com.example.data.catalog

import com.example.data.model.ColorFamily
import com.example.data.model.DepthSubjectHint
import com.example.data.model.Wallpaper

internal fun buildCatalogPartTen(): List<Wallpaper> = buildThemedPartCatalog(
    partTag = "p10",
    baseOrderStart = 1560,
    themes = listOf(
        ThemeTemplate("01", "Opal", "Cove", "Iridescent pale opal reflections swirling peacefully across quiet glass.", ColorFamily.COOL, listOf(0xFF70B2C4, 0xFFE0F4F8, 0xFF182A30), "Maya Nair", true, DepthSubjectHint.CENTER_PORTRAIT),
        ThemeTemplate("02", "Blush", "Limestone", "Soft pastel rose clay surfaces illuminated gently from the side.", ColorFamily.WARM, listOf(0xFFD99AA2, 0xFFF7E4E7, 0xFF332023), "Camille Laurent"),
        ThemeTemplate("03", "Azure", "Summit", "Crisp skyward azure gradients balanced cleanly above dark valleys.", ColorFamily.BLUE, listOf(0xFF4D88CF, 0xFFCFE4FA, 0xFF132338), "Lukas Meier", true, DepthSubjectHint.PEAK_HORIZON),
        ThemeTemplate("04", "Fern", "Sanctuary", "Tranquil deep forest fern tones smoothed with soft morning vapor.", ColorFamily.GREEN, listOf(0xFF477D5D, 0xFFC4E3D1, 0xFF122419), "Nora Lind", true, DepthSubjectHint.BOTANICAL_STEM),
        ThemeTemplate("05", "Mandarin", "Glow", "Warm pastel citrus tones diffused across frosted acrylic contours.", ColorFamily.ORANGE, listOf(0xFFE6975C, 0xFFFAE2CF, 0xFF362010), "Tariq Mansour"),
        ThemeTemplate("06", "Amethyst", "Spire", "Faceted translucent purple geometry glowing against velvet dusk.", ColorFamily.PURPLE, listOf(0xFF8C66C7, 0xFFE6DAF7, 0xFF1F1630), "Mina Park", true, DepthSubjectHint.CENTER_PORTRAIT),
        ThemeTemplate("07", "Basalt", "Echo", "Clean matte obsidian textures layered softly with silver rim light.", ColorFamily.MONOCHROME, listOf(0xFF545861, 0xFFDFE2E6, 0xFF16181C), "Victor Ray"),
        ThemeTemplate("08", "Seafoam", "Linen", "Cool seafoam tones woven gently into weightless studio drapery.", ColorFamily.COOL, listOf(0xFF63B8B2, 0xFFD9F5F3, 0xFF162B29), "Chloe Parker"),
        ThemeTemplate("09", "Champagne", "Dune", "Pale golden champagne contours sweeping softly into calm mist.", ColorFamily.WARM, listOf(0xFFDEC3A0, 0xFFFAF2E8, 0xFF332C22), "Lorenzo Bellini"),
        ThemeTemplate("10", "Marine", "Crest", "Clean deep marine gradients cresting smoothly under twilight calm.", ColorFamily.BLUE, listOf(0xFF436DB3, 0xFFC4D8F5, 0xFF121E33), "Soren Halvorsen", true, DepthSubjectHint.LOWER_SILHOUETTE),
        ThemeTemplate("11", "Olive", "Terrace", "Earthy muted olive plaster textures washed cleanly in natural sun.", ColorFamily.GREEN, listOf(0xFF7B946B, 0xFFE3ECDC, 0xFF1F261A), "Clara Lindqvist"),
        ThemeTemplate("12", "Coral", "Canopy", "Lively soft coral tones fading gently toward warm cream skies.", ColorFamily.ORANGE, listOf(0xFFE38C72, 0xFFFAE0D8, 0xFF361D16), "Hana Kim"),
        ThemeTemplate("13", "Lilac", "Prism", "Weightless lilac refractions scattered softly over brushed alabaster.", ColorFamily.PURPLE, listOf(0xFFA284D4, 0xFFEDE4FA, 0xFF241B33), "Iris Reid"),
        ThemeTemplate("14", "Charcoal", "Column", "Monolithic fluted charcoal geometry standing cleanly against fog.", ColorFamily.MONOCHROME, listOf(0xFF6B707A, 0xFFE8EAEE, 0xFF181A1E), "Oskar Holm", true, DepthSubjectHint.TOP_ARCH),
        ThemeTemplate("15", "Glacier", "Tide", "Clean icy turquoise reflections settling peacefully into still bays.", ColorFamily.COOL, listOf(0xFF5AA8BF, 0xFFD4EFF7, 0xFF14282E), "Freja Solberg"),
        ThemeTemplate("16", "Peach", "Atelier", "Diffused matte peach studio curves crafted for zero visual noise.", ColorFamily.WARM, listOf(0xFFE6A992, 0xFFFAF0EB, 0xFF36221C), "Kenji Sato"),
        ThemeTemplate("17", "Cerulean", "Beacon", "Crisp cerulean twilight rising cleanly around minimalist towers.", ColorFamily.BLUE, listOf(0xFF5382C7, 0xFFD4E4FA, 0xFF142136), "Mikkel Holm", true, DepthSubjectHint.CENTER_PORTRAIT),
        ThemeTemplate("18", "Pine", "Vapor", "Cool evergreen silhouettes dissolved smoothly into alpine cloud.", ColorFamily.GREEN, listOf(0xFF436E55, 0xFFBFD9C9, 0xFF111E17), "Callum Fraser", true, DepthSubjectHint.BOTANICAL_STEM),
        ThemeTemplate("19", "Amber", "Dune", "Sunlit amber curves sweeping rhythmically across serene plateaus.", ColorFamily.ORANGE, listOf(0xFFDE9454, 0xFFF7DEC8, 0xFF36200F), "Elena Brooks"),
        ThemeTemplate("20", "Wisteria", "Veil", "Translucent pastel violet petals suspended cleanly in quiet air.", ColorFamily.PURPLE, listOf(0xFF9474C2, 0xFFE9DFF7, 0xFF21182E), "Sora Takahashi"),
        ThemeTemplate("21", "Ivory", "Monolith", "Pure matte porcelain planes casting soft minimal shadows.", ColorFamily.MONOCHROME, listOf(0xFFCFD3DA, 0xFFFAFBFD, 0xFF282A2E), "Kuro Studio", true, DepthSubjectHint.TOP_ARCH),
        ThemeTemplate("22", "Lagoon", "Aura", "Cool teal-aqua gradient halos breathing slowly across clear backdrops.", ColorFamily.COOL, listOf(0xFF4CA8B0, 0xFFCFF2F5, 0xFF12292B), "Noah Bennett"),
        ThemeTemplate("23", "Midnight", "Crescent", "Velvet midnight indigo arcs designed cleanly for high-contrast clocks.", ColorFamily.BLUE, listOf(0xFF415B9E, 0xFFC4D3F5, 0xFF0E162B), "Arthur Vance", true, DepthSubjectHint.LOWER_SILHOUETTE)
    )
)
