package com.ar.musicplayer.utils.playerHelper

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.utils.MusicPlayer
import com.ar.musicplayer.utils.notification.NotificationManager
import com.ar.musicplayer.viewmodel.DetailsViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.viewmodel.RecommendationViewModel
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
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }
    @Provides
    @Singleton
    fun provideDetailsViewModel() = DetailsViewModel()

    @Provides
    @Singleton
    fun providePlayerViewModel() = PlayerViewModel()


    @Provides
    @Singleton
    fun provideRecommendationViewModel() = RecommendationViewModel()



    @Provides
    @Singleton
    fun provideMusicPlayer(
        @ApplicationContext context: Context,
        viewModel: PlayerViewModel,
        exoPlayer: ExoPlayer,
        detailsViewModel: DetailsViewModel,
        recommendationViewModel: RecommendationViewModel
    ): MusicPlayer {
        return MusicPlayer(context, viewModel, exoPlayer,detailsViewModel,recommendationViewModel )
    }

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        exoPlayer: ExoPlayer,
        playerViewModel: PlayerViewModel,
        musicPlayer: MusicPlayer, // Inject MusicPlayer
        favoriteViewModel: FavoriteViewModel
    ): NotificationManager {
        val notificationManager = NotificationManager(context, exoPlayer, playerViewModel,musicPlayer, favoriteViewModel = favoriteViewModel)
        musicPlayer.addListener(notificationManager)
        return notificationManager
    }


    @Provides
    @Singleton
    fun provideMusicDownloaderRepository(@ApplicationContext context: Context) = MusicDownloadRepository(context)

    @Provides
    @Singleton
    fun provideDownloaderViewModel(musicDownloadRepository: MusicDownloadRepository) = DownloaderViewModel(musicDownloadRepository)

}