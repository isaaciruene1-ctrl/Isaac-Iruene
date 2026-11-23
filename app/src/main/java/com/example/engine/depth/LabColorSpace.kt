package com.example.engine.depth

import kotlin.math.cbrt
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * High-speed CIE L*a*b* perceptual color space converter and luminance utilities.
 * Uses precomputed 256-entry sRGB linearization LUT and primitive FloatArrays to avoid GC churn.
 */
internal object LabColorSpace {

    private val srgbToLinear = FloatArray(256) { i ->
        val v = i / 255f
        if (v <= 0.04045f) v / 12.92f else ((v + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
    }

    private const val LAB_EPSILON = 216f / 24389f // 0.008856f
    private const val LAB_KAPPA = 24389f / 27f    // 903.2963f

    private fun fLab(t: Float): Float {
        return if (t > LAB_EPSILON) cbrt(t.toDouble()).toFloat() else (LAB_KAPPA * t + 16f) / 116f
    }

    /**
     * Converts packed ARGB pixels into planar CIE L*, a*, b* arrays and 0..255 perceptual luminance.
     */
    fun convertArgbToLabAndLum(
        argbPixels: IntArray,
        outL: FloatArray,
        outA: FloatArray,
        outB: FloatArray,
        outLum: FloatArray
    ) {
        val n = argbPixels.size
        for (i in 0 until n) {
            val c = argbPixels[i]
            val r8 = (c ushr 16) and 0xFF
            val g8 = (c ushr 8) and 0xFF
            val b8 = c and 0xFF

            outLum[i] = 0.299f * r8 + 0.587f * g8 + 0.114f * b8

            val rLin = srgbToLinear[r8]
            val gLin = srgbToLinear[g8]
            val bLin = srgbToLinear[b8]

            // D65 sRGB -> XYZ normalized by reference white (Xn=0.95047, Yn=1.00000, Zn=1.08883)
            val x = (0.4124564f * rLin + 0.3575761f * gLin + 0.1804375f * bLin) / 0.95047f
            val y = 0.2126729f * rLin + 0.7151522f * gLin + 0.0721750f * bLin
            val z = (0.0193339f * rLin + 0.1191920f * gLin + 0.9503041f * bLin) / 1.08883f

            val fx = fLab(x)
            val fy = fLab(y)
            val fz = fLab(z)

            outL[i] = 116f * fy - 16f
            outA[i] = 500f * (fx - fy)
            outB[i] = 200f * (fy - fz)
        }
    }

    /**
     * Fast 3x3 separable [1, 2, 1]/4 binomial smoothing on a 2D FloatArray to suppress JPEG noise
     * while preserving clean object contours.
     */
    fun smoothBinomial3x3(src: FloatArray, dst: FloatArray, tmp: FloatArray, w: Int, h: Int) {
        if (w < 3 || h < 3) {
            src.copyInto(dst)
            return
        }
        // Horizontal pass
        for (y in 0 until h) {
            val row = y * w
            tmp[row] = (3f * src[row] + src[row + 1]) * 0.25f
            for (x in 1 until w - 1) {
                tmp[row + x] = (src[row + x - 1] + 2f * src[row + x] + src[row + x + 1]) * 0.25f
            }
            tmp[row + w - 1] = (src[row + w - 2] + 3f * src[row + w - 1]) * 0.25f
        }
        // Vertical pass
        for (x in 0 until w) {
            dst[x] = (3f * tmp[x] + tmp[w + x]) * 0.25f
        }
        for (y in 1 until h - 1) {
            val prev = (y - 1) * w
            val curr = y * w
            val next = (y + 1) * w
            for (x in 0 until w) {
                dst[curr + x] = (tmp[prev + x] + 2f * tmp[curr + x] + tmp[next + x]) * 0.25f
            }
        }
        val last = (h - 1) * w
        val prevLast = (h - 2) * w
        for (x in 0 until w) {
            dst[last + x] = (tmp[prevLast + x] + 3f * tmp[last + x]) * 0.25f
        }
    }

    fun argbToLab(argb: Int, outLab: FloatArray) {
        val r8 = (argb ushr 16) and 0xFF
        val g8 = (argb ushr 8) and 0xFF
        val b8 = argb and 0xFF

        val rLin = srgbToLinear[r8]
        val gLin = srgbToLinear[g8]
        val bLin = srgbToLinear[b8]

        val x = (0.4124564f * rLin + 0.3575761f * gLin + 0.1804375f * bLin) / 0.95047f
        val y = 0.2126729f * rLin + 0.7151522f * gLin + 0.0721750f * bLin
        val z = (0.0193339f * rLin + 0.1191920f * gLin + 0.9503041f * bLin) / 1.08883f

        val fx = fLab(x)
        val fy = fLab(y)
        val fz = fLab(z)

        outLab[0] = 116f * fy - 16f
        outLab[1] = 500f * (fx - fy)
        outLab[2] = 200f * (fy - fz)
    }

    fun deltaE(l1: Float, a1: Float, b1: Float, l2: Float, a2: Float, b2: Float): Float {
        val dl = l1 - l2
        val da = a1 - a2
        val db = b1 - b2
        return sqrt(dl * dl + da * da + db * db)
    }

    fun pixelLuminance(argb: Int): Float {
        val r8 = (argb ushr 16) and 0xFF
        val g8 = (argb ushr 8) and 0xFF
        val b8 = argb and 0xFF
        return 0.299f * r8 + 0.587f * g8 + 0.114f * b8
    }

    /**
     * Converts CIE L*a*b* into gamut-clamped sRGB ARGB (0xFFRRGGBB), proportionally scaling chroma
     * if out of sRGB gamut so the atmospheric hue angle remains pure without channel clipping.
     */
    fun labToSrgbArgb(lStar: Float, aStar: Float, bStar: Float): Int {
        var scale = 1.0f
        var lo = 0f
        var hi = 1f
        val fy = (lStar.coerceIn(0f, 100f) + 16f) / 116f

        fun labToLinearRgb(a: Float, b: Float): Triple<Float, Float, Float> {
            val fx = a / 500f + fy
            val fz = fy - b / 200f
            fun fInv(t: Float): Float {
                val t3 = t * t * t
                return if (t3 > LAB_EPSILON) t3 else (116f * t - 16f) / LAB_KAPPA
            }
            val x = fInv(fx) * 0.95047f
            val y = fInv(fy) * 1.00000f
            val z = fInv(fz) * 1.08883f

            val r = 3.2404542f * x - 1.5371385f * y - 0.4985314f * z
            val g = -0.9692660f * x + 1.8760108f * y + 0.0415560f * z
            val bl = 0.0556434f * x - 0.2040259f * y + 1.0572252f * z
            return Triple(r, g, bl)
        }

        val (r0, g0, b0) = labToLinearRgb(aStar, bStar)
        if (r0 < 0f || r0 > 1f || g0 < 0f || g0 > 1f || b0 < 0f || b0 > 1f) {
            repeat(12) {
                val mid = (lo + hi) * 0.5f
                val (rm, gm, bm) = labToLinearRgb(aStar * mid, bStar * mid)
                if (rm in 0f..1f && gm in 0f..1f && bm in 0f..1f) {
                    lo = mid
                } else {
                    hi = mid
                }
            }
            scale = lo
        }

        val (rLin, gLin, bLin) = labToLinearRgb(aStar * scale, bStar * scale)
        fun linearToSrgb8(lin: Float): Int {
            val c = lin.coerceIn(0f, 1f)
            val s = if (c <= 0.0031308f) 12.92f * c else (1.055f * c.toDouble().pow(1.0 / 2.4).toFloat() - 0.055f)
            return (s * 255f + 0.5f).toInt().coerceIn(0, 255)
        }
        val r8 = linearToSrgb8(rLin)
        val g8 = linearToSrgb8(gLin)
        val b8 = linearToSrgb8(bLin)
        return (0xFF shl 24) or (r8 shl 16) or (g8 shl 8) or b8
    }

    /**
     * Derives a crisp yet organic background-matched primary clock color in CIE L*a*b*.
     * Retains calibrated atmospheric sky tint so numerals naturally harmonize with the wallpaper
     * rather than looking like disconnected stark black/white labels.
     */
    fun pickNaturalHarmonicClockHex(avgSkyL: Float, avgSkyA: Float, avgSkyB: Float): String {
        val chroma = sqrt(avgSkyA * avgSkyA + avgSkyB * avgSkyB)
        val tintAttenuation = if (chroma > 1e-3f) minOf(1f, 22f / chroma) else 0f
        val harmA = avgSkyA * 0.34f * tintAttenuation
        val harmB = avgSkyB * 0.34f * tintAttenuation

        val targetL = when {
            avgSkyL >= 76f -> 18f // Bright snow/pastel sky -> deep atmospheric slate/ink tint
            avgSkyL >= 62f -> 21f // Warm cream/daylight sky -> rich tonal charcoal tint
            avgSkyL >= 42f -> 95f // Mid-density twilight/clouds -> crisp pearlescent highlight
            else -> 92f           // Deep night/charcoal -> luminous atmospheric mist white
        }
        // Subtle ambient hue bias when sky is achromatic neutral so ink never reads flat grey
        val finalA = if (chroma < 3f) if (targetL > 50f) 1.5f else -1.5f else harmA
        val finalB = if (chroma < 3f) if (targetL > 50f) -3.5f else -5.0f else harmB

        val argb = labToSrgbArgb(targetL, finalA, finalB)
        return String.format("#%06X", argb and 0x00FFFFFF)
    }

    /**
     * Backward-compatible entry point delegating to [pickNaturalHarmonicClockHex].
     */
    fun pickHighContrastClockHex(avgSkyL: Float, avgSkyA: Float, avgSkyB: Float): String =
        pickNaturalHarmonicClockHex(avgSkyL, avgSkyA, avgSkyB)

    /**
     * Refines a wallpaper swatch into a secondary clock gradient/accent tint paired naturally
     * against [primaryArgb] without muddy low-contrast clipping.
     */
    fun harmonizeWallpaperAccent(swatchArgb: Int, primaryArgb: Int): Int {
        val swatchLab = FloatArray(3)
        val primaryLab = FloatArray(3)
        argbToLab(swatchArgb, swatchLab)
        argbToLab(primaryArgb, primaryLab)

        val primaryBright = primaryLab[0] >= 55f
        val targetL = if (primaryBright) {
            swatchLab[0].coerceIn(68f, 86f)
        } else {
            swatchLab[0].coerceIn(26f, 44f)
        }
        val chroma = sqrt(swatchLab[1] * swatchLab[1] + swatchLab[2] * swatchLab[2])
        val maxChroma = if (primaryBright) 36f else 32f
        val chromaGain = when {
            chroma < 1f -> 1f
            chroma < 14f -> 1.45f
            chroma > maxChroma -> maxChroma / chroma
            else -> 1f
        }
        val a = swatchLab[1] * chromaGain
        val b = swatchLab[2] * chromaGain
        return labToSrgbArgb(targetL, a, b)
    }
}
