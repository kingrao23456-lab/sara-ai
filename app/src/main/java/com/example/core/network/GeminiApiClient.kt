package com.example.core.network

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiApiClient {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    // Only models that are actually valid for the v1beta generateContent endpoint.
    // Any unrecognized/typo'd model id coming from the UI is safely mapped to the default.
    const val DEFAULT_MODEL = "gemini-3.6-flash"
    private val VALID_MODELS = setOf(
        "gemini-3.6-flash",
        "gemini-3.5-flash-lite",
        "gemini-2.5-pro",
        "gemini-2.5-flash"
    )

    fun normalizeModelId(requested: String?): String {
        if (requested == null) return DEFAULT_MODEL
        return if (VALID_MODELS.contains(requested)) requested else DEFAULT_MODEL
    }

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /** True Gemini API key currently configured (from local.properties/.env -> BuildConfig). */
    fun hasApiKey(): Boolean = getApiKey().isNotEmpty()

    fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key.isNullOrBlank() || key == "MY_GEMINI_API_KEY") "" else key
    }

    /**
     * Generate response with AI personality, search/maps grounding, memory context, and optional image.
     * Any requested model id is normalized against the real list of supported models, so a stale/typo'd
     * id from the UI can never break the request.
     */
    suspend fun generateAiResponse(
        prompt: String,
        personalitySystemInstruction: String,
        memoryContext: String = "",
        language: String = "English",
        modelName: String = DEFAULT_MODEL,
        enableSearchGrounding: Boolean = false,
        enableMapsGrounding: Boolean = false,
        imageBase64: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext "⚠️ Gemini API key not configured. Add GEMINI_API_KEY to your local .env file and rebuild the app."
        }

        val fullSystemInstructionText = buildString {
            append(personalitySystemInstruction)
            append("\n\nPreferred Language: ").append(language)
            if (memoryContext.isNotEmpty()) {
                append("\n\nRelevant Long-Term Memory & User Facts:\n").append(memoryContext)
            }
        }

        val userParts = mutableListOf<GeminiPart>()
        userParts.add(GeminiPart(text = prompt))
        if (!imageBase64.isNullOrEmpty()) {
            userParts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = imageBase64)))
        }

        val toolsList = mutableListOf<Map<String, Any>>()
        if (enableSearchGrounding) {
            toolsList.add(mapOf("googleSearch" to emptyMap<String, Any>()))
        }
        if (enableMapsGrounding) {
            toolsList.add(mapOf("googleMaps" to emptyMap<String, Any>()))
        }

        val request = GeminiRequest(
            contents = listOf(GeminiContent(role = "user", parts = userParts)),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = fullSystemInstructionText))),
            tools = if (toolsList.isNotEmpty()) toolsList else null
        )

        try {
            val response = apiService.generateContent(
                model = normalizeModelId(modelName),
                apiKey = apiKey,
                request = request
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "⚠️ Gemini returned an empty response (it may have been blocked by safety filters). Try rephrasing."
        } catch (e: retrofit2.HttpException) {
            val errorBody = try { e.response()?.errorBody()?.string() } catch (ex: Exception) { null }
            "⚠️ Gemini API error (HTTP ${e.code()}): ${errorBody?.take(200) ?: e.message()}"
        } catch (e: IOException) {
            "⚠️ Network error talking to Gemini: ${e.message ?: "check your internet connection"}"
        } catch (e: Exception) {
            "⚠️ Unexpected error calling Gemini: ${e.message}"
        }
    }

    /**
     * Turn-based voice helper (STT text in -> Gemini text out -> TTS speaks it).
     * For genuine real-time duplex speech-to-speech, use [GeminiLiveClient] + LiveVoiceManager instead.
     */
    suspend fun generateVoiceResponse(
        prompt: String,
        personalitySystemInstruction: String,
        voiceName: String = "Kore",
        memoryContext: String = "",
        language: String = "English",
        isNativeAudioMode: Boolean = true
    ): String = withContext(Dispatchers.IO) {
        val fullSystemInstructionText = buildString {
            append(personalitySystemInstruction)
            append("\n\nRespond concisely, naturally, and warmly in spoken voice conversation style (1 to 3 short sentences).")
            append("\nPreferred Language: ").append(language)
            if (memoryContext.isNotEmpty()) {
                append("\n\nRelevant Long-Term Memory Context:\n").append(memoryContext)
            }
        }

        generateAiResponse(
            prompt = prompt,
            personalitySystemInstruction = fullSystemInstructionText,
            memoryContext = memoryContext,
            language = language,
            modelName = DEFAULT_MODEL
        )
    }
}
