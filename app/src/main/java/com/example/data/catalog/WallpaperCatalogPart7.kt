package com.example.data.catalog

import com.example.data.model.ColorFamily
import com.example.data.model.DepthSubjectHint
import com.example.data.model.ResolutionTier
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperCategory

internal fun buildCatalogPartSeven(): List<Wallpaper> = buildList {
    // 1. NATURE (+23)
    addAll(
        buildCategoryBatch(
            prefix = "natp7",
            category = WallpaperCategory.NATURE,
            baseOrder = 650,
            specs = listOf(
                SeedSpec("01", "Banff Emerald Ridge", "Glacier-fed turquoise tarn nestled below crisp jagged alpine ridges.", ColorFamily.GREEN, listOf(0xFF478F82, 0xFFB8DFD8, 0xFF152925), listOf("nature", "banff", "lake", "alpine"), "Clara Reid", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("02", "Patagonia Granite Dawn", "Rose-lit granite spires emerging cleanly from blue valley mist.", ColorFamily.WARM, listOf(0xFFD9958B, 0xFF6B85A6, 0xFF1E2430), listOf("nature", "patagonia", "peaks", "dawn"), "Mateo Silva", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("03", "Lofoten Driftwood Bay", "Arctic white sand cove bordered by calm midnight-blue mountain walls.", ColorFamily.BLUE, listOf(0xFF547EA8, 0xFFD9E3ED, 0xFF162230), listOf("nature", "lofoten", "coast", "arctic"), "Soren Holm"),
                SeedSpec("04", "Arashiyama Bamboo Mist", "Tall emerald bamboo stalks rising quietly into morning fog.", ColorFamily.GREEN, listOf(0xFF4A7D56, 0xFFADD1B3, 0xFF142618), listOf("nature", "bamboo", "forest", "kyoto"), "Kenji Mori", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("05", "Namib Apricot Dunes", "Knife-edge desert dunes splitting sunlit apricot from deep indigo shadow.", ColorFamily.ORANGE, listOf(0xFFE08C53, 0xFF475773, 0xFF261810), listOf("nature", "desert", "dunes", "shadows"), "Tariq Mansour"),
                SeedSpec("06", "Skye Basalt Falls", "Silky waterfall ribbons pouring over dark volcanic basalt steps.", ColorFamily.COOL, listOf(0xFF6E8BA3, 0xFFD9E6F0, 0xFF19222B), listOf("nature", "waterfall", "scotland", "basalt"), "Callum Fraser"),
                SeedSpec("07", "Tuscany Cypress Mist", "Rolling golden hills punctuated by solitary morning cypress lines.", ColorFamily.WARM, listOf(0xFFC9A679, 0xFFF2E2CC, 0xFF2E261B), listOf("nature", "hills", "tuscany", "morning"), "Elena Conti"),
                SeedSpec("08", "Iceland Glacier Tongue", "Crisp cerulean ice caves carved gently inside volcanic ash glaciers.", ColorFamily.BLUE, listOf(0xFF5D9BD4, 0xFFE0F0FC, 0xFF16283B), listOf("nature", "glacier", "ice", "iceland"), "Freja Solberg", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Olympic Moss Cathedral", "Velvet green moss draping ancient cedar limbs in quiet coastal fog.", ColorFamily.GREEN, listOf(0xFF4D6B48, 0xFFAABFA5, 0xFF1A2418), listOf("nature", "moss", "rainforest", "cedar"), "Noah Bennett"),
                SeedSpec("10", "lavender Provence Row", "Soft violet bloom rows stretching gently toward pale amber hills.", ColorFamily.PURPLE, listOf(0xFF8F74BF, 0xFFE3D6FA, 0xFF231B33), listOf("nature", "lavender", "meadow", "summer"), "Camille Laurent"),
                SeedSpec("11", "Salar Mirror Horizon", "Cloud reflections doubled seamlessly across glassy highland salt flats.", ColorFamily.COOL, listOf(0xFF8AAEC9, 0xFFF0F5FA, 0xFF222C36), listOf("nature", "saltflats", "mirror", "clouds"), "Diego Vargas"),
                SeedSpec("12", "Monochrome Sea Stacks", "Minimalist silver long-exposure waves wrapping distant coastal monoliths.", ColorFamily.MONOCHROME, listOf(0xFF8B9099, 0xFFE6E8EC, 0xFF1C1E22), listOf("nature", "seastacks", "ocean", "minimal"), "Oskar Lind"),
                SeedSpec("13", "Redwood Sunbeams", "Warm golden sun shafts slicing down through coastal redwood fog.", ColorFamily.ORANGE, listOf(0xFFD49257, 0xFF476348, 0xFF211C14), listOf("nature", "redwood", "sunbeams", "forest"), "Arthur Vance", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("14", "Crater Lake Sapphire", "Still deep sapphire caldera waters beneath frost-rimmed pine cliffs.", ColorFamily.BLUE, listOf(0xFF3E6BA8, 0xFFACC6E8, 0xFF121F33), listOf("nature", "lake", "sapphire", "caldera"), "Hannah Park"),
                SeedSpec("15", "Sakura Petal Creek", "Blush cherry blossoms drifting calmly over dark slate mountain streams.", ColorFamily.WARM, listOf(0xFFE3A1AD, 0xFF5A636E, 0xFF291D21), listOf("nature", "sakura", "spring", "creek"), "Sora Takahashi"),
                SeedSpec("16", "Faroe Cliff Waterfall", "Sea mist rising gracefully past emerald coastal cliffs above dark swells.", ColorFamily.GREEN, listOf(0xFF577D68, 0xFFC2D6CB, 0xFF17241D), listOf("nature", "faroe", "cliffs", "ocean"), "Mikkel Holm"),
                SeedSpec("17", "Antelope Sandstone Ribbon", "Sculpted terracotta canyon waves catching soft overhead noon glow.", ColorFamily.ORANGE, listOf(0xFFD97852, 0xFFF7C9B5, 0xFF361B10), listOf("nature", "canyon", "sandstone", "texture"), "Chloe Brooks"),
                SeedSpec("18", "Boreal Birch Frost", "Slender silver birch trunks veiled in delicate crisp hoarfrost.", ColorFamily.MONOCHROME, listOf(0xFFB5BAC2, 0xFFF5F7FA, 0xFF26292E), listOf("nature", "birch", "winter", "frost"), "Vera Lin", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Amalfi Pine Cove", "Deep seafoam coves tucked beneath fragrant coastal stone pines.", ColorFamily.COOL, listOf(0xFF569AA6, 0xFFC9EBF0, 0xFF152C30), listOf("nature", "mediterranean", "cove", "turquoise"), "Lorenzo Bellini"),
                SeedSpec("20", "Dusk Tundra Aurora", "Soft violet and mint aurora curtains drifting above snowy hills.", ColorFamily.PURPLE, listOf(0xFF8373C7, 0xFF7AD4B3, 0xFF171A29), listOf("nature", "aurora", "tundra", "night"), "Henrik Solberg"),
                SeedSpec("21", "Highland Heather Ridge", "Muted plum heather slopes rolling under diffused Scottish overcast.", ColorFamily.PURPLE, listOf(0xFF8F6B8C, 0xFFDFCFE0, 0xFF261B26), listOf("nature", "highlands", "heather", "mist"), "Isla Campbell"),
                SeedSpec("22", "Mangrove Mirror Dawn", "Still warm coral skies mirrored neatly around slender coastal roots.", ColorFamily.WARM, listOf(0xFFE09E87, 0xFF596E78, 0xFF241D1D), listOf("nature", "mangrove", "reflection", "dawn"), "Maya Nair"),
                SeedSpec("23", "Alps Starry Col", "Clear alpine pass arcing directly toward faint Milky Way clouds.", ColorFamily.BLUE, listOf(0xFF4E6F9E, 0xFFD1DDF0, 0xFF121A29), listOf("nature", "stars", "alps", "night"), "Lukas Meier", true, DepthSubjectHint.PEAK_HORIZON)
            )
        )
    )

    // 2. ABSTRACT (+23)
    addAll(
        buildCategoryBatch(
            prefix = "absp7",
            category = WallpaperCategory.ABSTRACT,
            baseOrder = 675,
            specs = listOf(
                SeedSpec("01", "Silk Opaline Pleats", "Translucent opaline silk ribbons folding effortlessly in warm studio haze.", ColorFamily.WARM, listOf(0xFFE3C2B8, 0xFFF7EAE6, 0xFF332623), listOf("abstract", "silk", "opaline", "folds"), "Iris Reid"),
                SeedSpec("02", "Cobalt Glass Torus", "Smooth refracted cobalt glass loop floating inside soft gray fog.", ColorFamily.BLUE, listOf(0xFF4F7FC9, 0xFFB8D4FA, 0xFF152238), listOf("abstract", "glass", "cobalt", "3d"), "Kuro Studio", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("03", "Sage Travertine Waves", "Carved matte sage relief undulations washed with side sunlight.", ColorFamily.GREEN, listOf(0xFF7FA38D, 0xFFDCE8DF, 0xFF212E26), listOf("abstract", "sage", "relief", "matte"), "Nora Lind"),
                SeedSpec("04", "Apricot Velvet Gradient", "Smooth airbrushed apricot and rose aura fading into quiet dusk.", ColorFamily.ORANGE, listOf(0xFFE6966E, 0xFFFAD8C5, 0xFF382014), listOf("abstract", "gradient", "apricot", "aura"), "Sora Takahashi"),
                SeedSpec("05", "Amethyst Liquid Prism", "Thick pearlescent violet glass waves bending crisp spectrum highlights.", ColorFamily.PURPLE, listOf(0xFF936FD1, 0xFFE3D4FA, 0xFF211636), listOf("abstract", "prism", "amethyst", "liquid"), "Mina Park"),
                SeedSpec("06", "Obsidian Brushed Arcs", "Concentric graphite satin rings catching faint silver edge gleams.", ColorFamily.MONOCHROME, listOf(0xFF474A52, 0xFFB0B5BF, 0xFF121317), listOf("abstract", "obsidian", "rings", "graphite"), "Victor Ray"),
                SeedSpec("07", "Cyan Frosted Flutes", "Vertical ribbed frosted glass distorting soft aqua daylight.", ColorFamily.COOL, listOf(0xFF6BB5C2, 0xFFD9F2F7, 0xFF1A3036), listOf("abstract", "fluted", "frosted", "cyan"), "Theo Sterling"),
                SeedSpec("08", "Blush Clay Terraces", "Layered terracotta and blush matte clay discs in soft shadow.", ColorFamily.WARM, listOf(0xFFD99582, 0xFFF7DED6, 0xFF36201A), listOf("abstract", "clay", "terracotta", "layers"), "Camille Laurent", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Indigo Ferrofluid Spikes", "Sculptural dark indigo magnetic curves catching pale sky reflections.", ColorFamily.BLUE, listOf(0xFF4763AD, 0xFF9FB8ED, 0xFF121930), listOf("abstract", "ferrofluid", "indigo", "sculpt"), "Julian Meyer"),
                SeedSpec("10", "Mint Alabaster Veil", "Paper-thin mint translucent sheets suspended in calm diffused light.", ColorFamily.GREEN, listOf(0xFF79C4A3, 0xFFDDF7EC, 0xFF1B3328), listOf("abstract", "mint", "alabaster", "veil"), "Clara Lindqvist"),
                SeedSpec("11", "Saffron Chromatic Mist", "Warm saffron and cream diffused color fields glowing softly.", ColorFamily.ORANGE, listOf(0xFFE3A854, 0xFFFAE8C8, 0xFF36250E), listOf("abstract", "saffron", "colorfield", "glow"), "Dante Cross"),
                SeedSpec("12", "Lilac Bubble Refract", "Clustered lavender soap-film domes echoing soft pastel reflections.", ColorFamily.PURPLE, listOf(0xFFAE8CE0, 0xFFECE0FA, 0xFF281D38), listOf("abstract", "pastel", "lilac", "refract"), "Hana Kim", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("13", "Slate Limestone Grain", "Quiet horizontal sediment striations across velvety slate gray.", ColorFamily.MONOCHROME, listOf(0xFF787C85, 0xFFD1D5DC, 0xFF1D1E21), listOf("abstract", "slate", "texture", "minimal"), "Oskar Holm"),
                SeedSpec("14", "Iceberg Crystal Facets", "Sharp translucent glacier glass planes refracting cool cyan light.", ColorFamily.COOL, listOf(0xFF75B0D1, 0xFFE3F3FC, 0xFF1B2E38), listOf("abstract", "crystal", "facets", "ice"), "Freja Solberg"),
                SeedSpec("15", "Coral Plaster Swirl", "Smooth Venetian plaster swirls sculpted in warm peach and cream.", ColorFamily.WARM, listOf(0xFFDE8B78, 0xFFF7DDD6, 0xFF381F1A), listOf("abstract", "plaster", "coral", "swirl"), "Elena Conti"),
                SeedSpec("16", "Periwinkle Satin Wave", "Gentle periwinkle and silver silk contours drifting diagonally.", ColorFamily.BLUE, listOf(0xFF7A94D6, 0xFFDFE6FA, 0xFF1D2438), listOf("abstract", "periwinkle", "satin", "wave"), "Lucas Weber"),
                SeedSpec("17", "Forest Velvet Layers", "Deep pine and moss felt contours stacked cleanly in matte shadow.", ColorFamily.GREEN, listOf(0xFF47735B, 0xFF9EC2AD, 0xFF14241B), listOf("abstract", "forest", "velvet", "layers"), "Mikkel Holm"),
                SeedSpec("18", "Plum Smoked Acrylic", "Smoked plum acrylic sheets overlapping with soft rim glow.", ColorFamily.PURPLE, listOf(0xFF835EA3, 0xFFD9C7E8, 0xFF21162B), listOf("abstract", "acrylic", "plum", "translucent"), "Vera Lin", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Copper Foil Crinkle", "Macro metallic rose-copper foil folds reflecting warm sunset hues.", ColorFamily.ORANGE, listOf(0xFFD6845C, 0xFFF7CFC0, 0xFF331B10), listOf("abstract", "copper", "foil", "macro"), "Stefan K"),
                SeedSpec("20", "Ivory Paper Ribbons", "Delicately curled architectural ivory paper strips casting soft shadows.", ColorFamily.MONOCHROME, listOf(0xFFD6D8DC, 0xFFFAFBFD, 0xFF2B2C30), listOf("abstract", "paper", "ivory", "shadow"), "Nora Lind"),
                SeedSpec("21", "Glacier Ink Bloom", "Diluted cerulean ink billowing slowly through crystal water.", ColorFamily.COOL, listOf(0xFF5D9AC2, 0xFFD4ECF7, 0xFF152936), listOf("abstract", "ink", "cerulean", "bloom"), "Kenji Sato"),
                SeedSpec("22", "Rose Quartz Monolith", "Soft translucent blush quartz crystal polished into organic curves.", ColorFamily.WARM, listOf(0xFFDFA3AD, 0xFFFAE6EA, 0xFF382327), listOf("abstract", "quartz", "rose", "organic"), "Chloe Parker", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("23", "Teal Kinetic Ribbons", "Weightless teal gradients twisting calmly against pale mist backdrop.", ColorFamily.GREEN, listOf(0xFF4EA69E, 0xFFC5F0EB, 0xFF132E2B), listOf("abstract", "teal", "kinetic", "ribbon"), "Liam Cole")
            )
        )
    )

    // 3. AMOLED (+23)
    addAll(
        buildCategoryBatch(
            prefix = "amop7",
            category = WallpaperCategory.AMOLED,
            baseOrder = 700,
            specs = listOf(
                SeedSpec("01", "OLED Cyan Laser Ring", "Single crisp cyan halo suspended cleanly in pure #000000 void.", ColorFamily.COOL, listOf(0xFF000000, 0xFF5CE1E6, 0xFF0D2426), listOf("amoled", "oled", "cyan", "ring"), "Kuro Lab", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("02", "Pitch Ember Horizon", "Thin ember-orange horizon arc glowing softly above true black.", ColorFamily.ORANGE, listOf(0xFF000000, 0xFFE87A48, 0xFF2B1308), listOf("amoled", "ember", "horizon", "black"), "Dorian Gray"),
                SeedSpec("03", "Obsidian Violet Eclipse", "Delicate amethyst crescent rimmed around a pure black disk.", ColorFamily.PURPLE, listOf(0xFF000000, 0xFFA378E6, 0xFF1C122E), listOf("amoled", "violet", "eclipse", "minimal"), "Mina Park", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("04", "True-Black Emerald Silk", "Translucent emerald ribbon drifting narrowly across true OLED black.", ColorFamily.GREEN, listOf(0xFF000000, 0xFF44BF88, 0xFF0B2418), listOf("amoled", "emerald", "silk", "ribbon"), "Zane Reid"),
                SeedSpec("05", "Sapphire Core Pulse", "Minimalist electric blue ripple fading swiftly into zero-pixel black.", ColorFamily.BLUE, listOf(0xFF000000, 0xFF4D88E8, 0xFF0D1B33), listOf("amoled", "sapphire", "pulse", "dark"), "Alex Mercer"),
                SeedSpec("06", "Monochrome Silver Thread", "Razor-thin silver wireframe arc curving gracefully in pitch darkness.", ColorFamily.MONOCHROME, listOf(0xFF000000, 0xFFD9DDE3, 0xFF1A1B1F), listOf("amoled", "silver", "wireframe", "minimal"), "Victor Ray"),
                SeedSpec("07", "Rose Gold Void Orbit", "Warm rose-gold planetary thin line circling silently in true black.", ColorFamily.WARM, listOf(0xFF000000, 0xFFE09E8B, 0xFF291A16), listOf("amoled", "rosegold", "orbit", "line"), "Iris Reid"),
                SeedSpec("08", "OLED Coral Neon Drop", "Single glowing coral droplet falling across deep obsidian backdrop.", ColorFamily.WARM, listOf(0xFF000000, 0xFFE87878, 0xFF2E1212), listOf("amoled", "coral", "drop", "glow"), "Hana Kim", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Black Jade Botanical", "Single backlit jade fern leaf traced in crisp gold against #000000.", ColorFamily.GREEN, listOf(0xFF000000, 0xFF5CA378, 0xFF102418), listOf("amoled", "botanical", "jade", "leaf"), "Clara Reid", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("10", "Indigo Prism Sliver", "Narrow refractive indigo shard splitting cool blue light in darkness.", ColorFamily.BLUE, listOf(0xFF000000, 0xFF638AE6, 0xFF111B33), listOf("amoled", "prism", "indigo", "shard"), "Theo Sterling"),
                SeedSpec("11", "Amber Filament Arc", "Glowing warm tungsten filament spiral floating on pitch black canvas.", ColorFamily.ORANGE, listOf(0xFF000000, 0xFFF0A256, 0xFF2E1D0C), listOf("amoled", "filament", "amber", "spiral"), "Julian Meyer"),
                SeedSpec("12", "OLED Mint Frequency", "Fine mint audio waveform rippling cleanly across pitch black center.", ColorFamily.GREEN, listOf(0xFF000000, 0xFF64D4A8, 0xFF0E291E), listOf("amoled", "mint", "wave", "minimal"), "Kenji Sato"),
                SeedSpec("13", "Lavender Mist Crescent", "Soft lavender haze crescent cradling pure black lock-screen center.", ColorFamily.PURPLE, listOf(0xFF000000, 0xFFC5A3FA, 0xFF221833), listOf("amoled", "lavender", "crescent", "oled"), "Luna Hayes"),
                SeedSpec("14", "Charcoal Dune Rim", "Razor-fine silver rim tracing dark sand ridge over true black sky.", ColorFamily.MONOCHROME, listOf(0xFF000000, 0xFF9EA4B0, 0xFF17181C), listOf("amoled", "dune", "monochrome", "ridge"), "Oskar Holm", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("15", "Turquoise Void Ripple", "Concentric turquoise droplets echoing outward into zero-pixel black.", ColorFamily.COOL, listOf(0xFF000000, 0xFF4EC2CC, 0xFF0E292B), listOf("amoled", "turquoise", "ripple", "dark"), "Sora Takahashi"),
                SeedSpec("16", "Crimson Velvet Curve", "Deep crimson silk arc emerging subtly from pure obsidian shadow.", ColorFamily.WARM, listOf(0xFF000000, 0xFFD4556B, 0xFF2B0E14), listOf("amoled", "crimson", "velvet", "curve"), "Dante Cross"),
                SeedSpec("17", "Cobalt Star Trail", "Crisp cobalt polar star trails circling cleanly in pitch night black.", ColorFamily.BLUE, listOf(0xFF000000, 0xFF5D94E0, 0xFF101D30), listOf("amoled", "startrail", "cobalt", "night"), "Orion Survey"),
                SeedSpec("18", "Gold Leaf Void Crack", "Kintsugi gold hairline veining gently across matte obsidian.", ColorFamily.ORANGE, listOf(0xFF000000, 0xFFE3B868, 0xFF291F0C), listOf("amoled", "kintsugi", "gold", "minimal"), "Kenji Mori", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Plum Glass Sphere", "Dim backlit plum glass orb glowing at base of true black canvas.", ColorFamily.PURPLE, listOf(0xFF000000, 0xFF9966CC, 0xFF1B1026), listOf("amoled", "orb", "plum", "glass"), "Vera Lin"),
                SeedSpec("20", "Arctic Ice Sliver", "Crisp pale ice-blue crystal line suspended vertically in black space.", ColorFamily.COOL, listOf(0xFF000000, 0xFF9CD4ED, 0xFF13262E), listOf("amoled", "ice", "sliver", "minimal"), "Freja Solberg"),
                SeedSpec("21", "Neon Lime Contour", "Fine lime topographic lines fading gracefully into OLED black margins.", ColorFamily.GREEN, listOf(0xFF000000, 0xFF7BE082, 0xFF122B14), listOf("amoled", "topo", "lime", "contour"), "Max Weber"),
                SeedSpec("22", "Pure Ink Pebble", "Single backlit wet basalt pebble floating on pitch black mirror.", ColorFamily.MONOCHROME, listOf(0xFF000000, 0xFF8C929E, 0xFF16171A), listOf("amoled", "pebble", "stone", "zen"), "Nora Lind"),
                SeedSpec("23", "Peach Aura Halo", "Warm peach ring softly illuminating pure black lock-screen frame.", ColorFamily.WARM, listOf(0xFF000000, 0xFFF0A88C, 0xFF2E1B14), listOf("amoled", "peach", "halo", "glow"), "Camille Laurent")
            )
        )
    )

    // 4. FUTURISTIC (+23)
    addAll(
        buildCategoryBatch(
            prefix = "futp7",
            category = WallpaperCategory.FUTURISTIC,
            baseOrder = 725,
            specs = listOf(
                SeedSpec("01", "Orbital Ring Atrium", "Minimalist orbital station corridor bathed in clean cyan sky light.", ColorFamily.COOL, listOf(0xFF5CA8BF, 0xFFD9F2F7, 0xFF162B30), listOf("futuristic", "orbital", "atrium", "scifi"), "Kuro Lab", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("02", "Neo-Kyoto Rain Spire", "Holographic amber signage glowing softly over misty cyber spires.", ColorFamily.ORANGE, listOf(0xFFE08C53, 0xFF475973, 0xFF181A24), listOf("futuristic", "cyber", "kyoto", "rain"), "Sora Takahashi"),
                SeedSpec("03", "Quantum Lattice Core", "Precision cobalt optical grid pulsing inside dark server vaults.", ColorFamily.BLUE, listOf(0xFF4C7ED9, 0xFFB0CCFA, 0xFF111C33), listOf("futuristic", "quantum", "lattice", "cobalt"), "Theo Sterling"),
                SeedSpec("04", "Chrome Bio-Dome", "Geodesic glass biosphere glowing with lush emerald interior mist.", ColorFamily.GREEN, listOf(0xFF4EA67E, 0xFFC2EBDA, 0xFF13291F), listOf("futuristic", "biodome", "solarpunk", "green"), "Clara Reid"),
                SeedSpec("05", "Synthwave Violet Arch", "Refractive violet light portal hovering over reflective chrome floors.", ColorFamily.PURPLE, listOf(0xFF936CD9, 0xFFE3D1FA, 0xFF1D1430), listOf("futuristic", "portal", "violet", "chrome"), "Mina Park", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("06", "Stealth Carbon Wing", "Faceted matte stealth airframe resting cleanly in stark white hangar.", ColorFamily.MONOCHROME, listOf(0xFF454952, 0xFFE8EAEE, 0xFF15171A), listOf("futuristic", "stealth", "aerospace", "carbon"), "Zane Reid"),
                SeedSpec("07", "Solaris Mirror Array", "Geometric desert heliostat mirrors focusing golden beams skyward.", ColorFamily.WARM, listOf(0xFFD9A66C, 0xFFF7E3C6, 0xFF332414), listOf("futuristic", "solar", "array", "desert"), "Tariq Mansour"),
                SeedSpec("08", "Holo-Glass Console", "Floating translucent HUD panes gleaming gently in midnight studio.", ColorFamily.COOL, listOf(0xFF5CC4D1, 0xFFD1F7FA, 0xFF132A2E), listOf("futuristic", "hologram", "hud", "glass"), "Hana Kim", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Mars Terraforming Vault", "Pressurized translucent greenhouse dome overlooking crimson dunes.", ColorFamily.ORANGE, listOf(0xFFD97F57, 0xFF6B8575, 0xFF2E1A12), listOf("futuristic", "mars", "colony", "dome"), "Victor Ray"),
                SeedSpec("10", "Titanium Ion Thruster", "Blue xenon plasma glow radiating inside precision machined rings.", ColorFamily.BLUE, listOf(0xFF4C8BE6, 0xFFBDDCFF, 0xFF122138), listOf("futuristic", "thruster", "plasma", "ion"), "Orion Survey"),
                SeedSpec("11", "Nano-Carbon Weave", "Hyper-clean microscopic carbon nanotube geometry under soft rim light.", ColorFamily.MONOCHROME, listOf(0xFF5D626E, 0xFFD4D8E0, 0xFF16181C), listOf("futuristic", "nanotech", "carbon", "macro"), "Julian Meyer"),
                SeedSpec("12", "Bioluminescent Lab", "Suspended glass vessels glowing with soft mint algae luminescence.", ColorFamily.GREEN, listOf(0xFF54C49B, 0xFFCFF7E8, 0xFF122B22), listOf("futuristic", "biotech", "mint", "glow"), "Nora Lind"),
                SeedSpec("13", "Hyperloop Vacuum Tube", "Sleek brushed-aluminum transit corridor vanishing toward indigo light.", ColorFamily.PURPLE, listOf(0xFF7B6BC9, 0xFFD9D1FA, 0xFF19152E), listOf("futuristic", "hyperloop", "transit", "corridor"), "Dorian Gray"),
                SeedSpec("14", "Cryo Stasis Chamber", "Soft frosted white glass pods lit from beneath by cool cyan LEDs.", ColorFamily.COOL, listOf(0xFF73B5C9, 0xFFE6F7FC, 0xFF182A30), listOf("futuristic", "cryo", "white", "scifi"), "Vera Lin"),
                SeedSpec("15", "Solar Sail Horizon", "Paper-thin gold-mylar spacecraft sail unfurling above sunlit Earth.", ColorFamily.WARM, listOf(0xFFE0B872, 0xFF4C729E, 0xFF261E12), listOf("futuristic", "solarsail", "gold", "space"), "Cassini Lab"),
                SeedSpec("16", "Cybernetic Iris Lens", "Precision sapphire optical shutter rings illuminated cleanly in silver.", ColorFamily.BLUE, listOf(0xFF5282C9, 0xFFC7DCFA, 0xFF132036), listOf("futuristic", "optics", "lens", "sapphire"), "Liam Cole"),
                SeedSpec("17", "Emerald Matrix Monolith", "Translucent emerald glass slab etched with clean data glyphs.", ColorFamily.GREEN, listOf(0xFF479E75, 0xFFB8EBCD, 0xFF12291E), listOf("futuristic", "monolith", "emerald", "data"), "Kenji Mori"),
                SeedSpec("18", "Plasma Ribbon Spire", "Twisted rose-violet magnetic containment beam inside clean dark dome.", ColorFamily.PURPLE, listOf(0xFFA86ED4, 0xFFEACCF7, 0xFF231430), listOf("futuristic", "plasma", "fusion", "violet"), "Alex Mercer", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Maglev Dusk Bridge", "Floating white magnetic monorail gliding silently above sunset haze.", ColorFamily.ORANGE, listOf(0xFFE39464, 0xFF6B7894, 0xFF2B1C14), listOf("futuristic", "maglev", "dusk", "bridge"), "Max Weber"),
                SeedSpec("20", "Aether Ceramic Drone", "Sleek white porcelain-finish autonomous wing floating over mist.", ColorFamily.MONOCHROME, listOf(0xFFC2C6CE, 0xFFF7F8FA, 0xFF232529), listOf("futuristic", "drone", "ceramic", "minimal"), "Chloe Parker", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("21", "Subsea Research Ring", "Illuminated glass habitat nestled serenely across deep turquoise trench.", ColorFamily.COOL, listOf(0xFF428FA3, 0xFFAEDCE8, 0xFF102429), listOf("futuristic", "underwater", "habitat", "ocean"), "Iris Reid"),
                SeedSpec("22", "Amber Photon Relay", "Architectural desert laser tower sending warm amber sky signals.", ColorFamily.WARM, listOf(0xFFDB9B6B, 0xFFF7DEC8, 0xFF332114), listOf("futuristic", "tower", "amber", "desert"), "Elena Brooks"),
                SeedSpec("23", "Zenith Orbital Loft", "Minimalist cantilevered space lounge framing curved cobalt planet light.", ColorFamily.BLUE, listOf(0xFF5988CF, 0xFFD9E6FA, 0xFF142136), listOf("futuristic", "loft", "orbit", "cobalt"), "Arthur Vance")
            )
        )
    )

    // 5. ARCHITECTURE (+23)
    addAll(
        buildCategoryBatch(
            prefix = "arcp7",
            category = WallpaperCategory.ARCHITECTURE,
            baseOrder = 750,
            specs = listOf(
                SeedSpec("01", "Oslo Opera Marble", "Sloping white Carrara marble roof plane meeting cold Nordic sky.", ColorFamily.MONOCHROME, listOf(0xFFD9DCE3, 0xFFF7F8FA, 0xFF292B30), listOf("architecture", "marble", "oslo", "minimal"), "Oskar Holm", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("02", "Warm Travertine Colonnade", "Sun-washed limestone arches casting crisp midday colonnade shadows.", ColorFamily.WARM, listOf(0xFFD9B89A, 0xFFF7EAE0, 0xFF362A20), listOf("architecture", "travertine", "arches", "sunlight"), "Lorenzo Bellini", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("03", "Kyoto Cedar Pavilion", "Slatted hinoki cedar facade framed over tranquil mossy reflection pond.", ColorFamily.GREEN, listOf(0xFF6B8568, 0xFFD4DECE, 0xFF1F261D), listOf("architecture", "cedar", "kyoto", "pavilion"), "Kenji Sato"),
                SeedSpec("04", "Cobalt Glass Atrium", "High-angle geometric blue glass atrium ribs converging symmetrically.", ColorFamily.BLUE, listOf(0xFF4B7FC2, 0xFFC2DCFA, 0xFF142338), listOf("architecture", "glass", "atrium", "cobalt"), "Julian Meyer"),
                SeedSpec("05", "Marrakech Ochre Wall", "Smooth dusty terracotta courtyard wall kissed by late afternoon triangle light.", ColorFamily.ORANGE, listOf(0xFFD98762, 0xFFF5D1BF, 0xFF381E14), listOf("architecture", "terracotta", "ochre", "courtyard"), "Tariq Mansour"),
                SeedSpec("06", "Brutalist Spiral Stair", "Sculptural poured-concrete spiral staircase curving into soft skylight.", ColorFamily.MONOCHROME, listOf(0xFF8C9099, 0xFFE6E8EC, 0xFF222326), listOf("architecture", "brutalist", "spiral", "concrete"), "Nora Lind"),
                SeedSpec("07", "Lavender Dusk Villa", "Cantilevered stucco pavilion glowing gently against twilight violet hills.", ColorFamily.PURPLE, listOf(0xFF8D78BA, 0xFFE0D9F2, 0xFF211B2E), listOf("architecture", "villa", "twilight", "stucco"), "Camille Laurent"),
                SeedSpec("08", "Cyan Steel Lattice", "Clean diagonal powder-coated cyan trusses against bright overcast sky.", ColorFamily.COOL, listOf(0xFF6BAEC2, 0xFFE0F2F7, 0xFF182C33), listOf("architecture", "steel", "lattice", "cyan"), "Max Weber", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Desert Rammed Earth", "Layered warm sandstone rammed-earth monolith rising from arid plains.", ColorFamily.WARM, listOf(0xFFCFA17C, 0xFFF5E1CE, 0xFF332418), listOf("architecture", "desert", "earth", "monolith"), "Elena Brooks"),
                SeedSpec("10", "Emerald Conservatory", "Victorian wrought-iron glasshouse framing lush tropical palm silhouettes.", ColorFamily.GREEN, listOf(0xFF56856B, 0xFFC9DED1, 0xFF182920), listOf("architecture", "conservatory", "glasshouse", "emerald"), "Arthur Vance", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("11", "Nordic Black Timber", "Charred shou-sugi-ban timber gable framed neatly in crisp winter snow.", ColorFamily.MONOCHROME, listOf(0xFF474A50, 0xFFEBECEE, 0xFF141517), listOf("architecture", "timber", "nordic", "snow"), "Mikkel Holm"),
                SeedSpec("12", "Sapphire Pool Loggia", "Minimalist concrete overhang framing calm indigo infinity water.", ColorFamily.BLUE, listOf(0xFF4772B5, 0xFFBAD3FA, 0xFF132036), listOf("architecture", "pool", "loggia", "minimal"), "Chloe Parker"),
                SeedSpec("13", "Sienna Cloister Light", "Sunlit curved corridor slicing clean geometric amber shadow bars.", ColorFamily.ORANGE, listOf(0xFFD68C58, 0xFFF7D9C2, 0xFF361F10), listOf("architecture", "cloister", "corridor", "light"), "Mateo Navarro"),
                SeedSpec("14", "Amethyst Museum Fin", "Translucent violet dichroic glass facade catching oblique sunset angles.", ColorFamily.PURPLE, listOf(0xFF916CC7, 0xFFE3D4F7, 0xFF211630), listOf("architecture", "dichroic", "museum", "violet"), "Mina Park"),
                SeedSpec("15", "Ice-Blue Canal Loft", "Clean white minimalist bridge and glazed studio hovering over still water.", ColorFamily.COOL, listOf(0xFF7CA7C4, 0xFFE6F0F7, 0xFF1D2933), listOf("architecture", "canal", "loft", "glass"), "Henrik S"),
                SeedSpec("16", "Coral Adobe Steps", "Stepped pastel coral adobe geometry silhouetted against desert blue.", ColorFamily.WARM, listOf(0xFFDE9182, 0xFF7FA1C2, 0xFF301E1D), listOf("architecture", "adobe", "coral", "steps"), "Diego Vargas"),
                SeedSpec("17", "Moss Concrete Courtyard", "Tactile board-formed concrete walls enclosing quiet bamboo garden.", ColorFamily.GREEN, listOf(0xFF697A6A, 0xFFD4DDD4, 0xFF202620), listOf("architecture", "concrete", "courtyard", "zen"), "Kenji Mori"),
                SeedSpec("18", "Monolith Library Vault", "Towering symmetrical white bookshelf galleries lit softly from skylights.", ColorFamily.MONOCHROME, listOf(0xFFAEB2BA, 0xFFF5F6F8, 0xFF26282B), listOf("architecture", "library", "symmetry", "white"), "Iris Reid", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Azure Waterfront Pier", "Sleek cantilevered wooden boardwalk jutting calmly over glassy blue bay.", ColorFamily.BLUE, listOf(0xFF5D92C4, 0xFFD6E8FA, 0xFF172738), listOf("architecture", "pier", "waterfront", "wood"), "Liam Cole"),
                SeedSpec("20", "Copper Patina Roof", "Aged seafoam-copper museum spire curves catching crisp overcast skies.", ColorFamily.COOL, listOf(0xFF65A69E, 0xFFD4ECE9, 0xFF192E2B), listOf("architecture", "copper", "patina", "spire"), "Freja Solberg", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("21", "Amber Brick Rotunda", "Curved handmade terracotta brick cylinder rising cleanly into dusk.", ColorFamily.ORANGE, listOf(0xFFC97852, 0xFFF0C9B6, 0xFF301A10), listOf("architecture", "brick", "rotunda", "amber"), "Lorenzo M"),
                SeedSpec("22", "Plum Twilight Conservatory", "Backlit dark steel mullions glowing softly violet at blue hour.", ColorFamily.PURPLE, listOf(0xFF7A639E, 0xFFD1C4E8, 0xFF1D1629), listOf("architecture", "bluehour", "steel", "violet"), "Vera Lin"),
                SeedSpec("23", "Alabaster Archway Echo", "Nested white plaster arches stepping smoothly into serene shadow.", ColorFamily.WARM, listOf(0xFFD9CFC7, 0xFFFAF6F2, 0xFF2E2A26), listOf("architecture", "arch", "alabaster", "plaster"), "Nora Lind", true, DepthSubjectHint.TOP_ARCH)
            )
        )
    )

    // 6. MINIMALIST (+23)
    addAll(
        buildCategoryBatch(
            prefix = "minp7",
            category = WallpaperCategory.MINIMALIST,
            baseOrder = 775,
            specs = listOf(
                SeedSpec("01", "Single Clay Vessel", "Matte ivory ceramic amphora resting cleanly against pale plaster horizon.", ColorFamily.WARM, listOf(0xFFDED2C8, 0xFFFAF6F2, 0xFF2E2926), listOf("minimalist", "ceramic", "vessel", "calm"), "Nora Lind", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("02", "Cobalt Horizon Line", "Two calm cerulean and slate planes meeting across crisp sea mist.", ColorFamily.BLUE, listOf(0xFF5B8BC4, 0xFFD9E6F7, 0xFF182538), listOf("minimalist", "horizon", "cobalt", "sea"), "Oskar Holm"),
                SeedSpec("03", "Sage Pebble Balance", "Two stacked river stones centered quietly in diffused pale sage.", ColorFamily.GREEN, listOf(0xFF839E8C, 0xFFE3ECE6, 0xFF222B25), listOf("minimalist", "stone", "zen", "sage"), "Kenji Sato"),
                SeedSpec("04", "Paper Shadow Slant", "Single crisp diagonal sunlit shadow falling across warm cream linen.", ColorFamily.WARM, listOf(0xFFE0CFC0, 0xFFFAF4ED, 0xFF332C26), listOf("minimalist", "shadow", "linen", "cream"), "Camille Laurent"),
                SeedSpec("05", "Obsidian Sphere Matte", "Matte charcoal stone sphere poised gently inside soft gray gradient.", ColorFamily.MONOCHROME, listOf(0xFF545861, 0xFFE3E5E8, 0xFF18191C), listOf("minimalist", "sphere", "matte", "charcoal"), "Victor Ray", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("06", "Lilac Mist Horizon", "Dreamy lavender mist erasing the seam between still water and sky.", ColorFamily.PURPLE, listOf(0xFFA892CC, 0xFFEDE6FA, 0xFF261E33), listOf("minimalist", "lilac", "mist", "water"), "Mina Park"),
                SeedSpec("07", "Apricot Sun Disk", "Clean geometric apricot circle floating in pale dawn negative space.", ColorFamily.ORANGE, listOf(0xFFE69D73, 0xFFFAE8DC, 0xFF362217), listOf("minimalist", "sun", "apricot", "geometric"), "Sora Takahashi"),
                SeedSpec("08", "Cyan Glass Sheet", "Single translucent aqua glass edge lit gently over white matte desk.", ColorFamily.COOL, listOf(0xFF7BC4CC, 0xFFEBF7F8, 0xFF1D3236), listOf("minimalist", "glass", "cyan", "edge"), "Theo Sterling", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Olive Leaf Negative", "One slender silver-olive branch tucked neatly at lower canvas margin.", ColorFamily.GREEN, listOf(0xFF7E9472, 0xFFE8EDE4, 0xFF242B20), listOf("minimalist", "olive", "botanical", "negative"), "Elena Conti", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("10", "Indigo Fog Buoy", "Tiny solitary white buoy floating calmly on endless indigo sea mist.", ColorFamily.BLUE, listOf(0xFF5472A8, 0xFFD9E3F5, 0xFF151E30), listOf("minimalist", "sea", "indigo", "solitary"), "Mikkel Holm"),
                SeedSpec("11", "Graphite Step Fold", "Clean single architectural crease in heavy textured charcoal paper.", ColorFamily.MONOCHROME, listOf(0xFF696D75, 0xFFDCDFE3, 0xFF1D1E21), listOf("minimalist", "fold", "paper", "graphite"), "Julian Meyer"),
                SeedSpec("12", "Blush Dusk Gradient", "Weightless transition from warm blush pink into quiet twilight gray.", ColorFamily.WARM, listOf(0xFFDEA6AD, 0xFFF7EAEC, 0xFF332325), listOf("minimalist", "blush", "gradient", "calm"), "Iris Reid"),
                SeedSpec("13", "Terracotta Cube", "Warm matte terracotta cube catching clean soft top-left studio light.", ColorFamily.ORANGE, listOf(0xFFD68663, 0xFFF5DBCF, 0xFF361E14), listOf("minimalist", "cube", "terracotta", "matte"), "Lorenzo Bellini"),
                SeedSpec("14", "Plum Silk Corner", "Subtle draped plum satin fold resting in negative cream space.", ColorFamily.PURPLE, listOf(0xFF9373AD, 0xFFEBE3F2, 0xFF241B2B), listOf("minimalist", "plum", "silk", "fold"), "Vera Lin"),
                SeedSpec("15", "Arctic Snow Drift", "Soft wind-carved curve of pure white snow against pale ice sky.", ColorFamily.COOL, listOf(0xFFB8CDD9, 0xFFF5F9FC, 0xFF263038), listOf("minimalist", "snow", "arctic", "curve"), "Freja Solberg"),
                SeedSpec("16", "Matcha Ceramic Bowl", "Single handmade celadon tea bowl lit cleanly from soft side window.", ColorFamily.GREEN, listOf(0xFF78A387, 0xFFE0EDE4, 0xFF1E2B22), listOf("minimalist", "tea", "celadon", "craft"), "Kenji Mori"),
                SeedSpec("17", "Slate Pillar Silence", "Lone pale gray cylindrical column emerging softly from misty pond.", ColorFamily.MONOCHROME, listOf(0xFF969BA3, 0xFFEDEFF2, 0xFF232529), listOf("minimalist", "column", "mist", "pond"), "Nora Lind"),
                SeedSpec("18", "Azure Window Square", "Clean sunlit sky blue square projected quietly on white studio wall.", ColorFamily.BLUE, listOf(0xFF7CA7D9, 0xFFEBF3FC, 0xFF1F2D3D), listOf("minimalist", "window", "sky", "square"), "Chloe Parker", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Peach Linen Drape", "Breeze-lifted sheer peach curtain diffusing quiet morning light.", ColorFamily.WARM, listOf(0xFFE6B098, 0xFFFAF0EB, 0xFF36251E), listOf("minimalist", "linen", "peach", "sheer"), "Camille Laurent"),
                SeedSpec("20", "Teal Horizon Pier", "Thin wooden dock fading gracefully into tranquil pale teal fog.", ColorFamily.COOL, listOf(0xFF69A6AD, 0xFFDCEEF0, 0xFF1A2D30), listOf("minimalist", "pier", "teal", "fog"), "Liam Cole"),
                SeedSpec("21", "Ochre Sand Ripple", "Gentle single wind ripple sweeping smoothly across warm beige dune.", ColorFamily.ORANGE, listOf(0xFFD9AE7E, 0xFFF7EBD9, 0xFF332617), listOf("minimalist", "sand", "ochre", "dune"), "Tariq Mansour"),
                SeedSpec("22", "Violet Glass Prism", "Tiny refractive violet prism casting soft pastel spectrum line.", ColorFamily.PURPLE, listOf(0xFF9F84D1, 0xFFEDE6FA, 0xFF241C33), listOf("minimalist", "prism", "violet", "light"), "Hana Kim"),
                SeedSpec("23", "Pure Ash Reed", "Delicate dried ash reed silhouetted quietly against pale fog.", ColorFamily.MONOCHROME, listOf(0xFFA6AAB2, 0xFFF2F4F7, 0xFF26282B), listOf("minimalist", "reed", "ash", "quiet"), "Oskar Holm", true, DepthSubjectHint.BOTANICAL_STEM)
            )
        )
    )

    // 7. AUTOMOTIVE (+23)
    addAll(
        buildCategoryBatch(
            prefix = "autop7",
            category = WallpaperCategory.AUTOMOTIVE,
            baseOrder = 800,
            specs = listOf(
                SeedSpec("01", "Monaco Harbor GT", "Satin slate grand tourer poised quietly along dawn yacht boardwalk.", ColorFamily.COOL, listOf(0xFF6D849C, 0xFFD9E3ED, 0xFF1B232B), listOf("automotive", "monaco", "gt", "dawn"), "Lorenzo Bellini", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("02", "Nardo Gray Aero Wing", "Clean matte Nardo-gray rear wing curve in crisp softbox light.", ColorFamily.MONOCHROME, listOf(0xFF8C929B, 0xFFE3E6EB, 0xFF202226), listOf("automotive", "aero", "nardo", "wing"), "Marcus Vance"),
                SeedSpec("03", "Kyoto Cedar Roadster", "Ivory open-top classic resting beneath glowing autumn temple maples.", ColorFamily.WARM, listOf(0xFFD9A37C, 0xFFF7E6D9, 0xFF362215), listOf("automotive", "roadster", "kyoto", "classic"), "Kenji Sato"),
                SeedSpec("04", "Cobalt Le Mans Fin", "Sculpted dorsal shark fin painted metallic cobalt under pit lights.", ColorFamily.BLUE, listOf(0xFF4273C2, 0xFFB0CCFA, 0xFF111F36), listOf("automotive", "lemans", "cobalt", "prototype"), "Theo Sterling"),
                SeedSpec("05", "Forest Rally Fog", "Widebody forest-green rally coupe pausing beside misty pines.", ColorFamily.GREEN, listOf(0xFF457056, 0xFFB0CEBD, 0xFF14241B), listOf("automotive", "rally", "forest", "fog"), "Mikkel Holm"),
                SeedSpec("06", "Copper Canyon Coupe", "Metallic burnt-copper fastback parked cleanly along red canyon rim.", ColorFamily.ORANGE, listOf(0xFFD67E52, 0xFFF5CBB5, 0xFF361B0E), listOf("automotive", "copper", "canyon", "fastback"), "Elena Brooks"),
                SeedSpec("07", "Violet Midnight Charger", "Sleek electric concept sedan glowing softly in pale purple charging bay.", ColorFamily.PURPLE, listOf(0xFF8368C4, 0xFFD9CCF5, 0xFF1E1630), listOf("automotive", "ev", "concept", "violet"), "Mina Park"),
                SeedSpec("08", "Ceramic Brake Rotor", "Macro view of cross-drilled carbon-ceramic rotor and anodized caliper.", ColorFamily.MONOCHROME, listOf(0xFF5D626B, 0xFFD1D5DC, 0xFF16181C), listOf("automotive", "brakes", "carbon", "macro"), "Stefan K", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Ice Lake Rally Studs", "Matte glacier-white rally hatch carving crisp arc across frozen bay.", ColorFamily.COOL, listOf(0xFF7CA7C9, 0xFFEBF4FA, 0xFF1D2C38), listOf("automotive", "ice", "rally", "snow"), "Freja Solberg"),
                SeedSpec("10", "Sienna Leather Louver", "Vintage tan leather bonnet strap gleaming gently in late afternoon.", ColorFamily.WARM, listOf(0xFFC98B5E, 0xFFF2D9C4, 0xFF332012), listOf("automotive", "vintage", "leather", "detail"), "Arthur Vance"),
                SeedSpec("11", "Azure Tunnel Light", "Continuous overhead cyan light strips stretching over sleek silver hood.", ColorFamily.BLUE, listOf(0xFF5288C9, 0xFFC7E0FA, 0xFF132236), listOf("automotive", "tunnel", "reflection", "blue"), "Alex Mercer"),
                SeedSpec("12", "Sage Atelier Concept", "Minimalist matte sage shooting-brake silhouette in sunlit loft.", ColorFamily.GREEN, listOf(0xFF74947F, 0xFFD9E6DD, 0xFF1F2B23), listOf("automotive", "shootingbrake", "sage", "studio"), "Nora Lind"),
                SeedSpec("13", "Tangerine Pit Lane", "Warm dusk pit lane reflections glinting across matte orange bodywork.", ColorFamily.ORANGE, listOf(0xFFE3854F, 0xFFF7CEB5, 0xFF361B0D), listOf("automotive", "pitlane", "orange", "dusk"), "Mateo Navarro"),
                SeedSpec("14", "Amethyst Glass Canopy", "Seamless tinted panoramic coupe dome reflecting twilight lavender clouds.", ColorFamily.PURPLE, listOf(0xFF8A72BA, 0xFFE0D4F7, 0xFF1F182E), listOf("automotive", "canopy", "glass", "twilight"), "Vera Lin"),
                SeedSpec("15", "Stealth Diffuser Fin", "Matte forged-carbon rear diffuser blades lit in stark side shadow.", ColorFamily.MONOCHROME, listOf(0xFF3C4047, 0xFFB0B5BF, 0xFF121317), listOf("automotive", "diffuser", "forged", "carbon"), "Zane Reid"),
                SeedSpec("16", "Pacific Fog Cabriolet", "Soft pastel cream classic cabriolet parked along misty coastal turnout.", ColorFamily.COOL, listOf(0xFF7E9BB0, 0xFFEBF2F7, 0xFF202A33), listOf("automotive", "cabriolet", "coast", "fog"), "Chloe Parker"),
                SeedSpec("17", "Champagne Fender Arc", "Smooth champagne metallic fender shoulder gleaming in soft gallery light.", ColorFamily.WARM, listOf(0xFFD6BA98, 0xFFF7ECE0, 0xFF332B21), listOf("automotive", "champagne", "fender", "gallery"), "Camille Laurent"),
                SeedSpec("18", "Indigo Night Tailblade", "Razor-thin glowing indigo and crimson LED strip across dark fastback.", ColorFamily.BLUE, listOf(0xFF4C6EB8, 0xFFD16B7A, 0xFF141B2E), listOf("automotive", "led", "night", "indigo"), "Kuro Lab", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Moss Highland Overland", "Matte olive expedition rig climbing misty heather mountain track.", ColorFamily.GREEN, listOf(0xFF5A7355, 0xFFBFD1BA, 0xFF1A2418), listOf("automotive", "overland", "highlands", "olive"), "Callum Fraser"),
                SeedSpec("20", "Bonneville White Line", "Crisp silver aerodynamic nosecone resting on bright salt flat glare.", ColorFamily.MONOCHROME, listOf(0xFFC2C6CE, 0xFFF7F8FA, 0xFF26282B), listOf("automotive", "bonneville", "silver", "speed"), "Evan Cole", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("21", "Amber Headlight Mist", "Warm golden rally fog lamps piercing calm blue-hour forest drizzle.", ColorFamily.ORANGE, listOf(0xFFE09954, 0xFF546882, 0xFF1D1B17), listOf("automotive", "rally", "foglights", "amber"), "Noah Bennett"),
                SeedSpec("22", "Plum Satin Coupe", "Sculpted low-slung coupe finished in satin aubergine in quiet hangar.", ColorFamily.PURPLE, listOf(0xFF7D5994, 0xFFD4C2E0, 0xFF201526), listOf("automotive", "coupe", "aubergine", "hangar"), "Dante Cross"),
                SeedSpec("23", "Glacier Blue Concept", "Aerodynamic white-blue hypercar silhouette over wet runway mirror.", ColorFamily.COOL, listOf(0xFF6BA3C7, 0xFFE0F0FA, 0xFF182A36), listOf("automotive", "hypercar", "glacier", "runway"), "Lucas Weber")
            )
        )
    )

    // 8. SPACE (+23)
    addAll(
        buildCategoryBatch(
            prefix = "spcp7",
            category = WallpaperCategory.SPACE,
            baseOrder = 825,
            specs = listOf(
                SeedSpec("01", "JWST Deep Rose Nebula", "Rose-gold stellar pillars backlit cleanly inside silent cosmic void.", ColorFamily.WARM, listOf(0xFFD9959E, 0xFFF7DCE0, 0xFF24161A), listOf("space", "jwst", "nebula", "rose"), "JWST Curated", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("02", "Cobalt Ionosphere Glow", "Curved cyan-blue aurora airglow arcing cleanly above sleeping Earth.", ColorFamily.BLUE, listOf(0xFF4E8BD4, 0xFFC2DFFA, 0xFF112036), listOf("space", "earth", "airglow", "orbit"), "ISS Archive", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("03", "Mint Planetary Ring", "Pale mint ice ring fragments shimmering obliquely around distant gas moon.", ColorFamily.GREEN, listOf(0xFF63BA98, 0xFFCFF5E6, 0xFF132920), listOf("space", "rings", "mint", "planet"), "Cassini Lab"),
                SeedSpec("04", "Amethyst Spiral Core", "Delicate violet dust arms twisting peacefully toward bright white core.", ColorFamily.PURPLE, listOf(0xFF916FD4, 0xFFE3D4FA, 0xFF1D1430), listOf("space", "galaxy", "spiral", "amethyst"), "Vera Rubin"),
                SeedSpec("05", "Amber Solar Flare", "Golden magnetic plasma loop rising calmly off shadowed solar limb.", ColorFamily.ORANGE, listOf(0xFFE6914E, 0xFFFCE0C5, 0xFF2E190B), listOf("space", "sun", "flare", "amber"), "Helios Optics"),
                SeedSpec("06", "Silver Crater Rim", "Stark monochrome sunlit ridge along an ancient lunar mare edge.", ColorFamily.MONOCHROME, listOf(0xFF8C929E, 0xFFEBECEE, 0xFF141518), listOf("space", "moon", "crater", "silver"), "Artemis Lens", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("07", "Ice Giant Cyan Band", "Smooth pastel turquoise cloud bands wrapping tranquil icy sphere.", ColorFamily.COOL, listOf(0xFF6EBCC4, 0xFFD9F5F7, 0xFF162C2E), listOf("space", "icegiant", "cyan", "planet"), "Voyager Craft"),
                SeedSpec("08", "Coral Star Nursery", "Soft coral emission haze cradling crisp newborn blue-white stars.", ColorFamily.WARM, listOf(0xFFE08C79, 0xFF6E9AC4, 0xFF26191C), listOf("space", "nebula", "coral", "stars"), "Hubble Studio", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Sapphire Deep Field", "Tiny jewel-toned galaxies suspended symmetrically across quiet indigo void.", ColorFamily.BLUE, listOf(0xFF4772B8, 0xFFB8D1FA, 0xFF101B2E), listOf("space", "deepfield", "galaxies", "indigo"), "Atacama Array"),
                SeedSpec("10", "Seafoam Pulsar Cloud", "Luminous seafoam synchrotron glow radiating from neutron star center.", ColorFamily.GREEN, listOf(0xFF5BB89E, 0xFFCFF2E8, 0xFF132B24), listOf("space", "pulsar", "seafoam", "nebula"), "Chandra Art"),
                SeedSpec("11", "Sienna Olympus Rim", "Towering Martian volcano rim emerging calmly through pink dust haze.", ColorFamily.ORANGE, listOf(0xFFD68663, 0xFFF5D4C6, 0xFF331B12), listOf("space", "mars", "volcano", "dust"), "Viking Lens"),
                SeedSpec("12", "Lavender Rings Cassini", "Silky backlit lavender ice particles fanning smoothly around dark globe.", ColorFamily.PURPLE, listOf(0xFF9F82CC, 0xFFE8DDF7, 0xFF1E172E), listOf("space", "saturn", "lavender", "rings"), "Cassini Eye"),
                SeedSpec("13", "Obsidian Gravity Lens", "Fine bent silver star arcs curving cleanly around quiet dark mass.", ColorFamily.MONOCHROME, listOf(0xFF757B85, 0xFFE3E6EB, 0xFF121317), listOf("space", "blackhole", "lensing", "silver"), "Relativity Lab"),
                SeedSpec("14", "Glacier Comet Mist", "Cool turquoise ion tail slicing gracefully across velvet polar constellations.", ColorFamily.COOL, listOf(0xFF6BAAC4, 0xFFD9F0FA, 0xFF162830), listOf("space", "comet", "turquoise", "stars"), "Palomar Sky"),
                SeedSpec("15", "Peach Crescent Moon", "Warm horizon-tinted crescent hovering serenely above twilight cloud deck.", ColorFamily.WARM, listOf(0xFFE0A484, 0xFF5D7294, 0xFF211C21), listOf("space", "crescent", "peach", "twilight"), "Mauna Kea"),
                SeedSpec("16", "Periwinkle Starburst", "Bright young periwinkle star veil diffusing through cosmic hydrogen.", ColorFamily.BLUE, listOf(0xFF6B8DD6, 0xFFD9E3FA, 0xFF151F36), listOf("space", "starburst", "periwinkle", "cosmos"), "Kepler View"),
                SeedSpec("17", "Emerald Ring Nebula", "Translucent emerald planetary smoke halo glowing cleanly in dark void.", ColorFamily.GREEN, listOf(0xFF4CA87F, 0xFFBDEBD5, 0xFF11291E), listOf("space", "ringnebula", "emerald", "halo"), "Orion Survey"),
                SeedSpec("18", "Tangerine Accretion", "Warm golden-tangerine gas spiral swirling neatly into pitch center.", ColorFamily.ORANGE, listOf(0xFFE88E4A, 0xFFFCD6B8, 0xFF261509), listOf("space", "accretion", "tangerine", "gravity"), "Relativity Lab", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Plum Veil Galaxy", "Edge-on dark dust lane bisecting soft glowing violet galactic bulge.", ColorFamily.PURPLE, listOf(0xFF8262AD, 0xFFD9C9EB, 0xFF1A1326), listOf("space", "galaxy", "plum", "edgeon"), "Vera Rubin"),
                SeedSpec("20", "White Dwarf Halo", "Crisp icy white ember surrounded by soft spherical shell of vapor.", ColorFamily.MONOCHROME, listOf(0xFFA8B0BA, 0xFFF5F7FA, 0xFF1B1D21), listOf("space", "star", "white", "halo"), "Hubble Mosaic"),
                SeedSpec("21", "Teal Magnetosphere", "Translucent teal solar wind lines flowing smoothly over icy world.", ColorFamily.COOL, listOf(0xFF56B0BA, 0xFFCFF0F5, 0xFF142A2E), listOf("space", "magnetosphere", "teal", "science"), "Galileo Probe"),
                SeedSpec("22", "Rose Horizon Eclipse", "Delicate rose diamond-ring glint crowning silent lunar shadow.", ColorFamily.WARM, listOf(0xFFD9959E, 0xFF4A4E5E, 0xFF1B171E), listOf("space", "eclipse", "rose", "corona"), "Helios Optics", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("23", "Cobalt Exoplanet Rim", "Thin azure atmospheric rim glowing calmly over shadowed ocean world.", ColorFamily.BLUE, listOf(0xFF4F84C9, 0xFFC4DDF7, 0xFF121E33), listOf("space", "exoplanet", "azure", "rim"), "Kepler Exo")
            )
        )
    )

    // 9. ANIME & ARTISTIC (+23)
    addAll(
        buildCategoryBatch(
            prefix = "anip7",
            category = WallpaperCategory.ANIME_ARTISTIC,
            baseOrder = 850,
            specs = listOf(
                SeedSpec("01", "Kyoto Tram Golden Hour", "Painterly pastel streetcar gliding softly past sunlit cherry trees.", ColorFamily.WARM, listOf(0xFFE3A494, 0xFFF7E1D9, 0xFF33211E), listOf("anime", "kyoto", "tram", "painterly"), "Sora Takahashi", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("02", "Cobalt Shrine Stairway", "Hand-painted stone shrine stairs climbing toward drifting cumulus clouds.", ColorFamily.BLUE, listOf(0xFF5D8ECC, 0xFFD9E9FA, 0xFF162438), listOf("anime", "shrine", "clouds", "summer"), "Kenji Sato", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("03", "Moss Library Conservatory", "Cozy illustrated wooden library overtaken gently by sunlit ferns.", ColorFamily.GREEN, listOf(0xFF5B8768, 0xFFCDE0D1, 0xFF1A291E), listOf("artistic", "library", "cozy", "botanical"), "Clara Lindqvist"),
                SeedSpec("04", "Twilight Rooftop Breeze", "Soft pastel violet sky stretching peacefully above distant animated cityscape.", ColorFamily.PURPLE, listOf(0xFF9378C4, 0xFFE3D9F7, 0xFF211A30), listOf("anime", "rooftop", "twilight", "sky"), "Mina Park"),
                SeedSpec("05", "Apricot Lantern Alley", "Warm glowing paper lanterns swaying gently through misty wooden lane.", ColorFamily.ORANGE, listOf(0xFFE3945C, 0xFFF7D9C2, 0xFF361F12), listOf("anime", "lanterns", "alley", "warm"), "Hana Kim"),
                SeedSpec("06", "Ink Wash Mountain Crane", "Sumi-e charcoal brushstrokes evoking misty cliffs and single soaring crane.", ColorFamily.MONOCHROME, listOf(0xFF7D828A, 0xFFEDEFF2, 0xFF1E2024), listOf("artistic", "sumie", "ink", "mountains"), "Kenji Mori"),
                SeedSpec("07", "Seafoam Station Platform", "Solitary seaside train platform framed by glassy turquoise summer surf.", ColorFamily.COOL, listOf(0xFF66B5BF, 0xFFD9F2F5, 0xFF172D30), listOf("anime", "train", "seaside", "summer"), "Sora Takahashi"),
                SeedSpec("08", "Gouache Peach Cloudbank", "Thick textured gouache brushstrokes building luminous peach evening clouds.", ColorFamily.WARM, listOf(0xFFE09E84, 0xFFF7E0D6, 0xFF36221B), listOf("artistic", "gouache", "clouds", "peach"), "Camille Laurent", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Starlit Ferry Crossing", "Dreamlike cobalt night river dotted with glowing golden paper boats.", ColorFamily.BLUE, listOf(0xFF4B72B8, 0xFFE8C98E, 0xFF141D30), listOf("anime", "ferry", "night", "lantern"), "Luna Hayes"),
                SeedSpec("10", "Celadon Rain Garden", "Painterly emerald raindrops rippling softly across koi lotus pond.", ColorFamily.GREEN, listOf(0xFF54997C, 0xFFC5EBDC, 0xFF142920), listOf("artistic", "lotus", "rain", "celadon"), "Clara Reid"),
                SeedSpec("11", "Saffron Windmill Valley", "Illustrated sunlit meadow dotted with floating golden pollen and sails.", ColorFamily.ORANGE, listOf(0xFFE3AA59, 0xFFFAF0D9, 0xFF362510), listOf("anime", "meadow", "windmill", "gold"), "Iris Reid"),
                SeedSpec("12", "Amethyst Meteor Night", "Pastel violet shooting star trailing softly across illustrated hilltop.", ColorFamily.PURPLE, listOf(0xFF8C6BC7, 0xFFE3D4FA, 0xFF1E162E), listOf("anime", "meteor", "hilltop", "stars"), "Mina Park", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("13", "Silver Woodblock Wave", "Quiet modern ukiyo-e charcoal ocean swell in fine crisp linework.", ColorFamily.MONOCHROME, listOf(0xFF787E87, 0xFFE8EAEE, 0xFF1C1E22), listOf("artistic", "ukiyoe", "wave", "linework"), "Kenji Mori"),
                SeedSpec("14", "Glacier Observatory", "Hand-painted brass telescope dome resting quietly on snowbound peak.", ColorFamily.COOL, listOf(0xFF75A6C4, 0xFFE6F2FA, 0xFF1C2933), listOf("artistic", "observatory", "snow", "illustration"), "Freja Solberg"),
                SeedSpec("15", "Rose Petal Atelier", "Sunlit painter's studio window spilling blush petals over easel.", ColorFamily.WARM, listOf(0xFFDEA1A8, 0xFFFAE6E8, 0xFF362124), listOf("artistic", "atelier", "window", "pastel"), "Elena Conti"),
                SeedSpec("16", "Cerulean Balloon Drift", "Pastel hot-air balloons drifting calmly over endless blue sea clouds.", ColorFamily.BLUE, listOf(0xFF5C91CF, 0xFFD9EBFA, 0xFF162436), listOf("anime", "balloons", "sky", "cerulean"), "Chloe Parker"),
                SeedSpec("17", "Forest Spirit Gate", "Moss-covered torii arch glowing gently in dappled emerald woodland mist.", ColorFamily.GREEN, listOf(0xFF4A8261, 0xFFBBE0C8, 0xFF13261B), listOf("anime", "torii", "forest", "spirit"), "Kenji Sato", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("18", "Copper Rooftop Cats", "Warm orange twilight silhouette of quiet tiled eaves above misty town.", ColorFamily.ORANGE, listOf(0xFFD98457, 0xFFF5D1BD, 0xFF331B10), listOf("anime", "rooftop", "sunset", "cozy"), "Hana Kim", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Lilac Bakery Morning", "Watercolor storefront awning glowing softly beneath pale morning sky.", ColorFamily.PURPLE, listOf(0xFF9E84C9, 0xFFEDE6FA, 0xFF241D30), listOf("artistic", "watercolor", "bakery", "pastel"), "Camille Laurent"),
                SeedSpec("20", "Graphite Sketch Cathedral", "Architectural charcoal pencil rendering of vaulted light ribs.", ColorFamily.MONOCHROME, listOf(0xFF8C9099, 0xFFEBECEE, 0xFF222326), listOf("artistic", "sketch", "cathedral", "charcoal"), "Nora Lind"),
                SeedSpec("21", "Aquamarine Reef Drift", "Luminous illustrated coral arches glowing softly under turquoise current.", ColorFamily.COOL, listOf(0xFF52B5BA, 0xFFCFF2F5, 0xFF132B2E), listOf("artistic", "reef", "underwater", "aqua"), "Iris Reid"),
                SeedSpec("22", "Sunset Ferris Horizon", "Pastel carnival wheel silhouetted warmly against apricot sea reflections.", ColorFamily.WARM, listOf(0xFFE0947B, 0xFFF7DDD4, 0xFF361F18), listOf("anime", "ferriswheel", "sunset", "pastels"), "Sora Takahashi"),
                SeedSpec("23", "Indigo Lantern Pier", "Quiet hand-painted wooden dock floating in deep indigo starry fog.", ColorFamily.BLUE, listOf(0xFF4C6FB5, 0xFFC7D9FA, 0xFF131D30), listOf("anime", "dock", "indigo", "night"), "Luna Hayes")
            )
        )
    )

    // 10. CINEMATIC (+23)
    addAll(
        buildCategoryBatch(
            prefix = "cinp7",
            category = WallpaperCategory.CINEMATIC,
            baseOrder = 875,
            specs = listOf(
                SeedSpec("01", "Anamorphic Rain Diner", "Glowing warm neon diner window reflected softly across wet midnight asphalt.", ColorFamily.WARM, listOf(0xFFD9876E, 0xFF4E6278, 0xFF1C171B), listOf("cinematic", "rain", "diner", "anamorphic"), "Alex Mercer", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("02", "Noir Fog Lighthouse", "Solitary rotating beam slicing cleanly through deep slate Atlantic haze.", ColorFamily.COOL, listOf(0xFF6B89A3, 0xFFD9E6F2, 0xFF172029), listOf("cinematic", "lighthouse", "fog", "noir"), "Lucas Weber", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("03", "Sodium Alley Reflection", "35mm night frame catching golden sodium puddles between brick walls.", ColorFamily.ORANGE, listOf(0xFFD9904C, 0xFF4A4E59, 0xFF1F170F), listOf("cinematic", "35mm", "alley", "night"), "Mateo Navarro"),
                SeedSpec("04", "Emerald Subway Blur", "Still commuter silhouetted against motion-blurred emerald tiled station.", ColorFamily.GREEN, listOf(0xFF448263, 0xFFB0D9C4, 0xFF11241A), listOf("cinematic", "subway", "motion", "emerald"), "Hana Kim"),
                SeedSpec("05", "Cobalt Rooftop Mist", "Moody blue-hour penthouse glass terrace overlooking distant city lights.", ColorFamily.BLUE, listOf(0xFF4873B5, 0xFFB8D1F5, 0xFF121D30), listOf("cinematic", "bluehour", "city", "cobalt"), "Theo Sterling"),
                SeedSpec("06", "Silver 65mm Snowfall", "Crisp monochrome winter streetlamp haloed by drifting heavy snow.", ColorFamily.MONOCHROME, listOf(0xFF8C929B, 0xFFEDEFF2, 0xFF1A1B1F), listOf("cinematic", "snow", "streetlamp", "mono"), "Oskar Holm"),
                SeedSpec("07", "Violet Neon Laundromat", "Soft pastel violet fluorescent glow spilling onto quiet wet sidewalk.", ColorFamily.PURPLE, listOf(0xFF8F6DC4, 0xFFE0D1F7, 0xFF1E162E), listOf("cinematic", "neon", "violet", "night"), "Mina Park"),
                SeedSpec("08", "Golden Hour Train Car", "Warm amber sunlight streaming through dusty vintage rail window.", ColorFamily.WARM, listOf(0xFFD99E6E, 0xFFF7E0CE, 0xFF332215), listOf("cinematic", "train", "goldenhour", "film"), "Elena Conti", tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Arctic Runway Flare", "Cool cyan landing lights stretching infinitely into misty polar dusk.", ColorFamily.COOL, listOf(0xFF5CA3BD, 0xFFD4EEF7, 0xFF14262E), listOf("cinematic", "runway", "arctic", "flare"), "Freja Solberg"),
                SeedSpec("10", "Indigo Harbor Ferry", "Mist-wrapped harbor vessel cutting calmly through dark sapphire swells.", ColorFamily.BLUE, listOf(0xFF476FA8, 0xFFC2D6F2, 0xFF121C2B), listOf("cinematic", "ferry", "harbor", "sapphire"), "Mikkel Holm"),
                SeedSpec("11", "Misty Pine Cabin Lamp", "Single warm amber cabin window glowing serenely inside deep evergreen fog.", ColorFamily.GREEN, listOf(0xFF4E755B, 0xFFE0AA6E, 0xFF142119), listOf("cinematic", "cabin", "forest", "amber"), "Noah Bennett"),
                SeedSpec("12", "Tangerine Motel Dusk", "Retro desert highway marquee softly humming beneath dusty twilight rose.", ColorFamily.ORANGE, listOf(0xFFE08858, 0xFFF7D4C0, 0xFF331C10), listOf("cinematic", "desert", "motel", "dusk"), "Chloe Parker"),
                SeedSpec("13", "Plum Theater Velvet", "Dim crimson-plum stage curtains catching one soft overhead spotlight.", ColorFamily.PURPLE, listOf(0xFF825894, 0xFFDCC2E6, 0xFF211426), listOf("cinematic", "theater", "spotlight", "plum"), "Camille Laurent"),
                SeedSpec("14", "Noir Umbrella Bridge", "Minimalist monochrome rain silhouette crossing misted stone footbridge.", ColorFamily.MONOCHROME, listOf(0xFF6B6F78, 0xFFDCE0E6, 0xFF191B1F), listOf("cinematic", "noir", "umbrella", "bridge"), "Dorian Gray"),
                SeedSpec("15", "Rose Desert Mirage", "35mm hazy heat shimmer rippling softly over warm rose-ochre highway.", ColorFamily.WARM, listOf(0xFFD99988, 0xFFF7DFD9, 0xFF33201C), listOf("cinematic", "highway", "desert", "35mm"), "Elena Brooks"),
                SeedSpec("16", "Teal Bridge Haze", "Suspension cables disappearing cleanly into luminous coastal teal fog.", ColorFamily.COOL, listOf(0xFF5CA3A8, 0xFFD4F0F2, 0xFF14292B), listOf("cinematic", "bridge", "teal", "fog"), "Liam Cole"),
                SeedSpec("17", "Deep Blue Aquarium", "Silhouetted gallery visitor standing quietly before luminous cobalt glass.", ColorFamily.BLUE, listOf(0xFF3F72B8, 0xFFB0CEFA, 0xFF101D33), listOf("cinematic", "aquarium", "silhouette", "blue"), "Kenji Sato", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("18", "Jade Bamboo Storm", "Wind-swept emerald bamboo framing solitary lantern along stone steps.", ColorFamily.GREEN, listOf(0xFF4D8063, 0xFFC2DFCE, 0xFF14241B), listOf("cinematic", "bamboo", "storm", "lantern"), "Kenji Mori", tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Amber Dust Projector", "Warm cinema beam cutting brightly through floating dust motes in dark.", ColorFamily.ORANGE, listOf(0xFFE39E59, 0xFFF7DFC2, 0xFF2E1E0D), listOf("cinematic", "projector", "beam", "dust"), "Victor Ray"),
                SeedSpec("20", "Lilac Rain Overpass", "Soft violet brake-light trails streaking beneath wet city overpass.", ColorFamily.PURPLE, listOf(0xFF8E74BF, 0xFFE3D9F5, 0xFF1F182E), listOf("cinematic", "overpass", "rain", "trails"), "Sora Takahashi"),
                SeedSpec("21", "Charcoal Canyon Mist", "Dramatic stark black-and-silver morning fog pouring through cliff notch.", ColorFamily.MONOCHROME, listOf(0xFF696D75, 0xFFE0E2E6, 0xFF17181C), listOf("cinematic", "canyon", "fog", "monochrome"), "Callum Fraser"),
                SeedSpec("22", "Apricot Pier Silhouette", "Golden film backlight hugging quiet ocean pilings at low tide.", ColorFamily.WARM, listOf(0xFFE0A47B, 0xFFF7E0D1, 0xFF332217), listOf("cinematic", "pier", "tide", "backlight"), "Iris Reid"),
                SeedSpec("23", "Cyan Neon Conservatory", "Rain-streaked greenhouse glass reflecting cool cyan midnight signs.", ColorFamily.COOL, listOf(0xFF56ABB8, 0xFFC9EFF5, 0xFF13292E), listOf("cinematic", "glass", "rain", "cyan"), "Vera Lin")
            )
        )
    )

    // 11. DEPTH STUDIO (+23, ALL supportsDepth = true, portrait 9:16/2:3)
    addAll(
        buildCategoryBatch(
            prefix = "depp7",
            category = WallpaperCategory.DEPTH_STUDIO,
            baseOrder = 900,
            specs = listOf(
                SeedSpec("01", "Frost Marble Arch Depth", "Crisp white marble portico rising cleanly to frame upper lock-screen clock.", ColorFamily.MONOCHROME, listOf(0xFFAEB4BF, 0xFFF7F8FA, 0xFF222429), listOf("depth", "arch", "marble", "clock"), "Nora Lind", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("02", "Dolomite Needle Peak", "Sharp sunlit limestone spire centered beneath pale azure morning sky.", ColorFamily.BLUE, listOf(0xFF6B99CC, 0xFFE0EEFA, 0xFF192636), listOf("depth", "peak", "dolomites", "sky"), "Lukas Meier", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("03", "Monstera Dew Silhouette", "Crisp emerald tropical leaf rising smoothly across lower clock digits.", ColorFamily.GREEN, listOf(0xFF47825E, 0xFFC2E0CD, 0xFF13261B), listOf("depth", "botanical", "monstera", "leaf"), "Clara Lindqvist", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("04", "Terracotta Clay Bust", "Minimalist warm terracotta sculpture poised neatly in soft studio space.", ColorFamily.WARM, listOf(0xFFD68D72, 0xFFF7DDD4, 0xFF331F18), listOf("depth", "sculpture", "terracotta", "portrait"), "Camille Laurent", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("05", "Amethyst Geode Spire", "Translucent violet quartz crystal cluster towering into soft lavender haze.", ColorFamily.PURPLE, listOf(0xFF936FD4, 0xFFE8DDF7, 0xFF211636), listOf("depth", "crystal", "amethyst", "spire"), "Mina Park", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("06", "Amber Fox Portrait", "Crisp sunlit alpine fox silhouette framed cleanly against soft sky.", ColorFamily.ORANGE, listOf(0xFFE08C53, 0xFFF7D6BF, 0xFF331C0E), listOf("depth", "wildlife", "fox", "portrait"), "Noah Bennett", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("07", "Arctic Iceberg Crown", "Sculptural cyan-white iceberg crest floating in calm mirror fjord.", ColorFamily.COOL, listOf(0xFF70B0C9, 0xFFE6F5FA, 0xFF1A2D36), listOf("depth", "iceberg", "arctic", "peak"), "Freja Solberg", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("08", "White Orchid Stem", "Slender ivory moth orchid stem arching gracefully over pale linen.", ColorFamily.WARM, listOf(0xFFE3D2C8, 0xFFFAF6F2, 0xFF302925), listOf("depth", "orchid", "ivory", "botanical"), "Iris Reid", true, DepthSubjectHint.BOTANICAL_STEM, tier = ResolutionTier.QHD_1440),
                SeedSpec("09", "Obsidian Glass Torus Depth", "Refractive smoked glass ring centered cleanly for clock-behind overlap.", ColorFamily.MONOCHROME, listOf(0xFF4C5059, 0xFFD4D8E0, 0xFF141518), listOf("depth", "glass", "torus", "obsidian"), "Kuro Lab", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("10", "Cobalt Sailboat Mast", "Crisp tall white yacht sail cutting up into calm cobalt sea horizon.", ColorFamily.BLUE, listOf(0xFF4C80C4, 0xFFD9EAFA, 0xFF132238), listOf("depth", "sailboat", "cobalt", "ocean"), "Liam Cole", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("11", "Sage Eucalyptus Arch", "Soft silver-sage eucalyptus sprig rising quietly into clean pastel sky.", ColorFamily.GREEN, listOf(0xFF749982, 0xFFE0EDE4, 0xFF1E2B23), listOf("depth", "eucalyptus", "sage", "stem"), "Clara Reid", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("12", "Sienna Sandstone Hoodoo", "Towering red-rock desert spire standing tall in clear dawn negative space.", ColorFamily.ORANGE, listOf(0xFFD67E58, 0xFFF5D1C2, 0xFF331B12), listOf("depth", "desert", "hoodoo", "spire"), "Elena Brooks", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("13", "Lavender Lighthouse Depth", "Solitary white beacon tower framed beneath soft twilight violet mist.", ColorFamily.PURPLE, listOf(0xFF8E74BD, 0xFFE3D9F5, 0xFF1F182E), listOf("depth", "lighthouse", "lavender", "tower"), "Mikkel Holm", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("14", "Brutalist Concrete Fin", "Angular sunlit architectural concrete fin rising crisply into blue haze.", ColorFamily.COOL, listOf(0xFF7B94AB, 0xFFE3ECF5, 0xFF202830), listOf("depth", "concrete", "fin", "architecture"), "Julian Meyer", true, DepthSubjectHint.PEAK_HORIZON),
                SeedSpec("15", "Rose Ceramic Totem", "Stacked blush matte geometric pottery vessels in quiet studio light.", ColorFamily.WARM, listOf(0xFFDFA3AA, 0xFFFAE8EA, 0xFF362225), listOf("depth", "ceramic", "totem", "rose"), "Elena Conti", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("16", "Silver Birch Crown", "Slender frosted silver birch crown reaching gracefully across pale clouds.", ColorFamily.MONOCHROME, listOf(0xFF9EA4AE, 0xFFF2F4F7, 0xFF232529), listOf("depth", "birch", "crown", "silver"), "Oskar Holm", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("17", "Azure Jellyfish Bell", "Luminous translucent cobalt jellyfish bell pulsing serenely in abyss.", ColorFamily.BLUE, listOf(0xFF427BC7, 0xFFB8D9FA, 0xFF101E33), listOf("depth", "jellyfish", "cobalt", "ocean"), "Chloe Parker", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("18", "Emerald Cypress Silhouette", "Solitary slender Italian cypress rising calmly out of morning valley mist.", ColorFamily.GREEN, listOf(0xFF547D63, 0xFFD4E6DA, 0xFF19261E), listOf("depth", "cypress", "mist", "silhouette"), "Lorenzo Bellini", true, DepthSubjectHint.BOTANICAL_STEM, tier = ResolutionTier.QHD_1440),
                SeedSpec("19", "Copper Balloon Ascent", "Single sunlit copper hot-air balloon floating steadily against quiet sky.", ColorFamily.ORANGE, listOf(0xFFDE8E5B, 0xFFF7DEC9, 0xFF331E12), listOf("depth", "balloon", "copper", "sky"), "Max Weber", true, DepthSubjectHint.CENTER_PORTRAIT),
                SeedSpec("20", "Plum Magnolias Bloom", "Velvety deep-plum magnolia branch reaching delicately across morning fog.", ColorFamily.PURPLE, listOf(0xFF8C619E, 0xFFE6D4ED, 0xFF211626), listOf("depth", "magnolia", "plum", "botanical"), "Hana Kim", true, DepthSubjectHint.BOTANICAL_STEM),
                SeedSpec("21", "Glacier Wave Crest", "Translucent teal-blue wave lip curling cleanly under crisp sky.", ColorFamily.COOL, listOf(0xFF52A8B8, 0xFFCFF0F5, 0xFF13292E), listOf("depth", "wave", "teal", "crest"), "Arthur Vance", true, DepthSubjectHint.LOWER_SILHOUETTE),
                SeedSpec("22", "Champagne Arch Portal", "Curved champagne stone archway framing distant calm sea horizon.", ColorFamily.WARM, listOf(0xFFD9BE9E, 0xFFF7ECE0, 0xFF332B21), listOf("depth", "arch", "champagne", "portal"), "Mateo Navarro", true, DepthSubjectHint.TOP_ARCH),
                SeedSpec("23", "Graphite Falcon Wing", "Minimalist monochrome peregrine silhouette perched on crisp cliff edge.", ColorFamily.MONOCHROME, listOf(0xFF6B707A, 0xFFE6E8EC, 0xFF191B1F), listOf("depth", "bird", "falcon", "silhouette"), "Callum Fraser", true, DepthSubjectHint.LOWER_SILHOUETTE)
            )
        )
    )
}
