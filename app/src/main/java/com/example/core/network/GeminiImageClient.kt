package com.example.core.network

import android.util.Base64
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Calls Gemini's real image generation model (gemini-3.1-flash-image) and
 * returns the generated image as raw bytes. Used by ImageGenScreen, which
 * previously only faked generation with a delay and a static placeholder.
 */
object GeminiImageClient {

    private const val TAG = "GeminiImageClient"
    private const val IMAGE_MODEL = "gemini-3.1-flash-image"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /** Returns the generated image bytes (PNG/JPEG), or a failure with a human-readable message. */
    fun generateImage(prompt: String, style: String, aspectRatio: String, apiKey: String): Result<ByteArray> {
        if (apiKey.isEmpty()) {
            return Result.failure(IllegalStateException("Gemini API key not configured. Add GEMINI_API_KEY to your .env and rebuild."))
        }

        val fullPrompt = "Create a $style style image, aspect ratio $aspectRatio: $prompt"

        val body = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(
                    JSONObject().put("text", fullPrompt)
                ))
            ))
            put("generationConfig", JSONObject().apply {
                put("responseModalities", JSONArray().put("IMAGE"))
            })
        }

        return try {
            val request = Request.Builder()
                .url("$BASE_URL/$IMAGE_MODEL:generateContent?key=$apiKey")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    Log.e(TAG, "Image generation failed: HTTP ${response.code} $responseBody")
                    return Result.failure(Exception("Gemini image API error (HTTP ${response.code}): ${responseBody.take(300)}"))
                }

                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                val parts = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val inlineData = parts.getJSONObject(i).optJSONObject("inlineData")
                        if (inlineData != null) {
                            val data = inlineData.getString("data")
                            return Result.success(Base64.decode(data, Base64.DEFAULT))
                        }
                    }
                }
                Result.failure(Exception("Gemini didn't return an image (it may have declined the prompt). Try rephrasing."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Image generation request failed", e)
            Result.failure(e)
        }
    }
}
