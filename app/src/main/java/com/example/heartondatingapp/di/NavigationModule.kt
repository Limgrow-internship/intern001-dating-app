package com.example.heartondatingapp.di

import com.example.heartondatingapp.presentation.navigation.NavigationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NavigationModule {

    @Singleton
    @Provides
    fun provideNavigationManager(): NavigationManager {
        return NavigationManager()
    }
}
