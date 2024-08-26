package com.ar.musicplayer.utils.notification


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import com.ar.musicplayer.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

enum class ACTIONS {
    START,
    STOP
}

@AndroidEntryPoint
@UnstableApi
class AudioService : MediaSessionService() {

    private val customCommandFavorites = SessionCommand("ACTION_FAVORITES", Bundle.EMPTY)

    @Inject
    lateinit var  mediaSession: MediaSession

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
//        mediaSession.setCustomLayout(
//            listOf(
//                CommandButton.Builder()
//                    .setDisplayName("Save to favorites")
//                    .setIconResId(R.drawable.ic_favorite_border)
//                    .setSessionCommand(customCommandFavorites)
//                    .build()
//        ))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTIONS.START.toString() -> {
                notificationManager.startNotification(
                    mediaSession = mediaSession,
                    mediaSessionService = this
                )
            }
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
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}