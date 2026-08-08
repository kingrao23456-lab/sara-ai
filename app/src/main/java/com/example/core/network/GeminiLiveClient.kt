package com.example.core.network

import android.util.Base64
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Real-time, low-latency, speech-to-speech client for the Gemini Live API
 * (BidiGenerateContent over WebSocket). This is a genuine duplex audio
 * connection, NOT a Speech-to-Text -> text-model -> Text-to-Speech pipeline.
 *
 * Protocol reference: https://ai.google.dev/gemini-api/docs/live-api
 *
 * Audio contract:
 *  - You SEND raw 16-bit PCM, 16kHz, mono, little-endian audio chunks.
 *  - You RECEIVE raw 16-bit PCM, 24kHz, mono, little-endian audio chunks.
 *
 * NOTE ON MODEL NAME: Live-API model ids change fairly often as Google
 * rolls out new versions, and availability can vary per API key/project.
 * If connection fails with a "not found / not supported for
 * bidiGenerateContent" error, open Google AI Studio -> check which Live
 * model your key has access to, and update [LIVE_MODEL] below.
 */
class GeminiLiveClient(
    private val apiKey: String,
    private val modelName: String = LIVE_MODEL
) {
    companion object {
        private const val TAG = "GeminiLiveClient"
        const val LIVE_MODEL = "gemini-live-2.5-flash-native-audio"
        // Tried in order by LiveVoiceManager if the primary model's session
        // never confirms setup (e.g. not enabled for this API key/project yet).
        val FALLBACK_MODELS = listOf(
            "gemini-live-2.5-flash-native-audio",
            "gemini-3.1-flash-live-preview",
            "gemini-2.0-flash-exp"
        )
        private const val WS_URL =
            "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"
    }

    interface Listener {
        fun onOpen() {}
        fun onSetupComplete() {}
        /** Raw PCM16 24kHz mono audio bytes from the model. */
        fun onAudioChunk(pcm: ByteArray) {}
        fun onInputTranscript(text: String) {}
        fun onOutputTranscript(text: String) {}
        fun onTurnComplete() {}
        /** Model detected the user barging in; stop playback immediately. */
        fun onInterrupted() {}
        fun onError(message: String) {}
        fun onClosed() {}
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var listener: Listener? = null
    @Volatile private var setupSent = false
    @Volatile private var setupCompleted = false
    @Volatile var isConnected: Boolean = false
        private set

    private val timeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val setupTimeoutRunnable = Runnable {
        if (!setupCompleted) {
            Log.e(TAG, "Live API setup timed out (no setupComplete received)")
            listener?.onError("Live session timed out \u2014 server never confirmed setup. The Live model may not be available for your API key.")
            close()
        }
    }

    fun connect(
        systemInstruction: String,
        voiceName: String = "Kore",
        listener: Listener
    ) {
        this.listener = listener
        setupCompleted = false
        val request = Request.Builder()
            .url("$WS_URL?key=$apiKey")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                listener.onOpen()
                sendSetup(webSocket, systemInstruction, voiceName)
                timeoutHandler.postDelayed(setupTimeoutRunnable, 15000L)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleServerMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                timeoutHandler.removeCallbacks(setupTimeoutRunnable)
                Log.e(TAG, "Live API connection failed", t)
                val body = try { response?.body?.string() } catch (e: Exception) { null }
                listener.onError((t.message ?: "Unknown WebSocket error") + (body?.let { " | $it" } ?: ""))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                timeoutHandler.removeCallbacks(setupTimeoutRunnable)
                listener.onClosed()
            }
        })
    }

    private fun sendSetup(ws: WebSocket, systemInstruction: String, voiceName: String) {
        try {
            val setup = JSONObject().apply {
                put("setup", JSONObject().apply {
                    put("model", "models/$modelName")
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().put("AUDIO"))
                        put("speechConfig", JSONObject().apply {
                            put("voiceConfig", JSONObject().apply {
                                put("prebuiltVoiceConfig", JSONObject().apply {
                                    put("voiceName", voiceName)
                                })
                            })
                        })
                    })
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                    })
                    // Ask the server to also transcribe both sides as text,
                    // useful for showing a live transcript in the UI.
                    put("inputAudioTranscription", JSONObject())
                    put("outputAudioTranscription", JSONObject())
                })
            }
            ws.send(setup.toString())
            setupSent = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build/send Live API setup message", e)
            listener?.onError("Failed to start live session: ${e.message}")
        }
    }

    /** Send one chunk of raw PCM16/16kHz/mono microphone audio. */
    fun sendAudioChunk(pcm16Mono16k: ByteArray) {
        val ws = webSocket ?: return
        if (!setupSent) return
        val b64 = Base64.encodeToString(pcm16Mono16k, Base64.NO_WRAP)
        val msg = JSONObject().apply {
            put("realtimeInput", JSONObject().apply {
                put("audio", JSONObject().apply {
                    put("data", b64)
                    put("mimeType", "audio/pcm;rate=16000")
                })
            })
        }
        ws.send(msg.toString())
    }

    /** Send a text message into the same live session (e.g. typed fallback). */
    fun sendText(text: String) {
        val ws = webSocket ?: return
        val msg = JSONObject().apply {
            put("clientContent", JSONObject().apply {
                put("turns", JSONArray().put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", text)))
                }))
                put("turnComplete", true)
            })
        }
        ws.send(msg.toString())
    }

    private fun handleServerMessage(text: String) {
        val l = listener ?: return
        try {
            val json = JSONObject(text)
            when {
                json.has("setupComplete") -> {
                    setupCompleted = true
                    timeoutHandler.removeCallbacks(setupTimeoutRunnable)
                    l.onSetupComplete()
                }
                json.has("serverContent") -> {
                    val sc = json.getJSONObject("serverContent")

                    if (sc.optBoolean("interrupted", false)) {
                        l.onInterrupted()
                    }

                    sc.optJSONObject("modelTurn")?.let { modelTurn ->
                        val parts = modelTurn.optJSONArray("parts")
                        if (parts != null) {
                            for (i in 0 until parts.length()) {
                                val part = parts.getJSONObject(i)
                                val inlineData = part.optJSONObject("inlineData")
                                if (inlineData != null) {
                                    val mime = inlineData.optString("mimeType", "")
                                    if (mime.startsWith("audio/")) {
                                        val bytes = Base64.decode(
                                            inlineData.getString("data"),
                                            Base64.NO_WRAP
                                        )
                                        l.onAudioChunk(bytes)
                                    }
                                }
                                part.optString("text", "").takeIf { it.isNotEmpty() }?.let {
                                    l.onOutputTranscript(it)
                                }
                            }
                        }
                    }

                    sc.optJSONObject("inputTranscription")?.optString("text")?.let {
                        if (it.isNotEmpty()) l.onInputTranscript(it)
                    }
                    sc.optJSONObject("outputTranscription")?.optString("text")?.let {
                        if (it.isNotEmpty()) l.onOutputTranscript(it)
                    }

                    if (sc.optBoolean("turnComplete", false)) {
                        l.onTurnComplete()
                    }
                }
                json.has("error") -> {
                    l.onError(json.optJSONObject("error")?.optString("message") ?: "Live API error")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Live API message", e)
        }
    }

    fun close() {
        setupSent = false
        timeoutHandler.removeCallbacks(setupTimeoutRunnable)
        webSocket?.close(1000, "Client closed session")
        webSocket = null
        isConnected = false
    }
}
