package com.ar.musicplayer.utils.notification


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.R
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.utils.MusicPlayer
import com.ar.musicplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationManager @Inject constructor(
    private val context: Context,
    private val exoPlayer: ExoPlayer,
    private val viewModel: PlayerViewModel,
    private val musicPlayer: MusicPlayer,
    private val favoriteViewModel: FavoriteViewModel
) {
    private val notificationId: Int = 1
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val mediaSession = MediaSessionCompat(context, "MusicPlayerSession")
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    var isFav = viewModel.isFavorite.value

    private val playerListener =
    @UnstableApi
    object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            isFav = viewModel.isFavorite.value
            updatePlaybackState()
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            isFav = viewModel.isFavorite.value
            updatePlaybackState()
        }

        override fun onPositionDiscontinuity(reason: Int) {
            updatePlaybackState()
        }
    }

    init {
        createNotificationChannel()
        initializeMediaSession()
        exoPlayer.addListener(playerListener)
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification channel for music playback controls"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initializeMediaSession() {
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onSeekTo(pos: Long) {
                exoPlayer.seekTo(pos)
            }

            override fun onPlay() {
                viewModel.play()

            }

            override fun onPause() {
                viewModel.pause()
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
                        val songResponse = viewModel.currentSong.value
                        songResponse?.let { FavoriteSongEvent.toggleFavSong(it) }
                            ?.let { favoriteViewModel.onEvent(it) }
                    }
                    isFav = !isFav!!
                    updatePlaybackState()
                }
            }
        })
    }

    fun showNotification(song: SongResponse,isFavorite: Boolean,bitmap: Bitmap? = null) {
        isFav = isFavorite
        val notification = createNotification(song, bitmap)
        notificationManager.notify(notificationId, notification)
    }

    fun createNotification(song: SongResponse, bitmap: Bitmap?): Notification {

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.subtitle)
            .setSmallIcon(R.drawable.ic_music_note_24)
            .setLargeIcon(
                bitmap ?: BitmapFactory.decodeResource(
                    context.resources,
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

    fun updateNotificationState(){
        updatePlaybackState()
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
                    if(isFav == true) R.drawable.ic_favorite else R.drawable.ic_favorite_border // The icon for the action button
                ).setExtras(Bundle().apply { // Optional: Add extras if needed
                }).build()
            )
            .setBufferedPosition(exoPlayer.bufferedPosition)

        mediaSession.setPlaybackState(playbackStateBuilder.build())
        val metadataBuilder = MediaMetadataCompat.Builder()

        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
            if (exoPlayer.duration > 0) exoPlayer.duration else -1)

        mediaSession.setMetadata(metadataBuilder.build())
    }

    fun cancelNotification() {
        notificationManager.cancel(notificationId)
    }

    companion object {
        private const val CHANNEL_ID = "music_playback_channel"
    }


}