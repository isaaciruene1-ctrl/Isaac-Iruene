package com.example.data.catalog

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.LruCache
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperCategory
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Deterministic high-resolution procedural renderer for curated Stock, Superhero,
 * AMOLED, Abstract, and fallback exhibition wallpapers. Produces distinct layered artwork
 * tuned per category & palette without external assets or copyright issues.
 */
object ProceduralWallpaperRenderer {

    private val previewCache = LruCache<String, Bitmap>(36)

    fun isProceduralUrl(url: String): Boolean = url.startsWith("procedural://")

    fun getOrRenderPreview(
        wallpaper: Wallpaper,
        width: Int = 540,
        height: Int = 960
    ): Bitmap {
        val key = "${wallpaper.id}_${width}x$height"
        previewCache.get(key)?.let { if (!it.isRecycled) return it }
        val bmp = renderWallpaper(wallpaper, width, height)
        previewCache.put(key, bmp)
        return bmp
    }

    fun renderWallpaper(
        wallpaper: Wallpaper,
        width: Int = 1080,
        height: Int = 1920
    ): Bitmap {
        val w = width.coerceIn(180, 2160)
        val h = height.coerceIn(180, 3840)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val seed = wallpaper.id.hashCode().toLong() and 0xFFFFFFFFL
        val palette = wallpaper.dominantColors.map { it.toInt() }.ifEmpty {
            listOf(0xFF1F2235.toInt(), 0xFF6B57A8.toInt(), 0xFFD9845B.toInt())
        }
        val c0 = palette[0]
        val c1 = palette.getOrElse(1) { c0 }
        val c2 = palette.getOrElse(2) { c1 }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val fw = w.toFloat()
        val fh = h.toFloat()

        // Base sky / studio gradient
        val isAmoled = wallpaper.category == WallpaperCategory.AMOLED || wallpaper.category == WallpaperCategory.SPACE
        val angleBias = ((seed % 100L) / 100f)
        paint.shader = LinearGradient(
            fw * (0.1f + 0.5f * angleBias),
            0f,
            fw * (0.9f - 0.5f * angleBias),
            fh,
            intArrayOf(
                if (isAmoled) 0xFF000000.toInt() else c0,
                c1,
                if (isAmoled) 0xFF06060C.toInt() else c2
            ),
            floatArrayOf(0f, 0.54f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, fw, fh, paint)
        paint.shader = null

        when (wallpaper.category) {
            WallpaperCategory.SUPERHERO -> drawSuperheroMotif(canvas, fw, fh, c0, c1, c2, seed)
            WallpaperCategory.STOCK -> drawStockOsMotif(canvas, fw, fh, c0, c1, c2, seed)
            WallpaperCategory.AMOLED -> drawAmoledRibbons(canvas, fw, fh, c1, c2, seed)
            WallpaperCategory.ARCHITECTURE -> drawArchitectureMonument(canvas, fw, fh, c0, c1, c2, seed)
            WallpaperCategory.SPACE -> drawSpaceOrbital(canvas, fw, fh, c1, c2, seed)
            WallpaperCategory.NATURE -> drawAlpineHorizons(canvas, fw, fh, c0, c1, c2, seed)
            WallpaperCategory.FUTURISTIC, WallpaperCategory.AUTOMOTIVE -> drawKineticBeams(canvas, fw, fh, c1, c2, seed)
            WallpaperCategory.MINIMALIST -> drawMinimalistMonolith(canvas, fw, fh, c1, c2, seed)
            else -> drawSculpturalGlass(canvas, fw, fh, c1, c2, seed)
        }

        return bitmap
    }

    private fun drawStockOsMotif(
        canvas: Canvas,
        fw: Float,
        fh: Float,
        c0: Int,
        c1: Int,
        c2: Int,
        seed: Long
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = fw * (0.35f + ((seed % 30) / 100f))
        val cy = fh * (0.48f + (((seed / 7) % 18) / 100f))

        // Luminous OS bloom
        paint.shader = RadialGradient(
            cx,
            cy,
            min(fw, fh) * 0.65f,
            intArrayOf(withAlpha(c2, 210), withAlpha(c1, 110), 0x00000000),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, min(fw, fh) * 0.65f, paint)
        paint.shader = null

        // Flowing translucent fold ribbons inspired by modern flagship stock drops
        val foldPath = Path().apply {
            moveTo(-fw * 0.1f, fh * 0.72f)
            cubicTo(
                fw * 0.25f, fh * (0.46f + (seed % 11) * 0.01f),
                fw * 0.68f, fh * (0.82f - (seed % 9) * 0.01f),
                fw * 1.15f, fh * 0.54f
            )
            lineTo(fw * 1.15f, fh * 1.05f)
            lineTo(-fw * 0.1f, fh * 1.05f)
            close()
        }
        paint.shader = LinearGradient(
            0f, fh * 0.5f, fw, fh,
            withAlpha(c1, 225), withAlpha(c0, 240), Shader.TileMode.CLAMP
        )
        canvas.drawPath(foldPath, paint)
        paint.shader = null

        // Crisp concentric geometric glass disc
        val discR = min(fw, fh) * 0.27f
        paint.color = withAlpha(0xFFFFFFFF.toInt(), 38)
        canvas.drawCircle(fw * 0.5f, fh * 0.58f, discR, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = fw * 0.008f
        paint.color = withAlpha(0xFFFFFFFF.toInt(), 95)
        canvas.drawCircle(fw * 0.5f, fh * 0.58f, discR, paint)
    }

    private fun drawSuperheroMotif(
        canvas: Canvas,
        fw: Float,
        fh: Float,
        c0: Int,
        c1: Int,
        c2: Int,
        seed: Long
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = fw * 0.5f
        val cy = fh * 0.57f
        val radius = min(fw, fh) * 0.31f

        // Comic halftone atmospheric aura
        paint.shader = RadialGradient(
            cx,
            cy,
            radius * 2.1f,
            intArrayOf(withAlpha(c2, 185), withAlpha(c1, 80), 0x00000000),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius * 2.1f, paint)
        paint.shader = null

        // Dynamic speed lines radiating outward
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = fw * 0.006f
        paint.color = withAlpha(0xFFFFFFFF.toInt(), 42)
        for (i in 0 until 18) {
            val angle = (i * 20.0 + (seed % 15)) * PI / 180.0
            val rIn = radius * 1.15f
            val rOut = min(fw, fh) * 0.9f
            canvas.drawLine(
                cx + (cos(angle) * rIn).toFloat(),
                cy + (sin(angle) * rIn).toFloat(),
                cx + (cos(angle) * rOut).toFloat(),
                cy + (sin(angle) * rOut).toFloat(),
                paint
            )
        }

        // Emblem crest (shield / star / lightning / core) determined by seed % 4
        paint.style = Paint.Style.FILL
        when ((seed % 4L).toInt()) {
            0 -> {
                // Armored Star-Shield rings
                paint.color = withAlpha(c1, 240)
                canvas.drawCircle(cx, cy, radius, paint)
                paint.color = withAlpha(0xFFF6F7FB.toInt(), 230)
                canvas.drawCircle(cx, cy, radius * 0.76f, paint)
                paint.color = withAlpha(c0, 245)
                canvas.drawCircle(cx, cy, radius * 0.52f, paint)
                drawStarPath(canvas, cx, cy, radius * 0.42f, radius * 0.17f, 5, paint, 0xFFFFFFFF.toInt())
            }
            1 -> {
                // Kinetic Lightning / Crest Emblem
                paint.color = withAlpha(c0, 230)
                val shield = Path().apply {
                    moveTo(cx, cy - radius * 0.92f)
                    lineTo(cx + radius * 0.84f, cy - radius * 0.42f)
                    lineTo(cx + radius * 0.62f, cy + radius * 0.52f)
                    lineTo(cx, cy + radius * 0.95f)
                    lineTo(cx - radius * 0.62f, cy + radius * 0.52f)
                    lineTo(cx - radius * 0.84f, cy - radius * 0.42f)
                    close()
                }
                canvas.drawPath(shield, paint)
                paint.color = withAlpha(c2, 248)
                val bolt = Path().apply {
                    moveTo(cx + radius * 0.14f, cy - radius * 0.65f)
                    lineTo(cx - radius * 0.28f, cy + radius * 0.04f)
                    lineTo(cx + radius * 0.04f, cy + radius * 0.04f)
                    lineTo(cx - radius * 0.16f, cy + radius * 0.68f)
                    lineTo(cx + radius * 0.30f, cy - radius * 0.06f)
                    lineTo(cx - radius * 0.02f, cy - radius * 0.06f)
                    close()
                }
                canvas.drawPath(bolt, paint)
            }
            2 -> {
                // Reactor Hexagon Core
                paint.color = withAlpha(c0, 235)
                drawPolygon(canvas, cx, cy, radius * 0.95f, 6, paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = fw * 0.018f
                paint.color = withAlpha(c2, 245)
                drawPolygon(canvas, cx, cy, radius * 0.70f, 6, paint)
                paint.style = Paint.Style.FILL
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawCircle(cx, cy, radius * 0.28f, paint)
            }
            else -> {
                // Winged Vanguard Chevron
                paint.color = withAlpha(c1, 240)
                val chevron = Path().apply {
                    moveTo(cx, cy - radius * 0.88f)
                    lineTo(cx + radius * 0.95f, cy + radius * 0.56f)
                    lineTo(cx + radius * 0.42f, cy + radius * 0.56f)
                    lineTo(cx, cy - radius * 0.14f)
                    lineTo(cx - radius * 0.42f, cy + radius * 0.56f)
                    lineTo(cx - radius * 0.95f, cy + radius * 0.56f)
                    close()
                }
                canvas.drawPath(chevron, paint)
                paint.color = withAlpha(c2, 240)
                canvas.drawCircle(cx, cy + radius * 0.34f, radius * 0.18f, paint)
            }
        }
    }

    private fun drawAmoledRibbons(
        canvas: Canvas,
        fw: Float,
        fh: Float,
        c1: Int,
        c2: Int,
        seed: Long
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        for (i in 0 until 5) {
            val offset = (seed % 7) * 0.03f + i * 0.045f
            paint.strokeWidth = fw * (0.045f - i * 0.006f)
            paint.color = withAlpha(if (i % 2 == 0) c1 else c2, 185 - i * 24)
            val path = Path().apply {
                moveTo(-fw * 0.1f, fh * (0.48f + offset))
                cubicTo(fw * 0.32f, fh * (0.34f + offset), fw * 0.68f, fh * (0.72f - offset), fw * 1.1f, fh * (0.56f + offset))
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun drawArchitectureMonument(
        canvas: Canvas,
        fw: Float,
        fh: Float,
        c0: Int,
        c1: Int,
        c2: Int,
        seed: Long
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // Sunlit portal arch centered cleanly in lower 60% for crisp depth occlusion
        val archW = fw * 0.64f
        val archL = (fw - archW) * 0.5f
        val archT = fh * 0.38f
        paint.color = withAlpha(c2, 190)
        canvas.drawRoundRect(RectF(archL, archT, archL + archW, fh * 1.02f), archW * 0.5f, archW * 0.5f, paint)

        paint.color = withAlpha(c0, 235)
        val innerPad = fw * 0.08f
        canvas.drawRoundRect(
            RectF(archL + innerPad, archT + innerPad, archL + archW - innerPad, fh * 1.02f),
            (archW - innerPad * 2f) * 0.5f,
            (archW - innerPad * 2f) * 0.5f,
            paint
        )
        // Sun disc inside portal
        paint.color = withAlpha(c1, 210)
        canvas.drawCircle(fw * 0.5f, fh * (0.52f + (seed % 5) * 0.02f), fw * 0.14f, paint)
    }

    private fun drawSpaceOrbital(
        canvas: Canvas,
        fw: Float,
        fh: Float,
        c1: Int,
        c2: Int,
        seed: Long
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // Starfield
        paint.color = withAlpha(0xFFFFFFFF.toInt(), 170)
        for (i in 0 until 45) {
            val sx = (((seed * 31 + i * 997) % 1000) / 1000f) * fw
            val sy = (((seed * 17 + i * 613) % 1000) / 1000f) * fh * 0.72f
            canvas.drawCircle(sx, sy, (i % 3 + 1) * 1.4f, paint)
        }
        // Eclipse planet foreground silhouette
        val cx = fw * 0.5f
        val cy = fh * 0.58f
        val r = min(fw, fh) * 0.28f
        paint.shader = RadialGradient(
            cx, cy, r * 1.45f,
            intArrayOf(withAlpha(c2, 230), withAlpha(c1, 90), 0x00000000),
            floatArrayOf(0.55f, 0.78f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, r * 1.45f, paint)
        paint.shader = null
        paint.color = 0xFF090A12.toInt()
        canvas.drawCircle(cx, cy, r, paint)
    }

    private fun drawAlpineHorizons(
        canvas: Canvas,
        fw: Float,
        fh: Float,
        c0: Int,
        c1: Int,
        c2: Int,
        seed: Long
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // Glowing moon/sun
        paint.color = withAlpha(0xFFFFF4DC.toInt(), 210)
        canvas.drawCircle(fw * (0.3f + (seed % 5) * 0.1f), fh * 0.34f, fw * 0.12f, paint)

        // Layered mountain peaks (clear top band for clock, jagged foreground silhouette)
        val backRidge = Path().apply {
            moveTo(0f, fh * 0.66f)
            lineTo(fw * 0.28f, fh * 0.50f)
            lineTo(fw * 0.56f, fh * 0.60f)
            lineTo(fw * 0.82f, fh * 0.48f)
            lineTo(fw, fh * 0.62f)
            lineTo(fw, fh)
            lineTo(0f, fh)
            close()
        }
        paint.color = withAlpha(c1, 215)
        canvas.drawPath(backRidge, paint)

        val frontPeak = Path().apply {
            moveTo(0f, fh * 0.78f)
            lineTo(fw * 0.42f, fh * 0.56f)
            lineTo(fw * 0.74f, fh * 0.72f)
            lineTo(fw, fh * 0.64f)
            lineTo(fw, fh)
            lineTo(0f, fh)
            close()
        }
        paint.color = withAlpha(c0, 245)
        canvas.drawPath(frontPeak, paint)
    }

    private fun drawKineticBeams(
        canvas: Canvas,
        fw: Float,
        fh: Float,
        c1: Int,
        c2: Int,
        seed: Long
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (i in 0 until 4) {
            paint.shader = LinearGradient(
                0f, fh * (0.35f + i * 0.12f),
                fw, fh * (0.55f + i * 0.1f),
                withAlpha(if (i % 2 == 0) c1 else c2, 185),
                withAlpha(0xFFFFFFFF.toInt(), 25),
                Shader.TileMode.CLAMP
            )
            val path = Path().apply {
                val yBase = fh * (0.45f + i * 0.11f + (seed % 4) * 0.02f)
                moveTo(-fw * 0.1f, yBase)
                lineTo(fw * 1.1f, yBase - fh * 0.16f)
                lineTo(fw * 1.1f, yBase - fh * 0.06f)
                lineTo(-fw * 0.1f, yBase + fh * 0.10f)
                close()
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun drawMinimalistMonolith(
        canvas: Canvas,
        fw: Float,
        fh: Float,
        c1: Int,
        c2: Int,
        seed: Long
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = fw * 0.5f
        val cy = fh * 0.60f
        paint.shader = LinearGradient(
            cx - fw * 0.2f, cy - fh * 0.25f,
            cx + fw * 0.2f, cy + fh * 0.25f,
            withAlpha(c2, 230), withAlpha(c1, 200), Shader.TileMode.CLAMP
        )
        val wRect = fw * (0.44f + (seed % 3) * 0.05f)
        canvas.drawRoundRect(
            RectF(cx - wRect * 0.5f, cy - fh * 0.22f, cx + wRect * 0.5f, fh * 1.02f),
            fw * 0.08f,
            fw * 0.08f,
            paint
        )
    }

    private fun drawSculpturalGlass(
        canvas: Canvas,
        fw: Float,
        fh: Float,
        c1: Int,
        c2: Int,
        seed: Long
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (i in 0 until 3) {
            val cx = fw * (0.28f + i * 0.24f + ((seed % 5) * 0.02f))
            val cy = fh * (0.48f + i * 0.10f)
            val r = min(fw, fh) * (0.28f - i * 0.04f)
            paint.shader = RadialGradient(
                cx - r * 0.25f, cy - r * 0.25f, r * 1.3f,
                intArrayOf(withAlpha(0xFFFFFFFF.toInt(), 95), withAlpha(if (i % 2 == 0) c2 else c1, 210), withAlpha(c1, 80)),
                floatArrayOf(0f, 0.58f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, r, paint)
        }
    }

    private fun drawStarPath(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        outerR: Float,
        innerR: Float,
        points: Int,
        paint: Paint,
        color: Int
    ) {
        paint.color = color
        val path = Path()
        val step = PI / points
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = i * step - PI / 2.0
            val x = cx + (cos(angle) * r).toFloat()
            val y = cy + (sin(angle) * r).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawPolygon(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        r: Float,
        sides: Int,
        paint: Paint
    ) {
        val path = Path()
        for (i in 0 until sides) {
            val angle = i * (2.0 * PI / sides) - PI / 2.0
            val x = cx + (cos(angle) * r).toFloat()
            val y = cy + (sin(angle) * r).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun withAlpha(argb: Int, alpha: Int): Int {
        return (alpha.coerceIn(0, 255) shl 24) or (argb and 0x00FFFFFF)
    }
}
