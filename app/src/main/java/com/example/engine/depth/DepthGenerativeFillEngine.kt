package com.example.engine.depth

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.example.data.repository.AiAspectRatio
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class GenerativeFillMood(
    val label: String,
    val promptDirective: String
) {
    SEAMLESS_SCENE(
        label = "Seamless Match",
        promptDirective = "Continue the exact sky gradient, clouds, lighting direction, horizon texture, and photographic grain naturally."
    ),
    EXPANSIVE_SKY(
        label = "Open Sky",
        promptDirective = "Extend primarily into clean, airy upper sky atmosphere with soft tonal depth suitable behind a lock-screen clock."
    ),
    MINIMAL_STUDIO(
        label = "Studio Backdrop",
        promptDirective = "Extend outward into a clean, minimalist matte studio backdrop matching the existing palette and soft lighting falloff."
    ),
    CUSTOM(
        label = "Custom Cue",
        promptDirective = ""
    )
}

data class FramedCanvasSpec(
    val sourceWidth: Int,
    val sourceHeight: Int,
    val viewportWidth: Float,
    val viewportHeight: Float,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val placedRect: RectF,
    val visibleInnerRect: RectF,
    val gapLeft: Boolean,
    val gapTop: Boolean,
    val gapRight: Boolean,
    val gapBottom: Boolean
) {
    val hasUncoveredBorders: Boolean
        get() = gapLeft || gapTop || gapRight || gapBottom
}

data class OutpaintPreparedInput(
    val spec: FramedCanvasSpec,
    val baseLayerBitmap: Bitmap,
    val aiGuideBitmap: Bitmap,
    val recommendedAspectRatio: AiAspectRatio,
    val prompt: String
)

/**
 * Deterministic geometry, guide-canvas preparation, and boundary-preserving feather compositor
 * for Depth Studio's Gemini Generative Fill (Wallpaper Extend) feature.
 */
object DepthGenerativeFillEngine {

    private const val WORKING_WIDTH = 1080
    private const val AI_GUIDE_MAX_EDGE = 1152

    fun computeFrameSpec(
        sourceWidth: Int,
        sourceHeight: Int,
        viewportWidth: Float,
        viewportHeight: Float,
        zoomScale: Float,
        panOffsetX: Float,
        panOffsetY: Float
    ): FramedCanvasSpec {
        val safeVw = viewportWidth.coerceAtLeast(1f)
        val safeVh = viewportHeight.coerceAtLeast(1f)
        val srcW = sourceWidth.coerceAtLeast(1).toFloat()
        val srcH = sourceHeight.coerceAtLeast(1).toFloat()

        val baseScale = max(safeVw / srcW, safeVh / srcH)
        val effectiveScale = baseScale * zoomScale.coerceAtLeast(0.45f)

        val drawnW = srcW * effectiveScale
        val drawnH = srcH * effectiveScale
        val centerX = safeVw * 0.5f + panOffsetX
        val centerY = safeVh * 0.5f + panOffsetY

        val vpLeft = centerX - drawnW * 0.5f
        val vpTop = centerY - drawnH * 0.5f
        val vpRight = centerX + drawnW * 0.5f
        val vpBottom = centerY + drawnH * 0.5f

        val threshold = 2.0f
        val gapLeft = vpLeft > threshold
        val gapTop = vpTop > threshold
        val gapRight = vpRight < safeVw - threshold
        val gapBottom = vpBottom < safeVh - threshold

        val targetW = WORKING_WIDTH
        val targetH = ((targetW.toFloat() * safeVh / safeVw).roundToInt()).coerceIn(640, 2400)
        val sx = targetW / safeVw
        val sy = targetH / safeVh

        val placedRect = RectF(vpLeft * sx, vpTop * sy, vpRight * sx, vpBottom * sy)
        val visibleInnerRect = RectF(
            placedRect.left.coerceIn(0f, targetW.toFloat()),
            placedRect.top.coerceIn(0f, targetH.toFloat()),
            placedRect.right.coerceIn(0f, targetW.toFloat()),
            placedRect.bottom.coerceIn(0f, targetH.toFloat())
        )

        return FramedCanvasSpec(
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            viewportWidth = safeVw,
            viewportHeight = safeVh,
            canvasWidth = targetW,
            canvasHeight = targetH,
            placedRect = placedRect,
            visibleInnerRect = visibleInnerRect,
            gapLeft = gapLeft,
            gapTop = gapTop,
            gapRight = gapRight,
            gapBottom = gapBottom
        )
    }

    fun prepareOutpaintInput(
        sourceBitmap: Bitmap,
        spec: FramedCanvasSpec,
        mood: GenerativeFillMood,
        customHint: String = ""
    ): OutpaintPreparedInput {
        val cw = spec.canvasWidth
        val ch = spec.canvasHeight

        val baseLayer = createBitmap(cw, ch)
        val baseCanvas = Canvas(baseLayer)
        val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        baseCanvas.drawBitmap(sourceBitmap, null, spec.placedRect, imagePaint)

        // Build unmistakably neutral light-gray hatched canvas borders around the shifted photo
        val guideFull = createBitmap(cw, ch)
        val guideCanvas = Canvas(guideFull)
        guideCanvas.drawColor(Color.rgb(228, 231, 236))

        val hatchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(206, 211, 219)
            strokeWidth = 3f
        }
        val step = 36f
        var offset = -ch.toFloat()
        while (offset < cw + ch) {
            guideCanvas.drawLine(offset, 0f, offset + ch, ch.toFloat(), hatchPaint)
            offset += step
        }

        // Draw the positioned original photo crisply over the hatched canvas
        guideCanvas.drawBitmap(sourceBitmap, null, spec.placedRect, imagePaint)

        val maxEdge = max(cw, ch)
        val aiGuide = if (maxEdge > AI_GUIDE_MAX_EDGE) {
            val scale = AI_GUIDE_MAX_EDGE.toFloat() / maxEdge.toFloat()
            val scaled = guideFull.scale(
                (cw * scale).roundToInt().coerceAtLeast(1),
                (ch * scale).roundToInt().coerceAtLeast(1)
            )
            guideFull.recycle()
            scaled
        } else {
            guideFull
        }

        val ratio = cw.toFloat() / ch.toFloat()
        val aspectRatio = when {
            ratio < 0.65f -> AiAspectRatio.RATIO_9_16
            ratio < 0.85f -> AiAspectRatio.RATIO_3_4
            ratio <= 1.15f -> AiAspectRatio.RATIO_1_1
            else -> AiAspectRatio.RATIO_16_9
        }

        val missingRegions = buildList {
            if (spec.gapTop) add("top area")
            if (spec.gapBottom) add("bottom area")
            if (spec.gapLeft) add("left side")
            if (spec.gapRight) add("right side")
        }.joinToString(", ").ifEmpty { "uncovered border edges" }

        val styleClause = if (mood == GenerativeFillMood.CUSTOM && customHint.isNotBlank()) {
            "User creative direction: ${customHint.trim()}."
        } else {
            val extra = customHint.trim()
            if (extra.isNotEmpty()) "${mood.promptDirective} Additional cue: $extra."
            else mood.promptDirective
        }

        val prompt = "Outpaint and seamlessly extend this mobile wallpaper to fill ONLY the uncovered gray cross-hatched areas ($missingRegions). " +
            "Keep the existing photograph content, subject proportions, colors, and position 100% intact without cropping, moving, or adding extra subjects inside the original photo area. " +
            "Remove all cross-hatched gray border lines completely and replace them with natural seamless scenery. " +
            styleClause

        return OutpaintPreparedInput(
            spec = spec,
            baseLayerBitmap = baseLayer,
            aiGuideBitmap = aiGuide,
            recommendedAspectRatio = aspectRatio,
            prompt = prompt
        )
    }

    /**
     * Composites the Gemini outpainted output ONLY into uncovered border regions with a smooth
     * inward cubic-Hermite feather along uncovered edges, keeping interior original photo pixels bit-exact.
     */
    fun blendOutpaintedFill(
        baseLayerBitmap: Bitmap,
        generatedBitmap: Bitmap,
        spec: FramedCanvasSpec
    ): Bitmap {
        val cw = spec.canvasWidth
        val ch = spec.canvasHeight
        val alignedGen = if (generatedBitmap.width == cw && generatedBitmap.height == ch) {
            generatedBitmap
        } else {
            generatedBitmap.scale(cw, ch)
        }

        val n = cw * ch
        val origPixels = IntArray(n)
        val genPixels = IntArray(n)
        baseLayerBitmap.getPixels(origPixels, 0, cw, 0, 0, cw, ch)
        alignedGen.getPixels(genPixels, 0, cw, 0, 0, cw, ch)
        if (alignedGen !== generatedBitmap) {
            alignedGen.recycle()
        }

        val rLeft = spec.visibleInnerRect.left
        val rTop = spec.visibleInnerRect.top
        val rRight = spec.visibleInnerRect.right
        val rBottom = spec.visibleInnerRect.bottom
        val featherPx = max(12f, cw / 68f)

        val outPixels = IntArray(n)
        for (y in 0 until ch) {
            val row = y * cw
            val fy = y + 0.5f
            val insideY = fy >= rTop && fy <= rBottom
            val dyTop = if (spec.gapTop) fy - rTop else Float.POSITIVE_INFINITY
            val dyBot = if (spec.gapBottom) rBottom - fy else Float.POSITIVE_INFINITY
            val minDy = min(dyTop, dyBot)

            for (x in 0 until cw) {
                val idx = row + x
                val fx = x + 0.5f
                val origArgb = origPixels[idx]
                val genArgb = genPixels[idx]

                if (!insideY || fx < rLeft || fx > rRight || ((origArgb ushr 24) and 0xFF) < 250) {
                    outPixels[idx] = (0xFF shl 24) or (genArgb and 0x00FFFFFF)
                    continue
                }

                val dxLeft = if (spec.gapLeft) fx - rLeft else Float.POSITIVE_INFINITY
                val dxRight = if (spec.gapRight) rRight - fx else Float.POSITIVE_INFINITY
                val dInward = min(min(dxLeft, dxRight), minDy)

                if (dInward >= featherPx) {
                    outPixels[idx] = (0xFF shl 24) or (origArgb and 0x00FFFFFF)
                } else {
                    val t = (dInward / featherPx).coerceIn(0f, 1f)
                    val wOrig = t * t * (3f - 2f * t)
                    val wGen = 1f - wOrig

                    val oR = (origArgb ushr 16) and 0xFF
                    val oG = (origArgb ushr 8) and 0xFF
                    val oB = origArgb and 0xFF

                    val gR = (genArgb ushr 16) and 0xFF
                    val gG = (genArgb ushr 8) and 0xFF
                    val gB = genArgb and 0xFF

                    val mR = (oR * wOrig + gR * wGen).roundToInt().coerceIn(0, 255)
                    val mG = (oG * wOrig + gG * wGen).roundToInt().coerceIn(0, 255)
                    val mB = (oB * wOrig + gB * wGen).roundToInt().coerceIn(0, 255)
                    outPixels[idx] = (0xFF shl 24) or (mR shl 16) or (mG shl 8) or mB
                }
            }
        }

        val result = createBitmap(cw, ch)
        result.setPixels(outPixels, 0, cw, 0, 0, cw, ch)
        return result
    }
}
