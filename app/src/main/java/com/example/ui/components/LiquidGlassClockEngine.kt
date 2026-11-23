package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.ColorMatrix as AndroidColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint as AndroidPaint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.createBitmap
import com.example.ui.screens.CanvasLookFilter
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class LiquidGlassPreset(
    val label: String,
    val tintArgb: Long,
    val tintOpacity: Float,
    val saturation: Float,
    val chromaticAberration: Float
) {
    CLEAR_CRYSTAL("Clear Crystal", 0xFFFFFFFFL, 0.08f, 1.45f, 0.30f),
    COOL_ICE("Cool Ice", 0xFFCFE8FFL, 0.14f, 1.48f, 0.38f),
    WARM_QUARTZ("Warm Quartz", 0xFFFFE0C2L, 0.15f, 1.52f, 0.28f),
    PRISMATIC("Prismatic", 0xFFEFE6FFL, 0.12f, 1.70f, 0.72f)
}

/**
 * Physically-inspired Liquid Glass optical parameter set exposed with exact bounded ranges
 * and JSON serialization inside [ClockDesignConfig].
 */
data class LiquidGlassConfig(
    val refractionStrength: Float = DEFAULT_REFRACTION_STRENGTH,
    val ior: Float = DEFAULT_IOR,
    val bevelWidth: Float = DEFAULT_BEVEL_WIDTH,
    val chromaticAberration: Float = DEFAULT_CHROMATIC_ABERRATION,
    val frostBlur: Float = DEFAULT_FROST_BLUR,
    val glassSaturation: Float = DEFAULT_GLASS_SATURATION,
    val tintArgb: Long = DEFAULT_TINT_ARGB,
    val tintOpacity: Float = DEFAULT_TINT_OPACITY,
    val specularIntensity: Float = DEFAULT_SPECULAR_INTENSITY,
    val lightAngle: Float = DEFAULT_LIGHT_ANGLE,
    val innerThicknessShadow: Float = DEFAULT_INNER_THICKNESS_SHADOW,
    val backdropDropShadow: Float = DEFAULT_BACKDROP_DROP_SHADOW,
    val adaptiveContrast: Boolean = true
) {
    fun sanitized(): LiquidGlassConfig = copy(
        refractionStrength = refractionStrength.coerceIn(0f, 40f),
        ior = ior.coerceIn(1.00f, 1.80f),
        bevelWidth = bevelWidth.coerceIn(2f, 24f),
        chromaticAberration = chromaticAberration.coerceIn(0f, 1.0f),
        frostBlur = frostBlur.coerceIn(0f, 24f),
        glassSaturation = glassSaturation.coerceIn(1.0f, 2.0f),
        tintOpacity = tintOpacity.coerceIn(0f, 0.50f),
        specularIntensity = specularIntensity.coerceIn(0f, 1.50f),
        lightAngle = ((lightAngle % 360f) + 360f) % 360f,
        innerThicknessShadow = innerThicknessShadow.coerceIn(0f, 0.60f),
        backdropDropShadow = backdropDropShadow.coerceIn(0f, 1.0f)
    )

    fun activePresetOrNull(): LiquidGlassPreset? =
        LiquidGlassPreset.entries.firstOrNull { (it.tintArgb and 0x00FFFFFFL) == (tintArgb and 0x00FFFFFFL) }

    fun toJsonObject(): JSONObject = JSONObject().apply {
        val s = sanitized()
        put("refractionStrength", s.refractionStrength.toDouble())
        put("ior", s.ior.toDouble())
        put("bevelWidth", s.bevelWidth.toDouble())
        put("chromaticAberration", s.chromaticAberration.toDouble())
        put("frostBlur", s.frostBlur.toDouble())
        put("glassSaturation", s.glassSaturation.toDouble())
        put("tintArgb", s.tintArgb)
        put("tintOpacity", s.tintOpacity.toDouble())
        put("specularIntensity", s.specularIntensity.toDouble())
        put("lightAngle", s.lightAngle.toDouble())
        put("innerThicknessShadow", s.innerThicknessShadow.toDouble())
        put("backdropDropShadow", s.backdropDropShadow.toDouble())
        put("adaptiveContrast", s.adaptiveContrast)
    }

    companion object {
        const val DEFAULT_REFRACTION_STRENGTH = 18.0f
        const val DEFAULT_IOR = 1.48f
        const val DEFAULT_BEVEL_WIDTH = 10.0f
        const val DEFAULT_CHROMATIC_ABERRATION = 0.35f
        const val DEFAULT_FROST_BLUR = 6.0f
        const val DEFAULT_GLASS_SATURATION = 1.45f
        const val DEFAULT_TINT_ARGB = 0xFFFFFFFFL
        const val DEFAULT_TINT_OPACITY = 0.10f
        const val DEFAULT_SPECULAR_INTENSITY = 0.85f
        const val DEFAULT_LIGHT_ANGLE = 135.0f
        const val DEFAULT_INNER_THICKNESS_SHADOW = 0.22f
        const val DEFAULT_BACKDROP_DROP_SHADOW = 0.35f

        fun fromJsonObject(obj: JSONObject?): LiquidGlassConfig {
            if (obj == null) return LiquidGlassConfig()
            return LiquidGlassConfig(
                refractionStrength = obj.optDouble("refractionStrength", DEFAULT_REFRACTION_STRENGTH.toDouble()).toFloat(),
                ior = obj.optDouble("ior", DEFAULT_IOR.toDouble()).toFloat(),
                bevelWidth = obj.optDouble("bevelWidth", DEFAULT_BEVEL_WIDTH.toDouble()).toFloat(),
                chromaticAberration = obj.optDouble("chromaticAberration", DEFAULT_CHROMATIC_ABERRATION.toDouble()).toFloat(),
                frostBlur = obj.optDouble("frostBlur", DEFAULT_FROST_BLUR.toDouble()).toFloat(),
                glassSaturation = obj.optDouble("glassSaturation", DEFAULT_GLASS_SATURATION.toDouble()).toFloat(),
                tintArgb = obj.optLong("tintArgb", DEFAULT_TINT_ARGB),
                tintOpacity = obj.optDouble("tintOpacity", DEFAULT_TINT_OPACITY.toDouble()).toFloat(),
                specularIntensity = obj.optDouble("specularIntensity", DEFAULT_SPECULAR_INTENSITY.toDouble()).toFloat(),
                lightAngle = obj.optDouble("lightAngle", DEFAULT_LIGHT_ANGLE.toDouble()).toFloat(),
                innerThicknessShadow = obj.optDouble("innerThicknessShadow", DEFAULT_INNER_THICKNESS_SHADOW.toDouble()).toFloat(),
                backdropDropShadow = obj.optDouble("backdropDropShadow", DEFAULT_BACKDROP_DROP_SHADOW.toDouble()).toFloat(),
                adaptiveContrast = obj.optBoolean("adaptiveContrast", true)
            ).sanitized()
        }
    }
}

internal data class ResolvedOpticalParams(
    val avgLuminance: Float,
    val refractionStrength: Float,
    val ior: Float,
    val bevelWidth: Float,
    val chromaticAberration: Float,
    val frostBlur: Float,
    val glassSaturation: Float,
    val tintR: Float,
    val tintG: Float,
    val tintB: Float,
    val tintOpacity: Float,
    val specularIntensity: Float,
    val lightAngleRad: Float,
    val innerThicknessShadow: Float,
    val backdropDropShadow: Float
)

internal data class BackdropOpticalPatch(
    val unblurredPatch: Bitmap,
    val frostedPatch: Bitmap,
    val avgLuminance: Float
)

/**
 * Multi-pass optical Liquid Glass rendering engine with:
 * - Pass 1: Live cropped/zoomed/panned wallpaper pixels underneath the clock box + Gaussian [frostBlur]
 * - Pass 2: Glyph alpha mask -> multi-radius rounded bevel height map + 16px shadow halo map
 * - Composite: AGSL [RuntimeShader] (API 33+) or exact per-pixel optical software fallback (API 24-32 & tests)
 */
object LiquidGlassClockEngine {

    internal fun resolveAdaptiveOptics(
        config: LiquidGlassConfig,
        avgLuminance: Float
    ): ResolvedOpticalParams {
        val safe = config.sanitized()
        val lum = avgLuminance.coerceIn(0f, 1f)

        var specular = safe.specularIntensity
        var innerShadow = safe.innerThicknessShadow
        var dropShadow = safe.backdropDropShadow

        if (safe.adaptiveContrast) {
            if (lum > 0.70f) {
                val brightFactor = ((lum - 0.70f) / 0.30f).coerceIn(0f, 1f)
                innerShadow = (innerShadow + brightFactor * 0.24f).coerceIn(0f, 0.60f)
                dropShadow = (dropShadow + brightFactor * 0.42f).coerceIn(0f, 1.0f)
            } else if (lum < 0.25f) {
                val darkFactor = ((0.25f - lum) / 0.25f).coerceIn(0f, 1f)
                specular = (specular + darkFactor * 0.42f).coerceIn(0f, 1.50f)
            }
        }

        val tintInt = safe.tintArgb.toInt()
        val tr = ((tintInt ushr 16) and 0xFF) / 255f
        val tg = ((tintInt ushr 8) and 0xFF) / 255f
        val tb = (tintInt and 0xFF) / 255f

        return ResolvedOpticalParams(
            avgLuminance = lum,
            refractionStrength = safe.refractionStrength,
            ior = safe.ior,
            bevelWidth = safe.bevelWidth,
            chromaticAberration = safe.chromaticAberration,
            frostBlur = safe.frostBlur,
            glassSaturation = safe.glassSaturation,
            tintR = tr,
            tintG = tg,
            tintB = tb,
            tintOpacity = safe.tintOpacity,
            specularIntensity = specular,
            lightAngleRad = (safe.lightAngle * PI / 180.0).toFloat(),
            innerThicknessShadow = innerShadow,
            backdropDropShadow = dropShadow
        )
    }

    /**
     * Extracts the exact cropped/positioned wallpaper pixel patch directly underneath the clock's
     * live bounding box in screen space, accounting for `ContentScale.Crop`, zoom, pan, look filter, and dim.
     */
    internal fun extractCroppedBackgroundPatch(
        backdropBitmap: Bitmap?,
        viewportSize: IntSize,
        clockBoxRect: ClockBoxRect?,
        zoomScale: Float,
        panOffset: Offset,
        dimFraction: Float,
        lookFilter: CanvasLookFilter,
        frostBlurPx: Float,
        fallbackPrimary: Color,
        fallbackSecondary: Color,
        patchWidth: Int,
        patchHeight: Int
    ): BackdropOpticalPatch {
        val w = patchWidth.coerceIn(32, 640)
        val h = patchHeight.coerceIn(24, 640)
        val out = createBitmap(w, h)
        val canvas = AndroidCanvas(out)

        if (backdropBitmap != null && !backdropBitmap.isRecycled &&
            backdropBitmap.width > 0 && backdropBitmap.height > 0
        ) {
            val vw = viewportSize.width.coerceAtLeast(1).toFloat()
            val vh = viewportSize.height.coerceAtLeast(1).toFloat()
            val srcW = backdropBitmap.width.toFloat()
            val srcH = backdropBitmap.height.toFloat()

            val baseCropScale = max(vw / srcW, vh / srcH)
            val totalScale = (baseCropScale * zoomScale.coerceAtLeast(1f)).coerceAtLeast(0.01f)

            val boxLeft = clockBoxRect?.leftPx ?: (vw * 0.18f)
            val boxTop = clockBoxRect?.topPx ?: (vh * 0.14f)
            val boxW = (clockBoxRect?.widthPx ?: (vw * 0.64f)).coerceAtLeast(8f)
            val boxH = (clockBoxRect?.heightPx ?: (vh * 0.22f)).coerceAtLeast(8f)

            // Inverse map screen rect -> source bitmap coordinates matching GraphicsLayer + ContentScale.Crop
            val srcLeft = srcW * 0.5f + ((boxLeft - vw * 0.5f) - panOffset.x) / totalScale
            val srcTop = srcH * 0.5f + ((boxTop - vh * 0.5f) - panOffset.y) / totalScale
            val srcRight = srcW * 0.5f + (((boxLeft + boxW) - vw * 0.5f) - panOffset.x) / totalScale
            val srcBottom = srcH * 0.5f + (((boxTop + boxH) - vh * 0.5f) - panOffset.y) / totalScale

            val matrix = Matrix().apply {
                val mappedW = (srcRight - srcLeft).coerceAtLeast(1f)
                val mappedH = (srcBottom - srcTop).coerceAtLeast(1f)
                postTranslate(-srcLeft, -srcTop)
                postScale(w.toFloat() / mappedW, h.toFloat() / mappedH)
            }
            val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.FILTER_BITMAP_FLAG)
            lookFilter.toColorMatrixArray()?.let { cm ->
                paint.colorFilter = ColorMatrixColorFilter(AndroidColorMatrix(cm))
            }
            canvas.drawColor(AndroidColor.rgb(18, 22, 32))
            canvas.drawBitmap(backdropBitmap, matrix, paint)
            if (dimFraction > 0.01f) {
                canvas.drawColor(AndroidColor.argb((dimFraction.coerceIn(0f, 0.55f) * 255f).roundToInt(), 0, 0, 0))
            }
        } else {
            val c1 = androidColorFromCompose(fallbackPrimary)
            val c2 = androidColorFromCompose(fallbackSecondary)
            val grad = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                shader = android.graphics.LinearGradient(
                    0f, 0f, w.toFloat(), h.toFloat(),
                    intArrayOf(blendRgb(c1, AndroidColor.DKGRAY, 0.35f), blendRgb(c2, AndroidColor.BLACK, 0.25f)),
                    null,
                    android.graphics.Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), grad)
        }

        val lum = computeAverageLuminance(out)
        val frostRadius = ((frostBlurPx.coerceIn(0f, 24f) * (w.toFloat() / max(clockBoxRect?.widthPx ?: w.toFloat(), 1f))).coerceIn(0f, 18f)).roundToInt()
        val frosted = if (frostRadius >= 1) {
            boxBlurBitmap(out, frostRadius)
        } else {
            out
        }
        return BackdropOpticalPatch(
            unblurredPatch = out,
            frostedPatch = frosted,
            avgLuminance = lum
        )
    }

    /**
     * Generates a packed glyph height + normal + shadow map from an offscreen crisp glyph alpha bitmap:
     * - `A`: crisp anti-aliased glyph mask (0..255)
     * - `R`: smooth multi-radius interior bevel height (0 at edge -> 255 in stroke core)
     * - `G`: normalized edge gradient band
     * - `B`: wide 16px diffused glyph drop-shadow alpha mask
     */
    internal fun buildMultiRadiusBevelHeightMap(
        glyphMaskBitmap: Bitmap,
        bevelWidthPx: Float
    ): Bitmap {
        val w = glyphMaskBitmap.width.coerceAtLeast(2)
        val h = glyphMaskBitmap.height.coerceAtLeast(2)
        val n = w * h
        val rawPixels = IntArray(n)
        glyphMaskBitmap.getPixels(rawPixels, 0, w, 0, 0, w, h)

        val alphaF = FloatArray(n) { i -> ((rawPixels[i] ushr 24) and 0xFF) / 255f }

        val bw = bevelWidthPx.coerceIn(2f, 24f)
        val rSmall = max(1, (bw * 0.35f).roundToInt())
        val rMid = max(1, (bw * 0.70f).roundToInt())
        val rLarge = max(2, bw.roundToInt())

        val b1 = separableBoxBlurFloat(alphaF, w, h, rSmall)
        val b2 = separableBoxBlurFloat(alphaF, w, h, rMid)
        val b3 = separableBoxBlurFloat(alphaF, w, h, rLarge)
        val shadowField = separableBoxBlurFloat(alphaF, w, h, 8)

        val outPixels = IntArray(n)
        for (i in 0 until n) {
            val a = alphaF[i].coerceIn(0f, 1f)
            // Combine blurred alpha field clipped cleanly by glyph mask so edge transitions taper to 0 at glyph contour
            val combinedBlur = 0.45f * b1[i] + 0.35f * b2[i] + 0.20f * b3[i]
            val height = if (a > 0.01f) {
                ((combinedBlur - 0.48f) / 0.52f).coerceIn(0f, 1f) * min(1f, a * 1.15f)
            } else {
                0f
            }
            val edge = (1f - height) * a
            val aByte = (a * 255f).roundToInt().coerceIn(0, 255)
            val rByte = (height * 255f).roundToInt().coerceIn(0, 255)
            val gByte = (edge * 255f).roundToInt().coerceIn(0, 255)
            val bByte = (shadowField[i].coerceIn(0f, 1f) * 255f).roundToInt().coerceIn(0, 255)
            outPixels[i] = (aByte shl 24) or (rByte shl 16) or (gByte shl 8) or bByte
        }

        val out = createBitmap(w, h)
        out.setPixels(outPixels, 0, w, 0, 0, w, h)
        return out
    }

    /**
     * Exact CPU optical fallback for API < 33 and deterministic pixel-verified unit tests.
     * Simulates IOR lens refraction, prismatic RGB chromatic aberration, frost diffusion,
     * jewel saturation, translucent tint, directional Fresnel specular rim, inner thickness
     * darkening, and glyph-shaped drop shadow.
     */
    internal fun renderOpticalGlassSoftwareBitmap(
        glyphMap: Bitmap,
        backgroundPatch: Bitmap,
        frostedPatch: Bitmap,
        params: ResolvedOpticalParams,
        scaleXAspect: Float = 1f,
        scaleYAspect: Float = 1f,
        opacity: Float = 1f
    ): Bitmap {
        val w = glyphMap.width.coerceAtLeast(2)
        val h = glyphMap.height.coerceAtLeast(2)
        val bgScaled = if (backgroundPatch.width == w && backgroundPatch.height == h) backgroundPatch
        else Bitmap.createScaledBitmap(backgroundPatch, w, h, true)
        val frostScaled = if (frostedPatch.width == w && frostedPatch.height == h) frostedPatch
        else Bitmap.createScaledBitmap(frostedPatch, w, h, true)

        val n = w * h
        val mapPx = IntArray(n)
        val bgPx = IntArray(n)
        val frPx = IntArray(n)
        glyphMap.getPixels(mapPx, 0, w, 0, 0, w, h)
        bgScaled.getPixels(bgPx, 0, w, 0, 0, w, h)
        frostScaled.getPixels(frPx, 0, w, 0, 0, w, h)
        if (bgScaled !== backgroundPatch) bgScaled.recycle()
        if (frostScaled !== frostedPatch) frostScaled.recycle()

        val outPx = IntArray(n)
        val lightX = cos(params.lightAngleRad)
        val lightY = sin(params.lightAngleRad)
        val iorCurve = ((params.ior - 1.0f) / 0.48f).coerceIn(0.15f, 2.2f)
        val frostMix = (params.frostBlur / 24.0f * 0.85f).coerceIn(0f, 0.92f)
        val safeScaleX = scaleXAspect.coerceAtLeast(0.35f)
        val safeScaleY = scaleYAspect.coerceAtLeast(0.35f)
        val dropOffsetY = (6f / safeScaleY).roundToInt().coerceIn(1, max(1, h - 1))
        val safeOp = opacity.coerceIn(0f, 1f)

        fun sampleBilinearRgb(buf: IntArray, fx: Float, fy: Float, channelShift: Int): Float {
            val cx = fx.coerceIn(0f, (w - 1).toFloat())
            val cy = fy.coerceIn(0f, (h - 1).toFloat())
            val x0 = cx.toInt()
            val y0 = cy.toInt()
            val x1 = min(x0 + 1, w - 1)
            val y1 = min(y0 + 1, h - 1)
            val tx = cx - x0
            val ty = cy - y0
            val c00 = ((buf[y0 * w + x0] ushr channelShift) and 0xFF) / 255f
            val c10 = ((buf[y0 * w + x1] ushr channelShift) and 0xFF) / 255f
            val c01 = ((buf[y1 * w + x0] ushr channelShift) and 0xFF) / 255f
            val c11 = ((buf[y1 * w + x1] ushr channelShift) and 0xFF) / 255f
            val top = c00 + (c10 - c00) * tx
            val bot = c01 + (c11 - c01) * tx
            return top + (bot - top) * ty
        }

        for (y in 0 until h) {
            val row = y * w
            val shadowRow = max(0, y - dropOffsetY) * w
            val ym = max(0, y - 1) * w
            val yp = min(h - 1, y + 1) * w

            for (x in 0 until w) {
                val idx = row + x
                val cMap = mapPx[idx]
                val mask = ((cMap ushr 24) and 0xFF) / 255f
                val shadowMask = ((mapPx[shadowRow + x] and 0xFF)) / 255f
                val shadowAlpha = (shadowMask * params.backdropDropShadow * 0.72f * (1f - mask * 0.85f)).coerceIn(0f, 0.85f)

                if (mask <= 0.004f) {
                    val sA = (shadowAlpha * safeOp * 255f).roundToInt().coerceIn(0, 255)
                    outPx[idx] = if (sA == 0) 0 else (sA shl 24)
                    continue
                }

                val height = ((cMap ushr 16) and 0xFF) / 255f
                val xm = max(0, x - 1)
                val xp = min(w - 1, x + 1)
                val hL = ((mapPx[row + xm] ushr 16) and 0xFF) / 255f
                val hR = ((mapPx[row + xp] ushr 16) and 0xFF) / 255f
                val hT = ((mapPx[ym + x] ushr 16) and 0xFF) / 255f
                val hB = ((mapPx[yp + x] ushr 16) and 0xFF) / 255f

                val gx = (hL - hR) * 3.8f * iorCurve
                val gy = (hT - hB) * 3.8f * iorCurve
                val gz = 0.42f + height * 0.65f
                val invLen = 1f / sqrt(gx * gx + gy * gy + gz * gz)
                val nx = gx * invLen
                val ny = gy * invLen
                val nz = gz * invLen

                val rawEdge = (1f - height).coerceIn(0f, 1f)
                val edgeMask = rawEdge * rawEdge * (3f - 2f * rawEdge)
                val refrFactor = params.refractionStrength * (edgeMask * 1.15f + (1f - edgeMask) * 0.18f * (params.ior - 1f))
                val rx = (x.toFloat() - (nx * refrFactor) / safeScaleX).coerceIn(0f, (w - 1).toFloat())
                val ry = (y.toFloat() - (ny * refrFactor) / safeScaleY).coerceIn(0f, (h - 1).toFloat())

                val n2dLen = sqrt(nx * nx + ny * ny)
                val dirX = if (n2dLen > 0.001f) nx / n2dLen else 1f
                val dirY = if (n2dLen > 0.001f) ny / n2dLen else 0.4f
                val caScale = params.chromaticAberration * 0.004f * (0.25f + edgeMask * 1.15f)
                val caDx = w * caScale * dirX
                val caDy = h * caScale * dirY

                val sharpR = sampleBilinearRgb(bgPx, rx + caDx, ry + caDy, 16)
                val sharpG = sampleBilinearRgb(bgPx, rx, ry, 8)
                val sharpB = sampleBilinearRgb(bgPx, rx - caDx, ry - caDy, 0)

                val frostR = sampleBilinearRgb(frPx, rx + caDx * 0.5f, ry + caDy * 0.5f, 16)
                val frostG = sampleBilinearRgb(frPx, rx, ry, 8)
                val frostB = sampleBilinearRgb(frPx, rx - caDx * 0.5f, ry - caDy * 0.5f, 0)

                var r = sharpR + (frostR - sharpR) * frostMix
                var g = sharpG + (frostG - sharpG) * frostMix
                var b = sharpB + (frostB - sharpB) * frostMix

                // Jewel saturation boost
                val luma = 0.2126f * r + 0.7152f * g + 0.0722f * b
                r = (luma + (r - luma) * params.glassSaturation).coerceIn(0f, 1f)
                g = (luma + (g - luma) * params.glassSaturation).coerceIn(0f, 1f)
                b = (luma + (b - luma) * params.glassSaturation).coerceIn(0f, 1f)

                // Translucent tint blend
                val tAlpha = params.tintOpacity
                r = r * (1f - tAlpha) + (r * 0.45f + params.tintR * 0.85f).coerceIn(0f, 1f) * tAlpha
                g = g * (1f - tAlpha) + (g * 0.45f + params.tintG * 0.85f).coerceIn(0f, 1f) * tAlpha
                b = b * (1f - tAlpha) + (b * 0.45f + params.tintB * 0.85f).coerceIn(0f, 1f) * tAlpha

                // Inner thickness darkening on shadow-facing bevel edge
                val shadowAlign = max(-(dirX * lightX + dirY * lightY), 0f) * min(1f, n2dLen * 2.2f)
                val innerDark = (shadowAlign * params.innerThicknessShadow * (0.35f + 0.95f * edgeMask)).coerceIn(0f, 0.72f)
                r *= (1f - innerDark)
                g *= (1f - innerDark)
                b *= (1f - innerDark)

                // Directional Fresnel Specular + 0.35x secondary bounce rim highlight
                val fresnel = (1f - nz).coerceIn(0f, 1f).toDouble().pow(1.85).toFloat()
                val rawKeyDot = (dirX * lightX + dirY * lightY)
                val keySpec = max(rawKeyDot, 0f).toDouble().pow(2.4).toFloat()
                val bounceSpec = max(-rawKeyDot, 0f).toDouble().pow(2.8).toFloat() * 0.35f
                val rimMask = ((edgeMask + 0.06f) * min(1f, n2dLen * 2.4f + 0.15f)).coerceIn(0f, 1f)
                val specAmt = ((keySpec * (0.35f + 0.85f * fresnel) + bounceSpec * (0.25f + 0.75f * fresnel)) * params.specularIntensity * rimMask).coerceIn(0f, 1.4f)

                r = (r + (1f * 0.65f + params.tintR * 0.35f) * specAmt).coerceIn(0f, 1f)
                g = (g + (1f * 0.65f + params.tintG * 0.35f) * specAmt).coerceIn(0f, 1f)
                b = (b + (1f * 0.65f + params.tintB * 0.35f) * specAmt).coerceIn(0f, 1f)

                // Composite crisp glass over drop shadow
                val compAlpha = max(mask, shadowAlpha).coerceIn(0f, 1f)
                val compScale = if (compAlpha > 1e-4f) mask / compAlpha else 1f
                val outR = (r * compScale).coerceIn(0f, 1f)
                val outG = (g * compScale).coerceIn(0f, 1f)
                val outB = (b * compScale).coerceIn(0f, 1f)

                val a8 = (compAlpha * safeOp * 255f).roundToInt().coerceIn(0, 255)
                val r8 = (outR * 255f).roundToInt().coerceIn(0, 255)
                val g8 = (outG * 255f).roundToInt().coerceIn(0, 255)
                val b8 = (outB * 255f).roundToInt().coerceIn(0, 255)
                outPx[idx] = (a8 shl 24) or (r8 shl 16) or (g8 shl 8) or b8
            }
        }

        val result = createBitmap(w, h)
        result.setPixels(outPx, 0, w, 0, 0, w, h)
        return result
    }

    internal fun computeAverageLuminance(bitmap: Bitmap): Float {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) return 0.5f
        val px = IntArray(w * h)
        bitmap.getPixels(px, 0, w, 0, 0, w, h)
        var sum = 0.0
        var count = 0
        val step = max(1, (w * h) / 600)
        var i = 0
        while (i < px.size) {
            val c = px[i]
            val r = ((c ushr 16) and 0xFF) / 255.0
            val g = ((c ushr 8) and 0xFF) / 255.0
            val b = (c and 0xFF) / 255.0
            sum += 0.2126 * r + 0.7152 * g + 0.0722 * b
            count++
            i += step
        }
        return (sum / max(1, count)).toFloat().coerceIn(0f, 1f)
    }

    private fun boxBlurBitmap(src: Bitmap, radius: Int): Bitmap {
        val w = src.width
        val h = src.height
        val r = radius.coerceIn(1, 24)
        val n = w * h
        val px = IntArray(n)
        src.getPixels(px, 0, w, 0, 0, w, h)
        val tmp = IntArray(n)
        val out = IntArray(n)

        // Two-pass separable sliding box blur approximates smooth Gaussian diffusion quickly
        repeat(2) { pass ->
            val srcArr = if (pass == 0) px else out
            for (y in 0 until h) {
                val row = y * w
                var sr = 0; var sg = 0; var sb = 0
                for (dx in -r..r) {
                    val cx = dx.coerceIn(0, w - 1)
                    val c = srcArr[row + cx]
                    sr += (c ushr 16) and 0xFF
                    sg += (c ushr 8) and 0xFF
                    sb += c and 0xFF
                }
                val denom = 2 * r + 1
                for (x in 0 until w) {
                    tmp[row + x] = (0xFF shl 24) or
                        ((sr / denom) shl 16) or
                        ((sg / denom) shl 8) or
                        (sb / denom)
                    val remIdx = (x - r).coerceIn(0, w - 1)
                    val addIdx = (x + r + 1).coerceIn(0, w - 1)
                    val cr = srcArr[row + remIdx]
                    val ca = srcArr[row + addIdx]
                    sr += ((ca ushr 16) and 0xFF) - ((cr ushr 16) and 0xFF)
                    sg += ((ca ushr 8) and 0xFF) - ((cr ushr 8) and 0xFF)
                    sb += (ca and 0xFF) - (cr and 0xFF)
                }
            }
            for (x in 0 until w) {
                var sr = 0; var sg = 0; var sb = 0
                for (dy in -r..r) {
                    val cy = dy.coerceIn(0, h - 1)
                    val c = tmp[cy * w + x]
                    sr += (c ushr 16) and 0xFF
                    sg += (c ushr 8) and 0xFF
                    sb += c and 0xFF
                }
                val denom = 2 * r + 1
                for (y in 0 until h) {
                    out[y * w + x] = (0xFF shl 24) or
                        ((sr / denom) shl 16) or
                        ((sg / denom) shl 8) or
                        (sb / denom)
                    val remIdx = (y - r).coerceIn(0, h - 1) * w + x
                    val addIdx = (y + r + 1).coerceIn(0, h - 1) * w + x
                    val cr = tmp[remIdx]
                    val ca = tmp[addIdx]
                    sr += ((ca ushr 16) and 0xFF) - ((cr ushr 16) and 0xFF)
                    sg += ((ca ushr 8) and 0xFF) - ((cr ushr 8) and 0xFF)
                    sb += (ca and 0xFF) - (cr and 0xFF)
                }
            }
        }
        return createBitmap(w, h).apply { setPixels(out, 0, w, 0, 0, w, h) }
    }

    private fun separableBoxBlurFloat(src: FloatArray, w: Int, h: Int, radius: Int): FloatArray {
        val r = radius.coerceIn(1, 32)
        val n = w * h
        val tmp = FloatArray(n)
        val out = FloatArray(n)
        val denom = (2 * r + 1).toFloat()

        for (y in 0 until h) {
            val row = y * w
            var acc = 0f
            for (dx in -r..r) acc += src[row + dx.coerceIn(0, w - 1)]
            for (x in 0 until w) {
                tmp[row + x] = acc / denom
                val rem = src[row + (x - r).coerceIn(0, w - 1)]
                val add = src[row + (x + r + 1).coerceIn(0, w - 1)]
                acc += add - rem
            }
        }
        for (x in 0 until w) {
            var acc = 0f
            for (dy in -r..r) acc += tmp[dy.coerceIn(0, h - 1) * w + x]
            for (y in 0 until h) {
                out[y * w + x] = acc / denom
                val rem = tmp[(y - r).coerceIn(0, h - 1) * w + x]
                val add = tmp[(y + r + 1).coerceIn(0, h - 1) * w + x]
                acc += add - rem
            }
        }
        return out
    }

    private fun androidColorFromCompose(color: Color): Int = AndroidColor.argb(
        (color.alpha * 255f).roundToInt().coerceIn(0, 255),
        (color.red * 255f).roundToInt().coerceIn(0, 255),
        (color.green * 255f).roundToInt().coerceIn(0, 255),
        (color.blue * 255f).roundToInt().coerceIn(0, 255)
    )

    private fun blendRgb(a: Int, b: Int, t: Float): Int {
        val f = t.coerceIn(0f, 1f)
        val r = (((a ushr 16) and 0xFF) * (1f - f) + ((b ushr 16) and 0xFF) * f).roundToInt()
        val g = (((a ushr 8) and 0xFF) * (1f - f) + ((b ushr 8) and 0xFF) * f).roundToInt()
        val bl = ((a and 0xFF) * (1f - f) + (b and 0xFF) * f).roundToInt()
        return AndroidColor.rgb(r, g, bl)
    }

    internal const val LIQUID_GLASS_AGSL = """
        uniform shader uBackground;
        uniform shader uFrosted;
        uniform shader uGlyphMap;
        uniform float2 uSize;
        uniform float2 uBgSize;
        uniform float2 uScaleXY;
        uniform float uRefraction;
        uniform float uIor;
        uniform float uChromaticAberration;
        uniform float uFrostAmount;
        uniform float uSaturation;
        uniform float3 uTintRgb;
        uniform float uTintOpacity;
        uniform float uSpecular;
        uniform float2 uLightDir;
        uniform float uInnerShadow;
        uniform float uDropShadow;
        uniform float uOpacity;

        half4 main(float2 fragCoord) {
            float4 centerMap = uGlyphMap.eval(fragCoord);
            float mask = centerMap.a;
            float height = centerMap.r;

            float dropOffsetY = 6.0 / max(uScaleXY.y, 0.35);
            float shadowMask = uGlyphMap.eval(clamp(fragCoord - float2(0.0, dropOffsetY), float2(0.0), uSize - float2(1.0))).b;
            float shadowAlpha = clamp(shadowMask * uDropShadow * 0.72 * (1.0 - mask * 0.85), 0.0, 0.85);

            if (mask <= 0.003) {
                return half4(0.0, 0.0, 0.0, half(shadowAlpha * uOpacity));
            }

            float stepPx = 1.5;
            float hL = uGlyphMap.eval(clamp(fragCoord - float2(stepPx, 0.0), float2(0.0), uSize - float2(1.0))).r;
            float hR = uGlyphMap.eval(clamp(fragCoord + float2(stepPx, 0.0), float2(0.0), uSize - float2(1.0))).r;
            float hT = uGlyphMap.eval(clamp(fragCoord - float2(0.0, stepPx), float2(0.0), uSize - float2(1.0))).r;
            float hB = uGlyphMap.eval(clamp(fragCoord + float2(0.0, stepPx), float2(0.0), uSize - float2(1.0))).r;

            float iorCurve = clamp((uIor - 1.0) / 0.48, 0.15, 2.2);
            float3 n = normalize(float3((hL - hR) * 3.8 * iorCurve, (hT - hB) * 3.8 * iorCurve, 0.42 + height * 0.65));

            float edgeMask = smoothstep(0.0, 0.90, 1.0 - height) * smoothstep(0.0, 0.15, mask);
            float refrPx = uRefraction * (edgeMask * 1.15 + (1.0 - edgeMask) * 0.18 * (uIor - 1.0));
            float2 bgScale = uBgSize / max(uSize, float2(1.0));
            float2 safeScale = max(uScaleXY, float2(0.35));
            float2 bgCoord = clamp((fragCoord - (n.xy * refrPx) / safeScale) * bgScale, float2(1.0), uBgSize - float2(1.0));

            float2 dir2 = length(n.xy) > 0.001 ? normalize(n.xy) : float2(1.0, 0.4);
            float2 caShift = uBgSize * (uChromaticAberration * 0.004) * (0.25 + edgeMask * 1.15) * dir2;

            float rSharp = uBackground.eval(clamp(bgCoord + caShift, float2(1.0), uBgSize - float2(1.0))).r;
            float gSharp = uBackground.eval(bgCoord).g;
            float bSharp = uBackground.eval(clamp(bgCoord - caShift, float2(1.0), uBgSize - float2(1.0))).b;

            float rFrost = uFrosted.eval(clamp(bgCoord + caShift * 0.5, float2(1.0), uBgSize - float2(1.0))).r;
            float gFrost = uFrosted.eval(bgCoord).g;
            float bFrost = uFrosted.eval(clamp(bgCoord - caShift * 0.5, float2(1.0), uBgSize - float2(1.0))).b;

            float3 glassRgb = mix(float3(rSharp, gSharp, bSharp), float3(rFrost, gFrost, bFrost), uFrostAmount);
            float luma = dot(glassRgb, float3(0.2126, 0.7152, 0.0722));
            glassRgb = clamp(mix(float3(luma), glassRgb, uSaturation), 0.0, 1.0);

            float3 tintedTarget = clamp(glassRgb * 0.45 + uTintRgb * 0.85, 0.0, 1.0);
            glassRgb = mix(glassRgb, tintedTarget, uTintOpacity);

            float n2Mag = clamp(length(n.xy) * 2.2, 0.0, 1.0);
            float shadowAlign = max(dot(dir2, -uLightDir), 0.0) * n2Mag;
            float innerDark = clamp(shadowAlign * uInnerShadow * (0.35 + 0.95 * edgeMask), 0.0, 0.72);
            glassRgb *= (1.0 - innerDark);

            float fresnel = pow(clamp(1.0 - n.z, 0.0, 1.0), 1.85);
            float keyDot = dot(dir2, uLightDir);
            float keySpec = pow(max(keyDot, 0.0), 2.4);
            float bounceSpec = pow(max(-keyDot, 0.0), 2.8) * 0.35;
            float rimMask = clamp((edgeMask + 0.06) * clamp(length(n.xy) * 2.4 + 0.15, 0.0, 1.0), 0.0, 1.0);
            float specAmt = clamp((keySpec * (0.35 + 0.85 * fresnel) + bounceSpec * (0.25 + 0.75 * fresnel)) * uSpecular * rimMask, 0.0, 1.4);

            float3 specColor = mix(float3(1.0), uTintRgb, 0.35);
            glassRgb = clamp(glassRgb + specColor * specAmt, 0.0, 1.0);

            float finalAlpha = max(mask, shadowAlpha) * uOpacity;
            float3 premulRgb = glassRgb * (mask * uOpacity);
            return half4(half3(premulRgb), half(finalAlpha));
        }
    """
}

/**
 * Dual-pass Compose optical clock surface for [HyperOsClockFinish.BLUR_GLASS]:
 * Captures the live typography + glyphs offscreen into [GraphicsLayer] -> builds multi-radius
 * bevel height map -> binds real cropped wallpaper background + frost patch -> executes either
 * hardware AGSL [RuntimeShader] (API 33+) or optical software shader fallback at 60fps.
 */
@Composable
internal fun LiquidGlassClockLayer(
    liquidGlassConfig: LiquidGlassConfig,
    backdropBitmap: Bitmap?,
    viewportSize: IntSize,
    clockBoxRect: ClockBoxRect?,
    zoomScale: Float,
    panOffset: Offset,
    dimFraction: Float,
    lookFilter: CanvasLookFilter,
    fallbackPrimary: Color,
    fallbackSecondary: Color,
    scaleXAspect: Float,
    scaleYAspect: Float,
    opacity: Float,
    captureKey: Any?,
    modifier: Modifier = Modifier,
    maskContent: @Composable () -> Unit
) {
    val glyphCaptureLayer: GraphicsLayer = rememberGraphicsLayer()
    var unscaledSize by remember { mutableStateOf(IntSize.Zero) }
    var glyphHeightMap by remember { mutableStateOf<Bitmap?>(null) }

    val safeConfig = remember(liquidGlassConfig) { liquidGlassConfig.sanitized() }
    val density = LocalDensity.current

    // Rebuild multi-radius bevel height map whenever clock glyphs, face, font, weight, or bevelWidth change
    LaunchedEffect(captureKey, unscaledSize, safeConfig.bevelWidth) {
        if (unscaledSize.width <= 0 || unscaledSize.height <= 0) return@LaunchedEffect
        // Wait two frame commits so Compose finishes drawing new face typography into glyphCaptureLayer
        withFrameNanos { }
        withFrameNanos { }
        val captured = runCatching {
            val img = glyphCaptureLayer.toImageBitmap()
            val rawBmp = img.asAndroidBitmap()
            if (rawBmp.width > 0 && rawBmp.height > 0) {
                val swBmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    rawBmp.config == Bitmap.Config.HARDWARE
                ) {
                    rawBmp.copy(Bitmap.Config.ARGB_8888, false)
                } else if (rawBmp.config != Bitmap.Config.ARGB_8888) {
                    rawBmp.copy(Bitmap.Config.ARGB_8888, false)
                } else {
                    rawBmp
                }
                if (swBmp != null && swBmp.width > 0 && swBmp.height > 0) {
                    val maxDim = 540
                    val workBmp = if (swBmp.width > maxDim || swBmp.height > maxDim) {
                        val scaleFactor = minOf(
                            maxDim.toFloat() / swBmp.width.toFloat(),
                            maxDim.toFloat() / swBmp.height.toFloat()
                        )
                        val targetW = max(24, (swBmp.width * scaleFactor).roundToInt())
                        val targetH = max(24, (swBmp.height * scaleFactor).roundToInt())
                        Bitmap.createScaledBitmap(swBmp, targetW, targetH, true)
                    } else {
                        swBmp
                    }
                    val ratioScale = workBmp.width.toFloat() / rawBmp.width.toFloat()
                    val heightMap = LiquidGlassClockEngine.buildMultiRadiusBevelHeightMap(
                        glyphMaskBitmap = workBmp,
                        bevelWidthPx = (safeConfig.bevelWidth * ratioScale).coerceIn(2f, 24f)
                    )
                    if (workBmp !== swBmp) workBmp.recycle()
                    if (swBmp !== rawBmp) swBmp.recycle()
                    heightMap
                } else null
            } else null
        }.getOrNull()
        if (captured != null) {
            glyphHeightMap = captured
        }
    }

    // Re-extract live cropped background pixel patch whenever clock moves, wallpaper pans/zooms, or frostBlur changes
    val bgPatch = remember(
        backdropBitmap,
        viewportSize,
        clockBoxRect?.leftPx?.roundToInt(),
        clockBoxRect?.topPx?.roundToInt(),
        clockBoxRect?.widthPx?.roundToInt(),
        clockBoxRect?.heightPx?.roundToInt(),
        zoomScale,
        panOffset.x.roundToInt(),
        panOffset.y.roundToInt(),
        dimFraction,
        lookFilter,
        safeConfig.frostBlur,
        unscaledSize,
        fallbackPrimary,
        fallbackSecondary
    ) {
        val targetW = (glyphHeightMap?.width ?: unscaledSize.width.coerceIn(64, 420)).coerceIn(48, 520)
        val targetH = (glyphHeightMap?.height ?: unscaledSize.height.coerceIn(32, 320)).coerceIn(32, 520)
        LiquidGlassClockEngine.extractCroppedBackgroundPatch(
            backdropBitmap = backdropBitmap,
            viewportSize = viewportSize,
            clockBoxRect = clockBoxRect,
            zoomScale = zoomScale,
            panOffset = panOffset,
            dimFraction = dimFraction,
            lookFilter = lookFilter,
            frostBlurPx = safeConfig.frostBlur,
            fallbackPrimary = fallbackPrimary,
            fallbackSecondary = fallbackSecondary,
            patchWidth = targetW,
            patchHeight = targetH
        )
    }

    val resolvedOptics = remember(safeConfig, bgPatch.avgLuminance) {
        LiquidGlassClockEngine.resolveAdaptiveOptics(safeConfig, bgPatch.avgLuminance)
    }

    val softwareRendered = remember(glyphHeightMap, bgPatch, resolvedOptics, scaleXAspect, scaleYAspect, opacity) {
        val hMap = glyphHeightMap
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && hMap != null) {
            LiquidGlassClockEngine.renderOpticalGlassSoftwareBitmap(
                glyphMap = hMap,
                backgroundPatch = bgPatch.unblurredPatch,
                frostedPatch = bgPatch.frostedPatch,
                params = resolvedOptics,
                scaleXAspect = scaleXAspect,
                scaleYAspect = scaleYAspect,
                opacity = opacity
            ).asImageBitmap()
        } else {
            null
        }
    }

    Box(
        modifier = modifier.onSizeChanged { measured ->
            if (measured.width > 0 && measured.height > 0) {
                unscaledSize = measured
            }
        }
    ) {
        // Hidden offscreen glyph recorder: records crisp alpha silhouettes without painting flat fallback
        Box(
            modifier = Modifier.drawWithContent {
                glyphCaptureLayer.record {
                    this@drawWithContent.drawContent()
                }
                if (glyphHeightMap == null) {
                    // Translucent placeholder shimmer only while single warm-up pass completes
                    drawLayer(glyphCaptureLayer)
                }
            }
        ) {
            maskContent()
        }

        val hMap = glyphHeightMap
        if (hMap != null && unscaledSize.width > 0 && unscaledSize.height > 0) {
            val canvasWidthDp = with(density) { unscaledSize.width.toDp() }
            val canvasHeightDp = with(density) { unscaledSize.height.toDp() }

            Canvas(modifier = Modifier.size(canvasWidthDp, canvasHeightDp)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    drawAgslLiquidGlass(
                        glyphMap = hMap,
                        bgPatch = bgPatch,
                        params = resolvedOptics,
                        scaleXAspect = scaleXAspect,
                        scaleYAspect = scaleYAspect,
                        opacity = opacity,
                        canvasW = size.width,
                        canvasH = size.height
                    )
                } else if (softwareRendered != null) {
                    drawImage(
                        image = softwareRendered,
                        dstSize = IntSize(size.width.roundToInt().coerceAtLeast(1), size.height.roundToInt().coerceAtLeast(1))
                    )
                } else {
                    drawLayer(glyphCaptureLayer)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAgslLiquidGlass(
    glyphMap: Bitmap,
    bgPatch: BackdropOpticalPatch,
    params: ResolvedOpticalParams,
    scaleXAspect: Float,
    scaleYAspect: Float,
    opacity: Float,
    canvasW: Float,
    canvasH: Float
) {
    val rendered = runCatching {
        val shader = RuntimeShader(LiquidGlassClockEngine.LIQUID_GLASS_AGSL)
        val glyphShader = android.graphics.BitmapShader(
            glyphMap,
            android.graphics.Shader.TileMode.CLAMP,
            android.graphics.Shader.TileMode.CLAMP
        )
        val bgShader = android.graphics.BitmapShader(
            bgPatch.unblurredPatch,
            android.graphics.Shader.TileMode.CLAMP,
            android.graphics.Shader.TileMode.CLAMP
        )
        val frostShader = android.graphics.BitmapShader(
            bgPatch.frostedPatch,
            android.graphics.Shader.TileMode.CLAMP,
            android.graphics.Shader.TileMode.CLAMP
        )

        shader.setInputShader("uBackground", bgShader)
        shader.setInputShader("uFrosted", frostShader)
        shader.setInputShader("uGlyphMap", glyphShader)
        shader.setFloatUniform("uSize", glyphMap.width.toFloat(), glyphMap.height.toFloat())
        shader.setFloatUniform("uBgSize", bgPatch.unblurredPatch.width.toFloat(), bgPatch.unblurredPatch.height.toFloat())
        shader.setFloatUniform("uScaleXY", scaleXAspect.coerceAtLeast(0.35f), scaleYAspect.coerceAtLeast(0.35f))
        shader.setFloatUniform("uRefraction", params.refractionStrength)
        shader.setFloatUniform("uIor", params.ior)
        shader.setFloatUniform("uChromaticAberration", params.chromaticAberration)
        shader.setFloatUniform("uFrostAmount", (params.frostBlur / 24.0f * 0.85f).coerceIn(0f, 0.92f))
        shader.setFloatUniform("uSaturation", params.glassSaturation)
        shader.setFloatUniform("uTintRgb", params.tintR, params.tintG, params.tintB)
        shader.setFloatUniform("uTintOpacity", params.tintOpacity)
        shader.setFloatUniform("uSpecular", params.specularIntensity)
        shader.setFloatUniform("uLightDir", cos(params.lightAngleRad), sin(params.lightAngleRad))
        shader.setFloatUniform("uInnerShadow", params.innerThicknessShadow)
        shader.setFloatUniform("uDropShadow", params.backdropDropShadow)
        shader.setFloatUniform("uOpacity", opacity.coerceIn(0f, 1f))

        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas
            val sx = canvasW / glyphMap.width.toFloat().coerceAtLeast(1f)
            val sy = canvasH / glyphMap.height.toFloat().coerceAtLeast(1f)
            nativeCanvas.save()
            nativeCanvas.scale(sx, sy)
            val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                this.shader = shader
            }
            nativeCanvas.drawRect(0f, 0f, glyphMap.width.toFloat(), glyphMap.height.toFloat(), paint)
            nativeCanvas.restore()
        }
        true
    }.getOrDefault(false)

    if (!rendered) {
        val fallbackBmp = LiquidGlassClockEngine.renderOpticalGlassSoftwareBitmap(
            glyphMap = glyphMap,
            backgroundPatch = bgPatch.unblurredPatch,
            frostedPatch = bgPatch.frostedPatch,
            params = params,
            scaleXAspect = scaleXAspect,
            scaleYAspect = scaleYAspect,
            opacity = opacity
        )
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawBitmap(
                fallbackBmp,
                Rect(0, 0, fallbackBmp.width, fallbackBmp.height),
                RectF(0f, 0f, canvasW, canvasH),
                AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.FILTER_BITMAP_FLAG)
            )
        }
    }
}
