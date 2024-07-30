package com.ar.musicplayer.utils.playerHelper

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.screens.player.PlayerViewModel
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionDao
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionViewModel
import com.ar.musicplayer.screens.player.DetailsViewModel
import com.ar.musicplayer.screens.player.RecommendationViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MusicPlayerSingleton {

    @Provides
    fun provideContentResolver(
        @ApplicationContext context: Context
    ): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideExoPlayer(application: Application): ExoPlayer =
        ExoPlayer.Builder(application).build()

    @Provides
    @Singleton
    fun provideDetailsViewModel() = DetailsViewModel()


    @Provides
    @Singleton
    fun provideRecommendationViewModel() = RecommendationViewModel()



    @Provides
    @Singleton
    fun provideMusicDownloaderRepository(@ApplicationContext context: Context) = MusicDownloadRepository(context)

    @Provides
    @Singleton
    fun provideDownloaderViewModel(musicDownloadRepository: MusicDownloadRepository) = DownloaderViewModel(musicDownloadRepository)


    @Provides
    @Singleton
    fun provideLastSessionViewModel(lastSessionDao: LastSessionDao) = LastSessionViewModel(lastSessionDao = lastSessionDao)

    @Provides
    @Singleton
    fun provideMusicPlayerViewModel(
        application: Application,
        exoPlayer: ExoPlayer,
        lastSessionViewModel: LastSessionViewModel
    ): PlayerViewModel {
       return PlayerViewModel(application,exoPlayer,lastSessionViewModel)
    }

}