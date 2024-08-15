package com.ar.musicplayer.utils.notification

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.ar.musicplayer.PlayNow.Companion.CHANNEL_ID
import com.ar.musicplayer.PlayNow.Companion.NOTIFICATION_ID
import com.ar.musicplayer.R
import com.ar.musicplayer.data.repository.FavoriteDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@UnstableApi
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val favoriteDataRepository: FavoriteDataRepository,
    private val exoPlayer: ExoPlayer
) {


    fun startNotification(
        mediaSession: MediaSession,
        mediaSessionService: MediaSessionService
    ) {
        buildNotification(mediaSession)
        startForegroundNotificationService(mediaSessionService)
    }

    private fun startForegroundNotificationService(mediaSessionService: MediaSessionService) {
        val notification = Notification.Builder(
            context,
            CHANNEL_ID
        )
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        mediaSessionService.startForeground(
            NOTIFICATION_ID,
            notification
        )
    }


    private fun buildNotification(mediaSession: MediaSession) {
        PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
            .setMediaDescriptionAdapter(
                NotificationAdapter(
                    context = context,
                    pendingIntent = mediaSession.sessionActivity
                )
            )
            .setSmallIconResourceId(R.drawable.ic_music_note_24)
            .build()
            .apply {
                setMediaSessionToken(mediaSession.sessionCompatToken)
                setUseNextActionInCompactView(true)
                setUsePreviousActionInCompactView(true)
                setPriority(NotificationCompat.PRIORITY_LOW)
                setPlayer(exoPlayer)
            }
    }
}