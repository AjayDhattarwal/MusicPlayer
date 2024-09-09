package com.ar.musicplayer.utils.notification

import android.app.Notification
import android.app.NotificationManager as AndroidNotificationManager
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.ar.musicplayer.PlayNow.Companion.NOTIFICATION_ID
import com.ar.musicplayer.PlayNow.Companion.REMOVE_FROM_FAVORITES
import com.ar.musicplayer.PlayNow.Companion.SAVE_TO_FAVORITES
import com.ar.musicplayer.R
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

enum class ACTIONS {
    START,
    UPDATE,
    STOP
}


@UnstableApi
@AndroidEntryPoint
class AudioService : MediaSessionService() {

    @Inject
    lateinit var player: ExoPlayer

    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var mediaSession: MediaSession


    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSession.Builder(this, player)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTIONS.START.toString() -> {
                notificationManager.startNotification(
                    mediaSession = mediaSession,
                    mediaSessionService = this
                )
            }
            ACTIONS.UPDATE.toString() -> notificationManager.startNotification(mediaSession, mediaSessionService = this)

            ACTIONS.STOP.toString() ->{
                Log.d("service","service Stopped")
                stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession


    override fun onDestroy() {
        super.onDestroy()
        mediaSession.apply {
            release()
            player.stop()
            player.release()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }


}
