package com.ar.musicplayer.utils.notification

import MusicPlayer
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.utils.MusicPlayerSingleton

@UnstableApi
class MusicPlayerService : Service() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var notificationManager: NotificationManager
    var musicPlayer = MusicPlayerSingleton.getInstance()

    override fun onCreate() {
        super.onCreate()

        exoPlayer = musicPlayer.getPlayer()
        notificationManager = NotificationManager(this, musicPlayer, exoPlayer)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationManager.cancelNotification()
        exoPlayer.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
