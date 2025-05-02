package com.example.dormhopfrontend.model.network

import com.example.dormhopfrontend.model.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object NetworkModule {
<<<<<<< HEAD
    @Provides @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
=======

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder().build()
>>>>>>> b415288ab12c93f049217ef3a1cbb40ddf906c9c

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            /* TODO: Change the URL later to the deployed app*/
<<<<<<< HEAD
            .baseUrl("http://34.86.55.150/api/")   // emulator → your machine
=======
            .baseUrl("http://10.0.2.2:5000/")   // emulator → your machine
>>>>>>> b415288ab12c93f049217ef3a1cbb40ddf906c9c
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}