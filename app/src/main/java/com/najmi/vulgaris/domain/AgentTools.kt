package com.najmi.vulgaris.domain

import com.najmi.vulgaris.data.model.FixtureResult
import com.najmi.vulgaris.data.model.Standing
import com.najmi.vulgaris.data.model.TeamSearchResult
import com.najmi.vulgaris.data.model.TeamStatistics
import com.najmi.vulgaris.data.model.TopScorer
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
    description = """Search for a football team by name. Returns a list of matching teams with their IDs.
IMPORTANT: Always use this first to get a team's ID before calling other team-related tools.
Returns: Team ID, name, country, and founded year for each match.""",
    parameters = listOf(
        ToolParameter("query", "string", "Team name to search for (e.g., 'Liverpool', 'Real Madrid')", required = true)
    )
)

object GetUpcomingFixturesTool : AgentTool(
    name = "get_upcoming_fixtures",
    description = """Get upcoming scheduled matches for a team.
Returns: Date, time, home team, away team, competition name, and venue for each fixture.
Use this to answer questions about when a team plays next.""",
    parameters = listOf(
        ToolParameter("team_id", "integer", "Team ID obtained from search_team", required = true),
        ToolParameter("count", "integer", "Number of fixtures to retrieve (1-10, default: 5)", required = false)
    )
)

object GetRecentFixturesTool : AgentTool(
    name = "get_recent_fixtures",
    description = """Get recent match results for a team.
Returns: Date, home team, away team, final score, and competition for each match.
Use this to answer questions about a team's recent form or past results.""",
    parameters = listOf(
        ToolParameter("team_id", "integer", "Team ID obtained from search_team", required = true),
        ToolParameter("count", "integer", "Number of past fixtures to retrieve (1-10, default: 5)", required = false)
    )
)

object GetStandingsTool : AgentTool(
    name = "get_standings",
    description = """Get the current league table/standings.
Returns: Position, team name, matches played, wins, draws, losses, goals for/against, goal difference, and points.
Common league IDs: Premier League=39, La Liga=140, Bundesliga=78, Serie A=135, Ligue 1=61, Champions League=2, Europa League=3.""",
    parameters = listOf(
        ToolParameter("league_id", "integer", "League ID (e.g., 39 for Premier League)", required = true)
    )
)

object GetLiveMatchesTool : AgentTool(
    name = "get_live_matches",
    description = """Get all matches currently being played right now (live).
Returns: Current minute, home team, away team, current score, and competition for each live match.
Use this to answer questions about matches happening now or current scores.""",
    parameters = emptyList()
)

object GetTeamStatisticsTool : AgentTool(
    name = "get_team_statistics",
    description = """Get comprehensive season statistics for a team in a specific league.
Returns: Form, total wins/draws/losses, goals scored/conceded, clean sheets, biggest win, top formation used.
Use this for in-depth team performance analysis.""",
    parameters = listOf(
        ToolParameter("team_id", "integer", "Team ID obtained from search_team", required = true),
        ToolParameter("league_id", "integer", "League ID (e.g., 39 for Premier League)", required = true)
    )
)

object GetTopScorersTool : AgentTool(
    name = "get_top_scorers",
    description = """Get the top goal scorers for a league in the current season.
Returns: Player name, team, goals scored, assists, and matches played.
Use this to answer questions about leading scorers or golden boot race.""",
    parameters = listOf(
        ToolParameter("league_id", "integer", "League ID (e.g., 39 for Premier League)", required = true),
        ToolParameter("count", "integer", "Number of top scorers to retrieve (1-20, default: 10)", required = false)
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
            GetStandingsTool,
            GetLiveMatchesTool,
            GetTeamStatisticsTool,
            GetTopScorersTool
        )
        
        // Common league IDs for reference
        val LEAGUE_IDS = mapOf(
            "premier league" to 39,
            "la liga" to 140,
            "bundesliga" to 78,
            "serie a" to 135,
            "ligue 1" to 61,
            "champions league" to 2,
            "europa league" to 3,
            "world cup" to 1,
            "euro" to 4,
            "fa cup" to 45,
            "carabao cup" to 48,
            "copa del rey" to 143
        )
        
        fun getToolDescriptions(): String {
            return AVAILABLE_TOOLS.joinToString("\n\n") { tool ->
                val params = if (tool.parameters.isEmpty()) {
                    "None"
                } else {
                    tool.parameters.joinToString(", ") { p ->
                        "${p.name}: ${p.type}${if (p.required) " (required)" else " (optional)"}"
                    }
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
            "get_live_matches" -> executeGetLiveMatches()
            "get_team_statistics" -> executeGetTeamStatistics(arguments)
            "get_top_scorers" -> executeGetTopScorers(arguments)
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
        val count = arguments["count"]?.toIntOrNull()?.coerceIn(1, 10) ?: 5
        
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
        val count = arguments["count"]?.toIntOrNull()?.coerceIn(1, 10) ?: 5
        
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
    
    private suspend fun executeGetLiveMatches(): ToolResult {
        return footballRepository.getLiveMatches().fold(
            onSuccess = { fixtures ->
                ToolResult.Success(
                    toolName = "get_live_matches",
                    data = formatLiveMatches(fixtures)
                )
            },
            onFailure = { ToolResult.Error(it.message ?: "Failed to get live matches") }
        )
    }
    
    private suspend fun executeGetTeamStatistics(arguments: Map<String, String>): ToolResult {
        val teamId = arguments["team_id"]?.toIntOrNull()
            ?: return ToolResult.Error("Missing or invalid required parameter: team_id")
        val leagueId = arguments["league_id"]?.toIntOrNull()
            ?: return ToolResult.Error("Missing or invalid required parameter: league_id")
        
        return footballRepository.getTeamStatistics(teamId, leagueId).fold(
            onSuccess = { stats ->
                ToolResult.Success(
                    toolName = "get_team_statistics",
                    data = formatTeamStatistics(stats)
                )
            },
            onFailure = { ToolResult.Error(it.message ?: "Failed to get team statistics") }
        )
    }
    
    private suspend fun executeGetTopScorers(arguments: Map<String, String>): ToolResult {
        val leagueId = arguments["league_id"]?.toIntOrNull()
            ?: return ToolResult.Error("Missing or invalid required parameter: league_id")
        val count = arguments["count"]?.toIntOrNull()?.coerceIn(1, 20) ?: 10
        
        return footballRepository.getTopScorers(leagueId).fold(
            onSuccess = { scorers ->
                ToolResult.Success(
                    toolName = "get_top_scorers",
                    data = formatTopScorers(scorers.take(count))
                )
            },
            onFailure = { ToolResult.Error(it.message ?: "Failed to get top scorers") }
        )
    }
    
    private fun formatTeamResults(teams: List<TeamSearchResult>): String {
        if (teams.isEmpty()) return "No teams found matching the search query."
        
        return buildString {
            appendLine("Found ${teams.size} team(s):")
            teams.take(5).forEach { result ->
                appendLine("- ${result.team.name}")
                appendLine("  ID: ${result.team.id}")
                appendLine("  Country: ${result.team.country ?: "Unknown"}")
                result.team.founded?.let { appendLine("  Founded: $it") }
            }
        }
    }
    
    private fun formatFixtureResults(fixtures: List<FixtureResult>, type: String): String {
        if (fixtures.isEmpty()) return "No $type fixtures found for this team."
        
        return buildString {
            appendLine("${type.replaceFirstChar { it.uppercase() }} fixtures:")
            fixtures.forEach { fixture ->
                val home = fixture.teams.home.name
                val away = fixture.teams.away.name
                val score = if (fixture.goals.home != null && fixture.goals.away != null) {
                    "${fixture.goals.home} - ${fixture.goals.away}"
                } else {
                    "vs"
                }
                val date = fixture.fixture.date.substringBefore("T")
                val time = fixture.fixture.date.substringAfter("T").substringBefore("+").take(5)
                val competition = fixture.league.name
                val venue = fixture.fixture.venue?.name ?: ""
                
                appendLine("- $date at $time")
                appendLine("  $home $score $away")
                appendLine("  Competition: $competition")
                if (venue.isNotBlank()) appendLine("  Venue: $venue")
            }
        }
    }
    
    private fun formatStandingsResults(standings: List<Standing>): String {
        if (standings.isEmpty()) return "No standings data available for this league."
        
        return buildString {
            appendLine("League Standings:")
            appendLine("Pos | Team | P | W | D | L | GD | Pts")
            appendLine("-".repeat(50))
            standings.take(20).forEach { s ->
                val team = s.team.name.take(18).padEnd(18)
                appendLine("${s.rank.toString().padStart(2)}. $team | ${s.all.played} | ${s.all.win} | ${s.all.draw} | ${s.all.lose} | ${s.goalsDiff.toString().padStart(3)} | ${s.points}")
            }
        }
    }
    
    private fun formatLiveMatches(fixtures: List<FixtureResult>): String {
        if (fixtures.isEmpty()) return "No matches are currently being played."
        
        return buildString {
            appendLine("Live Matches (${fixtures.size} in progress):")
            fixtures.forEach { fixture ->
                val home = fixture.teams.home.name
                val away = fixture.teams.away.name
                val score = "${fixture.goals.home ?: 0} - ${fixture.goals.away ?: 0}"
                val minute = fixture.fixture.status?.elapsed?.let { "${it}'" } ?: ""
                val status = fixture.fixture.status?.short ?: ""
                val competition = fixture.league.name
                
                appendLine("- [$minute $status] $home $score $away")
                appendLine("  Competition: $competition")
            }
        }
    }
    
    private fun formatTeamStatistics(stats: TeamStatistics): String {
        return buildString {
            appendLine("Team Statistics for ${stats.team.name} in ${stats.league.name}:")
            appendLine()
            appendLine("Form: ${stats.form ?: "N/A"}")
            appendLine()
            appendLine("Overall Record:")
            stats.fixtures?.let { f ->
                appendLine("  Played: ${f.played?.total ?: 0}")
                appendLine("  Wins: ${f.wins?.total ?: 0}")
                appendLine("  Draws: ${f.draws?.total ?: 0}")
                appendLine("  Losses: ${f.loses?.total ?: 0}")
            }
            appendLine()
            appendLine("Goals:")
            stats.goals?.let { g ->
                appendLine("  Scored: ${g.goalsFor?.total?.total ?: 0} (${g.goalsFor?.average?.total ?: "0"} per game)")
                appendLine("  Conceded: ${g.against?.total?.total ?: 0} (${g.against?.average?.total ?: "0"} per game)")
            }
            appendLine()
            appendLine("Clean Sheets: ${stats.cleanSheet?.total ?: 0}")
            appendLine("Failed to Score: ${stats.failedToScore?.total ?: 0}")
            stats.biggest?.let { b ->
                b.streak?.wins?.let { appendLine("Best Win Streak: $it matches") }
                b.wins?.home?.let { appendLine("Biggest Home Win: $it") }
                b.wins?.away?.let { appendLine("Biggest Away Win: $it") }
            }
        }
    }
    
    private fun formatTopScorers(scorers: List<TopScorer>): String {
        if (scorers.isEmpty()) return "No top scorer data available for this league."
        
        return buildString {
            appendLine("Top Scorers:")
            appendLine("Rank | Player | Team | Goals | Assists")
            appendLine("-".repeat(55))
            scorers.forEachIndexed { index, scorer ->
                val player = scorer.player.name.take(20).padEnd(20)
                val team = scorer.statistics.firstOrNull()?.team?.name?.take(15) ?: "Unknown"
                val goals = scorer.statistics.firstOrNull()?.goals?.total ?: 0
                val assists = scorer.statistics.firstOrNull()?.goals?.assists ?: 0
                appendLine("${(index + 1).toString().padStart(2)}. $player | $team | $goals | $assists")
            }
        }
    }
}

sealed class ToolResult {
    data class Success(val toolName: String, val data: String) : ToolResult()
    data class Error(val message: String) : ToolResult()
}
