package com.intern001.dating.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedRefreshToken: String? = null

    init {
        scope.launch {
            context.dataStore.data.collect { preferences ->
                cachedAccessToken = preferences[ACCESS_TOKEN_KEY]
                cachedRefreshToken = preferences[REFRESH_TOKEN_KEY]
            }
        }
    }

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken

        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    fun getAccessToken(): String? = cachedAccessToken

    fun getRefreshToken(): String? = cachedRefreshToken

    suspend fun getAccessTokenAsync(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }.first()
    }

    suspend fun getRefreshTokenAsync(): String? {
        return context.dataStore.data.map { preferences ->
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
        cachedAccessToken = null
        cachedRefreshToken = null

        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }
}
