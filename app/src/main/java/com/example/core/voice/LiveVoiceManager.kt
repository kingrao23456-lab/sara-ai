package com.example.core.voice

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import com.example.core.network.GeminiLiveClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Drives a real, continuous, duplex voice conversation with Gemini:
 * mic -> [GeminiLiveClient] -> speaker, streamed both ways in real time.
 * This replaces the old "record -> SpeechRecognizer -> TTS" turn-based flow
 * for true Live/voice-to-voice mode.
 */
class LiveVoiceManager {

    enum class CallState { IDLE, CONNECTING, LISTENING, USER_SPEAKING, MODEL_SPEAKING, ERROR }

    companion object {
        private const val TAG = "LiveVoiceManager"
        private const val INPUT_SAMPLE_RATE = 16000
        private const val OUTPUT_SAMPLE_RATE = 24000
    }

    private var liveClient: GeminiLiveClient? = null
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var scope: CoroutineScope? = null
    private var captureJob: Job? = null

    var onStateChange: ((CallState) -> Unit)? = null
    var onOutputTranscript: ((String) -> Unit)? = null
    var onInputTranscript: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    private var state: CallState = CallState.IDLE
        set(value) {
            field = value
            onStateChange?.invoke(value)
        }

    @SuppressLint("MissingPermission") // caller must have already checked RECORD_AUDIO
    fun start(
        context: Context,
        apiKey: String,
        systemInstruction: String,
        voiceName: String = "Kore"
    ) {
        if (state != CallState.IDLE) return
        state = CallState.CONNECTING

        val supervisor = SupervisorJob()
        val newScope = CoroutineScope(Dispatchers.IO + supervisor)
        scope = newScope

        attemptConnect(
            context = context,
            apiKey = apiKey,
            systemInstruction = systemInstruction,
            voiceName = voiceName,
            scope = newScope,
            modelIndex = 0
        )
    }

    /**
     * Tries each model in [GeminiLiveClient.FALLBACK_MODELS] in order.
     * If a model's session never confirms setup (not enabled for this key,
     * or briefly unavailable), we silently move to the next candidate
     * instead of surfacing a confusing error for a model the user never chose.
     */
    private fun attemptConnect(
        context: Context,
        apiKey: String,
        systemInstruction: String,
        voiceName: String,
        scope: CoroutineScope,
        modelIndex: Int
    ) {
        val models = GeminiLiveClient.FALLBACK_MODELS
        if (modelIndex >= models.size) {
            onError?.invoke("Couldn't start a live voice session with any available model. Your API key may not have Live API access yet.")
            state = CallState.ERROR
            return
        }
        val model = models[modelIndex]
        Log.d(TAG, "Attempting live connection with model: $model")

        val client = GeminiLiveClient(apiKey, model)
        liveClient = client

        client.connect(
            systemInstruction = systemInstruction,
            voiceName = voiceName,
            listener = object : GeminiLiveClient.Listener {
                override fun onOpen() {
                    Log.d(TAG, "Live session opened ($model)")
                }

                override fun onSetupComplete() {
                    setupAudioTrack(context)
                    startMicCapture(scope)
                    state = CallState.LISTENING
                }

                override fun onAudioChunk(pcm: ByteArray) {
                    state = CallState.MODEL_SPEAKING
                    audioTrack?.write(pcm, 0, pcm.size)
                }

                override fun onInputTranscript(text: String) {
                    onInputTranscript?.invoke(text)
                }

                override fun onOutputTranscript(text: String) {
                    onOutputTranscript?.invoke(text)
                }

                override fun onTurnComplete() {
                    state = CallState.LISTENING
                }

                override fun onInterrupted() {
                    // Barge-in: flush whatever audio is still queued for playback.
                    audioTrack?.pause()
                    audioTrack?.flush()
                    audioTrack?.play()
                    state = CallState.LISTENING
                }

                override fun onError(message: String) {
                    Log.e(TAG, "Live API error on $model: $message")
                    val alreadyListening = state == CallState.LISTENING || state == CallState.MODEL_SPEAKING
                    if (!alreadyListening && modelIndex + 1 < models.size) {
                        // Setup never succeeded with this model — try the next one.
                        client.close()
                        attemptConnect(context, apiKey, systemInstruction, voiceName, scope, modelIndex + 1)
                    } else {
                        onError?.invoke(message)
                        state = CallState.ERROR
                    }
                }

                override fun onClosed() {
                    if (state != CallState.IDLE) state = CallState.IDLE
                }
            }
        )
    }

    private fun setupAudioTrack(context: Context) {
        val minBuf = AudioTrack.getMinBufferSize(
            OUTPUT_SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(OUTPUT_SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBuf.coerceAtLeast(4096) * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        audioTrack?.play()

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    @SuppressLint("MissingPermission")
    private fun startMicCapture(scope: CoroutineScope) {
        val minBuf = AudioRecord.getMinBufferSize(
            INPUT_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = minBuf.coerceAtLeast(3200)

        val record = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            INPUT_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize * 2
        )
        audioRecord = record

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            onError?.invoke("Microphone could not be initialized")
            state = CallState.ERROR
            return
        }

        record.startRecording()

        captureJob = scope.launch {
            val buffer = ByteArray(bufferSize)
            while (true) {
                val read = record.read(buffer, 0, buffer.size)
                if (read > 0) {
                    val chunk = if (read == buffer.size) buffer.copyOf() else buffer.copyOf(read)
                    liveClient?.sendAudioChunk(chunk)
                }
            }
        }
    }

    fun stop() {
        captureJob?.cancel()
        captureJob = null

        audioRecord?.let {
            try {
                if (it.state == AudioRecord.STATE_INITIALIZED) it.stop()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping AudioRecord", e)
            }
            it.release()
        }
        audioRecord = null

        audioTrack?.let {
            try {
                it.stop()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping AudioTrack", e)
            }
            it.release()
        }
        audioTrack = null

        liveClient?.close()
        liveClient = null

        scope?.cancel()
        scope = null

        state = CallState.IDLE
    }

    fun isActive(): Boolean = state != CallState.IDLE
}
