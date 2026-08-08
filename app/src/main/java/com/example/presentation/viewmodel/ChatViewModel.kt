package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ChatRepository
import com.example.domain.model.AIPersonality
import com.example.domain.model.ChatMessage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _searchGroundingEnabled = MutableStateFlow(false)
    val searchGroundingEnabled: StateFlow<Boolean> = _searchGroundingEnabled.asStateFlow()

    private val _mapsGroundingEnabled = MutableStateFlow(false)
    val mapsGroundingEnabled: StateFlow<Boolean> = _mapsGroundingEnabled.asStateFlow()

    private val _selectedImageBase64 = MutableStateFlow<String?>(null)
    val selectedImageBase64: StateFlow<String?> = _selectedImageBase64.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.6-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _selectedThinkingMode = MutableStateFlow("BALANCED")
    val selectedThinkingMode: StateFlow<String> = _selectedThinkingMode.asStateFlow()

    private val _isTeamModeEnabled = MutableStateFlow(false)
    val isTeamModeEnabled: StateFlow<Boolean> = _isTeamModeEnabled.asStateFlow()

    private val _selectedTeamMembers = MutableStateFlow<Set<String>>(setOf("zoya", "alex", "zayn"))
    val selectedTeamMembers: StateFlow<Set<String>> = _selectedTeamMembers.asStateFlow()

    private val _currentSessionId = MutableStateFlow("default_session")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    fun setSelectedModel(modelId: String) { _selectedModel.value = modelId }
    fun setSelectedThinkingMode(modeId: String) { _selectedThinkingMode.value = modeId }
    fun toggleTeamMode() { _isTeamModeEnabled.value = !_isTeamModeEnabled.value }
    fun toggleTeamMember(personalityId: String) {
        val current = _selectedTeamMembers.value.toMutableSet()
        if (current.contains(personalityId)) {
            if (current.size > 1) current.remove(personalityId)
        } else {
            if (current.size < 8) current.add(personalityId)
        }
        _selectedTeamMembers.value = current
    }

    private val _replyingToMessage = MutableStateFlow<ChatMessage?>(null)
    val replyingToMessage: StateFlow<ChatMessage?> = _replyingToMessage.asStateFlow()

    private val _pinnedMessageIds = MutableStateFlow<Set<String>>(emptySet())
    val pinnedMessageIds: StateFlow<Set<String>> = _pinnedMessageIds.asStateFlow()

    private val _starredMessageIds = MutableStateFlow<Set<String>>(emptySet())
    val starredMessageIds: StateFlow<Set<String>> = _starredMessageIds.asStateFlow()

    private var generationJob: kotlinx.coroutines.Job? = null

    fun loadMessages(sessionId: String = _currentSessionId.value) {
        _currentSessionId.value = sessionId
        viewModelScope.launch {
            chatRepository.getMessagesForSession(sessionId).collect { list ->
                _messages.value = list
            }
        }
    }

    fun setSession(sessionId: String) {
        _currentSessionId.value = sessionId
        loadMessages(sessionId)
    }

    fun setReplyToMessage(msg: ChatMessage?) {
        _replyingToMessage.value = msg
    }

    fun togglePinMessage(id: String) {
        val current = _pinnedMessageIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _pinnedMessageIds.value = current
    }

    fun toggleStarMessage(id: String) {
        val current = _starredMessageIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _starredMessageIds.value = current
    }

    fun toggleSearchGrounding() {
        _searchGroundingEnabled.value = !_searchGroundingEnabled.value
    }

    fun toggleMapsGrounding() {
        _mapsGroundingEnabled.value = !_mapsGroundingEnabled.value
    }

    fun setSelectedImage(base64: String?) {
        _selectedImageBase64.value = base64
    }

    fun stopGeneration() {
        generationJob?.cancel()
        _isGenerating.value = false
    }

    fun sendMessage(
        text: String,
        personality: AIPersonality,
        language: String = "English"
    ) {
        if (text.isBlank() && _selectedImageBase64.value == null) return

        val replyContext = _replyingToMessage.value?.let { "Replying to [${it.sender}]: \"${it.text}\"\n\n" } ?: ""
        val thinkingPrefix = if (_selectedThinkingMode.value == "DEEP_THINKING") "[Mode: Deep Thinking Reasoning] " else ""
        val userPrompt = thinkingPrefix + replyContext + text.ifBlank { "Analyze this image" }
        val imageBase64 = _selectedImageBase64.value

        _selectedImageBase64.value = null
        _replyingToMessage.value = null
        _isGenerating.value = true

        generationJob = viewModelScope.launch {
            try {
                if (_isTeamModeEnabled.value) {
                    val team = AIPersonality.ALL_PERSONALITIES.filter { _selectedTeamMembers.value.contains(it.id) }
                    val activeTeam = if (team.isNotEmpty()) team else listOf(personality)
                    activeTeam.forEach { member ->
                        chatRepository.sendUserMessage(
                            text = userPrompt,
                            personality = member,
                            sessionId = _currentSessionId.value,
                            language = language,
                            imageBase64 = imageBase64,
                            enableSearchGrounding = _searchGroundingEnabled.value,
                            enableMapsGrounding = _mapsGroundingEnabled.value,
                            modelName = _selectedModel.value
                        )
                    }
                } else {
                    chatRepository.sendUserMessage(
                        text = userPrompt,
                        personality = personality,
                        sessionId = _currentSessionId.value,
                        language = language,
                        imageBase64 = imageBase64,
                        enableSearchGrounding = _searchGroundingEnabled.value,
                        enableMapsGrounding = _mapsGroundingEnabled.value,
                        modelName = _selectedModel.value
                    )
                }
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun regenerateLastResponse(personality: AIPersonality, language: String = "English") {
        val lastUserMsg = _messages.value.lastOrNull { it.sender == com.example.domain.model.Sender.USER }
        if (lastUserMsg != null && !_isGenerating.value) {
            sendMessage(lastUserMsg.text, personality, language)
        }
    }

    fun exportChatAsText(): String {
        val sessionName = when (_currentSessionId.value) {
            "work_session" -> "Work Workspace"
            "learning_session" -> "Learning Notes"
            else -> "General Chat"
        }
        val builder = StringBuilder("=== Sara AI Chat Export: $sessionName ===\n\n")
        _messages.value.forEach { msg ->
            builder.append("[${msg.sender.name}] ${msg.text}\n---\n")
        }
        return builder.toString()
    }

    fun clearChat() {
        viewModelScope.launch {
            chatRepository.clearChatHistory()
        }
    }

    fun deleteMessage(id: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(id)
        }
    }
}
