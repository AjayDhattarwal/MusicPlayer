package com.ar.musicplayer.di

import android.content.Context
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import com.ar.musicplayer.PlayNow.Companion.SAVE_TO_FAVORITES
import com.ar.musicplayer.R
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
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()



    @Singleton
    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager =
        NotificationManager(context)



}