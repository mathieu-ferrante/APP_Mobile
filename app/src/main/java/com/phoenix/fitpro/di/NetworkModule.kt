package com.phoenix.fitpro.di

import com.phoenix.fitpro.BuildConfig
import com.phoenix.fitpro.data.remote.OpenFoodFactsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            // Open Food Facts exige un User-Agent identifiant ; les clients
            // anonymes generiques sont limites en priorite.
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", OpenFoodFactsApi.USER_AGENT)
                        .build()
                )
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                // Le corps complet ne sert qu'au debogage et fait fuiter les
                // reponses dans logcat en production.
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                else HttpLoggingInterceptor.Level.NONE
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(OpenFoodFactsApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): com.google.gson.Gson {
        return com.google.gson.GsonBuilder().create()
    }

    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(retrofit: Retrofit): OpenFoodFactsApi {
        return retrofit.create(OpenFoodFactsApi::class.java)
    }
}
