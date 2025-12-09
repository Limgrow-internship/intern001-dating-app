package com.intern001.dating.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.intern001.dating.BuildConfig
import com.intern001.dating.data.api.DatingApiService
import com.intern001.dating.data.local.prefs.TokenManager
import com.intern001.dating.data.model.response.PhotoListDeserializer
import com.intern001.dating.data.model.response.PhotoResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            val token = tokenManager.getAccessToken()
            android.util.Log.d("AuthInterceptor", "Request URL: ${originalRequest.url}")
            android.util.Log.d("AuthInterceptor", "Token exists: ${token != null}")

            token?.let {
                android.util.Log.d("AuthInterceptor", "Full token: $it")
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            requestBuilder.addHeader("Accept", "application/json")
            requestBuilder.addHeader("Content-Type", "application/json")

            val request = requestBuilder.build()
            android.util.Log.d("AuthInterceptor", "Authorization header: ${request.header("Authorization")}")

            val response = chain.proceed(request)
            android.util.Log.d("AuthInterceptor", "Response code: ${response.code}")

            response
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor,
        tokenManager: TokenManager,
    ): OkHttpClient {
        // Create a simple refresh client without authenticator to avoid circular dependency
        val refreshClient = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()

        // Create refresh API service
        val refreshRetrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(refreshClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val refreshApiService = refreshRetrofit.create(DatingApiService::class.java)

        // Create TokenAuthenticator with refresh API service
        val tokenAuthenticator = com.intern001.dating.data.api.TokenAuthenticator(
            tokenManager,
            refreshApiService,
        )

        // Create main OkHttpClient with authenticator
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(
                object : TypeToken<List<PhotoResponse>>() {}.type,
                PhotoListDeserializer(),
            )
            .create()
    }

    @Provides
    @Singleton
    @Named("defaultRetrofit")
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("datingApi")
    fun provideDatingApiService(@Named("defaultRetrofit") retrofit: Retrofit): DatingApiService {
        return retrofit.create(DatingApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("restCountriesRetrofit")
    fun provideRestCountriesRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://restcountries.com/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("countryApi")
    fun provideCountryApi(@Named("restCountriesRetrofit") restCountriesRetrofit: Retrofit): DatingApiService {
        return restCountriesRetrofit.create(DatingApiService::class.java)
    }
}
