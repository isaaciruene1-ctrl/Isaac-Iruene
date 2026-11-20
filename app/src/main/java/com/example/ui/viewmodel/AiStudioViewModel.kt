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
import com.example.data.media.WallpaperTarget
import com.example.data.model.Wallpaper
import com.example.data.repository.AiAspectRatio
import com.example.data.repository.AiCapabilities
import com.example.data.repository.AiCapabilityRegistry
import com.example.data.repository.AiImageModelTier
import com.example.data.repository.AiImageResolution
import com.example.data.repository.AiStudioRepository
import com.example.data.repository.GroundingSource
import com.example.data.repository.PhotoAnalysisResult
import com.google.firebase.ai.Chat
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AiStudioTab(val label: String) {
    CREATE_EDIT("Create & Edit"),
    ANALYZE("Analyze Photo"),
    CURATOR("Curator Chat")
}

sealed interface AiImageUiState {
    data object Idle : AiImageUiState
    data object Loading : AiImageUiState
    data class Success(val bitmap: Bitmap) : AiImageUiState
    data class Error(val message: String) : AiImageUiState
}

sealed interface AiAnalysisUiState {
    data object Idle : AiAnalysisUiState
    data object Loading : AiAnalysisUiState
    data class Success(val result: PhotoAnalysisResult) : AiAnalysisUiState
    data class Error(val message: String) : AiAnalysisUiState
}

sealed interface AiDiagnosticState {
    data object Idle : AiDiagnosticState
    data object Checking : AiDiagnosticState
    data class Healthy(val modelUsed: String, val latencyMs: Long, val message: String) : AiDiagnosticState
    data class Issue(val message: String) : AiDiagnosticState
}

data class CuratorChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val sources: List<GroundingSource> = emptyList(),
    val isStreaming: Boolean = false
)

class AiStudioViewModel(
    private val aiRepo: AiStudioRepository,
    private val mediaService: WallpaperMediaService
) : ViewModel() {

    val capabilities: StateFlow<AiCapabilities> = AiCapabilityRegistry.capabilities

    private val _activeTab = MutableStateFlow(AiStudioTab.CREATE_EDIT)
    val activeTab: StateFlow<AiStudioTab> = _activeTab.asStateFlow()

    private val _prompt = MutableStateFlow("")
    val prompt: StateFlow<String> = _prompt.asStateFlow()

    private val _modelTier = MutableStateFlow(AiImageModelTier.NANO_BANANA)
    val modelTier: StateFlow<AiImageModelTier> = _modelTier.asStateFlow()

    private val _resolution = MutableStateFlow(AiImageResolution.RES_1K)
    val resolution: StateFlow<AiImageResolution> = _resolution.asStateFlow()

    private val _aspectRatio = MutableStateFlow(AiAspectRatio.RATIO_9_16)
    val aspectRatio: StateFlow<AiAspectRatio> = _aspectRatio.asStateFlow()

    private val _referenceBitmap = MutableStateFlow<Bitmap?>(null)
    val referenceBitmap: StateFlow<Bitmap?> = _referenceBitmap.asStateFlow()

    private val _isLoadingReference = MutableStateFlow(false)
    val isLoadingReference: StateFlow<Boolean> = _isLoadingReference.asStateFlow()

    private val _imageUiState = MutableStateFlow<AiImageUiState>(AiImageUiState.Idle)
    val imageUiState: StateFlow<AiImageUiState> = _imageUiState.asStateFlow()

    private val _isPolishingPrompt = MutableStateFlow(false)
    val isPolishingPrompt: StateFlow<Boolean> = _isPolishingPrompt.asStateFlow()

    private val _isSavingOrApplying = MutableStateFlow(false)
    val isSavingOrApplying: StateFlow<Boolean> = _isSavingOrApplying.asStateFlow()

    private val _analysisBitmap = MutableStateFlow<Bitmap?>(null)
    val analysisBitmap: StateFlow<Bitmap?> = _analysisBitmap.asStateFlow()

    private val _analysisUiState = MutableStateFlow<AiAnalysisUiState>(AiAnalysisUiState.Idle)
    val analysisUiState: StateFlow<AiAnalysisUiState> = _analysisUiState.asStateFlow()

    private var chatSession: Chat? = null
    private var chatUsesGrounding: Boolean = true

    private val _chatMessages = MutableStateFlow(
        listOf(
            CuratorChatMessage(
                id = "welcome",
                isUser = false,
                text = "Welcome to BACKDROP Studio. Ask me for pastel color palettes, Depth Studio framing advice, current visual design trends, or bespoke wallpaper prompts."
            )
        )
    )
    val chatMessages: StateFlow<List<CuratorChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatSending = MutableStateFlow(false)
    val isChatSending: StateFlow<Boolean> = _isChatSending.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    private val _lastFailedPrompt = MutableStateFlow<String?>(null)
    val lastFailedPrompt: StateFlow<String?> = _lastFailedPrompt.asStateFlow()

    private val _actionStatus = MutableStateFlow<String?>(null)
    val actionStatus: StateFlow<String?> = _actionStatus.asStateFlow()

    private val _actionIsError = MutableStateFlow(false)
    val actionIsError: StateFlow<Boolean> = _actionIsError.asStateFlow()

    private val _diagnosticState = MutableStateFlow<AiDiagnosticState>(AiDiagnosticState.Idle)
    val diagnosticState: StateFlow<AiDiagnosticState> = _diagnosticState.asStateFlow()

    private var referenceLoadJob: Job? = null
    private var generateJob: Job? = null
    private var polishJob: Job? = null
    private var analyzeJob: Job? = null
    private var chatJob: Job? = null
    private var diagnosticJob: Job? = null

    fun selectTab(tab: AiStudioTab) {
        _activeTab.value = tab
    }

    fun updatePrompt(value: String) {
        _prompt.value = value
    }

    fun selectModelTier(tier: AiImageModelTier) {
        _modelTier.value = tier
    }

    fun selectResolution(res: AiImageResolution) {
        _resolution.value = res
    }

    fun selectAspectRatio(ratio: AiAspectRatio) {
        _aspectRatio.value = ratio
    }

    fun clearReferenceBitmap() {
        referenceLoadJob?.cancel()
        _isLoadingReference.value = false
        _referenceBitmap.value = null
    }

    fun clearStatus() {
        _actionStatus.value = null
        _actionIsError.value = false
    }

    private fun postStatus(message: String, isError: Boolean) {
        _actionIsError.value = isError
        _actionStatus.value = message
    }

    fun setReferenceFromUri(context: Context, uri: Uri) {
        referenceLoadJob?.cancel()
        referenceLoadJob = viewModelScope.launch {
            _isLoadingReference.value = true
            try {
                val bmp = decodeUriBitmap(context, uri)
                if (bmp != null) {
                    _referenceBitmap.value = bmp
                    clearStatus()
                } else {
                    postStatus("Could not load selected image.", isError = true)
                }
            } finally {
                _isLoadingReference.value = false
            }
        }
    }

    fun setAnalysisImageFromUri(context: Context, uri: Uri) {
        analyzeJob?.cancel()
        analyzeJob = viewModelScope.launch {
            _analysisUiState.value = AiAnalysisUiState.Loading
            val bmp = decodeUriBitmap(context, uri)
            if (bmp != null) {
                _analysisBitmap.value = bmp
                runAnalysisOnBitmap(bmp)
            } else {
                _analysisUiState.value = AiAnalysisUiState.Error("Could not decode selected photo.")
            }
        }
    }

    fun prepareRemixFromWallpaper(wallpaper: Wallpaper) {
        generateJob?.cancel()
        polishJob?.cancel()
        _isPolishingPrompt.value = false
        _activeTab.value = if (capabilities.value.imageGenAvailable) {
            AiStudioTab.CREATE_EDIT
        } else {
            AiStudioTab.CURATOR
        }
        _imageUiState.value = AiImageUiState.Idle
        clearStatus()
        _prompt.value = "Reimagine '${wallpaper.title}' in a soft pastel Material You studio palette with luminous depth"
        referenceLoadJob?.cancel()
        referenceLoadJob = viewModelScope.launch {
            _isLoadingReference.value = true
            try {
                val bmp = mediaService.loadFullBitmap(wallpaper)
                if (bmp != null) {
                    _referenceBitmap.value = bmp
                } else {
                    postStatus("Could not load '${wallpaper.title}' as reference image.", isError = true)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to prepare remix bitmap", e)
                postStatus(e.localizedMessage ?: "Failed to load reference image.", isError = true)
            } finally {
                _isLoadingReference.value = false
            }
        }
    }

    fun prepareAnalyzeWallpaper(wallpaper: Wallpaper) {
        _activeTab.value = AiStudioTab.ANALYZE
        clearStatus()
        analyzeJob?.cancel()
        analyzeJob = viewModelScope.launch {
            _analysisBitmap.value = null
            _analysisUiState.value = AiAnalysisUiState.Loading
            val bmp = mediaService.loadFullBitmap(wallpaper)
            if (bmp != null) {
                _analysisBitmap.value = bmp
                runAnalysisOnBitmap(bmp)
            } else {
                _analysisUiState.value = AiAnalysisUiState.Error("Unable to load wallpaper bitmap for analysis.")
            }
        }
    }

    fun prepareCreateFromPrompt(seedPrompt: String) {
        _activeTab.value = AiStudioTab.CREATE_EDIT
        clearStatus()
        if (seedPrompt.isNotBlank()) {
            _prompt.value = seedPrompt.trim()
        }
    }

    fun runConnectionDiagnostic() {
        if (_diagnosticState.value is AiDiagnosticState.Checking) return
        diagnosticJob?.cancel()
        diagnosticJob = viewModelScope.launch {
            _diagnosticState.value = AiDiagnosticState.Checking
            try {
                val report = aiRepo.probeAiConnection()
                _diagnosticState.value = if (report.healthy) {
                    AiDiagnosticState.Healthy(
                        modelUsed = report.activeModel,
                        latencyMs = report.latencyMs,
                        message = report.message
                    )
                } else {
                    AiDiagnosticState.Issue(report.message)
                }
            } catch (e: TimeoutCancellationException) {
                _diagnosticState.value = AiDiagnosticState.Issue("Gemini health check timed out after 15 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI diagnostic failed", e)
                _diagnosticState.value = AiDiagnosticState.Issue(
                    aiRepo.formatActionableError(e, "Could not connect to Gemini AI Logic.")
                )
            }
        }
    }

    fun polishCurrentPrompt() {
        val current = _prompt.value.trim()
        if (current.isEmpty() || _isPolishingPrompt.value) return
        polishJob?.cancel()
        polishJob = viewModelScope.launch {
            _isPolishingPrompt.value = true
            clearStatus()
            try {
                _prompt.value = aiRepo.polishPrompt(current)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Prompt polish timed out", e)
                postStatus("Prompt enhancement timed out. Please try again.", isError = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Prompt polish failed", e)
                postStatus(
                    aiRepo.formatActionableError(e, "Prompt enhancement failed."),
                    isError = true
                )
            } finally {
                _isPolishingPrompt.value = false
            }
        }
    }

    fun generateOrEdit() {
        val text = _prompt.value.trim()
        if (text.isEmpty()) {
            _imageUiState.value = AiImageUiState.Error("Enter a wallpaper prompt first.")
            return
        }
        generateJob?.cancel()
        clearStatus()
        generateJob = viewModelScope.launch {
            _imageUiState.value = AiImageUiState.Loading
            try {
                val bmp = aiRepo.generateOrEditWallpaper(
                    prompt = text,
                    referenceBitmap = _referenceBitmap.value,
                    modelTier = _modelTier.value,
                    resolution = _resolution.value,
                    aspectRatio = _aspectRatio.value
                )
                _imageUiState.value = AiImageUiState.Success(bmp)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Image generation timed out", e)
                _imageUiState.value = AiImageUiState.Error(
                    "Image synthesis timed out after 60 seconds. Please try again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Image generation failed", e)
                _imageUiState.value = AiImageUiState.Error(
                    aiRepo.formatActionableError(e, "AI image generation failed.")
                )
            }
        }
    }

    fun useGeneratedAsReference() {
        val success = _imageUiState.value as? AiImageUiState.Success ?: return
        _referenceBitmap.value = success.bitmap
        postStatus("Loaded generated image as reference for further editing.", isError = false)
    }

    fun saveGeneratedToPictures() {
        val success = _imageUiState.value as? AiImageUiState.Success ?: return
        if (_isSavingOrApplying.value) return
        viewModelScope.launch {
            _isSavingOrApplying.value = true
            try {
                val res = mediaService.saveBitmapToPictures(
                    success.bitmap,
                    "BACKDROP_AI_${System.currentTimeMillis() % 100000}"
                )
                if (res.isSuccess) {
                    postStatus("Saved AI wallpaper to Pictures/BACKDROP.", isError = false)
                } else {
                    postStatus(res.exceptionOrNull()?.localizedMessage ?: "Failed to save image.", isError = true)
                }
            } finally {
                _isSavingOrApplying.value = false
            }
        }
    }

    fun applyGeneratedWallpaper(target: WallpaperTarget) {
        val success = _imageUiState.value as? AiImageUiState.Success ?: return
        if (_isSavingOrApplying.value) return
        viewModelScope.launch {
            _isSavingOrApplying.value = true
            try {
                val res = mediaService.applyBitmapAsWallpaper(success.bitmap, target)
                if (res.isSuccess) {
                    val label = when (target) {
                        WallpaperTarget.HOME -> "Home screen"
                        WallpaperTarget.LOCK -> "Lock screen"
                        WallpaperTarget.BOTH -> "Home & Lock screens"
                    }
                    postStatus("Applied AI wallpaper to $label!", isError = false)
                } else {
                    postStatus(res.exceptionOrNull()?.localizedMessage ?: "Failed to apply wallpaper.", isError = true)
                }
            } finally {
                _isSavingOrApplying.value = false
            }
        }
    }

    fun analyzeCurrentBitmap() {
        val bmp = _analysisBitmap.value ?: return
        analyzeJob?.cancel()
        analyzeJob = viewModelScope.launch {
            _analysisUiState.value = AiAnalysisUiState.Loading
            runAnalysisOnBitmap(bmp)
        }
    }

    private suspend fun runAnalysisOnBitmap(bmp: Bitmap) {
        try {
            val result = aiRepo.analyzePhoto(bmp)
            _analysisUiState.value = AiAnalysisUiState.Success(result)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Photo analysis timed out", e)
            _analysisUiState.value = AiAnalysisUiState.Error("Analysis timed out after 60 seconds.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Photo analysis failed", e)
            _analysisUiState.value = AiAnalysisUiState.Error(
                aiRepo.formatActionableError(e, "Photo analysis failed.")
            )
        }
    }

    fun applyAnalyzedPromptToStudio(suggestedPrompt: String) {
        _prompt.value = suggestedPrompt
        _activeTab.value = AiStudioTab.CREATE_EDIT
    }

    fun sendChatMessage(text: String) {
        val clean = text.trim()
        if (clean.isEmpty() || _isChatSending.value) return
        val userMsg = CuratorChatMessage(
            id = "u_${System.currentTimeMillis()}",
            isUser = true,
            text = clean
        )
        val botId = "m_${System.currentTimeMillis() + 1}"
        val botPlaceholder = CuratorChatMessage(
            id = botId,
            isUser = false,
            text = "",
            isStreaming = true
        )
        _chatError.value = null
        _lastFailedPrompt.value = null
        _chatMessages.value = _chatMessages.value + userMsg + botPlaceholder
        _isChatSending.value = true

        chatJob?.cancel()
        chatJob = viewModelScope.launch {
            val sb = StringBuilder()
            val collectedSources = linkedMapOf<String, GroundingSource>()
            try {
                try {
                    val activeChat = chatSession ?: aiRepo.startCuratorChat(
                        useGrounding = chatUsesGrounding,
                        extraHistory = buildHistoryForSession(excludeLastTurns = 2)
                    ).also { chatSession = it }

                    aiRepo.sendCuratorMessageStream(activeChat, clean).collect { chunk ->
                        sb.append(chunk.deltaText)
                        chunk.sources.forEach { src -> collectedSources[src.uri] = src }
                        updateStreamingMessage(botId, sb.toString(), collectedSources.values.toList())
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (groundedErr: Exception) {
                    if (sb.isNotEmpty() || !aiRepo.shouldAttemptFallback(groundedErr)) {
                        throw groundedErr
                    }
                    Log.w(TAG, "Primary Curator chat failed; trying fallback Gemini models", groundedErr)
                    chatUsesGrounding = false
                    chatSession = null
                    var lastFallbackErr: Exception = groundedErr
                    var fallbackSucceeded = false
                    for (candidateModelId in AiStudioRepository.GENERAL_MODEL_FALLBACK_CHAIN) {
                        if (sb.isNotEmpty()) break
                        try {
                            val fallbackChat = aiRepo.startCuratorChat(
                                useGrounding = false,
                                modelNameOverride = candidateModelId,
                                extraHistory = buildHistoryForSession(excludeLastTurns = 2)
                            )
                            aiRepo.sendCuratorMessageStream(fallbackChat, clean).collect { chunk ->
                                sb.append(chunk.deltaText)
                                updateStreamingMessage(botId, sb.toString(), emptyList())
                            }
                            if (sb.isNotBlank()) {
                                chatSession = fallbackChat
                                fallbackSucceeded = true
                                break
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (candidateErr: Exception) {
                            lastFallbackErr = candidateErr
                            Log.w(TAG, "Curator fallback ($candidateModelId) failed", candidateErr)
                        }
                    }
                    if (!fallbackSucceeded && sb.isEmpty()) {
                        throw lastFallbackErr
                    }
                }

                val finalText = sb.toString().ifBlank { "I couldn't generate a response for that. Try rephrasing your question." }
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == botId) msg.copy(text = finalText, isStreaming = false) else msg
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Curator stream timed out", e)
                _chatError.value = "Curator timed out after 60 seconds."
                _lastFailedPrompt.value = clean
                removeStreamingPlaceholder(botId)
            } catch (e: CancellationException) {
                removeStreamingPlaceholder(botId)
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Curator stream failed", e)
                chatSession = null
                _chatError.value = aiRepo.formatActionableError(
                    e,
                    "Curator chat failed. Check connection and retry."
                )
                _lastFailedPrompt.value = clean
                removeStreamingPlaceholder(botId)
            } finally {
                _isChatSending.value = false
            }
        }
    }

    fun retryLastChatMessage() {
        val retryText = _lastFailedPrompt.value ?: return
        _chatError.value = null
        _lastFailedPrompt.value = null
        // Drop the trailing failed user message so sendChatMessage doesn't duplicate it
        val list = _chatMessages.value
        if (list.lastOrNull()?.isUser == true && list.lastOrNull()?.text == retryText) {
            _chatMessages.value = list.dropLast(1)
        }
        sendChatMessage(retryText)
    }

    private fun updateStreamingMessage(
        botId: String,
        currentText: String,
        sources: List<GroundingSource>
    ) {
        _chatMessages.value = _chatMessages.value.map { msg ->
            if (msg.id == botId) {
                msg.copy(
                    text = currentText,
                    sources = sources,
                    isStreaming = true
                )
            } else msg
        }
    }

    private fun buildHistoryForSession(excludeLastTurns: Int): List<com.google.firebase.ai.type.Content> {
        val raw = _chatMessages.value
            .drop(1) // skip welcome banner already seeded in startCuratorChat
            .dropLast(excludeLastTurns.coerceAtMost(_chatMessages.value.size))
            .filter { it.text.isNotBlank() }

        val balanced = mutableListOf<CuratorChatMessage>()
        for (msg in raw) {
            if (msg.isUser) {
                if (balanced.lastOrNull()?.isUser == true) {
                    balanced[balanced.lastIndex] = msg
                } else {
                    balanced.add(msg)
                }
            } else if (balanced.lastOrNull()?.isUser == true) {
                balanced.add(msg)
            }
        }
        if (balanced.lastOrNull()?.isUser == true) {
            balanced.removeAt(balanced.lastIndex)
        }
        return balanced.map { msg ->
            content(if (msg.isUser) "user" else "model") { text(msg.text) }
        }
    }

    private fun removeStreamingPlaceholder(botId: String) {
        _chatMessages.value = _chatMessages.value.filterNot { it.id == botId && it.text.isBlank() }
            .map { if (it.id == botId) it.copy(isStreaming = false) else it }
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
        private const val TAG = "AiStudioVM"

        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AiStudioViewModel(
                    aiRepo = AiStudioRepository(),
                    mediaService = WallpaperMediaService(context.applicationContext)
                )
            }
        }
    }
}
