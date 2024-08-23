package com.ar.musicplayer.data.repository

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.utils.notification.ACTIONS
import com.ar.musicplayer.utils.notification.AudioService
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlayerRepository @Inject constructor(
    private val application: Context,
    private val exoPlayer: ExoPlayer,
    private val lyricRepository: LyricRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main) // For coroutine scope
) {

    private val _currentPosition = MutableStateFlow(exoPlayer.currentPosition)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(exoPlayer.duration.takeIf { it > 0 } ?: 0L)
    val duration: StateFlow<Long> = _duration

    private val _currentIndex = MutableStateFlow(exoPlayer.currentMediaItemIndex)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _lyricsData = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val lyricsData: StateFlow<List<Pair<Int, String>>> = _lyricsData

    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading: StateFlow<Boolean> = _isLyricsLoading

    private val _isPlaying = MutableStateFlow(exoPlayer.isPlaying)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _playlist = MutableLiveData<List<SongResponse>>()
    val playlist: LiveData<List<SongResponse>> get() = _playlist

    private val handler = Handler(Looper.getMainLooper())
    private val updateLyricsRunnable = Runnable { /* Update lyrics logic */ }

    private var isServiceStarted = false

    private val playerListener = @UnstableApi
    object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val newPosition = exoPlayer.currentPosition
            val newDuration = exoPlayer.duration.takeIf { it > 0 } ?: 0L

            if (_currentPosition.value != newPosition) {
                _currentPosition.value = newPosition
            }
            if (_duration.value != newDuration) {
                _duration.value = newDuration
            }
        }

        @UnstableApi
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val newIndex = exoPlayer.currentMediaItemIndex
            _lyricsData.value = emptyList()
            if (_currentIndex.value != newIndex) {
                _currentIndex.value = newIndex
            }
            if (mediaItem?.mediaMetadata?.title != null) {
                val title = exoPlayer.currentMediaItem?.mediaMetadata?.title.toString()
                val artistList = exoPlayer.currentMediaItem?.mediaMetadata?.artist
                    ?.split(",", "&amp;", "with", "&quot;")
                    ?.map { it.trim() }
                    ?: listOf()

                val albumName = exoPlayer.currentMediaItem?.mediaMetadata?.albumTitle.toString()
                val duration = exoPlayer.currentMediaItem?.mediaMetadata?.durationMs

                _isLyricsLoading.value = true
                scope.launch {
                    lyricRepository.fetchLyrics(
                        trackName = title,
                        artistList = artistList,
                        albumName = albumName,
                        duration = duration?.toInt() ?: 0,
                        onSuccess = {
                            _lyricsData.value = it
                            _isLyricsLoading.value = false
                        },
                        onError = {
                            Log.d("lyrics", "${it.message}")
                            _lyricsData.value = emptyList()
                            _isLyricsLoading.value = false
                        }
                    )
                }
                updateLastSession()
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) {
                if (!isServiceStarted && (playlist.value?.size ?: 0) > 0) {
                    startForegroundService()
                    isServiceStarted = true
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_READY && playWhenReady) {
                handler.post(updateLyricsRunnable)
            } else {
                handler.removeCallbacks(updateLyricsRunnable)
            }
        }
    }

    init {
        exoPlayer.addListener(playerListener)
        loadLastSession()
    }

    fun playPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    fun setRepeatMode(mode: Int) {
        exoPlayer.repeatMode = mode
    }

    fun toggleShuffleMode() {
        exoPlayer.shuffleModeEnabled = !exoPlayer.shuffleModeEnabled
    }

    fun addMediaItems(mediaItems: List<MediaItem>, startIndex: Int = 0) {
        exoPlayer.setMediaItems(mediaItems, startIndex, 0)
        exoPlayer.prepare()
    }

    fun clearMediaItems() {
        exoPlayer.clearMediaItems()
    }

    private fun updateLastSession() {
        // Your logic to update the last session
    }

    private fun loadLastSession() {
        // Your logic to load the last session
    }

    @UnstableApi
    private fun startForegroundService() {
        val intent = Intent(application, AudioService::class.java).apply {
            action = ACTIONS.START.toString()
        }
        application.startService(intent)
    }
}