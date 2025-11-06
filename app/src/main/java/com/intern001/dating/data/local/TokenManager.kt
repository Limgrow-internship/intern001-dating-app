package com.intern001.dating.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        companion object {
            private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
            private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        }

        suspend fun saveTokens(
            accessToken: String,
            refreshToken: String,
        ) {
            context.dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = accessToken
                preferences[REFRESH_TOKEN_KEY] = refreshToken
            }
        }

        fun getAccessToken(): String? =
            runBlocking {
                context.dataStore.data.map { preferences ->
                    preferences[ACCESS_TOKEN_KEY]
                }.first()
            }

        fun getRefreshToken(): String? =
            runBlocking {
                context.dataStore.data.map { preferences ->
                    preferences[REFRESH_TOKEN_KEY]
                }.first()
            }

        val accessTokenFlow: Flow<String?> =
            context.dataStore.data.map { preferences ->
                preferences[ACCESS_TOKEN_KEY]
            }

        val refreshTokenFlow: Flow<String?> =
            context.dataStore.data.map { preferences ->
                preferences[REFRESH_TOKEN_KEY]
            }

        suspend fun clearTokens() {
            context.dataStore.edit { preferences ->
                preferences.remove(ACCESS_TOKEN_KEY)
                preferences.remove(REFRESH_TOKEN_KEY)
            }
        }
    }
