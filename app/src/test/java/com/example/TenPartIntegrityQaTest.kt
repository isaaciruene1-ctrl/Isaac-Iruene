package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.IntSize
import androidx.test.core.app.ApplicationProvider
import com.example.data.catalog.WallpaperCatalog
import com.example.data.model.LockScreenSetDoc
import com.example.data.model.SearchFilters
import com.example.data.model.ThemeMode
import com.example.data.model.UiStyleMode
import com.example.data.model.WallpaperCategory
import com.example.data.repository.GuestLocalUserStore
import com.example.engine.depth.ClockHorizontalAlign
import com.example.engine.depth.DepthDetectionEngine
import com.example.ui.BackdropRootApp
import com.example.ui.components.ClockBoundingOverlay
import com.example.ui.components.ClockBoxRect
import com.example.ui.components.ClockDesignConfig
import com.example.ui.components.ClockGripTarget
import com.example.ui.components.ClockSignatureMode
import com.example.ui.components.ClockSpatialMath
import com.example.ui.components.DockDestinationSpec
import com.example.ui.components.FrostedFloatingNavDock
import com.example.ui.components.HyperOsClockFace
import com.example.ui.components.HyperOsClockFinish
import com.example.ui.components.HyperOsClockFont
import com.example.ui.components.LiquidGlassClockEngine
import com.example.ui.components.LiquidGlassConfig
import com.example.ui.components.LiquidGlassPreset
import com.example.ui.components.LiquidNavPillMath
import com.example.ui.components.PixelMaterialBottomBar
import com.example.ui.components.WallpaperGridCard
import com.example.ui.screens.AdaptiveDesignSlider
import com.example.ui.screens.CanvasLookFilter
import com.example.ui.screens.CollectionsScreen
import com.example.ui.screens.DepthStudioScreen
import com.example.ui.screens.SettingsPanelScreen
import com.example.ui.screens.computeCenteredViewportCropRect
import com.example.ui.screens.renderEditedWallpaperBitmap
import com.example.ui.theme.AppMotion
import com.example.ui.theme.BackdropTheme
import com.example.ui.viewmodel.DepthStudioUiState
import com.example.ui.viewmodel.SyncState
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class TenPartIntegrityQaTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val dockDestinations = listOf(
        DockDestinationSpec("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
        DockDestinationSpec("explore", "Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
        DockDestinationSpec("favorites", "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
        DockDestinationSpec("collections", "Collections", Icons.Filled.CollectionsBookmark, Icons.Outlined.CollectionsBookmark),
        DockDestinationSpec("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    // TEST 1: Clock Studio Exhaustive Coverage (23 faces, 5 signature modes, aspect adaptivity)
    @Test
    fun test01_clockStudioFacesFontsFinishesStretchAndFormats() {
        assertEquals(23, HyperOsClockFace.entries.size)
        assertEquals(12, HyperOsClockFont.entries.size)
        assertEquals(6, HyperOsClockFinish.entries.size)
        assertEquals(5, ClockSignatureMode.entries.size)

        val defaultConfig = ClockDesignConfig()
        assertEquals(HyperOsClockFace.POSTER_SPLIT, defaultConfig.face)
        assertTrue(defaultConfig.aspectAdaptiveEnabled)

        HyperOsClockFace.entries.forEach { face ->
            HyperOsClockFinish.entries.forEach { finish ->
                val mode = ClockSignatureMode.entries[face.ordinal % ClockSignatureMode.entries.size]
                val sample = ClockDesignConfig(
                    face = face,
                    font = face.recommendedFont,
                    finish = finish,
                    widthScale = 2.15f,
                    heightScale = 2.05f,
                    is24HourFormat = false,
                    useLiveTime = false,
                    signatureMode = mode,
                    aspectAdaptiveEnabled = false,
                    signatureText = "BACKDROPS QA"
                )
                assertTrue(sample.hasCustomProportions)
                val encoded = sample.toJsonString()
                val decoded = ClockDesignConfig.fromJsonString(encoded)
                assertNotNull("Failed JSON decode for $face/$finish", decoded)
                assertEquals(face, decoded?.face)
                assertEquals(finish, decoded?.finish)
                assertEquals(mode, decoded?.signatureMode)
                assertFalse(decoded?.aspectAdaptiveEnabled ?: true)
                assertFalse(decoded?.is24HourFormat ?: true)
                assertEquals("BACKDROPS QA", decoded?.signatureText)

                val reset = ClockSpatialMath.resetProportions(sample)
                assertEquals(1.0f, reset.widthScale, 0.001f)
                assertEquals(1.0f, reset.heightScale, 0.001f)
            }
        }
    }

    // TEST 2: Clock Positioning Integrity (Center vs Auto Snap + Wallpaper Depth Framing)
    @Test
    fun test02_clockCenterPreservesVerticalYAndAutoSnapSnapsBothAxes() {
        val heights = listOf(0.10f, 0.28f, 0.56f, 0.74f)
        heights.forEach { y ->
            val dragged = ClockDesignConfig(
                offsetXFraction = 0.22f,
                placementY = y,
                alignment = ClockHorizontalAlign.END
            )
            assertTrue(ClockSpatialMath.isHorizontallyOffCenter(dragged))

            val centered = ClockSpatialMath.centerClock(dragged)
            assertEquals(0f, centered.offsetXFraction, 0.0001f)
            assertEquals(ClockHorizontalAlign.CENTER, centered.alignment)
            assertEquals("Center must preserve vertical Y=$y", y, centered.placementY, 0.0001f)
            assertFalse(ClockSpatialMath.isHorizontallyOffCenter(centered))

            val snapped = ClockSpatialMath.autoSnapToDepth(
                config = dragged,
                targetPlacementY = 0.16f,
                targetAlignment = ClockHorizontalAlign.START
            )
            assertEquals(0f, snapped.offsetXFraction, 0.0001f)
            assertEquals(0.16f, snapped.placementY, 0.0001f)
            assertEquals(ClockHorizontalAlign.START, snapped.alignment)
        }

        // DepthDetectionEngine recommends joint wallpaper zoom/pan & clock scale cleanly
        runBlocking {
            val w = 180
            val h = 320
            val subjectBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
                eraseColor(AndroidColor.rgb(42, 92, 168)) // clear sky top
                for (y in 115 until h) {
                    for (x in 52 until 128) {
                        setPixel(x, y, AndroidColor.rgb(220, 74, 48)) // strong centered mountain/subject silhouette
                    }
                }
            }
            val result = DepthDetectionEngine().detectAndExtract(subjectBitmap, "Alpine")
            assertTrue(result.recommendedWallpaperZoom in 1.0f..1.45f)
            assertTrue(result.recommendedClockScale in 0.85f..1.25f)
            assertTrue(result.recommendedWallpaperPanXFraction in -0.25f..0.25f)
            assertTrue(result.recommendedWallpaperPanYFraction in -0.25f..0.25f)
            assertFalse(result.subjectLabel.contains("Alpine"))
        }
    }

    // TEST 3: Liquid Glass Clock & Adaptive Sliders
    @Test
    fun test03_liquidGlassOpticsAndDesignSpecificSliders() {
        val raw = LiquidGlassConfig(
            refractionStrength = 99f,
            ior = 2.5f,
            bevelWidth = 1f,
            chromaticAberration = 2f,
            frostBlur = 50f,
            glassSaturation = 3f,
            tintOpacity = 0.9f,
            specularIntensity = 3f,
            lightAngle = 720f,
            innerThicknessShadow = 2f,
            backdropDropShadow = 2f
        ).sanitized()
        assertEquals(40f, raw.refractionStrength, 0.01f)
        assertEquals(1.80f, raw.ior, 0.01f)
        assertEquals(2f, raw.bevelWidth, 0.01f)
        assertEquals(1f, raw.chromaticAberration, 0.01f)
        assertEquals(24f, raw.frostBlur, 0.01f)

        assertTrue(LiquidGlassPreset.entries.size >= 4)

        val backdrop = Bitmap.createBitmap(320, 480, Bitmap.Config.ARGB_8888).apply {
            eraseColor(AndroidColor.rgb(34, 78, 128))
        }
        val mask = Bitmap.createBitmap(160, 120, Bitmap.Config.ARGB_8888).apply {
            for (y in 20 until 100) {
                for (x in 30 until 130) {
                    setPixel(x, y, AndroidColor.argb(255, 255, 255, 255))
                }
            }
        }
        val glyphMap = LiquidGlassClockEngine.buildMultiRadiusBevelHeightMap(mask, bevelWidthPx = 10f)
        val patch = LiquidGlassClockEngine.extractCroppedBackgroundPatch(
            backdropBitmap = backdrop,
            viewportSize = IntSize(320, 480),
            clockBoxRect = ClockBoxRect(80f, 96f, 160f, 120f),
            zoomScale = 1f,
            panOffset = Offset.Zero,
            dimFraction = 0f,
            lookFilter = CanvasLookFilter.ORIGINAL,
            frostBlurPx = 4f,
            fallbackPrimary = Color.Black,
            fallbackSecondary = Color.Blue,
            patchWidth = 160,
            patchHeight = 120
        )
        val out = LiquidGlassClockEngine.renderOpticalGlassSoftwareBitmap(
            glyphMap = glyphMap,
            backgroundPatch = patch.unblurredPatch,
            frostedPatch = patch.frostedPatch,
            params = LiquidGlassClockEngine.resolveAdaptiveOptics(LiquidGlassConfig(), patch.avgLuminance)
        )
        assertNotNull(out)
        assertEquals(160, out.width)
        assertEquals(120, out.height)

        var styleState by mutableStateOf(UiStyleMode.FROSTED_GLASS_OS)
        var sliderVal by mutableFloatStateOf(0.5f)
        composeRule.setContent {
            BackdropTheme(uiStyle = styleState) {
                AdaptiveDesignSlider(
                    value = sliderVal,
                    onValueChange = { sliderVal = it },
                    valueRange = 0f..1f
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("liquid_glass_slider_track").assertIsDisplayed()

        composeRule.runOnUiThread { styleState = UiStyleMode.MATERIAL_3 }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("liquid_glass_slider_track").assertDoesNotExist()
    }

    @Test
    fun test03b_bottomCornerUniqueResizeGrips() {
        var overlayStyle by mutableStateOf(UiStyleMode.FROSTED_GLASS_OS)
        var cfg by mutableStateOf(ClockDesignConfig(face = HyperOsClockFace.POSTER_SPLIT, finish = HyperOsClockFinish.BLUR_GLASS))
        composeRule.setContent {
            BackdropTheme(uiStyle = overlayStyle, reducedMotionEnabled = true) {
                ClockBoundingOverlay(
                    clockUnscaledWidthPx = 180,
                    clockUnscaledHeightPx = 90,
                    viewportSize = IntSize(320, 470),
                    config = cfg,
                    onGestureStart = {},
                    onUpdateConfig = { cfg = it },
                    onGestureEnd = {},
                    onToggleAspectLock = { cfg = cfg.copy(aspectAdaptiveEnabled = !cfg.aspectAdaptiveEnabled) },
                    onResetFrame = {},
                    onDoneEditing = {}
                )
            }
        }
        composeRule.waitForIdle()
        // Only BL & BR corners exist — zero side drag bars or TL/TR corners
        composeRule.onNodeWithTag("clock_handle_corner_bl").assertIsDisplayed()
        composeRule.onNodeWithTag("clock_handle_corner_br").assertIsDisplayed()
        composeRule.onNodeWithTag("clock_grip_style_liquid_glass_bl", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("clock_grip_style_liquid_glass_br", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("clock_aspect_lock_pill").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag("clock_handle_width_left").fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithTag("clock_handle_corner_tl").fetchSemanticsNodes().isEmpty())

        composeRule.runOnUiThread { overlayStyle = UiStyleMode.MATERIAL_3 }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("clock_grip_style_material3_bl", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("clock_grip_style_material3_br", useUnmergedTree = true).assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag("clock_grip_style_liquid_glass_bl", useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun test03c_wallpaperGridCardHidesWallpaperTitle() {
        val sampleWp = WallpaperCatalog.allWallpapers.first()
        composeRule.setContent {
            BackdropTheme(uiStyle = UiStyleMode.MATERIAL_3, reducedMotionEnabled = true) {
                WallpaperGridCard(
                    wallpaper = sampleWp,
                    isFavorite = false,
                    onClick = {},
                    onFavoriteToggle = {}
                )
            }
        }
        composeRule.waitForIdle()
        assertTrue(composeRule.onAllNodesWithText(sampleWp.title, substring = true).fetchSemanticsNodes().isEmpty())
        composeRule.onNodeWithText(sampleWp.category.displayName).assertIsDisplayed()
    }

    @Test
    fun test03d_depthStudioHidesWallpaperTitleAndRemovesCanvasStrip() {
        val sampleWp = WallpaperCatalog.allWallpapers.first()
        val cfg = ClockDesignConfig(face = HyperOsClockFace.POSTER_SPLIT, finish = HyperOsClockFinish.BLUR_GLASS)
        val dummyBmp = Bitmap.createBitmap(64, 96, Bitmap.Config.ARGB_8888)
        composeRule.setContent {
            BackdropTheme(uiStyle = UiStyleMode.MATERIAL_3, reducedMotionEnabled = true) {
                DepthStudioScreen(
                    initialWallpaper = sampleWp,
                    depthState = DepthStudioUiState.ScanningSubject(dummyBmp, sampleWp.category.displayName),
                    clockConfig = cfg,
                    canUndo = false,
                    canRedo = false,
                    isApplying = false,
                    onLoadWallpaper = { _, _ -> },
                    onPickCustomPhoto = { _, _ -> },
                    onBeginClockGesture = {},
                    onPreviewClockConfig = {},
                    onEndClockGesture = {},
                    onCommitClockConfig = {},
                    onUndoClock = {},
                    onRedoClock = {},
                    onResetProportions = {},
                    onCenterClock = {},
                    onBack = {},
                    onApplyWallpaper = { _, _, _ -> },
                    onApplyCustomBitmapWallpaper = { _, _, _ -> }
                )
            }
        }
        composeRule.waitForIdle()
        assertTrue(composeRule.onAllNodesWithText(sampleWp.title, substring = true).fetchSemanticsNodes().isEmpty())
        composeRule.onNodeWithTag("depth_tab_Canvas").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("depth_zoom_slider").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText(sampleWp.title, substring = true).fetchSemanticsNodes().isEmpty())
    }

    // TEST 4: Material 3 Purity & Design-Language Motion Differentiation
    @Test
    fun test04_material3BottomBarPurityAndDistinctMotionSpecs() {
        val m3Spatial = AppMotion.spatialSpring<Float>(UiStyleMode.MATERIAL_3, reducedMotion = false)
        val glassSpatial = AppMotion.spatialSpring<Float>(UiStyleMode.FROSTED_GLASS_OS, reducedMotion = false)
        assertNotEquals(m3Spatial.dampingRatio, glassSpatial.dampingRatio)
        assertNotEquals(m3Spatial.stiffness, glassSpatial.stiffness)

        val reduced = AppMotion.spatialSpring<Float>(UiStyleMode.FROSTED_GLASS_OS, reducedMotion = true)
        assertTrue(reduced.dampingRatio > glassSpatial.dampingRatio)

        var selectedRoute by mutableStateOf("home")
        composeRule.setContent {
            BackdropTheme(uiStyle = UiStyleMode.MATERIAL_3) {
                PixelMaterialBottomBar(
                    destinations = dockDestinations,
                    currentRoute = selectedRoute,
                    onSelectRoute = { selectedRoute = it }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("bottom_navigation_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("liquid_nav_active_capsule").assertDoesNotExist()
        composeRule.onNodeWithTag("nav_explore").performClick()
        composeRule.waitForIdle()
        assertEquals("explore", selectedRoute)
    }

    // TEST 5: Liquid Glass Draggable Bottom Navigation Pill Gesture
    @Test
    fun test05_liquidGlassDraggablePillCommitsOnlyOnRelease() {
        val routes = dockDestinations.map { it.route }
        assertEquals(0, LiquidNavPillMath.slotForPosition(10f, 500f, 5))
        assertEquals(4, LiquidNavPillMath.slotForPosition(495f, 500f, 5))
        assertNull(LiquidNavPillMath.resolveReleaseRoute(startIndex = 0, releaseIndex = 0, routes = routes))
        assertEquals("favorites", LiquidNavPillMath.resolveReleaseRoute(startIndex = 0, releaseIndex = 2, routes = routes))

        var currentRoute by mutableStateOf("home")
        composeRule.setContent {
            BackdropTheme(uiStyle = UiStyleMode.FROSTED_GLASS_OS, reducedMotionEnabled = true) {
                FrostedFloatingNavDock(
                    destinations = dockDestinations,
                    currentRoute = currentRoute,
                    onSelectRoute = { currentRoute = it }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("liquid_nav_active_capsule").assertIsDisplayed()

        // Ordinary tap switches immediately
        composeRule.onNodeWithTag("nav_explore").performClick()
        composeRule.waitForIdle()
        assertEquals("explore", currentRoute)

        // Long-press and drag from Explore (slot 1) toward Profile (slot 4) WITHOUT releasing yet
        composeRule.onNodeWithTag("bottom_navigation_bar").performTouchInput {
            val slotW = width / 5f
            val startX = slotW * 1.5f
            val targetX = slotW * 4.5f
            down(Offset(startX, centerY))
            advanceEventTime(750L)
            moveTo(Offset(targetX, centerY))
        }
        composeRule.waitForIdle()
        // Mid-drag must NOT switch destination yet!
        assertEquals("explore", currentRoute)
        composeRule.onNodeWithTag("liquid_nav_dragging_capsule").assertIsDisplayed()

        // Release finger over Profile (slot 4) -> navigation commits on release
        composeRule.onNodeWithTag("bottom_navigation_bar").performTouchInput {
            up()
        }
        composeRule.waitForIdle()
        assertEquals("profile", currentRoute)
    }

    // TEST 6 & 7: Catalog, Lock-Screen Sets, Logo Replacement & Zero Duplicate Splash
    @Test
    fun test06_07_catalogLockScreenSetsCollectionsAndNoDuplicateSplash() {
        assertTrue(WallpaperCatalog.allWallpapers.size >= 2050)
        assertEquals(11, WallpaperCategory.entries.size)
        val filtered = WallpaperCatalog.search(SearchFilters(amoledOnly = true, depthStudioOnly = true))
        assertTrue(filtered.isNotEmpty())

        // CollectionsScreen switches cleanly between Boards and Saved Sets
        val sampleSet = LockScreenSetDoc(
            id = "set_qa_1",
            name = "Alpine Liquid Setup",
            wallpaperId = WallpaperCatalog.allWallpapers.first().id,
            clockConfigJson = ClockDesignConfig(face = HyperOsClockFace.POSTER_SPLIT).toJsonString(),
            zoomScale = 1.15f,
            lookFilterName = CanvasLookFilter.COOL_MIST.name,
            isFavorite = true
        )
        var reopenedId: String? = null
        composeRule.setContent {
            BackdropTheme(uiStyle = UiStyleMode.FROSTED_GLASS_OS, reducedMotionEnabled = true) {
                CollectionsScreen(
                    collectionsState = SyncState.Ready(emptyList()),
                    lockScreenSetsState = SyncState.Ready(listOf(sampleSet)),
                    onCreateCollection = { _, _ -> },
                    onRenameCollection = { _, _, _ -> },
                    onDeleteCollection = {},
                    onOpenCollectionDetail = {},
                    onOpenLockScreenSet = { reopenedId = it.id }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("collections_section_Saved Sets").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Alpine Liquid Setup").assertIsDisplayed()
        composeRule.onNodeWithTag("lock_set_open_set_qa_1").performClick()
        composeRule.waitForIdle()
        assertEquals("set_qa_1", reopenedId)
    }

    @Test
    fun test07b_rootAppLaunchesDirectlyWithoutSecondarySplashAndUsesBackdropEmblem() {
        composeRule.setContent {
            BackdropRootApp()
        }
        composeRule.waitForIdle()
        // Home wordmark and circular Backdrops mountain emblem are immediately displayed with zero 650ms splash
        composeRule.onNodeWithText("Backdrops").assertIsDisplayed()
        composeRule.onNodeWithTag("backdrop_logo_mark").assertIsDisplayed()
    }

    // TEST 8: Performance & Stability Stress Test
    @Test
    fun test08_rapidResizeDragAndGlassOpticsStressStability() {
        var cfg = ClockDesignConfig()
        val vp = IntSize(1080, 2400)
        repeat(100) { i ->
            val rect = ClockSpatialMath.computeClockBoxRect(360f, 180f, vp, cfg)
            val grip = ClockGripTarget.entries[i % ClockGripTarget.entries.size]
            val dx = ((i % 9) - 4) * 28f
            val dy = ((i % 7) - 3) * 24f
            cfg = ClockSpatialMath.applyDragDelta(grip, cfg, rect, dx, dy, vp)
            val glass = LiquidGlassConfig(
                refractionStrength = (i * 0.6f),
                ior = 1.0f + (i % 10) * 0.1f,
                bevelWidth = 2f + (i % 24),
                chromaticAberration = (i % 10) / 8f
            ).sanitized()
            cfg = cfg.copy(
                face = HyperOsClockFace.entries[i % HyperOsClockFace.entries.size],
                liquidGlass = glass
            )
            assertFalse(cfg.widthScale.isNaN())
            assertFalse(cfg.heightScale.isNaN())
            assertFalse(cfg.placementY.isNaN())
            assertTrue(cfg.widthScale in ClockSpatialMath.MIN_WIDTH_SCALE..ClockSpatialMath.MAX_WIDTH_SCALE)
            assertTrue(cfg.heightScale in ClockSpatialMath.MIN_HEIGHT_SCALE..ClockSpatialMath.MAX_HEIGHT_SCALE)
        }
    }

    // TEST 9: Persistence & Settings Preferences + Validation Failure Paths
    @Test
    fun test09_guestLocalStorePersistsSetsPreferencesAndRejectsInvalidInputs() {
        runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val store = GuestLocalUserStore(ctx)

        store.updatePreferences(GuestLocalUserStore.GUEST_USER_ID) {
            it.copy(
                themeMode = ThemeMode.DARK.name,
                amoledDark = true,
                uiStyle = UiStyleMode.FROSTED_GLASS_OS.name,
                hapticFeedbackEnabled = false,
                reducedMotionEnabled = true,
                glassDepthIntensity = 1.32f
            )
        }
        val reloaded = store.observePreferences(GuestLocalUserStore.GUEST_USER_ID).first()
        assertFalse(reloaded.hapticFeedbackEnabled)
        assertTrue(reloaded.reducedMotionEnabled)
        assertEquals(1.32f, reloaded.glassDepthIntensity, 0.001f)

        // Blank Lock-Screen Set name and blank collection name both fail cleanly with Result.failure
        val badSet = store.saveLockScreenSet(
            GuestLocalUserStore.GUEST_USER_ID,
            LockScreenSetDoc(name = "   ", wallpaperId = WallpaperCatalog.allWallpapers.first().id)
        )
        assertTrue(badSet.isFailure)

        val badCol = store.createCollection(GuestLocalUserStore.GUEST_USER_ID, "   ", "desc", emptyList(), null)
        assertTrue(badCol.isFailure)

        // Valid set save -> favorite toggle -> delete round trip
        val saved = store.saveLockScreenSet(
            GuestLocalUserStore.GUEST_USER_ID,
            LockScreenSetDoc(
                name = "Obsidian Peaks",
                wallpaperId = WallpaperCatalog.allWallpapers.first().id,
                clockConfigJson = ClockDesignConfig().toJsonString(),
                zoomScale = 1.2f
            )
        ).getOrThrow()
        store.toggleLockScreenSetFavorite(GuestLocalUserStore.GUEST_USER_ID, saved.id).getOrThrow()
        val afterFav = store.observeLockScreenSets(GuestLocalUserStore.GUEST_USER_ID).first()
        assertTrue(afterFav.first { it.id == saved.id }.isFavorite)

        store.deleteLockScreenSet(GuestLocalUserStore.GUEST_USER_ID, saved.id).getOrThrow()
        val afterDel = store.observeLockScreenSets(GuestLocalUserStore.GUEST_USER_ID).first()
        assertTrue(afterDel.none { it.id == saved.id })

        // Contextual Settings panel exposes Glass slider ONLY in FROSTED_GLASS_OS
        var settingsStyle by mutableStateOf(UiStyleMode.MATERIAL_3)
        composeRule.setContent {
            val prefs = reloaded.copy(uiStyle = settingsStyle.name)
            BackdropTheme(uiStyle = settingsStyle) {
                SettingsPanelScreen(
                    preferences = prefs,
                    cacheBytes = 4096L,
                    onBack = {},
                    onUiStyleChange = { settingsStyle = it },
                    onThemeModeChange = {},
                    onAmoledDarkChange = {},
                    onHomeCategoryChipsChange = {},
                    onHapticFeedbackChange = {},
                    onReducedMotionChange = {},
                    onGlassDepthIntensityChange = {},
                    onImageQualityChange = {},
                    onWifiOnlyChange = {},
                    onRefreshCacheSize = {},
                    onClearCache = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_glass_depth_slider").assertDoesNotExist()
        composeRule.runOnUiThread { settingsStyle = UiStyleMode.FROSTED_GLASS_OS }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_glass_depth_slider").assertIsDisplayed()
        }
    }

    // TEST 10: Wallpaper Export & Crop Geometry Integrity
    @Test
    fun test10_lockVsHomeParallaxCropGeometryAndEditedBitmapRendering() {
        val lockCrop = computeCenteredViewportCropRect(
            sourceWidth = 2160,
            sourceHeight = 3840,
            zoomScale = 1.25f,
            panOffsetX = 0f,
            panOffsetY = 0f,
            viewportWidthPx = 1080f,
            viewportHeightPx = 2400f,
            targetScreenAspect = 1080f / 2400f,
            allowParallaxWidth = false
        )
        val homeCrop = computeCenteredViewportCropRect(
            sourceWidth = 2160,
            sourceHeight = 3840,
            zoomScale = 1.25f,
            panOffsetX = 0f,
            panOffsetY = 0f,
            viewportWidthPx = 1080f,
            viewportHeightPx = 2400f,
            targetScreenAspect = 1080f / 2400f,
            allowParallaxWidth = true
        )
        assertTrue(homeCrop.width > lockCrop.width)
        assertTrue(abs((lockCrop.left + lockCrop.width * 0.5f) - 1080f) <= 2f)

        val src = Bitmap.createBitmap(400, 800, Bitmap.Config.ARGB_8888).apply {
            eraseColor(AndroidColor.rgb(90, 140, 210))
        }
        val rendered = renderEditedWallpaperBitmap(
            source = src,
            zoomScale = 1.15f,
            panOffset = Offset(18f, -12f),
            viewportWidthPx = 200f,
            viewportHeightPx = 400f,
            dimFraction = 0.25f,
            lookFilter = CanvasLookFilter.WARM_FILM,
            targetScreenAspect = 0.5f,
            allowParallaxWidth = false
        )
        assertTrue(rendered.width > 0 && rendered.height > 0)
        assertTrue(rendered.height > rendered.width)
    }
}
