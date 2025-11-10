package com.intern001.dating.di
import com.intern001.dating.data.api.DatingApiService
import dagger.Module import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    private const val BASE_URL = "http://192.168.88.80:3000/api/"

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDatingApiService(retrofit: Retrofit): DatingApiService {
        return retrofit.create(DatingApiService::class.java)
    }
}
