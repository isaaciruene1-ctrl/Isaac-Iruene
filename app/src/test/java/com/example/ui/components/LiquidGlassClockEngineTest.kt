package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.IntSize
import com.example.data.catalog.WallpaperCatalog
import com.example.data.model.ThemeMode
import com.example.data.model.UiStyleMode
import com.example.data.model.UserPreferencesDoc
import com.example.engine.depth.LabColorSpace
import com.example.ui.screens.CanvasLookFilter
import com.example.ui.screens.DepthStudioScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.theme.BackdropTheme
import com.example.ui.theme.ensureDarkGlassContrastScheme
import com.example.ui.viewmodel.DepthStudioUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
class LiquidGlassClockEngineTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun defaultClockConfigIsPosterBoldAndClockFaceNamesAreRefined() {
        val defaults = ClockDesignConfig()
        assertEquals(HyperOsClockFace.POSTER_SPLIT, defaults.face)
        assertEquals("Poster Bold", defaults.face.label)
        assertEquals(HyperOsClockFont.BIG_SHOULDERS, defaults.font)
        assertEquals(850, defaults.weightLevel)

        val labels = HyperOsClockFace.entries.map { it.label }
        assertFalse(labels.contains("Badge Pop"))
        assertFalse(labels.contains("Chop Seal"))
        assertFalse(labels.contains("Doodle"))
        assertTrue(labels.containsAll(listOf("Poster Bold", "Editorial Stack", "Vogue Serif", "Kanso Seal", "Horizon Minimal", "Daylight Pill")))
    }

    @Test
    fun liquidGlassConfigDefaultsSanitizeAndJsonRoundTrip() {
        val defaults = LiquidGlassConfig()
        assertEquals(18.0f, defaults.refractionStrength, 0.001f)
        assertEquals(1.48f, defaults.ior, 0.001f)
        assertEquals(10.0f, defaults.bevelWidth, 0.001f)
        assertEquals(0.35f, defaults.chromaticAberration, 0.001f)
        assertEquals(6.0f, defaults.frostBlur, 0.001f)
        assertEquals(1.45f, defaults.glassSaturation, 0.001f)
        assertEquals(0xFFFFFFFFL, defaults.tintArgb)
        assertEquals(0.10f, defaults.tintOpacity, 0.001f)
        assertEquals(0.85f, defaults.specularIntensity, 0.001f)
        assertEquals(135.0f, defaults.lightAngle, 0.001f)
        assertEquals(0.22f, defaults.innerThicknessShadow, 0.001f)
        assertEquals(0.35f, defaults.backdropDropShadow, 0.001f)
        assertTrue(defaults.adaptiveContrast)

        val outOfBounds = LiquidGlassConfig(
            refractionStrength = 99f,
            ior = 3.2f,
            bevelWidth = 0f,
            chromaticAberration = -2f,
            frostBlur = 50f,
            glassSaturation = 4f,
            tintOpacity = 0.99f,
            specularIntensity = 5f,
            lightAngle = 405f,
            innerThicknessShadow = 0.9f,
            backdropDropShadow = 2.0f
        ).sanitized()
        assertEquals(40f, outOfBounds.refractionStrength, 0.001f)
        assertEquals(1.80f, outOfBounds.ior, 0.001f)
        assertEquals(2f, outOfBounds.bevelWidth, 0.001f)
        assertEquals(0f, outOfBounds.chromaticAberration, 0.001f)
        assertEquals(24f, outOfBounds.frostBlur, 0.001f)
        assertEquals(2.0f, outOfBounds.glassSaturation, 0.001f)
        assertEquals(0.50f, outOfBounds.tintOpacity, 0.001f)
        assertEquals(1.50f, outOfBounds.specularIntensity, 0.001f)
        assertEquals(45f, outOfBounds.lightAngle, 0.001f)
        assertEquals(0.60f, outOfBounds.innerThicknessShadow, 0.001f)
        assertEquals(1.0f, outOfBounds.backdropDropShadow, 0.001f)

        val wrapped = ClockDesignConfig(
            finish = HyperOsClockFinish.BLUR_GLASS,
            liquidGlass = LiquidGlassConfig(
                refractionStrength = 26f,
                ior = 1.62f,
                bevelWidth = 14f,
                chromaticAberration = 0.65f,
                tintArgb = LiquidGlassPreset.COOL_ICE.tintArgb,
                tintOpacity = 0.14f,
                lightAngle = 210f
            )
        )
        val decoded = ClockDesignConfig.fromJsonString(wrapped.toJsonString())
        assertNotNull(decoded)
        assertEquals(HyperOsClockFinish.BLUR_GLASS, decoded?.finish)
        assertEquals(26f, decoded?.liquidGlass?.refractionStrength ?: 0f, 0.01f)
        assertEquals(1.62f, decoded?.liquidGlass?.ior ?: 0f, 0.01f)
        assertEquals(14f, decoded?.liquidGlass?.bevelWidth ?: 0f, 0.01f)
        assertEquals(0.65f, decoded?.liquidGlass?.chromaticAberration ?: 0f, 0.01f)
        assertEquals(210f, decoded?.liquidGlass?.lightAngle ?: 0f, 0.01f)
    }

    @Test
    fun adaptiveOpticsTonesContrastOnBrightAndDarkSkies() {
        val base = LiquidGlassConfig()
        val brightOptics = LiquidGlassClockEngine.resolveAdaptiveOptics(base, avgLuminance = 0.88f)
        assertTrue(brightOptics.innerThicknessShadow > base.innerThicknessShadow)
        assertTrue(brightOptics.backdropDropShadow > base.backdropDropShadow)

        val darkOptics = LiquidGlassClockEngine.resolveAdaptiveOptics(base, avgLuminance = 0.10f)
        assertTrue(darkOptics.specularIntensity > base.specularIntensity)

        val staticOptics = LiquidGlassClockEngine.resolveAdaptiveOptics(base.copy(adaptiveContrast = false), avgLuminance = 0.90f)
        assertEquals(base.innerThicknessShadow, staticOptics.innerThicknessShadow, 0.001f)
        assertEquals(base.backdropDropShadow, staticOptics.backdropDropShadow, 0.001f)
    }

    @Test
    fun opticalPipelineRefractsWallpaperAndStaysTransparentOutsideGlyphs() {
        val w = 160
        val h = 120

        // Rich multi-stop wallpaper gradient crossing both left & right bevel edges of the glyph
        val bg = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            val c = Canvas(this)
            val gradPaint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, w.toFloat(), h.toFloat(),
                    intArrayOf(
                        AndroidColor.rgb(245, 92, 54),
                        AndroidColor.rgb(255, 224, 92),
                        AndroidColor.rgb(32, 165, 240)
                    ),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), gradPaint)
        }

        // Bold vertical bar centered cleanly over the gradient, with generous outer margins
        val mask = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            val c = Canvas(this)
            c.drawColor(AndroidColor.TRANSPARENT)
            val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.WHITE }
            c.drawRoundRect(50f, 24f, 110f, 96f, 10f, 10f, barPaint)
        }

        val glyphMap = LiquidGlassClockEngine.buildMultiRadiusBevelHeightMap(mask, bevelWidthPx = 10f)
        val patch = LiquidGlassClockEngine.extractCroppedBackgroundPatch(
            backdropBitmap = bg,
            viewportSize = IntSize(160, 120),
            clockBoxRect = ClockBoxRect(0f, 0f, 160f, 120f),
            zoomScale = 1f,
            panOffset = androidx.compose.ui.geometry.Offset.Zero,
            dimFraction = 0f,
            lookFilter = CanvasLookFilter.ORIGINAL,
            frostBlurPx = 4f,
            fallbackPrimary = Color.Black,
            fallbackSecondary = Color.Blue,
            patchWidth = w,
            patchHeight = h
        )

        val refractedCfg = LiquidGlassConfig(
            refractionStrength = 28f,
            ior = 1.60f,
            bevelWidth = 10f,
            chromaticAberration = 0.85f,
            frostBlur = 2f,
            specularIntensity = 0.85f,
            lightAngle = 135f
        )
        val zeroRefractionCfg = refractedCfg.copy(refractionStrength = 0f, chromaticAberration = 0f, specularIntensity = 0f, innerThicknessShadow = 0f)

        val glassOut = LiquidGlassClockEngine.renderOpticalGlassSoftwareBitmap(
            glyphMap = glyphMap,
            backgroundPatch = patch.unblurredPatch,
            frostedPatch = patch.frostedPatch,
            params = LiquidGlassClockEngine.resolveAdaptiveOptics(refractedCfg, patch.avgLuminance)
        )
        val flatOut = LiquidGlassClockEngine.renderOpticalGlassSoftwareBitmap(
            glyphMap = glyphMap,
            backgroundPatch = patch.unblurredPatch,
            frostedPatch = patch.frostedPatch,
            params = LiquidGlassClockEngine.resolveAdaptiveOptics(zeroRefractionCfg, patch.avgLuminance)
        )

        // Margin pixels far from glyph and drop shadow must stay strictly transparent (no blurred card box!)
        assertEquals("Top-left sky corner must stay 100% transparent", 0, (glassOut.getPixel(4, 4) ushr 24) and 0xFF)
        assertEquals("Bottom-right sky corner must stay 100% transparent", 0, (glassOut.getPixel(w - 4, h - 4) ushr 24) and 0xFF)

        // Bevel pixels along left & right edges differ significantly with refraction + Fresnel vs flat 0
        var bevelDelta = 0
        for (x in 50..58) {
            val p1 = glassOut.getPixel(x, 60)
            val p2 = flatOut.getPixel(x, 60)
            bevelDelta += abs(((p1 ushr 16) and 0xFF) - ((p2 ushr 16) and 0xFF))
            bevelDelta += abs(((p1 ushr 8) and 0xFF) - ((p2 ushr 8) and 0xFF))
            bevelDelta += abs((p1 and 0xFF) - (p2 and 0xFF))
        }
        assertTrue("Expected noticeable optical bevel refraction + Fresnel delta, got $bevelDelta", bevelDelta > 120)
    }

    @Test
    fun labNaturalHarmonicClockAndDarkGlassContrastStayLegible() {
        // Warm sunset sky -> warm light clock color (not harsh cold stark white)
        val warmRgb = LabColorSpace.pickNaturalHarmonicClockHex(avgSkyL = 32f, avgSkyA = 24f, avgSkyB = 34f)
        val warmColor = AndroidColor.parseColor(warmRgb)
        assertTrue(AndroidColor.red(warmColor) > AndroidColor.blue(warmColor))

        // Cool indigo night sky -> cool atmospheric ice tint
        val coolRgb = LabColorSpace.pickNaturalHarmonicClockHex(avgSkyL = 24f, avgSkyA = -4f, avgSkyB = -28f)
        val coolColor = AndroidColor.parseColor(coolRgb)
        assertTrue(AndroidColor.blue(coolColor) >= AndroidColor.red(coolColor))

        // Bright snowy sky -> dark high-legibility slate tint
        val darkInkHex = LabColorSpace.pickNaturalHarmonicClockHex(avgSkyL = 88f, avgSkyA = 0f, avgSkyB = -8f)
        val darkInk = AndroidColor.parseColor(darkInkHex)
        assertTrue("Bright sky should pick dark legible clock ink, got $darkInkHex", LabColorSpace.pixelLuminance(darkInk) < 75f)

        // Murky dynamic dark scheme must be rescued by ensureDarkGlassContrastScheme
        val murkyScheme = darkColorScheme(
            primary = Color(0xFF1E1A30),
            onPrimary = Color(0xFF282438),
            primaryContainer = Color(0xFF050507),
            onPrimaryContainer = Color(0xFF2A2635),
            secondaryContainer = Color(0xFFDCD8EE),
            onSecondaryContainer = Color(0xFF22202E),
            onSurface = Color(0xFF404040)
        )
        val fixed = ensureDarkGlassContrastScheme(murkyScheme, amoledDark = false, isGlass = true)
        assertTrue(fixed.onSurface.luminance() > 0.70f)
        assertTrue(fixed.onPrimaryContainer.luminance() > 0.65f)
        assertTrue(fixed.onSecondaryContainer.luminance() > 0.65f)
        assertTrue(fixed.primary.luminance() >= 0.35f)
    }

    @Test
    fun profileShowsDeveloperCreditAndDepthStudioIsDeclutteredWithGlassOpticsDrawer() {
        composeRule.setContent {
            BackdropTheme(
                themeMode = ThemeMode.DARK,
                amoledDark = false,
                uiStyle = UiStyleMode.FROSTED_GLASS_OS,
                useDynamicColor = false
            ) {
                ProfileSettingsScreen(
                    favoriteCount = 4,
                    collectionCount = 2,
                    preferences = UserPreferencesDoc(),
                    cacheBytes = 4096L,
                    onRefreshCacheSize = {},
                    onClearCache = {},
                    onOpenSettingsPanel = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Backdrops Studio").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("developer_credit_card"))
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Developed by Isaac Iruene").assertIsDisplayed()
    }

    @Test
    fun depthStudioDeclutteredClockWorkspaceShowsDirectFontsAndGlassOpticsDrawer() {
        val sample = WallpaperCatalog.allWallpapers.first()
        val sampleBitmap = Bitmap.createBitmap(64, 96, Bitmap.Config.ARGB_8888)
        composeRule.setContent {
            BackdropTheme(
                themeMode = ThemeMode.DARK,
                amoledDark = false,
                uiStyle = UiStyleMode.FROSTED_GLASS_OS,
                useDynamicColor = false
            ) {
                var cfg by remember { mutableStateOf(ClockDesignConfig(finish = HyperOsClockFinish.BLUR_GLASS)) }
                DepthStudioScreen(
                    initialWallpaper = sample,
                    depthState = DepthStudioUiState.ScanningSubject(sampleBitmap, sample.title),
                    clockConfig = cfg,
                    canUndo = false,
                    canRedo = false,
                    isApplying = false,
                    onLoadWallpaper = { _, _ -> },
                    onPickCustomPhoto = { _, _ -> },
                    onBeginClockGesture = {},
                    onPreviewClockConfig = { cfg = it },
                    onEndClockGesture = {},
                    onCommitClockConfig = { transform -> cfg = transform(cfg) },
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
        composeRule.onNodeWithTag("depth_tab_Clock").performClick()
        composeRule.waitForIdle()

        // Decluttered font workspace: switch to Type & Surface and verify no category filter pills like "All Fonts"
        composeRule.onNodeWithTag("clock_subpill_TYPE_FINISH").performClick()
        composeRule.waitForIdle()

        assertTrue(composeRule.onAllNodesWithText("All Fonts").fetchSemanticsNodes().isEmpty())
        composeRule.onNodeWithTag("clock_font_strip").assertIsDisplayed()
        composeRule.onNodeWithTag("clock_font_strip").performScrollToNode(hasTestTag("clock_font_PLAYFAIR"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("clock_font_PLAYFAIR").assertIsDisplayed()
        composeRule.onNodeWithTag("glass_optics_drawer_toggle").performScrollTo().assertIsDisplayed()
    }
}
