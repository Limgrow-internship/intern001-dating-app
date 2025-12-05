package com.intern001.dating.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.intern001.dating.data.local.ChatDatabase
import com.intern001.dating.data.local.ChatLocalRepository
import com.intern001.dating.data.repository.ChatRepositoryImpl
import com.intern001.dating.domain.repository.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

const val USER_PREFERENCES_NAME = "user_preferences"
val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, USER_PREFERENCES_NAME))
    },
)

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.userPreferencesDataStore
    }

    /**
     * Provide Room Database instance
     * Singleton để đảm bảo chỉ có một instance database trong app
     */
    @Singleton
    @Provides
    fun provideChatDatabase(@ApplicationContext context: Context): ChatDatabase {
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            ChatDatabase.DATABASE_NAME,
        )
            .fallbackToDestructiveMigration() // Nếu schema thay đổi, sẽ recreate database
            .build()
    }

    /**
     * Provide ChatLocalRepository
     */
    @Singleton
    @Provides
    fun provideChatLocalRepository(database: ChatDatabase): ChatLocalRepository {
        return ChatLocalRepository(database.messageDao())
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {
    @Binds
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl,
    ): ChatRepository
}
