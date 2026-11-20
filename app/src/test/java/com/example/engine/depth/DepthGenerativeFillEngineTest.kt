package com.example.engine.depth

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class DepthGenerativeFillEngineTest {

    @Test
    fun computeFrameSpecDetectsNoGapsWhenCenteredAtDefaultScale() {
        val spec = DepthGenerativeFillEngine.computeFrameSpec(
            sourceWidth = 1080,
            sourceHeight = 1920,
            viewportWidth = 1080f,
            viewportHeight = 1920f,
            zoomScale = 1.0f,
            panOffsetX = 0f,
            panOffsetY = 0f
        )
        assertFalse(spec.hasUncoveredBorders)
        assertFalse(spec.gapLeft)
        assertFalse(spec.gapTop)
        assertFalse(spec.gapRight)
        assertFalse(spec.gapBottom)
    }

    @Test
    fun computeFrameSpecDetectsTopGapWhenDraggedDownAndAllGapsWhenZoomedOut() {
        val draggedDownSpec = DepthGenerativeFillEngine.computeFrameSpec(
            sourceWidth = 1080,
            sourceHeight = 1920,
            viewportWidth = 1080f,
            viewportHeight = 1920f,
            zoomScale = 1.0f,
            panOffsetX = 0f,
            panOffsetY = 180f
        )
        assertTrue(draggedDownSpec.hasUncoveredBorders)
        assertTrue(draggedDownSpec.gapTop)
        assertFalse(draggedDownSpec.gapBottom)
        assertFalse(draggedDownSpec.gapLeft)
        assertFalse(draggedDownSpec.gapRight)

        val zoomedOutSpec = DepthGenerativeFillEngine.computeFrameSpec(
            sourceWidth = 1080,
            sourceHeight = 1920,
            viewportWidth = 1080f,
            viewportHeight = 1920f,
            zoomScale = 0.65f,
            panOffsetX = 0f,
            panOffsetY = 0f
        )
        assertTrue(zoomedOutSpec.hasUncoveredBorders)
        assertTrue(zoomedOutSpec.gapLeft)
        assertTrue(zoomedOutSpec.gapTop)
        assertTrue(zoomedOutSpec.gapRight)
        assertTrue(zoomedOutSpec.gapBottom)
    }

    @Test
    fun prepareAndBlendOutpaintedFillPreservesInteriorAndFillsUncoveredBorders() {
        val src = Bitmap.createBitmap(360, 640, Bitmap.Config.ARGB_8888)
        Canvas(src).drawColor(Color.rgb(32, 96, 212))

        val spec = DepthGenerativeFillEngine.computeFrameSpec(
            sourceWidth = 360,
            sourceHeight = 640,
            viewportWidth = 360f,
            viewportHeight = 640f,
            zoomScale = 0.65f,
            panOffsetX = 0f,
            panOffsetY = 0f
        )
        val prepared = DepthGenerativeFillEngine.prepareOutpaintInput(
            sourceBitmap = src,
            spec = spec,
            mood = GenerativeFillMood.EXPANSIVE_SKY,
            customHint = "soft twilight cirrus"
        )

        assertTrue(prepared.prompt.contains("top area"))
        assertTrue(prepared.prompt.contains("bottom area"))
        assertTrue(prepared.prompt.contains("soft twilight cirrus"))

        // Guide canvas center must preserve original blue photo while uncovered corner is hatched neutral gray
        val cx = prepared.aiGuideBitmap.width / 2
        val cy = prepared.aiGuideBitmap.height / 2
        val centerBlue = Color.blue(prepared.aiGuideBitmap.getPixel(cx, cy))
        assertTrue("Expected original photo preserved at center, blue=$centerBlue", centerBlue > 180)

        val cornerColor = prepared.aiGuideBitmap.getPixel(6, 6)
        assertTrue(
            "Expected uncovered corner to contain neutral light-gray cue",
            Color.red(cornerColor) > 180 && Color.green(cornerColor) > 180 && Color.blue(cornerColor) > 180
        )

        // Simulate Gemini returning a coral outpainted image
        val generated = Bitmap.createBitmap(spec.canvasWidth, spec.canvasHeight, Bitmap.Config.ARGB_8888)
        Canvas(generated).drawColor(Color.rgb(235, 95, 72))

        val blended = DepthGenerativeFillEngine.blendOutpaintedFill(
            baseLayerBitmap = prepared.baseLayerBitmap,
            generatedBitmap = generated,
            spec = spec
        )
        assertEquals(spec.canvasWidth, blended.width)
        assertEquals(spec.canvasHeight, blended.height)

        // Uncovered corner takes generated coral pixels
        val outCorner = blended.getPixel(8, 8)
        assertEquals(235, Color.red(outCorner))
        assertEquals(95, Color.green(outCorner))

        // Interior far from the seam stays bit-exact original blue
        val outCenter = blended.getPixel(spec.canvasWidth / 2, spec.canvasHeight / 2)
        assertEquals(32, Color.red(outCenter))
        assertEquals(96, Color.green(outCenter))
        assertEquals(212, Color.blue(outCenter))

        prepared.baseLayerBitmap.recycle()
        prepared.aiGuideBitmap.recycle()
        generated.recycle()
        blended.recycle()
        src.recycle()
    }
}
