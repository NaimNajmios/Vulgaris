package com.najmi.vulgaris.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Cerebras API models
@Serializable
data class CerebrasRequest(
    val model: String = "llama-3.3-70b",
    val messages: List<CerebrasMessage>,
    val temperature: Double = 0.7,
    @SerialName("max_tokens")
    val maxTokens: Int = 4096,
    val stream: Boolean = false
)

@Serializable
data class CerebrasMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

@Serializable
data class CerebrasResponse(
    val id: String,
    val model: String,
    val choices: List<CerebrasChoice>,
    val usage: CerebrasUsage? = null
)

@Serializable
data class CerebrasChoice(
    val index: Int,
    val message: CerebrasMessage,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class CerebrasUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int
)

// Chat message for UI
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val toolsUsed: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val tokenCount: Int = 0,
    val responseTimeMs: Long = 0
)

// Agent execution result
data class AgentResult(
    val response: String,
    val toolsUsed: List<String>,
    val tokenCount: Int,
    val responseTimeMs: Long
)

// Tool call from AI
@Serializable
data class ToolCall(
    val name: String,
    val arguments: Map<String, String> = emptyMap()
)
