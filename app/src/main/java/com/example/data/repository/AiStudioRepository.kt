package com.example.data.repository

import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.scale
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AspectRatio
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.ImageConfig
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ImageSize
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

enum class AiImageModelTier(val label: String, val modelId: String, val subtitle: String) {
    NANO_BANANA("Nano Banana 2.1", "gemini-nano-banana-2.1", "Fast creative synthesis & remixing"),
    PRO_STUDIO("Pro Studio 3", "gemini-3-pro-image", "Studio-grade textures & 4K fidelity")
}

enum class AiImageResolution(val label: String, val sdkSize: ImageSize) {
    RES_1K("1K", ImageSize.SIZE_1K),
    RES_2K("2K", ImageSize.SIZE_2K),
    RES_4K("4K", ImageSize.SIZE_4K)
}

enum class AiAspectRatio(val label: String, val sdkRatio: AspectRatio) {
    RATIO_1_1("1:1", AspectRatio.SQUARE_1x1),
    RATIO_2_3("2:3", AspectRatio.PORTRAIT_2x3),
    RATIO_3_2("3:2", AspectRatio.LANDSCAPE_3x2),
    RATIO_3_4("3:4", AspectRatio.PORTRAIT_3x4),
    RATIO_4_3("4:3", AspectRatio.LANDSCAPE_4x3),
    RATIO_9_16("9:16", AspectRatio.PORTRAIT_9x16),
    RATIO_16_9("16:9", AspectRatio.LANDSCAPE_16x9),
    RATIO_21_9("21:9", AspectRatio.LANDSCAPE_21x9)
}

data class PhotoAnalysisResult(
    val title: String,
    val moodSummary: String,
    val dominantHexColors: List<String>,
    val depthReadinessScore: Int,
    val depthGuidance: String,
    val suggestedRemixPrompt: String
)

data class GroundingSource(
    val title: String,
    val uri: String
)

data class CuratorStreamChunk(
    val deltaText: String,
    val sources: List<GroundingSource>
)

data class AiHealthReport(
    val healthy: Boolean,
    val activeModel: String,
    val latencyMs: Long,
    val message: String
)

class AiStudioRepository {

    private val analysisSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "title" to Schema.string(description = "Short editorial title for the image (2-5 words)"),
                "moodSummary" to Schema.string(description = "Concise visual and lighting critique (2 sentences)"),
                "dominantHexColors" to Schema.array(
                    items = Schema.string(),
                    description = "3 to 5 hex color codes like #B59AE0"
                ),
                "depthReadinessScore" to Schema.integer(
                    description = "Score from 1 to 100 for lock-screen depth-effect suitability"
                ),
                "depthGuidance" to Schema.string(
                    description = "Specific observation about foreground subject separation and clock headroom"
                ),
                "suggestedRemixPrompt" to Schema.string(
                    description = "A rich wallpaper generation prompt inspired by this image"
                )
            )
        )
    }

    private val analysisModel by lazy {
        Firebase.ai.generativeModel(
            modelName = PRIMARY_GENERAL_MODEL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = analysisSchema
            }
        )
    }

    private val promptPolishModel by lazy {
        Firebase.ai.generativeModel(modelName = FAST_GENERAL_MODEL)
    }

    private val curatorSystemInstruction = content {
        text(
            "You are BACKDROP Curator, an expert art director, mobile wallpaper stylist, " +
                "and visual design advisor. Help users choose color palettes, aspect ratios, " +
                "Depth Studio lock-screen framing, and custom wallpaper prompts. Use Google " +
                "Search grounding when asked about recent design trends, phone screen specs, " +
                "or photography inspiration. Keep answers concise, warm, and actionable."
        )
    }

    private val groundedCuratorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = PRIMARY_GENERAL_MODEL,
            tools = listOf(Tool.googleSearch()),
            systemInstruction = curatorSystemInstruction
        )
    }

    suspend fun generateOrEditWallpaper(
        prompt: String,
        referenceBitmap: Bitmap?,
        modelTier: AiImageModelTier,
        resolution: AiImageResolution,
        aspectRatio: AiAspectRatio
    ): Bitmap {
        val scaledRef = if (referenceBitmap != null) {
            prepareSafeSoftwareBitmap(referenceBitmap, 1536)
        } else null

        val primaryConfig = generationConfig {
            responseModalities = listOf(ResponseModality.IMAGE, ResponseModality.TEXT)
            imageConfig = ImageConfig.builder()
                .setAspectRatio(aspectRatio.sdkRatio)
                .setImageSize(resolution.sdkSize)
                .build()
        }
        val primaryModel = Firebase.ai.generativeModel(
            modelName = modelTier.modelId,
            generationConfig = primaryConfig
        )

        return withTimeout(60_000L) {
            val response = try {
                invokeImageModel(primaryModel, prompt, scaledRef)
            } catch (e: CancellationException) {
                throw e
            } catch (primaryErr: Exception) {
                Log.w(TAG, "Primary image request failed (${modelTier.modelId}), trying fallbacks", primaryErr)
                if (!shouldAttemptFallback(primaryErr)) throw primaryErr
                try {
                    val baselineModel = Firebase.ai.generativeModel(
                        modelName = AiImageModelTier.NANO_BANANA.modelId,
                        generationConfig = generationConfig {
                            responseModalities = listOf(ResponseModality.IMAGE)
                        }
                    )
                    invokeImageModel(baselineModel, prompt, scaledRef)
                } catch (e: CancellationException) {
                    throw e
                } catch (fallbackErr: Exception) {
                    Log.w(TAG, "Baseline Nano Banana failed, trying fast image model", fallbackErr)
                    val liteImageModel = Firebase.ai.generativeModel(
                        modelName = FAST_IMAGE_MODEL,
                        generationConfig = generationConfig {
                            responseModalities = listOf(ResponseModality.IMAGE)
                        }
                    )
                    try {
                        invokeImageModel(liteImageModel, prompt, scaledRef)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (liteErr: Exception) {
                        if (modelTier == AiImageModelTier.PRO_STUDIO) throw liteErr
                        Log.w(TAG, "Fast image model failed, trying Pro Studio image model", liteErr)
                        val proFallback = Firebase.ai.generativeModel(
                            modelName = AiImageModelTier.PRO_STUDIO.modelId,
                            generationConfig = generationConfig {
                                responseModalities = listOf(ResponseModality.IMAGE)
                            }
                        )
                        invokeImageModel(proFallback, prompt, scaledRef)
                    }
                }
            }
            extractBitmapOrThrow(
                response = response,
                fallbackMessage = "Gemini did not return an image. Please refine your prompt and try again."
            )
        }
    }

    suspend fun generateOutpaintedFill(
        guideBitmap: Bitmap,
        prompt: String,
        aspectRatio: AiAspectRatio
    ): Bitmap {
        val scaledGuide = prepareSafeSoftwareBitmap(guideBitmap, 1280)
        val primaryConfig = generationConfig {
            responseModalities = listOf(ResponseModality.IMAGE, ResponseModality.TEXT)
            imageConfig = ImageConfig.builder()
                .setAspectRatio(aspectRatio.sdkRatio)
                .setImageSize(ImageSize.SIZE_2K)
                .build()
        }
        val primaryModel = Firebase.ai.generativeModel(
            modelName = AiImageModelTier.NANO_BANANA.modelId,
            generationConfig = primaryConfig
        )

        return withTimeout(60_000L) {
            val response = try {
                invokeImageModel(primaryModel, prompt, scaledGuide)
            } catch (e: CancellationException) {
                throw e
            } catch (primaryErr: Exception) {
                Log.w(TAG, "Primary outpaint request failed, trying fallbacks", primaryErr)
                if (!shouldAttemptFallback(primaryErr)) throw primaryErr
                try {
                    val baselineModel = Firebase.ai.generativeModel(
                        modelName = AiImageModelTier.NANO_BANANA.modelId,
                        generationConfig = generationConfig {
                            responseModalities = listOf(ResponseModality.IMAGE)
                        }
                    )
                    invokeImageModel(baselineModel, prompt, scaledGuide)
                } catch (e: CancellationException) {
                    throw e
                } catch (fallbackErr: Exception) {
                    val liteImageModel = Firebase.ai.generativeModel(
                        modelName = FAST_IMAGE_MODEL,
                        generationConfig = generationConfig {
                            responseModalities = listOf(ResponseModality.IMAGE)
                        }
                    )
                    invokeImageModel(liteImageModel, prompt, scaledGuide)
                }
            }
            extractBitmapOrThrow(
                response = response,
                fallbackMessage = "Gemini did not return an extended image. Adjust framing or try another cue."
            )
        }
    }

    private suspend fun invokeImageModel(
        model: GenerativeModel,
        prompt: String,
        imageBitmap: Bitmap?
    ): GenerateContentResponse {
        return if (imageBitmap != null) {
            model.generateContent(
                content {
                    image(imageBitmap)
                    text(prompt)
                }
            )
        } else {
            model.generateContent(prompt)
        }
    }

    private fun extractBitmapOrThrow(
        response: GenerateContentResponse,
        fallbackMessage: String
    ): Bitmap {
        val parts = response.candidates.firstOrNull()?.content?.parts.orEmpty()
        val imagePart = parts.filterIsInstance<ImagePart>().firstOrNull()
        if (imagePart != null) return imagePart.image

        val refusalNote = parts.filterIsInstance<TextPart>()
            .joinToString(" ") { it.text.trim() }
            .trim()
        throw IllegalStateException(
            if (refusalNote.isNotBlank()) refusalNote else fallbackMessage
        )
    }

    internal fun shouldAttemptFallback(err: Throwable): Boolean {
        val msg = buildErrorChainText(err).lowercase()
        val isNet = msg.contains("unknownhost") ||
            msg.contains("unable to resolve host") ||
            msg.contains("network is unreachable")
        return !isNet
    }

    suspend fun analyzePhoto(bitmap: Bitmap): PhotoAnalysisResult {
        val scaled = prepareSafeSoftwareBitmap(bitmap, 1280)
        val promptText = "Analyze this image as a mobile wallpaper candidate for BACKDROP. " +
            "Identify its mood, dominant hex colors (#RRGGBB), lock-screen depth separation quality, " +
            "and craft a detailed remix prompt."
        val jsonFallbackPrompt = "Return ONLY a valid JSON object (no markdown fences) with keys: " +
            "title (string), moodSummary (string), dominantHexColors (array of #RRGGBB strings), " +
            "depthReadinessScore (int 1..100), depthGuidance (string), suggestedRemixPrompt (string)."

        return withTimeout(60_000L) {
            var lastError: Exception? = null
            // Attempt 1: Structured JSON schema on primary model
            try {
                val primaryResponse = analysisModel.generateContent(
                    content {
                        image(scaled)
                        text(promptText)
                    }
                )
                val primaryText = primaryResponse.text
                if (!primaryText.isNullOrBlank()) {
                    return@withTimeout parsePhotoAnalysisJson(primaryText)
                }
                lastError = IllegalStateException("Primary model returned empty analysis text.")
            } catch (e: CancellationException) {
                throw e
            } catch (primaryErr: Exception) {
                Log.w(TAG, "Primary schema analysis failed, trying fallback models", primaryErr)
                if (!shouldAttemptFallback(primaryErr)) throw primaryErr
                lastError = primaryErr
            }

            for (candidateModelId in GENERAL_MODEL_FALLBACK_CHAIN) {
                try {
                    val jsonModel = Firebase.ai.generativeModel(
                        modelName = candidateModelId,
                        generationConfig = generationConfig {
                            responseMimeType = "application/json"
                        }
                    )
                    val r = jsonModel.generateContent(
                        content {
                            image(scaled)
                            text(jsonFallbackPrompt)
                        }
                    )
                    val candidateText = r.text
                    if (!candidateText.isNullOrBlank()) {
                        return@withTimeout parsePhotoAnalysisJson(candidateText)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (candidateErr: Exception) {
                    lastError = candidateErr
                    Log.w(TAG, "Analysis fallback ($candidateModelId) failed", candidateErr)
                    if (!shouldAttemptFallback(candidateErr)) throw candidateErr
                }
            }
            throw (lastError ?: IllegalStateException("Gemini returned an empty analysis response."))
        }
    }

    private fun parsePhotoAnalysisJson(rawText: String): PhotoAnalysisResult {
        val jsonCandidate = extractJsonObjectString(rawText)
        val json = JSONObject(jsonCandidate)
        val colorsArray = json.optJSONArray("dominantHexColors")
        val hexList = buildList {
            if (colorsArray != null) {
                for (i in 0 until colorsArray.length()) {
                    val raw = colorsArray.optString(i).trim()
                    val normalized = if (raw.startsWith("#")) raw else "#$raw"
                    if (HEX_COLOR_REGEX.matches(normalized)) {
                        add(normalized.uppercase())
                    }
                }
            }
        }
        return PhotoAnalysisResult(
            title = json.optString("title", "Visual Study").ifBlank { "Visual Study" },
            moodSummary = json.optString("moodSummary", ""),
            dominantHexColors = hexList,
            depthReadinessScore = json.optInt("depthReadinessScore", 70).coerceIn(1, 100),
            depthGuidance = json.optString("depthGuidance", ""),
            suggestedRemixPrompt = json.optString("suggestedRemixPrompt", "")
        )
    }

    suspend fun probeAiConnection(): AiHealthReport {
        val started = System.currentTimeMillis()
        return withTimeout(15_000L) {
            var lastError: Exception? = null
            for (candidateModelId in GENERAL_MODEL_FALLBACK_CHAIN) {
                try {
                    val model = Firebase.ai.generativeModel(modelName = candidateModelId)
                    val res = model.generateContent("Reply with only: OK")
                    if (!res.text.isNullOrBlank()) {
                        val elapsed = (System.currentTimeMillis() - started).coerceAtLeast(1L)
                        return@withTimeout AiHealthReport(
                            healthy = true,
                            activeModel = candidateModelId,
                            latencyMs = elapsed,
                            message = "Connected to $candidateModelId in ${elapsed}ms (App Check active)."
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    lastError = e
                    if (!shouldAttemptFallback(e)) break
                }
            }
            val elapsed = (System.currentTimeMillis() - started).coerceAtLeast(1L)
            AiHealthReport(
                healthy = false,
                activeModel = PRIMARY_GENERAL_MODEL,
                latencyMs = elapsed,
                message = lastError?.let {
                    formatActionableError(it, "Could not reach Firebase AI Logic.")
                } ?: "Gemini did not respond."
            )
        }
    }

    suspend fun polishPrompt(seedPrompt: String): String = withTimeout(30_000L) {
        val instruction = "Expand this wallpaper idea into a vivid, concise 1-sentence art direction prompt " +
            "with lighting, color palette, and clean composition (no text in image): $seedPrompt"
        var lastError: Exception? = null
        try {
            val response = promptPolishModel.generateContent(instruction)
            val text = response.text?.trim()?.takeIf { it.isNotEmpty() }
            if (text != null) return@withTimeout text
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            lastError = e
            Log.w(TAG, "Fast polish model failed, trying fallbacks", e)
            if (!shouldAttemptFallback(e)) throw e
        }

        for (candidateModelId in GENERAL_MODEL_FALLBACK_CHAIN) {
            try {
                val model = Firebase.ai.generativeModel(modelName = candidateModelId)
                val response = model.generateContent(instruction)
                val text = response.text?.trim()?.takeIf { it.isNotEmpty() }
                if (text != null) return@withTimeout text
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw (lastError ?: IllegalStateException("Could not enhance prompt."))
    }

    fun startCuratorChat(
        useGrounding: Boolean = true,
        modelNameOverride: String? = null,
        extraHistory: List<Content> = emptyList()
    ): Chat {
        val seedHistory = listOf(
            content("user") { text("Introduce yourself briefly as the BACKDROP Curator.") },
            content("model") {
                text(
                    "Welcome to BACKDROP Studio. Ask me for color palette ideas, lock-screen " +
                        "depth framing tips, current visual design trends, or bespoke wallpaper prompts."
                )
            }
        ) + extraHistory
        val targetModel = when {
            modelNameOverride != null -> Firebase.ai.generativeModel(
                modelName = modelNameOverride,
                systemInstruction = curatorSystemInstruction
            )
            useGrounding -> groundedCuratorModel
            else -> Firebase.ai.generativeModel(
                modelName = PRIMARY_GENERAL_MODEL,
                systemInstruction = curatorSystemInstruction
            )
        }
        return targetModel.startChat(history = seedHistory)
    }

    fun sendCuratorMessageStream(chat: Chat, userMessage: String): Flow<CuratorStreamChunk> = flow {
        withTimeout(60_000L) {
            chat.sendMessageStream(userMessage).collect { chunk ->
                val sources = chunk.candidates.firstOrNull()
                    ?.groundingMetadata
                    ?.groundingChunks
                    ?.mapNotNull { gc ->
                        val web = gc.web
                        if (web != null && !web.uri.isNullOrBlank()) {
                            GroundingSource(
                                title = web.title?.takeIf { it.isNotBlank() } ?: web.domain ?: "Source",
                                uri = web.uri.orEmpty()
                            )
                        } else null
                    }
                    .orEmpty()
                emit(
                    CuratorStreamChunk(
                        deltaText = chunk.text.orEmpty(),
                        sources = sources
                    )
                )
            }
        }
    }

    fun formatActionableError(error: Throwable, fallback: String): String {
        val chain = buildErrorChainText(error)
        val lower = chain.lowercase()
        return when {
            lower.contains("app check") || lower.contains("appcheck") || lower.contains("attestation") ->
                "App Check attestation failed (403). Launch from the emulator or rebuild after Firebase provisioning. (${error.localizedMessage ?: "403"})"
            lower.contains("unknownhost") || lower.contains("unable to resolve host") || lower.contains("failed to connect") ->
                "Network connection unavailable. Check internet access and retry."
            lower.contains("billing") || lower.contains("blaze") ->
                "This Gemini model requires Firebase Blaze billing on the project. (${error.localizedMessage ?: "Billing required"})"
            else -> error.localizedMessage?.takeIf { it.isNotBlank() } ?: fallback
        }
    }

    private fun buildErrorChainText(error: Throwable): String = buildString {
        var cur: Throwable? = error
        var depth = 0
        while (cur != null && depth < 6) {
            append(cur.javaClass.simpleName)
            append(": ")
            append(cur.message.orEmpty())
            append(" | ")
            cur = cur.cause
            depth++
        }
    }

    private fun extractJsonObjectString(raw: String): String {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        return if (start >= 0 && end > start) raw.substring(start, end + 1) else raw.trim()
    }

    private suspend fun prepareSafeSoftwareBitmap(bitmap: Bitmap, maxEdge: Int): Bitmap =
        withContext(Dispatchers.Default) {
            require(!bitmap.isRecycled) { "Source bitmap was recycled before AI processing." }
            val safeBitmap = if (
                (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                    bitmap.config == Bitmap.Config.HARDWARE) ||
                bitmap.config != Bitmap.Config.ARGB_8888
            ) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
            } else {
                bitmap
            }
            val w = safeBitmap.width.coerceAtLeast(1)
            val h = safeBitmap.height.coerceAtLeast(1)
            val maxCurrent = maxOf(w, h)
            if (maxCurrent <= maxEdge) return@withContext safeBitmap
            val ratio = maxEdge.toFloat() / maxCurrent.toFloat()
            val targetW = (w * ratio).toInt().coerceAtLeast(1)
            val targetH = (h * ratio).toInt().coerceAtLeast(1)
            val scaled = safeBitmap.scale(targetW, targetH)
            if (safeBitmap !== bitmap && safeBitmap !== scaled) {
                safeBitmap.recycle()
            }
            scaled
        }

    companion object {
        private const val TAG = "AiStudioRepo"
        const val PRIMARY_GENERAL_MODEL = "gemini-3.8-flash"
        const val FAST_GENERAL_MODEL = "gemini-3.5-flash-lite"
        const val FAST_IMAGE_MODEL = "gemini-3.1-flash-lite-image"
        val GENERAL_MODEL_FALLBACK_CHAIN = listOf(
            "gemini-3.8-flash",
            "gemini-3.5-flash-lite",
            "gemini-4-argon"
        )
        private val HEX_COLOR_REGEX = Regex("^#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?$")
    }
}
