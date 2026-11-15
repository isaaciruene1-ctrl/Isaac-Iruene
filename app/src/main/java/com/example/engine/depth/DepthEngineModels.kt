package com.example.engine.depth

import android.graphics.Bitmap

enum class ClockHorizontalAlign {
    START,
    CENTER,
    END
}

enum class DepthSubjectCategory(val displayName: String) {
    CELESTIAL_ORB("Celestial Subject"),
    SUMMIT_SILHOUETTE("Peak & Horizon"),
    PORTRAIT_FIGURE("Foreground Subject"),
    BOTANICAL_ARCH("Botanical & Arch"),
    FLAT_CANVAS("Flat Backdrop")
}

data class DepthDetectionResult(
    val subjectLabel: String,
    val subjectCategory: DepthSubjectCategory,
    val isDepthEligible: Boolean,
    val depthQualityScore: Int,
    val cutoutBitmap: Bitmap?,
    val recommendedClockTopFraction: Float,
    val recommendedAlign: ClockHorizontalAlign,
    val recommendedClockColorHex: String,
    val subjectTopFraction: Float,
    val subjectPeakXFraction: Float,
    val foregroundCoverageFraction: Float,
    val processingTimeMs: Long
)
