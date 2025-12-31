package com.najmi.vulgaris.domain

import com.najmi.vulgaris.data.model.FixtureResult
import com.najmi.vulgaris.data.model.Standing
import com.najmi.vulgaris.data.model.TeamSearchResult
import com.najmi.vulgaris.data.repository.FootballRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Represents a tool that the AI agent can use to fetch football data.
 */
sealed class AgentTool(
    val name: String,
    val description: String,
    val parameters: List<ToolParameter>
) {
    data class ToolParameter(
        val name: String,
        val type: String,
        val description: String,
        val required: Boolean = true
    )
}

object SearchTeamTool : AgentTool(
    name = "search_team",
    description = "Search for a football team by name. Returns team ID, name, country, and logo.",
    parameters = listOf(
        ToolParameter("query", "string", "Team name to search for", required = true)
    )
)

object GetUpcomingFixturesTool : AgentTool(
    name = "get_upcoming_fixtures",
    description = "Get upcoming matches for a team. Returns next scheduled matches with date, opponent, and competition.",
    parameters = listOf(
        ToolParameter("team_id", "integer", "Team ID from search_team result", required = true),
        ToolParameter("count", "integer", "Number of fixtures to retrieve (default: 5)", required = false)
    )
)

object GetRecentFixturesTool : AgentTool(
    name = "get_recent_fixtures",
    description = "Get recent match results for a team. Returns past matches with scores and competition.",
    parameters = listOf(
        ToolParameter("team_id", "integer", "Team ID from search_team result", required = true),
        ToolParameter("count", "integer", "Number of fixtures to retrieve (default: 5)", required = false)
    )
)

object GetStandingsTool : AgentTool(
    name = "get_standings",
    description = "Get league standings/table. Returns current table with position, points, wins, draws, losses.",
    parameters = listOf(
        ToolParameter("league_id", "integer", "League ID (e.g., 39 for Premier League, 140 for La Liga)", required = true)
    )
)

/**
 * Executes agent tools against the Football API.
 */
@Singleton
class ToolExecutor @Inject constructor(
    private val footballRepository: FootballRepository
) {
    companion object {
        val AVAILABLE_TOOLS = listOf(
            SearchTeamTool,
            GetUpcomingFixturesTool,
            GetRecentFixturesTool,
            GetStandingsTool
        )
        
        // Common league IDs for reference
        val LEAGUE_IDS = mapOf(
            "premier league" to 39,
            "la liga" to 140,
            "bundesliga" to 78,
            "serie a" to 135,
            "ligue 1" to 61,
            "champions league" to 2,
            "europa league" to 3
        )
        
        fun getToolDescriptions(): String {
            return AVAILABLE_TOOLS.joinToString("\n\n") { tool ->
                val params = tool.parameters.joinToString(", ") { p ->
                    "${p.name}: ${p.type}${if (p.required) " (required)" else " (optional)"}"
                }
                """
                |Tool: ${tool.name}
                |Description: ${tool.description}
                |Parameters: $params
                """.trimMargin()
            }
        }
    }
    
    suspend fun execute(toolName: String, arguments: Map<String, String>): ToolResult {
        return when (toolName) {
            "search_team" -> executeSearchTeam(arguments)
            "get_upcoming_fixtures" -> executeGetUpcomingFixtures(arguments)
            "get_recent_fixtures" -> executeGetRecentFixtures(arguments)
            "get_standings" -> executeGetStandings(arguments)
            else -> ToolResult.Error("Unknown tool: $toolName")
        }
    }
    
    private suspend fun executeSearchTeam(arguments: Map<String, String>): ToolResult {
        val query = arguments["query"] ?: return ToolResult.Error("Missing required parameter: query")
        
        return footballRepository.searchTeams(query).fold(
            onSuccess = { teams ->
                ToolResult.Success(
                    toolName = "search_team",
                    data = formatTeamResults(teams)
                )
            },
            onFailure = { ToolResult.Error(it.message ?: "Failed to search teams") }
        )
    }
    
    private suspend fun executeGetUpcomingFixtures(arguments: Map<String, String>): ToolResult {
        val teamId = arguments["team_id"]?.toIntOrNull()
            ?: return ToolResult.Error("Missing or invalid required parameter: team_id")
        val count = arguments["count"]?.toIntOrNull() ?: 5
        
        return footballRepository.getUpcomingFixtures(teamId, count).fold(
            onSuccess = { fixtures ->
                ToolResult.Success(
                    toolName = "get_upcoming_fixtures",
                    data = formatFixtureResults(fixtures, "upcoming")
                )
            },
            onFailure = { ToolResult.Error(it.message ?: "Failed to get fixtures") }
        )
    }
    
    private suspend fun executeGetRecentFixtures(arguments: Map<String, String>): ToolResult {
        val teamId = arguments["team_id"]?.toIntOrNull()
            ?: return ToolResult.Error("Missing or invalid required parameter: team_id")
        val count = arguments["count"]?.toIntOrNull() ?: 5
        
        return footballRepository.getRecentFixtures(teamId, count).fold(
            onSuccess = { fixtures ->
                ToolResult.Success(
                    toolName = "get_recent_fixtures",
                    data = formatFixtureResults(fixtures, "recent")
                )
            },
            onFailure = { ToolResult.Error(it.message ?: "Failed to get fixtures") }
        )
    }
    
    private suspend fun executeGetStandings(arguments: Map<String, String>): ToolResult {
        val leagueId = arguments["league_id"]?.toIntOrNull()
            ?: return ToolResult.Error("Missing or invalid required parameter: league_id")
        
        return footballRepository.getStandings(leagueId).fold(
            onSuccess = { standings ->
                ToolResult.Success(
                    toolName = "get_standings",
                    data = formatStandingsResults(standings)
                )
            },
            onFailure = { ToolResult.Error(it.message ?: "Failed to get standings") }
        )
    }
    
    private fun formatTeamResults(teams: List<TeamSearchResult>): String {
        if (teams.isEmpty()) return "No teams found."
        
        return teams.take(5).joinToString("\n") { result ->
            "- ${result.team.name} (ID: ${result.team.id}, Country: ${result.team.country ?: "Unknown"})"
        }
    }
    
    private fun formatFixtureResults(fixtures: List<FixtureResult>, type: String): String {
        if (fixtures.isEmpty()) return "No $type fixtures found."
        
        return fixtures.joinToString("\n") { fixture ->
            val home = fixture.teams.home.name
            val away = fixture.teams.away.name
            val score = if (fixture.goals.home != null && fixture.goals.away != null) {
                "${fixture.goals.home} - ${fixture.goals.away}"
            } else {
                "vs"
            }
            val date = fixture.fixture.date.substringBefore("T")
            val competition = fixture.league.name
            
            "- $home $score $away ($date, $competition)"
        }
    }
    
    private fun formatStandingsResults(standings: List<Standing>): String {
        if (standings.isEmpty()) return "No standings found."
        
        val header = "Pos | Team | Pts | W | D | L | GD"
        val rows = standings.take(20).joinToString("\n") { s ->
            "${s.rank}. ${s.team.name} | ${s.points} pts | ${s.all.win}W ${s.all.draw}D ${s.all.lose}L | GD: ${s.goalsDiff}"
        }
        return "$header\n$rows"
    }
}

sealed class ToolResult {
    data class Success(val toolName: String, val data: String) : ToolResult()
    data class Error(val message: String) : ToolResult()
}
