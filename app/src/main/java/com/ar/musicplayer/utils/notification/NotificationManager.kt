package com.ar.musicplayer.utils.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.ar.musicplayer.PlayNow.Companion.CHANNEL_ID
import com.ar.musicplayer.PlayNow.Companion.NOTIFICATION_ID
import com.ar.musicplayer.R
import javax.inject.Inject

class NotificationManager @Inject constructor(
    private val context: Context,
) {

    val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    @UnstableApi
    fun startNotification(
        mediaSession: MediaSession,
        mediaSessionService: MediaSessionService
    ) {
        val notification = createNotification(mediaSession)

        mediaSessionService.startForeground(
            NOTIFICATION_ID,
            notification
        )
    }


    @UnstableApi
    fun createNotification(mediaSession: MediaSession): Notification {

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_music_note_24)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionCompatToken)
            )
            .build()
    }

    @UnstableApi
    fun updateMediaStyleNotification(mediaSession: MediaSession) {

        val notification = createNotification(mediaSession)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }



}



// MediaStyleNotificationHelper.MediaStyle(mediaSession)

