package com.najmi.vulgaris.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.najmi.vulgaris.data.repository.AiRepository
import com.najmi.vulgaris.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val cerebrasApiKey: String = "",
    val footballApiKey: String = "",
    val isTestingConnection: Boolean = false,
    val connectionTestResult: ConnectionTestResult? = null
)

sealed class ConnectionTestResult {
    data object Success : ConnectionTestResult()
    data class Error(val message: String) : ConnectionTestResult()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val aiRepository: AiRepository
) : ViewModel() {
    
    private val _isTestingConnection = MutableStateFlow(false)
    private val _connectionTestResult = MutableStateFlow<ConnectionTestResult?>(null)
    
    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.cerebrasApiKey,
        settingsRepository.footballApiKey,
        _isTestingConnection,
        _connectionTestResult
    ) { cerebras, football, isTesting, testResult ->
        SettingsUiState(
            cerebrasApiKey = cerebras,
            footballApiKey = football,
            isTestingConnection = isTesting,
            connectionTestResult = testResult
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )
    
    fun updateCerebrasApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.saveCerebrasApiKey(key)
            _connectionTestResult.value = null
        }
    }
    
    fun updateFootballApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.saveFootballApiKey(key)
            _connectionTestResult.value = null
        }
    }
    
    fun testConnection() {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _connectionTestResult.value = null
            
            try {
                val result = aiRepository.testConnection()
                _connectionTestResult.value = if (result.isSuccess && result.getOrNull() == true) {
                    ConnectionTestResult.Success
                } else {
                    ConnectionTestResult.Error(result.exceptionOrNull()?.message ?: "Connection failed")
                }
            } catch (e: Exception) {
                _connectionTestResult.value = ConnectionTestResult.Error(e.message ?: "Connection failed")
            } finally {
                _isTestingConnection.value = false
            }
        }
    }
    
    fun clearTestResult() {
        _connectionTestResult.value = null
    }
}
