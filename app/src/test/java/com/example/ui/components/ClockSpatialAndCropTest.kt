package com.example.ui.components

import android.content.Context
import androidx.compose.ui.unit.IntSize
import androidx.test.core.app.ApplicationProvider
import com.example.data.media.WallpaperMediaService
import com.example.data.repository.GuestLocalUserStore
import com.example.ui.screens.computeCenteredViewportCropRect
import com.example.ui.viewmodel.DepthStudioViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ClockSpatialAndCropTest {

    private val phoneViewport = IntSize(width = 1080, height = 2400)

    @Test
    fun cornerBottomRightDragAnchorsTopLeftAndResizesBothAxesInSingleGesture() {
        val startConfig = ClockDesignConfig(
            widthScale = 1.0f,
            heightScale = 1.0f,
            offsetXFraction = 0f,
            placementY = 0.20f
        )
        val startRect = ClockSpatialMath.computeClockBoxRect(
            unscaledWidthPx = 400f,
            unscaledHeightPx = 200f,
            viewportSize = phoneViewport,
            config = startConfig
        )

        val updatedConfig = ClockSpatialMath.applyDragDelta(
            target = ClockGripTarget.CORNER_BR,
            startConfig = startConfig,
            startRect = startRect,
            totalDx = 160f,
            totalDy = 120f,
            viewportSize = phoneViewport
        )
        val updatedRect = ClockSpatialMath.computeClockBoxRect(
            unscaledWidthPx = 400f,
            unscaledHeightPx = 200f,
            viewportSize = phoneViewport,
            config = updatedConfig
        )

        assertTrue("Expected widthScale to grow beyond 1.0", updatedConfig.widthScale > 1.35f)
        assertTrue("Expected heightScale to grow beyond 1.0", updatedConfig.heightScale > 1.55f)
        assertEquals("Top-Left anchor X must remain fixed", startRect.leftPx, updatedRect.leftPx, 2.5f)
        assertEquals("Top-Left anchor Y must remain fixed", startRect.topPx, updatedRect.topPx, 2.5f)
    }

    @Test
    fun cornerBottomLeftDragAnchorsTopRightAndSupportsNegativeThenPositiveDeltas() {
        val startConfig = ClockDesignConfig(
            widthScale = 1.0f,
            heightScale = 1.0f,
            offsetXFraction = 0f,
            placementY = 0.20f
        )
        val startRect = ClockSpatialMath.computeClockBoxRect(
            unscaledWidthPx = 400f,
            unscaledHeightPx = 200f,
            viewportSize = phoneViewport,
            config = startConfig
        )

        val stretchedLeftDown = ClockSpatialMath.applyDragDelta(
            target = ClockGripTarget.CORNER_BL,
            startConfig = startConfig,
            startRect = startRect,
            totalDx = -140f,
            totalDy = 100f,
            viewportSize = phoneViewport
        )
        val stretchedRect = ClockSpatialMath.computeClockBoxRect(
            unscaledWidthPx = 400f,
            unscaledHeightPx = 200f,
            viewportSize = phoneViewport,
            config = stretchedLeftDown
        )

        assertTrue(stretchedLeftDown.widthScale > 1.25f)
        assertTrue(stretchedLeftDown.heightScale > 1.40f)
        assertEquals("Top-Right anchor X must remain fixed", startRect.rightPx, stretchedRect.rightPx, 2.5f)
        assertEquals("Top-Right anchor Y must remain fixed", startRect.topPx, stretchedRect.topPx, 2.5f)
    }

    @Test
    fun bodyDragPermitsFullCanvasPlacementWhileStayingInsideSafeViewportBounds() {
        val startConfig = ClockDesignConfig()
        val startRect = ClockSpatialMath.computeClockBoxRect(400f, 200f, phoneViewport, startConfig)

        val movedBottomRight = ClockSpatialMath.applyDragDelta(
            target = ClockGripTarget.BODY,
            startConfig = startConfig,
            startRect = startRect,
            totalDx = 4000f,
            totalDy = 6000f,
            viewportSize = phoneViewport,
            minMarginPx = 12f
        )
        val clampedRect = ClockSpatialMath.computeClockBoxRect(400f, 200f, phoneViewport, movedBottomRight, minMarginPx = 12f)

        assertTrue("Clock should reach lower canvas area", movedBottomRight.placementY > 0.70f)
        assertTrue("Clock should reach right edge without disappearing", clampedRect.rightPx <= phoneViewport.width - 11f)
        assertTrue("Clock bottom must stay inside viewport", clampedRect.bottomPx <= phoneViewport.height - 11f)
    }

    @Test
    fun centerThresholdDetectsOffCenterAndCenterOnScreenPreservesVerticalY() {
        val centered = ClockDesignConfig(offsetXFraction = 0f, placementY = 0.14f)
        assertFalse(ClockSpatialMath.isHorizontallyOffCenter(centered))

        val movedAway = centered.copy(offsetXFraction = 0.12f, placementY = 0.42f)
        assertTrue(ClockSpatialMath.isHorizontallyOffCenter(movedAway))

        // Center is horizontal-only and strictly preserves user's placementY
        val recentered = ClockSpatialMath.centerClock(movedAway)
        assertEquals(0f, recentered.offsetXFraction, 0.0001f)
        assertEquals(0.42f, recentered.placementY, 0.0001f)
        assertFalse(ClockSpatialMath.isHorizontallyOffCenter(recentered))

        // Auto Snap aligns both horizontally and vertically to the Depth Engine pocket
        val snapped = ClockSpatialMath.autoSnapToDepth(movedAway, targetPlacementY = 0.16f)
        assertEquals(0f, snapped.offsetXFraction, 0.0001f)
        assertEquals(0.16f, snapped.placementY, 0.0001f)
    }

    @Test
    fun resetProportionsRestoresBaselineNonDestructivelyAndJsonRoundTrips() {
        val original = ClockDesignConfig(
            face = HyperOsClockFace.CLASSIC_UTILITY,
            font = HyperOsClockFont.OUTFIT,
            finish = HyperOsClockFinish.STRIPED,
            widthScale = 1.85f,
            heightScale = 1.60f,
            is24HourFormat = false,
            signatureText = "QUIET HORIZON",
            customPrimaryArgb = 0xFFE7DEFFL,
            customSecondaryArgb = 0xFFCDEEE2L,
            depthOverlapEnabled = false
        )
        assertTrue(original.hasCustomProportions)
        val reset = ClockSpatialMath.resetProportions(original)
        assertEquals(1.0f, reset.widthScale, 0.001f)
        assertEquals(1.0f, reset.heightScale, 0.001f)
        assertEquals("Signature preserved after proportion reset", "QUIET HORIZON", reset.signatureText)
        assertEquals(HyperOsClockFinish.STRIPED, reset.finish)

        val decoded = ClockDesignConfig.fromJsonString(original.toJsonString())
        assertNotNull(decoded)
        assertEquals(original.face, decoded?.face)
        assertEquals(original.finish, decoded?.finish)
        assertEquals(original.is24HourFormat, decoded?.is24HourFormat)
        assertEquals(original.signatureText, decoded?.signatureText)
        assertEquals(original.customPrimaryArgb, decoded?.customPrimaryArgb)
        assertEquals(original.depthOverlapEnabled, decoded?.depthOverlapEnabled)
    }

    @Test
    fun depthStudioViewModelSupportsUndoRedoAndLocalPersistence() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val store = GuestLocalUserStore(ctx)
        val vm = DepthStudioViewModel(
            mediaService = WallpaperMediaService(ctx),
            localStore = store
        )
        assertFalse(vm.canUndo.value)
        assertFalse(vm.canRedo.value)

        vm.commitClockConfig { it.copy(face = HyperOsClockFace.MAGAZINE_STACKED) }
        assertTrue(vm.canUndo.value)
        assertEquals(HyperOsClockFace.MAGAZINE_STACKED, vm.clockConfig.value.face)

        vm.beginClockGesture(vm.clockConfig.value)
        vm.previewClockConfig(vm.clockConfig.value.copy(widthScale = 1.75f))
        vm.endClockGesture()
        assertEquals(1.75f, vm.clockConfig.value.widthScale, 0.001f)

        vm.undoClockEdit()
        assertEquals(1.0f, vm.clockConfig.value.widthScale, 0.001f)
        assertTrue(vm.canRedo.value)

        vm.redoClockEdit()
        assertEquals(1.75f, vm.clockConfig.value.widthScale, 0.001f)
        assertEquals(1.75f, store.loadClockDesignConfig()?.widthScale ?: 0f, 0.001f)
    }

    @Test
    fun viewportCropPreservesExactCenterOnLockAndOptionalWiderParallax() {
        // 9:16 source (2160x3840) onto 9:20 phone display (aspect 0.45) with zero pan/zoom
        val lockCrop = computeCenteredViewportCropRect(
            sourceWidth = 2160,
            sourceHeight = 3840,
            zoomScale = 1.0f,
            panOffsetX = 0f,
            panOffsetY = 0f,
            viewportWidthPx = 1080f,
            viewportHeightPx = 2400f,
            targetScreenAspect = 1080f / 2400f,
            allowParallaxWidth = false
        )
        assertEquals(3840, lockCrop.height)
        assertEquals(1728, lockCrop.width)
        val lockCenterX = lockCrop.left + lockCrop.width / 2f
        assertTrue("Crop center must stay centered on bitmap X", abs(lockCenterX - 1080f) <= 1.5f)

        val homeParallaxCrop = computeCenteredViewportCropRect(
            sourceWidth = 2160,
            sourceHeight = 3840,
            zoomScale = 1.0f,
            panOffsetX = 0f,
            panOffsetY = 0f,
            viewportWidthPx = 1080f,
            viewportHeightPx = 2400f,
            targetScreenAspect = 1080f / 2400f,
            allowParallaxWidth = true
        )
        assertTrue("Parallax crop should provide extra centered width", homeParallaxCrop.width > lockCrop.width)
        val homeCenterX = homeParallaxCrop.left + homeParallaxCrop.width / 2f
        assertTrue("Parallax crop must still be horizontally centered", abs(homeCenterX - 1080f) <= 1.5f)
    }

    @Test
    fun aspectAdaptiveSwitchingMorphsSmoothlyBetweenVerticalBalancedAndWideUnlessLocked() {
        val vp = IntSize(1080, 2400)
        // Start in stacked vertical Magazine mode stretched out wide
        val wideStretched = ClockDesignConfig(
            face = HyperOsClockFace.MAGAZINE_STACKED,
            font = HyperOsClockFace.MAGAZINE_STACKED.recommendedFont,
            widthScale = 2.0f,
            heightScale = 0.65f,
            aspectAdaptiveEnabled = true
        )
        val morphedToWide = ClockSpatialMath.applyAspectAdaptiveResolution(
            updatedConfig = wideStretched,
            unscaledWidthPx = 280f,
            unscaledHeightPx = 260f,
            viewportSize = vp
        )
        assertEquals(ClockAspectProfile.WIDE_HORIZONTAL, morphedToWide.face.aspectProfile)
        assertEquals(HyperOsClockFace.POSTER_SPLIT, morphedToWide.face)

        // Start in wide Horizon Silk stretched tall vertically -> morphs to Canyon Stack
        val tallStretched = ClockDesignConfig(
            face = HyperOsClockFace.HORIZON_SILK,
            font = HyperOsClockFace.HORIZON_SILK.recommendedFont,
            widthScale = 0.65f,
            heightScale = 2.1f,
            aspectAdaptiveEnabled = true
        )
        val morphedToTall = ClockSpatialMath.applyAspectAdaptiveResolution(
            updatedConfig = tallStretched,
            unscaledWidthPx = 300f,
            unscaledHeightPx = 220f,
            viewportSize = vp
        )
        assertEquals(ClockAspectProfile.TALL_VERTICAL, morphedToTall.face.aspectProfile)
        assertEquals(HyperOsClockFace.CANYON_MONUMENT, morphedToTall.face)

        // With aspect lock active, face remains unchanged
        val lockedStretched = wideStretched.copy(aspectAdaptiveEnabled = false)
        val unchanged = ClockSpatialMath.applyAspectAdaptiveResolution(
            updatedConfig = lockedStretched,
            unscaledWidthPx = 280f,
            unscaledHeightPx = 260f,
            viewportSize = vp
        )
        assertEquals(HyperOsClockFace.MAGAZINE_STACKED, unchanged.face)
    }
}
