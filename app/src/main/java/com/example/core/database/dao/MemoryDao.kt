package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entity.MemoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_items ORDER BY timestamp DESC")
    fun getAllMemoryItems(): Flow<List<MemoryItemEntity>>

    @Query("SELECT * FROM memory_items WHERE category = :category ORDER BY timestamp DESC")
    fun getMemoryByCategory(category: String): Flow<List<MemoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryItemEntity)

    @Update
    suspend fun updateMemory(memory: MemoryItemEntity)

    @Query("DELETE FROM memory_items WHERE id = :id")
    suspend fun deleteMemoryById(id: String)

    @Query("DELETE FROM memory_items")
    suspend fun clearAllMemory()
}
