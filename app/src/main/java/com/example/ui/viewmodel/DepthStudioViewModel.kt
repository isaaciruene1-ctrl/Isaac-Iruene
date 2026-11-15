package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.media.WallpaperMediaService
import com.example.data.model.Wallpaper
import com.example.engine.depth.DepthDetectionEngine
import com.example.engine.depth.DepthDetectionResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface DepthStudioUiState {
    data object LoadingFullBitmap : DepthStudioUiState
    data class ScanningSubject(val fullBitmap: Bitmap, val subjectHint: String) : DepthStudioUiState
    data class Ready(
        val key: String,
        val fullBitmap: Bitmap,
        val result: DepthDetectionResult
    ) : DepthStudioUiState
    data class BitmapError(val message: String) : DepthStudioUiState
    data class EngineError(val key: String, val fullBitmap: Bitmap, val message: String) : DepthStudioUiState
}

/**
 * Dedicated ViewModel isolating Depth Studio bitmap loading, pixel-level DepthDetectionEngine
 * background execution, cancellation on rapid wallpaper switching, and bounded LRU caching.
 */
class DepthStudioViewModel(
    private val mediaService: WallpaperMediaService,
    private val depthEngine: DepthDetectionEngine = DepthDetectionEngine()
) : ViewModel() {

    private val _state = MutableStateFlow<DepthStudioUiState>(DepthStudioUiState.LoadingFullBitmap)
    val state: StateFlow<DepthStudioUiState> = _state.asStateFlow()

    private data class CachedDepthEntry(
        val fullBitmap: Bitmap,
        val result: DepthDetectionResult
    )

    // Access-ordered bounded cache (max 4 full-res wallpapers to keep 4K RAM bounded)
    private val depthCache = object : LinkedHashMap<String, CachedDepthEntry>(8, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CachedDepthEntry>?): Boolean {
            return size > MAX_CACHE_ENTRIES
        }
    }

    private var depthScanJob: Job? = null
    private var currentKey: String? = null

    fun loadWallpaperForDepthStudio(wallpaper: Wallpaper, forceRescan: Boolean = false) {
        val key = wallpaper.id
        if (!forceRescan) {
            depthCache[key]?.let { cached ->
                currentKey = key
                _state.value = DepthStudioUiState.Ready(
                    key = key,
                    fullBitmap = cached.fullBitmap,
                    result = cached.result
                )
                return
            }
            if (currentKey == key && (_state.value is DepthStudioUiState.ScanningSubject || _state.value is DepthStudioUiState.LoadingFullBitmap)) {
                return
            }
        }
        currentKey = key
        depthScanJob?.cancel()
        depthScanJob = viewModelScope.launch {
            _state.value = DepthStudioUiState.LoadingFullBitmap
            val fullBmp = mediaService.loadFullBitmap(wallpaper)
            if (fullBmp == null) {
                _state.value = DepthStudioUiState.BitmapError(
                    "Unable to load full-resolution wallpaper pixels."
                )
                return@launch
            }
            runDepthScan(key = key, fullBmp = fullBmp, titleHint = wallpaper.title)
        }
    }

    fun loadCustomPhotoForDepthStudio(context: Context, uri: Uri, forceRescan: Boolean = false) {
        val key = "uri_$uri"
        if (!forceRescan) {
            depthCache[key]?.let { cached ->
                currentKey = key
                _state.value = DepthStudioUiState.Ready(
                    key = key,
                    fullBitmap = cached.fullBitmap,
                    result = cached.result
                )
                return
            }
            if (currentKey == key && (_state.value is DepthStudioUiState.ScanningSubject || _state.value is DepthStudioUiState.LoadingFullBitmap)) {
                return
            }
        }
        currentKey = key
        depthScanJob?.cancel()
        depthScanJob = viewModelScope.launch {
            _state.value = DepthStudioUiState.LoadingFullBitmap
            val bmp = decodeUriBitmap(context, uri)
            if (bmp == null) {
                _state.value = DepthStudioUiState.BitmapError("Could not load selected photo.")
                return@launch
            }
            runDepthScan(key = key, fullBmp = bmp, titleHint = "Custom Photo")
        }
    }

    private suspend fun runDepthScan(
        key: String,
        fullBmp: Bitmap,
        titleHint: String
    ) {
        _state.value = DepthStudioUiState.ScanningSubject(
            fullBitmap = fullBmp,
            subjectHint = titleHint
        )
        try {
            val detection = depthEngine.detectAndExtract(fullBmp, titleHint)
            depthCache[key] = CachedDepthEntry(fullBmp, detection)
            _state.value = DepthStudioUiState.Ready(
                key = key,
                fullBitmap = fullBmp,
                result = detection
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Depth engine scan failed for $key", e)
            _state.value = DepthStudioUiState.EngineError(
                key = key,
                fullBitmap = fullBmp,
                message = e.localizedMessage ?: "Depth contour scan failed."
            )
        }
    }

    private suspend fun decodeUriBitmap(context: Context, uri: Uri): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode URI bitmap", e)
                null
            }
        }

    companion object {
        private const val TAG = "DepthStudioVM"
        private const val MAX_CACHE_ENTRIES = 2

        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DepthStudioViewModel(
                    mediaService = WallpaperMediaService(context.applicationContext),
                    depthEngine = DepthDetectionEngine()
                )
            }
        }
    }
}
