package com.ar.musicplayer.utils.notification

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.PlayNow.Companion.CHANNEL_ID
import com.ar.musicplayer.PlayNow.Companion.NOTIFICATION_ID
import com.ar.musicplayer.R
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationService : Service() {


    @Inject
    lateinit var player: ExoPlayer

    @Inject
    lateinit var playerViewModel: PlayerViewModel

    @Inject
    lateinit var favoriteViewModel: FavoriteViewModel

    private val mediaSession by lazy { MediaSessionCompat(this, "MusicPlayerSession") }

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    var isFav = false

    override fun onCreate() {
        super.onCreate()
        initializeMediaSession()
    }

    private fun collectFavoriteStatus() {
        coroutineScope.launch {
            favoriteViewModel.onEvent(
                FavoriteSongEvent.IsFavoriteSong(
                    playerViewModel.currentSong.value?.id.toString(), callback = { favScope ->
                        coroutineScope.launch {
                            favScope.collect{ favResponse ->
                                isFav = favResponse
                            }
                        }
                    }
                ))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> updateNotification()
            Actions.STOP.toString() -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun initializeMediaSession() {
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onSeekTo(pos: Long) {
                playerViewModel.seekTo(pos)
            }

            override fun onPlay() {
                player.play()
            }

            override fun onPause() {
                player.pause()
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
                        songResponse?.let { FavoriteSongEvent.ToggleFavSong(it) }
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
            .setSmallIcon(R.drawable.ic_music_note_24)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        collectFavoriteStatus()
        updatePlaybackState()
        startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
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
            .addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    "ACTION_FAVORITE",
                    "Favorite",
                    if (isFav == true) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                ).setExtras(Bundle().apply {
                }).build()
            )
            .setBufferedPosition(player.bufferedPosition)

        mediaSession.setPlaybackState(playbackStateBuilder.build())


        val metadataBuilder = MediaMetadataCompat.Builder()

        metadataBuilder.putText(
            MediaMetadataCompat.METADATA_KEY_TITLE,
            playerViewModel.currentSong.value?.title?.replace("&quot;","")
        )

        metadataBuilder.putText(
            MediaMetadataCompat.METADATA_KEY_ARTIST,
            playerViewModel.currentSong.value?.moreInfo?.artistMap?.artists?.distinctBy{it.name}?.joinToString(", "){it.name.toString()}
        )

        metadataBuilder.putLong(
            MediaMetadataCompat.METADATA_KEY_DURATION,
            if (player.duration > 0) player.duration else -1
        )
        metadataBuilder.putBitmap(
            MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
            if (playerViewModel.preloadedImage.value != null){
                playerViewModel.preloadedImage.value
            }else{
                null
            }
        )
        mediaSession.setMetadata(metadataBuilder.build())
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("kill", "notificationService destroyed")
    }

    enum class Actions {
        START,STOP
    }
}
