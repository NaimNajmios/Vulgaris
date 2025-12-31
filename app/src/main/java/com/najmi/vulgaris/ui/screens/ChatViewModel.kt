package com.najmi.vulgaris.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.najmi.vulgaris.data.model.ChatMessage
import com.najmi.vulgaris.data.repository.SettingsRepository
import com.najmi.vulgaris.domain.AgentOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = "",
    val error: String? = null,
    val hasApiKeys: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val agentOrchestrator: AgentOrchestrator,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _inputText = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)
    private val _hasApiKeys = MutableStateFlow(false)
    
    init {
        viewModelScope.launch {
            combine(
                settingsRepository.cerebrasApiKey,
                settingsRepository.footballApiKey
            ) { cerebras, football ->
                cerebras.isNotBlank() && football.isNotBlank()
            }.collect { hasKeys ->
                _hasApiKeys.value = hasKeys
            }
        }
    }
    
    val uiState: StateFlow<ChatUiState> = combine(
        _messages,
        _isLoading,
        _inputText,
        _error
    ) { messages, isLoading, inputText, error ->
        ChatUiState(
            messages = messages,
            isLoading = isLoading,
            inputText = inputText,
            error = error,
            hasApiKeys = _hasApiKeys.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState()
    )
    
    fun updateInputText(text: String) {
        _inputText.value = text
    }
    
    fun sendMessage() {
        val query = _inputText.value.trim()
        if (query.isBlank() || _isLoading.value) return
        
        viewModelScope.launch {
            // Add user message
            val userMessage = ChatMessage(
                content = query,
                isUser = true
            )
            _messages.value = _messages.value + userMessage
            _inputText.value = ""
            
            // Add loading placeholder
            val loadingMessage = ChatMessage(
                content = "",
                isUser = false,
                isLoading = true
            )
            _messages.value = _messages.value + loadingMessage
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = agentOrchestrator.processQuery(query)
                
                // Replace loading with response
                val responseMessage = ChatMessage(
                    content = result.response,
                    isUser = false,
                    toolsUsed = result.toolsUsed,
                    tokenCount = result.tokenCount,
                    responseTimeMs = result.responseTimeMs
                )
                
                _messages.value = _messages.value.dropLast(1) + responseMessage
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
                _messages.value = _messages.value.dropLast(1) + ChatMessage(
                    content = "Error: ${e.message ?: "Something went wrong"}",
                    isUser = false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearChat() {
        _messages.value = emptyList()
        agentOrchestrator.clearHistory()
    }
}
