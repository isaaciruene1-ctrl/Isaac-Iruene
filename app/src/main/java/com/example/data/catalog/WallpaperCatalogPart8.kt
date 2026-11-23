package com.example.data.catalog

import com.example.data.model.ColorFamily
import com.example.data.model.DepthSubjectHint
import com.example.data.model.ResolutionTier
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperCategory

internal fun buildCatalogPartEight(): List<Wallpaper> = buildList {
    // 1. NATURE (+23)
    addAll(
        buildCategoryBatch(
            prefix = "natp8",
            category = WallpaperCategory.NATURE,
            baseOrder = 950,
            specs = listOf(
                SeedSpec("01", "Yosemite Granite Mist", "Towering El Capitan monolith softened by drifting silver valley mist.", ColorFamily.COOL, listOf(0xFF7B94AB, 0xFFE3ECF5, 0xFF1E262E), listOf("nature", "yosemite", "granite", "mist"), "Noah Bennett", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("02", "Atacama Flamingo Lagoon", "Mineral pastel salt lagoon glowing peach under high Andean morning sun.", ColorFamily.WARM, listOf(0xFFE0A48E, 0xFF789EB8, 0xFF2B2121), listOf("nature", "atacama", "lagoon", "andes"), "Diego Vargas"),
                SeedSpec("03", "Plitvice Emerald Tiers", "Stepped travertine waterfalls pouring through translucent emerald pools.", ColorFamily.GREEN, listOf(0xFF47997A, 0xFFC2EBDA, 0xFF122920), listOf("nature", "waterfall", "emerald", "pools"), "Lukas Meier"),
                SeedSpec("04", "Bora Turquoise Shallows", "Crystal lagoon ripples shifting gently from pale mint to deep cobalt.", ColorFamily.BLUE, listOf(0xFF4E9BD4, 0xFFC9EEFA, 0xFF13293B), listOf("nature", "lagoon", "ocean", "turquoise"), "Chloe Parker"),
                SeedSpec("05", "Sossusvlei Amber Ridge", "Curved apricot sand dunes backlit gently by clean desert sunrise.", ColorFamily.ORANGE, listOf(0xFFE08C52, 0xFFF7D4B8, 0xFF361D0D), listOf("nature", "desert", "dune", "sunrise"), "Tariq Mansour"),
                SeedSpec("06", "Hokkaido Snow Pine", "Lonely cedar tree standing serene in endless pristine white powder.", ColorFamily.MONOCHROME, listOf(0xFFAAB0BA, 0xFFF7F8FA, 0xFF232529), listOf("nature", "hokkaido", "snow", "pine"), "Kenji Sato"),
                SeedSpec("07", "Violet Lupine Meadow", "Wild violet lupine spires glowing before misty New Zealand peaks.", ColorFamily.PURPLE, listOf(0xFF8C70C4, 0xFFE0D4F7, 0xFF1F182E), listOf("nature", "lupine", "meadow", "violet"), "Iris Reid"),
                SeedSpec("08", "Giverny Lily Reflection", "Still forest pond dappled with blush waterlilies and willow shadows.", ColorFamily.GREEN, listOf(0xFF568A6E, 0xFFE3B8C2, 0xFF182920), listOf("nature", "pond", "waterlily", "calm"), "Camille Laurent", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Svalbard Blue Ice", "Drifting tabular glacier floes suspended cleanly in mirror Arctic bay.", ColorFamily.BLUE, listOf(0xFF6B9FD1, 0xFFE3F0FC, 0xFF182838), listOf("nature", "svalbard", "ice", "arctic"), "Freja Solberg"),
                SeedSpec("10", "Cappadocia Rose Valley", "Soft sculpted rose-tuff ridges bathed cleanly in calm morning haze.", ColorFamily.WARM, listOf(0xFFD9988E, 0xFFF7DFDB, 0xFF331E1B), listOf("nature", "cappadocia", "rose", "valley"), "Elena Conti"),
                SeedSpec("11", "Norwegian Fjell Fog", "Cool slate plateau waterfalls disappearing gracefully into drifting cloud.", ColorFamily.COOL, listOf(0xFF7294A8, 0xFFDDEBF2, 0xFF1B262E), listOf("nature", "norway", "plateau", "fog"), "Soren Halvorsen"),
                SeedSpec("12", "Sahara Starlight Dune", "Silky golden ridge beneath crystal-clear velvet southern constellations.", ColorFamily.ORANGE, listOf(0xFFD69A64, 0xFF3E4C6B, 0xFF1A191F), listOf("nature", "sahara", "stars", "night"), "Tariq Mansour"),
                SeedSpec("13", "Yakushima Ancient Cedar", "Dewy emerald moss clinging cleanly to thousand-year cedar roots.", ColorFamily.GREEN, listOf(0xFF477355, 0xFFAED1B9, 0xFF132418), listOf("nature", "yakushima", "moss", "ancient"), "Kenji Mori", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("14", "Wisteria Tunnel Dusk", "Cascading soft lilac wisteria blossoms glowing warmly at blue hour.", ColorFamily.PURPLE, listOf(0xFF9C7EC9, 0xFFEBDCF7, 0xFF231A30), listOf("nature", "wisteria", "lilac", "spring"), "Sora Takahashi"),
                SeedSpec("15", "Mono Lake Tufa Dusk", "Limestone spires rising silently out of glass-calm charcoal dusk water.", ColorFamily.MONOCHROME, listOf(0xFF7D828C, 0xFFE3E6EB, 0xFF1B1D21), listOf("nature", "tufa", "lake", "silhouette"), "Arthur Vance", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("16", "Big Sur Sea Mist", "Pacific emerald surf breaking gently beneath sunlit coastal headlands.", ColorFamily.COOL, listOf(0xFF5CA3A8, 0xFFD4F0F2, 0xFF15292B), listOf("nature", "bigsur", "ocean", "surf"), "Chloe Parker"),
                SeedSpec("17", "Sedona Coral Mesa", "Warm sandstone towers reflecting soft peach twilight clouds.", ColorFamily.WARM, listOf(0xFFD98870, 0xFFF7D4C9, 0xFF331B14), listOf("nature", "sedona", "mesa", "coral"), "Elena Brooks"),
                SeedSpec("18", "Crater Ice Sapphire", "Deep cobalt meltwater pool gleaming atop crisp glacier saddle.", ColorFamily.BLUE, listOf(0xFF4A85C9, 0xFFC7E0FA, 0xFF132338), listOf("nature", "glacier", "cobalt", "alpine"), "Lukas Meier", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Teal Canyon Rapids", "Silky glacier-melt emerald river carving smooth limestone gorges.", ColorFamily.GREEN, listOf(0xFF4CA38B, 0xFFC5EBE0, 0xFF122923), listOf("nature", "river", "gorge", "emerald"), "Callum Fraser"),
                SeedSpec("20", "Autumn Aspen Haze", "Quaking golden-amber aspen trunks shimmering cleanly through crisp fog.", ColorFamily.ORANGE, listOf(0xFFE0A354, 0xFFFAE6C5, 0xFF33230E), listOf("nature", "aspen", "autumn", "gold"), "Noah Bennett"),
                SeedSpec("21", "Twilight Plum Fjord", "Still fjord mirror tinted soft plum beneath vanishing alpine twilight.", ColorFamily.PURPLE, listOf(0xFF836B9E, 0xFFDCCCE8, 0xFF1E1726), listOf("nature", "fjord", "twilight", "plum"), "Mikkel Holm"),
                SeedSpec("22", "Frost Reed Marsh", "Silvery frozen reeds lined neatly along misty dawn wetlands.", ColorFamily.MONOCHROME, listOf(0xFF9EA4AD, 0xFFF0F2F5, 0xFF232529), listOf("nature", "marsh", "reeds", "frost"), "Oskar Lind"),
                SeedSpec("23", "Coral Reef Sunbeam", "Diagonal sunlit rays shimmering softly through tranquil turquoise cove.", ColorFamily.COOL, listOf(0xFF54B2C2, 0xFFCFF2F7, 0xFF132A2E), listOf("nature", "ocean", "sunbeams", "coral"), "Maya Nair")
            )
        )
    )

    // 2. ABSTRACT (+23)
    addAll(
        buildCategoryBatch(
            prefix = "absp8",
            category = WallpaperCategory.ABSTRACT,
            baseOrder = 975,
            specs = listOf(
                SeedSpec("01", "Opal Glass Caustics", "Refracted water-light caustics dancing smoothly over frosted alabaster.", ColorFamily.COOL, listOf(0xFF7BC4CF, 0xFFEAF7FA, 0xFF1B3033), listOf("abstract", "caustics", "opal", "water"), "Iris Reid"),
                SeedSpec("02", "Warm Terracotta Helix", "Sculptural matte terracotta ribbon twisting gently in soft daylight.", ColorFamily.WARM, listOf(0xFFD99078, 0xFFF7DDD4, 0xFF362018), listOf("abstract", "terracotta", "helix", "sculpt"), "Camille Laurent", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("03", "Cobalt Matte Archways", "Nested matte cobalt paper curves casting layered soft shadows.", ColorFamily.BLUE, listOf(0xFF4F7EC7, 0xFFC4DAFA, 0xFF142138), listOf("abstract", "cobalt", "paper", "arches"), "Julian Meyer"),
                SeedSpec("04", "Sage Frosted Discs", "Overlapping translucent sage resin circles glowing smoothly.", ColorFamily.GREEN, listOf(0xFF75A68C, 0xFFDDF0E6, 0xFF1B2B23), listOf("abstract", "sage", "resin", "discs"), "Nora Lind"),
                SeedSpec("05", "Orchid Velvet Contours", "Airy orchid and lilac gradient ridges rolling cleanly across screen.", ColorFamily.PURPLE, listOf(0xFF9F7BD6, 0xFFECE0FA, 0xFF241A36), listOf("abstract", "orchid", "contours", "velvet"), "Mina Park"),
                SeedSpec("06", "Amber Ambergris Ripple", "Molten honey-amber resin ripples catching clean warm highlights.", ColorFamily.ORANGE, listOf(0xFFE39C59, 0xFFFAE2C8, 0xFF362210), listOf("abstract", "amber", "resin", "ripple"), "Dante Cross"),
                SeedSpec("07", "Graphite Silk Tension", "Stretched matte charcoal fabric planes catching sharp rim lighting.", ColorFamily.MONOCHROME, listOf(0xFF545861, 0xFFD9DCE0, 0xFF17181C), listOf("abstract", "graphite", "fabric", "minimal"), "Victor Ray"),
                SeedSpec("08", "Peach Frost Prism", "Soft frosted glass prism blending warm peach and cream highlights.", ColorFamily.WARM, listOf(0xFFE6AB94, 0xFFFAF0EB, 0xFF36231C), listOf("abstract", "peach", "prism", "frosted"), "Sora Takahashi", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Cerulean Liquid Silk", "Weightless cerulean silk waves undulating slowly over pale mist.", ColorFamily.BLUE, listOf(0xFF5E96D1, 0xFFD9ECFA, 0xFF162638), listOf("abstract", "cerulean", "silk", "wave"), "Theo Sterling"),
                SeedSpec("10", "Celadon Marble Vein", "Translucent celadon onyx backlit softly with pale ivory veins.", ColorFamily.GREEN, listOf(0xFF6BB595, 0xFFD9F2E6, 0xFF182E24), listOf("abstract", "onyx", "celadon", "marble"), "Clara Lindqvist"),
                SeedSpec("11", "Apricot Aura Blur", "Gentle atmospheric apricot and coral sphere fading into soft cream.", ColorFamily.ORANGE, listOf(0xFFE89B74, 0xFFFAE6DC, 0xFF362015), listOf("abstract", "aura", "apricot", "blur"), "Hana Kim"),
                SeedSpec("12", "Periwinkle Foam Curve", "Sculpted acoustic pastel purple waves lit cleanly from above.", ColorFamily.PURPLE, listOf(0xFF8C7DD1, 0xFFE3DFFA, 0xFF1F1B33), listOf("abstract", "periwinkle", "wave", "matte"), "Vera Lin"),
                SeedSpec("13", "Silver Mercury Drop", "Liquid mercury beads coalescing cleanly on brushed steel mirror.", ColorFamily.MONOCHROME, listOf(0xFFA3A8B2, 0xFFF2F4F7, 0xFF232529), listOf("abstract", "mercury", "silver", "liquid"), "Stefan K", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("14", "Aqua Lens Refraction", "Curved aqua borosilicate rods bending soft cool light rays.", ColorFamily.COOL, listOf(0xFF5CB8C4, 0xFFD4F2F7, 0xFF142D30), listOf("abstract", "aqua", "refraction", "glass"), "Kuro Studio"),
                SeedSpec("15", "Dusty Rose Dune Fold", "Smooth velvety blush-rose terrain folds in calm studio glow.", ColorFamily.WARM, listOf(0xFFD998A0, 0xFFF7E3E6, 0xFF332023), listOf("abstract", "rose", "velvet", "fold"), "Elena Conti"),
                SeedSpec("16", "Sapphire Glass Cube", "Suspended beveled sapphire glass blocks refracting crisp blue gradients.", ColorFamily.BLUE, listOf(0xFF4778C4, 0xFFBAD4FA, 0xFF122038), listOf("abstract", "sapphire", "cube", "3d"), "Liam Cole"),
                SeedSpec("17", "Mint Chromatic Ribbon", "Continuous floating mint and seafoam ribbon twisting cleanly in air.", ColorFamily.GREEN, listOf(0xFF5EC49E, 0xFFD1F7E8, 0xFF142E24), listOf("abstract", "mint", "ribbon", "pastel"), "Kenji Sato"),
                SeedSpec("18", "Tangerine Gradient Grid", "Soft glass checkerboard tiles diffusing radiant tangerine backlight.", ColorFamily.ORANGE, listOf(0xFFE68D54, 0xFFFAD8C2, 0xFF361D0E), listOf("abstract", "tangerine", "grid", "glass"), "Max Weber", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Plum Nebula Fluid", "Thick pearlescent plum pigments swirling slowly into quiet midnight.", ColorFamily.PURPLE, listOf(0xFF855CA6, 0xFFDCC7EB, 0xFF1F1429), listOf("abstract", "plum", "fluid", "macro"), "Luna Hayes"),
                SeedSpec("20", "Ivory Alabaster Shell", "Paper-thin fluted white porcelain shell glowing warmly from inside.", ColorFamily.MONOCHROME, listOf(0xFFD9DCE0, 0xFFFAFBFD, 0xFF2B2C30), listOf("abstract", "porcelain", "ivory", "shell"), "Oskar Holm"),
                SeedSpec("21", "Cyan Mist Strata", "Translucent horizontal cyan fog bars drifting smoothly over ice blue.", ColorFamily.COOL, listOf(0xFF6EAEC7, 0xFFE0F2F7, 0xFF182B33), listOf("abstract", "strata", "cyan", "mist"), "Freja Solberg"),
                SeedSpec("22", "Champagne Satin Swell", "Luminous champagne silk swells catching gentle golden edge light.", ColorFamily.WARM, listOf(0xFFDCC2A1, 0xFFFAF0E3, 0xFF332C22), listOf("abstract", "champagne", "silk", "gold"), "Camille Laurent"),
                SeedSpec("23", "Indigo Ripple Interference", "Concentric calm indigo wave interference patterns on matte surface.", ColorFamily.BLUE, listOf(0xFF5473BF, 0xFFD1DCFA, 0xFF141D33), listOf("abstract", "indigo", "ripple", "pattern"), "Julian Meyer")
            )
        )
    )

    // 3. AMOLED (+23)
    addAll(
        buildCategoryBatch(
            prefix = "amop8",
            category = WallpaperCategory.AMOLED,
            baseOrder = 1000,
            specs = listOf(
                SeedSpec("01", "OLED Sapphire Lotus", "Backlit sapphire glass petals floating centered in true #000000 black.", ColorFamily.BLUE, listOf(0xFF000000, 0xFF5490E6, 0xFF0E1D33), listOf("amoled", "sapphire", "lotus", "oled"), "Kuro Lab", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("02", "Pitch Copper Crescent", "Razor-thin glowing copper arc cradling pure black center screen.", ColorFamily.ORANGE, listOf(0xFF000000, 0xFFE38E59, 0xFF2B180C), listOf("amoled", "copper", "crescent", "black"), "Dorian Gray"),
                SeedSpec("03", "True-Black Mint Halo", "Crisp mint bioluminescent ring suspended serenely in zero-pixel dark.", ColorFamily.GREEN, listOf(0xFF000000, 0xFF5CD4A3, 0xFF0D291E), listOf("amoled", "mint", "halo", "minimal"), "Clara Reid"),
                SeedSpec("04", "Obsidian Plum Prism", "Single translucent violet glass triangle refracting rim light in black.", ColorFamily.PURPLE, listOf(0xFF000000, 0xFFA678E0, 0xFF1E132E), listOf("amoled", "plum", "prism", "dark"), "Mina Park", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("05", "Black Cyan Waveguide", "Fine cyan optical fiber arcs tracing curved paths in pitch darkness.", ColorFamily.COOL, listOf(0xFF000000, 0xFF5CE0EB, 0xFF0E292B), listOf("amoled", "cyan", "fiber", "wave"), "Theo Sterling"),
                SeedSpec("06", "Silver Horizon Thread", "Single luminous silver string tensioned gently across pure black void.", ColorFamily.MONOCHROME, listOf(0xFF000000, 0xFFDDE0E6, 0xFF1A1B1F), listOf("amoled", "silver", "minimal", "line"), "Victor Ray"),
                SeedSpec("07", "Blush Dusk Void Sphere", "Soft warm blush glass orb dimly glowing inside deep obsidian frame.", ColorFamily.WARM, listOf(0xFF000000, 0xFFE39CA4, 0xFF2B181A), listOf("amoled", "blush", "orb", "glow"), "Iris Reid"),
                SeedSpec("08", "OLED Cobalt Topography", "Delicate cobalt elevation lines fading smoothly into pure OLED margins.", ColorFamily.BLUE, listOf(0xFF000000, 0xFF598CE0, 0xFF0E1B33), listOf("amoled", "cobalt", "topo", "contour"), "Alex Mercer", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Pitch Jade Reed", "Single backlit jade reed bending quietly across true-black night.", ColorFamily.GREEN, listOf(0xFF000000, 0xFF5EB58C, 0xFF0E241A), listOf("amoled", "jade", "reed", "botanical"), "Kenji Sato", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("10", "Tangerine Void Ring", "Warm tangerine neon halo orbiting obliquely in pitch black emptiness.", ColorFamily.ORANGE, listOf(0xFF000000, 0xFFED8E4C, 0xFF2E180A), listOf("amoled", "tangerine", "ring", "neon"), "Julian Meyer"),
                SeedSpec("11", "Amethyst Rain Streaks", "Subtle falling violet micro-streaks gleaming cleanly against #000000.", ColorFamily.PURPLE, listOf(0xFF000000, 0xFF9C78D6, 0xFF1B132B), listOf("amoled", "amethyst", "rain", "minimal"), "Hana Kim"),
                SeedSpec("12", "Ice Quartz Black Slate", "Single frosted cyan crystal needle emerging sharply from true black.", ColorFamily.COOL, listOf(0xFF000000, 0xFF8CE0EB, 0xFF102629), listOf("amoled", "quartz", "ice", "crystal"), "Freja Solberg"),
                SeedSpec("13", "Gold Crescent Silhouette", "Warm backlit mountain ridge traced cleanly in gold over black.", ColorFamily.WARM, listOf(0xFF000000, 0xFFE0B472, 0xFF291F10), listOf("amoled", "gold", "ridge", "minimal"), "Lukas Meier", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("14", "Monochrome Smoke Ribbon", "Wispy silver smoke trail ascending silently up pure black background.", ColorFamily.MONOCHROME, listOf(0xFF000000, 0xFFB0B5BF, 0xFF17181C), listOf("amoled", "smoke", "silver", "zen"), "Nora Lind"),
                SeedSpec("15", "Indigo Gravity Well", "Minimalist concentric indigo grids dipping cleanly into dark void.", ColorFamily.BLUE, listOf(0xFF000000, 0xFF5D78D9, 0xFF10162E), listOf("amoled", "indigo", "gravity", "grid"), "Zane Reid"),
                SeedSpec("16", "Seafoam Mist Eclipse", "Soft turquoise corona rim hugging pure pitch black disk.", ColorFamily.GREEN, listOf(0xFF000000, 0xFF52C4A8, 0xFF0D2620), listOf("amoled", "seafoam", "eclipse", "corona"), "Kenji Mori"),
                SeedSpec("17", "Ember Spark Drift", "Sparse warm orange embers drifting upwards through velvety OLED black.", ColorFamily.ORANGE, listOf(0xFF000000, 0xFFE68449, 0xFF2B150A), listOf("amoled", "embers", "sparks", "warm"), "Dante Cross"),
                SeedSpec("18", "Lavender Wireframe Torus", "Fine lilac geometric mesh torus rotating smoothly inside black canvas.", ColorFamily.PURPLE, listOf(0xFF000000, 0xFFB896EB, 0xFF1E162E), listOf("amoled", "lavender", "mesh", "torus"), "Vera Lin", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Cyan Ice Comet", "Razor-crisp cool cyan light streak cutting serenely across true black.", ColorFamily.COOL, listOf(0xFF000000, 0xFF75D6E6, 0xFF102429), listOf("amoled", "comet", "cyan", "streak"), "Sora Takahashi"),
                SeedSpec("20", "Rose Copper Foil Edge", "Warm curved rose-copper specular highlight resting on pitch obsidian.", ColorFamily.WARM, listOf(0xFF000000, 0xFFDEA18B, 0xFF291A15), listOf("amoled", "copper", "foil", "edge"), "Camille Laurent"),
                SeedSpec("21", "Midnight White Moon", "Tiny crisp pale silver full moon floating softly over #000000 void.", ColorFamily.MONOCHROME, listOf(0xFF000000, 0xFFE8EAEE, 0xFF18191C), listOf("amoled", "moon", "white", "minimal"), "Artemis Lens"),
                SeedSpec("22", "Azure Aurora Thread", "Single luminous azure curtain ribbon hanging delicately in black night.", ColorFamily.BLUE, listOf(0xFF000000, 0xFF529CE0, 0xFF0D1E30), listOf("amoled", "aurora", "azure", "ribbon"), "Henrik Solberg"),
                SeedSpec("23", "Forest Emerald Drop", "Single glowing emerald dewdrop suspended cleanly against pitch dark.", ColorFamily.GREEN, listOf(0xFF000000, 0xFF4EC284, 0xFF0D2618), listOf("amoled", "dewdrop", "emerald", "minimal"), "Clara Lindqvist")
            )
        )
    )

    // 4. FUTURISTIC (+23)
    addAll(
        buildCategoryBatch(
            prefix = "futp8",
            category = WallpaperCategory.FUTURISTIC,
            baseOrder = 1025,
            specs = listOf(
                SeedSpec("01", "Aero Glass Skybridge", "Translucent cantilevered skywalk floating high above soft cloud banks.", ColorFamily.COOL, listOf(0xFF6CB5C9, 0xFFE3F5FA, 0xFF182C33), listOf("futuristic", "skybridge", "clouds", "glass"), "Julian Meyer", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("02", "Solarpunk Canopy Tower", "Spiraling white timber skyscraper draped in sunlit cascading gardens.", ColorFamily.GREEN, listOf(0xFF549975, 0xFFCFE8D9, 0xFF15291E), listOf("futuristic", "solarpunk", "gardens", "tower"), "Clara Reid"),
                SeedSpec("03", "Cobalt Magnetic Ring", "Giant gleaming cobalt particle ring floating inside clean laboratory mist.", ColorFamily.BLUE, listOf(0xFF477BC9, 0xFFBBD6FA, 0xFF122038), listOf("futuristic", "collider", "cobalt", "ring"), "Theo Sterling"),
                SeedSpec("04", "Dune Harvester Mirage", "Minimalist brushed titanium desert crawler gliding cleanly across dust.", ColorFamily.WARM, listOf(0xFFD6A87E, 0xFFF7E5D4, 0xFF332418), listOf("futuristic", "desert", "rover", "titanium"), "Tariq Mansour"),
                SeedSpec("05", "Amethyst Quantum Cube", "Levitating frosted lavender optical processor glowing cleanly in dark vault.", ColorFamily.PURPLE, listOf(0xFF8C68CC, 0xFFE0D1FA, 0xFF1E1430), listOf("futuristic", "quantum", "cube", "violet"), "Mina Park", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("06", "Amber Spaceport Apron", "Sleek matte starliner silhouette illuminated warmly in amber mist.", ColorFamily.ORANGE, listOf(0xFFE08C53, 0xFFF7D1B8, 0xFF331B0E), listOf("futuristic", "starliner", "spaceport", "amber"), "Zane Reid"),
                SeedSpec("07", "Monolith Cleanroom", "Ultra-pure white wafer lab gallery receding symmetrically into soft blur.", ColorFamily.MONOCHROME, listOf(0xFFB5BAC2, 0xFFF7F8FA, 0xFF26282E), listOf("futuristic", "cleanroom", "minimal", "white"), "Nora Lind"),
                SeedSpec("08", "Cyan Orbital Mirror", "Faceted silver solar collector drifting smoothly above ice-blue cloudtops.", ColorFamily.COOL, listOf(0xFF66B2C7, 0xFFDDF3F7, 0xFF172C30), listOf("futuristic", "satellite", "mirror", "orbit"), "Cassini Lab", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Indigo Data Cathedral", "Towering translucent indigo server blades casting crisp geometric light.", ColorFamily.BLUE, listOf(0xFF4C68B8, 0xFFC2CEFA, 0xFF121933), listOf("futuristic", "datacenter", "indigo", "geometry"), "Kuro Lab"),
                SeedSpec("10", "Emerald Hydroponic Arch", "Glowing vertical misted plant cylinders curving cleanly beneath glass dome.", ColorFamily.GREEN, listOf(0xFF4DA880, 0xFFC2EBDA, 0xFF12291F), listOf("futuristic", "hydroponic", "green", "arch"), "Mikkel Holm"),
                SeedSpec("11", "Copper Fusion Toroid", "Precision machined copper magnetic coils gleaming softly in clean hangar.", ColorFamily.ORANGE, listOf(0xFFD98456, 0xFFF7CBB2, 0xFF331A0E), listOf("futuristic", "fusion", "copper", "coil"), "Stefan K"),
                SeedSpec("12", "Violet Hologram Koi", "Weightless luminous violet holo-fish swimming calmly over dark marble.", ColorFamily.PURPLE, listOf(0xFF936CD9, 0xFFE6D4FA, 0xFF211533), listOf("futuristic", "hologram", "violet", "koi"), "Sora Takahashi"),
                SeedSpec("13", "Matte Stealth Glider", "Seamless dark charcoal hypersonic glider perched quietly on salt lake.", ColorFamily.MONOCHROME, listOf(0xFF474A52, 0xFFD9DCE0, 0xFF141518), listOf("futuristic", "glider", "stealth", "aerospace"), "Evan Cole"),
                SeedSpec("14", "Rose Lunar Habitat", "3D-printed warm rose regolith domes glowing calmly beneath starry Earthrise.", ColorFamily.WARM, listOf(0xFFD99891, 0xFFF7DEDB, 0xFF331E1C), listOf("futuristic", "lunar", "habitat", "rose"), "Artemis Lens"),
                SeedSpec("15", "Turquoise Levitation Rail", "Glowing cyan magnetic guideway stretching smoothly over misty fjord.", ColorFamily.COOL, listOf(0xFF56B8BF, 0xFFCFF3F5, 0xFF132D2E), listOf("futuristic", "maglev", "turquoise", "fjord"), "Freja Solberg"),
                SeedSpec("16", "Sapphire Exo-Helm", "Reflective cobalt visor curving cleanly inside quiet minimalist airlock.", ColorFamily.BLUE, listOf(0xFF4C80C7, 0xFFC7DFFA, 0xFF132136), listOf("futuristic", "astronaut", "visor", "sapphire"), "Orion Survey", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("17", "Mint Acoustic Vault", "Biomorphic white and mint sound-diffusing ribs curving smoothly overhead.", ColorFamily.GREEN, listOf(0xFF68C29F, 0xFFD9F7EB, 0xFF183026), listOf("futuristic", "acoustic", "mint", "parametric"), "Arthur Vance"),
                SeedSpec("18", "Tangerine Warp Corridor", "Receding concentric warm orange light frames glowing softly in white tunnel.", ColorFamily.ORANGE, listOf(0xFFE68C4E, 0xFFFAD8C0, 0xFF361D0D), listOf("futuristic", "corridor", "light", "tangerine"), "Alex Mercer", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Lavender Skyscraper Spire", "Needle-thin frosted glass tower dissolving gracefully into violet clouds.", ColorFamily.PURPLE, listOf(0xFF8C73BD, 0xFFE3D9F5, 0xFF1F182E), listOf("futuristic", "skyscraper", "spire", "lavender"), "Chloe Parker"),
                SeedSpec("20", "Chrome Acoustic Orb", "Polished liquid-chrome sphere hovering gently above matte slate plinth.", ColorFamily.MONOCHROME, listOf(0xFF9EA4AE, 0xFFF2F4F7, 0xFF212326), listOf("futuristic", "chrome", "orb", "sculpture"), "Victor Ray"),
                SeedSpec("21", "Champagne Sun Shade", "Deployable origami gold-cream solar sails folded neatly inside clean dock.", ColorFamily.WARM, listOf(0xFFDCC09E, 0xFFFAF0E3, 0xFF332B21), listOf("futuristic", "origami", "solar", "champagne"), "Camille Laurent"),
                SeedSpec("22", "Ice Blue Cryo Core", "Translucent cryo-crystal columns pulsing steadily in cool alpine bunker.", ColorFamily.COOL, listOf(0xFF70B0CC, 0xFFE3F3FA, 0xFF182A33), listOf("futuristic", "cryo", "ice", "core"), "Vera Lin"),
                SeedSpec("23", "Cobalt Drone Swarm", "Geometric constellation of pale blue micro-drones over misty sea horizon.", ColorFamily.BLUE, listOf(0xFF5688CC, 0xFFD1E3FA, 0xFF142136), listOf("futuristic", "drones", "constellation", "cobalt"), "Liam Cole")
            )
        )
    )

    // 5. ARCHITECTURE (+23)
    addAll(
        buildCategoryBatch(
            prefix = "arcp8",
            category = WallpaperCategory.ARCHITECTURE,
            baseOrder = 1050,
            specs = listOf(
                SeedSpec("01", "Milan Travertine Stairs", "Warm sculpted stone staircase sweeping cleanly beneath quiet circular oculus.", ColorFamily.WARM, listOf(0xFFD9BA9E, 0xFFF7ECE3, 0xFF332A22), listOf("architecture", "stairs", "travertine", "oculus"), "Lorenzo Bellini", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("02", "Copenhagen Steel Bridge", "Crisp pale gray steel cables fanning symmetrically across misty harbor.", ColorFamily.COOL, listOf(0xFF7D97AD, 0xFFE6EFF7, 0xFF1F272E), listOf("architecture", "bridge", "copenhagen", "cables"), "Mikkel Holm"),
                SeedSpec("03", "Kyoto Shoji Sungrid", "Warm translucent rice-paper screen filtering soft bamboo leaf silhouettes.", ColorFamily.GREEN, listOf(0xFF7A947B, 0xFFEBEFE8, 0xFF232B23), listOf("architecture", "shoji", "kyoto", "bamboo"), "Kenji Sato"),
                SeedSpec("04", "Azure Concrete Fold", "Bold geometric ultramarine museum wall framing sharp crisp white clouds.", ColorFamily.BLUE, listOf(0xFF477BC4, 0xFFD6E6FA, 0xFF122138), listOf("architecture", "ultramarine", "museum", "wall"), "Mateo Navarro"),
                SeedSpec("05", "Terracotta Louvre Light", "Warm amber midday sun slicing geometric shadows across sand courtyard.", ColorFamily.ORANGE, listOf(0xFFD98E5C, 0xFFF7D9C4, 0xFF331E10), listOf("architecture", "courtyard", "amber", "geometry"), "Tariq Mansour"),
                SeedSpec("06", "Brutalist Concrete Wing", "Sculptural cantilevered dark concrete fin floating against pale fog.", ColorFamily.MONOCHROME, listOf(0xFF737882, 0xFFE3E5E8, 0xFF1D1F24), listOf("architecture", "brutalist", "concrete", "cantilever"), "Nora Lind", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("07", "Violet Dichroic Balcony", "Translucent lavender architectural glass fins glowing softly at sunset.", ColorFamily.PURPLE, listOf(0xFF9578C9, 0xFFE6DCF7, 0xFF211A30), listOf("architecture", "dichroic", "lavender", "glass"), "Mina Park"),
                SeedSpec("08", "Ivory Plaster Rotunda", "Seamless curved white plaster walls welcoming calm top-down daylight.", ColorFamily.MONOCHROME, listOf(0xFFD1D5DC, 0xFFFAFBFD, 0xFF282A2E), listOf("architecture", "rotunda", "plaster", "minimal"), "Oskar Holm", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Sage Courtyard Arch", "Quiet pastel sage stucco arch framing sunlit olive tree canopy.", ColorFamily.GREEN, listOf(0xFF789982, 0xFFE0EDE4, 0xFF1F2B23), listOf("architecture", "sage", "arch", "courtyard"), "Elena Conti", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("10", "Cobalt Pool Pavillion", "Minimalist mirrored pool courtyard reflecting clean twilight cobalt skies.", ColorFamily.BLUE, listOf(0xFF4C7CC2, 0xFFC7DDF7, 0xFF132036), listOf("architecture", "pool", "pavilion", "cobalt"), "Chloe Parker"),
                SeedSpec("11", "Saffron Timber Pergola", "Warm golden cedar slats projecting parallel sun shadows on limestone.", ColorFamily.ORANGE, listOf(0xFFDE9B5E, 0xFFF7E1C8, 0xFF332110), listOf("architecture", "pergola", "cedar", "shadows"), "Arthur Vance"),
                SeedSpec("12", "Glacier Glass Conservatory", "Icy cyan low-iron glass tower shimmering calmly beside winter pines.", ColorFamily.COOL, listOf(0xFF73AFC4, 0xFFE6F4F7, 0xFF1A2B30), listOf("architecture", "glass", "tower", "winter"), "Freja Solberg"),
                SeedSpec("13", "Plum Sandstone Arcade", "Dusk-lit historic plum-tinted stone arcades glowing quietly over cobblestones.", ColorFamily.PURPLE, listOf(0xFF8C6B9E, 0xFFE3D4EB, 0xFF211826), listOf("architecture", "arcade", "stone", "dusk"), "Camille Laurent"),
                SeedSpec("14", "Rose Limestone Gallery", "Soft blush limestone monoliths framing calm interior museum shadows.", ColorFamily.WARM, listOf(0xFFDCA6A1, 0xFFFAE8E6, 0xFF332220), listOf("architecture", "limestone", "gallery", "rose"), "Iris Reid"),
                SeedSpec("15", "Emerald Slate Terrace", "Dark wet slate steps climbing gently alongside lush moss wall.", ColorFamily.GREEN, listOf(0xFF527862, 0xFFC4DBD0, 0xFF16241C), listOf("architecture", "slate", "terrace", "moss"), "Kenji Mori"),
                SeedSpec("16", "Periwinkle Sky Oculus", "Circular rooftop concrete aperture framing crisp soft morning clouds.", ColorFamily.BLUE, listOf(0xFF6B8FD1, 0xFFDDE8FA, 0xFF192338), listOf("architecture", "oculus", "aperture", "sky"), "Julian Meyer"),
                SeedSpec("17", "Slate Steel Monorail", "Clean silver elevated concrete track curving softly across minimalist sky.", ColorFamily.MONOCHROME, listOf(0xFF8C929B, 0xFFEBECEE, 0xFF202226), listOf("architecture", "track", "curve", "minimal"), "Max Weber"),
                SeedSpec("18", "Apricot Concrete Breeze", "Mid-century warm apricot breeze-block screen glowing softly in golden hour.", ColorFamily.ORANGE, listOf(0xFFE0966E, 0xFFF7DDD0, 0xFF361F14), listOf("architecture", "breezeblock", "midcentury", "apricot"), "Elena Brooks", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Teal Copper Campanile", "Minimalist patina-teal tower silhouette standing cleanly above calm fog.", ColorFamily.COOL, listOf(0xFF5EA3A8, 0xFFD4EFF2, 0xFF15292B), listOf("architecture", "tower", "patina", "teal"), "Lorenzo M"),
                SeedSpec("20", "Amethyst Neon Museum", "Clean white architectural steps washed gently in twilight lilac light.", ColorFamily.PURPLE, listOf(0xFF9378C7, 0xFFE6DCF7, 0xFF211A30), listOf("architecture", "steps", "museum", "lilac"), "Hana Kim"),
                SeedSpec("21", "Sandstone Desert Loft", "Low-profile raw sandstone roof slab blending seamlessly into desert haze.", ColorFamily.WARM, listOf(0xFFD6B08E, 0xFFF7EAE0, 0xFF33271C), listOf("architecture", "sandstone", "desert", "loft"), "Diego Vargas"),
                SeedSpec("22", "Indigo Pier Observatory", "Dark charred-wood geometric hut perched silently over indigo lake.", ColorFamily.BLUE, listOf(0xFF4769A8, 0xFFC2D4F5, 0xFF121C2E), listOf("architecture", "cabin", "indigo", "lake"), "Henrik S"),
                SeedSpec("23", "Alabaster Vault Ribs", "Symmetrical pale stone vault ribs converging serenely above quiet nave.", ColorFamily.MONOCHROME, listOf(0xFFAEB4BE, 0xFFF5F7FA, 0xFF232529), listOf("architecture", "vault", "symmetry", "stone"), "Liam Cole", true, DepthSubjectHint.TOP_ARCH)
            )
        )
    )

    // 6. MINIMALIST (+23)
    addAll(
        buildCategoryBatch(
            prefix = "minp8",
            category = WallpaperCategory.MINIMALIST,
            baseOrder = 1075,
            specs = listOf(
                SeedSpec("01", "Celadon Single Bowl", "Minimalist glazed celadon ceramic saucer balanced on pale matte linen.", ColorFamily.GREEN, listOf(0xFF7CA38C, 0xFFE6F0EA, 0xFF1E2B23), listOf("minimalist", "celadon", "ceramic", "zen"), "Kenji Mori", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("02", "Cobalt Single Cloud", "Tiny solitary white cumulus cloud drifting calmly across pure cobalt.", ColorFamily.BLUE, listOf(0xFF548AD1, 0xFFD9E9FA, 0xFF14243B), listOf("minimalist", "cloud", "cobalt", "sky"), "Oskar Holm"),
                SeedSpec("03", "Plaster Morning Arc", "Warm morning sunbeam arching delicately over quiet alabaster niche.", ColorFamily.WARM, listOf(0xFFE0CFC2, 0xFFFAF5F0, 0xFF332B26), listOf("minimalist", "plaster", "sunbeam", "niche"), "Nora Lind"),
                SeedSpec("04", "Charcoal Pebble Stack", "Three smooth river stones stacked in crisp monochrome gallery silence.", ColorFamily.MONOCHROME, listOf(0xFF636870, 0xFFE3E6EA, 0xFF191B1F), listOf("minimalist", "pebbles", "stack", "stone"), "Victor Ray", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("05", "Lavender Dune Seam", "Soft pastel lavender sky blending cleanly into frosted dusk horizon.", ColorFamily.PURPLE, listOf(0xFFA890CF, 0xFFEDE6FA, 0xFF241C33), listOf("minimalist", "lavender", "horizon", "dusk"), "Mina Park"),
                SeedSpec("06", "Apricot Glass Cylinder", "Frosted warm apricot glass cylinder casting soft translucent glow.", ColorFamily.ORANGE, listOf(0xFFE6A178, 0xFFFAE8DC, 0xFF362216), listOf("minimalist", "glass", "apricot", "cylinder"), "Camille Laurent"),
                SeedSpec("07", "Ice Horizon Post", "Single lone dark timber post rising calmly out of misty teal sea.", ColorFamily.COOL, listOf(0xFF75B0BA, 0xFFE3F3F5, 0xFF1B2E30), listOf("minimalist", "sea", "post", "mist"), "Mikkel Holm"),
                SeedSpec("08", "Sand Linen Pleat", "Clean horizontal fold across unbleached cream Belgian linen cloth.", ColorFamily.WARM, listOf(0xFFDCCBB8, 0xFFFAF5EE, 0xFF332D26), listOf("minimalist", "linen", "cream", "fold"), "Elena Conti", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Sage Fern Shadow", "Soft diffused botanical shadow resting subtly on pale sage plaster.", ColorFamily.GREEN, listOf(0xFF839E8C, 0xFFE8F0EB, 0xFF222B25), listOf("minimalist", "shadow", "sage", "botanical"), "Clara Lindqvist", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("10", "Indigo Paper Boat", "Crisp single folded white paper boat floating across calm indigo glass.", ColorFamily.BLUE, listOf(0xFF5278BD, 0xFFD6E3FA, 0xFF141E33), listOf("minimalist", "paperboat", "indigo", "calm"), "Sora Takahashi"),
                SeedSpec("11", "Silver Wire Circle", "Fine brushed silver loop balanced lightly against pale gray mist.", ColorFamily.MONOCHROME, listOf(0xFFA6ABB5, 0xFFF2F4F7, 0xFF26282B), listOf("minimalist", "silver", "circle", "wire"), "Julian Meyer"),
                SeedSpec("12", "Copper Sun Rim", "Thin warm copper semicircle peeking calmly behind matte ivory sheet.", ColorFamily.ORANGE, listOf(0xFFD98A5F, 0xFFF7E0D4, 0xFF331D12), listOf("minimalist", "copper", "sun", "ivory"), "Iris Reid"),
                SeedSpec("13", "Plum Mist Peak", "Single snowy mountain tip floating serenely above pastel plum haze.", ColorFamily.PURPLE, listOf(0xFF987AB8, 0xFFEBDCF7, 0xFF221A2E), listOf("minimalist", "peak", "plum", "mist"), "Lukas Meier", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("14", "Glacier Glass Edge", "Razor-clean pale cyan acrylic edge glowing softly in white studio.", ColorFamily.COOL, listOf(0xFF84C4CF, 0xFFECF8FA, 0xFF1F3136), listOf("minimalist", "acrylic", "cyan", "edge"), "Theo Sterling"),
                SeedSpec("15", "Blush Clay Arc", "Smooth curved blush pottery handle photographed against warm sand.", ColorFamily.WARM, listOf(0xFFE0AAA6, 0xFFFAF0EE, 0xFF362422), listOf("minimalist", "pottery", "blush", "arc"), "Chloe Parker"),
                SeedSpec("16", "Periwinkle Mist Lake", "Glassy periwinkle surface broken solely by one faint ripple ring.", ColorFamily.BLUE, listOf(0xFF7596D4, 0xFFE3EBFA, 0xFF1C2538), listOf("minimalist", "lake", "ripple", "periwinkle"), "Henrik S"),
                SeedSpec("17", "Matcha Bamboo Node", "Single crisp vertical bamboo node resting calmly in diffused green light.", ColorFamily.GREEN, listOf(0xFF6B9978, 0xFFDCEBE0, 0xFF1B2B20), listOf("minimalist", "bamboo", "matcha", "zen"), "Kenji Sato", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("18", "Alabaster Light Bar", "Single soft angled warm beam gliding quietly across matte white wall.", ColorFamily.MONOCHROME, listOf(0xFFD6D9DE, 0xFFFAFBFD, 0xFF2B2C30), listOf("minimalist", "light", "white", "beam"), "Nora Lind", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Saffron Paper Kite", "Tiny distant ochre kite suspended calmly against clear warm morning sky.", ColorFamily.ORANGE, listOf(0xFFE0A863, 0xFFFAEBD4, 0xFF332512), listOf("minimalist", "kite", "saffron", "sky"), "Hana Kim"),
                SeedSpec("20", "Lilac Frosted Vase", "Tall slender frosted lilac glass vase lit cleanly from behind.", ColorFamily.PURPLE, listOf(0xFFA68CD1, 0xFFEDE6FA, 0xFF241D33), listOf("minimalist", "vase", "lilac", "frosted"), "Vera Lin"),
                SeedSpec("21", "Arctic Sea Fog", "Endless tranquil silver-cyan fog dissolving horizon line completely.", ColorFamily.COOL, listOf(0xFF8CB8C4, 0xFFEAF5F7, 0xFF223033), listOf("minimalist", "arctic", "fog", "cyan"), "Freja Solberg"),
                SeedSpec("22", "Peach Plaster Step", "Soft matte peach stair edge catching calm late-afternoon glow.", ColorFamily.WARM, listOf(0xFFE6AC96, 0xFFFAF0EB, 0xFF36241E), listOf("minimalist", "peach", "stair", "matte"), "Lorenzo Bellini"),
                SeedSpec("23", "Cobalt Moon Sliver", "Delicate pale silver crescent moon resting inside serene cobalt dusk.", ColorFamily.BLUE, listOf(0xFF527EC4, 0xFFD9E6FA, 0xFF142136), listOf("minimalist", "moon", "crescent", "cobalt"), "Liam Cole")
            )
        )
    )

    // 7. AUTOMOTIVE (+23)
    addAll(
        buildCategoryBatch(
            prefix = "autop8",
            category = WallpaperCategory.AUTOMOTIVE,
            baseOrder = 1100,
            specs = listOf(
                SeedSpec("01", "Dolomite Hairpin Coupe", "Matte silver coupe poised cleanly beside sunlit limestone mountain parapet.", ColorFamily.COOL, listOf(0xFF7D94A8, 0xFFE3ECF5, 0xFF1F272E), listOf("automotive", "dolomites", "coupe", "silver"), "Lukas Meier", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("02", "Carbon Vent Macro", "Precision woven dry-carbon side intake blade lit in dark studio bay.", ColorFamily.MONOCHROME, listOf(0xFF3C4047, 0xFFB0B5BF, 0xFF121317), listOf("automotive", "carbon", "macro", "intake"), "Marcus Vance"),
                SeedSpec("03", "Sienna Classic Rallye", "Heritage warm sienna coupe parked quietly beneath Tuscany cypresses.", ColorFamily.WARM, listOf(0xFFC98768, 0xFFF5D8CC, 0xFF331F16), listOf("automotive", "tuscany", "classic", "sienna"), "Lorenzo Bellini"),
                SeedSpec("04", "Sapphire Prototype Wing", "Gloss sapphire endurance wing sculpting specular light across pit wall.", ColorFamily.BLUE, listOf(0xFF4273C2, 0xFFB8D4FA, 0xFF111F36), listOf("automotive", "endurance", "wing", "sapphire"), "Theo Sterling"),
                SeedSpec("05", "Emerald Pebble Tourer", "Deep emerald metallic hood shoulder reflecting overhead oak canopy.", ColorFamily.GREEN, listOf(0xFF427358, 0xFFB0D4BF, 0xFF13261B), listOf("automotive", "emerald", "tourer", "hood"), "Arthur Vance"),
                SeedSpec("06", "Papaya Aero Splitter", "Vibrant papaya-orange front aero fence slicing cleanly above wet tarmac.", ColorFamily.ORANGE, listOf(0xFFE8864D, 0xFFF7CFAF, 0xFF361D0D), listOf("automotive", "papaya", "splitter", "track"), "Felix Grant"),
                SeedSpec("07", "Violet Synth Fastback", "Retro-futuristic satin plum fastback glowing softly under violet bay tubes.", ColorFamily.PURPLE, listOf(0xFF8C66BA, 0xFFE0CCF5, 0xFF20152E), listOf("automotive", "fastback", "plum", "synth"), "Mina Park"),
                SeedSpec("08", "Titanium Exhaust Tips", "Heat-blued titanium quad exhaust trumpets framed symmetrically in matte black.", ColorFamily.COOL, listOf(0xFF5D8AA8, 0xFFD9C4A6, 0xFF1A2026), listOf("automotive", "titanium", "exhaust", "macro"), "Stefan K", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Arctic White Coupe", "Matte pearl-white electric grand tourer crossing silent glacier bridge.", ColorFamily.MONOCHROME, listOf(0xFFC2C6CE, 0xFFF7F8FA, 0xFF26282B), listOf("automotive", "ev", "glacier", "white"), "Freja Solberg"),
                SeedSpec("10", "Desert Dusk Roadster", "Open-top brushed-aluminum roadster overlooking warm orange sandstone mesas.", ColorFamily.WARM, listOf(0xFFD99A72, 0xFFF7DEC9, 0xFF332014), listOf("automotive", "roadster", "desert", "dusk"), "Elena Brooks"),
                SeedSpec("11", "Cobalt Wet Tarmac", "Twin crisp cobalt daytime blades mirrored across dark midnight asphalt.", ColorFamily.BLUE, listOf(0xFF4C7EC7, 0xFFBBD6FA, 0xFF121E33), listOf("automotive", "night", "rain", "cobalt"), "Alex Mercer"),
                SeedSpec("12", "Olive Pine Defender", "Boxy matte olive expedition wagon parked quietly inside misty forest.", ColorFamily.GREEN, listOf(0xFF577355, 0xFFC2D4BF, 0xFF192418), listOf("automotive", "defender", "forest", "olive"), "Callum Fraser"),
                SeedSpec("13", "Amber Tunnel Speed", "Motion-blurred warm amber tunnel lights streaking over sleek dark coupe.", ColorFamily.ORANGE, listOf(0xFFE08D4C, 0xFFF7D1B2, 0xFF331B0B), listOf("automotive", "tunnel", "speed", "amber"), "Sora Takahashi"),
                SeedSpec("14", "Amethyst Hangar Wing", "Sleek smoked-amethyst carbon hypercar silhouette inside concrete hangar.", ColorFamily.PURPLE, listOf(0xFF7B5EA3, 0xFFD4C4EB, 0xFF1C1429), listOf("automotive", "hypercar", "hangar", "amethyst"), "Dante Cross"),
                SeedSpec("15", "Brushed Aluminum Cockpit", "Minimalist machined aluminum gauge cluster glowing calmly at twilight.", ColorFamily.MONOCHROME, listOf(0xFF9EA4AE, 0xFFEBECEE, 0xFF212326), listOf("automotive", "cockpit", "gauges", "aluminum"), "Oskar Holm"),
                SeedSpec("16", "Seafoam Classic Coastline", "Pastel seafoam heritage coupe parked calmly above sunlit cliffside.", ColorFamily.COOL, listOf(0xFF6BB5BA, 0xFFD9F2F5, 0xFF182E30), listOf("automotive", "classic", "coast", "seafoam"), "Chloe Parker"),
                SeedSpec("17", "Champagne Tailfin Dusk", "Curved champagne-gold classic tailfin catching soft golden hour glow.", ColorFamily.WARM, listOf(0xFFD9BA91, 0xFFF7ECE0, 0xFF33291E), listOf("automotive", "classic", "champagne", "gold"), "Camille Laurent"),
                SeedSpec("18", "Indigo Pit Telemetry", "Glowing indigo pitwall screens silhouetting sleek carbon rear wing.", ColorFamily.BLUE, listOf(0xFF4F72C4, 0xFFC4D4FA, 0xFF121B33), listOf("automotive", "motorsport", "indigo", "pitlane"), "Max Weber", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Sage Fog Fastback", "Muted sage grand tourer emerging smoothly from coastal morning fog.", ColorFamily.GREEN, listOf(0xFF70947D, 0xFFD9E8DF, 0xFF1D2921), listOf("automotive", "fastback", "sage", "fog"), "Nora Lind"),
                SeedSpec("20", "Copper Salt Streamliner", "Brushed copper land-speed racer casting crisp shadow on salt flat.", ColorFamily.ORANGE, listOf(0xFFD6845C, 0xFFF7D1C0, 0xFF331B10), listOf("automotive", "streamliner", "salt", "copper"), "Evan Cole", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("21", "Lavender Neon Garage", "Polished dark fender curve catching calm pastel violet neon tubes.", ColorFamily.PURPLE, listOf(0xFF9474C9, 0xFFE6DAF7, 0xFF211830), listOf("automotive", "garage", "lavender", "neon"), "Hana Kim"),
                SeedSpec("22", "Charcoal Wheel Lock", "Matte gunmetal forged centerlock hub framed sharply against slate.", ColorFamily.MONOCHROME, listOf(0xFF565B63, 0xFFD1D5DC, 0xFF16181C), listOf("automotive", "wheel", "centerlock", "gunmetal"), "Zane Reid"),
                SeedSpec("23", "Glacier Blue Overpass", "Clean electric coupe gliding beneath icy cyan elevated highway arches.", ColorFamily.COOL, listOf(0xFF66A6C4, 0xFFDDF0FA, 0xFF172933), listOf("automotive", "overpass", "glacier", "ev"), "Lucas Weber")
            )
        )
    )

    // 8. SPACE (+23)
    addAll(
        buildCategoryBatch(
            prefix = "spcp8",
            category = WallpaperCategory.SPACE,
            baseOrder = 1125,
            specs = listOf(
                SeedSpec("01", "Saturn Silver Crescent", "Hairline sunlit icy crescent of Saturn floating peacefully over rings.", ColorFamily.COOL, listOf(0xFF8AAEC4, 0xFFEAF3FA, 0xFF1A242B), listOf("space", "saturn", "crescent", "rings"), "Cassini Lab", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("02", "Carina Rose Spire", "Towering rose-amber interstellar dust spire silhouetted in deep starlight.", ColorFamily.WARM, listOf(0xFFD9908A, 0xFFF7D9D6, 0xFF261717), listOf("space", "carina", "nebula", "spire"), "JWST Curated"),
                SeedSpec("03", "Cobalt Orbital Dawn", "Brilliant cobalt atmospheric arc hugging sleeping cloud continents.", ColorFamily.BLUE, listOf(0xFF4C88D4, 0xFFC7E0FA, 0xFF112138), listOf("space", "earth", "orbit", "cobalt"), "ISS Archive", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("04", "Mint Emission Veil", "Fine mint hydrogen filaments weaving silently through obsidian cosmos.", ColorFamily.GREEN, listOf(0xFF54B894, 0xFFC9F5E3, 0xFF122920), listOf("space", "nebula", "mint", "filaments"), "Hubble Studio"),
                SeedSpec("05", "Amethyst Spiral Dust", "Tilted pastel violet spiral galaxy floating smoothly amidst distant suns.", ColorFamily.PURPLE, listOf(0xFF9572CF, 0xFFE6D9FA, 0xFF1F1630), listOf("space", "galaxy", "amethyst", "spiral"), "Vera Rubin"),
                SeedSpec("06", "Amber Io Horizon", "Sulfur-amber volcanic crescent moon drifting cleanly past shadow band.", ColorFamily.ORANGE, listOf(0xFFE69654, 0xFFFCE0C5, 0xFF2E1A0C), listOf("space", "io", "amber", "moon"), "Juno Mission"),
                SeedSpec("07", "Lunar Mare Serenity", "Crisp monochrome Tycho rays fanning brightly across ancient crater plains.", ColorFamily.MONOCHROME, listOf(0xFF8C929E, 0xFFEDEFF2, 0xFF141518), listOf("space", "lunar", "moon", "tycho"), "Artemis Lens", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("08", "Cyan Triton Geysers", "Pale frosted nitrogen plains venting wispy cyan plumes into black sky.", ColorFamily.COOL, listOf(0xFF6EBCC7, 0xFFD9F5F7, 0xFF162C2E), listOf("space", "triton", "ice", "cyan"), "Voyager Craft", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Blush Lagoon Nebula", "Soft blush-pink stellar clouds illuminating newborn cerulean star knots.", ColorFamily.WARM, listOf(0xFFE098A4, 0xFF6B99CC, 0xFF261B21), listOf("space", "lagoon", "blush", "nebula"), "Atacama Array"),
                SeedSpec("10", "Indigo Starlight Halo", "Deep indigo reflection cloud encircling bright sapphire binary pair.", ColorFamily.BLUE, listOf(0xFF4A73BF, 0xFFBBD1FA, 0xFF111C33), listOf("space", "binary", "indigo", "stars"), "Kepler View"),
                SeedSpec("11", "Emerald Coma Trail", "Translucent emerald comet coma glowing delicately among northern stars.", ColorFamily.GREEN, listOf(0xFF4CA87F, 0xFFBDEBD5, 0xFF11291E), listOf("space", "comet", "emerald", "stars"), "Palomar Sky"),
                SeedSpec("12", "Copper Mars Polar", "Swirling cinnamon dust curls lapping white polar carbon-dioxide ice.", ColorFamily.ORANGE, listOf(0xFFD6865E, 0xFFF5D6C6, 0xFF331B12), listOf("space", "mars", "polar", "copper"), "Viking Lens"),
                SeedSpec("13", "Plum Pulsar Nebula", "Soft violet relativistic mist swirling cleanly around quiet pulsar core.", ColorFamily.PURPLE, listOf(0xFF8A68BA, 0xFFE0D1F5, 0xFF1E142E), listOf("space", "pulsar", "plum", "cosmos"), "Chandra Art"),
                SeedSpec("14", "Silver Asteroid Belt", "Sunlit pale silicate monoliths drifting calmly against starry obsidian.", ColorFamily.MONOCHROME, listOf(0xFF7D828C, 0xFFE6E8EC, 0xFF15161A), listOf("space", "asteroid", "silver", "belt"), "Osiris Survey"),
                SeedSpec("15", "Teal Ice Ring Gap", "Razor-sharp turquoise ice ring ringlets backlit gently in shadow.", ColorFamily.COOL, listOf(0xFF56B5BD, 0xFFCFF2F5, 0xFF132A2E), listOf("space", "rings", "teal", "ice"), "Cassini Eye"),
                SeedSpec("16", "Peach Sunset Exoplanet", "Warm apricot clouds drifting lazily across distant rocky horizon.", ColorFamily.WARM, listOf(0xFFE0A482, 0xFFF7E0D4, 0xFF332117), listOf("space", "exoplanet", "peach", "horizon"), "Kepler Exo"),
                SeedSpec("17", "Sapphire Milky Arch", "Crystal sapphire star clouds curving gracefully over dark orbital limb.", ColorFamily.BLUE, listOf(0xFF477BC4, 0xFFBED8FA, 0xFF111E33), listOf("space", "milkyway", "sapphire", "night"), "Mauna Kea"),
                SeedSpec("18", "Seafoam Supernova Arc", "Luminous seafoam shockwave shell expanding smoothly into calm night.", ColorFamily.GREEN, listOf(0xFF5EBFA1, 0xFFCFF5EA, 0xFF142E26), listOf("space", "supernova", "seafoam", "arc"), "Hubble Mosaic", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Tangerine Corona Loop", "Radiant golden-orange solar arches glowing cleanly above eclipse black.", ColorFamily.ORANGE, listOf(0xFFE8914D, 0xFFFCD9BE, 0xFF2B170A), listOf("space", "corona", "solar", "orange"), "Helios Optics"),
                SeedSpec("20", "Lilac Starburst Veil", "Dreamy lilac cosmic gas framing pinpoint blue-white stellar cluster.", ColorFamily.PURPLE, listOf(0xFF9F84D1, 0xFFEDE6FA, 0xFF221A33), listOf("space", "lilac", "cluster", "veil"), "La Palma Lens"),
                SeedSpec("21", "Charcoal Crater Rim", "Stark sunlit rim of deep lunar shadow crater etched cleanly in gray.", ColorFamily.MONOCHROME, listOf(0xFF6B7078, 0xFFE0E2E6, 0xFF141518), listOf("space", "crater", "moon", "monochrome"), "Artemis Lens", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("22", "Glacier Aurora Orbit", "Pastel ice-blue aurora ribbons shimmering cleanly beneath dark stars.", ColorFamily.COOL, listOf(0xFF6CB2CC, 0xFFE0F3FA, 0xFF162A33), listOf("space", "aurora", "glacier", "orbit"), "Expedition 70"),
                SeedSpec("23", "Coral Nebula Heart", "Soft rose-coral gas heart glowing steadily inside deep starry space.", ColorFamily.WARM, listOf(0xFFDE8E88, 0xFFF7DAD8, 0xFF2E1A18), listOf("space", "coral", "nebula", "heart"), "Astro Pastel")
            )
        )
    )

    // 9. ANIME & ARTISTIC (+23)
    addAll(
        buildCategoryBatch(
            prefix = "anip8",
            category = WallpaperCategory.ANIME_ARTISTIC,
            baseOrder = 1150,
            specs = listOf(
                SeedSpec("01", "Sakura Canal Boat", "Painterly pastel blossoms drifting quietly over sunlit turquoise canal.", ColorFamily.WARM, listOf(0xFFE3A1AD, 0xFF6BAEC2, 0xFF291D21), listOf("anime", "sakura", "canal", "pastel"), "Sora Takahashi", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("02", "Summer Nimbus Hilltop", "Towering hand-painted white cumulus clouds rising over cobalt hilltop.", ColorFamily.BLUE, listOf(0xFF568CD4, 0xFFEBF4FC, 0xFF15253B), listOf("anime", "clouds", "summer", "hilltop"), "Kenji Sato", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("03", "Moss Greenhouse Tea", "Cozy illustrated sunlit greenhouse table surrounded by soft ferns.", ColorFamily.GREEN, listOf(0xFF5A8A6B, 0xFFCFE3D4, 0xFF18291E), listOf("artistic", "greenhouse", "cozy", "tea"), "Clara Reid"),
                SeedSpec("04", "Violet Sky Train", "Dreamlike animated commuter train gliding silently across twilight sea.", ColorFamily.PURPLE, listOf(0xFF8E74C9, 0xFFE3D9F7, 0xFF1F1830), listOf("anime", "train", "twilight", "sea"), "Mina Park"),
                SeedSpec("05", "Amber Fox Shrine", "Warm glowing paper lanterns lining mossy stone steps at dusk.", ColorFamily.ORANGE, listOf(0xFFE08D54, 0xFFF7D4BC, 0xFF331C0F), listOf("anime", "shrine", "lanterns", "fox"), "Hana Kim"),
                SeedSpec("06", "Monochrome Ink Cliff", "Sweeping sumi-e charcoal brushstrokes evoking misty cliffside pines.", ColorFamily.MONOCHROME, listOf(0xFF757A82, 0xFFEDEFF2, 0xFF1C1E21), listOf("artistic", "ink", "sumie", "pine"), "Kenji Mori"),
                SeedSpec("07", "Cyan Beach Pier", "Painterly pastel cyan ocean swells washing calmly beneath wooden dock.", ColorFamily.COOL, listOf(0xFF64B5C2, 0xFFD9F3F7, 0xFF162D30), listOf("anime", "beach", "pier", "cyan"), "Iris Reid"),
                SeedSpec("08", "Gouache Dusk Harbor", "Warm textured gouache harbor sailboats lit gently by apricot sunset.", ColorFamily.WARM, listOf(0xFFE09E82, 0xFFF7E1D6, 0xFF33211B), listOf("artistic", "gouache", "harbor", "boats"), "Camille Laurent", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Indigo Rooftop Telescope", "Illustrated midnight balcony framing crisp constellations and glowing city.", ColorFamily.BLUE, listOf(0xFF4C72B8, 0xFFC7DAFA, 0xFF131E33), listOf("anime", "balcony", "night", "telescope"), "Luna Hayes"),
                SeedSpec("10", "Celadon Bamboo Walkway", "Sun-dappled emerald bamboo grove painted cleanly in luminous watercolor.", ColorFamily.GREEN, listOf(0xFF569E7B, 0xFFC9EDE0, 0xFF14291E), listOf("artistic", "watercolor", "bamboo", "grove"), "Clara Lindqvist"),
                SeedSpec("11", "Tangerine Hilltop Wind", "Breeze-swept sunlit amber grass flowing below clean blue morning sky.", ColorFamily.ORANGE, listOf(0xFFE39D59, 0xFFFAE4C8, 0xFF332110), listOf("anime", "hilltop", "grass", "breeze"), "Sora Takahashi"),
                SeedSpec("12", "Amethyst Lantern Lake", "Floating pastel violet sky lanterns mirroring over quiet mountain lake.", ColorFamily.PURPLE, listOf(0xFF9370CC, 0xFFE6D6FA, 0xFF201730), listOf("anime", "lanterns", "lake", "amethyst"), "Mina Park", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("13", "Silver Pencil Torii", "Fine architectural graphite drawing of misty cedar gate above valley.", ColorFamily.MONOCHROME, listOf(0xFF8C9099, 0xFFEBECEE, 0xFF202226), listOf("artistic", "pencil", "torii", "graphite"), "Nora Lind"),
                SeedSpec("14", "Glacier Pastel Bay", "Dreamlike seafoam-blue mountain cove painted in smooth gouache planes.", ColorFamily.COOL, listOf(0xFF70AEC4, 0xFFE0F2F7, 0xFF182A30), listOf("artistic", "gouache", "cove", "glacier"), "Freja Solberg"),
                SeedSpec("15", "Rose Flower Shop", "Warm sunlit pastel storefront overflowing gently with illustrated blooms.", ColorFamily.WARM, listOf(0xFFDFA1A8, 0xFFFAE6E8, 0xFF362124), listOf("anime", "flower", "shop", "pastel"), "Elena Conti"),
                SeedSpec("16", "Cerulean Kite Field", "High summer coastal grass dotted with drifting pale cerulean paper kites.", ColorFamily.BLUE, listOf(0xFF5C91CC, 0xFFD9EBFA, 0xFF162436), listOf("anime", "kites", "coastal", "sky"), "Chloe Parker"),
                SeedSpec("17", "Forest Spirit Pond", "Luminous emerald fireflies hovering silently over moss-rimmed sanctuary.", ColorFamily.GREEN, listOf(0xFF4A8563, 0xFFBBE3CC, 0xFF13261B), listOf("anime", "fireflies", "forest", "pond"), "Kenji Sato"),
                SeedSpec("18", "Copper Library Loft", "Warm sunset light streaming into cozy illustrated wooden book loft.", ColorFamily.ORANGE, listOf(0xFFD98A5C, 0xFFF7D9C4, 0xFF331D12), listOf("artistic", "library", "loft", "sunset"), "Hana Kim", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Lilac Rain Umbrella", "Pastel lilac vinyl umbrella glowing calmly beneath rainy cherry trees.", ColorFamily.PURPLE, listOf(0xFFA388CF, 0xFFEDE6FA, 0xFF241C33), listOf("anime", "rain", "umbrella", "lilac"), "Sora Takahashi", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("20", "Charcoal Woodcut Moon", "Crisp modern woodblock print of full silver moon behind cloud drift.", ColorFamily.MONOCHROME, listOf(0xFF70757E, 0xFFE6E8EC, 0xFF1A1B1F), listOf("artistic", "woodcut", "moon", "clouds"), "Kenji Mori"),
                SeedSpec("21", "Aquamarine Glass Boat", "Small painted rowboat floating effortlessly over glowing aqua reef.", ColorFamily.COOL, listOf(0xFF54B8BF, 0xFFCFF3F5, 0xFF132B2E), listOf("artistic", "boat", "aqua", "reef"), "Iris Reid"),
                SeedSpec("22", "Peach Sunset Stairs", "Sun-drenched pastel coastal village steps glowing warm peach at dusk.", ColorFamily.WARM, listOf(0xFFE09E84, 0xFFF7E0D6, 0xFF36211A), listOf("anime", "stairs", "coastal", "peach"), "Camille Laurent"),
                SeedSpec("23", "Cobalt Paper Lanterns", "Glowing indigo and white lanterns swaying over calm midnight river.", ColorFamily.BLUE, listOf(0xFF4E78C2, 0xFFC7DCFA, 0xFF132036), listOf("anime", "lanterns", "river", "indigo"), "Luna Hayes")
            )
        )
    )

    // 10. CINEMATIC (+23)
    addAll(
        buildCategoryBatch(
            prefix = "cinp8",
            category = WallpaperCategory.CINEMATIC,
            baseOrder = 1175,
            specs = listOf(
                SeedSpec("01", "Anamorphic Pier Mist", "35mm twilight mist drifting past glowing warm wooden boardwalk lamps.", ColorFamily.WARM, listOf(0xFFD9987E, 0xFF52667D, 0xFF1E1A1E), listOf("cinematic", "pier", "35mm", "mist"), "Alex Mercer", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("02", "Cobalt Subway Rain", "Wet elevated train platform gleaming cobalt under night drizzle.", ColorFamily.BLUE, listOf(0xFF4A7AC2, 0xFFBDD6FA, 0xFF121E33), listOf("cinematic", "subway", "rain", "cobalt"), "Theo Sterling"),
                SeedSpec("03", "Sodium Tunnel Echo", "Warm amber tunnel arches curving smoothly into moody night fog.", ColorFamily.ORANGE, listOf(0xFFE0924E, 0xFFF7D6B8, 0xFF331D0B), listOf("cinematic", "tunnel", "amber", "fog"), "Mateo Navarro"),
                SeedSpec("04", "Emerald Neon Storefront", "Quiet wet sidewalk reflecting luminous emerald botanical shop sign.", ColorFamily.GREEN, listOf(0xFF478C6B, 0xFFBAE0CE, 0xFF11261C), listOf("cinematic", "neon", "emerald", "night"), "Hana Kim"),
                SeedSpec("05", "Noir Harbor Crane", "Minimalist monochrome dockyard silhouette standing in thick Atlantic fog.", ColorFamily.MONOCHROME, listOf(0xFF737882, 0xFFE3E6EB, 0xFF1A1B1F), listOf("cinematic", "harbor", "noir", "fog"), "Oskar Holm", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("06", "Violet Rooftop Steam", "Pastel purple twilight steam rising softly past quiet rooftop vents.", ColorFamily.PURPLE, listOf(0xFF9174C4, 0xFFE3D9F7, 0xFF201830), listOf("cinematic", "rooftop", "steam", "violet"), "Mina Park"),
                SeedSpec("07", "Cyan Icebreaker Wake", "Arctic research vessel cutting clean cyan trail across misty pack ice.", ColorFamily.COOL, listOf(0xFF5EA8BF, 0xFFD4F0F7, 0xFF152A30), listOf("cinematic", "arctic", "ship", "cyan"), "Freja Solberg"),
                SeedSpec("08", "Golden Diner Booth", "Soft 65mm golden afternoon shadows sliding quietly across vinyl booth.", ColorFamily.WARM, listOf(0xFFD99E72, 0xFFF7E1CE, 0xFF332216), listOf("cinematic", "diner", "65mm", "golden"), "Elena Conti", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Sapphire Alpine Tram", "Aerial cable car suspended quietly inside vast cobalt alpine valley fog.", ColorFamily.BLUE, listOf(0xFF4E7EC4, 0xFFC4DCFA, 0xFF132036), listOf("cinematic", "cablecar", "alps", "cobalt"), "Lukas Meier", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("10", "Forest Train Crossing", "Moody emerald woodland rail signal glowing amber in misty twilight.", ColorFamily.GREEN, listOf(0xFF4E785E, 0xFFE0AE75, 0xFF15241B), listOf("cinematic", "railway", "forest", "mist"), "Noah Bennett"),
                SeedSpec("11", "Tangerine Highway Dust", "Warm film flare spilling brightly across empty southwestern highway curve.", ColorFamily.ORANGE, listOf(0xFFE38E59, 0xFFF7D4BE, 0xFF331D0F), listOf("cinematic", "highway", "flare", "desert"), "Chloe Parker"),
                SeedSpec("12", "Plum Jazz Alley", "Soft plum neon glow spilling onto damp cobblestones at midnight.", ColorFamily.PURPLE, listOf(0xFF835E9E, 0xFFDCC7EB, 0xFF1E1426), listOf("cinematic", "alley", "plum", "night"), "Camille Laurent"),
                SeedSpec("13", "Silver Cinema Hall", "Symmetrical vintage monochrome velvet seats bathed in projector haze.", ColorFamily.MONOCHROME, listOf(0xFF7D828C, 0xFFE6E8EC, 0xFF1B1C20), listOf("cinematic", "cinema", "seats", "mono"), "Victor Ray"),
                SeedSpec("14", "Teal Coastal Highway", "Pacific mist curling over empty teal seaside concrete guardrails.", ColorFamily.COOL, listOf(0xFF5DA3A8, 0xFFD4F0F2, 0xFF15292B), listOf("cinematic", "highway", "teal", "coast"), "Liam Cole"),
                SeedSpec("15", "Rose Window Curtains", "Late sunset blush shadows undulating gently across sheer studio curtains.", ColorFamily.WARM, listOf(0xFFDEA19E, 0xFFFAE6E5, 0xFF332120), listOf("cinematic", "curtains", "blush", "sunset"), "Iris Reid"),
                SeedSpec("16", "Indigo Observatory Night", "Crisp indigo dome silhouetted quietly under sweeping starry timelapse.", ColorFamily.BLUE, listOf(0xFF476EB5, 0xFFC2D6FA, 0xFF121C30), listOf("cinematic", "observatory", "stars", "indigo"), "Kenji Sato"),
                SeedSpec("17", "Jade Lantern Courtyard", "Solitary mossy courtyard glowing serenely through jade evening drizzle.", ColorFamily.GREEN, listOf(0xFF4D856B, 0xFFC2E3D4, 0xFF14261E), listOf("cinematic", "courtyard", "jade", "rain"), "Kenji Mori"),
                SeedSpec("18", "Amber Desert Overpass", "Warm sodium light pools illuminating quiet concrete canyon overpass.", ColorFamily.ORANGE, listOf(0xFFD98E56, 0xFFF7D4BA, 0xFF331C0F), listOf("cinematic", "overpass", "amber", "night"), "Elena Brooks", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Lavender Subway Car", "Soft pastel violet reflections glazing quiet midnight subway glass.", ColorFamily.PURPLE, listOf(0xFF947AC4, 0xFFE3D9F5, 0xFF20192E), listOf("cinematic", "subway", "lavender", "glass"), "Sora Takahashi"),
                SeedSpec("20", "Graphite Fog Pier", "Lonely dark wooden pilings receding calmly into endless silver mist.", ColorFamily.MONOCHROME, listOf(0xFF6B7078, 0xFFE0E2E6, 0xFF191B1F), listOf("cinematic", "pier", "fog", "graphite"), "Mikkel Holm"),
                SeedSpec("21", "Arctic Ice Fog", "Moody frozen fjord light cutting softly through pale cyan sea smoke.", ColorFamily.COOL, listOf(0xFF6BA6BF, 0xFFDDF0F7, 0xFF182930), listOf("cinematic", "fjord", "cyan", "smoke"), "Lucas Weber"),
                SeedSpec("22", "Apricot Lighthouse Dusk", "Warm sunset halo wrapping white coastal tower above dark ocean.", ColorFamily.WARM, listOf(0xFFE09E7E, 0xFF5D7594, 0xFF241D1D), listOf("cinematic", "lighthouse", "apricot", "dusk"), "Callum Fraser"),
                SeedSpec("23", "Cobalt Rain Window", "City bokeh lights melting peacefully behind rain-beaded studio glass.", ColorFamily.BLUE, listOf(0xFF4F80C4, 0xFFC4DCFA, 0xFF142036), listOf("cinematic", "rain", "bokeh", "cobalt"), "Vera Lin")
            )
        )
    )

    // 11. DEPTH STUDIO (+23, ALL supportsDepth = true, portrait 9:16/2:3)
    addAll(
        buildCategoryBatch(
            prefix = "depp8",
            category = WallpaperCategory.DEPTH_STUDIO,
            baseOrder = 1200,
            specs = listOf(
                SeedSpec("01", "Alabaster Column Depth", "Fluted ivory travertine column rising cleanly for behind-clock depth.", ColorFamily.WARM, listOf(0xFFDCD0C4, 0xFFFAF6F2, 0xFF2E2924), listOf("depth", "column", "alabaster", "architecture"), "Lorenzo Bellini", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("02", "Matterhorn Dawn Peak", "Crisp backlit alpine pyramid rising cleanly beneath clear indigo sky.", ColorFamily.BLUE, listOf(0xFF5B88C7, 0xFFD9E8FA, 0xFF162236), listOf("depth", "matterhorn", "peak", "alpine"), "Lukas Meier", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("03", "Fern Frond Emerald Depth", "Single backlit emerald fern arch framing quiet lock-screen numerals.", ColorFamily.GREEN, listOf(0xFF47825E, 0xFFC2E3CE, 0xFF12261A), listOf("depth", "fern", "botanical", "emerald"), "Clara Lindqvist", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("04", "Frosted Glass Arch Depth", "Translucent ribbed glass portal glowing gently against cool slate void.", ColorFamily.COOL, listOf(0xFF6EB0C4, 0xFFE3F4F7, 0xFF182A30), listOf("depth", "arch", "frosted", "glass"), "Kuro Studio", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("05", "Amethyst Crystal Spire", "Faceted translucent purple quartz rising sharply into pastel lavender haze.", ColorFamily.PURPLE, listOf(0xFF936FD4, 0xFFE8DCF7, 0xFF201533), listOf("depth", "crystal", "amethyst", "spire"), "Mina Park", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("06", "Amber Heron Silhouette", "Graceful sunlit heron standing calmly along pale apricot marsh sky.", ColorFamily.ORANGE, listOf(0xFFE09866, 0xFFF7DEC8, 0xFF332012), listOf("depth", "heron", "wildlife", "silhouette"), "Noah Bennett", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("07", "Silver Basalt Sea Stack", "Dramatic monochrome sea pillar emerging cleanly from glassy mist.", ColorFamily.MONOCHROME, listOf(0xFF8C929C, 0xFFEDEFF2, 0xFF1D1F24), listOf("depth", "seastack", "silver", "minimal"), "Oskar Holm", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("08", "Blush Magnolia Branch", "Sculptural pastel pink magnolia blossoms reaching smoothly across cream.", ColorFamily.WARM, listOf(0xFFE3A4AD, 0xFFFAF0F2, 0xFF362225), listOf("depth", "magnolia", "blush", "botanical"), "Iris Reid", true, DepthSubjectHint.BOTANICAL_STEM, tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Cobalt Lighthouse Beacon", "Tall white coastal tower soaring sharply into crisp sapphire twilight.", ColorFamily.BLUE, listOf(0xFF4A7BC4, 0xFFC4DCFA, 0xFF122038), listOf("depth", "lighthouse", "cobalt", "tower"), "Mikkel Holm", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("10", "Sage Ceramic Amphora", "Matte sage stoneware vase poised centrally inside soft window shadows.", ColorFamily.GREEN, listOf(0xFF759682, 0xFFE0ECE4, 0xFF1E2922), listOf("depth", "amphora", "sage", "ceramic"), "Nora Lind", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("11", "Sienna Canyon Pinnacle", "Towering sunlit terracotta rock needle framing open morning desert sky.", ColorFamily.ORANGE, listOf(0xFFD9825A, 0xFFF5D4C4, 0xFF331B12), listOf("depth", "canyon", "pinnacle", "terracotta"), "Elena Brooks", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("12", "Violet Lotus Silhouette", "Backlit deep-violet lotus stem rising gracefully over dark misted pond.", ColorFamily.PURPLE, listOf(0xFF855CA8, 0xFFDCC9ED, 0xFF1E1429), listOf("depth", "lotus", "violet", "stem"), "Hana Kim", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("13", "Glacier Blue Crevasse", "Crisp translucent cyan ice ridge cutting upwards into clear arctic light.", ColorFamily.COOL, listOf(0xFF66ACC7, 0xFFE0F3FA, 0xFF162A30), listOf("depth", "glacier", "ridge", "cyan"), "Freja Solberg", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("14", "Charcoal Pebble Tower", "Balanced monochrome cairn stones framed cleanly against pale fog.", ColorFamily.MONOCHROME, listOf(0xFF6B707A, 0xFFE6E8EC, 0xFF191B1F), listOf("depth", "cairn", "stones", "zen"), "Victor Ray", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("15", "Rose Terracotta Arch", "Warm curved rose-clay doorway framing pale afternoon desert sky.", ColorFamily.WARM, listOf(0xFFD9988E, 0xFFF7E0DC, 0xFF331F1C), listOf("depth", "arch", "rose", "clay"), "Camille Laurent", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("16", "Periwinkle Sail Fin", "Sleek racing yacht spinnaker crest rising boldly against cool sky.", ColorFamily.BLUE, listOf(0xFF668BD4, 0xFFDCE6FA, 0xFF182338), listOf("depth", "yacht", "sail", "periwinkle"), "Liam Cole", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("17", "Mint Bamboo Stalks", "Crisp pale mint bamboo silhouettes standing upright in diffused fog.", ColorFamily.GREEN, listOf(0xFF5EB592, 0xFFD4F2E6, 0xFF152B22), listOf("depth", "bamboo", "mint", "botanical"), "Kenji Sato", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("18", "Copper Dune Peak", "Razor-sharp sunlit copper dune crest dividing orange light from shade.", ColorFamily.ORANGE, listOf(0xFFE08C53, 0xFFF7D6BF, 0xFF361D0D), listOf("depth", "dune", "copper", "peak"), "Tariq Mansour", true, DepthSubjectHint.PEAK_HORIZON, tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Lavender Paper Torus", "Sculptural pastel violet paper ring suspended neatly in clean studio.", ColorFamily.PURPLE, listOf(0xFF9F82CC, 0xFFEDE6FA, 0xFF231B33), listOf("depth", "torus", "lavender", "paper"), "Vera Lin", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("20", "Teal Wave Lip Depth", "Curling translucent seafoam wave lip poised under clean pastel sky.", ColorFamily.COOL, listOf(0xFF56B0BA, 0xFFD4F2F5, 0xFF142A2E), listOf("depth", "wave", "teal", "ocean"), "Chloe Parker", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("21", "Ivory Origami Crane", "Crisp matte ivory folded crane perched smoothly in calm studio light.", ColorFamily.MONOCHROME, listOf(0xFFC2C6CE, 0xFFF7F8FA, 0xFF26282B), listOf("depth", "origami", "crane", "ivory"), "Kenji Mori", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("22", "Champagne Arch Horizon", "Minimalist warm sandstone arch opening onto glassy twilight sea.", ColorFamily.WARM, listOf(0xFFD9BE9C, 0xFFF7ECE0, 0xFF332B21), listOf("depth", "arch", "champagne", "sea"), "Mateo Navarro", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("23", "Indigo Owl Silhouette", "Crisp nocturnal snowy owl silhouette poised against midnight blue moon.", ColorFamily.BLUE, listOf(0xFF4C72B8, 0xFFD9E6FA, 0xFF131D30), listOf("depth", "owl", "indigo", "silhouette"), "Arthur Vance", true, DepthSubjectHint.CENTER_PORTRAIT)
            )
        )
    )
}
