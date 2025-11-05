package com.intern001.dating

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HeartOnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
