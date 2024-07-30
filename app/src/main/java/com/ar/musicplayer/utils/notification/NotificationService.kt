package com.ar.musicplayer.utils.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.R
import com.ar.musicplayer.screens.player.PlayerViewModel
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationService : Service() {

    companion object {
        const val ACTION_UPDATE_NOTIFICATION = "com.example.musicplayer.UPDATE_NOTIFICATION"
        const val CHANNEL_ID = "music_channel"
        const val NOTIFICATION_ID = 1
    }

    @Inject
    lateinit var player: ExoPlayer

    @Inject
    lateinit var playerViewModel: PlayerViewModel

    @Inject
    lateinit var favoriteViewModel: FavoriteViewModel

    private val mediaSession by lazy { MediaSessionCompat(this, "MusicPlayerSession") }

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    var isFav =  false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializeMediaSession()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPDATE_NOTIFICATION -> updateNotification()
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        playerViewModel.killPlayer()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music Player Controls"
            }

            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initializeMediaSession() {
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onSeekTo(pos: Long) {
                playerViewModel.seekTo(pos)
            }

            override fun onPlay() {
                playerViewModel.playPause()
            }

            override fun onPause() {
                playerViewModel.playPause()
            }

            override fun onSkipToPrevious() {
                playerViewModel.skipPrevious()
            }

            override fun onSkipToNext() {
                playerViewModel.skipNext()
            }

            override fun onCustomAction(action: String?, extras: Bundle?) {
                super.onCustomAction(action, extras)
                if (action == "ACTION_FAVORITE") {
                    coroutineScope.launch {
                        val songResponse = playerViewModel.currentSong.value
                        songResponse?.let { FavoriteSongEvent.toggleFavSong(it) }
                            ?.let { favoriteViewModel.onEvent(it) }
                    }
                    isFav = !isFav
                    updatePlaybackState()
                }
            }
        })
    }


    private fun updateNotification() {
        val notification =  NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle((playerViewModel.currentSong.value?.title))
            .setContentText((playerViewModel.currentSong.value?.subtitle))
            .setSmallIcon(R.drawable.ic_music_note_24)
            .setLargeIcon(
                if (playerViewModel.preloadedImage.value != null){
                    playerViewModel.preloadedImage.value
                }else{
                    null
                }
            )
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        updatePlaybackState()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updatePlaybackState() {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                when (player.playbackState) {
                    Player.STATE_READY -> if (player.playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
                    Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
                    Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
                    else -> PlaybackStateCompat.STATE_NONE
                },
                player.currentPosition,
                1.0f
            )
            .addCustomAction( // Define your favorite action
                PlaybackStateCompat.CustomAction.Builder(
                    "ACTION_FAVORITE", // A unique string identifier for your action
                    "Favorite", // The text to display on the action button
                    if (isFav == true) R.drawable.ic_favorite else R.drawable.ic_favorite_border // The icon for the action button
                ).setExtras(Bundle().apply { // Optional: Add extras if needed
                }).build()
            )
            .setBufferedPosition(player.bufferedPosition)

        mediaSession.setPlaybackState(playbackStateBuilder.build())
        val metadataBuilder = MediaMetadataCompat.Builder()

        metadataBuilder.putLong(
            MediaMetadataCompat.METADATA_KEY_DURATION,
            if (player.duration > 0) player.duration else -1
        )
        mediaSession.setMetadata(metadataBuilder.build())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        playerViewModel.killPlayer()
        super.onDestroy()
    }
}
