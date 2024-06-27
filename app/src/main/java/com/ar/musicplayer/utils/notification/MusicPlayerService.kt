package com.ar.musicplayer.utils.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.utils.MusicPlayer
import com.ar.musicplayer.utils.MusicPlayerSingleton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@UnstableApi
class MusicPlayerService @Inject constructor(

)  : Service() {

    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var exoPlayer: ExoPlayer

    override fun onCreate() {
        super.onCreate()
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
