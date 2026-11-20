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
import com.example.data.repository.AiCapabilities
import com.example.data.repository.AiCapabilityRegistry
import com.example.data.repository.AiStudioRepository
import com.example.engine.depth.DepthDetectionEngine
import com.example.engine.depth.DepthDetectionResult
import com.example.engine.depth.DepthGenerativeFillEngine
import com.example.engine.depth.GenerativeFillMood
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
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

sealed interface GenerativeFillUiState {
    data object Idle : GenerativeFillUiState
    data object Filling : GenerativeFillUiState
    data class Applied(val moodLabel: String) : GenerativeFillUiState
    data class Error(val message: String) : GenerativeFillUiState
}

/**
 * Dedicated ViewModel isolating Depth Studio bitmap loading, pixel-level DepthDetectionEngine
 * background execution, Gemini Generative Fill (Wallpaper Extend), cancellation, and LRU caching.
 */
class DepthStudioViewModel(
    private val mediaService: WallpaperMediaService,
    private val depthEngine: DepthDetectionEngine = DepthDetectionEngine(),
    private val aiRepo: AiStudioRepository = AiStudioRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<DepthStudioUiState>(DepthStudioUiState.LoadingFullBitmap)
    val state: StateFlow<DepthStudioUiState> = _state.asStateFlow()

    private val _fillState = MutableStateFlow<GenerativeFillUiState>(GenerativeFillUiState.Idle)
    val fillState: StateFlow<GenerativeFillUiState> = _fillState.asStateFlow()

    private val _framingResetVersion = MutableStateFlow(0)
    val framingResetVersion: StateFlow<Int> = _framingResetVersion.asStateFlow()

    val aiCapabilities: StateFlow<AiCapabilities> = AiCapabilityRegistry.capabilities

    private data class CachedDepthEntry(
        val key: String,
        val fullBitmap: Bitmap,
        val result: DepthDetectionResult
    )

    // Access-ordered bounded cache (max 2 full-res wallpapers to keep 4K RAM bounded)
    private val depthCache = object : LinkedHashMap<String, CachedDepthEntry>(6, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CachedDepthEntry>?): Boolean {
            return size > MAX_CACHE_ENTRIES
        }
    }

    private var depthScanJob: Job? = null
    private var generativeFillJob: Job? = null
    private var currentKey: String? = null
    private var currentTitleHint: String = "Wallpaper"
    private var preFillSnapshot: CachedDepthEntry? = null

    fun loadWallpaperForDepthStudio(wallpaper: Wallpaper, forceRescan: Boolean = false) {
        val key = wallpaper.id
        if (!forceRescan) {
            if (currentKey == key && (_fillState.value is GenerativeFillUiState.Applied || _fillState.value is GenerativeFillUiState.Filling)) {
                return
            }
            depthCache[key]?.let { cached ->
                currentKey = key
                currentTitleHint = wallpaper.title
                preFillSnapshot = null
                _fillState.value = GenerativeFillUiState.Idle
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
        currentTitleHint = wallpaper.title
        preFillSnapshot = null
        generativeFillJob?.cancel()
        _fillState.value = GenerativeFillUiState.Idle
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
            runDepthScan(key = key, fullBmp = fullBmp, titleHint = wallpaper.title, saveToCache = true)
        }
    }

    fun loadCustomPhotoForDepthStudio(context: Context, uri: Uri, forceRescan: Boolean = false) {
        val key = "uri_$uri"
        if (!forceRescan) {
            if (currentKey == key && (_fillState.value is GenerativeFillUiState.Applied || _fillState.value is GenerativeFillUiState.Filling)) {
                return
            }
            depthCache[key]?.let { cached ->
                currentKey = key
                currentTitleHint = "Custom Photo"
                preFillSnapshot = null
                _fillState.value = GenerativeFillUiState.Idle
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
        currentTitleHint = "Custom Photo"
        preFillSnapshot = null
        generativeFillJob?.cancel()
        _fillState.value = GenerativeFillUiState.Idle
        depthScanJob?.cancel()
        depthScanJob = viewModelScope.launch {
            _state.value = DepthStudioUiState.LoadingFullBitmap
            val bmp = decodeUriBitmap(context, uri)
            if (bmp == null) {
                _state.value = DepthStudioUiState.BitmapError("Could not load selected photo.")
                return@launch
            }
            runDepthScan(key = key, fullBmp = bmp, titleHint = "Custom Photo", saveToCache = true)
        }
    }

    fun applyGenerativeFill(
        viewportWidth: Float,
        viewportHeight: Float,
        zoomScale: Float,
        panOffsetX: Float,
        panOffsetY: Float,
        mood: GenerativeFillMood,
        customHint: String
    ) {
        val currentBitmap = when (val s = _state.value) {
            is DepthStudioUiState.Ready -> s.fullBitmap
            is DepthStudioUiState.ScanningSubject -> s.fullBitmap
            is DepthStudioUiState.EngineError -> s.fullBitmap
            else -> {
                _fillState.value = GenerativeFillUiState.Error("Wait for the wallpaper canvas to finish loading first.")
                return
            }
        }

        generativeFillJob?.cancel()
        generativeFillJob = viewModelScope.launch {
            _fillState.value = GenerativeFillUiState.Filling
            var preparedBase: Bitmap? = null
            var preparedGuide: Bitmap? = null
            try {
                val prepared = withContext(Dispatchers.Default) {
                    val spec = DepthGenerativeFillEngine.computeFrameSpec(
                        sourceWidth = currentBitmap.width,
                        sourceHeight = currentBitmap.height,
                        viewportWidth = viewportWidth,
                        viewportHeight = viewportHeight,
                        zoomScale = zoomScale,
                        panOffsetX = panOffsetX,
                        panOffsetY = panOffsetY
                    )
                    if (!spec.hasUncoveredBorders) return@withContext null
                    DepthGenerativeFillEngine.prepareOutpaintInput(
                        sourceBitmap = currentBitmap,
                        spec = spec,
                        mood = mood,
                        customHint = customHint
                    )
                }
                if (prepared == null) {
                    _fillState.value = GenerativeFillUiState.Error(
                        "Zoom out below 1.0x or drag the wallpaper away from an edge to reveal empty space to fill."
                    )
                    return@launch
                }
                preparedBase = prepared.baseLayerBitmap
                preparedGuide = prepared.aiGuideBitmap

                // Preserve the original Ready snapshot once before outpainting so Undo restores bit-exact source
                if (preFillSnapshot == null) {
                    (_state.value as? DepthStudioUiState.Ready)?.let { readyState ->
                        preFillSnapshot = CachedDepthEntry(
                            key = readyState.key,
                            fullBitmap = readyState.fullBitmap,
                            result = readyState.result
                        )
                    }
                }

                val generatedRaw = aiRepo.generateOutpaintedFill(
                    guideBitmap = prepared.aiGuideBitmap,
                    prompt = prepared.prompt,
                    aspectRatio = prepared.recommendedAspectRatio
                )

                val extendedBitmap = withContext(Dispatchers.Default) {
                    DepthGenerativeFillEngine.blendOutpaintedFill(
                        baseLayerBitmap = prepared.baseLayerBitmap,
                        generatedBitmap = generatedRaw,
                        spec = prepared.spec
                    )
                }

                _framingResetVersion.value += 1
                _fillState.value = GenerativeFillUiState.Applied(mood.label)

                depthScanJob?.cancel()
                val extendedKey = "${currentKey ?: "canvas"}#ext_${System.currentTimeMillis()}"
                runDepthScan(
                    key = extendedKey,
                    fullBmp = extendedBitmap,
                    titleHint = currentTitleHint,
                    saveToCache = false
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Generative fill timed out", e)
                _fillState.value = GenerativeFillUiState.Error(
                    "Generative fill timed out after 60 seconds. Please try again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Generative fill failed", e)
                _fillState.value = GenerativeFillUiState.Error(
                    aiRepo.formatActionableError(
                        e,
                        "Generative fill failed. Check connection and try again."
                    )
                )
            } finally {
                preparedBase?.recycle()
                preparedGuide?.recycle()
            }
        }
    }

    fun undoGenerativeFill() {
        generativeFillJob?.cancel()
        val snapshot = preFillSnapshot ?: return
        preFillSnapshot = null
        _fillState.value = GenerativeFillUiState.Idle
        _framingResetVersion.value += 1
        _state.value = DepthStudioUiState.Ready(
            key = snapshot.key,
            fullBitmap = snapshot.fullBitmap,
            result = snapshot.result
        )
    }

    fun clearFillError() {
        if (_fillState.value is GenerativeFillUiState.Error) {
            _fillState.value = if (preFillSnapshot != null) {
                GenerativeFillUiState.Applied("Extended")
            } else {
                GenerativeFillUiState.Idle
            }
        }
    }

    private suspend fun runDepthScan(
        key: String,
        fullBmp: Bitmap,
        titleHint: String,
        saveToCache: Boolean
    ) {
        _state.value = DepthStudioUiState.ScanningSubject(
            fullBitmap = fullBmp,
            subjectHint = titleHint
        )
        try {
            val detection = depthEngine.detectAndExtract(fullBmp, titleHint)
            if (saveToCache) {
                depthCache[key] = CachedDepthEntry(key, fullBmp, detection)
            }
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
                    depthEngine = DepthDetectionEngine(),
                    aiRepo = AiStudioRepository()
                )
            }
        }
    }
}
