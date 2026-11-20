package com.example.engine.depth

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class DepthDetectionEngineTest {

    private val engine = DepthDetectionEngine()

    @Test
    fun mountainPeakOverSmoothSunsetGradientIsolatesWithHighIoU() = runBlocking {
        val w = 360
        val h = 640
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // Smooth sunset sky gradient (deep twilight indigo -> warm coral -> apricot)
        val skyPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, h * 0.72f,
                intArrayOf(
                    Color.rgb(34, 42, 86),
                    Color.rgb(142, 92, 134),
                    Color.rgb(242, 178, 138)
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), skyPaint)

        // Sharp foreground alpine mountain peak reaching y = 0.25 * h (160px)
        val mountainPath = Path().apply {
            moveTo(0f, h * 0.58f)
            lineTo(w * 0.50f, h * 0.25f)
            lineTo(w.toFloat(), h * 0.60f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        val mountainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(26, 32, 46)
            style = Paint.Style.FILL
        }
        canvas.drawPath(mountainPath, mountainPaint)

        val result = engine.detectAndExtract(bmp, "Alpine Peak")
        assertTrue("Expected mountain peak to be depth-eligible, got $result", result.isDepthEligible)
        val cutout = result.cutoutBitmap
        assertNotNull("Expected cutout bitmap for mountain peak", cutout)
        assertTrue("Clock top fraction should sit above/behind peak, got ${result.recommendedClockTopFraction}", result.recommendedClockTopFraction in 0.07f..0.28f)
        assertTrue("Expected peak fraction near 0.25, got ${result.subjectTopFraction}", abs(result.subjectTopFraction - 0.25f) < 0.05f)

        // Compute pixel-level Intersection-over-Union against mathematical triangle horizon
        var intersection = 0
        var union = 0
        val cutoutPixels = IntArray(w * h)
        cutout!!.getPixels(cutoutPixels, 0, w, 0, 0, w, h)
        for (y in 0 until h) {
            val leftEdge = w * 0.50f - (y - h * 0.25f) * (w * 0.50f / (h * 0.33f))
            val rightEdge = w * 0.50f + (y - h * 0.25f) * (w * 0.50f / (h * 0.35f))
            for (x in 0 until w) {
                val gtFg = y >= h * 0.25f && x >= leftEdge && x <= rightEdge
                val predFg = ((cutoutPixels[y * w + x] ushr 24) and 0xFF) >= 128
                if (gtFg && predFg) intersection++
                if (gtFg || predFg) union++
            }
        }
        val iou = intersection.toFloat() / union.coerceAtLeast(1)
        assertTrue("Expected mountain cutout IoU >= 0.90, got $iou", iou >= 0.90f)
    }

    @Test
    fun luminousFullMoonInNightSkyIsExtractedCleanly() = runBlocking {
        val w = 360
        val h = 640
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        canvas.drawColor(Color.rgb(8, 10, 18))

        val cx = w * 0.50f
        val cy = h * 0.34f
        val r = w * 0.17f

        val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(232, 234, 240)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, r, moonPaint)

        val result = engine.detectAndExtract(bmp, "Obsidian Full Moon")
        assertTrue("Expected moon orb to be depth-eligible, got $result", result.isDepthEligible)
        assertEquals(DepthSubjectCategory.CELESTIAL_ORB, result.subjectCategory)
        val cutout = result.cutoutBitmap
        assertNotNull("Expected moon cutout bitmap", cutout)

        var intersection = 0
        var union = 0
        val pixels = IntArray(w * h)
        cutout!!.getPixels(pixels, 0, w, 0, 0, w, h)
        val rSq = r * r
        for (y in 0 until h) {
            val dy = y - cy
            for (x in 0 until w) {
                val dx = x - cx
                val gtFg = (dx * dx + dy * dy) <= rSq
                val predFg = ((pixels[y * w + x] ushr 24) and 0xFF) >= 128
                if (gtFg && predFg) intersection++
                if (gtFg || predFg) union++
            }
        }
        val iou = intersection.toFloat() / union.coerceAtLeast(1)
        assertTrue("Expected moon cutout IoU >= 0.88, got $iou", iou >= 0.88f)
    }

    @Test
    fun flatSmoothGradientCanvasRejectsFalseDepthArtifacts() = runBlocking {
        val w = 360
        val h = 640
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(Color.rgb(180, 165, 224), Color.rgb(245, 210, 198)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        val result = engine.detectAndExtract(bmp, "Aurora Prism Diffusion")
        assertFalse("Flat smooth canvas must not report false foreground depth, got $result", result.isDepthEligible)
        assertNull("Flat smooth canvas must not return a cutout bitmap", result.cutoutBitmap)
        assertEquals(DepthSubjectCategory.FLAT_CANVAS, result.subjectCategory)
    }

    @Test
    fun offCenterLighthouseSpireRecommendsOppositeAlignmentAndHighIoU() = runBlocking {
        val w = 360
        val h = 640
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val skyPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(Color.rgb(24, 52, 82), Color.rgb(122, 168, 198)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), skyPaint)

        // Tapered coastal lighthouse tower on the right side (x ~ 0.72..0.84, y ~ 0.22..0.90)
        val towerPath = Path().apply {
            moveTo(w * 0.74f, h * 0.22f)
            lineTo(w * 0.80f, h * 0.22f)
            lineTo(w * 0.84f, h * 0.90f)
            lineTo(w * 0.70f, h * 0.90f)
            close()
        }
        val towerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(228, 86, 62)
            style = Paint.Style.FILL
        }
        canvas.drawPath(towerPath, towerPaint)

        val result = engine.detectAndExtract(bmp, "Atlantic Beacon Tower")
        assertTrue("Expected off-center spire to be depth-eligible, got $result", result.isDepthEligible)
        assertEquals(ClockHorizontalAlign.START, result.recommendedAlign)
        val cutout = result.cutoutBitmap
        assertNotNull("Expected cutout bitmap for lighthouse spire", cutout)

        var intersection = 0
        var union = 0
        val pixels = IntArray(w * h)
        cutout!!.getPixels(pixels, 0, w, 0, 0, w, h)
        for (y in 0 until h) {
            val fracY = ((y - h * 0.22f) / (h * 0.68f)).coerceIn(0f, 1f)
            val left = w * (0.74f - 0.04f * fracY)
            val right = w * (0.80f + 0.04f * fracY)
            for (x in 0 until w) {
                val gtFg = y >= h * 0.22f && y <= h * 0.90f && x >= left && x <= right
                val predFg = ((pixels[y * w + x] ushr 24) and 0xFF) >= 128
                if (gtFg && predFg) intersection++
                if (gtFg || predFg) union++
            }
        }
        val iou = intersection.toFloat() / union.coerceAtLeast(1)
        assertTrue("Expected lighthouse cutout IoU >= 0.88, got $iou", iou >= 0.88f)
    }

    @Test
    fun highResSnowCappedMountainUpsamplesCleanlyOverTexturedSky() = runBlocking {
        val w = 1080
        val h = 1920
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // Rich twilight-to-peach sky with gentle atmospheric cloud bands
        val skyPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, h * 0.72f,
                intArrayOf(
                    Color.rgb(38, 62, 108),
                    Color.rgb(120, 128, 176),
                    Color.rgb(228, 178, 166)
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), skyPaint)

        // Two-tone mountain (dark basalt body + bright snow-capped summit above y = 0.28 * h)
        val mountainPath = Path().apply {
            moveTo(0f, h * 0.62f)
            lineTo(w * 0.52f, h * 0.28f)
            lineTo(w.toFloat(), h * 0.64f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        val mountainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, h * 0.28f, 0f, h * 0.68f,
                intArrayOf(
                    Color.rgb(238, 244, 252),
                    Color.rgb(92, 108, 128),
                    Color.rgb(22, 28, 40)
                ),
                floatArrayOf(0f, 0.28f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawPath(mountainPath, mountainPaint)

        val result = engine.detectAndExtract(bmp, "Fitz Roy Granite Spire")
        assertTrue("Expected high-res snow peak to be depth-eligible, got $result", result.isDepthEligible)
        val cutout = result.cutoutBitmap
        assertNotNull("Expected upsampled cutout bitmap", cutout)
        assertEquals(w, cutout!!.width)
        assertEquals(h, cutout.height)

        // Verify snow peak summit pixel (inside tip) is opaque and clear sky top is transparent
        val skyPixelAlpha = (cutout.getPixel(w / 2, (h * 0.12f).toInt()) ushr 24) and 0xFF
        val snowTipAlpha = (cutout.getPixel((w * 0.52f).toInt(), (h * 0.34f).toInt()) ushr 24) and 0xFF
        val baseRockAlpha = (cutout.getPixel(w / 2, (h * 0.78f).toInt()) ushr 24) and 0xFF
        assertTrue("Sky headroom must stay transparent, got alpha=$skyPixelAlpha", skyPixelAlpha < 20)
        assertTrue("Snow peak interior must be solid opaque, got alpha=$snowTipAlpha", snowTipAlpha > 230)
        assertTrue("Lower rock ridge must be solid opaque, got alpha=$baseRockAlpha", baseRockAlpha > 230)
    }

    @Test
    fun detachedUpperSkyCloudIsStrippedAndClockTuckIsPositive() = runBlocking {
        val w = 360
        val h = 640
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val skyPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, h * 0.72f,
                intArrayOf(
                    Color.rgb(36, 48, 92),
                    Color.rgb(164, 122, 148),
                    Color.rgb(244, 190, 144)
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), skyPaint)

        // Detached dark cloud bank high in the upper sky (y ~ 0.08..0.15)
        val strayCloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(16, 20, 34)
            style = Paint.Style.FILL
        }
        canvas.drawOval(w * 0.14f, h * 0.07f, w * 0.44f, h * 0.14f, strayCloudPaint)

        // Crisp foreground mountain peak starting at y = 0.25 * h
        val mountainPath = Path().apply {
            moveTo(0f, h * 0.60f)
            lineTo(w * 0.52f, h * 0.25f)
            lineTo(w.toFloat(), h * 0.62f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        val mountainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(22, 26, 38)
            style = Paint.Style.FILL
        }
        canvas.drawPath(mountainPath, mountainPaint)

        val result = engine.detectAndExtract(bmp, "Peak With High Cloud")
        assertTrue("Expected mountain peak to remain depth-eligible, got $result", result.isDepthEligible)
        val cutout = result.cutoutBitmap
        assertNotNull("Expected cutout bitmap", cutout)
        assertTrue("Expected peak near 0.25 without stray cloud pull, got ${result.subjectTopFraction}", result.subjectTopFraction >= 0.20f)
        assertTrue("Expected non-zero clock tuck fraction, got ${result.recommendedClockOverlapFraction}", result.recommendedClockOverlapFraction > 0.03f)

        val cloudRegionAlpha = (cutout!!.getPixel((w * 0.28f).toInt(), (h * 0.105f).toInt()) ushr 24) and 0xFF
        assertTrue("Detached upper sky cloud should be stripped, got alpha=$cloudRegionAlpha", cloudRegionAlpha < 25)
    }
}
