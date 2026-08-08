package com.example.data.repository

import com.example.core.database.dao.MemoryDao
import com.example.core.database.entity.MemoryItemEntity
import com.example.core.security.KeystoreHelper
import com.example.domain.model.MemoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MemoryRepository(
    private val memoryDao: MemoryDao,
    private val keystoreHelper: KeystoreHelper
) {

    fun getAllMemoryItems(): Flow<List<MemoryItem>> {
        return memoryDao.getAllMemoryItems().map { entities ->
            entities.map { entity ->
                val decryptedContent = if (entity.isEncrypted) {
                    keystoreHelper.decrypt(entity.content)
                } else {
                    entity.content
                }
                MemoryItem(
                    id = entity.id,
                    keyTag = entity.keyTag,
                    content = decryptedContent,
                    category = entity.category,
                    timestamp = entity.timestamp,
                    isEncrypted = entity.isEncrypted
                )
            }
        }
    }

    suspend fun addMemoryItem(keyTag: String, rawContent: String, category: String = "General"): MemoryItem {
        val id = java.util.UUID.randomUUID().toString()
        val encryptedContent = keystoreHelper.encrypt(rawContent)
        val entity = MemoryItemEntity(
            id = id,
            keyTag = keyTag,
            content = encryptedContent,
            category = category,
            timestamp = System.currentTimeMillis(),
            isEncrypted = true
        )
        memoryDao.insertMemory(entity)
        return MemoryItem(
            id = id,
            keyTag = keyTag,
            content = rawContent,
            category = category,
            timestamp = entity.timestamp,
            isEncrypted = true
        )
    }

    suspend fun updateMemoryItem(id: String, keyTag: String, rawContent: String, category: String) {
        val encryptedContent = keystoreHelper.encrypt(rawContent)
        val entity = MemoryItemEntity(
            id = id,
            keyTag = keyTag,
            content = encryptedContent,
            category = category,
            timestamp = System.currentTimeMillis(),
            isEncrypted = true
        )
        memoryDao.updateMemory(entity)
    }

    suspend fun deleteMemoryItem(id: String) {
        memoryDao.deleteMemoryById(id)
    }

    suspend fun clearAllMemory() {
        memoryDao.clearAllMemory()
    }
}
