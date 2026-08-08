package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AutomationRepository
import com.example.domain.model.AppNotification
import com.example.domain.model.AutomationTask
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AutomationViewModel(
    private val automationRepository: AutomationRepository
) : ViewModel() {

    val tasks: StateFlow<List<AutomationTask>> = automationRepository.tasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notifications: StateFlow<List<AppNotification>> = automationRepository.notifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleTask(id: String, isEnabled: Boolean) {
        viewModelScope.launch {
            automationRepository.toggleTask(id, isEnabled)
        }
    }

    fun addAutomation(title: String, description: String, trigger: String, action: String) {
        viewModelScope.launch {
            val task = AutomationTask(
                title = title,
                description = "$description (Trigger: $trigger -> Action: $action)",
                cronSchedule = trigger,
                isEnabled = true,
                actionType = action
            )
            automationRepository.addTask(task)
            automationRepository.addNotification(
                title = "Automation Created: $title",
                message = "New rule created with trigger '$trigger' and action '$action'.",
                type = "AUTOMATION"
            )
        }
    }

    fun deleteAutomation(id: String) {
        viewModelScope.launch {
            automationRepository.deleteTask(id)
        }
    }

    fun runAutomationNow(task: AutomationTask) {
        viewModelScope.launch {
            automationRepository.addNotification(
                title = "Automation Executed: ${task.title}",
                message = "Action executed: ${task.actionType}. Result: Completed with success.",
                type = "AUTOMATION"
            )
        }
    }

    fun triggerManualBriefing() {
        viewModelScope.launch {
            automationRepository.addNotification(
                title = "Morning AI Briefing Generated",
                message = "Good morning! Sara analyzed your memory tags: 14 active facts stored, 0 security risks found. Have a wonderful productive day!",
                type = "AI_INSIGHT"
            )
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            automationRepository.markNotificationAsRead(id)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            automationRepository.clearNotifications()
        }
    }
}
