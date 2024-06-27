package com.ar.musicplayer.utils

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.utils.notification.NotificationManager
import com.ar.musicplayer.viewmodel.PlayerViewModel
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
    fun providePlayerViewModel() = PlayerViewModel()


    @Provides
    @Singleton
    fun provideMusicPlayer(
        @ApplicationContext context: Context,
        viewModel: PlayerViewModel,
        exoPlayer: ExoPlayer
    ): MusicPlayer {
        return MusicPlayer(context, viewModel, exoPlayer )
    }

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        exoPlayer: ExoPlayer,
        playerViewModel: PlayerViewModel,
        musicPlayer: MusicPlayer // Inject MusicPlayer
    ): NotificationManager {
        val notificationManager = NotificationManager(context, exoPlayer, playerViewModel,musicPlayer)
        musicPlayer.addListener(notificationManager) // Set NotificationManager as a listener
        return notificationManager
    }

}