package com.ar.musicplayer.utils.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.MainActivity
import com.ar.musicplayer.R
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.utils.MusicPlayer
import com.ar.musicplayer.utils.helper.loadImageBitmapFromUrl
import com.ar.musicplayer.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MusicPlayerService : Service() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var musicPlayer: MusicPlayer

    @Inject
    lateinit var playerViewModel: PlayerViewModel

    @Inject
    lateinit var favoriteViewModel: FavoriteViewModel

    private val notificationId = 1
    private val mediaSession by lazy { MediaSessionCompat(this, "MusicPlayerSession") }
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    var isFav =  false

    init {
        coroutineScope.launch {
            playerViewModel.isPlaying.collect { isPlaying ->
                if (isPlaying) {
                    exoPlayer.play()
                    Log.d("service", "inservice play ")
                } else {
                    exoPlayer.pause()
                    Log.d("service", "inservice pause")
                }
                Log.d("service", "inservice")
                updateNotification()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MusicPlayerService", "Service created")

        createNotificationChannel()

        // Initialize the ExoPlayer
        musicPlayer.getPlayer().addListener(playerListener)
        initializeMediaSession()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val song = intent?.getParcelableExtra("song") as? SongResponse
        song?.let {
            musicPlayer.play(it)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MusicPlayerService", "Service destroyed")
        musicPlayer.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val playerListener =
        @UnstableApi
        object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    musicPlayer.skipToNext()
                }
                isFav = playerViewModel.isFavorite.value ?: false
                updatePlaybackState()
                Log.d("service", "notification onPlaybackStateChanged ")
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                isFav = playerViewModel.isFavorite.value ?: false
                updatePlaybackState()
                Log.d("service", "notification play when ready   ")
            }

            override fun onPositionDiscontinuity(reason: Int) {
                updatePlaybackState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
            }
        }

    private fun createNotification(song: SongResponse, bitmap: Bitmap? = null): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.subtitle )
            .setSmallIcon(R.drawable.ic_music_note_24)
            .setContentIntent(pendingIntent)
            .setLargeIcon(
                bitmap ?: BitmapFactory.decodeResource(
                    this.resources,
                    R.drawable.ic_music_note_24
                )
            )
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotification() {
        if (playerViewModel.playlist.value.isNotEmpty()) {
            val currentSong = playerViewModel.currentSong.value
            coroutineScope.launch {
                val bitmap = currentSong?.image?.let {
                    loadImageBitmapFromUrl(it, context = this@MusicPlayerService)
                } ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                withContext(Dispatchers.Main) {
                    currentSong?.let {
                        playerViewModel.isFavorite.value?.let { it1 ->
                            isFav = it1
                            val notification =
                                createNotification(song = it, playerViewModel.bitmapImg.value)
                            notificationManager.notify(notificationId, notification)
                            startForeground(notificationId, notification)
                        }
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initializeMediaSession() {
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onSeekTo(pos: Long) {
                exoPlayer.seekTo(pos)
            }

            override fun onPlay() {
                playerViewModel.play()
            }

            override fun onPause() {
                playerViewModel.pause()
            }

            override fun onSkipToPrevious() {
                musicPlayer.skipToPrevious()
            }

            override fun onSkipToNext() {
                musicPlayer.skipToNext()
            }

            override fun onCustomAction(action: String?, extras: Bundle?) {
                super.onCustomAction(action, extras)
                if (action == "ACTION_FAVORITE") {
                    coroutineScope.launch {
                        val songResponse = playerViewModel.currentSong.value
                        songResponse?.let { FavoriteSongEvent.toggleFavSong(it) }
                            ?.let { favoriteViewModel.onEvent(it) }
                    }
                    isFav = !isFav!!
                    updatePlaybackState()
                }
            }
        })
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
                when (exoPlayer.playbackState) {
                    Player.STATE_READY -> if (exoPlayer.playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
                    Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
                    Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
                    else -> PlaybackStateCompat.STATE_NONE
                },
                exoPlayer.currentPosition,
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
            .setBufferedPosition(exoPlayer.bufferedPosition)

        mediaSession.setPlaybackState(playbackStateBuilder.build())
        val metadataBuilder = MediaMetadataCompat.Builder()

        metadataBuilder.putLong(
            MediaMetadataCompat.METADATA_KEY_DURATION,
            if (exoPlayer.duration > 0) exoPlayer.duration else -1
        )

        mediaSession.setMetadata(metadataBuilder.build())
    }

    companion object {
        fun startService(context: Context, song: SongResponse) {
            val intent = Intent(context, MusicPlayerService::class.java)
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MusicPlayerService::class.java)
            context.stopService(intent)
        }
    }

}

object Constants {
    const val NOTIFICATION_CHANNEL_ID = "music_player_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Music Player"
}
