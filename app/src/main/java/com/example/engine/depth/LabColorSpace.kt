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
     * Chooses a crisp, high-contrast clock color hex tuned to the sky/backdrop luminance behind the clock.
     */
    fun pickHighContrastClockHex(avgSkyL: Float, avgSkyA: Float, avgSkyB: Float): String {
        return when {
            avgSkyL >= 76f -> "#181922" // Very bright snow/alabaster/pastel sky -> deep charcoal ink
            avgSkyL >= 62f -> "#231F2B" // Mid-bright daylight/cream sky -> rich slate charcoal
            avgSkyB < -12f && avgSkyL < 38f -> "#F2F5FF" // Deep twilight blue -> crisp cool ice-white
            avgSkyA > 10f && avgSkyL < 45f -> "#FFF4EC" // Warm dusk/rose sky -> soft warm ivory
            else -> "#FFFFFF"
        }
    }
}
