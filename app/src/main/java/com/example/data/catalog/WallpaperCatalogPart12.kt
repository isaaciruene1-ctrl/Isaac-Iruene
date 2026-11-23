package com.example.data.catalog

import com.example.data.model.ColorFamily
import com.example.data.model.DepthSubjectHint
import com.example.data.model.Wallpaper

internal fun buildCatalogPartTwelve(): List<Wallpaper> = buildThemedPartCatalog(
    partTag = "p12",
    baseOrderStart = 2160,
    themes = listOf(
        ThemeTemplate("01", "Solstice", "Halo", "Luminous pastel champagne glow radiating gently across calm sky.", ColorFamily.WARM, listOf(0xFFE6C8A2, 0xFFFAF3E8, 0xFF332A1E), "Iris Reid", true, DepthSubjectHint.CENTER_PORTRAIT),
        ThemeTemplate("02", "Mariana", "Pulse", "Deep ocean cobalt swells smoothed cleanly toward tranquil dusk.", ColorFamily.BLUE, listOf(0xFF4168AD, 0xFFC2D6F5, 0xFF111C30), "Chloe Parker"),
        ThemeTemplate("03", "Matcha", "Swirl", "Creamy pale matcha waves layered gently in tranquil studio calm.", ColorFamily.GREEN, listOf(0xFF7BA686, 0xFFE3F0E6, 0xFF1C2920), "Kenji Sato"),
        ThemeTemplate("04", "Marigold", "Dusk", "Rich pastel marigold gradients warming cool evening clouds.", ColorFamily.ORANGE, listOf(0xFFE6A357, 0xFFFAE6CC, 0xFF36230F), "Mateo Navarro"),
        ThemeTemplate("05", "Orchid", "Frost", "Crisp translucent orchid crystal surfaces glowing softly inside fog.", ColorFamily.PURPLE, listOf(0xFF9C78CC, 0xFFEDE2FA, 0xFF231930), "Mina Park", true, DepthSubjectHint.CENTER_PORTRAIT),
        ThemeTemplate("06", "Carbon", "Matte", "Ultra-clean charcoal matte planes tailored smoothly for OLED depth.", ColorFamily.MONOCHROME, listOf(0xFF4A4E57, 0xFFD9DCE0, 0xFF141518), "Victor Ray"),
        ThemeTemplate("07", "Fjord", "Glass", "Cool refracted glacier ripples glistening faintly over calm water.", ColorFamily.COOL, listOf(0xFF5FAEC2, 0xFFD9F3FA, 0xFF16292E), "Soren Halvorsen", true, DepthSubjectHint.LOWER_SILHOUETTE),
        ThemeTemplate("08", "Terracotta", "Sun", "Rounded desert terracotta forms framed smoothly in golden stillness.", ColorFamily.WARM, listOf(0xFFD9876C, 0xFFF7DDD4, 0xFF331D15), "Lorenzo Bellini"),
        ThemeTemplate("09", "Twilight", "Bay", "Still cobalt-indigo bay reflecting last ribbons of pastel dusk.", ColorFamily.BLUE, listOf(0xFF4E78B8, 0xFFCCDEFA, 0xFF142033), "Callum Fraser"),
        ThemeTemplate("10", "Sequoia", "Shade", "Deep woodland emerald shadows softened cleanly by coastal haze.", ColorFamily.GREEN, listOf(0xFF437356, 0xFFC2DEC9, 0xFF112117), "Noah Bennett", true, DepthSubjectHint.BOTANICAL_STEM),
        ThemeTemplate("11", "Saffron", "Ridge", "Saffron-lit canyon crests rising cleanly above velvet violet valleys.", ColorFamily.ORANGE, listOf(0xFFE09452, 0xFFF7DCC2, 0xFF36200E), "Elena Brooks", true, DepthSubjectHint.PEAK_HORIZON),
        ThemeTemplate("12", "Heather", "Moor", "Soft dusty heather hills receding peacefully into pale morning mist.", ColorFamily.PURPLE, listOf(0xFF8C74AB, 0xFFE6DCF2, 0xFF201929), "Camille Laurent"),
        ThemeTemplate("13", "Pewter", "Silk", "Smooth silver-pewter tones curving gracefully across quiet frames.", ColorFamily.MONOCHROME, listOf(0xFF8C929C, 0xFFF0F2F5, 0xFF1F2126), "Oskar Holm"),
        ThemeTemplate("14", "Tundra", "Light", "Pale frosted cyan daylight washing cleanly over pristine ice plains.", ColorFamily.COOL, listOf(0xFF74BAC7, 0xFFE6F6FA, 0xFF1A2B2E), "Freja Solberg"),
        ThemeTemplate("15", "Nectar", "Cloud", "Velvety warm peach clouds drifting gently above serene silhouettes.", ColorFamily.WARM, listOf(0xFFE6AC98, 0xFFFAF0EC, 0xFF36231D), "Sora Takahashi", true, DepthSubjectHint.LOWER_SILHOUETTE),
        ThemeTemplate("16", "Horizon", "Azure", "Crisp bright azure expanse crafted for airy home screen icons.", ColorFamily.BLUE, listOf(0xFF568ED4, 0xFFD6E8FA, 0xFF162438), "Liam Cole"),
        ThemeTemplate("17", "Bamboo", "Dew", "Fresh morning dew along tall slender bamboo stalks in quiet light.", ColorFamily.GREEN, listOf(0xFF59A886, 0xFFCFF0E2, 0xFF142920), "Kenji Mori", true, DepthSubjectHint.BOTANICAL_STEM),
        ThemeTemplate("18", "Cinnamon", "Foil", "Warm metallic copper-cinnamon waves catching subtle studio sheen.", ColorFamily.ORANGE, listOf(0xFFD68258, 0xFFF5D6C6, 0xFF331B10), "Dante Cross"),
        ThemeTemplate("19", "Iris", "Nocturne", "Deep velvety iris dusk dissolving softly toward clean starlight.", ColorFamily.PURPLE, listOf(0xFF725499, 0xFFDDD1ED, 0xFF1B1226), "Luna Hayes"),
        ThemeTemplate("20", "Quartz", "White", "Airy bright quartz facets breathing subtly under soft clean glow.", ColorFamily.MONOCHROME, listOf(0xFFD4D7DE, 0xFFFAFBFD, 0xFF2A2C30), "Kuro Studio", true, DepthSubjectHint.TOP_ARCH),
        ThemeTemplate("21", "Cove", "Aqua", "Translucent pastel aqua ripples calming busy icon grids.", ColorFamily.COOL, listOf(0xFF59B8B8, 0xFFD6F7F7, 0xFF142E2E), "Maya Nair"),
        ThemeTemplate("22", "Linen", "Dawn", "Warm natural linen folds brushed delicately by early morning sun.", ColorFamily.WARM, listOf(0xFFDCC4AC, 0xFFFAF2EB, 0xFF302821), "Vera Lin"),
        ThemeTemplate("23", "Aether", "Zenith", "Seamless sapphire-to-mist gradients leaving crystal room for widgets.", ColorFamily.BLUE, listOf(0xFF4C7BC4, 0xFFCFE2FA, 0xFF132136), "Arthur Vance", true, DepthSubjectHint.PEAK_HORIZON)
    )
)
