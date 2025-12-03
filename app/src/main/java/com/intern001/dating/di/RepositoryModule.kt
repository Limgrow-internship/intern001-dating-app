package com.intern001.dating.di

import com.intern001.dating.data.repository.AuthRepositoryImpl
import com.intern001.dating.data.repository.LanguageRepositoryImpl
import com.intern001.dating.data.repository.LikedYouRepositoryImpl
import com.intern001.dating.data.repository.LocationRepositoryImpl
import com.intern001.dating.data.repository.MatchRepositoryImpl
import com.intern001.dating.data.repository.MatchStatusRepositoryImpl
import com.intern001.dating.data.repository.NotificationRepositoryImpl
import com.intern001.dating.data.repository.PhotoRepositoryImpl
import com.intern001.dating.data.repository.RecommendationRepositoryImpl
import com.intern001.dating.data.repository.UserRepositoryImpl
import com.intern001.dating.domain.repository.AuthRepository
import com.intern001.dating.domain.repository.LanguageRepository
import com.intern001.dating.domain.repository.LikedYouRepository
import com.intern001.dating.domain.repository.LocationRepository
import com.intern001.dating.domain.repository.MatchRepository
import com.intern001.dating.domain.repository.MatchStatusRepository
import com.intern001.dating.domain.repository.NotificationRepository
import com.intern001.dating.domain.repository.PhotoRepository
import com.intern001.dating.domain.repository.RecommendationRepository
import com.intern001.dating.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLanguageRepository(
        languageRepositoryImpl: LanguageRepositoryImpl,
    ): LanguageRepository

    @Binds
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl,
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindMatchRepository(
        matchRepositoryImpl: MatchRepositoryImpl,
    ): MatchRepository

    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(
        recommendationRepositoryImpl: RecommendationRepositoryImpl,
    ): RecommendationRepository

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(
        photoRepositoryImpl: PhotoRepositoryImpl,
    ): PhotoRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl,
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl,
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindMatchStatusRepository(
        impl: MatchStatusRepositoryImpl,
    ): MatchStatusRepository

    @Binds
    @Singleton
    abstract fun bindLikedYouRepository(
        impl: LikedYouRepositoryImpl,
    ): LikedYouRepository
}
