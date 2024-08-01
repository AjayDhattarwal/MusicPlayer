package com.ar.musicplayer.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.lifecycle.*
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil.ImageLoader
import coil.decode.BitmapFactoryDecoder
import coil.request.ImageRequest
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionEvent
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionViewModel
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.screens.player.DetailsViewModel
import com.ar.musicplayer.utils.events.DetailsEvent
import com.ar.musicplayer.utils.notification.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext
import okhttp3.Callback
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
    private val lastSessionViewModel: LastSessionViewModel,
    private val detailsViewModel: DetailsViewModel,
) : AndroidViewModel(application) {


    private var job: Job? = null
    private var lastSessionJob: Job? = null

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
            val song = _playlist.value?.getOrNull(index)
            emit(song)

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

    val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val newPosition = player.currentPosition
            val newDuration = player.duration.takeIf { it > 0 } ?: 0L

            if (_currentPosition.value != newPosition) {
                _currentPosition.postValue(newPosition)
            }
            if (_duration.value != newDuration) {
                _duration.postValue(newDuration)
            }

            if (playbackState == Player.STATE_READY) {
                preloadImages()
                updateLastSession()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val newIndex = player.currentMediaItemIndex
            if (_currentIndex.value != newIndex) {
                _currentIndex.value = newIndex
                preloadImages()
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.postValue(isPlaying)
            updateNotification()

        }
    }


    init {
        player.addListener(playerListener)
        loadLastSession()


    }


    private fun loadLastSession() {
        lastSessionJob = viewModelScope.launch{
            val lastSession = lastSessionViewModel.getLastSessionForPlaying()
            if (lastSession.isNotEmpty()) {
                _currentPlaylistId.postValue("history")
                _playlist.value = lastSession.reversed().map { (_, songResponse) -> songResponse }
                val mediaItems = lastSession.reversed().map { (_, songResponse) ->
                    MediaItem.Builder()
                        .setUri(decodeDES(songResponse.moreInfo?.encryptedMediaUrl.toString()))
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(songResponse.title)
                                .build()
                        )
                        .build()
                }
                player.setMediaItems(mediaItems, mediaItems.size - 1, 0)
                player.prepare()
                player.pause()
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
                player.seekToDefaultPosition(index)
                seekTo(0)
                player.play()
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
        if(currentPlaylistId.value != "history"){
            _playlist.value = emptyList()
            player.clearMediaItems()
            _currentPlaylistId.value = "history"
        }
        val currentPlaylist = _playlist.value?.toMutableList() ?: mutableListOf()
        if (song.moreInfo?.encryptedMediaUrl.isNullOrEmpty()) {
            makePerfectSong(song) { perfectSong ->
                updateSongInPlaylist(perfectSong, currentPlaylist)
            }
        } else {
            updateSongInPlaylist(song, currentPlaylist)
        }
    }
    fun makePerfectSong(song: SongResponse, onCallback: (SongResponse) -> Unit) {
        detailsViewModel.onEvent(
            DetailsEvent.getSongDetails(
                song.id.toString(),
                "song.getDetails",
                callback = onCallback
            )
        )
    }

    private fun updateSongInPlaylist(song: SongResponse, currentPlaylist: MutableList<SongResponse>) {
        var currentIndex = _currentIndex.value ?: 0
        if (currentPlaylist.contains(song)) {
            currentPlaylist.remove(song)
            currentPlaylist.add(song)
        } else {
            currentPlaylist.add(song)
        }
        currentIndex = currentPlaylist.size - 1

        _playlist.value = currentPlaylist
        _currentIndex.value = currentIndex

        val mediaItems = currentPlaylist.map { song ->
            MediaItem.Builder()
                .setUri(decodeDES(song.moreInfo?.encryptedMediaUrl.toString()))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title) // Assuming songResponse has the song name
                        .build()
                )
                .build()

        }
        player.setMediaItems(mediaItems, currentIndex, 0)
        player.prepare()
        player.play()
    }






    fun setPlaylist(newPlaylist: List<SongResponse>, playlistId: String) {
        _playlist.value = newPlaylist
        _currentIndex.value = 0
        _currentPlaylistId.value = playlistId

        val mediaItems = newPlaylist.map { song ->
            MediaItem.Builder()
                .setUri(decodeDES(song.moreInfo?.encryptedMediaUrl.toString()))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title) // Assuming songResponse has the song name
                        .build()
                )
                .build()
        }
        player.setMediaItems(mediaItems)
        player.prepare()
        player.play()
        updateNotification()
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
        Intent(context, NotificationService::class.java).also{
            it.action = NotificationService.Actions.START.toString()
            context.startService(it)
        }
    }



    private fun preloadImages() {
        viewModelScope.launch {
            if(playlist.value?.isNotEmpty() == true){
                val currentPlaylist = _playlist.value ?: emptyList()
                val currentIndex = _currentIndex.value ?: 0
                loadImage(currentPlaylist[currentIndex].image?.replace("150x150","350x350")){ bitmap ->

                    if (bitmap != null) {
                        _preloadedImage.postValue(bitmap)
                        updateNotification()
                    } else {
                        Log.e("ImageLoading", "Failed to load image")
                    }
                }

            }
        }
    }

    private suspend fun loadImage(url: String?, onSuccess: (Bitmap?) -> Unit) {
        withContext(Dispatchers.IO) {
            val bitmap = url?.let {
                val loader = ImageLoader(getApplication<Application>().applicationContext)
                val request = ImageRequest.Builder(getApplication<Application>().applicationContext)
                    .data(it)
                    .allowHardware(false)
                    .decoderFactory(BitmapFactoryDecoder.Factory())
                    .build()
                runCatching {
                    (loader.execute(request).drawable as? BitmapDrawable)?.bitmap
                }.getOrNull() // Handle potential exceptions
            }
            onSuccess(bitmap) // Call the callback with the Bitmap (or null if it failed)
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


    private fun updateLastSession(){
        job?.cancel()
        lastSessionViewModel.onEvent(
            LastSessionEvent.InsertLastPlayedData(
                songResponse = currentSong.value!!,
                playCount = 0,
                skipCount = 1
            )
        )
        val playStartTime = System.currentTimeMillis()
        job = viewModelScope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(10000)
            val elapsedTime = System.currentTimeMillis() - playStartTime
            val playCount = if (elapsedTime >= 10000) 1 else 0
            val skipCount = -1
            Log.d("LastSessionService", "Elapsed time: $elapsedTime, playCount: $playCount, skipCount: $skipCount")
            lastSessionViewModel.onEvent(
                LastSessionEvent.InsertLastPlayedData(
                    songResponse = currentSong.value!!,
                    playCount = playCount,
                    skipCount = skipCount
                )
            )
        }

    }

    override fun onCleared() {
        super.onCleared()
        player.removeListener(playerListener)
        player.clearMediaItems()
        _playlist.value = emptyList()
        _currentIndex.value = 0
        _currentPlaylistId.value = ""
        _isPlaying.value = false
        Log.d("kill", "player viewModel cleared")
    }
}
