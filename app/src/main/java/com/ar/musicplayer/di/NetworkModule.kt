package com.ar.musicplayer.di

import com.ar.musicplayer.api.ApiService
import com.ar.musicplayer.api.ImportSpotifyPlaylist
import com.ar.musicplayer.api.LyricsByLrclib
import com.ar.musicplayer.api.Translate
import com.ar.musicplayer.api.YouTubeApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Singleton
    @Provides
    @Named("jiosaavan")
    fun providesJioSaavnRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://jiosaavn.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    @Named("spotifyImport")
    fun provideSpotifyRetrofit(okHttpClient: OkHttpClient): Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://api-partner.spotify.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    @Named("lyrics")
    fun provideLrclibRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://lrclib.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }


    @Singleton
    @Provides
    @Named("translate")
    fun provideTranslateRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://romantranslation.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    @Named("youtube")
    fun providesYoutubeRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://www.youtube.com/")
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideYoutubeApiService(@Named("youtube") youtubeRetrofit: Retrofit): YouTubeApi {
        return youtubeRetrofit.create(YouTubeApi::class.java)
    }


    @Singleton
    @Provides
    fun provideApiService(@Named("jiosaavan") jioSaavnRetrofit: Retrofit): ApiService {
        return jioSaavnRetrofit.create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideLyricsByLrclib(@Named("lyrics") lrclibRetrofit: Retrofit): LyricsByLrclib {
        return lrclibRetrofit.create(LyricsByLrclib::class.java)
    }

    @Singleton
    @Provides
    fun provideTranslate(@Named("translate") translateRetrofit: Retrofit): Translate {
        return translateRetrofit.create(Translate::class.java)
    }

    @Singleton
    @Provides
    fun provideImportSpotifyPlaylist(@Named("spotifyImport") retrofit: Retrofit): ImportSpotifyPlaylist {
        return retrofit.create(ImportSpotifyPlaylist::class.java)
    }




}
