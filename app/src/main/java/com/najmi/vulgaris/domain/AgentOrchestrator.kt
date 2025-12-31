package com.najmi.vulgaris.domain

import com.najmi.vulgaris.data.model.AgentResult
import com.najmi.vulgaris.data.model.CerebrasMessage
import com.najmi.vulgaris.data.repository.AiRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentOrchestrator @Inject constructor(
    private val aiRepository: AiRepository,
    private val toolExecutor: ToolExecutor
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val conversationHistory = mutableListOf<CerebrasMessage>()
    
    companion object {
        private const val TOOLS_PLACEHOLDER = "{{TOOLS}}"
        
        private const val SYSTEM_PROMPT = """You are a helpful football assistant that answers questions about football/soccer.

You have access to the following tools to fetch live data:

{{TOOLS}}

When a user asks a question:
1. Analyze what data you need to answer
2. If you need data, respond with ONLY a JSON object containing the tools to call
3. After receiving tool results, synthesize a natural, conversational response

TOOL CALL FORMAT (respond with ONLY this JSON, no other text):
{
  "tools": [
    {"name": "tool_name", "arguments": {"param": "value"}}
  ]
}

IMPORTANT RULES:
- For team searches, use search_team first to get the team ID
- Common league IDs: Premier League=39, La Liga=140, Bundesliga=78, Serie A=135, Champions League=2
- If no tools are needed, respond directly with your answer
- Be concise but informative in your final responses
- Use football terminology appropriately

Example: "When does Liverpool play next?"
Step 1: Call search_team with "Liverpool" to get team ID
Step 2: Call get_upcoming_fixtures with the team ID
Step 3: Synthesize a natural response with the fixture info"""
        
        private fun getSystemPrompt(): String {
            return SYSTEM_PROMPT.replace(TOOLS_PLACEHOLDER, ToolExecutor.getToolDescriptions())
        }
    }
    
    fun clearHistory() {
        conversationHistory.clear()
    }
    
    suspend fun processQuery(userQuery: String): AgentResult {
        val startTime = System.currentTimeMillis()
        val toolsUsed = mutableListOf<String>()
        var totalTokens = 0
        
        // Add user message to history
        conversationHistory.add(CerebrasMessage(role = "user", content = userQuery))
        
        // Build messages with system prompt
        val messages = mutableListOf(
            CerebrasMessage(role = "system", content = getSystemPrompt())
        ) + conversationHistory
        
        // First AI call - planning
        val planningResult = aiRepository.generateResponse(messages)
        
        if (planningResult.isFailure) {
            return AgentResult(
                response = "Error: ${planningResult.exceptionOrNull()?.message ?: "Failed to process query"}",
                toolsUsed = emptyList(),
                tokenCount = 0,
                responseTimeMs = System.currentTimeMillis() - startTime
            )
        }
        
        val planningResponse = planningResult.getOrThrow()
        totalTokens += planningResponse.usage?.totalTokens ?: 0
        
        val aiContent = planningResponse.choices.firstOrNull()?.message?.content ?: ""
        
        // Check if AI wants to use tools
        val toolCalls = parseToolCalls(aiContent)
        
        if (toolCalls.isEmpty()) {
            // Direct response, no tools needed
            conversationHistory.add(CerebrasMessage(role = "assistant", content = aiContent))
            return AgentResult(
                response = aiContent,
                toolsUsed = emptyList(),
                tokenCount = totalTokens,
                responseTimeMs = System.currentTimeMillis() - startTime
            )
        }
        
        // Execute tools
        val toolResults = StringBuilder()
        for (toolCall in toolCalls) {
            toolsUsed.add(toolCall.name)
            val result = toolExecutor.execute(toolCall.name, toolCall.arguments)
            when (result) {
                is ToolResult.Success -> {
                    toolResults.appendLine("Tool: ${result.toolName}")
                    toolResults.appendLine("Result: ${result.data}")
                    toolResults.appendLine()
                }
                is ToolResult.Error -> {
                    toolResults.appendLine("Tool Error: ${result.message}")
                    toolResults.appendLine()
                }
            }
        }
        
        // Add tool results to conversation (but not the raw planning JSON)
        conversationHistory.add(CerebrasMessage(role = "assistant", content = "I'm looking up the data..."))
        conversationHistory.add(CerebrasMessage(
            role = "user",
            content = "Here are the tool results:\n\n$toolResults\n\nPlease provide a natural language response based on these results. Do NOT include any JSON in your response."
        ))
        
        // Second AI call - synthesis
        val synthesisMessages = listOf(
            CerebrasMessage(role = "system", content = getSystemPrompt())
        ) + conversationHistory
        
        val synthesisResult = aiRepository.generateResponse(synthesisMessages)
        
        if (synthesisResult.isFailure) {
            // Clean up conversation history
            conversationHistory.removeLast()
            conversationHistory.removeLast()
            return AgentResult(
                response = "I found some data but had trouble summarizing it:\n\n$toolResults",
                toolsUsed = toolsUsed,
                tokenCount = totalTokens,
                responseTimeMs = System.currentTimeMillis() - startTime
            )
        }
        
        val synthesisResponse = synthesisResult.getOrThrow()
        totalTokens += synthesisResponse.usage?.totalTokens ?: 0
        
        var finalResponse = synthesisResponse.choices.firstOrNull()?.message?.content ?: toolResults.toString()
        
        // Strip any JSON that might have leaked into the response
        val jsonStart = finalResponse.indexOf("{\"tools\":")
        if (jsonStart != -1) {
            finalResponse = finalResponse.substring(0, jsonStart).trim()
            if (finalResponse.isEmpty()) {
                finalResponse = "Here's what I found:\n\n$toolResults"
            }
        }
        
        // Update conversation history with final response
        conversationHistory.removeLast() // Remove tool results message
        conversationHistory.removeLast() // Remove placeholder message
        conversationHistory.add(CerebrasMessage(role = "assistant", content = finalResponse))
        
        // Keep conversation history manageable
        if (conversationHistory.size > 20) {
            conversationHistory.removeAt(0)
            conversationHistory.removeAt(0)
        }
        
        return AgentResult(
            response = finalResponse,
            toolsUsed = toolsUsed,
            tokenCount = totalTokens,
            responseTimeMs = System.currentTimeMillis() - startTime
        )
    }
    
    private fun parseToolCalls(content: String): List<ParsedToolCall> {
        try {
            // Try to find JSON in the response
            val jsonStart = content.indexOf('{')
            val jsonEnd = content.lastIndexOf('}')
            
            if (jsonStart == -1 || jsonEnd == -1) {
                return emptyList()
            }
            
            val jsonString = content.substring(jsonStart, jsonEnd + 1)
            val jsonObject = json.parseToJsonElement(jsonString).jsonObject
            
            val tools = jsonObject["tools"]?.jsonArray ?: return emptyList()
            
            return tools.mapNotNull { toolElement ->
                try {
                    val toolObj = toolElement.jsonObject
                    val name = toolObj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val args = toolObj["arguments"]?.jsonObject?.let { argsObj ->
                        argsObj.entries.associate { (key, value) ->
                            key to value.jsonPrimitive.content
                        }
                    } ?: emptyMap()
                    
                    ParsedToolCall(name, args)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            return emptyList()
        }
    }
    
    private data class ParsedToolCall(
        val name: String,
        val arguments: Map<String, String>
    )
}
