package com.intern001.dating.di

import com.intern001.dating.domain.model.common.validator.EmailValidator
import com.intern001.dating.domain.model.common.validator.PasswordValidator
import com.intern001.dating.domain.repository.AuthRepository
import com.intern001.dating.domain.usecase.auth.FacebookLoginUseCase
import com.intern001.dating.domain.usecase.auth.GoogleLoginUseCase
import com.intern001.dating.domain.usecase.auth.LoginUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideEmailValidator(): EmailValidator {
        return EmailValidator()
    }

    @Provides
    @Singleton
    fun providePasswordValidator(): PasswordValidator {
        return PasswordValidator()
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(
        authRepository: AuthRepository,
        emailValidator: EmailValidator,
        passwordValidator: PasswordValidator,
    ): LoginUseCase {
        return LoginUseCase(
            authRepository = authRepository,
            emailValidator = emailValidator,
            passwordValidator = passwordValidator,
        )
    }

    @Provides
    @Singleton
    fun provideGoogleLoginUseCase(authRepository: AuthRepository): GoogleLoginUseCase {
        return GoogleLoginUseCase(authRepository)
    }

    @Provides
    fun provideFacebookLoginUseCase(authRepository: AuthRepository): FacebookLoginUseCase {
        return FacebookLoginUseCase(authRepository)
    }

    // Note: Match and Recommendation UseCases are automatically provided by Hilt
    // via constructor injection (@Inject constructors).
    // No need for manual @Provides methods - this reduces boilerplate code.
}
