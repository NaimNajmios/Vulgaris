package com.najmi.vulgaris.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// API-Football response wrappers
@Serializable
data class FootballApiResponse<T>(
    val response: List<T>,
    val results: Int = 0,
    val paging: Paging? = null,
    val errors: List<String> = emptyList()
)

@Serializable
data class Paging(
    val current: Int,
    val total: Int
)

// Team models
@Serializable
data class TeamSearchResult(
    val team: Team,
    val venue: Venue? = null
)

@Serializable
data class Team(
    val id: Int,
    val name: String,
    val code: String? = null,
    val country: String? = null,
    val founded: Int? = null,
    val national: Boolean = false,
    val logo: String? = null
)

@Serializable
data class Venue(
    val id: Int? = null,
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val capacity: Int? = null,
    val surface: String? = null,
    val image: String? = null
)

// Fixture models
@Serializable
data class FixtureResult(
    val fixture: FixtureInfo,
    val league: LeagueInfo,
    val teams: FixtureTeams,
    val goals: Goals,
    val score: Score? = null
)

@Serializable
data class FixtureInfo(
    val id: Int,
    val referee: String? = null,
    val timezone: String? = null,
    val date: String,
    val timestamp: Long,
    val periods: Periods? = null,
    val venue: Venue? = null,
    val status: FixtureStatus
)

@Serializable
data class FixtureStatus(
    val long: String,
    val short: String,
    val elapsed: Int? = null
)

@Serializable
data class Periods(
    val first: Long? = null,
    val second: Long? = null
)

@Serializable
data class LeagueInfo(
    val id: Int,
    val name: String,
    val country: String? = null,
    val logo: String? = null,
    val flag: String? = null,
    val season: Int,
    val round: String? = null
)

@Serializable
data class FixtureTeams(
    val home: FixtureTeam,
    val away: FixtureTeam
)

@Serializable
data class FixtureTeam(
    val id: Int,
    val name: String,
    val logo: String? = null,
    val winner: Boolean? = null
)

@Serializable
data class Goals(
    val home: Int? = null,
    val away: Int? = null
)

@Serializable
data class Score(
    val halftime: Goals? = null,
    val fulltime: Goals? = null,
    val extratime: Goals? = null,
    val penalty: Goals? = null
)

// Standings models
@Serializable
data class StandingsResult(
    val league: StandingsLeague
)

@Serializable
data class StandingsLeague(
    val id: Int,
    val name: String,
    val country: String,
    val logo: String? = null,
    val flag: String? = null,
    val season: Int,
    val standings: List<List<Standing>>
)

@Serializable
data class Standing(
    val rank: Int,
    val team: Team,
    val points: Int,
    val goalsDiff: Int,
    val group: String? = null,
    val form: String? = null,
    val status: String? = null,
    val description: String? = null,
    val all: StandingStats,
    val home: StandingStats? = null,
    val away: StandingStats? = null,
    val update: String? = null
)

@Serializable
data class StandingStats(
    val played: Int,
    val win: Int,
    val draw: Int,
    val lose: Int,
    val goals: StandingGoals
)

@Serializable
data class StandingGoals(
    @SerialName("for")
    val goalsFor: Int,
    val against: Int
)

// Team Statistics models
@Serializable
data class TeamStatistics(
    val league: LeagueInfo,
    val team: Team,
    val form: String? = null,
    val fixtures: FixturesStats? = null,
    val goals: GoalsStats? = null,
    val biggest: BiggestStats? = null,
    val cleanSheet: HomeAwayTotal? = null,
    val failedToScore: HomeAwayTotal? = null,
    val penalty: PenaltyStats? = null,
    val lineups: List<LineupInfo>? = null
)

@Serializable
data class FixturesStats(
    val played: HomeAwayTotal? = null,
    val wins: HomeAwayTotal? = null,
    val draws: HomeAwayTotal? = null,
    val loses: HomeAwayTotal? = null
)

@Serializable
data class HomeAwayTotal(
    val home: Int? = null,
    val away: Int? = null,
    val total: Int? = null
)

@Serializable
data class GoalsStats(
    @SerialName("for")
    val goalsFor: GoalDetail? = null,
    val against: GoalDetail? = null
)

@Serializable
data class GoalDetail(
    val total: HomeAwayTotal? = null,
    val average: GoalAverage? = null
)

@Serializable
data class GoalAverage(
    val home: String? = null,
    val away: String? = null,
    val total: String? = null
)

@Serializable
data class BiggestStats(
    val streak: StreakStats? = null,
    val wins: HomeAwayString? = null,
    val loses: HomeAwayString? = null,
    val goals: BiggestGoals? = null
)

@Serializable
data class StreakStats(
    val wins: Int? = null,
    val draws: Int? = null,
    val loses: Int? = null
)

@Serializable
data class HomeAwayString(
    val home: String? = null,
    val away: String? = null
)

@Serializable
data class BiggestGoals(
    @SerialName("for")
    val goalsFor: HomeAwayGoals? = null,
    val against: HomeAwayGoals? = null
)

@Serializable
data class HomeAwayGoals(
    val home: Int? = null,
    val away: Int? = null
)

@Serializable
data class PenaltyStats(
    val scored: PenaltyDetail? = null,
    val missed: PenaltyDetail? = null,
    val total: Int? = null
)

@Serializable
data class PenaltyDetail(
    val total: Int? = null,
    val percentage: String? = null
)

@Serializable
data class LineupInfo(
    val formation: String,
    val played: Int
)

// Top Scorers models
@Serializable
data class TopScorer(
    val player: PlayerInfo,
    val statistics: List<PlayerStatistics>
)

@Serializable
data class PlayerInfo(
    val id: Int,
    val name: String,
    val firstname: String? = null,
    val lastname: String? = null,
    val age: Int? = null,
    val nationality: String? = null,
    val height: String? = null,
    val weight: String? = null,
    val photo: String? = null
)

@Serializable
data class PlayerStatistics(
    val team: Team? = null,
    val league: LeagueInfo? = null,
    val games: PlayerGames? = null,
    val goals: PlayerGoals? = null,
    val passes: PlayerPasses? = null,
    val cards: PlayerCards? = null
)

@Serializable
data class PlayerGames(
    val appearences: Int? = null,
    val lineups: Int? = null,
    val minutes: Int? = null,
    val position: String? = null,
    val rating: String? = null,
    val captain: Boolean? = null
)

@Serializable
data class PlayerGoals(
    val total: Int? = null,
    val conceded: Int? = null,
    val assists: Int? = null,
    val saves: Int? = null
)

@Serializable
data class PlayerPasses(
    val total: Int? = null,
    val key: Int? = null,
    val accuracy: Int? = null
)

@Serializable
data class PlayerCards(
    val yellow: Int? = null,
    val yellowred: Int? = null,
    val red: Int? = null
)
