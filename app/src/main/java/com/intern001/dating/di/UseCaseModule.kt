package com.intern001.dating.di

import com.intern001.dating.domain.model.common.validator.EmailValidator
import com.intern001.dating.domain.model.common.validator.PasswordValidator
import com.intern001.dating.domain.repository.AuthRepository
import com.intern001.dating.domain.repository.MatchRepository
import com.intern001.dating.domain.repository.RecommendationRepository
import com.intern001.dating.domain.usecase.auth.FacebookLoginUseCase
import com.intern001.dating.domain.usecase.auth.GoogleLoginUseCase
import com.intern001.dating.domain.usecase.auth.LoginUseCase
import com.intern001.dating.domain.usecase.match.BlockUserUseCase
import com.intern001.dating.domain.usecase.match.GetMatchCardsUseCase
import com.intern001.dating.domain.usecase.match.GetMatchesUseCase
import com.intern001.dating.domain.usecase.match.GetNextMatchCardUseCase
import com.intern001.dating.domain.usecase.match.LikeUserUseCase
import com.intern001.dating.domain.usecase.match.PassUserUseCase
import com.intern001.dating.domain.usecase.match.SuperLikeUserUseCase
import com.intern001.dating.domain.usecase.match.UnmatchUserUseCase
import com.intern001.dating.domain.usecase.recommendation.GetRecommendationCriteriaUseCase
import com.intern001.dating.domain.usecase.recommendation.UpdateRecommendationCriteriaUseCase
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

    // Match UseCases
    @Provides
    @Singleton
    fun provideGetNextMatchCardUseCase(matchRepository: MatchRepository): GetNextMatchCardUseCase {
        return GetNextMatchCardUseCase(matchRepository)
    }

    @Provides
    @Singleton
    fun provideGetMatchCardsUseCase(matchRepository: MatchRepository): GetMatchCardsUseCase {
        return GetMatchCardsUseCase(matchRepository)
    }

    @Provides
    @Singleton
    fun provideLikeUserUseCase(matchRepository: MatchRepository): LikeUserUseCase {
        return LikeUserUseCase(matchRepository)
    }

    @Provides
    @Singleton
    fun providePassUserUseCase(matchRepository: MatchRepository): PassUserUseCase {
        return PassUserUseCase(matchRepository)
    }

    @Provides
    @Singleton
    fun provideSuperLikeUserUseCase(matchRepository: MatchRepository): SuperLikeUserUseCase {
        return SuperLikeUserUseCase(matchRepository)
    }

    @Provides
    @Singleton
    fun provideBlockUserUseCase(matchRepository: MatchRepository): BlockUserUseCase {
        return BlockUserUseCase(matchRepository)
    }

    @Provides
    @Singleton
    fun provideGetMatchesUseCase(matchRepository: MatchRepository): GetMatchesUseCase {
        return GetMatchesUseCase(matchRepository)
    }

    @Provides
    @Singleton
    fun provideUnmatchUserUseCase(matchRepository: MatchRepository): UnmatchUserUseCase {
        return UnmatchUserUseCase(matchRepository)
    }

    // Recommendation UseCases
    @Provides
    @Singleton
    fun provideGetRecommendationCriteriaUseCase(
        recommendationRepository: RecommendationRepository,
    ): GetRecommendationCriteriaUseCase {
        return GetRecommendationCriteriaUseCase(recommendationRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateRecommendationCriteriaUseCase(
        recommendationRepository: RecommendationRepository,
    ): UpdateRecommendationCriteriaUseCase {
        return UpdateRecommendationCriteriaUseCase(recommendationRepository)
    }
}
