package com.example.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.GeminiApiClient
import com.example.core.voice.AndroidVoiceManager
import com.example.core.voice.LiveVoiceManager
import com.example.data.repository.ChatRepository
import com.example.domain.model.AIPersonality
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class VoiceState { IDLE, LISTENING, PROCESSING, SPEAKING }
enum class VoiceMode { VOICE_TO_VOICE, CHAT_MODE, HYBRID }
enum class RelationshipMode { FRIEND, BEST_FRIEND, STUDY_PARTNER, CODING_PARTNER, WORK_ASSISTANT, CREATIVE_PARTNER, FITNESS_COACH, MENTOR }
enum class EmotionTone { HAPPY, SAD, STRESSED, EXCITED, TIRED, CALM, ANGRY }

class VoiceViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _voiceMode = MutableStateFlow(VoiceMode.VOICE_TO_VOICE)
    val voiceMode: StateFlow<VoiceMode> = _voiceMode.asStateFlow()

    private val _relationshipMode = MutableStateFlow(RelationshipMode.FRIEND)
    val relationshipMode: StateFlow<RelationshipMode> = _relationshipMode.asStateFlow()

    private val _currentEmotion = MutableStateFlow(EmotionTone.CALM)
    val currentEmotion: StateFlow<EmotionTone> = _currentEmotion.asStateFlow()

    private val _userSpokenText = MutableStateFlow("Tap mic to speak...")
    val userSpokenText: StateFlow<String> = _userSpokenText.asStateFlow()

    private val _lastTranscript = MutableStateFlow("Sara is ready to talk.")
    val lastTranscript: StateFlow<String> = _lastTranscript.asStateFlow()

    private val _voiceSpeed = MutableStateFlow(1.0f)
    val voiceSpeed: StateFlow<Float> = _voiceSpeed.asStateFlow()

    private val _voicePitch = MutableStateFlow(1.0f)
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _isContinuousConversation = MutableStateFlow(true)
    val isContinuousConversation: StateFlow<Boolean> = _isContinuousConversation.asStateFlow()

    private val _isWakeWordEnabled = MutableStateFlow(false)
    val isWakeWordEnabled: StateFlow<Boolean> = _isWakeWordEnabled.asStateFlow()

    private val _selectedAccent = MutableStateFlow("US Accent")
    val selectedAccent: StateFlow<String> = _selectedAccent.asStateFlow()

    private val _smartSuggestions = MutableStateFlow(
        listOf(
            "How can you assist me today?",
            "Tell me something inspiring!",
            "Let's review my goals",
            "Give me a quick productivity tip"
        )
    )
    val smartSuggestions: StateFlow<List<String>> = _smartSuggestions.asStateFlow()

    private var speakJob: Job? = null

    // --- Real Gemini Live API duplex voice call (true voice-to-voice, not turn-based) ---
    private val liveVoiceManager = LiveVoiceManager()

    private val _isLiveCallActive = MutableStateFlow(false)
    val isLiveCallActive: StateFlow<Boolean> = _isLiveCallActive.asStateFlow()

    private val _liveCallError = MutableStateFlow<String?>(null)
    val liveCallError: StateFlow<String?> = _liveCallError.asStateFlow()

    /** Starts a real, continuous, duplex voice call with Gemini over the Live API. */
    fun startLiveCall(context: Context, personality: AIPersonality, language: String = "English") {
        if (_isLiveCallActive.value) return
        val apiKey = GeminiApiClient.getApiKey()
        if (apiKey.isEmpty()) {
            _liveCallError.value = "Gemini API key not configured. Add GEMINI_API_KEY to your .env and rebuild."
            return
        }
        _liveCallError.value = null
        _isLiveCallActive.value = true
        _voiceState.value = VoiceState.PROCESSING
        _lastTranscript.value = "Connecting live call..."

        val systemInstruction = buildString {
            append(personality.systemInstruction)
            append("\n\nYou are in a real-time spoken phone call. Reply naturally and briefly, like a real conversation.")
            append("\nPreferred Language: ").append(language)
        }

        liveVoiceManager.onStateChange = { state ->
            _voiceState.value = when (state) {
                LiveVoiceManager.CallState.IDLE -> {
                    _isLiveCallActive.value = false
                    VoiceState.IDLE
                }
                LiveVoiceManager.CallState.CONNECTING -> VoiceState.PROCESSING
                LiveVoiceManager.CallState.LISTENING,
                LiveVoiceManager.CallState.USER_SPEAKING -> VoiceState.LISTENING
                LiveVoiceManager.CallState.MODEL_SPEAKING -> VoiceState.SPEAKING
                LiveVoiceManager.CallState.ERROR -> {
                    _isLiveCallActive.value = false
                    VoiceState.IDLE
                }
            }
        }
        liveVoiceManager.onOutputTranscript = { text -> _lastTranscript.value = text }
        liveVoiceManager.onInputTranscript = { text -> _userSpokenText.value = text }
        liveVoiceManager.onError = { message ->
            _liveCallError.value = message
            _lastTranscript.value = "⚠️ Live call error: $message"
        }

        liveVoiceManager.start(
            context = context,
            apiKey = apiKey,
            systemInstruction = systemInstruction,
            voiceName = personality.voiceName
        )
    }

    fun stopLiveCall() {
        liveVoiceManager.stop()
        _isLiveCallActive.value = false
        _voiceState.value = VoiceState.IDLE
        _userSpokenText.value = "Tap mic to speak..."
    }

    fun setVoiceMode(mode: VoiceMode) { _voiceMode.value = mode }
    fun setRelationshipMode(mode: RelationshipMode) { _relationshipMode.value = mode }
    fun setEmotion(emotion: EmotionTone) { _currentEmotion.value = emotion }

    fun setVoiceSpeed(speed: Float) { _voiceSpeed.value = speed }
    fun setVoicePitch(pitch: Float) { _voicePitch.value = pitch }

    fun toggleContinuousConversation() { _isContinuousConversation.value = !_isContinuousConversation.value }
    fun toggleWakeWord() { _isWakeWordEnabled.value = !_isWakeWordEnabled.value }
    fun setAccent(accent: String) { _selectedAccent.value = accent }

    fun startListening(
        context: Context? = null,
        languageLocale: String = "en-US",
        personality: AIPersonality = AIPersonality.ZOYA,
        language: String = "English"
    ) {
        speakJob?.cancel()
        AndroidVoiceManager.stopSpeaking()
        _voiceState.value = VoiceState.LISTENING
        _userSpokenText.value = "Listening to your voice..."

        context?.let { ctx ->
            AndroidVoiceManager.startListening(
                context = ctx,
                languageLocale = languageLocale,
                onPartialResult = { partial ->
                    if (partial.isNotBlank()) {
                        _userSpokenText.value = partial
                    }
                },
                onFinalResult = { final ->
                    if (final.isNotBlank() && !final.contains("Listening to your voice")) {
                        _userSpokenText.value = final
                        stopListeningAndProcess(
                            context = ctx,
                            simulatedInput = final,
                            personality = personality,
                            language = language
                        )
                    } else if (_userSpokenText.value.isNotBlank() && !_userSpokenText.value.contains("Listening to your voice")) {
                        stopListeningAndProcess(
                            context = ctx,
                            simulatedInput = _userSpokenText.value,
                            personality = personality,
                            language = language
                        )
                    } else {
                        _voiceState.value = VoiceState.IDLE
                        _userSpokenText.value = "Tap mic to speak..."
                    }
                },
                onError = { err ->
                    android.util.Log.e("VoiceViewModel", "Speech recognition notice: $err")
                    if (_userSpokenText.value.isNotBlank() && !_userSpokenText.value.contains("Listening to your voice")) {
                        stopListeningAndProcess(
                            context = ctx,
                            simulatedInput = _userSpokenText.value,
                            personality = personality,
                            language = language
                        )
                    } else {
                        if (_voiceState.value == VoiceState.LISTENING) {
                            _voiceState.value = VoiceState.IDLE
                            _userSpokenText.value = "Tap mic to speak..."
                        }
                    }
                }
            )
        }
    }

    fun interruptAi() {
        speakJob?.cancel()
        AndroidVoiceManager.stopSpeaking()
        AndroidVoiceManager.stopListening()
        _voiceState.value = VoiceState.IDLE
        _lastTranscript.value = "[Interrupted] Listening again..."
        _userSpokenText.value = "Tap mic to speak..."
    }

    fun stopListeningAndProcess(
        context: Context? = null,
        simulatedInput: String = "",
        personality: AIPersonality = AIPersonality.ZOYA,
        language: String = "English"
    ) {
        if (_voiceState.value == VoiceState.LISTENING || _voiceState.value == VoiceState.IDLE) {
            context?.let { AndroidVoiceManager.stopListening() }
            _voiceState.value = VoiceState.PROCESSING

            val inputText = if (simulatedInput.isNotBlank()) {
                simulatedInput
            } else if (_userSpokenText.value.isNotBlank() && !_userSpokenText.value.contains("Listening")) {
                _userSpokenText.value
            } else {
                "How can you assist me today?"
            }

            _userSpokenText.value = inputText

            speakJob = viewModelScope.launch {
                val contextPrompt = "[Mode: ${_relationshipMode.value.name}, Emotion: ${_currentEmotion.value.name}] $inputText"

                val aiMsg = chatRepository.sendVoiceUserMessage(
                    text = contextPrompt,
                    personality = personality,
                    language = language,
                    isVoiceToVoiceMode = (_voiceMode.value == VoiceMode.VOICE_TO_VOICE)
                )

                _lastTranscript.value = aiMsg.text
                updateSmartSuggestions(aiMsg.text)

                _voiceState.value = VoiceState.SPEAKING

                if (context != null) {
                    AndroidVoiceManager.speak(
                        context = context,
                        text = aiMsg.text,
                        personality = personality,
                        customPitch = _voicePitch.value,
                        customSpeed = _voiceSpeed.value,
                        onDone = {
                            viewModelScope.launch {
                                if (_isContinuousConversation.value && _voiceMode.value != VoiceMode.CHAT_MODE) {
                                    startListening(context, personality = personality, language = language)
                                } else {
                                    _voiceState.value = VoiceState.IDLE
                                }
                            }
                        }
                    )
                } else {
                    val duration = (aiMsg.text.length * (50L / _voiceSpeed.value)).toLong().coerceIn(2000L, 8000L)
                    delay(duration)
                    if (_isContinuousConversation.value && _voiceMode.value != VoiceMode.CHAT_MODE) {
                        _voiceState.value = VoiceState.LISTENING
                        _userSpokenText.value = "Listening for your reply..."
                    } else {
                        _voiceState.value = VoiceState.IDLE
                    }
                }
            }
        }
    }

    fun previewVoice(context: Context? = null, personality: AIPersonality) {
        speakJob?.cancel()
        AndroidVoiceManager.stopSpeaking()
        speakJob = viewModelScope.launch {
            _voiceState.value = VoiceState.SPEAKING
            _lastTranscript.value = "Voice Preview (${personality.name}): \"${personality.greeting}\""

            if (context != null) {
                AndroidVoiceManager.speak(
                    context = context,
                    text = personality.greeting,
                    personality = personality,
                    customPitch = _voicePitch.value,
                    customSpeed = _voiceSpeed.value,
                    onDone = {
                        _voiceState.value = VoiceState.IDLE
                    }
                )
            } else {
                delay(3000)
                _voiceState.value = VoiceState.IDLE
            }
        }
    }

    private fun updateSmartSuggestions(lastAiResponse: String) {
        val suggestions = mutableListOf<String>()
        val lower = lastAiResponse.lowercase()

        when {
            lower.contains("goal") || lower.contains("task") || lower.contains("plan") -> {
                suggestions.add("Add this to my task list")
                suggestions.add("Break this down into steps")
                suggestions.add("Set a reminder for this")
            }
            lower.contains("code") || lower.contains("function") || lower.contains("bug") -> {
                suggestions.add("Explain this code step-by-step")
                suggestions.add("Optimize for performance")
                suggestions.add("Write a test case for this")
            }
            lower.contains("study") || lower.contains("learn") || lower.contains("explain") -> {
                suggestions.add("Quiz me on this topic")
                suggestions.add("Summarize in 3 bullet points")
                suggestions.add("Give me a real-world example")
            }
            else -> {
                suggestions.add("Tell me more about that")
                suggestions.add("What do you recommend next?")
                suggestions.add("Save this insight to memory")
            }
        }
        suggestions.add("Switch personality")
        _smartSuggestions.value = suggestions
    }

    fun resetVoiceState() {
        speakJob?.cancel()
        AndroidVoiceManager.stopSpeaking()
        AndroidVoiceManager.stopListening()
        _voiceState.value = VoiceState.IDLE
        _userSpokenText.value = "Tap mic to speak..."
    }

    override fun onCleared() {
        super.onCleared()
        AndroidVoiceManager.release()
        liveVoiceManager.stop()
    }
}
