package com.najmi.vulgaris.data.repository

import com.najmi.vulgaris.data.api.FootballApi
import com.najmi.vulgaris.data.model.FixtureResult
import com.najmi.vulgaris.data.model.Standing
import com.najmi.vulgaris.data.model.TeamSearchResult
import com.najmi.vulgaris.data.model.TeamStatistics
import com.najmi.vulgaris.data.model.TopScorer
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FootballRepository @Inject constructor(
    private val footballApi: FootballApi,
    private val settingsRepository: SettingsRepository
) {
    // In-memory cache
    private val teamCache = mutableMapOf<String, List<TeamSearchResult>>()
    private val fixtureCache = mutableMapOf<String, List<FixtureResult>>()
    private val standingsCache = mutableMapOf<String, List<Standing>>()
    private val statsCache = mutableMapOf<String, TeamStatistics>()
    private val scorersCache = mutableMapOf<String, List<TopScorer>>()
    
    private suspend fun getApiKey(): String {
        return settingsRepository.footballApiKey.first()
    }
    
    private fun getCurrentSeason(): Int {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        // Football season typically starts in August
        return if (month >= Calendar.AUGUST) year else year - 1
    }
    
    suspend fun searchTeams(query: String): Result<List<TeamSearchResult>> {
        return try {
            // Check cache first
            teamCache[query.lowercase()]?.let { return Result.success(it) }
            
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Football API key not configured"))
            }
            
            val response = footballApi.searchTeams(query, apiKey)
            if (response.errors.isNotEmpty()) {
                Result.failure(Exception(response.errors.joinToString()))
            } else {
                teamCache[query.lowercase()] = response.response
                Result.success(response.response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUpcomingFixtures(teamId: Int, count: Int = 5): Result<List<FixtureResult>> {
        return try {
            val cacheKey = "upcoming_${teamId}_$count"
            fixtureCache[cacheKey]?.let { return Result.success(it) }
            
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Football API key not configured"))
            }
            
            val response = footballApi.getUpcomingFixtures(teamId, count, apiKey)
            if (response.errors.isNotEmpty()) {
                Result.failure(Exception(response.errors.joinToString()))
            } else {
                fixtureCache[cacheKey] = response.response
                Result.success(response.response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRecentFixtures(teamId: Int, count: Int = 5): Result<List<FixtureResult>> {
        return try {
            val cacheKey = "recent_${teamId}_$count"
            fixtureCache[cacheKey]?.let { return Result.success(it) }
            
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Football API key not configured"))
            }
            
            val response = footballApi.getRecentFixtures(teamId, count, apiKey)
            if (response.errors.isNotEmpty()) {
                Result.failure(Exception(response.errors.joinToString()))
            } else {
                fixtureCache[cacheKey] = response.response
                Result.success(response.response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getStandings(leagueId: Int): Result<List<Standing>> {
        return try {
            val season = getCurrentSeason()
            val cacheKey = "standings_${leagueId}_$season"
            standingsCache[cacheKey]?.let { return Result.success(it) }
            
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Football API key not configured"))
            }
            
            val response = footballApi.getStandings(leagueId, season, apiKey)
            if (response.errors.isNotEmpty()) {
                Result.failure(Exception(response.errors.joinToString()))
            } else {
                val standings = response.response.firstOrNull()?.league?.standings?.flatten() ?: emptyList()
                standingsCache[cacheKey] = standings
                Result.success(standings)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getLiveMatches(): Result<List<FixtureResult>> {
        return try {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Football API key not configured"))
            }
            
            // Don't cache live matches - always fresh
            val response = footballApi.getLiveMatches("all", apiKey)
            if (response.errors.isNotEmpty()) {
                Result.failure(Exception(response.errors.joinToString()))
            } else {
                Result.success(response.response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTeamStatistics(teamId: Int, leagueId: Int): Result<TeamStatistics> {
        return try {
            val season = getCurrentSeason()
            val cacheKey = "stats_${teamId}_${leagueId}_$season"
            statsCache[cacheKey]?.let { return Result.success(it) }
            
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Football API key not configured"))
            }
            
            val response = footballApi.getTeamStatistics(teamId, leagueId, season, apiKey)
            if (response.errors.isNotEmpty()) {
                Result.failure(Exception(response.errors.joinToString()))
            } else {
                statsCache[cacheKey] = response.response
                Result.success(response.response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTopScorers(leagueId: Int): Result<List<TopScorer>> {
        return try {
            val season = getCurrentSeason()
            val cacheKey = "scorers_${leagueId}_$season"
            scorersCache[cacheKey]?.let { return Result.success(it) }
            
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(Exception("Football API key not configured"))
            }
            
            val response = footballApi.getTopScorers(leagueId, season, apiKey)
            if (response.errors.isNotEmpty()) {
                Result.failure(Exception(response.errors.joinToString()))
            } else {
                scorersCache[cacheKey] = response.response
                Result.success(response.response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun clearCache() {
        teamCache.clear()
        fixtureCache.clear()
        standingsCache.clear()
        statsCache.clear()
        scorersCache.clear()
    }
}
