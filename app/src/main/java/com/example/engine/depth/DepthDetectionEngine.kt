package com.example.engine.depth

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Deterministic, pixel-accurate on-device Depth Detection Engine for Backdrops.
 *
 * Pipeline:
 * 1. Safe software ARGB_8888 normalization + aspect-preserving analysis grid (<= 360px wide).
 * 2. Perceptual CIE L*a*b* conversion + binomial noise suppression + Sobel contour magnitude.
 * 3. Smooth-Sky Geodesic Edge-Barrier Propagation (4-directional raster sweeps x 2 iterations):
 *    smooth gradient skies accumulate ~0 barrier cost while foreground boundaries create sharp jumps.
 * 4. Adaptive Upper-Sky Prototype Divergence + Boundary-Guided Saliency Fusion & Otsu thresholding.
 * 5. Connected-component denoising, border-seeded interior hole closing, and lock-clock overlap
 *    eligibility verification (ensures open headroom above a distinct foreground subject).
 * 6. Full-resolution Luminance-Guided Joint Alpha Upsampling along silhouette edges for zero haloing.
 */
class DepthDetectionEngine {

    suspend fun detectAndExtract(
        sourceBitmap: Bitmap,
        titleHint: String = "Wallpaper"
    ): DepthDetectionResult = withContext(Dispatchers.Default) {
        val startMs = SystemClock.elapsedRealtime()
        val fullBmp = ensureSoftwareArgb8888(sourceBitmap)
        val fullW = fullBmp.width
        val fullH = fullBmp.height

        if (fullW < 32 || fullH < 32) {
            return@withContext flatResult(
                titleHint = titleHint,
                processingTimeMs = SystemClock.elapsedRealtime() - startMs,
                skyL = 20f,
                skyA = 0f,
                skyB = 0f
            )
        }

        // Scale to balanced coarse grid (360px on shorter/width axis, preserving aspect ratio)
        val targetCw = min(ANALYSIS_WIDTH, fullW)
        val targetCh = max(32, (fullH.toLong() * targetCw / fullW).toInt())
        val coarseBmp = if (fullW == targetCw && fullH == targetCh) {
            fullBmp
        } else {
            fullBmp.scale(targetCw, targetCh)
        }

        val cw = coarseBmp.width
        val ch = coarseBmp.height
        val n = cw * ch

        val coarsePixels = IntArray(n)
        coarseBmp.getPixels(coarsePixels, 0, cw, 0, 0, cw, ch)
        if (coarseBmp !== fullBmp) {
            coarseBmp.recycle()
        }

        coroutineContext.ensureActive()

        // 1. Convert coarse pixels to perceptual CIE L*a*b* + luminance
        val rawL = FloatArray(n)
        val rawA = FloatArray(n)
        val rawB = FloatArray(n)
        val coarseLum = FloatArray(n)
        LabColorSpace.convertArgbToLabAndLum(coarsePixels, rawL, rawA, rawB, coarseLum)

        // Smooth Lab slightly to ignore film grain / JPEG macroblocks in the sky
        val smL = FloatArray(n)
        val smA = FloatArray(n)
        val smB = FloatArray(n)
        val tmp = FloatArray(n)
        LabColorSpace.smoothBinomial3x3(rawL, smL, tmp, cw, ch)
        LabColorSpace.smoothBinomial3x3(rawA, smA, tmp, cw, ch)
        LabColorSpace.smoothBinomial3x3(rawB, smB, tmp, cw, ch)

        coroutineContext.ensureActive()

        // 2. Compute perceptual Lab Sobel gradient magnitude
        val edgeMag = FloatArray(n)
        computeLabSobel(smL, smA, smB, edgeMag, cw, ch)

        // 3. Build Top-Sky Prototypes & Seed Map
        // Identify low-edge upper sky pixels (top 3 rows + clean upper margin pixels)
        val skySeeds = BooleanArray(n)
        val topBorderStats = buildTopSkyModel(smL, smA, smB, edgeMag, skySeeds, cw, ch)

        // 4. Multi-sweep Smooth-Sky Geodesic Edge-Barrier Distance Transform
        val geoDist = FloatArray(n) { Float.POSITIVE_INFINITY }
        for (i in 0 until n) {
            if (skySeeds[i]) geoDist[i] = 0f
        }
        runGeodesicBarrierPasses(geoDist, smL, smA, smB, edgeMag, cw, ch)

        coroutineContext.ensureActive()

        // 5. Compute Upper-Sky Prototype Divergence (helps isolate glowing orbs or high-contrast dark peaks)
        val protoDiv = FloatArray(n)
        computeSkyPrototypeDivergence(smL, smA, smB, topBorderStats, protoDiv)

        // Normalize geodesic barrier distance and sky divergence at the 98.8th percentile so compact
        // subjects (moons, spires, blossoms covering >= 1.5% of frame) retain full dynamic range.
        val maxGeo = percentile(geoDist, 0.988f).coerceAtLeast(1f)
        val maxDiv = percentile(protoDiv, 0.988f).coerceAtLeast(1f)

        // Fuse Geodesic Barrier (primary structural separator) with moderated Sky Divergence.
        // Max pure-gradient sky contribution is capped at 0.18 * 1.25 = 0.225 < min Otsu tau (0.28).
        val saliency = FloatArray(n)
        for (i in 0 until n) {
            val gNorm = (geoDist[i] / maxGeo).coerceIn(0f, 1.35f)
            val dNorm = (protoDiv[i] / maxDiv).coerceIn(0f, 1.25f)
            val fused = (0.82f * gNorm + 0.18f * dNorm).coerceIn(0f, 1f)
            saliency[i] = fused
        }

        // Check global separation strength before thresholding
        val saliencyContrast = computeForegroundBackgroundContrast(saliency)
        if (saliencyContrast < MIN_SEPARATION_CONTRAST || maxGeo < MIN_BARRIER_LAB) {
            return@withContext flatResult(
                titleHint = titleHint,
                processingTimeMs = SystemClock.elapsedRealtime() - startMs,
                skyL = topBorderStats.meanL,
                skyA = topBorderStats.meanA,
                skyB = topBorderStats.meanB
            )
        }

        // 6. Adaptive Otsu thresholding + Connected-Component Cleanup + Border-Seeded Hole Closing
        val tau = computeOtsuThreshold(saliency).coerceIn(0.28f, 0.64f)
        val binaryMask = BooleanArray(n) { i -> saliency[i] >= tau }

        val cleanedMask = filterComponentsAndCloseHoles(binaryMask, saliency, cw, ch)

        coroutineContext.ensureActive()

        // 7. Evaluate whether the detected foreground actually forms a lock-screen Depth Subject
        val geometry = evaluateDepthSubjectGeometry(cleanedMask, saliency, cw, ch, topBorderStats)
        if (!geometry.isEligible) {
            return@withContext DepthDetectionResult(
                subjectLabel = "No isolated foreground",
                subjectCategory = DepthSubjectCategory.FLAT_CANVAS,
                isDepthEligible = false,
                depthQualityScore = geometry.qualityScore,
                cutoutBitmap = null,
                recommendedClockTopFraction = 0.14f,
                recommendedAlign = ClockHorizontalAlign.CENTER,
                recommendedClockColorHex = LabColorSpace.pickNaturalHarmonicClockHex(
                    topBorderStats.meanL,
                    topBorderStats.meanA,
                    topBorderStats.meanB
                ),
                subjectTopFraction = geometry.peakYFraction,
                subjectPeakXFraction = geometry.peakXFraction,
                foregroundCoverageFraction = geometry.coverageFraction,
                processingTimeMs = SystemClock.elapsedRealtime() - startMs
            )
        }

        // 8. Refine coarse alpha matte (sub-pixel boundary transition) and upsample cleanly to full resolution
        val coarseAlpha = buildSoftCoarseAlpha(cleanedMask, saliency, tau, cw, ch)
        val cutoutBitmap = upsampleCutoutLabGuided(
            fullBitmap = fullBmp,
            coarseAlpha = coarseAlpha,
            coarseL = rawL,
            coarseA = rawA,
            coarseB = rawB,
            cw = cw,
            ch = ch
        )

        val clockColorHex = sampleClockRegionContrastHex(
            smL = smL,
            smA = smA,
            smB = smB,
            mask = cleanedMask,
            cw = cw,
            ch = ch,
            clockTopFrac = geometry.recommendedClockTopFraction,
            fallbackStats = topBorderStats
        )

        val label = inferSubjectLabel(geometry.category, titleHint)

        DepthDetectionResult(
            subjectLabel = label,
            subjectCategory = geometry.category,
            isDepthEligible = true,
            depthQualityScore = geometry.qualityScore,
            cutoutBitmap = cutoutBitmap,
            recommendedClockTopFraction = geometry.recommendedClockTopFraction,
            recommendedAlign = geometry.recommendedAlign,
            recommendedClockColorHex = clockColorHex,
            subjectTopFraction = geometry.peakYFraction,
            subjectPeakXFraction = geometry.peakXFraction,
            foregroundCoverageFraction = geometry.coverageFraction,
            processingTimeMs = SystemClock.elapsedRealtime() - startMs,
            recommendedClockOverlapFraction = geometry.clockOcclusionFraction,
            recommendedWallpaperZoom = geometry.recommendedWallpaperZoom,
            recommendedWallpaperPanXFraction = geometry.recommendedWallpaperPanXFraction,
            recommendedWallpaperPanYFraction = geometry.recommendedWallpaperPanYFraction,
            recommendedClockScale = geometry.recommendedClockScale
        )
    }

    private fun computeLabSobel(
        lArr: FloatArray,
        aArr: FloatArray,
        bArr: FloatArray,
        outEdge: FloatArray,
        w: Int,
        h: Int
    ) {
        for (y in 1 until h - 1) {
            val rowM = (y - 1) * w
            val rowC = y * w
            val rowP = (y + 1) * w
            for (x in 1 until w - 1) {
                val tl = rowM + x - 1
                val tc = rowM + x
                val tr = rowM + x + 1
                val ml = rowC + x - 1
                val mr = rowC + x + 1
                val bl = rowP + x - 1
                val bc = rowP + x
                val br = rowP + x + 1

                val gxL = (lArr[tr] + 2f * lArr[mr] + lArr[br]) - (lArr[tl] + 2f * lArr[ml] + lArr[bl])
                val gyL = (lArr[bl] + 2f * lArr[bc] + lArr[br]) - (lArr[tl] + 2f * lArr[tc] + lArr[tr])

                val gxA = (aArr[tr] + 2f * aArr[mr] + aArr[br]) - (aArr[tl] + 2f * aArr[ml] + aArr[bl])
                val gyA = (aArr[bl] + 2f * aArr[bc] + aArr[br]) - (aArr[tl] + 2f * aArr[tc] + aArr[tr])

                val gxB = (bArr[tr] + 2f * bArr[mr] + bArr[br]) - (bArr[tl] + 2f * bArr[ml] + bArr[bl])
                val gyB = (bArr[bl] + 2f * bArr[bc] + bArr[br]) - (bArr[tl] + 2f * bArr[tc] + bArr[tr])

                val mag = sqrt(gxL * gxL + gyL * gyL + gxA * gxA + gyA * gyA + gxB * gxB + gyB * gyB) * 0.125f
                outEdge[rowC + x] = mag
            }
        }
    }

    private data class SkyModel(
        val protoL: FloatArray,
        val protoA: FloatArray,
        val protoB: FloatArray,
        val meanL: Float,
        val meanA: Float,
        val meanB: Float
    )

    private fun buildTopSkyModel(
        lArr: FloatArray,
        aArr: FloatArray,
        bArr: FloatArray,
        edgeMag: FloatArray,
        skySeeds: BooleanArray,
        w: Int,
        h: Int
    ): SkyModel {
        // Seed top 2 rows where edge gradient is calm (fallback to entire top row if textured)
        val topBandRows = max(2, (h * 0.015f).roundToInt())
        var seedCount = 0
        for (y in 0 until topBandRows) {
            val row = y * w
            for (x in 0 until w) {
                val idx = row + x
                if (edgeMag[idx] < 6.5f) {
                    skySeeds[idx] = true
                    seedCount++
                }
            }
        }
        if (seedCount < w / 3) {
            for (x in 0 until w) {
                skySeeds[x] = true
            }
        }

        // Also seed calm upper-left & upper-right margins matching top sky (for centered floating subjects)
        var sumL = 0f
        var sumA = 0f
        var sumB = 0f
        var cnt = 0
        val sampleMaxY = max(4, (h * 0.12f).roundToInt())
        for (y in 0 until sampleMaxY step 2) {
            val row = y * w
            for (x in 0 until w step 2) {
                val idx = row + x
                if (edgeMag[idx] < 5.0f) {
                    sumL += lArr[idx]
                    sumA += aArr[idx]
                    sumB += bArr[idx]
                    cnt++
                }
            }
        }
        if (cnt == 0) {
            for (x in 0 until w) {
                sumL += lArr[x]
                sumA += aArr[x]
                sumB += bArr[x]
                cnt++
            }
        }
        val meanL = sumL / cnt
        val meanA = sumA / cnt
        val meanB = sumB / cnt

        // Capture 3 upper-sky color prototypes (left, center, right calm samples across top 16%)
        val pL = FloatArray(3)
        val pA = FloatArray(3)
        val pB = FloatArray(3)
        val regions = intArrayOf(0, w / 3, (2 * w) / 3, w)
        val upperH = max(4, (h * 0.16f).roundToInt())
        for (r in 0 until 3) {
            var rL = 0f
            var rA = 0f
            var rB = 0f
            var rC = 0
            for (y in 0 until upperH step 2) {
                val row = y * w
                for (x in regions[r] until regions[r + 1] step 2) {
                    val idx = row + x
                    if (edgeMag[idx] < 5.5f) {
                        rL += lArr[idx]
                        rA += aArr[idx]
                        rB += bArr[idx]
                        rC++
                    }
                }
            }
            if (rC > 0) {
                pL[r] = rL / rC
                pA[r] = rA / rC
                pB[r] = rB / rC
            } else {
                pL[r] = meanL
                pA[r] = meanA
                pB[r] = meanB
            }
        }

        // Walk contiguously down left & right upper margins from y=0, stopping immediately
        // if an object silhouette crosses the border (prevents seed leaks into dark silhouettes).
        val marginMaxY = (h * 0.45f).roundToInt().coerceIn(2, h - 1)
        var leftOpen = skySeeds[0]
        var rightOpen = skySeeds[w - 1]
        for (y in 1 until marginMaxY) {
            val leftIdx = y * w
            val prevLeft = leftIdx - w
            if (leftOpen) {
                val step = LabColorSpace.deltaE(
                    lArr[leftIdx], aArr[leftIdx], bArr[leftIdx],
                    lArr[prevLeft], aArr[prevLeft], bArr[prevLeft]
                )
                if (edgeMag[leftIdx] < 4.5f && step < 3.0f) {
                    skySeeds[leftIdx] = true
                } else {
                    leftOpen = false
                }
            }

            val rightIdx = leftIdx + w - 1
            val prevRight = prevLeft + w - 1
            if (rightOpen) {
                val step = LabColorSpace.deltaE(
                    lArr[rightIdx], aArr[rightIdx], bArr[rightIdx],
                    lArr[prevRight], aArr[prevRight], bArr[prevRight]
                )
                if (edgeMag[rightIdx] < 4.5f && step < 3.0f) {
                    skySeeds[rightIdx] = true
                } else {
                    rightOpen = false
                }
            }
            if (!leftOpen && !rightOpen) break
        }

        return SkyModel(pL, pA, pB, meanL, meanA, meanB)
    }

    private fun runGeodesicBarrierPasses(
        dist: FloatArray,
        lArr: FloatArray,
        aArr: FloatArray,
        bArr: FloatArray,
        edgeMag: FloatArray,
        w: Int,
        h: Int
    ) {
        // Two full 4-directional raster sweep iterations propagate smoothly around concave curves
        // and floating celestial subjects in ~18ms total.
        repeat(2) {
            // Pass 1: Top-Left -> Bottom-Right
            for (y in 0 until h) {
                val row = y * w
                for (x in 0 until w) {
                    val p = row + x
                    var best = dist[p]
                    if (x > 0) {
                        val c = stepCost(p - 1, p, 1f, dist, lArr, aArr, bArr, edgeMag)
                        if (c < best) best = c
                    }
                    if (y > 0) {
                        val q = p - w
                        val c = stepCost(q, p, 1f, dist, lArr, aArr, bArr, edgeMag)
                        if (c < best) best = c
                        if (x > 0) {
                            val cd = stepCost(q - 1, p, 1.35f, dist, lArr, aArr, bArr, edgeMag)
                            if (cd < best) best = cd
                        }
                        if (x < w - 1) {
                            val cd = stepCost(q + 1, p, 1.35f, dist, lArr, aArr, bArr, edgeMag)
                            if (cd < best) best = cd
                        }
                    }
                    dist[p] = best
                }
            }

            // Pass 2: Bottom-Right -> Top-Left
            for (y in h - 1 downTo 0) {
                val row = y * w
                for (x in w - 1 downTo 0) {
                    val p = row + x
                    var best = dist[p]
                    if (x < w - 1) {
                        val c = stepCost(p + 1, p, 1f, dist, lArr, aArr, bArr, edgeMag)
                        if (c < best) best = c
                    }
                    if (y < h - 1) {
                        val q = p + w
                        val c = stepCost(q, p, 1f, dist, lArr, aArr, bArr, edgeMag)
                        if (c < best) best = c
                        if (x > 0) {
                            val cd = stepCost(q - 1, p, 1.35f, dist, lArr, aArr, bArr, edgeMag)
                            if (cd < best) best = cd
                        }
                        if (x < w - 1) {
                            val cd = stepCost(q + 1, p, 1.35f, dist, lArr, aArr, bArr, edgeMag)
                            if (cd < best) best = cd
                        }
                    }
                    dist[p] = best
                }
            }

            // Pass 3: Top-Right -> Bottom-Left
            for (y in 0 until h) {
                val row = y * w
                for (x in w - 1 downTo 0) {
                    val p = row + x
                    var best = dist[p]
                    if (x < w - 1) {
                        val c = stepCost(p + 1, p, 1f, dist, lArr, aArr, bArr, edgeMag)
                        if (c < best) best = c
                    }
                    if (y > 0) {
                        val q = p - w
                        val c = stepCost(q, p, 1f, dist, lArr, aArr, bArr, edgeMag)
                        if (c < best) best = c
                    }
                    dist[p] = best
                }
            }

            // Pass 4: Bottom-Left -> Top-Right
            for (y in h - 1 downTo 0) {
                val row = y * w
                for (x in 0 until w) {
                    val p = row + x
                    var best = dist[p]
                    if (x > 0) {
                        val c = stepCost(p - 1, p, 1f, dist, lArr, aArr, bArr, edgeMag)
                        if (c < best) best = c
                    }
                    if (y < h - 1) {
                        val q = p + w
                        val c = stepCost(q, p, 1f, dist, lArr, aArr, bArr, edgeMag)
                        if (c < best) best = c
                    }
                    dist[p] = best
                }
            }
        }
    }

    private fun stepCost(
        q: Int,
        p: Int,
        diagWeight: Float,
        dist: FloatArray,
        lArr: FloatArray,
        aArr: FloatArray,
        bArr: FloatArray,
        edgeMag: FloatArray
    ): Float {
        val dq = dist[q]
        if (dq == Float.POSITIVE_INFINITY) return Float.POSITIVE_INFINITY
        val dL = lArr[p] - lArr[q]
        val dA = aArr[p] - aArr[q]
        val dB = bArr[p] - bArr[q]
        val stepDeltaE = sqrt(dL * dL + dA * dA + dB * dB)
        // Smooth-sky deadzone: gradients < 1.85 deltaE/px accumulate zero barrier cost
        val barrierColor = max(0f, stepDeltaE - SMOOTH_SKY_DEADZONE)
        val barrierEdge = max(0f, edgeMag[p] - 2.0f) * 0.48f
        return dq + (barrierColor + barrierEdge) * diagWeight
    }

    private fun computeSkyPrototypeDivergence(
        lArr: FloatArray,
        aArr: FloatArray,
        bArr: FloatArray,
        skyModel: SkyModel,
        outDiv: FloatArray
    ) {
        val pL = skyModel.protoL
        val pA = skyModel.protoA
        val pB = skyModel.protoB
        for (i in lArr.indices) {
            val l = lArr[i]
            val a = aArr[i]
            val b = bArr[i]
            val d0 = LabColorSpace.deltaE(l, a, b, pL[0], pA[0], pB[0])
            val d1 = LabColorSpace.deltaE(l, a, b, pL[1], pA[1], pB[1])
            val d2 = LabColorSpace.deltaE(l, a, b, pL[2], pA[2], pB[2])
            outDiv[i] = min(d0, min(d1, d2))
        }
    }

    private fun percentile(values: FloatArray, quantile: Float): Float {
        val hist = IntArray(256)
        var maxV = 0f
        for (v in values) {
            if (v.isFinite() && v > maxV) maxV = v
        }
        if (maxV <= 0f) return 0f
        val inv = 255f / maxV
        for (v in values) {
            val bin = if (v.isFinite()) (v * inv).toInt().coerceIn(0, 255) else 255
            hist[bin]++
        }
        val target = (values.size * quantile).toInt()
        var acc = 0
        for (bin in 0..255) {
            acc += hist[bin]
            if (acc >= target) return (bin / 255f) * maxV
        }
        return maxV
    }

    private fun computeForegroundBackgroundContrast(saliency: FloatArray): Float {
        val hist = IntArray(64)
        for (v in saliency) {
            hist[(v * 63f).toInt().coerceIn(0, 63)]++
        }
        var acc = 0
        val q20Target = (saliency.size * 0.20f).toInt()
        val q985Target = (saliency.size * 0.985f).toInt()
        var q20 = 0f
        var q985 = 0f
        var found20 = false
        for (b in 0..63) {
            acc += hist[b]
            if (!found20 && acc >= q20Target) {
                q20 = b / 63f
                found20 = true
            }
            if (acc >= q985Target) {
                q985 = b / 63f
                break
            }
        }
        return q985 - q20
    }

    private fun computeOtsuThreshold(saliency: FloatArray): Float {
        val hist = IntArray(256)
        var sumTotal = 0.0
        for (v in saliency) {
            val bin = (v * 255f).toInt().coerceIn(0, 255)
            hist[bin]++
            sumTotal += bin
        }
        val total = saliency.size.toDouble()
        var sumB = 0.0
        var wB = 0
        var maxBetween = -1.0
        var bestBin = 110

        for (t in 0..255) {
            wB += hist[t]
            if (wB == 0) continue
            val wF = saliency.size - wB
            if (wF == 0) break
            sumB += t.toDouble() * hist[t]
            val mB = sumB / wB
            val mF = (sumTotal - sumB) / wF
            val between = wB.toDouble() * wF.toDouble() * (mB - mF) * (mB - mF)
            if (between > maxBetween) {
                maxBetween = between
                bestBin = t
            }
        }
        return bestBin / 255f
    }

    private data class CandidateComponent(
        val pixels: IntArray,
        val area: Int,
        val meanSaliency: Float,
        val minY: Int,
        val maxY: Int,
        val minX: Int,
        val maxX: Int,
        val touchesBottom: Boolean,
        val dominanceScore: Float
    )

    private fun filterComponentsAndCloseHoles(
        mask: BooleanArray,
        saliency: FloatArray,
        w: Int,
        h: Int
    ): BooleanArray {
        val n = w * h
        val visited = BooleanArray(n)
        val queue = IntArray(n)
        val keep = BooleanArray(n)
        val minCandidateArea = max(10, (n * 0.003f).toInt())
        val minMainArea = max(12, (n * 0.008f).toInt())

        val candidates = ArrayList<CandidateComponent>(8)

        // 1. Collect connected foreground components and score subject dominance
        for (start in 0 until n) {
            if (!mask[start] || visited[start]) continue
            var head = 0
            var tail = 0
            queue[tail++] = start
            visited[start] = true

            var sumSal = 0f
            var minY = h
            var maxY = 0
            var minX = w
            var maxX = 0
            while (head < tail) {
                val p = queue[head++]
                sumSal += saliency[p]
                val py = p / w
                val px = p - py * w
                if (py < minY) minY = py
                if (py > maxY) maxY = py
                if (px < minX) minX = px
                if (px > maxX) maxX = px

                if (px > 0 && mask[p - 1] && !visited[p - 1]) {
                    visited[p - 1] = true; queue[tail++] = p - 1
                }
                if (px < w - 1 && mask[p + 1] && !visited[p + 1]) {
                    visited[p + 1] = true; queue[tail++] = p + 1
                }
                if (py > 0 && mask[p - w] && !visited[p - w]) {
                    visited[p - w] = true; queue[tail++] = p - w
                }
                if (py < h - 1 && mask[p + w] && !visited[p + w]) {
                    visited[p + w] = true; queue[tail++] = p + w
                }
            }

            val area = tail
            if (area < minCandidateArea) continue
            val meanSal = sumSal / area
            val touchesBottom = maxY >= h - max(4, (h * 0.03f).roundToInt())
            val isSignificant = area >= minMainArea ||
                (meanSal > 0.58f && minY > h * 0.04f && maxY < h * 0.90f)
            if (!isSignificant) continue

            // Prefer strong lower-anchored or clean mid-frame silhouettes over top-edge stray wisps
            val topEdgePenalty = if (minY <= max(2, (h * 0.025f).roundToInt()) && maxY < h * 0.40f) 0.25f else 1.0f
            val anchorBonus = if (touchesBottom) 1.65f else 1.0f
            val score = area.toFloat() * meanSal * anchorBonus * topEdgePenalty

            candidates.add(
                CandidateComponent(
                    pixels = queue.copyOf(area),
                    area = area,
                    meanSaliency = meanSal,
                    minY = minY,
                    maxY = maxY,
                    minX = minX,
                    maxX = maxX,
                    touchesBottom = touchesBottom,
                    dominanceScore = score
                )
            )
        }

        // Retain the dominant foreground subject and only spatially close, substantial companion parts
        // (strips detached upper clouds, birds, or ceiling vignettes so the silhouette is clean).
        val dominant = candidates.maxByOrNull { it.dominanceScore }
        if (dominant != null) {
            val proxY = (h * 0.10f).roundToInt()
            val proxX = (w * 0.14f).roundToInt()
            val minCompanionArea = max(minMainArea, (dominant.area * 0.18f).roundToInt())
            val upperCutoffY = dominant.minY - (h * 0.12f).roundToInt()

            for (comp in candidates) {
                val isDominant = comp === dominant
                val gapX = max(0, max(comp.minX - dominant.maxX, dominant.minX - comp.maxX))
                val gapY = max(0, max(comp.minY - dominant.maxY, dominant.minY - comp.maxY))
                val isCohesiveCompanion = !isDominant &&
                    comp.area >= minCompanionArea &&
                    comp.minY >= upperCutoffY &&
                    gapX <= proxX &&
                    gapY <= proxY &&
                    (comp.touchesBottom || gapY <= proxY / 2)

                if (isDominant || isCohesiveCompanion) {
                    for (p in comp.pixels) {
                        keep[p] = true
                    }
                }
            }
        }

        // 2. Border-seeded sky flood fill: closes interior holes inside subjects (e.g. bright snow patches, eyes, moon craters)
        val skyReachable = BooleanArray(n)
        var head = 0
        var tail = 0

        fun tryEnqueueSky(idx: Int) {
            if (!keep[idx] && !skyReachable[idx]) {
                skyReachable[idx] = true
                queue[tail++] = idx
            }
        }

        for (x in 0 until w) {
            tryEnqueueSky(x)
            tryEnqueueSky((h - 1) * w + x)
        }
        for (y in 0 until h) {
            tryEnqueueSky(y * w)
            tryEnqueueSky(y * w + w - 1)
        }

        while (head < tail) {
            val p = queue[head++]
            val py = p / w
            val px = p - py * w
            if (px > 0) tryEnqueueSky(p - 1)
            if (px < w - 1) tryEnqueueSky(p + 1)
            if (py > 0) tryEnqueueSky(p - w)
            if (py < h - 1) tryEnqueueSky(p + w)
        }

        return BooleanArray(n) { i -> !skyReachable[i] }
    }

    private data class DepthGeometryEval(
        val isEligible: Boolean,
        val category: DepthSubjectCategory,
        val qualityScore: Int,
        val peakYFraction: Float,
        val peakXFraction: Float,
        val coverageFraction: Float,
        val recommendedClockTopFraction: Float,
        val recommendedAlign: ClockHorizontalAlign,
        val clockOcclusionFraction: Float,
        val recommendedWallpaperZoom: Float = 1.0f,
        val recommendedWallpaperPanXFraction: Float = 0f,
        val recommendedWallpaperPanYFraction: Float = 0f,
        val recommendedClockScale: Float = 1.0f
    )

    private fun evaluateDepthSubjectGeometry(
        mask: BooleanArray,
        saliency: FloatArray,
        w: Int,
        h: Int,
        skyStats: SkyModel
    ): DepthGeometryEval {
        val n = w * h
        var fgCount = 0
        var minY = h
        var maxY = -1
        var minX = w
        var maxX = -1

        var topBandCount = 0
        var topBandMeanX = 0f

        val topClearEnd = (h * 0.08f).roundToInt().coerceAtLeast(1)
        var topClearFg = 0

        val clockBandStart = (h * 0.10f).roundToInt()
        val clockBandEnd = (h * 0.56f).roundToInt().coerceAtMost(h)
        var clockBandFg = 0
        var clockBandMeanX = 0f

        // Per-row occupancy across horizontal zones [ALL, LEFT, CENTER, RIGHT] for exact clock sliding solver
        val rowTotalCenter = FloatArray(h)
        val rowTotalStart = FloatArray(h)
        val rowTotalEnd = FloatArray(h)
        val cStart = (w * 0.22f).roundToInt()
        val cEnd = (w * 0.78f).roundToInt().coerceAtLeast(cStart + 1)
        val sEnd = (w * 0.54f).roundToInt().coerceAtLeast(1)
        val eStart = (w * 0.46f).roundToInt().coerceAtMost(w - 1)

        for (y in 0 until h) {
            val row = y * w
            var cCnt = 0
            var sCnt = 0
            var eCnt = 0
            for (x in 0 until w) {
                if (!mask[row + x]) continue
                fgCount++
                if (y < minY) minY = y
                if (y > maxY) maxY = y
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < topClearEnd) topClearFg++
                if (y in clockBandStart until clockBandEnd) {
                    clockBandFg++
                    clockBandMeanX += x
                }
                if (x in cStart until cEnd) cCnt++
                if (x < sEnd) sCnt++
                if (x >= eStart) eCnt++
            }
            rowTotalCenter[y] = cCnt.toFloat() / max(1, cEnd - cStart)
            rowTotalStart[y] = sCnt.toFloat() / max(1, sEnd)
            rowTotalEnd[y] = eCnt.toFloat() / max(1, w - eStart)
        }

        val coverage = fgCount.toFloat() / n.toFloat()
        if (fgCount < (n * 0.012f) || coverage > 0.88f || minY >= h) {
            return DepthGeometryEval(
                isEligible = false,
                category = DepthSubjectCategory.FLAT_CANVAS,
                qualityScore = 15,
                peakYFraction = 0.5f,
                peakXFraction = 0.5f,
                coverageFraction = coverage,
                recommendedClockTopFraction = 0.14f,
                recommendedAlign = ClockHorizontalAlign.CENTER,
                clockOcclusionFraction = 0f
            )
        }

        val peakCrownEnd = min(h, minY + max(4, (h * 0.05f).roundToInt()))
        for (y in minY until peakCrownEnd) {
            val row = y * w
            for (x in 0 until w) {
                if (mask[row + x]) {
                    topBandMeanX += x
                    topBandCount++
                }
            }
        }

        val peakYFrac = minY.toFloat() / h.toFloat()
        val peakXFrac = if (topBandCount > 0) (topBandMeanX / topBandCount) / w.toFloat() else 0.5f
        val bottomYFrac = maxY.toFloat() / h.toFloat()
        val widthFrac = (maxX - minX + 1).toFloat() / w.toFloat()
        val heightFrac = (maxY - minY + 1).toFloat() / h.toFloat()
        val topClearOcclusion = topClearFg.toFloat() / (topClearEnd * w).toFloat()
        val clockBandCoverage = clockBandFg.toFloat() / max(1, (clockBandEnd - clockBandStart) * w).toFloat()

        // True depth eligibility requires crisp open top sky and subject intrusion in the upper-mid zone
        val entersClockZone = peakYFrac in 0.03f..0.54f && clockBandCoverage >= 0.018f
        val hasOpenHeadroom = topClearOcclusion < 0.72f
        val isWellSeparated = entersClockZone && hasOpenHeadroom

        val isFloatingOrb = bottomYFrac < 0.84f &&
            peakYFrac > 0.05f &&
            widthFrac in 0.12f..0.72f &&
            abs(widthFrac - heightFrac) < 0.25f

        val category = when {
            !isWellSeparated -> DepthSubjectCategory.FLAT_CANVAS
            isFloatingOrb -> DepthSubjectCategory.CELESTIAL_ORB
            widthFrac > 0.78f && bottomYFrac > 0.90f -> DepthSubjectCategory.SUMMIT_SILHOUETTE
            widthFrac < 0.45f -> DepthSubjectCategory.BOTANICAL_ARCH
            else -> DepthSubjectCategory.PORTRAIT_FIGURE
        }

        val clockMeanX = if (clockBandFg > 0) (clockBandMeanX / clockBandFg) / w.toFloat() else peakXFrac
        val recommendedAlign = when {
            clockBandCoverage < 0.22f && clockMeanX > 0.66f -> ClockHorizontalAlign.START
            clockBandCoverage < 0.22f && clockMeanX < 0.34f -> ClockHorizontalAlign.END
            else -> ClockHorizontalAlign.CENTER
        }

        val activeRowOccupancy = when (recommendedAlign) {
            ClockHorizontalAlign.START -> rowTotalStart
            ClockHorizontalAlign.END -> rowTotalEnd
            ClockHorizontalAlign.CENTER -> rowTotalCenter
        }

        // Pinpoint Sliding-Window Lock-Clock Occlusion Solver:
        // Models a standard clock height of 25% of frame:
        // - Upper 62% of the clock window ([top, top + 0.155]) should remain clear sky (>= 75% visible).
        // - Lower 38% of the clock window ([top + 0.155, top + 0.250]) should cleanly tuck behind the subject (~22-38% occluded).
        val upperRows = max(2, (h * 0.155f).roundToInt())
        val lowerRows = max(2, (h * 0.095f).roundToInt())
        val totalWindowRows = upperRows + lowerRows
        val minStartRow = (h * 0.07f).roundToInt()
        val maxStartRow = min(h - totalWindowRows - 1, (h * 0.27f).roundToInt())

        var bestTopFrac = when (category) {
            DepthSubjectCategory.CELESTIAL_ORB -> (peakYFrac - 0.05f).coerceIn(0.08f, 0.26f)
            else -> (peakYFrac - 0.12f).coerceIn(0.07f, 0.26f)
        }
        var bestWindowScore = Float.NEGATIVE_INFINITY
        var bestLowerOcc = 0f

        if (maxStartRow > minStartRow) {
            val step = max(1, h / 160)
            var r = minStartRow
            while (r <= maxStartRow) {
                var upperFg = 0f
                for (y in r until (r + upperRows)) {
                    upperFg += activeRowOccupancy[y]
                }
                val upperOcc = upperFg / upperRows

                var lowerFg = 0f
                for (y in (r + upperRows) until (r + totalWindowRows)) {
                    lowerFg += activeRowOccupancy[y]
                }
                val lowerOcc = lowerFg / lowerRows

                // Target ~0.26 occlusion behind lower numerals while heavily penalizing covered upper numerals
                val upperPenalty = if (upperOcc <= 0.14f) upperOcc * 0.8f else 0.112f + (upperOcc - 0.14f) * 3.8f
                val targetLower = 0.28f
                val lowerFit = 1f - abs(lowerOcc - targetLower) * 2.2f
                val occlusionBonus = if (lowerOcc >= 0.06f && lowerOcc <= 0.65f) 0.45f else -0.40f
                val score = lowerFit + occlusionBonus - upperPenalty

                if (score > bestWindowScore) {
                    bestWindowScore = score
                    bestTopFrac = (r.toFloat() / h.toFloat()).coerceIn(0.07f, 0.27f)
                    bestLowerOcc = lowerOcc
                }
                r += step
            }
        }

        // Score edge sharpness & separation confidence 0..100
        var fgSalSum = 0f
        var bgSalSum = 0f
        var bgCnt = 0
        for (i in 0 until n) {
            if (mask[i]) fgSalSum += saliency[i]
            else {
                bgSalSum += saliency[i]
                bgCnt++
            }
        }
        val fgMeanSal = fgSalSum / max(1, fgCount)
        val bgMeanSal = if (bgCnt > 0) bgSalSum / bgCnt else 0f
        val separationGap = (fgMeanSal - bgMeanSal).coerceIn(0f, 1f)
        val headroomBonus = (1f - topClearOcclusion).coerceIn(0f, 1f) * 20f
        val quality = ((separationGap * 82f) + headroomBonus).roundToInt().coerceIn(20, 99)

        // Joint Wallpaper + Clock Framing Recommendation for Auto Snap:
        // Ensures ideal upper sky headroom + lower-numeral foreground overlap (~22%-34%).
        var recZoom = 1.0f
        var recPanYFrac = 0f
        var recPanXFrac = 0f
        var recClockScale = 1.0f

        when {
            peakYFrac > 0.34f -> {
                // Subject peak sits fairly low: zoom gently and pan image upward so silhouette rises into clock base
                val liftNeed = ((peakYFrac - 0.25f) / 0.25f).coerceIn(0f, 1f)
                recZoom = (1.10f + liftNeed * 0.18f).coerceIn(1.0f, 1.32f)
                recPanYFrac = (-0.08f - liftNeed * 0.14f).coerceIn(-0.24f, 0f)
                recClockScale = 1.04f
            }
            peakYFrac < 0.14f -> {
                // Tall subject crowds top sky: zoom slightly and push wallpaper down to carve crisp clock headroom
                val headroomNeed = ((0.16f - peakYFrac) / 0.14f).coerceIn(0f, 1f)
                recZoom = (1.10f + headroomNeed * 0.14f).coerceIn(1.0f, 1.28f)
                recPanYFrac = (0.09f + headroomNeed * 0.12f).coerceIn(0f, 0.22f)
                recClockScale = 0.96f
            }
            else -> {
                // Sweet-spot headroom: crisp slight framing zoom if subject crown sits off-center horizontally
                if (abs(peakXFrac - 0.5f) > 0.12f && widthFrac < 0.65f) {
                    recZoom = 1.08f
                    recPanXFrac = ((0.5f - peakXFrac) * 0.22f).coerceIn(-0.12f, 0.12f)
                }
            }
        }

        // Re-evaluate effective clock top fraction in the framed viewport so Auto Snap lands cleanly
        val effectivePeakY = (peakYFrac - (recPanYFrac * 0.35f)).coerceIn(0.10f, 0.45f)
        val framedTopFrac = if (recZoom > 1.02f) {
            when (category) {
                DepthSubjectCategory.CELESTIAL_ORB -> (effectivePeakY - 0.06f).coerceIn(0.08f, 0.25f)
                else -> (effectivePeakY - 0.13f).coerceIn(0.08f, 0.26f)
            }
        } else {
            bestTopFrac
        }

        return DepthGeometryEval(
            isEligible = isWellSeparated && quality >= 42,
            category = category,
            qualityScore = quality,
            peakYFraction = peakYFrac,
            peakXFraction = peakXFrac,
            coverageFraction = coverage,
            recommendedClockTopFraction = framedTopFrac,
            recommendedAlign = recommendedAlign,
            clockOcclusionFraction = bestLowerOcc.coerceIn(0f, 1f),
            recommendedWallpaperZoom = recZoom,
            recommendedWallpaperPanXFraction = recPanXFrac,
            recommendedWallpaperPanYFraction = recPanYFrac,
            recommendedClockScale = recClockScale
        )
    }

    private fun buildSoftCoarseAlpha(
        mask: BooleanArray,
        saliency: FloatArray,
        tau: Float,
        w: Int,
        h: Int
    ): FloatArray {
        val n = w * h
        val alpha = FloatArray(n)
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                val i = row + x
                if (mask[i]) {
                    // Check if interior (all 4-neighbors in mask) vs boundary pixel
                    val isInterior = (x > 0 && mask[i - 1]) &&
                        (x < w - 1 && mask[i + 1]) &&
                        (y > 0 && mask[i - w]) &&
                        (y < h - 1 && mask[i + w])
                    if (isInterior) {
                        alpha[i] = 1f
                    } else {
                        val soft = ((saliency[i] - tau * 0.85f) / max(0.08f, tau * 0.30f)).coerceIn(0.65f, 1f)
                        alpha[i] = soft
                    }
                } else {
                    // Check if 4-adjacent to foreground to give smooth feathering across the coarse grid
                    val nearFg = (x > 0 && mask[i - 1]) ||
                        (x < w - 1 && mask[i + 1]) ||
                        (y > 0 && mask[i - w]) ||
                        (y < h - 1 && mask[i + w])
                    alpha[i] = if (nearFg) {
                        ((saliency[i] - tau * 0.70f) / max(0.08f, tau * 0.45f)).coerceIn(0f, 0.35f)
                    } else 0f
                }
            }
        }
        return alpha
    }

    /**
     * Full-resolution CIE L*a*b* Joint Color-Guided Alpha Upsampler.
     * - Copies pure interior foreground pixels directly (zero compute, bit-exact RGB).
     * - Skips pure exterior sky pixels immediately.
     * - Snaps the contour ribbon using spatial bilinear weights modulated by perceptual L*a*b* deltaE affinity,
     *   eliminating edge halos even at iso-luminant color boundaries (e.g. sunset orange vs slate blue).
     */
    private fun upsampleCutoutLabGuided(
        fullBitmap: Bitmap,
        coarseAlpha: FloatArray,
        coarseL: FloatArray,
        coarseA: FloatArray,
        coarseB: FloatArray,
        cw: Int,
        ch: Int
    ): Bitmap {
        val fw = fullBitmap.width
        val fh = fullBitmap.height
        val fullCount = fw * fh
        val srcPixels = IntArray(fullCount)
        fullBitmap.getPixels(srcPixels, 0, fw, 0, 0, fw, fh)

        val outPixels = IntArray(fullCount)
        val scaleX = cw.toFloat() / fw.toFloat()
        val scaleY = ch.toFloat() / fh.toFloat()
        val invSigmaLabSq = 1f / (11f * 11f)
        val labBuf = FloatArray(3)

        val mapX0 = IntArray(fw)
        val mapX1 = IntArray(fw)
        val mapFx = FloatArray(fw)
        for (x in 0 until fw) {
            val gx = ((x + 0.5f) * scaleX - 0.5f).coerceIn(0f, (cw - 1).toFloat())
            val x0 = floor(gx).toInt()
            mapX0[x] = x0
            mapX1[x] = min(x0 + 1, cw - 1)
            mapFx[x] = gx - x0
        }

        for (y in 0 until fh) {
            val gy = ((y + 0.5f) * scaleY - 0.5f).coerceIn(0f, (ch - 1).toFloat())
            val y0 = floor(gy).toInt()
            val y1 = min(y0 + 1, ch - 1)
            val fy = gy - y0
            val oneMinusFy = 1f - fy

            val cRow0 = y0 * cw
            val cRow1 = y1 * cw
            val fRow = y * fw

            for (x in 0 until fw) {
                val x0 = mapX0[x]
                val x1 = mapX1[x]
                val i00 = cRow0 + x0
                val i10 = cRow0 + x1
                val i01 = cRow1 + x0
                val i11 = cRow1 + x1

                val a00 = coarseAlpha[i00]
                val a10 = coarseAlpha[i10]
                val a01 = coarseAlpha[i01]
                val a11 = coarseAlpha[i11]

                val fIdx = fRow + x
                if (a00 >= 0.99f && a10 >= 0.99f && a01 >= 0.99f && a11 >= 0.99f) {
                    outPixels[fIdx] = srcPixels[fIdx]
                    continue
                }
                if (a00 <= 0.01f && a10 <= 0.01f && a01 <= 0.01f && a11 <= 0.01f) {
                    outPixels[fIdx] = 0
                    continue
                }

                val srcArgb = srcPixels[fIdx]
                LabColorSpace.argbToLab(srcArgb, labBuf)
                val fL = labBuf[0]
                val fA = labBuf[1]
                val fB = labBuf[2]

                val fx = mapFx[x]
                val oneMinusFx = 1f - fx

                fun labWeight(idx: Int, spatialWeight: Float): Float {
                    val dl = fL - coarseL[idx]
                    val da = fA - coarseA[idx]
                    val db = fB - coarseB[idx]
                    val de2 = dl * dl + 0.72f * (da * da + db * db)
                    return spatialWeight / (1f + de2 * invSigmaLabSq)
                }

                val w00 = labWeight(i00, oneMinusFx * oneMinusFy)
                val w10 = labWeight(i10, fx * oneMinusFy)
                val w01 = labWeight(i01, oneMinusFx * fy)
                val w11 = labWeight(i11, fx * fy)

                val wSum = w00 + w10 + w01 + w11
                val rawAlpha = if (wSum > 1e-6f) {
                    (w00 * a00 + w10 * a10 + w01 * a01 + w11 * a11) / wSum
                } else {
                    0f
                }

                val t = ((rawAlpha - 0.16f) / 0.68f).coerceIn(0f, 1f)
                val smoothAlpha = t * t * (3f - 2f * t)
                val alphaByte = (smoothAlpha * 255f).roundToInt().coerceIn(0, 255)
                outPixels[fIdx] = if (alphaByte == 0) {
                    0
                } else {
                    (alphaByte shl 24) or (srcArgb and 0x00FFFFFF)
                }
            }
        }

        val cutout = createBitmap(fw, fh)
        cutout.setPixels(outPixels, 0, fw, 0, 0, fw, fh)
        return cutout
    }

    private fun sampleClockRegionContrastHex(
        smL: FloatArray,
        smA: FloatArray,
        smB: FloatArray,
        mask: BooleanArray,
        cw: Int,
        ch: Int,
        clockTopFrac: Float,
        fallbackStats: SkyModel
    ): String {
        val yStart = (clockTopFrac * ch).roundToInt().coerceIn(0, ch - 1)
        val yEnd = ((clockTopFrac + 0.18f) * ch).roundToInt().coerceIn(yStart + 1, ch)
        var sL = 0f
        var sA = 0f
        var sB = 0f
        var cnt = 0
        for (y in yStart until yEnd) {
            val row = y * cw
            for (x in (cw / 5) until (4 * cw / 5)) {
                val idx = row + x
                if (!mask[idx]) {
                    sL += smL[idx]
                    sA += smA[idx]
                    sB += smB[idx]
                    cnt++
                }
            }
        }
        return if (cnt > 0) {
            LabColorSpace.pickNaturalHarmonicClockHex(sL / cnt, sA / cnt, sB / cnt)
        } else {
            LabColorSpace.pickNaturalHarmonicClockHex(fallbackStats.meanL, fallbackStats.meanA, fallbackStats.meanB)
        }
    }

    private fun inferSubjectLabel(category: DepthSubjectCategory, @Suppress("UNUSED_PARAMETER") titleHint: String): String {
        return category.displayName
    }

    private fun flatResult(
        titleHint: String,
        processingTimeMs: Long,
        skyL: Float,
        skyA: Float,
        skyB: Float
    ): DepthDetectionResult = DepthDetectionResult(
        subjectLabel = titleHint,
        subjectCategory = DepthSubjectCategory.FLAT_CANVAS,
        isDepthEligible = false,
        depthQualityScore = 15,
        cutoutBitmap = null,
        recommendedClockTopFraction = 0.14f,
        recommendedAlign = ClockHorizontalAlign.CENTER,
        recommendedClockColorHex = LabColorSpace.pickNaturalHarmonicClockHex(skyL, skyA, skyB),
        subjectTopFraction = 0.5f,
        subjectPeakXFraction = 0.5f,
        foregroundCoverageFraction = 0f,
        processingTimeMs = processingTimeMs
    )

    private fun ensureSoftwareArgb8888(bitmap: Bitmap): Bitmap {
        return if (bitmap.config == Bitmap.Config.ARGB_8888) {
            bitmap
        } else {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        }
    }

    companion object {
        private const val ANALYSIS_WIDTH = 360
        private const val SMOOTH_SKY_DEADZONE = 1.85f
        private const val MIN_BARRIER_LAB = 6.5f
        private const val MIN_SEPARATION_CONTRAST = 0.20f
    }
}
