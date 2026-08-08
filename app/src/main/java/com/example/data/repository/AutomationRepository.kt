package com.example.data.repository

import com.example.core.database.dao.AutomationDao
import com.example.core.database.dao.NotificationDao
import com.example.core.database.entity.AutomationTaskEntity
import com.example.core.database.entity.NotificationEntity
import com.example.domain.model.AppNotification
import com.example.domain.model.AutomationTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AutomationRepository(
    private val automationDao: AutomationDao,
    private val notificationDao: NotificationDao
) {

    val tasks: Flow<List<AutomationTask>> = automationDao.getAllTasks().map { entities ->
        if (entities.isEmpty()) {
            getPresetTasks()
        } else {
            entities.map {
                AutomationTask(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    cronSchedule = it.cronSchedule,
                    isEnabled = it.isEnabled,
                    actionType = it.actionType
                )
            }
        }
    }

    val notifications: Flow<List<AppNotification>> = notificationDao.getAllNotifications().map { entities ->
        entities.map {
            AppNotification(
                id = it.id,
                title = it.title,
                message = it.message,
                timestamp = it.timestamp,
                type = it.type,
                isRead = it.isRead
            )
        }
    }

    private fun getPresetTasks(): List<AutomationTask> {
        return listOf(
            AutomationTask(
                id = "task_morning",
                title = "Morning AI Briefing",
                description = "Summarize user schedule, memory highlights, and weather every morning.",
                cronSchedule = "Daily at 8:00 AM",
                isEnabled = true,
                actionType = "MORNING_BRIEF"
            ),
            AutomationTask(
                id = "task_memory_sync",
                title = "Auto Memory Synthesis",
                description = "Consolidate chat memory and group tags automatically every Sunday.",
                cronSchedule = "Weekly on Sunday",
                isEnabled = true,
                actionType = "MEMORY_PRUNE"
            ),
            AutomationTask(
                id = "task_security_audit",
                title = "Keystore Security Check",
                description = "Verify Android Keystore master keys and encrypted database integrity.",
                cronSchedule = "Daily at Midnight",
                isEnabled = true,
                actionType = "SECURITY_CHECK"
            )
        )
    }

    suspend fun addTask(task: AutomationTask) {
        automationDao.insertTask(
            AutomationTaskEntity(
                id = task.id,
                title = task.title,
                description = task.description,
                cronSchedule = task.cronSchedule,
                isEnabled = task.isEnabled,
                actionType = task.actionType
            )
        )
    }

    suspend fun deleteTask(id: String) {
        automationDao.deleteTaskById(id)
    }

    suspend fun toggleTask(id: String, isEnabled: Boolean) {
        automationDao.setTaskEnabled(id, isEnabled)
    }

    suspend fun addNotification(title: String, message: String, type: String = "INFO") {
        notificationDao.insertNotification(
            NotificationEntity(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                message = message,
                timestamp = System.currentTimeMillis(),
                type = type,
                isRead = false
            )
        )
    }

    suspend fun markNotificationAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    suspend fun clearNotifications() {
        notificationDao.clearAllNotifications()
    }
}
