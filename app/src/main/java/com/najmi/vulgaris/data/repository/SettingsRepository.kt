package com.najmi.vulgaris.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val CEREBRAS_API_KEY = stringPreferencesKey("cerebras_api_key")
        private val FOOTBALL_API_KEY = stringPreferencesKey("football_api_key")
    }
    
    val cerebrasApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CEREBRAS_API_KEY] ?: ""
    }
    
    val footballApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[FOOTBALL_API_KEY] ?: ""
    }
    
    suspend fun saveCerebrasApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[CEREBRAS_API_KEY] = key
        }
    }
    
    suspend fun saveFootballApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[FOOTBALL_API_KEY] = key
        }
    }
    
    suspend fun getCerebrasApiKeySync(): String {
        var key = ""
        context.dataStore.data.map { preferences ->
            key = preferences[CEREBRAS_API_KEY] ?: ""
        }
        return key
    }
}
