package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entity.AutomationTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationDao {
    @Query("SELECT * FROM automation_tasks")
    fun getAllTasks(): Flow<List<AutomationTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AutomationTaskEntity)

    @Query("UPDATE automation_tasks SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setTaskEnabled(id: String, isEnabled: Boolean)

    @Query("DELETE FROM automation_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)
}
