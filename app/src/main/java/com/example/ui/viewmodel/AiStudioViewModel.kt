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
import com.example.data.repository.AiImageModelTier
import com.example.data.repository.AiImageResolution
import com.example.data.repository.AiStudioRepository
import com.example.data.repository.GroundingSource
import com.example.data.repository.PhotoAnalysisResult
import com.google.firebase.ai.Chat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
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

    private val _imageUiState = MutableStateFlow<AiImageUiState>(AiImageUiState.Idle)
    val imageUiState: StateFlow<AiImageUiState> = _imageUiState.asStateFlow()

    private val _isPolishingPrompt = MutableStateFlow(false)
    val isPolishingPrompt: StateFlow<Boolean> = _isPolishingPrompt.asStateFlow()

    private val _analysisBitmap = MutableStateFlow<Bitmap?>(null)
    val analysisBitmap: StateFlow<Bitmap?> = _analysisBitmap.asStateFlow()

    private val _analysisUiState = MutableStateFlow<AiAnalysisUiState>(AiAnalysisUiState.Idle)
    val analysisUiState: StateFlow<AiAnalysisUiState> = _analysisUiState.asStateFlow()

    private val chatSession: Chat by lazy { aiRepo.startCuratorChat() }

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

    private val _actionStatus = MutableStateFlow<String?>(null)
    val actionStatus: StateFlow<String?> = _actionStatus.asStateFlow()

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
        _referenceBitmap.value = null
    }

    fun clearStatus() {
        _actionStatus.value = null
    }

    fun setReferenceFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val bmp = decodeUriBitmap(context, uri)
            if (bmp != null) {
                _referenceBitmap.value = bmp
            } else {
                _actionStatus.value = "Could not load selected image."
            }
        }
    }

    fun setAnalysisImageFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val bmp = decodeUriBitmap(context, uri)
            if (bmp != null) {
                _analysisBitmap.value = bmp
                analyzeCurrentBitmap()
            } else {
                _analysisUiState.value = AiAnalysisUiState.Error("Could not decode selected photo.")
            }
        }
    }

    fun prepareRemixFromWallpaper(wallpaper: Wallpaper) {
        _activeTab.value = AiStudioTab.CREATE_EDIT
        _prompt.value = "Reimagine '${wallpaper.title}' in a soft pastel Material You studio palette with luminous depth"
        viewModelScope.launch {
            val bmp = mediaService.loadFullBitmap(wallpaper)
            _referenceBitmap.value = bmp
        }
    }

    fun prepareAnalyzeWallpaper(wallpaper: Wallpaper) {
        _activeTab.value = AiStudioTab.ANALYZE
        _analysisUiState.value = AiAnalysisUiState.Loading
        viewModelScope.launch {
            val bmp = mediaService.loadFullBitmap(wallpaper)
            if (bmp != null) {
                _analysisBitmap.value = bmp
                analyzeCurrentBitmap()
            } else {
                _analysisUiState.value = AiAnalysisUiState.Error("Unable to load wallpaper bitmap for analysis.")
            }
        }
    }

    fun polishCurrentPrompt() {
        val current = _prompt.value.trim()
        if (current.isEmpty()) return
        viewModelScope.launch {
            _isPolishingPrompt.value = true
            try {
                _prompt.value = aiRepo.polishPrompt(current)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Prompt polish failed", e)
                _actionStatus.value = e.localizedMessage ?: "Prompt enhancement failed."
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
        viewModelScope.launch {
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
                    e.localizedMessage ?: "AI image generation failed."
                )
            }
        }
    }

    fun useGeneratedAsReference() {
        val success = _imageUiState.value as? AiImageUiState.Success ?: return
        _referenceBitmap.value = success.bitmap
        _actionStatus.value = "Loaded generated image for further editing."
    }

    fun saveGeneratedToPictures() {
        val success = _imageUiState.value as? AiImageUiState.Success ?: return
        viewModelScope.launch {
            val res = mediaService.saveBitmapToPictures(
                success.bitmap,
                "BACKDROP_AI_${System.currentTimeMillis() % 100000}"
            )
            _actionStatus.value = if (res.isSuccess) {
                "Saved AI wallpaper to Pictures/BACKDROP"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to save image."
            }
        }
    }

    fun applyGeneratedWallpaper(target: WallpaperTarget) {
        val success = _imageUiState.value as? AiImageUiState.Success ?: return
        viewModelScope.launch {
            val res = mediaService.applyBitmapAsWallpaper(success.bitmap, target)
            _actionStatus.value = if (res.isSuccess) {
                "Applied AI wallpaper to device!"
            } else {
                res.exceptionOrNull()?.localizedMessage ?: "Failed to apply wallpaper."
            }
        }
    }

    fun analyzeCurrentBitmap() {
        val bmp = _analysisBitmap.value ?: return
        viewModelScope.launch {
            _analysisUiState.value = AiAnalysisUiState.Loading
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
                    e.localizedMessage ?: "Photo analysis failed."
                )
            }
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
        _chatMessages.value = _chatMessages.value + userMsg + botPlaceholder
        _isChatSending.value = true

        viewModelScope.launch {
            val sb = StringBuilder()
            val collectedSources = linkedMapOf<String, GroundingSource>()
            try {
                aiRepo.sendCuratorMessageStream(chatSession, clean).collect { chunk ->
                    sb.append(chunk.deltaText)
                    chunk.sources.forEach { src -> collectedSources[src.uri] = src }
                    _chatMessages.value = _chatMessages.value.map { msg ->
                        if (msg.id == botId) {
                            msg.copy(
                                text = sb.toString(),
                                sources = collectedSources.values.toList(),
                                isStreaming = true
                            )
                        } else msg
                    }
                }
                val finalText = sb.toString().ifBlank { "I couldn't generate a response for that." }
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == botId) msg.copy(text = finalText, isStreaming = false) else msg
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Curator stream timed out", e)
                _chatError.value = "Curator timed out after 60 seconds."
                removeStreamingPlaceholder(botId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Curator stream failed", e)
                _chatError.value = e.localizedMessage ?: "Curator chat failed."
                removeStreamingPlaceholder(botId)
            } finally {
                _isChatSending.value = false
            }
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
