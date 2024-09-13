package com.ar.musicplayer.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.*
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.data.repository.LastSessionRepository
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.repository.PlayerRepository
import com.ar.musicplayer.utils.notification.ACTIONS
import com.ar.musicplayer.utils.notification.AudioService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val playerRepository: PlayerRepository,
    private val lastSessionRepository: LastSessionRepository
) : AndroidViewModel(application) {

    val currentPosition: StateFlow<Long> = playerRepository.currentPosition
    val duration: StateFlow<Long> = playerRepository.duration
    val currentIndex: StateFlow<Int> = playerRepository.currentIndex
    val isPlaying: StateFlow<Boolean> = playerRepository.isPlaying
    val currentSong: StateFlow<SongResponse?> = playerRepository.currentSong
    val playlist: StateFlow<List<SongResponse>> = playerRepository.playlist
    val currentPlaylistId: StateFlow<String?> = playerRepository.currentPlaylistId
    val showBottomSheet: StateFlow<Boolean> = playerRepository.showBottomSheet
    val repeatMode = playerRepository.repeatMode
    val shuffleModeEnabled = playerRepository.shuffleModeEnabled
    val isBuffering = playerRepository.isBuffering

    val lastSession = lastSessionRepository.lastSession
    val listeningHistory = lastSessionRepository.listeningHistory


    val currentLyricIndex = playerRepository.currentLyricIndex
    val lyricsData: StateFlow<List<Pair<Int, String>>> = playerRepository.lyricsData
    val isLyricsLoading: StateFlow<Boolean> = playerRepository.isLyricsLoading

    private val _currentSongColor = MutableStateFlow(Color.Gray)
    val currentSongColor: StateFlow<Color> = _currentSongColor


    fun playPause() {
        playerRepository.playPause()
    }

    fun seekTo(position: Long) {
        playerRepository.seekTo(position)
    }

    fun setRepeatMode(mode: Int) {
        playerRepository.setRepeatMode(mode)
    }

    fun toggleShuffleMode() {
        playerRepository.toggleShuffleMode()
    }


    fun setNewTrack(song: SongResponse){
        playerRepository.setNewTrack(song)
    }

    fun skipNext() {
        playerRepository.skipNext()
    }

    fun skipPrevious() {
        playerRepository.skipPrevious()
    }


    fun changeSong(index: Int){
        playerRepository.changeSong(index)
    }

    fun setPlaylist(newPlaylist: List<SongResponse>, playlistId: String) {
        try {
            // Ensure the playlist is not null
            checkNotNull(playlist) { "Playlist is null" }

            playerRepository.setPlaylist(newPlaylist, playlistId)

        } catch (e: Exception) {
            Log.e("PlayerRepository", "Error setting playlist", e)
        }
    }

    fun removeTrack(index: Int){
        playerRepository.removeTrack(index)
    }

    fun replaceIndex(add: Int, remove: Int) {
        playerRepository.replaceIndex(add, remove)
    }

    fun setCurrentSongColor(color: Color){
        _currentSongColor.value = color
    }


    override fun onCleared() {
        val context = getApplication<Application>().applicationContext
        super.onCleared()

        playerRepository.destroy()

        Intent(context, AudioService::class.java).also {
            it.action = ACTIONS.STOP.toString()
            context.stopService(it)
        }
    }


}




