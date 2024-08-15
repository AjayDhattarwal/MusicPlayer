package com.ar.musicplayer.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import com.ar.musicplayer.data.repository.FavoriteDataRepository
import com.ar.musicplayer.utils.notification.NotificationManager
import com.ar.musicplayer.viewmodel.ThemeViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MusicPlayerSingleton {

    @Singleton
    @Provides
    fun provideAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @Singleton
    @Provides
    @UnstableApi
    fun provideExoPlayer(@ApplicationContext context: Context, audioAttributes: AudioAttributes): ExoPlayer =
        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setTrackSelector(DefaultTrackSelector(context))
            .setHandleAudioBecomingNoisy(true)
            .build()

    @Singleton
    @Provides
    fun provideMediaSession(@ApplicationContext context: Context, player: ExoPlayer): MediaSession =
        MediaSession.Builder(context, player).build()

    @Singleton
    @Provides
    @UnstableApi
    fun provideNotificationManager(@ApplicationContext context: Context, exoPlayer: ExoPlayer,favoriteDataRepository: FavoriteDataRepository): NotificationManager =
        NotificationManager(context,favoriteDataRepository, exoPlayer)

    @Provides
    fun provideThemeViewModel(@ApplicationContext context: Context) = ThemeViewModel(context)

}