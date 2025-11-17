package com.intern001.dating

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class HeartOnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        applyLanguage()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(updateBaseContextLocale(base))
    }

    private fun applyLanguage() {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "en") ?: "en"
        val locale = Locale(lang)
        Locale.setDefault(locale)
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "en") ?: "en"

        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
