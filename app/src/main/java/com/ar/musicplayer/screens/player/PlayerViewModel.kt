package com.ar.musicplayer.screens.player

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.lifecycle.*
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player
import coil.ImageLoader
import coil.request.ImageRequest
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionEvent
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionViewModel
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.utils.notification.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.Base64
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val player: ExoPlayer,
    private val lastSessionViewModel: LastSessionViewModel
) : AndroidViewModel(application) {


    private val _preloadedImage = MutableLiveData<Bitmap?>()
    val preloadedImage: LiveData<Bitmap?> get() = _preloadedImage

    private val _playlist = MutableLiveData<List<SongResponse>>()
    val playlist: LiveData<List<SongResponse>> get() = _playlist

    private val _currentPlaylistId = MutableLiveData<String?>()
    val currentPlaylistId: LiveData<String?> get() = _currentPlaylistId

    private val _currentIndex = MutableLiveData<Int>()
    val currentIndex: LiveData<Int> get() = _currentIndex

    val currentSong: LiveData<SongResponse?> = currentIndex.switchMap { index ->
        liveData {
            emit(_playlist.value?.getOrNull(index))
        }
    }

    private val _isPlaying = MutableLiveData(player.isPlaying)
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _currentPosition = MutableLiveData(player.currentPosition)
    val currentPosition: LiveData<Long> get() = _currentPosition

    private val _duration = MutableLiveData(player.duration.takeIf { it > 0 } ?: 0L)
    val duration: LiveData<Long> get() = _duration

    private val _repeatMode = MutableLiveData(player.repeatMode)
    val repeatMode: LiveData<Int> get() = _repeatMode

    private val _shuffleModeEnabled = MutableLiveData(player.shuffleModeEnabled)
    val shuffleModeEnabled: LiveData<Boolean> get() = _shuffleModeEnabled

    private val _isFavourite = MutableLiveData(false)
    val isFavourite: LiveData<Boolean> get() = _isFavourite

    private val _isDownloaded = MutableLiveData(false)
    val isDownloaded: LiveData<Boolean> get() = _isDownloaded

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val newPosition = player.currentPosition
                val newDuration = player.duration.takeIf { it > 0 } ?: 0L

                // Only update if there's a change
                if (_currentPosition.value != newPosition) {
                    _currentPosition.postValue(newPosition)
                }
                if (_duration.value != newDuration) {
                    _duration.postValue(newDuration)
                }

                // Only preload and update notifications when necessary
                if (playbackState == Player.STATE_READY) {
                    preloadImages()
                    updateNotification()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val newIndex = player.currentMediaItemIndex

                if (_currentIndex.value != newIndex) {
//                    _currentIndex.value = newIndex
                    updateNotification()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // Only update if there's a change
                if (_isPlaying.value != isPlaying) {
                    _isPlaying.postValue(isPlaying)
                    updateNotification()
                }
            }
        })
        loadLastSession()
    }


    private fun loadLastSession() {
        lastSessionViewModel.onEvent(LastSessionEvent.LoadLastSessionData)
        lastSessionViewModel.lastSession.observeForever { session ->
            session?.let {

                val mediaItems = session.map { song ->
                    MediaItem.fromUri(decodeDES(song.moreInfo?.encryptedMediaUrl.toString()))
                }

                Log.d("MusicPlayerViewModel", "Loaded playlist: ${session.size}")
                Log.d("MusicPlayerViewModel", "Playlist size: ${playlist.value?.size}")
                Log.d("MusicPlayerViewModel", "Current index: ${currentIndex.value}")

                player.setMediaItems(mediaItems, mediaItems.size -1, 0)
                player.prepare()
                player.pause()
                viewModelScope.launch {
                    delay(Duration.ofSeconds(5))
                    _playlist.value = session
                    _currentIndex.value = session.size - 1
                }

            }
        }
    }



    fun playPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun changeSong(index: Int) {
        _playlist.value?.let { playlist ->
            if (index >= 0 && index < playlist.size) {
                _currentIndex.value = index
                Log.d("MusicPlayerViewModel", "Seeking to index: $index")
                player.seekToDefaultPosition(index)  // Seek to the new index
                seekTo(0)
                player.play()  // Start playback
            }
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
        _currentPosition.postValue(player.currentPosition)
    }

    fun setRepeatMode(mode: Int) {
        player.repeatMode = mode
        _repeatMode.postValue(mode)
    }

    fun toggleShuffleMode() {
        val newShuffleMode = !player.shuffleModeEnabled
        player.shuffleModeEnabled = newShuffleMode
        _shuffleModeEnabled.postValue(newShuffleMode)
    }

    fun toggleFavourite() {
        _isFavourite.postValue(!(_isFavourite.value ?: false))
    }

    fun setNewTrack(song: SongResponse) {
        val currentPlaylist = _playlist.value?.toMutableList() ?: mutableListOf()
        var currentIndex = _currentIndex.value ?: 0

        if (_playlist.value?.contains(song) == true) {
            currentPlaylist.remove(song)
            currentPlaylist.add(song)
            currentIndex = currentPlaylist.size - 1
        } else {
            currentPlaylist.add(song)
            currentIndex = currentPlaylist.size - 1
        }

        _playlist.value = currentPlaylist
        _currentIndex.value = currentIndex

        val mediaItems = currentPlaylist.map {
            MediaItem.fromUri(decodeDES(it.moreInfo?.encryptedMediaUrl.toString()))
        }
        player.setMediaItems(mediaItems, currentIndex, 0)
        player.prepare()
        player.play()

    }

    fun setPlaylist(newPlaylist: List<SongResponse>, playlistId: String) {
        viewModelScope.launch {
            _playlist.value = newPlaylist
            _currentIndex.value = 0
            _currentPlaylistId.value = playlistId

            val mediaItems = newPlaylist.map { song ->
                MediaItem.fromUri(decodeDES(song.moreInfo?.encryptedMediaUrl.toString()))
            }
            player.setMediaItems(mediaItems)
            player.prepare()
            player.play()
            updateNotification()
        }
    }



    fun skipNext() {
        val currentIndex = _currentIndex.value ?: return
        val playlistSize = _playlist.value?.size ?: return

        if (currentIndex < playlistSize - 1) {
            changeSong(currentIndex + 1)
        } else {
            changeSong(0) // Go to the first song if at the end of the playlist
        }
    }

    fun skipPrevious() {
        val currentIndex = _currentIndex.value ?: return

        if (currentIndex > 0) {
            changeSong(currentIndex - 1)
        } else {
            changeSong(_playlist.value?.size?.minus(1) ?: 0) // Go to the last song if at the beginning of the playlist
        }
    }

    private fun updateNotification() {
        val context = getApplication<Application>().applicationContext
        val intent = Intent(context, NotificationService::class.java).apply {
            action = NotificationService.ACTION_UPDATE_NOTIFICATION
        }
        context.startService(intent)
    }

    fun killPlayer(){
        onCleared()
    }

    override fun onCleared() {
        super.onCleared()
        player.clearMediaItems()
        player.release()
        _playlist.value = emptyList()
        _currentIndex.value = 0
        _currentPlaylistId.value = null

    }



    private fun preloadImages() {
        viewModelScope.launch {
            if(playlist.value?.isNotEmpty() == true){
                val currentPlaylist = _playlist.value ?: emptyList()
                val currentIndex = _currentIndex.value ?: 0

                val preloadedBitmap =
                    loadImage(currentPlaylist[currentIndex].image?.replace("150x150","350x350"))
                _preloadedImage.postValue(preloadedBitmap)
            }
        }
    }

    private suspend fun loadImage(url: String?): Bitmap? {
        return withContext(Dispatchers.IO) {
            url?.let {
                val loader = ImageLoader(getApplication<Application>().applicationContext)
                val request = ImageRequest.Builder(getApplication<Application>().applicationContext)
                    .data(it)
                    .build()
                val drawable = (loader.execute(request).drawable as? BitmapDrawable)?.bitmap
                drawable
            }
        }
    }

    private fun decodeDES(input: String): String {
        Log.d("input","$input")
        val key = "38346591"
        val algorithm = "DES/ECB/PKCS5Padding"

        val keyFactory = SecretKeyFactory.getInstance("DES")
        val desKeySpec = DESKeySpec(key.toByteArray(StandardCharsets.UTF_8))
        val secretKey = keyFactory.generateSecret(desKeySpec)

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)

        val encryptedBytes = Base64.getDecoder().decode(input.replace("\\",""))
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        var decoded = String(decryptedBytes, StandardCharsets.UTF_8)

        // Replace ".mp4" pattern
        val pattern = Pattern.compile("\\.mp4.*")
        val matcher = pattern.matcher(decoded)
        decoded = matcher.replaceAll(".mp4")

        // Replace "http:" with "https:"
        decoded = decoded.replace("http:", "https:")

        return decoded
    }
}
