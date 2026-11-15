package com.example.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AspectRatio
import com.google.firebase.ai.type.ImageConfig
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ImageSize
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = analysisSchema
            }
        )
    }

    private val promptPolishModel by lazy {
        Firebase.ai.generativeModel(modelName = "gemini-3.5-flash-lite")
    }

    private val curatorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            tools = listOf(Tool.googleSearch()),
            systemInstruction = content {
                text(
                    "You are BACKDROP Curator, an expert art director, mobile wallpaper stylist, " +
                        "and visual design advisor. Help users choose color palettes, aspect ratios, " +
                        "Depth Studio lock-screen framing, and custom wallpaper prompts. Use Google " +
                        "Search grounding when asked about recent design trends, phone screen specs, " +
                        "or photography inspiration. Keep answers concise, warm, and actionable."
                )
            }
        )
    }

    suspend fun generateOrEditWallpaper(
        prompt: String,
        referenceBitmap: Bitmap?,
        modelTier: AiImageModelTier,
        resolution: AiImageResolution,
        aspectRatio: AiAspectRatio
    ): Bitmap {
        val config = generationConfig {
            responseModalities = listOf(ResponseModality.IMAGE)
            imageConfig = ImageConfig.builder()
                .setAspectRatio(aspectRatio.sdkRatio)
                .setImageSize(resolution.sdkSize)
                .build()
        }
        val model = Firebase.ai.generativeModel(
            modelName = modelTier.modelId,
            generationConfig = config
        )
        return withTimeout(60_000L) {
            val response = if (referenceBitmap != null) {
                val scaled = scaleBitmapIfNeeded(referenceBitmap, 1536)
                model.generateContent(
                    content {
                        image(scaled)
                        text(prompt)
                    }
                )
            } else {
                model.generateContent(prompt)
            }
            val imagePart = response.candidates.firstOrNull()
                ?.content?.parts?.filterIsInstance<ImagePart>()
                ?.firstOrNull()
            imagePart?.image ?: throw IllegalStateException(
                "Gemini did not return an image. Please refine your prompt and try again."
            )
        }
    }

    suspend fun analyzePhoto(bitmap: Bitmap): PhotoAnalysisResult {
        val scaled = scaleBitmapIfNeeded(bitmap, 1280)
        return withTimeout(60_000L) {
            val response = analysisModel.generateContent(
                content {
                    image(scaled)
                    text(
                        "Analyze this image as a mobile wallpaper candidate for BACKDROP. " +
                            "Identify its mood, dominant hex colors, lock-screen depth separation quality, " +
                            "and craft a detailed remix prompt."
                    )
                }
            )
            val jsonText = response.text ?: throw IllegalStateException("Empty analysis returned.")
            val json = JSONObject(jsonText)
            val colorsArray = json.optJSONArray("dominantHexColors")
            val hexList = buildList {
                if (colorsArray != null) {
                    for (i in 0 until colorsArray.length()) {
                        add(colorsArray.optString(i))
                    }
                }
            }.filter { it.isNotBlank() }

            PhotoAnalysisResult(
                title = json.optString("title", "Visual Study"),
                moodSummary = json.optString("moodSummary", ""),
                dominantHexColors = hexList,
                depthReadinessScore = json.optInt("depthReadinessScore", 70).coerceIn(1, 100),
                depthGuidance = json.optString("depthGuidance", ""),
                suggestedRemixPrompt = json.optString("suggestedRemixPrompt", "")
            )
        }
    }

    suspend fun polishPrompt(seedPrompt: String): String = withTimeout(30_000L) {
        val response = promptPolishModel.generateContent(
            "Expand this wallpaper idea into a vivid, concise 1-sentence art direction prompt " +
                "with lighting, color palette, and clean composition (no text in image): $seedPrompt"
        )
        response.text?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("Could not enhance prompt.")
    }

    fun startCuratorChat(): Chat {
        return curatorModel.startChat(
            history = listOf(
                content("user") { text("Introduce yourself briefly as the BACKDROP Curator.") },
                content("model") {
                    text(
                        "Welcome to BACKDROP Studio. Ask me for color palette ideas, lock-screen " +
                            "depth framing tips, current visual design trends, or bespoke wallpaper prompts."
                    )
                }
            )
        )
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

    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxEdge: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val maxCurrent = maxOf(w, h)
        if (maxCurrent <= maxEdge) return bitmap
        val ratio = maxEdge.toFloat() / maxCurrent.toFloat()
        val targetW = (w * ratio).toInt().coerceAtLeast(1)
        val targetH = (h * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    companion object {
        private const val TAG = "AiStudioRepo"
    }
}
