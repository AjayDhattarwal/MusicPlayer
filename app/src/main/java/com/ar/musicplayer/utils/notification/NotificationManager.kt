package com.ar.musicplayer.utils.notification

import MusicPlayer
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.ar.musicplayer.R
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.ui.theme.MusicPlayerTheme
import com.ar.musicplayer.utils.MusicPlayerSingleton
import okhttp3.internal.notify
@UnstableApi
class NotificationManager(
    private val context: Context,
    private val musicPlayer: MusicPlayer,
    private val exoPlayer: ExoPlayer,
    private val notificationId: Int = 1
) {
    private val viewModel = musicPlayer.getViewModel()
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val mediaSession = MediaSessionCompat(context, "MusicPlayerSession")
    private val playerListener = @UnstableApi
    object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlaybackState()

        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
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
        })
    }

    fun showNotification(song: SongResponse, isPlaying: Boolean, bitmap: Bitmap? = null) {
        val notification = createNotification(song, isPlaying, bitmap)
        notificationManager.notify(notificationId, notification)
    }

    fun createNotification(song: SongResponse, isPlaying: Boolean, bitmap: Bitmap?): Notification {

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
                    else -> PlaybackStateCompat.STATE_NONE
                },
                exoPlayer.currentPosition,
                1.0f
            )
            .addCustomAction( // Define your favorite action
                PlaybackStateCompat.CustomAction.Builder(
                    "ACTION_FAVORITE", // A unique string identifier for your action
                    "Favorite", // The text to display on the action button
                    R.drawable.ic_favorite_border // The icon for the action button
                ).setExtras(Bundle().apply { // Optional: Add extras if needed
                    // putExtra("EXTRA_KEY", extraValue)
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
















//package com.ar.musicplayer.utils.notification
//
//
//import MusicPlayer
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.graphics.drawable.Drawable
//import android.os.Build
//import android.support.v4.media.MediaMetadataCompat
//import android.support.v4.media.session.MediaSessionCompat
//import android.support.v4.media.session.PlaybackStateCompat
//import android.util.Log
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.ui.graphics.Color
//import androidx.lifecycle.lifecycleScope
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import androidx.lifecycle.LifecycleOwner
//import androidx.media.app.NotificationCompat.MediaStyle
//import androidx.media3.common.Player
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.ui.PlayerNotificationManager
//import com.ar.musicplayer.R
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.DecodeFormat
//import com.bumptech.glide.load.engine.DiskCacheStrategy
//import com.bumptech.glide.request.RequestOptions
//import com.bumptech.glide.request.target.CustomTarget
//import com.bumptech.glide.request.transition.Transition
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.firstOrNull
//import kotlinx.coroutines.flow.getAndUpdate
//import kotlinx.coroutines.flow.launchIn
//import kotlinx.coroutines.flow.observeOn
//import kotlinx.coroutines.flow.onEach
//import kotlinx.coroutines.flow.updateAndGet
//import kotlinx.coroutines.launch
//
//@UnstableApi
//class NotificationManager(
//    private val context: Context,
//    private val musicPlayer: MusicPlayer,
//    private val exoPlayer: ExoPlayer,
//    private val notificationId: Int = 1
//) {
//    private val viewModel = musicPlayer.getViewModel()
//    private val notificationManager = NotificationManagerCompat.from(context)
//    private lateinit var playerNotificationManager: PlayerNotificationManager
//    private val mediaSession = MediaSessionCompat(context, "MusicPlayerSession")
//    val bitmap = mutableStateOf<Bitmap?>(null)
//
//    private val playerListener = object : Player.Listener {
//        override fun onPlaybackStateChanged(playbackState: Int) {
//            updatePlaybackState()
//        }
//
//        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
//            updatePlaybackState()
//        }
//
//        override fun onPositionDiscontinuity(reason: Int) {
//            updatePlaybackState()
//        }
//    }
//
//    private val mediaDescriptionAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
//        override fun getCurrentContentTitle(player: Player): CharSequence {
//            return viewModel.currentSong.value?.title ?: "Unknown Title"
//        }
//
//        override fun createCurrentContentIntent(player: Player): PendingIntent? {
//            // Implement to create a pending intent to open the activity
//            return null
//        }
//
//        override fun getCurrentContentText(player: Player): CharSequence? {
//            return viewModel.currentSong.value?.subtitle ?: "Unknown Subtitle"
//        }
//
//        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
//            val currentSong = viewModel.currentSong ?: return null
//            val largeIconUrl = currentSong.value?.image?.replace("150x150","500x500")
//            Log.d("url",largeIconUrl.toString())
//            if (largeIconUrl != null) {
//                Glide.with(context)
//                    .asBitmap()
//                    .load(largeIconUrl)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(object : CustomTarget<Bitmap>() {
//                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                            callback.onBitmap(resource)
//                        }
//
//                        override fun onLoadCleared(placeholder: Drawable?) {
//                        }
//                    })
//                return null
//            } else {
//                return null
//            }
//        }
//    }
//
//    private val notificationListener = object : PlayerNotificationManager.NotificationListener {
//        override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
//            Log.d("NotificationManager", "Notification posted: $notificationId, ongoing: $ongoing")
//        }
//
//        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
//            Log.d("NotificationManager", "Notification cancelled: $notificationId, dismissedByUser: $dismissedByUser")
//        }
//    }
//
//    init {
//        bitmap.value = null
//        createNotificationChannel()
//        initializeMediaSession()
//        initializePlayerNotificationManager()
//        exoPlayer.addListener(playerListener)
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                "Music Playback",
//                NotificationManager.IMPORTANCE_LOW
//            ).apply {
//                description = "Notification channel for music playback controls"
//            }
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//
//    fun updatePlaybackState() {
//        val playbackStateBuilder = PlaybackStateCompat.Builder()
//            .setActions(
//                PlaybackStateCompat.ACTION_PLAY or
//                        PlaybackStateCompat.ACTION_PAUSE or
//                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
//                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
//                        PlaybackStateCompat.ACTION_SEEK_TO
//            )
//            .setState(
//                when (exoPlayer.playbackState) {
//                    Player.STATE_READY -> if (exoPlayer.playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
//                    Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
//                    else -> PlaybackStateCompat.STATE_NONE
//                },
//                exoPlayer.currentPosition,
//                1.0f
//            )
//            .setBufferedPosition(exoPlayer.bufferedPosition)
//        mediaSession.setPlaybackState(playbackStateBuilder.build())
//
//        // Update MediaMetadata with duration
//        val metadataBuilder = MediaMetadataCompat.Builder()
//
//        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
//            if (exoPlayer.duration > 0) exoPlayer.duration else -1)
//
//
//        mediaSession.setMetadata(metadataBuilder.build())
//    }
//
//    private fun initializePlayerNotificationManager() {
//        playerNotificationManager = PlayerNotificationManager.Builder(context, notificationId, CHANNEL_ID)
//            .setMediaDescriptionAdapter(mediaDescriptionAdapter)
//            .setNotificationListener(notificationListener)
//            .setSmallIconResourceId(R.drawable.ic_music_note_24)
//            .build()
//            .apply {
//                setMediaSessionToken(mediaSession.sessionToken)
//                setUseNextActionInCompactView(true)
//                setUsePreviousActionInCompactView(true)
//                setPriority(NotificationCompat.PRIORITY_LOW)
//                setPlayer(exoPlayer)
//            }
//
//    }
//
//    private fun initializeMediaSession() {
//        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
//            override fun onSeekTo(pos: Long) {
//                exoPlayer.seekTo(pos)
//            }
//
//            override fun onPlay() {
//                viewModel.play()
//            }
//
//            override fun onPause() {
//                viewModel.pause()
//            }
//
//            override fun onSkipToPrevious() {
//                musicPlayer.skipToPrevious()
//            }
//
//            override fun onSkipToNext() {
//                musicPlayer.skipToNext()
//            }
//        })
//        mediaSession.isActive = true
//    }
//
//
//    companion object {
//        private const val CHANNEL_ID = "music_playback_channel"
//    }
//}
//

