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
