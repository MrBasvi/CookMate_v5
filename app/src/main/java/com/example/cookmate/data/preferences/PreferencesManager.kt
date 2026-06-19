package com.example.cookmate.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFERENCES_NAME = "cookmate_preferences"
private const val MIN_HISTORY_LIMIT = 1
private const val MAX_HISTORY_LIMIT = 100

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val SHOW_ONLY_FAVORITES = booleanPreferencesKey("show_only_favorites")
        val OFFLINE_ONLY_MODE = booleanPreferencesKey("offline_only_mode")
        val BACKGROUND_SYNC_ENABLED = booleanPreferencesKey("background_sync_enabled")
        val HISTORY_LIMIT = intPreferencesKey("history_limit")
        val START_DESTINATION = stringPreferencesKey("start_destination")
    }

    val showOnlyFavoritesFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_ONLY_FAVORITES] ?: false
    }

    val offlineOnlyModeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[OFFLINE_ONLY_MODE] ?: false
    }

    val backgroundSyncEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[BACKGROUND_SYNC_ENABLED] ?: true
    }

    val historyLimitFlow: Flow<Int> = dataStore.data.map { preferences ->
        (preferences[HISTORY_LIMIT] ?: 30).coerceIn(MIN_HISTORY_LIMIT, MAX_HISTORY_LIMIT)
    }

    val startDestinationFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[START_DESTINATION] ?: "search"
    }

    suspend fun setShowOnlyFavorites(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_ONLY_FAVORITES] = show
        }
    }

    suspend fun setOfflineOnlyMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[OFFLINE_ONLY_MODE] = enabled
        }
    }

    suspend fun setBackgroundSyncEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BACKGROUND_SYNC_ENABLED] = enabled
        }
    }

    suspend fun setHistoryLimit(limit: Int) {
        dataStore.edit { preferences ->
            preferences[HISTORY_LIMIT] = limit.coerceIn(MIN_HISTORY_LIMIT, MAX_HISTORY_LIMIT)
        }
    }

    suspend fun setStartDestination(route: String) {
        dataStore.edit { preferences ->
            preferences[START_DESTINATION] = route
        }
    }
}
