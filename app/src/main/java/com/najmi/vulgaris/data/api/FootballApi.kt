package com.najmi.vulgaris.data.api

import com.najmi.vulgaris.data.model.FixtureResult
import com.najmi.vulgaris.data.model.FootballApiResponse
import com.najmi.vulgaris.data.model.StandingsResult
import com.najmi.vulgaris.data.model.TeamSearchResult
import com.najmi.vulgaris.data.model.TeamStatistics
import com.najmi.vulgaris.data.model.TopScorer
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FootballApi {
    
    companion object {
        const val BASE_URL = "https://v3.football.api-sports.io/"
    }
    
    @GET("teams")
    suspend fun searchTeams(
        @Query("search") search: String,
        @Header("x-apisports-key") apiKey: String
    ): FootballApiResponse<TeamSearchResult>
    
    @GET("teams")
    suspend fun getTeamById(
        @Query("id") teamId: Int,
        @Header("x-apisports-key") apiKey: String
    ): FootballApiResponse<TeamSearchResult>
    
    @GET("fixtures")
    suspend fun getFixtures(
        @Query("team") teamId: Int,
        @Query("season") season: Int,
        @Query("next") next: Int? = null,
        @Query("last") last: Int? = null,
        @Header("x-apisports-key") apiKey: String
    ): FootballApiResponse<FixtureResult>
    
    @GET("fixtures")
    suspend fun getUpcomingFixtures(
        @Query("team") teamId: Int,
        @Query("next") count: Int = 5,
        @Header("x-apisports-key") apiKey: String
    ): FootballApiResponse<FixtureResult>
    
    @GET("fixtures")
    suspend fun getRecentFixtures(
        @Query("team") teamId: Int,
        @Query("last") count: Int = 5,
        @Header("x-apisports-key") apiKey: String
    ): FootballApiResponse<FixtureResult>
    
    @GET("standings")
    suspend fun getStandings(
        @Query("league") leagueId: Int,
        @Query("season") season: Int,
        @Header("x-apisports-key") apiKey: String
    ): FootballApiResponse<StandingsResult>
    
    @GET("leagues")
    suspend fun getLeagues(
        @Query("team") teamId: Int,
        @Query("current") current: Boolean = true,
        @Header("x-apisports-key") apiKey: String
    ): FootballApiResponse<LeagueResult>
    
    @GET("fixtures")
    suspend fun getLiveMatches(
        @Query("live") live: String = "all",
        @Header("x-apisports-key") apiKey: String
    ): FootballApiResponse<FixtureResult>
    
    @GET("teams/statistics")
    suspend fun getTeamStatistics(
        @Query("team") teamId: Int,
        @Query("league") leagueId: Int,
        @Query("season") season: Int,
        @Header("x-apisports-key") apiKey: String
    ): TeamStatisticsResponse
    
    @GET("players/topscorers")
    suspend fun getTopScorers(
        @Query("league") leagueId: Int,
        @Query("season") season: Int,
        @Header("x-apisports-key") apiKey: String
    ): FootballApiResponse<TopScorer>
}

// Response wrapper for team statistics (different structure)
@kotlinx.serialization.Serializable
data class TeamStatisticsResponse(
    val response: TeamStatistics,
    val errors: List<String> = emptyList()
)

// Additional model for leagues endpoint
@kotlinx.serialization.Serializable
data class LeagueResult(
    val league: com.najmi.vulgaris.data.model.LeagueInfo,
    val country: CountryInfo? = null,
    val seasons: List<SeasonInfo>? = null
)

@kotlinx.serialization.Serializable
data class CountryInfo(
    val name: String,
    val code: String? = null,
    val flag: String? = null
)

@kotlinx.serialization.Serializable
data class SeasonInfo(
    val year: Int,
    val start: String,
    val end: String,
    val current: Boolean
)
