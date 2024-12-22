package com.ar.musicplayer.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.*
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.data.repository.LastSessionRepository
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.repository.PlayerRepository
import com.ar.musicplayer.utils.helper.NetworkConnectivityObserver
import com.ar.musicplayer.utils.notification.ACTIONS
import com.ar.musicplayer.utils.notification.AudioService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val playerRepository: PlayerRepository,
    private val lastSessionRepository: LastSessionRepository,
    private val networkConnectivityObserver: NetworkConnectivityObserver
) : AndroidViewModel(application) {



    val currentPosition: StateFlow<Long>  get() = playerRepository.currentPosition
    val duration: StateFlow<Long> get() = playerRepository.duration
    val currentIndex: StateFlow<Int> get() = playerRepository.currentIndex
    val isPlaying: StateFlow<Boolean> get() = playerRepository.isPlaying
    val currentSong: StateFlow<SongResponse?> get() = playerRepository.currentSong
    val playlist: StateFlow<List<SongResponse>> get() = playerRepository.playlist
    val currentPlaylistId: StateFlow<String?> get() = playerRepository.currentPlaylistId
    val showBottomSheet: StateFlow<Boolean> get() = playerRepository.showBottomSheet
    val repeatMode get() = playerRepository.repeatMode
    val shuffleModeEnabled get() = playerRepository.shuffleModeEnabled
    val isBuffering  get() = playerRepository.isBuffering

    val lastSession get() = lastSessionRepository.lastSession
    val listeningHistory get() = lastSessionRepository.listeningHistory


    val currentLyricIndex get() = playerRepository.currentLyricIndex
    val lyricsData: StateFlow<List<Pair<Int, String>>> get() = playerRepository.lyricsData
    val isLyricsLoading: StateFlow<Boolean> get() = playerRepository.isLyricsLoading

    private val _currentSongColor = MutableStateFlow(Color.Gray)
    val currentSongColor: StateFlow<Color> get() = _currentSongColor


    init {
        viewModelScope.launch {

            networkConnectivityObserver.observe().collect { isConnected ->
                if (isConnected) {
                    playerRepository.retryPlayback()
                    playerRepository.updateNotification()
                }
            }
        }
    }


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
        viewModelScope.launch{
            try {
                checkNotNull(playlist) { "Playlist is null" }

                playerRepository.setPlaylist(newPlaylist, playlistId)

            } catch (e: Exception) {
                Log.e("PlayerRepository", "Error setting playlist", e)
            }
        }
    }

    fun removeTrack(index: Int){
        viewModelScope.launch{ playerRepository.removeTrack(index) }
    }

    fun replaceIndex(add: Int, remove: Int) {
        viewModelScope.launch { playerRepository.replaceIndex(add, remove) }
    }

    fun setCurrentSongColor(color: Color){
        _currentSongColor.value = color
    }


    override fun onCleared() {
        super.onCleared()
        playerRepository.destroy()
    }

}




