package com.rangerscards.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    val isDarkTheme: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[THEME] ?: 2
        }

    val isIncludeEnglishSearchResults: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[INCLUDE_ENGLISH_SEARCH_RESULTS] ?: false
        }

    private companion object {
        val THEME = intPreferencesKey("theme")
        const val TAG = "UserPreferencesRepo"
        val CARDS_UPDATED_AT = stringPreferencesKey("cards_updated_at")
        val INCLUDE_ENGLISH_SEARCH_RESULTS = booleanPreferencesKey("english_results")
    }

    suspend fun saveThemePreference(theme: Int) {
        dataStore.edit { preferences ->
            preferences[THEME] = theme
        }
    }

    suspend fun saveCardsUpdatedTimestamp(timestamp: String) {
        dataStore.edit { preferences ->
            preferences[CARDS_UPDATED_AT] = timestamp
        }
    }

    suspend fun saveIncludeEnglishSearchResults(isIncludeEnglishSearchResults: Boolean) {
        dataStore.edit { preferences ->
            preferences[INCLUDE_ENGLISH_SEARCH_RESULTS] = isIncludeEnglishSearchResults
        }
    }

    fun getCarsUpdatedAt(): Flow<String> {
        val carsUpdatedAt = dataStore.data.catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[CARDS_UPDATED_AT] ?: ""
        }
        return carsUpdatedAt
    }

    fun compareTimestamps(timestamp1: String, timestamp2: String): Boolean {
        if (timestamp1.isEmpty()) return true
        // Define the date format
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC") // Set the timezone to UTC

        // Parse the timestamps
        val date1: Date = format.parse(timestamp1) ?: Date(0) // Fallback to epoch if parsing fails
        val date2: Date = format.parse(timestamp2) ?: Date(0) // Fallback to epoch if parsing fails
        return when {
            date1.before(date2) -> true
            date1.after(date2) -> false
            else -> false
        }
    }
}