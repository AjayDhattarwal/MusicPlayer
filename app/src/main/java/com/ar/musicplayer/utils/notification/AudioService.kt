package com.ar.musicplayer.utils.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.ar.musicplayer.MainActivity
import com.ar.musicplayer.PlayNow.Companion.CHANNEL_ID
import com.ar.musicplayer.PlayNow.Companion.NOTIFICATION_ID
import com.ar.musicplayer.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
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

    lateinit var notificationManager : NotificationManager

    private lateinit var mediaSession: MediaSession

    private lateinit var wakeLock: PowerManager.WakeLock


    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mediaSession = MediaSession
            .Builder(this, player)
            .build()


        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp:AudioPlayback"
        )

        if (!this::player.isInitialized || player.isReleased) {
            initializePlayer()
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when(intent?.action) {
            ACTIONS.START.toString() -> {
                initializePlayer()
                startForeground(NOTIFICATION_ID, createNotification(mediaSession))
                wakeLock.acquire()
            }
            ACTIONS.UPDATE.toString() -> {
                initializePlayer()
                updateMediaStyleNotification(mediaSession)
            }
            ACTIONS.STOP.toString() -> {
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        mediaSession.apply {
            player.stop()
            player.release()
            release()
        }
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        Timber.tag("service").d("service onDestroy called")
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession


    fun createNotification(mediaSession: MediaSession): Notification {

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_music_note_24)
            .setContentIntent(contentPendingIntent)
            .setStyle(
                MediaStyleNotificationHelper.MediaStyle(mediaSession)
            )
            .build()
    }

    fun updateMediaStyleNotification(mediaSession: MediaSession) {

        val notification = createNotification(mediaSession)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun initializePlayer() {

        if (!this::player.isInitialized || player.isReleased) {
            player = ExoPlayer.Builder(this)
                .build().apply {
                    setAudioAttributes(audioAttributes, true)
                    setHandleAudioBecomingNoisy(true)
                    setWakeMode(C.WAKE_MODE_NETWORK)
                }

            mediaSession = MediaSession
                .Builder(this, player)
                .build()

            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp:AudioPlayback"
            )
        }

    }



}
