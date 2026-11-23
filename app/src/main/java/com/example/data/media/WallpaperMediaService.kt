package com.example.data.media

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.model.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

enum class WallpaperTarget {
    HOME, LOCK, BOTH
}

sealed interface DownloadProgress {
    data object Starting : DownloadProgress
    data class InProgress(val fraction: Float, val bytesRead: Long, val totalBytes: Long) : DownloadProgress
    data class Completed(val uri: Uri, val fileName: String, val isDuplicateHandled: Boolean) : DownloadProgress
    data class Failed(val message: String) : DownloadProgress
}

object BackdropImageLoader {
    @Volatile
    private var instance: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        return instance ?: synchronized(this) {
            instance ?: ImageLoader.Builder(context.applicationContext)
                .memoryCache {
                    MemoryCache.Builder(context.applicationContext)
                        .maxSizePercent(0.20)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(File(context.applicationContext.cacheDir, "backdrop_image_cache"))
                        .maxSizeBytes(150L * 1024L * 1024L)
                        .build()
                }
                .crossfade(true)
                .build()
                .also { instance = it }
        }
    }

    fun getCacheSizeBytes(context: Context): Long {
        val dir = File(context.applicationContext.cacheDir, "backdrop_image_cache")
        if (!dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    suspend fun clearCache(context: Context) = withContext(Dispatchers.IO) {
        val loader = get(context)
        loader.memoryCache?.clear()
        loader.diskCache?.clear()
    }
}

class WallpaperMediaService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    fun downloadWallpaper(wallpaper: Wallpaper): Flow<DownloadProgress> = flow {
        emit(DownloadProgress.Starting)
        val sanitizedTitle = wallpaper.title
            .replace(Regex("[^a-zA-Z0-9_\\- ]"), "")
            .trim()
            .replace(" ", "_")
            .lowercase()
            .ifEmpty { wallpaper.id }
        val baseName = "Backdrops_${sanitizedTitle}_${wallpaper.id}"

        val bytes = if (wallpaper.isBundledAsset) {
            decodeDrawableToJpeg(wallpaper.fallbackResId)
        } else if (com.example.data.catalog.ProceduralWallpaperRenderer.isProceduralUrl(wallpaper.fullResUrl)) {
            val bmp = com.example.data.catalog.ProceduralWallpaperRenderer.renderWallpaper(
                wallpaper = wallpaper,
                width = wallpaper.width,
                height = wallpaper.height
            )
            emit(DownloadProgress.InProgress(1f, 1L, 1L))
            val out = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 95, out)
            out.toByteArray()
        } else {
            val cachedBytes = runCatching {
                BackdropImageLoader.get(context).diskCache?.openSnapshot(wallpaper.fullResUrl)?.use { snap ->
                    snap.data.toFile().takeIf { it.exists() && it.length() > 1024L }?.readBytes()
                }
            }.getOrNull()

            if (cachedBytes != null && cachedBytes.isNotEmpty()) {
                emit(DownloadProgress.InProgress(1f, cachedBytes.size.toLong(), cachedBytes.size.toLong()))
                cachedBytes
            } else {
                try {
                    fetchBytesResumableWithProgress(wallpaper.id, wallpaper.fullResUrl) { progress, read, total ->
                        emit(DownloadProgress.InProgress(progress, read, total))
                    }
                } catch (_: Exception) {
                    if (wallpaper.id.startsWith("exp_")) {
                        val bmp = com.example.data.catalog.ProceduralWallpaperRenderer.renderWallpaper(
                            wallpaper, wallpaper.width, wallpaper.height
                        )
                        val out = ByteArrayOutputStream()
                        bmp.compress(Bitmap.CompressFormat.JPEG, 94, out)
                        out.toByteArray()
                    } else {
                        decodeDrawableToJpeg(wallpaper.fallbackResId)
                    }
                }
            }
        }

        if (bytes.isEmpty()) {
            emit(DownloadProgress.Failed("Unable to retrieve wallpaper image bytes."))
            return@flow
        }

        val (savedUri, finalName, renamed) = saveToPictures(bytes, baseName)
        if (savedUri != null) {
            emit(DownloadProgress.Completed(savedUri, finalName, renamed))
        } else {
            emit(DownloadProgress.Failed("Could not write image to Pictures/Backdrops."))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun saveBitmapToPictures(bitmap: Bitmap, titlePrefix: String): Result<Uri> =
        withContext(Dispatchers.IO) {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                val (uri, _, _) = saveToPictures(stream.toByteArray(), titlePrefix)
                if (uri != null) Result.success(uri)
                else Result.failure(IllegalStateException("MediaStore failed to store image."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun applyWallpaper(
        wallpaper: Wallpaper,
        target: WallpaperTarget,
        allowHomeParallax: Boolean = false
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadFullBitmap(wallpaper)
                ?: return@withContext Result.failure(IllegalStateException("Unable to decode wallpaper bitmap."))
            applyBitmapAsWallpaper(bitmap, target, allowHomeParallax)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyBitmapAsWallpaper(
        bitmap: Bitmap,
        target: WallpaperTarget,
        allowHomeParallax: Boolean = false
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val manager = WallpaperManager.getInstance(context.applicationContext)
            if (!manager.isWallpaperSupported) {
                return@withContext Result.failure(IllegalStateException("Setting wallpapers is not supported on this device."))
            }
            if (!manager.isSetWallpaperAllowed) {
                return@withContext Result.failure(IllegalStateException("Device policy restricts changing the wallpaper."))
            }

            val screenAspect = com.example.ui.screens.resolveDevicePortraitAspect(context)
            val parallaxEnabled = allowHomeParallax && target != WallpaperTarget.LOCK
            val cropRect = com.example.ui.screens.computeCenteredViewportCropRect(
                sourceWidth = bitmap.width,
                sourceHeight = bitmap.height,
                zoomScale = 1f,
                panOffsetX = 0f,
                panOffsetY = 0f,
                viewportWidthPx = 0f,
                viewportHeightPx = 0f,
                targetScreenAspect = screenAspect,
                allowParallaxWidth = parallaxEnabled
            )
            val alignedBitmap = if (
                cropRect.left == 0 &&
                cropRect.top == 0 &&
                cropRect.width == bitmap.width &&
                cropRect.height == bitmap.height
            ) {
                bitmap
            } else {
                Bitmap.createBitmap(bitmap, cropRect.left, cropRect.top, cropRect.width, cropRect.height)
            }

            val safeBitmap = if (alignedBitmap.config == Bitmap.Config.ARGB_8888) {
                alignedBitmap
            } else {
                alignedBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: alignedBitmap
            }

            // Use an explicit centered Rect when exact 1:1 Lock/Home framing is desired so OS launchers
            // do not top-left align or add horizontal shift.
            val cropHint: android.graphics.Rect? = if (parallaxEnabled) {
                null
            } else {
                android.graphics.Rect(0, 0, safeBitmap.width, safeBitmap.height)
            }

            val flags = when (target) {
                WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }
            val jpegBytes = ByteArrayOutputStream().use { out ->
                safeBitmap.compress(Bitmap.CompressFormat.JPEG, 96, out)
                out.toByteArray()
            }
            val streamId = ByteArrayInputStream(jpegBytes).use { input ->
                manager.setStream(input, cropHint, true, flags)
            }
            if (streamId == 0) {
                val bmpId = manager.setBitmap(safeBitmap, cropHint, true, flags)
                if (bmpId == 0) {
                    return@withContext Result.failure(
                        IllegalStateException("System WallpaperManager rejected the wallpaper update.")
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun shareWallpaper(wallpaper: Wallpaper) {
        val bitmap = withContext(Dispatchers.IO) { loadFullBitmap(wallpaper) } ?: return
        shareBitmap(bitmap, "${wallpaper.title} — Backdrops")
    }

    suspend fun shareBitmap(bitmap: Bitmap, shareTitle: String) {
        val uri = withContext(Dispatchers.IO) {
            val sharedDir = File(context.cacheDir, "shared").apply { mkdirs() }
            val file = File(sharedDir, "backdrop_share_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "$shareTitle • YOUR SCREEN. YOUR CANVAS.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Wallpaper").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    suspend fun loadFullBitmap(wallpaper: Wallpaper): Bitmap? = withContext(Dispatchers.IO) {
        val opts = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inScaled = false
        }
        if (wallpaper.isBundledAsset) {
            return@withContext BitmapFactory.decodeResource(context.resources, wallpaper.fallbackResId, opts)
        }
        if (com.example.data.catalog.ProceduralWallpaperRenderer.isProceduralUrl(wallpaper.fullResUrl)) {
            return@withContext com.example.data.catalog.ProceduralWallpaperRenderer.renderWallpaper(
                wallpaper = wallpaper,
                width = wallpaper.width.coerceAtMost(1440),
                height = wallpaper.height.coerceAtMost(2560)
            )
        }
        // Check Coil disk cache first so already-displayed wallpapers load instantly
        try {
            BackdropImageLoader.get(context).diskCache?.openSnapshot(wallpaper.fullResUrl)?.use { snapshot ->
                val cached = BitmapFactory.decodeFile(snapshot.data.toFile().absolutePath, opts)
                if (cached != null) return@withContext cached
            }
        } catch (_: Exception) {
        }

        try {
            val request = Request.Builder().url(wallpaper.fullResUrl).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.byteStream()?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, opts)
                    }
                } else null
            }
        } catch (_: Exception) {
            null
        } ?: if (wallpaper.id.startsWith("exp_")) {
            com.example.data.catalog.ProceduralWallpaperRenderer.renderWallpaper(wallpaper, 1080, 1920)
        } else {
            BitmapFactory.decodeResource(context.resources, wallpaper.fallbackResId, opts)
        }
    }

    /**
     * Download Manager V2: supports HTTP 206 Partial Content resume via temporary `.part` files
     * so interrupted large 4K downloads continue cleanly without re-fetching downloaded bytes.
     */
    private suspend fun fetchBytesResumableWithProgress(
        wallpaperId: String,
        url: String,
        onProgress: suspend (Float, Long, Long) -> Unit
    ): ByteArray {
        val dlDir = File(context.cacheDir, "dl_cache").apply { mkdirs() }
        val safeKey = wallpaperId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val partFile = File(dlDir, "$safeKey.part")
        var existingBytes = if (partFile.exists()) partFile.length() else 0L

        val reqBuilder = Request.Builder().url(url)
        if (existingBytes > 0L) {
            reqBuilder.header("Range", "bytes=$existingBytes-")
        }

        client.newCall(reqBuilder.build()).execute().use { response ->
            if (response.code == 416 && existingBytes > 0L) {
                partFile.delete()
                return fetchBytesResumableWithProgress(wallpaperId, url, onProgress)
            }
            if (!response.isSuccessful && response.code != 206) {
                error("HTTP ${response.code}")
            }
            val body = response.body ?: error("Empty body")
            val isPartialAppend = response.code == 206 && existingBytes > 0L
            if (!isPartialAppend) {
                existingBytes = 0L
            }
            val remaining = body.contentLength()
            val total = if (remaining > 0L) existingBytes + remaining else -1L
            val buffer = ByteArray(16384)
            var readTotal = existingBytes

            FileOutputStream(partFile, isPartialAppend).use { fileOut ->
                body.byteStream().use { input ->
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        fileOut.write(buffer, 0, read)
                        readTotal += read
                        val fraction = if (total > 0L) (readTotal.toFloat() / total).coerceIn(0f, 1f) else 0.5f
                        onProgress(fraction, readTotal, total)
                    }
                }
            }
            val resultBytes = partFile.readBytes()
            partFile.delete()
            return resultBytes
        }
    }

    private fun decodeDrawableToJpeg(resId: Int): ByteArray {
        val bmp = BitmapFactory.decodeResource(context.resources, resId) ?: return ByteArray(0)
        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 94, out)
        return out.toByteArray()
    }

    private fun saveToPictures(bytes: ByteArray, baseName: String): Triple<Uri?, String, Boolean> {
        val resolver = context.contentResolver
        var fileName = "$baseName.jpg"
        var renamed = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val existing = checkExistingMediaStore(fileName)
            if (existing) {
                fileName = "${baseName}_${System.currentTimeMillis() % 10000}.jpg"
                renamed = true
            }
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Backdrops")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return Triple(null, fileName, renamed)
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return Triple(uri, fileName, renamed)
        } else {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Backdrops").apply {
                mkdirs()
            }
            var file = File(dir, fileName)
            if (file.exists()) {
                fileName = "${baseName}_${System.currentTimeMillis() % 10000}.jpg"
                file = File(dir, fileName)
                renamed = true
            }
            FileOutputStream(file).use { it.write(bytes) }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            return Triple(uri, fileName, renamed)
        }
    }

    private fun checkExistingMediaStore(displayName: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
        return context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            arrayOf(displayName),
            null
        )?.use { cursor -> cursor.count > 0 } ?: false
    }
}
