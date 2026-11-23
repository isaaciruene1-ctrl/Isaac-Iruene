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
import com.example.data.model.LockScreenSetDoc
import com.example.data.model.Wallpaper
import com.example.data.repository.GuestLocalUserStore
import com.example.data.weather.OpenMeteoWeatherRepository
import com.example.data.weather.WeatherTelemetryState
import com.example.engine.depth.ClockHorizontalAlign
import com.example.engine.depth.DepthDetectionEngine
import com.example.engine.depth.DepthDetectionResult
import com.example.engine.depth.MaskBrushStroke
import com.example.ui.components.ClockDesignConfig
import com.example.ui.components.ClockSpatialMath
import com.example.ui.components.ClockWidgetTelemetry
import com.example.ui.screens.CanvasLookFilter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CanvasFramingState(
    val zoomScale: Float = 1.0f,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f,
    val rotationDegrees: Float = 0f,
    val dimFraction: Float = 0f,
    val lookFilter: CanvasLookFilter = CanvasLookFilter.ORIGINAL
)

data class StudioHistorySnapshot(
    val clockConfig: ClockDesignConfig = ClockDesignConfig(),
    val canvasFraming: CanvasFramingState = CanvasFramingState(),
    val wallpaperId: String = "",
    val customPhotoUri: String = "",
    val maskStrokes: List<MaskBrushStroke> = emptyList(),
    val maskFeather: Float = 0.45f
)

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
 * Dedicated ViewModel managing Depth Studio bitmap extraction, Depth Engine V2 manual mask refinement,
 * Canvas framing (zoom/pan/rotation/dim/filter), atomic Undo/Redo history across all Studio dimensions,
 * live Open-Meteo + Battery widget telemetry, and local clock config persistence.
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

    private val _canvasFraming = MutableStateFlow(CanvasFramingState())
    val canvasFraming: StateFlow<CanvasFramingState> = _canvasFraming.asStateFlow()

    private val _activeWallpaperId = MutableStateFlow("")
    val activeWallpaperId: StateFlow<String> = _activeWallpaperId.asStateFlow()

    private val _customPhotoUriString = MutableStateFlow("")
    val customPhotoUriString: StateFlow<String> = _customPhotoUriString.asStateFlow()

    private val _maskStrokes = MutableStateFlow<List<MaskBrushStroke>>(emptyList())
    val maskStrokes: StateFlow<List<MaskBrushStroke>> = _maskStrokes.asStateFlow()

    private val _maskFeather = MutableStateFlow(0.45f)
    val maskFeather: StateFlow<Float> = _maskFeather.asStateFlow()

    private val _refinedCutoutBitmap = MutableStateFlow<Bitmap?>(null)
    val refinedCutoutBitmap: StateFlow<Bitmap?> = _refinedCutoutBitmap.asStateFlow()

    private val _widgetTelemetry = MutableStateFlow(ClockWidgetTelemetry())
    val widgetTelemetry: StateFlow<ClockWidgetTelemetry> = _widgetTelemetry.asStateFlow()

    private val _weatherState = MutableStateFlow<WeatherTelemetryState>(WeatherTelemetryState.Idle)
    val weatherState: StateFlow<WeatherTelemetryState> = _weatherState.asStateFlow()

    private val undoStack = ArrayDeque<StudioHistorySnapshot>()
    private val redoStack = ArrayDeque<StudioHistorySnapshot>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private var activeGestureOrigin: StudioHistorySnapshot? = null

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
    private var maskRefineJob: Job? = null
    private var currentKey: String? = null

    fun currentSnapshot(): StudioHistorySnapshot = StudioHistorySnapshot(
        clockConfig = _clockConfig.value,
        canvasFraming = _canvasFraming.value,
        wallpaperId = _activeWallpaperId.value,
        customPhotoUri = _customPhotoUriString.value,
        maskStrokes = _maskStrokes.value,
        maskFeather = _maskFeather.value
    )

    fun commitClockConfig(transform: (ClockDesignConfig) -> ClockDesignConfig) {
        val current = _clockConfig.value
        val next = transform(current)
        if (next == current) return
        pushUndoState(currentSnapshot())
        _clockConfig.value = next
        localStore?.saveClockDesignConfig(next)
    }

    fun beginClockGesture(@Suppress("UNUSED_PARAMETER") snapshot: ClockDesignConfig = _clockConfig.value) {
        activeGestureOrigin = currentSnapshot()
    }

    fun previewClockConfig(next: ClockDesignConfig) {
        _clockConfig.value = next
    }

    fun endClockGesture() {
        endStudioGesture()
    }

    fun beginStudioGesture() {
        if (activeGestureOrigin == null) {
            activeGestureOrigin = currentSnapshot()
        }
    }

    fun endStudioGesture() {
        val origin = activeGestureOrigin ?: return
        activeGestureOrigin = null
        val finalSnapshot = currentSnapshot()
        if (finalSnapshot != origin) {
            pushUndoState(origin)
            localStore?.saveClockDesignConfig(finalSnapshot.clockConfig)
        }
    }

    fun previewCanvasFraming(next: CanvasFramingState) {
        _canvasFraming.value = next.copy(
            zoomScale = next.zoomScale.coerceIn(1.0f, 2.2f),
            dimFraction = next.dimFraction.coerceIn(0f, 0.50f)
        )
    }

    fun commitCanvasFraming(transform: (CanvasFramingState) -> CanvasFramingState) {
        val prev = currentSnapshot()
        val next = transform(prev.canvasFraming).let {
            it.copy(
                zoomScale = it.zoomScale.coerceIn(1.0f, 2.2f),
                dimFraction = it.dimFraction.coerceIn(0f, 0.50f)
            )
        }
        if (next == prev.canvasFraming) return
        pushUndoState(prev)
        _canvasFraming.value = next
    }

    fun resetCanvasFraming() {
        commitCanvasFraming { CanvasFramingState() }
    }

    fun undoClockEdit() = undoEdit()

    fun redoClockEdit() = redoEdit()

    fun undoEdit() {
        if (undoStack.isEmpty()) return
        val previous = undoStack.removeLast()
        redoStack.addLast(currentSnapshot())
        applySnapshotRestored(previous)
        updateHistoryFlags()
    }

    fun redoEdit() {
        if (redoStack.isEmpty()) return
        val restored = redoStack.removeLast()
        undoStack.addLast(currentSnapshot())
        applySnapshotRestored(restored)
        updateHistoryFlags()
    }

    private fun applySnapshotRestored(snapshot: StudioHistorySnapshot) {
        val prevStrokes = _maskStrokes.value
        val prevFeather = _maskFeather.value
        _clockConfig.value = snapshot.clockConfig
        _canvasFraming.value = snapshot.canvasFraming
        _activeWallpaperId.value = snapshot.wallpaperId
        _customPhotoUriString.value = snapshot.customPhotoUri
        _maskStrokes.value = snapshot.maskStrokes
        _maskFeather.value = snapshot.maskFeather
        localStore?.saveClockDesignConfig(snapshot.clockConfig)
        if (prevStrokes != snapshot.maskStrokes || prevFeather != snapshot.maskFeather) {
            recomputeRefinedCutout()
        }
    }

    fun resetClockProportions() {
        commitClockConfig { ClockSpatialMath.resetProportions(it) }
    }

    fun centerClockOnScreen() {
        commitClockConfig { ClockSpatialMath.centerClock(it) }
    }

    fun autoSnapClockToDepth(viewportWidthPx: Float = 1080f, viewportHeightPx: Float = 2400f) {
        val ready = (_state.value as? DepthStudioUiState.Ready)?.result
        val readyTop = ready?.recommendedClockTopFraction ?: ClockSpatialMath.DEFAULT_PLACEMENT_Y
        val readyAlign = ready?.recommendedAlign ?: ClockHorizontalAlign.CENTER
        val readyScale = ready?.recommendedClockScale ?: 1.0f
        val prev = currentSnapshot()

        val targetZoom = (ready?.recommendedWallpaperZoom ?: 1.0f).coerceIn(1.0f, 1.80f)
        val vw = viewportWidthPx.coerceAtLeast(1f)
        val vh = viewportHeightPx.coerceAtLeast(1f)
        val maxPanX = ((vw * (targetZoom - 1f)) * 0.5f).coerceAtLeast(180f)
        val maxPanY = ((vh * (targetZoom - 1f)) * 0.5f).coerceAtLeast(220f)
        val targetPanX = ((ready?.recommendedWallpaperPanXFraction ?: 0f) * vw).coerceIn(-maxPanX, maxPanX)
        val targetPanY = ((ready?.recommendedWallpaperPanYFraction ?: 0f) * vh).coerceIn(-maxPanY, maxPanY)

        val nextClock = ClockSpatialMath.autoSnapToDepth(_clockConfig.value, readyTop, readyAlign).copy(
            widthScale = readyScale,
            heightScale = readyScale
        )
        val nextFraming = _canvasFraming.value.copy(
            zoomScale = targetZoom,
            panOffsetX = targetPanX,
            panOffsetY = targetPanY,
            rotationDegrees = 0f
        )

        if (nextClock == prev.clockConfig && nextFraming == prev.canvasFraming) return
        pushUndoState(prev)
        _clockConfig.value = nextClock
        _canvasFraming.value = nextFraming
        localStore?.saveClockDesignConfig(nextClock)
    }

    fun restoreClockConfig(restored: ClockDesignConfig) {
        commitClockConfig { restored }
    }

    fun restoreFromLockScreenSet(setDoc: LockScreenSetDoc) {
        val restoredClock = ClockDesignConfig.fromJsonString(setDoc.clockConfigJson) ?: _clockConfig.value
        val restoredFilter = runCatching { CanvasLookFilter.valueOf(setDoc.lookFilterName) }
            .getOrDefault(CanvasLookFilter.ORIGINAL)
        _clockConfig.value = restoredClock
        _canvasFraming.value = CanvasFramingState(
            zoomScale = setDoc.zoomScale.coerceIn(1f, 2.2f),
            panOffsetX = setDoc.panOffsetX,
            panOffsetY = setDoc.panOffsetY,
            rotationDegrees = setDoc.rotationDegrees,
            dimFraction = setDoc.dimFraction.coerceIn(0f, 0.50f),
            lookFilter = restoredFilter
        )
        _activeWallpaperId.value = setDoc.wallpaperId
        _customPhotoUriString.value = setDoc.customPhotoUri
        _maskStrokes.value = emptyList()
        undoStack.clear()
        redoStack.clear()
        updateHistoryFlags()
        localStore?.saveClockDesignConfig(restoredClock)
    }

    fun resetClockFrameAndCenter() {
        commitClockConfig { cfg ->
            ClockSpatialMath.centerClock(ClockSpatialMath.resetProportions(cfg))
        }
    }

    fun addMaskStroke(stroke: MaskBrushStroke) {
        if (stroke.normalizedPoints.isEmpty()) return
        val prev = currentSnapshot()
        pushUndoState(prev)
        _maskStrokes.value = prev.maskStrokes + stroke
        recomputeRefinedCutout()
    }

    fun clearMaskStrokes() {
        if (_maskStrokes.value.isEmpty()) return
        val prev = currentSnapshot()
        pushUndoState(prev)
        _maskStrokes.value = emptyList()
        recomputeRefinedCutout()
    }

    fun setMaskFeather(feather: Float, commitUndo: Boolean = false) {
        val clamped = feather.coerceIn(0.15f, 1.3f)
        if (commitUndo) {
            val prev = currentSnapshot()
            if (prev.maskFeather != clamped) {
                pushUndoState(prev)
            }
        }
        _maskFeather.value = clamped
        recomputeRefinedCutout()
    }

    private fun recomputeRefinedCutout() {
        val ready = _state.value as? DepthStudioUiState.Ready ?: return
        val strokes = _maskStrokes.value
        val feather = _maskFeather.value
        if (strokes.isEmpty()) {
            maskRefineJob?.cancel()
            _refinedCutoutBitmap.value = ready.result.cutoutBitmap
            return
        }
        maskRefineJob?.cancel()
        maskRefineJob = viewModelScope.launch {
            val refined = depthEngine.applyManualMaskRefinement(
                sourceBitmap = ready.fullBitmap,
                baseCutout = ready.result.cutoutBitmap,
                strokes = strokes,
                featherStrength = feather
            )
            _refinedCutoutBitmap.value = refined ?: ready.result.cutoutBitmap
        }
    }

    fun refreshTelemetry(context: Context, weatherCity: String, useLocation: Boolean) {
        val batteryPct = OpenMeteoWeatherRepository.readBatteryPercent(context)
        _widgetTelemetry.value = _widgetTelemetry.value.copy(batteryPercent = batteryPct)
        viewModelScope.launch {
            val weather = when {
                weatherCity.isNotBlank() -> OpenMeteoWeatherRepository.refreshByCity(weatherCity)
                useLocation -> OpenMeteoWeatherRepository.refreshByDeviceCoarseLocation(context)
                else -> OpenMeteoWeatherRepository.state.value
            }
            _weatherState.value = weather
            when (weather) {
                is WeatherTelemetryState.Ready -> {
                    _widgetTelemetry.value = ClockWidgetTelemetry(
                        weatherText = weather.compactReadout,
                        batteryPercent = OpenMeteoWeatherRepository.readBatteryPercent(context),
                        isWeatherAvailable = true
                    )
                }
                is WeatherTelemetryState.PermissionDenied -> {
                    _widgetTelemetry.value = ClockWidgetTelemetry(
                        weatherText = "ENABLE LOCATION OR CITY",
                        batteryPercent = batteryPct,
                        isWeatherAvailable = false
                    )
                }
                is WeatherTelemetryState.Unavailable -> {
                    _widgetTelemetry.value = ClockWidgetTelemetry(
                        weatherText = "WEATHER OFFLINE",
                        batteryPercent = batteryPct,
                        isWeatherAvailable = false
                    )
                }
                else -> Unit
            }
        }
    }

    private fun pushUndoState(snapshot: StudioHistorySnapshot) {
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
        if (_activeWallpaperId.value != key || _customPhotoUriString.value.isNotEmpty()) {
            _activeWallpaperId.value = key
            _customPhotoUriString.value = ""
            if (!forceRescan) {
                _maskStrokes.value = emptyList()
            }
        }
        if (!forceRescan) {
            depthCache[key]?.let { cached ->
                currentKey = key
                _refinedCutoutBitmap.value = cached.result.cutoutBitmap
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
            runDepthScan(key = key, fullBmp = fullBmp, titleHint = wallpaper.category.displayName, saveToCache = true, forceRescan = forceRescan)
        }
    }

    fun loadCustomPhotoForDepthStudio(context: Context, uri: Uri, forceRescan: Boolean = false) {
        val key = "uri_$uri"
        if (_customPhotoUriString.value != uri.toString()) {
            _customPhotoUriString.value = uri.toString()
            if (!forceRescan) {
                _maskStrokes.value = emptyList()
            }
        }
        if (!forceRescan) {
            depthCache[key]?.let { cached ->
                currentKey = key
                _refinedCutoutBitmap.value = cached.result.cutoutBitmap
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
            runDepthScan(key = key, fullBmp = bmp, titleHint = "Custom Photo", saveToCache = true, forceRescan = forceRescan)
        }
    }

    private suspend fun runDepthScan(
        key: String,
        fullBmp: Bitmap,
        titleHint: String,
        saveToCache: Boolean,
        forceRescan: Boolean = false
    ) {
        _state.value = DepthStudioUiState.ScanningSubject(
            fullBitmap = fullBmp,
            subjectHint = titleHint
        )
        try {
            val detection = depthEngine.detectAndExtract(
                sourceBitmap = fullBmp,
                titleHint = titleHint,
                cacheKey = if (forceRescan) null else key
            )
            if (saveToCache) {
                depthCache[key] = CachedDepthEntry(key, fullBmp, detection)
            }
            _refinedCutoutBitmap.value = if (_maskStrokes.value.isNotEmpty()) {
                depthEngine.applyManualMaskRefinement(
                    sourceBitmap = fullBmp,
                    baseCutout = detection.cutoutBitmap,
                    strokes = _maskStrokes.value,
                    featherStrength = _maskFeather.value
                ) ?: detection.cutoutBitmap
            } else {
                detection.cutoutBitmap
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
        private const val MAX_CACHE_ENTRIES = 3
        private const val MAX_HISTORY_STEPS = 48

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
