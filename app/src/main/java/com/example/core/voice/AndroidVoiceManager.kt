package com.example.core.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.domain.model.AIPersonality
import com.example.domain.model.Gender
import java.util.Locale

object AndroidVoiceManager {
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())

    fun init(context: Context) {
        val appContext = context.applicationContext
        if (tts == null) {
            mainHandler.post {
                tts = TextToSpeech(appContext) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        isTtsInitialized = true
                        tts?.language = Locale.US
                    } else {
                        Log.e("AndroidVoiceManager", "TTS Initialization failed with status $status")
                    }
                }
            }
        }
    }

    fun speak(
        context: Context,
        text: String,
        personality: AIPersonality,
        customPitch: Float? = null,
        customSpeed: Float? = null,
        onStart: () -> Unit = {},
        onDone: () -> Unit = {}
    ) {
        init(context)
        val engine = tts ?: run {
            onDone()
            return
        }

        engine.stop()

        val pitch = customPitch ?: personality.pitch
        val speed = customSpeed ?: personality.speed

        if (personality.gender == Gender.MALE) {
            try {
                val maleVoice = engine.voices?.find { v ->
                    val nameLower = v.name.lowercase()
                    nameLower.contains("male") ||
                    nameLower.contains("en-us-x-iom") ||
                    nameLower.contains("en-us-x-sfg") ||
                    nameLower.contains("en-us-x-tpf") ||
                    nameLower.contains("m-local")
                }
                if (maleVoice != null) {
                    engine.voice = maleVoice
                }
            } catch (e: Exception) {
                Log.e("AndroidVoiceManager", "Error selecting male voice", e)
            }
            engine.setPitch(pitch.coerceAtMost(0.75f))
        } else {
            try {
                val femaleVoice = engine.voices?.find { v ->
                    val nameLower = v.name.lowercase()
                    nameLower.contains("female") ||
                    nameLower.contains("en-us-x-sfc") ||
                    nameLower.contains("f-local")
                }
                if (femaleVoice != null) {
                    engine.voice = femaleVoice
                }
            } catch (e: Exception) {
                Log.e("AndroidVoiceManager", "Error selecting female voice", e)
            }
            engine.setPitch(pitch)
        }

        engine.setSpeechRate(speed)

        val utteranceId = "SaraVoiceUtterance_${System.currentTimeMillis()}"

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                mainHandler.post { onStart() }
            }

            override fun onDone(id: String?) {
                mainHandler.post { onDone() }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                mainHandler.post { onDone() }
            }

            override fun onError(id: String?, errorCode: Int) {
                mainHandler.post { onDone() }
            }
        })

        val params = Bundle()
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopSpeaking() {
        mainHandler.post {
            try {
                tts?.stop()
            } catch (e: Exception) {
                Log.e("AndroidVoiceManager", "Error stopping TTS", e)
            }
        }
    }

    fun startListening(
        context: Context,
        languageLocale: String = "en-US",
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val appContext = context.applicationContext

        mainHandler.post {
            stopListening()

            if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                onError("Speech recognition not available on this device")
                return@post
            }

            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
                var capturedText = ""

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageLocale)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        if (capturedText.isNotBlank()) {
                            onFinalResult(capturedText)
                        }
                    }

                    override fun onError(error: Int) {
                        if (capturedText.isNotBlank()) {
                            onFinalResult(capturedText)
                            return
                        }
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient mic permissions"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error during recognition"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                            else -> "Speech recognition notice ($error)"
                        }
                        onError(errorMsg)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: capturedText
                        if (text.isNotBlank()) {
                            capturedText = text
                            onFinalResult(text)
                        } else {
                            onError("No speech captured")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            capturedText = text
                            onPartialResult(text)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e("AndroidVoiceManager", "Error starting SpeechRecognizer", e)
                onError("Unable to initialize speech recognizer: ${e.message}")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.e("AndroidVoiceManager", "Error stopping listening", e)
            } finally {
                speechRecognizer = null
            }
        }
    }

    fun release() {
        stopSpeaking()
        stopListening()
        mainHandler.post {
            tts?.shutdown()
            tts = null
        }
    }
}
