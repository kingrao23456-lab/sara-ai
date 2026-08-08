package com.example.data.repository

import com.example.core.database.dao.ChatDao
import com.example.core.database.entity.ChatMessageEntity
import com.example.core.network.GeminiApiClient
import com.example.domain.model.AIPersonality
import com.example.domain.model.ChatMessage
import com.example.domain.model.Sender
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ChatRepository(
    private val chatDao: ChatDao,
    private val memoryRepository: MemoryRepository
) {

    fun getMessagesForSession(sessionId: String = "default_session"): Flow<List<ChatMessage>> {
        return chatDao.getMessagesBySession(sessionId).map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    sessionId = entity.sessionId,
                    sender = try { Sender.valueOf(entity.sender) } catch (e: Exception) { Sender.AI },
                    text = entity.text,
                    timestamp = entity.timestamp,
                    imagePath = entity.imagePath,
                    personalityId = entity.personalityId,
                    isVoiceMessage = entity.isVoiceMessage
                )
            }
        }
    }

    suspend fun sendUserMessage(
        text: String,
        personality: AIPersonality,
        sessionId: String = "default_session",
        language: String = "English",
        imageBase64: String? = null,
        enableSearchGrounding: Boolean = false,
        enableMapsGrounding: Boolean = false,
        modelName: String = GeminiApiClient.DEFAULT_MODEL
    ): ChatMessage {
        val userMsgId = java.util.UUID.randomUUID().toString()
        val userMessage = ChatMessage(
            id = userMsgId,
            sessionId = sessionId,
            sender = Sender.USER,
            text = text,
            timestamp = System.currentTimeMillis(),
            imagePath = imageBase64,
            personalityId = personality.id
        )

        // Save User Message to Database
        chatDao.insertMessage(
            ChatMessageEntity(
                id = userMessage.id,
                sessionId = userMessage.sessionId,
                sender = userMessage.sender.name,
                text = userMessage.text,
                timestamp = userMessage.timestamp,
                imagePath = userMessage.imagePath,
                personalityId = userMessage.personalityId,
                isVoiceMessage = userMessage.isVoiceMessage
            )
        )

        // Get Memory Context to inform AI
        val memoryList = memoryRepository.getAllMemoryItems().first()
        val memoryContext = memoryList.take(5).joinToString("\n") { "- ${it.keyTag}: ${it.content}" }

        // Query Gemini
        val aiReplyText = GeminiApiClient.generateAiResponse(
            prompt = text,
            personalitySystemInstruction = personality.systemInstruction,
            memoryContext = memoryContext,
            language = language,
            modelName = modelName,
            enableSearchGrounding = enableSearchGrounding,
            enableMapsGrounding = enableMapsGrounding,
            imageBase64 = imageBase64
        )

        // Save AI Message to Database
        val aiMsgId = java.util.UUID.randomUUID().toString()
        val aiMessage = ChatMessage(
            id = aiMsgId,
            sessionId = sessionId,
            sender = Sender.AI,
            text = aiReplyText,
            timestamp = System.currentTimeMillis(),
            personalityId = personality.id
        )

        chatDao.insertMessage(
            ChatMessageEntity(
                id = aiMessage.id,
                sessionId = aiMessage.sessionId,
                sender = aiMessage.sender.name,
                text = aiMessage.text,
                timestamp = aiMessage.timestamp,
                personalityId = aiMessage.personalityId,
                isVoiceMessage = aiMessage.isVoiceMessage
            )
        )

        // Auto-extract memory tag if user shares a key fact like "My name is X" or "My favorite X is Y"
        checkAndExtractMemory(text)

        return aiMessage
    }

    suspend fun sendVoiceUserMessage(
        text: String,
        personality: AIPersonality,
        sessionId: String = "default_session",
        language: String = "English",
        isVoiceToVoiceMode: Boolean = true
    ): ChatMessage {
        val userMsgId = java.util.UUID.randomUUID().toString()
        val userMessage = ChatMessage(
            id = userMsgId,
            sessionId = sessionId,
            sender = Sender.USER,
            text = text,
            timestamp = System.currentTimeMillis(),
            personalityId = personality.id,
            isVoiceMessage = true
        )

        chatDao.insertMessage(
            ChatMessageEntity(
                id = userMessage.id,
                sessionId = userMessage.sessionId,
                sender = userMessage.sender.name,
                text = userMessage.text,
                timestamp = userMessage.timestamp,
                personalityId = userMessage.personalityId,
                isVoiceMessage = true
            )
        )

        val memoryList = memoryRepository.getAllMemoryItems().first()
        val memoryContext = memoryList.take(5).joinToString("\n") { "- ${it.keyTag}: ${it.content}" }

        val aiReplyText = GeminiApiClient.generateVoiceResponse(
            prompt = text,
            personalitySystemInstruction = personality.systemInstruction,
            voiceName = personality.voiceName,
            memoryContext = memoryContext,
            language = language,
            isNativeAudioMode = isVoiceToVoiceMode
        )

        val aiMsgId = java.util.UUID.randomUUID().toString()
        val aiMessage = ChatMessage(
            id = aiMsgId,
            sessionId = sessionId,
            sender = Sender.AI,
            text = aiReplyText,
            timestamp = System.currentTimeMillis(),
            personalityId = personality.id,
            isVoiceMessage = true
        )

        chatDao.insertMessage(
            ChatMessageEntity(
                id = aiMessage.id,
                sessionId = aiMessage.sessionId,
                sender = aiMessage.sender.name,
                text = aiMessage.text,
                timestamp = aiMessage.timestamp,
                personalityId = aiMessage.personalityId,
                isVoiceMessage = true
            )
        )

        checkAndExtractMemory(text)

        return aiMessage
    }

    private suspend fun checkAndExtractMemory(userText: String) {
        val lower = userText.lowercase()
        if (lower.contains("my favorite") || lower.contains("i love") || lower.contains("i work at") || lower.contains("my birthday")) {
            val tag = when {
                lower.contains("favorite") -> "User Preference"
                lower.contains("work") -> "Work & Career"
                lower.contains("birthday") -> "Personal Date"
                else -> "Key Fact"
            }
            memoryRepository.addMemoryItem(tag, userText, "Auto-Extracted")
        }
    }

    suspend fun clearChatHistory() {
        chatDao.clearAllMessages()
    }

    suspend fun deleteMessage(id: String) {
        chatDao.deleteMessageById(id)
    }
}
