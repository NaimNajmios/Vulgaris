package com.najmi.vulgaris.domain

import com.najmi.vulgaris.data.model.AgentResult
import com.najmi.vulgaris.data.model.CerebrasMessage
import com.najmi.vulgaris.data.repository.AiRepository
import kotlinx.serialization.json.Json
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
        
        private const val SYSTEM_PROMPT = """You are a helpful football assistant. You answer questions about football/soccer using live data from tools.

AVAILABLE TOOLS:
{{TOOLS}}

HOW TO USE TOOLS:
When you need data, respond with ONLY a JSON object (no other text before or after):
{"tools": [{"name": "tool_name", "arguments": {"param": "value"}}]}

IMPORTANT WORKFLOW - Always follow this sequence:
1. For questions about a specific team, FIRST call search_team to get the team's ID
2. Then call the appropriate tool (get_upcoming_fixtures, get_recent_fixtures, get_team_statistics) using that team_id NUMBER

EXAMPLES:
User: "When does Liverpool play next?"
Your response (Step 1): {"tools": [{"name": "search_team", "arguments": {"query": "Liverpool"}}]}
[After receiving Liverpool's team_id=40 from search results]
Your response (Step 2): {"tools": [{"name": "get_upcoming_fixtures", "arguments": {"team_id": "40", "count": "3"}}]}

User: "Show Premier League table"
Your response: {"tools": [{"name": "get_standings", "arguments": {"league_id": "39"}}]}

COMMON TEAM IDS (use these directly without search):
- Liverpool: 40, Arsenal: 42, Chelsea: 49, Manchester United: 33, Manchester City: 50
- Real Madrid: 541, Barcelona: 529, Bayern Munich: 157, Juventus: 496, PSG: 85

COMMON LEAGUE IDS:
- Premier League: 39, La Liga: 140, Bundesliga: 78, Serie A: 135, Ligue 1: 61
- Champions League: 2, Europa League: 3, FA Cup: 45

RULES:
- For well-known teams, you can use the IDs above directly
- Always respond with ONLY the JSON when calling tools, no explanatory text
- team_id and league_id must be numbers (as strings like "40", not descriptions)
- After receiving tool results, give a natural, conversational summary
- NEVER include JSON in your final response to users"""
        
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
        var iterationCount = 0
        val maxIterations = 3 // Prevent infinite loops
        
        // Add user message to history
        conversationHistory.add(CerebrasMessage(role = "user", content = userQuery))
        
        // Iterative tool calling loop
        while (iterationCount < maxIterations) {
            iterationCount++
            
            // Build messages with system prompt
            val messages = mutableListOf(
                CerebrasMessage(role = "system", content = getSystemPrompt())
            ) + conversationHistory
            
            // AI call
            val aiResult = aiRepository.generateResponse(messages)
            
            if (aiResult.isFailure) {
                conversationHistory.removeLast()
                return AgentResult(
                    response = "Error: ${aiResult.exceptionOrNull()?.message ?: "Failed to process query"}",
                    toolsUsed = toolsUsed,
                    tokenCount = totalTokens,
                    responseTimeMs = System.currentTimeMillis() - startTime
                )
            }
            
            val aiResponse = aiResult.getOrThrow()
            totalTokens += aiResponse.usage?.totalTokens ?: 0
            
            val aiContent = aiResponse.choices.firstOrNull()?.message?.content ?: ""
            
            // Check if AI wants to use tools
            val toolCalls = parseToolCalls(aiContent)
            
            if (toolCalls.isEmpty()) {
                // No more tool calls needed - this is the final response
                val cleanResponse = stripJsonFromResponse(aiContent)
                conversationHistory.add(CerebrasMessage(role = "assistant", content = cleanResponse))
                
                return AgentResult(
                    response = cleanResponse,
                    toolsUsed = toolsUsed,
                    tokenCount = totalTokens,
                    responseTimeMs = System.currentTimeMillis() - startTime
                )
            }
            
            // Execute tools and add results to conversation
            val toolResults = StringBuilder()
            for (toolCall in toolCalls) {
                toolsUsed.add(toolCall.name)
                val result = toolExecutor.execute(toolCall.name, toolCall.arguments)
                when (result) {
                    is ToolResult.Success -> {
                        toolResults.appendLine("### ${result.toolName} Results:")
                        toolResults.appendLine(result.data)
                        toolResults.appendLine()
                    }
                    is ToolResult.Error -> {
                        toolResults.appendLine("### ${toolCall.name} Error:")
                        toolResults.appendLine(result.message)
                        toolResults.appendLine()
                    }
                }
            }
            
            // Add tool results to conversation for next iteration
            conversationHistory.add(CerebrasMessage(role = "assistant", content = "Looking up data..."))
            conversationHistory.add(CerebrasMessage(
                role = "user", 
                content = """Tool results received:

$toolResults

Based on these results, either:
1. If you have all the data needed, provide a natural language response (NO JSON)
2. If you need more data (e.g., you got a team_id and now need fixtures), call the next tool with ONLY JSON

Remember: team_id must be a number like "40", not a description."""
            ))
        }
        
        // Max iterations reached
        val fallbackResponse = "I encountered some issues getting all the data. Here's what I found:\n\n" +
            toolsUsed.joinToString(", ") { "Used: $it" }
        conversationHistory.add(CerebrasMessage(role = "assistant", content = fallbackResponse))
        
        return AgentResult(
            response = fallbackResponse,
            toolsUsed = toolsUsed,
            tokenCount = totalTokens,
            responseTimeMs = System.currentTimeMillis() - startTime
        )
    }
    
    private fun stripJsonFromResponse(response: String): String {
        var cleaned = response
        
        // Remove any JSON blocks that look like tool calls
        val jsonPatterns = listOf(
            """\{[\s\S]*"tools"[\s\S]*\}""".toRegex(),
            """\{[\s\S]*"name"[\s\S]*"arguments"[\s\S]*\}""".toRegex()
        )
        
        for (pattern in jsonPatterns) {
            cleaned = cleaned.replace(pattern, "").trim()
        }
        
        // Also check for JSON at the end
        val jsonStart = cleaned.lastIndexOf("{")
        val jsonEnd = cleaned.lastIndexOf("}")
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            val potentialJson = cleaned.substring(jsonStart, jsonEnd + 1)
            if (potentialJson.contains("\"tools\"") || potentialJson.contains("\"name\"")) {
                cleaned = cleaned.substring(0, jsonStart).trim()
            }
        }
        
        return cleaned.trim().ifBlank { response }
    }
    
    private fun parseToolCalls(content: String): List<ParsedToolCall> {
        try {
            // Try to find JSON in the response
            val jsonStart = content.indexOf('{')
            val jsonEnd = content.lastIndexOf('}')
            
            if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart) {
                return emptyList()
            }
            
            val jsonString = content.substring(jsonStart, jsonEnd + 1)
            
            // Try parsing as tool call JSON
            val jsonObject = try {
                json.parseToJsonElement(jsonString).jsonObject
            } catch (e: Exception) {
                return emptyList()
            }
            
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
