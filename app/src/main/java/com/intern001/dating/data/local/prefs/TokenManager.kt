package com.intern001.dating.data.local.prefs

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) {
    fun saveTokens(accessToken: String?, refreshToken: String?) {
        sharedPreferences.edit()
            .putString("ACCESS_TOKEN", accessToken)
            .putString("REFRESH_TOKEN", refreshToken)
            .apply()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString("ACCESS_TOKEN", null)
    }

    suspend fun getAccessTokenAsync(): String? {
        return getAccessToken()
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("REFRESH_TOKEN", null)
    }

    fun clearTokens() {
        sharedPreferences.edit()
            .remove("ACCESS_TOKEN")
            .remove("REFRESH_TOKEN")
            .apply()
    }

    fun saveUserProfile(
        firstName: String,
        lastName: String,
        gender: String,
        mode: String,
        avatar: String,
        picture: String,
    ) {
        sharedPreferences.edit()
            .putString("USER_FIRST_NAME", firstName)
            .putString("USER_LAST_NAME", lastName)
            .putString("USER_GENDER", gender)
            .putString("USER_MODE", mode)
            .putString("USER_AVATAR", avatar)
            .putString("USER_PICTURE", picture)
            .apply()
    }

    fun getFirstName(): String? = sharedPreferences.getString("USER_FIRST_NAME", null)
    fun getLastName(): String? = sharedPreferences.getString("USER_LAST_NAME", null)
    fun getGender(): String? = sharedPreferences.getString("USER_GENDER", null)
    fun getMode(): String? = sharedPreferences.getString("USER_MODE", null)
    fun getAvatar(): String? = sharedPreferences.getString("USER_AVATAR", null)
    fun getPicture(): String? = sharedPreferences.getString("USER_PICTURE", null)
    fun getUserId(): String? = sharedPreferences.getString("USER_ID", null)

    fun setUserId(userId: String) {
        sharedPreferences.edit().putString("USER_ID", userId).apply()
    }

    fun saveEmailAndPassword(email: String, password: String) {
        sharedPreferences.edit()
            .putString("USER_EMAIL", email)
            .putString("USER_PASSWORD", password)
            .apply()
    }

    fun getEmail(): String? = sharedPreferences.getString("USER_EMAIL", null)
    fun getPassword(): String? = sharedPreferences.getString("USER_PASSWORD", null)

    fun saveUserInfo(userId: String?, userEmail: String?) {
        sharedPreferences.edit()
            .putString("USER_ID", userId)
            .putString("USER_EMAIL", userEmail)
            .apply()
    }

    fun getUserEmail(): String? = sharedPreferences.getString("USER_EMAIL", null)
}
