package com.intern001.dating.di

import com.intern001.dating.data.repository.AuthRepositoryImpl
import com.intern001.dating.data.repository.LanguageRepositoryImpl
import com.intern001.dating.data.repository.UserRepositoryImpl
import com.intern001.dating.domain.repository.AuthRepository
import com.intern001.dating.domain.repository.LanguageRepository
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
}
