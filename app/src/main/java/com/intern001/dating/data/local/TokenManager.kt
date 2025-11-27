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
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val FIRST_NAME_KEY = stringPreferencesKey("first_name")
        private val LAST_NAME_KEY = stringPreferencesKey("last_name")
        private val GENDER_KEY = stringPreferencesKey("gender")
        private val MODE_KEY = stringPreferencesKey("mode")
        private val AVATAR_KEY = stringPreferencesKey("avatar")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedRefreshToken: String? = null

    @Volatile
    private var cachedUserId: String? = null

    @Volatile
    private var cachedUserEmail: String? = null

    @Volatile
    private var cachedFirstName: String? = null

    @Volatile
    private var cachedLastName: String? = null

    @Volatile
    private var cachedGender: String? = null

    @Volatile
    private var cachedMode: String? = null

    @Volatile
    private var cachedAvatar: String? = null


    init {
        scope.launch {
            context.dataStore.data.collect { preferences ->
                cachedAccessToken = preferences[ACCESS_TOKEN_KEY]
                cachedRefreshToken = preferences[REFRESH_TOKEN_KEY]
                cachedUserId = preferences[USER_ID_KEY]
                cachedUserEmail = preferences[USER_EMAIL_KEY]

                cachedFirstName = preferences[FIRST_NAME_KEY]
                cachedLastName = preferences[LAST_NAME_KEY]
                cachedGender = preferences[GENDER_KEY]
                cachedMode = preferences[MODE_KEY]
                cachedAvatar = preferences[AVATAR_KEY]
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

    suspend fun saveUserInfo(
        userId: String,
        userEmail: String,
    ) {
        cachedUserId = userId
        cachedUserEmail = userEmail

        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = userEmail
        }
    }

    suspend fun saveUserProfile(
        firstName: String,
        lastName: String,
        gender: String,
        mode: String,
        avatar: String
    ) {
        cachedFirstName = firstName
        cachedLastName = lastName
        cachedGender = gender
        cachedMode = mode
        cachedAvatar = avatar

        context.dataStore.edit { pref ->
            pref[FIRST_NAME_KEY] = firstName
            pref[LAST_NAME_KEY] = lastName
            pref[GENDER_KEY] = gender
            pref[MODE_KEY] = mode
            pref[AVATAR_KEY] = avatar
        }
    }


    fun getFirstName(): String? = cachedFirstName

    fun getLastName(): String? = cachedLastName

    fun getGender(): String? = cachedGender

    fun getMode(): String? = cachedMode

    fun getAvatar(): String? = cachedAvatar

    fun getUserId(): String? = cachedUserId

    fun getUserEmail(): String? = cachedUserEmail

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
        cachedUserId = null
        cachedUserEmail = null

        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)

            preferences.remove(FIRST_NAME_KEY)
            preferences.remove(LAST_NAME_KEY)
            preferences.remove(GENDER_KEY)
            preferences.remove(MODE_KEY)
            preferences.remove(AVATAR_KEY)
        }
    }
}
