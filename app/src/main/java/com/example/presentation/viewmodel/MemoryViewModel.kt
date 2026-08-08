package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MemoryRepository
import com.example.domain.model.MemoryItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MemoryViewModel(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val memoryItems: StateFlow<List<MemoryItem>> = memoryRepository.getAllMemoryItems()
        .combine(_searchQuery) { items, query ->
            if (query.isBlank()) items
            else items.filter { it.keyTag.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) }
        }
        .combine(_selectedCategory) { items, category ->
            if (category == "All") items
            else items.filter { it.category.equals(category, ignoreCase = true) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _askPermissionBeforeSaving = MutableStateFlow(true)
    val askPermissionBeforeSaving: StateFlow<Boolean> = _askPermissionBeforeSaving.asStateFlow()

    private val _perChatMemoryEnabled = MutableStateFlow(true)
    val perChatMemoryEnabled: StateFlow<Boolean> = _perChatMemoryEnabled.asStateFlow()

    private val _neverSaveSensitive = MutableStateFlow(true)
    val neverSaveSensitive: StateFlow<Boolean> = _neverSaveSensitive.asStateFlow()

    fun toggleAskPermission() { _askPermissionBeforeSaving.value = !_askPermissionBeforeSaving.value }
    fun togglePerChatMemory() { _perChatMemoryEnabled.value = !_perChatMemoryEnabled.value }
    fun toggleNeverSaveSensitive() { _neverSaveSensitive.value = !_neverSaveSensitive.value }

    fun exportBackupJson(): String {
        val items = memoryItems.value
        val sb = StringBuilder("[\n")
        items.forEachIndexed { idx, item ->
            sb.append("  {\"keyTag\": \"${item.keyTag}\", \"content\": \"${item.content}\", \"category\": \"${item.category}\"}")
            if (idx < items.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addMemory(keyTag: String, content: String, category: String) {
        if (keyTag.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            memoryRepository.addMemoryItem(keyTag, content, category)
        }
    }

    fun updateMemory(id: String, keyTag: String, content: String, category: String) {
        viewModelScope.launch {
            memoryRepository.updateMemoryItem(id, keyTag, content, category)
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch {
            memoryRepository.deleteMemoryItem(id)
        }
    }

    fun clearAllMemory() {
        viewModelScope.launch {
            memoryRepository.clearAllMemory()
        }
    }
}
