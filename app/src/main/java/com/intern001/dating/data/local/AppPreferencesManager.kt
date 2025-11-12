package com.intern001.dating.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.appPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

@Singleton
class AppPreferencesManager
@Inject
constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val LANGUAGE_SELECTED_KEY = booleanPreferencesKey("language_selected")
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.appPrefsDataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
        }
    }

    suspend fun isOnboardingCompleted(): Boolean {
        return context.appPrefsDataStore.data.map { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] == true
        }.first()
    }

    suspend fun setLanguageSelected(selected: Boolean) {
        context.appPrefsDataStore.edit { preferences ->
            preferences[LANGUAGE_SELECTED_KEY] = selected
        }
    }

    suspend fun isLanguageSelected(): Boolean {
        return context.appPrefsDataStore.data.map { preferences ->
            preferences[LANGUAGE_SELECTED_KEY] == true
        }.first()
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.appPrefsDataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] == true
    }

    val languageSelectedFlow: Flow<Boolean> = context.appPrefsDataStore.data.map { preferences ->
        preferences[LANGUAGE_SELECTED_KEY] == true
    }
}
