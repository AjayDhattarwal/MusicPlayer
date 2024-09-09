package com.ar.musicplayer.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.traceEventEnd
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.trace
import androidx.lifecycle.*
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.data.repository.LastSessionRepository
import com.ar.musicplayer.data.repository.SongDetailsRepository
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.api.ApiConfig
import com.ar.musicplayer.api.ApiService
import com.ar.musicplayer.data.models.LyricsResponse
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.TranslationResponse
import com.ar.musicplayer.data.models.perfect
import com.ar.musicplayer.data.models.permutations
import com.ar.musicplayer.data.repository.PlayerRepository
import com.ar.musicplayer.utils.notification.ACTIONS
import com.ar.musicplayer.utils.notification.AudioService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.switchMap
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.inject.Inject
import kotlin.math.absoluteValue

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

    fun addMediaItems(mediaItems: List<MediaItem>, startIndex: Int = 0) {
        playerRepository.addMediaItems(mediaItems, startIndex)
    }

    fun clearMediaItems() {
        playerRepository.clearMediaItems()
    }

    fun getRecommendations(id: String, call: String = "reco.getreco") {
        playerRepository.getRecommendations(id, call)
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

    fun toggleFavourite(){
//        playerRepository.toggleFavourite()
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



//
//    private fun loadLastSession() {
//        lastSessionJob = viewModelScope.launch{
//            val lastSession = lastSessionRepository.getLastSessionForPlaying()
//            if (lastSession.isNotEmpty()) {
//                _currentPlaylistId.postValue("history")
//                _playlist.value = lastSession.reversed().map { (_, songResponse) -> songResponse }
//                val mediaItems = lastSession.reversed().map { (_, song) ->
//                    val artist = song.moreInfo?.artistMap?.artists?.distinctBy{it.name}?.joinToString(", "){it.name.toString()}
//                    MediaItem.Builder()
//                        .setUri(
//                            decodeDES(
//                                song.moreInfo?.encryptedMediaUrl.toString(),
//                                song.moreInfo?.kbps320 ?: false
//                            )
//                        )
//                        .setMediaMetadata(
//                            MediaMetadata.Builder()
//                                .setTitle(song.title?.perfect())
//                                .setArtworkUri(Uri.parse(song.image))
//                                .setSubtitle(song.subtitle?.perfect())
//                                .setArtist(artist?.perfect())
//                                .setAlbumTitle(song.moreInfo?.album?.perfect() ?: song.title?.perfect() )
//                                .setDurationMs(song.moreInfo?.duration?.toLong())
//                                .build()
//                        )
//                        .build()
//                }
//                exoPlayer.setMediaItems(mediaItems, mediaItems.size - 1, 0)
//                exoPlayer.prepare()
//                showBottomSheet.value = true
//                exoPlayer.pause()
//                delay(2000)
//                getRecommendations(playlist.value!![currentIndex.value!!].id.toString())
//            }
//        }
//    }



//    fun changeSong(index: Int) {
//        _playlist.value?.let { playlist ->
//            if (index >= 0 && index < playlist.size) {
//                _currentIndex.value = index
//                exoPlayer.seekToDefaultPosition(index)
//                seekTo(0)
//                exoPlayer.play()
//            }
//        }
//    }


//    fun setNewTrack(song: SongResponse) {
//        if(currentPlaylistId.value != "history"){
//            _playlist.value = emptyList()
//            exoPlayer.clearMediaItems()
//            _currentPlaylistId.value = "history"
//        }
//        if (song.moreInfo?.encryptedMediaUrl.isNullOrEmpty()) {
//            makePerfectSong(song) { perfectSong ->
//                addSongInPlaylist(perfectSong)
//            }
//        } else {
//            addSongInPlaylist(song)
//        }
//        if(!showBottomSheet.value){
//            showBottomSheet.value = true
//        }
//    }



//    private fun addSongInPlaylist(song: SongResponse) {
//        _playlist.value = playlist.value.orEmpty() + song
//        val artist = song.moreInfo?.artistMap?.artists?.distinctBy{it.name}?.joinToString(", "){it.name.toString()}
//
//        val mediaItem = MediaItem.Builder()
//            .setUri(decodeDES(
//                song.moreInfo?.encryptedMediaUrl.toString(),
//                song.moreInfo?.kbps320 ?: false
//            ))
//            .setMediaMetadata(
//                MediaMetadata.Builder()
//                    .setTitle(song.title?.perfect())
//                    .setArtworkUri(Uri.parse(song.image))
//                    .setSubtitle(song.subtitle?.perfect())
//                    .setArtist(artist?.perfect())
//                    .setAlbumTitle(song.moreInfo?.album?.perfect() ?: song.title?.perfect() )
//                    .setDurationMs(song.moreInfo?.duration?.toLong())
//                    .build()
//            )
//            .build()
//
//        exoPlayer.addMediaItem(mediaItem)
//        exoPlayer.prepare()
//        exoPlayer.seekToDefaultPosition(exoPlayer.mediaItemCount - 1)
//    }
////
////



//
//    private fun addRecoSongInPlaylist(song: SongResponse) {
//
//        if(_playlist.value?.size!! > 100){
//            _playlist.value = _playlist.value!!.takeLast(100)
//            val lastIndex = exoPlayer.mediaItemCount - _playlist.value!!.size
//            exoPlayer.removeMediaItems(0, lastIndex)
//            exoPlayer.prepare()
//            _currentIndex.value = exoPlayer.currentMediaItemIndex
//        }
//
//        _playlist.value  = playlist.value?.plus(song)?.distinct()
//
//        val artist = song.moreInfo?.artistMap?.artists?.distinctBy{it.name}?.joinToString(", "){it.name.toString()}
//
//        val mediaItem = MediaItem.Builder()
//            .setUri(
//                decodeDES(
//                    song.moreInfo?.encryptedMediaUrl.toString(),
//                    song.moreInfo?.kbps320 ?: false
//                )
//            )
//            .setMediaMetadata(
//                MediaMetadata.Builder()
//                    .setTitle(song.title?.perfect())
//                    .setArtworkUri(Uri.parse(song.image))
//                    .setSubtitle(song.subtitle?.perfect())
//                    .setArtist(artist?.perfect())
//                    .setAlbumTitle(song.moreInfo?.album?.perfect() ?: song.title?.perfect() )
//                    .setDurationMs(song.moreInfo?.duration?.toLong())
//                    .build()
//            )
//            .build()
//
//        exoPlayer.addMediaItem(mediaItem)
//
//        Log.d("testcase", "playlist size ${_playlist.value!!.size}, ,,  player current index ${exoPlayer.currentMediaItemIndex} ,,,, current Index ${currentIndex.value}")
//    }
//

