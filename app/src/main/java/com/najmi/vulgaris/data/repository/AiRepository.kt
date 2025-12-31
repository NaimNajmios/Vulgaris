package com.najmi.vulgaris.data.repository

import com.najmi.vulgaris.data.api.CerebrasApi
import com.najmi.vulgaris.data.model.CerebrasMessage
import com.najmi.vulgaris.data.model.CerebrasRequest
import com.najmi.vulgaris.data.model.CerebrasResponse
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(
    private val cerebrasApi: CerebrasApi,
    private val settingsRepository: SettingsRepository
) {
    private suspend fun getApiKey(): String {
        return settingsRepository.cerebrasApiKey.first()
    }
    
    suspend fun generateResponse(
        messages: List<CerebrasMessage>,
        model: String = "llama-3.3-70b"
    ): Result<CerebrasResponse> {
        return try {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Cerebras API key not configured. Please add your API key in Settings."))
            }
            
            val request = CerebrasRequest(
                model = model,
                messages = messages,
                temperature = 0.7,
                maxTokens = 4096
            )
            
            val response = cerebrasApi.generateContent(request, "Bearer $apiKey")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun testConnection(): Result<Boolean> {
        return try {
            val testMessages = listOf(
                CerebrasMessage(role = "user", content = "Say 'Hello' in one word")
            )
            val result = generateResponse(testMessages)
            Result.success(result.isSuccess)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
