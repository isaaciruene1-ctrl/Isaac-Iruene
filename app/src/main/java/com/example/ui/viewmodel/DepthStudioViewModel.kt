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
import com.example.data.repository.GuestLocalUserStore
import com.example.engine.depth.DepthDetectionEngine
import com.example.engine.depth.DepthDetectionResult
import com.example.ui.components.ClockDesignConfig
import com.example.ui.components.ClockSpatialMath
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
 * background execution, clock spatial/design configuration, Undo/Redo history, and local persistence.
 */
class DepthStudioViewModel(
    private val mediaService: WallpaperMediaService,
    private val depthEngine: DepthDetectionEngine = DepthDetectionEngine(),
    private val localStore: GuestLocalUserStore? = null
) : ViewModel() {

    private val _state = MutableStateFlow<DepthStudioUiState>(DepthStudioUiState.LoadingFullBitmap)
    val state: StateFlow<DepthStudioUiState> = _state.asStateFlow()

    private val initialSavedClock = localStore?.loadClockDesignConfig() ?: ClockDesignConfig()
    private val _clockConfig = MutableStateFlow(initialSavedClock)
    val clockConfig: StateFlow<ClockDesignConfig> = _clockConfig.asStateFlow()

    private val undoStack = ArrayDeque<ClockDesignConfig>()
    private val redoStack = ArrayDeque<ClockDesignConfig>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private var activeGestureOrigin: ClockDesignConfig? = null

    private data class CachedDepthEntry(
        val key: String,
        val fullBitmap: Bitmap,
        val result: DepthDetectionResult
    )

    private val depthCache = object : LinkedHashMap<String, CachedDepthEntry>(6, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CachedDepthEntry>?): Boolean {
            return size > MAX_CACHE_ENTRIES
        }
    }

    private var depthScanJob: Job? = null
    private var currentKey: String? = null

    fun commitClockConfig(transform: (ClockDesignConfig) -> ClockDesignConfig) {
        val current = _clockConfig.value
        val next = transform(current)
        if (next == current) return
        pushUndoState(current)
        _clockConfig.value = next
        localStore?.saveClockDesignConfig(next)
    }

    fun beginClockGesture(snapshot: ClockDesignConfig = _clockConfig.value) {
        activeGestureOrigin = snapshot
    }

    fun previewClockConfig(next: ClockDesignConfig) {
        _clockConfig.value = next
    }

    fun endClockGesture() {
        val origin = activeGestureOrigin ?: return
        activeGestureOrigin = null
        val finalConfig = _clockConfig.value
        if (finalConfig != origin) {
            pushUndoState(origin)
            localStore?.saveClockDesignConfig(finalConfig)
        }
    }

    fun undoClockEdit() {
        if (undoStack.isEmpty()) return
        val previous = undoStack.removeLast()
        redoStack.addLast(_clockConfig.value)
        _clockConfig.value = previous
        updateHistoryFlags()
        localStore?.saveClockDesignConfig(previous)
    }

    fun redoClockEdit() {
        if (redoStack.isEmpty()) return
        val restored = redoStack.removeLast()
        undoStack.addLast(_clockConfig.value)
        _clockConfig.value = restored
        updateHistoryFlags()
        localStore?.saveClockDesignConfig(restored)
    }

    fun resetClockProportions() {
        commitClockConfig { ClockSpatialMath.resetProportions(it) }
    }

    fun centerClockOnScreen() {
        commitClockConfig { ClockSpatialMath.centerClock(it) }
    }

    fun autoSnapClockToDepth() {
        val ready = (_state.value as? DepthStudioUiState.Ready)?.result
        val readyTop = ready?.recommendedClockTopFraction ?: ClockSpatialMath.DEFAULT_PLACEMENT_Y
        val readyAlign = ready?.recommendedAlign ?: com.example.engine.depth.ClockHorizontalAlign.CENTER
        val readyScale = ready?.recommendedClockScale ?: 1.0f
        commitClockConfig {
            ClockSpatialMath.autoSnapToDepth(it, readyTop, readyAlign).copy(
                widthScale = readyScale,
                heightScale = readyScale
            )
        }
    }

    fun restoreClockConfig(restored: ClockDesignConfig) {
        commitClockConfig { restored }
    }

    fun resetClockFrameAndCenter() {
        commitClockConfig { cfg ->
            ClockSpatialMath.centerClock(ClockSpatialMath.resetProportions(cfg))
        }
    }

    private fun pushUndoState(snapshot: ClockDesignConfig) {
        if (undoStack.size >= MAX_HISTORY_STEPS) {
            undoStack.removeFirst()
        }
        undoStack.addLast(snapshot)
        redoStack.clear()
        updateHistoryFlags()
    }

    private fun updateHistoryFlags() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

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
            runDepthScan(key = key, fullBmp = fullBmp, titleHint = wallpaper.category.displayName, saveToCache = true)
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
            runDepthScan(key = key, fullBmp = bmp, titleHint = "Custom Photo", saveToCache = true)
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
        private const val MAX_HISTORY_STEPS = 40

        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                DepthStudioViewModel(
                    mediaService = WallpaperMediaService(appContext),
                    depthEngine = DepthDetectionEngine(),
                    localStore = GuestLocalUserStore(appContext)
                )
            }
        }
    }
}
