package com.ar.musicplayer.di

import android.content.ContentResolver
import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.data.repository.FavoriteDataRepository
import com.ar.musicplayer.data.repository.HomeDataRepository
import com.ar.musicplayer.data.repository.LastSessionRepository
import com.ar.musicplayer.data.repository.SongDetailsRepository
import com.ar.musicplayer.api.ApiService
import com.ar.musicplayer.api.LyricsByLrclib
import com.ar.musicplayer.api.Translate
import com.ar.musicplayer.data.models.LyricsResponse
import com.ar.musicplayer.data.repository.GenerativeAiRepository
import com.ar.musicplayer.data.repository.LyricRepository
import com.ar.musicplayer.data.repository.PlayerRepository
import com.ar.musicplayer.data.repository.PlaylistRepository
import com.ar.musicplayer.utils.download.MusicDownloadRepository
import com.ar.musicplayer.utils.helper.NetworkConnectivityObserver
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavDao
import com.ar.musicplayer.utils.roomdatabase.homescreendb.HomeDataDao
import com.ar.musicplayer.utils.roomdatabase.lastsession.LastSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideContentResolver(
        @ApplicationContext context: Context
    ): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideHomeDataRepository(
        apiService: ApiService,
        homeDataDao: HomeDataDao
    ): HomeDataRepository {
        return HomeDataRepository(apiService, homeDataDao)
    }

    @Provides
    @Singleton
    fun provideMusicDownloaderRepository(@ApplicationContext context: Context) = MusicDownloadRepository(context)


    @Provides
    fun provideNetworkConnectivityObserver(@ApplicationContext context: Context): NetworkConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }

    @Provides
    fun provideSongDetailsRepository(apiService: ApiService): SongDetailsRepository {
        return SongDetailsRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideLastSessionRepository(lastSessionDao: LastSessionDao) = LastSessionRepository(lastSessionDao)

    @Provides
    @Singleton
    fun provideFavoriteDataRepository(favDao: FavDao) = FavoriteDataRepository(favDao)

    @Provides
    @Singleton
    fun providePlaylistRepository(apiService: ApiService) = PlaylistRepository(apiService)


    @Provides
    @Singleton
    fun provideLyricRepository(
        lyricsByLrclib: LyricsByLrclib,
        translationService: Translate
    ) = LyricRepository(lyricsByLrclib,translationService)


    @Provides
    @Singleton
    fun providePlayerRepository(
        @ApplicationContext context: Context,
        exoPlayer: ExoPlayer,
        lyricRepository: LyricRepository,
        lastSessionRepository : LastSessionRepository,
        songDetailsRepository : SongDetailsRepository
    ) =
        PlayerRepository(
            application = context,
            exoPlayer = exoPlayer,
            lyricRepository = lyricRepository,
            lastSessionRepository = lastSessionRepository,
            songDetailsRepository =  songDetailsRepository,
        )

    @Provides
    @Singleton
    fun provideGenerativeAiRepository(): GenerativeAiRepository {
        return GenerativeAiRepository()
    }
}