package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.data.catalog.ProceduralWallpaperRenderer
import com.example.data.catalog.WallpaperCatalog
import com.example.data.media.WallpaperMediaService
import com.example.data.model.AppLanguageOption
import com.example.data.model.DockLayoutMode
import com.example.data.model.ImageQualityPref
import com.example.data.model.LockScreenSetDoc
import com.example.data.model.ThemeMode
import com.example.data.model.UiStyleMode
import com.example.data.model.UserPreferencesDoc
import com.example.data.model.WallpaperCategory
import com.example.data.repository.GuestLocalUserStore
import com.example.data.weather.OpenMeteoWeatherRepository
import com.example.data.weather.WeatherTelemetryState
import com.example.engine.depth.DepthDetectionEngine
import com.example.engine.depth.MaskBrushMode
import com.example.engine.depth.MaskBrushStroke
import com.example.engine.overlay.CompatibilityStatus
import com.example.engine.overlay.LockScreenCompatibilityManager
import com.example.engine.overlay.OemProfile
import com.example.ui.components.ClockDesignConfig
import com.example.ui.components.ClockSignatureMode
import com.example.ui.components.ClockWidgetTelemetry
import com.example.ui.components.HyperOsClockFace
import com.example.ui.components.HyperOsClockFinish
import com.example.ui.components.HyperOsClockFont
import com.example.ui.components.HyperOsLockClock
import com.example.ui.components.TactileSurface
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LockScreenCompatibilityScreen
import com.example.ui.screens.LockScreenModeScreen
import com.example.ui.screens.OnboardingFlowScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.SettingsPanelScreen
import com.example.ui.theme.BackdropCustomAccentSwatches
import com.example.ui.theme.BackdropTheme
import com.example.ui.theme.ProvideLocalizedAppContext
import com.example.ui.viewmodel.BackdropViewModel
import com.example.ui.viewmodel.CanvasFramingState
import com.example.ui.viewmodel.DepthStudioViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class Backdrop11IntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun facesFontsAndTallPosterRenderCleanlyUnderLiquidGlassAndSeparatedSlots() {
        assertEquals(33, HyperOsClockFace.entries.size)
        assertEquals(22, HyperOsClockFont.entries.size)
        assertTrue(HyperOsClockFace.entries.contains(HyperOsClockFace.TALL_POSTER_1340))
        assertEquals(9, AppLanguageOption.entries.size)

        composeRule.setContent {
            BackdropTheme(
                themeMode = ThemeMode.LIGHT,
                uiStyle = UiStyleMode.FROSTED_GLASS_OS,
                reducedMotionEnabled = true
            ) {
                TactileSurface {
                    HyperOsLockClock(
                        face = HyperOsClockFace.TALL_POSTER_1340,
                        font = HyperOsClockFont.BEBAS_NEUE,
                        finish = HyperOsClockFinish.BLUR_GLASS,
                        primaryColor = androidx.compose.ui.graphics.Color.White,
                        secondaryColor = androidx.compose.ui.graphics.Color.Cyan,
                        showDateWidget = true,
                        showWeatherWidget = true,
                        showBatteryWidget = true,
                        signatureMode = ClockSignatureMode.DATE_WEATHER,
                        signatureText = "CUSTOM STUDIO NOTE",
                        telemetry = ClockWidgetTelemetry(
                            weatherText = "19° CLEAR • TOKYO",
                            batteryPercent = 87,
                            isWeatherAvailable = true
                        )
                    )
                }
            }
        }
        composeRule.waitForIdle()
        // Layered optical synthesis renders contour + fill for "0941" under Liquid Glass;
        // Date/Weather/Battery telemetry and Custom Signature each occupy independent slots.
        assertTrue(composeRule.onAllNodesWithText("0941", substring = true).fetchSemanticsNodes().isNotEmpty())
        composeRule.onNodeWithText("19° CLEAR • TOKYO", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("87%", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("CUSTOM STUDIO NOTE", substring = true).assertIsDisplayed()
    }

    @Test
    fun atomicUndoRedoCoversClockFramingAndManualMaskStrokes() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val store = GuestLocalUserStore(ctx)
        val vm = DepthStudioViewModel(
            mediaService = WallpaperMediaService(ctx),
            localStore = store
        )

        vm.commitClockConfig { it.copy(face = HyperOsClockFace.TALL_POSTER_1340) }
        vm.commitCanvasFraming { CanvasFramingState(zoomScale = 1.65f, rotationDegrees = 18f) }
        vm.addMaskStroke(
            MaskBrushStroke(
                normalizedPoints = listOf(0.5f to 0.5f, 0.55f to 0.55f),
                mode = MaskBrushMode.KEEP
            )
        )

        assertTrue(vm.canUndo.value)
        assertEquals(HyperOsClockFace.TALL_POSTER_1340, vm.clockConfig.value.face)
        assertEquals(1.65f, vm.canvasFraming.value.zoomScale, 0.001f)
        assertEquals(18f, vm.canvasFraming.value.rotationDegrees, 0.001f)
        assertEquals(1, vm.maskStrokes.value.size)

        vm.undoClockEdit()
        assertEquals(0, vm.maskStrokes.value.size)
        vm.undoClockEdit()
        assertEquals(1.0f, vm.canvasFraming.value.zoomScale, 0.001f)
        assertEquals(0f, vm.canvasFraming.value.rotationDegrees, 0.001f)
        vm.undoClockEdit()
        assertNotEquals(HyperOsClockFace.TALL_POSTER_1340, vm.clockConfig.value.face)

        assertTrue(vm.canRedo.value)
        vm.redoClockEdit()
        vm.redoClockEdit()
        vm.redoClockEdit()
        assertEquals(HyperOsClockFace.TALL_POSTER_1340, vm.clockConfig.value.face)
        assertEquals(1.65f, vm.canvasFraming.value.zoomScale, 0.001f)
        assertEquals(1, vm.maskStrokes.value.size)
    }

    @Test
    fun depthEngineV2ManualKeepAndEraseBrushModifiesAlphaWithFeather() = runBlocking {
        val w = 64
        val h = 64
        val src = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            eraseColor(AndroidColor.rgb(210, 95, 65))
        }
        val baseCutout = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            eraseColor(AndroidColor.TRANSPARENT)
            for (y in 20 until 44) {
                for (x in 20 until 44) {
                    setPixel(x, y, AndroidColor.argb(255, 210, 95, 65))
                }
            }
        }
        val engine = DepthDetectionEngine()
        val refined = engine.applyManualMaskRefinement(
            sourceBitmap = src,
            baseCutout = baseCutout,
            strokes = listOf(
                MaskBrushStroke(
                    normalizedPoints = listOf(0.12f to 0.12f),
                    radiusFraction = 0.08f,
                    mode = MaskBrushMode.KEEP
                ),
                MaskBrushStroke(
                    normalizedPoints = listOf(0.50f to 0.50f),
                    radiusFraction = 0.08f,
                    mode = MaskBrushMode.ERASE
                )
            ),
            featherStrength = 0.45f
        )
        assertNotNull(refined)
        val keptAlpha = (refined!!.getPixel(8, 8) ushr 24) and 0xFF
        val erasedAlpha = (refined.getPixel(32, 32) ushr 24) and 0xFF
        assertTrue("Keep stroke should paint opaque alpha at (8,8), got $keptAlpha", keptAlpha > 200)
        assertTrue("Erase stroke should clear alpha at (32,32), got $erasedAlpha", erasedAlpha < 40)
    }

    @Test
    fun openMeteoWeatherParserExtractsTemperatureWmoCodeAndCity() {
        val sampleJson = """
            {
              "current": {
                "temperature_2m": 22.6,
                "weather_code": 61,
                "is_day": 1
              }
            }
        """.trimIndent()
        val parsed = OpenMeteoWeatherRepository.parseForecastJson(sampleJson, "London")
        assertTrue(parsed is WeatherTelemetryState.Ready)
        val ready = parsed as WeatherTelemetryState.Ready
        assertEquals(23, ready.temperatureC)
        assertEquals(61, ready.weatherCode)
        assertTrue(ready.compactReadout.contains("23°"))
        assertTrue(ready.compactReadout.contains("LONDON"))
    }

    @Test
    fun compatibilityManagerClassifiesAllThirteenOemProfilesAndInspectsDevice() {
        assertEquals(13, OemProfile.entries.size)
        assertEquals(OemProfile.PIXEL, LockScreenCompatibilityManager.detectOemProfile(manufacturer = "Google", model = "Pixel 9 Pro"))
        assertEquals(OemProfile.SAMSUNG_ONE_UI, LockScreenCompatibilityManager.detectOemProfile(manufacturer = "samsung", model = "SM-S928B"))
        assertEquals(OemProfile.XIAOMI_HYPEROS_MIUI, LockScreenCompatibilityManager.detectOemProfile(manufacturer = "Xiaomi", model = "24031PN60G"))
        assertEquals(OemProfile.OPPO_COLOROS, LockScreenCompatibilityManager.detectOemProfile(manufacturer = "OPPO", model = "CPH2581"))
        assertEquals(OemProfile.ONEPLUS_OXYGENOS, LockScreenCompatibilityManager.detectOemProfile(manufacturer = "OnePlus", model = "CPH2583"))
        assertEquals(OemProfile.VIVO_ORIGINOS_FUNTOUCH, LockScreenCompatibilityManager.detectOemProfile(manufacturer = "vivo", model = "V2309A"))
        assertEquals(OemProfile.REALME_UI, LockScreenCompatibilityManager.detectOemProfile(manufacturer = "realme", model = "RMX3851"))
        assertEquals(OemProfile.MOTOROLA, LockScreenCompatibilityManager.detectOemProfile(manufacturer = "motorola", model = "moto edge 50"))
        assertEquals(OemProfile.NOTHING_OS, LockScreenCompatibilityManager.detectOemProfile(manufacturer = "Nothing", model = "A015"))
        assertEquals(OemProfile.TABLET, LockScreenCompatibilityManager.detectOemProfile(manufacturer = "Google", model = "Pixel Tablet", smallestScreenWidthDp = 800))
        assertEquals(OemProfile.FOLDABLE, LockScreenCompatibilityManager.detectOemProfile(manufacturer = "samsung", model = "Galaxy Z Fold6", hasHingeFeature = true))
        assertEquals(OemProfile.CUSTOM_ROM, LockScreenCompatibilityManager.detectOemProfile(manufacturer = "Google", display = "lineage_cheetah-userdebug"))
        assertEquals(OemProfile.STOCK_ANDROID, LockScreenCompatibilityManager.detectOemProfile(manufacturer = "Generic", model = "AOSP"))

        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val report = LockScreenCompatibilityManager.inspectDevice(ctx)
        assertEquals(13, report.allTestedProfiles.size)
        assertTrue(report.effectiveStatus in CompatibilityStatus.entries)
    }

    @Test
    fun catalogExpansionAndProceduralRendererProduceValidStockAndSuperheroAssets() {
        val all = WallpaperCatalog.allWallpapers
        assertTrue("Expected >= 2800 wallpapers, got ${all.size}", all.size >= 2800)
        val stockCount = all.count { it.category == WallpaperCategory.STOCK }
        val heroCount = all.count { it.category == WallpaperCategory.SUPERHERO }
        assertTrue("Expected >= 210 Stock wallpapers, got $stockCount", stockCount >= 210)
        assertTrue("Expected >= 60 Superhero wallpapers, got $heroCount", heroCount >= 60)

        val proceduralSample = all.first { ProceduralWallpaperRenderer.isProceduralUrl(it.fullResUrl) }
        val rendered = ProceduralWallpaperRenderer.getOrRenderPreview(proceduralSample, 180, 320)
        assertEquals(180, rendered.width)
        assertEquals(320, rendered.height)
    }

    @Test
    fun preferenceResetsRestoreSlidersWithoutTouchingSetsOrFavorites() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val store = GuestLocalUserStore(ctx)
        val vm = BackdropViewModel(
            syncRepository = store,
            mediaService = WallpaperMediaService(ctx)
        )
        val wpId = WallpaperCatalog.allWallpapers.first().id
        store.addFavorite(GuestLocalUserStore.GUEST_USER_ID, wpId).getOrThrow()
        val savedSet = store.saveLockScreenSet(
            GuestLocalUserStore.GUEST_USER_ID,
            LockScreenSetDoc(name = "Protected Studio Set", wallpaperId = wpId)
        ).getOrThrow()

        store.updatePreferences(GuestLocalUserStore.GUEST_USER_ID) {
            it.copy(
                dynamicThemingEnabled = false,
                m3CornerRadiusScale = 1.4f,
                glassBlurLevel = 1.4f,
                dockLayoutMode = DockLayoutMode.SIDE.name,
                useClassicClockHandles = true,
                languageCode = "fr"
            )
        }
        vm.resetMaterial3Customizations()
        kotlinx.coroutines.delay(50)
        val afterM3Reset = store.observePreferences(GuestLocalUserStore.GUEST_USER_ID).first()
        assertEquals(1.0f, afterM3Reset.m3CornerRadiusScale, 0.001f)
        assertTrue(afterM3Reset.dynamicThemingEnabled)

        vm.restoreAllPreferenceDefaults()
        kotlinx.coroutines.delay(50)
        val afterRestoreAll = store.observePreferences(GuestLocalUserStore.GUEST_USER_ID).first()
        assertEquals("en", afterRestoreAll.languageCode)
        assertFalse(afterRestoreAll.useClassicClockHandles)
        assertTrue(store.observeFavorites(GuestLocalUserStore.GUEST_USER_ID).first().any { it.wallpaperId == wpId })
        assertTrue(store.observeLockScreenSets(GuestLocalUserStore.GUEST_USER_ID).first().any { it.id == savedSet.id })
    }

    @Test
    fun homeScreenHidesShuffleButton() {
        composeRule.setContent {
            BackdropTheme(uiStyle = UiStyleMode.MATERIAL_3, reducedMotionEnabled = true) {
                HomeScreen(
                    favoriteIds = emptySet(),
                    isRefreshing = false,
                    showCategoryChips = false,
                    discoverySeed = 2L,
                    onRefresh = {},
                    onSearchShortcut = {},
                    onSettingsShortcut = {},
                    onProfileShortcut = {},
                    onOpenWallpaper = {},
                    onToggleFavorite = {},
                    onSelectCategory = {},
                    onOpenCollection = {},
                    onOpenDepthStudio = {}
                )
            }
        }
        composeRule.waitForIdle()
        assertTrue(composeRule.onAllNodesWithTag("home_shuffle_button").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun profileScreenShowsVersion11AndTenTapDeveloperEasterEgg() {
        composeRule.setContent {
            BackdropTheme(uiStyle = UiStyleMode.FROSTED_GLASS_OS, reducedMotionEnabled = true) {
                ProfileSettingsScreen(
                    favoriteCount = 3,
                    collectionCount = 1,
                    preferences = UserPreferencesDoc(),
                    cacheBytes = 2048L,
                    onRefreshCacheSize = {},
                    onClearCache = {},
                    onOpenSettingsPanel = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("1.1").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("developer_credit_card"))
        composeRule.waitForIdle()
        repeat(10) {
            composeRule.onNodeWithTag("developer_credit_card").performClick()
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("developer_easter_egg_dialog").assertIsDisplayed()
        composeRule.onNodeWithText("Studio Inspector Unlocked").assertIsDisplayed()
    }

    @Test
    fun settingsPanelExposesDynamicThemingSwatches() {
        var dynamicOff by mutableStateOf(false)
        composeRule.setContent {
            BackdropTheme(uiStyle = UiStyleMode.MATERIAL_3, reducedMotionEnabled = true) {
                SettingsPanelScreen(
                    preferences = UserPreferencesDoc(
                        uiStyle = UiStyleMode.MATERIAL_3.name,
                        dynamicThemingEnabled = dynamicOff,
                        customAccentHex = BackdropCustomAccentSwatches.first().second
                    ),
                    cacheBytes = 1024L,
                    onBack = {},
                    onUiStyleChange = {},
                    onThemeModeChange = {},
                    onAmoledDarkChange = {},
                    onHomeCategoryChipsChange = {},
                    onImageQualityChange = {},
                    onWifiOnlyChange = {},
                    onRefreshCacheSize = {},
                    onClearCache = {},
                    onDynamicThemingChange = { dynamicOff = it }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_dynamic_theming_switch").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_panel_screen").performTouchInput {
            swipeUp(startY = bottom * 0.8f, endY = bottom * 0.3f)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_accent_swatch_${BackdropCustomAccentSwatches.first().first}")
            .assertIsDisplayed()
    }

    @Test
    fun arabicLocalizationTriggersRtlLayoutInOnboarding() {
        var observedDir = LayoutDirection.Ltr
        var langCode by mutableStateOf("ar")
        composeRule.setContent {
            ProvideLocalizedAppContext(languageCode = langCode) {
                observedDir = LocalLayoutDirection.current
                OnboardingFlowScreen(
                    preferences = UserPreferencesDoc(languageCode = langCode),
                    onLanguageSelected = { langCode = it },
                    onUiStyleSelected = {},
                    onThemeModeSelected = {},
                    onAmoledDarkSelected = {},
                    onDynamicThemingSelected = {},
                    onClassicHandlesSelected = {},
                    onCompleteOnboarding = {}
                )
            }
        }
        composeRule.waitForIdle()
        assertEquals(LayoutDirection.Rtl, observedDir)
    }

    @Test
    fun lockScreenModeScreenRendersToggleAndControls() {
        composeRule.setContent {
            BackdropTheme(uiStyle = UiStyleMode.MATERIAL_3, reducedMotionEnabled = true) {
                LockScreenModeScreen(
                    preferences = UserPreferencesDoc(),
                    savedSets = emptyList(),
                    currentClockConfig = ClockDesignConfig(),
                    onBack = {},
                    onOpenCompatibility = {},
                    onSelectOverlaySetId = {},
                    onSetOverlayEnabled = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("lockscreen_enable_switch").assertIsDisplayed()
        composeRule.onNodeWithTag("lockscreen_preview_button").assertIsDisplayed()
        composeRule.onNodeWithTag("lockscreen_apply_button").assertIsDisplayed()
    }

    @Test
    fun lockScreenCompatibilityScreenRendersSummaryCard() {
        composeRule.setContent {
            BackdropTheme(uiStyle = UiStyleMode.MATERIAL_3, reducedMotionEnabled = true) {
                LockScreenCompatibilityScreen(onBack = {})
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("compat_device_summary_card").assertIsDisplayed()
    }
}
