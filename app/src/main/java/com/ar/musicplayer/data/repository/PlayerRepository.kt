package com.ar.musicplayer.data.repository

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.api.ApiConfig
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.perfect
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.notification.ACTIONS
import com.ar.musicplayer.utils.notification.AudioService
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.switchMap
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

class PlayerRepository @Inject constructor(
    private val application: Context,
    private val exoPlayer: ExoPlayer,
    private val lyricRepository: LyricRepository,
    private val lastSessionRepository: LastSessionRepository,
    private val songDetailsRepository: SongDetailsRepository,
) {
    private var job: Job? = null
    val coroutineScope = CoroutineScope(Dispatchers.Main)
    val preferencesManager = PreferencesManager(application)

    private val _currentPosition = MutableStateFlow(exoPlayer.currentPosition)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(exoPlayer.duration.takeIf { it > 0 } ?: 0L)
    val duration: StateFlow<Long> = _duration

    private val _currentIndex = MutableStateFlow(exoPlayer.currentMediaItemIndex)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _isPlaying = MutableStateFlow(exoPlayer.isPlaying)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _playlist = MutableStateFlow<List<SongResponse>>(emptyList())
    val playlist: StateFlow<List<SongResponse>> get() = _playlist

    val currentSong: StateFlow<SongResponse?> = combine(currentIndex, playlist) { index, songs ->
        songs.getOrNull(index)
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    private val _currentPlaylistId = MutableStateFlow<String>("")
    val currentPlaylistId: StateFlow<String?> get() = _currentPlaylistId


    private val _repeatMode = MutableLiveData(exoPlayer.repeatMode)
    val repeatMode: LiveData<Int> get() = _repeatMode

    private val _shuffleModeEnabled = MutableLiveData(exoPlayer.shuffleModeEnabled)
    val shuffleModeEnabled: LiveData<Boolean> get() = _shuffleModeEnabled


    private val _lyricsData = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val lyricsData: StateFlow<List<Pair<Int, String>>> = _lyricsData

    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading: StateFlow<Boolean> = _isLyricsLoading

    private val _currentLyricIndex = MutableLiveData<Int>(0)
    val currentLyricIndex: LiveData<Int> = _currentLyricIndex

    private val handler = Handler(Looper.getMainLooper())
    private val updateLyricsRunnable = object : Runnable {
        override fun run() {
            updateLyric()
            // Schedule the next update after 0.5 second
            handler.postDelayed(this, 500L)
        }
    }

    private var isServiceStarted = false

    val showBottomSheet = MutableStateFlow<Boolean>(false)

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

                lyricRepository.fetchLyrics(
                    trackName = title,
                    artistList = artistList,
                    albumName = albumName,
                    duration = duration?.toInt() ?: 0,
                    onSuccess = {
                        Log.d("lyrics", "Lyrics ::::::    ${it}")
                        _lyricsData.value = it
                        _isLyricsLoading.value = false
                    },
                    onError = {
                        Log.d("lyrics", "${it.message}")
                        _lyricsData.value = emptyList()
                        _isLyricsLoading.value = false
                    }
                )
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
        coroutineScope.launch {
            loadLastSession()
        }
    }

    fun playPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    fun setRepeatMode(mode: Int) {
        exoPlayer.repeatMode = mode
        _repeatMode.postValue(mode)
    }

    fun toggleShuffleMode() {
        val newShuffleMode = !exoPlayer.shuffleModeEnabled
        exoPlayer.shuffleModeEnabled = newShuffleMode
        _shuffleModeEnabled.postValue(newShuffleMode)
    }

    fun addMediaItems(mediaItems: List<MediaItem>, startIndex: Int = 0) {
        exoPlayer.setMediaItems(mediaItems, startIndex, 0)
        exoPlayer.prepare()
    }

    fun clearMediaItems() {
        exoPlayer.clearMediaItems()
    }


    @OptIn(UnstableApi::class)
    private suspend fun loadLastSession() {
        val lastSession = lastSessionRepository.getLastSessionForPlaying()
        if (lastSession.isNotEmpty()) {
            _currentPlaylistId.value = "history"
            _playlist.value = lastSession.reversed().map { (_, songResponse) -> songResponse }
            val mediaItems = lastSession.reversed().map { (_, song) ->
                val artist = song.moreInfo?.artistMap?.artists?.distinctBy{it.name}?.joinToString(", "){it.name.toString()}
                MediaItem.Builder()
                    .setUri(
                        decodeDES(
                            song.moreInfo?.encryptedMediaUrl.toString(),
                            song.moreInfo?.kbps320 ?: false
                        )
                    )
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.title?.perfect())
                            .setArtworkUri(Uri.parse(song.image))
                            .setSubtitle(song.subtitle?.perfect())
                            .setArtist(artist?.perfect())
                            .setAlbumTitle(song.moreInfo?.album?.perfect() ?: song.title?.perfect() )
                            .setDurationMs(song.moreInfo?.duration?.toLong())
                            .build()
                    )
                    .build()
            }
            exoPlayer.setMediaItems(mediaItems, mediaItems.size - 1, 0)
            exoPlayer.prepare()
            showBottomSheet.value = true
            exoPlayer.pause()
            delay(2000)
            getRecommendations(playlist.value[currentIndex.value].id.toString())
        }
    }

    fun getRecommendations(id: String, call: String = "reco.getreco") {
        val client = ApiConfig.getApiService().getRecoSongs(
            pid = id,
            call = call
        )

        client.enqueue(object : Callback<List<SongResponse>> {
            @OptIn(UnstableApi::class)
            override fun onResponse(
                call: Call<List<SongResponse>>,
                response: Response<List<SongResponse>>
            ) {
                if (response.isSuccessful) {

                    response.body()?.forEach { song ->
                        if (playlist.value?.any { it.id == song.id } == false) {
                            addRecoSongInPlaylist(song)
                        }
                    }

                }
            }

            override fun onFailure(call: Call<List<SongResponse>>, t: Throwable) {
                android.util.Log.d("reco", "${t.message}")
            }
        })
    }

    @UnstableApi
    private fun addRecoSongInPlaylist(song: SongResponse) {

        if(_playlist.value.size > 100){
            _playlist.value = _playlist.value.takeLast(100)
            val lastIndex = exoPlayer.mediaItemCount - _playlist.value.size
            exoPlayer.removeMediaItems(0, lastIndex)
            exoPlayer.prepare()
            _currentIndex.value = exoPlayer.currentMediaItemIndex
        }

        _playlist.value  = playlist.value.plus(song).distinct()

        val artist = song.moreInfo?.artistMap?.artists?.distinctBy{it.name}?.joinToString(", "){it.name.toString()}

        val mediaItem = MediaItem.Builder()
            .setUri(
                decodeDES(
                    song.moreInfo?.encryptedMediaUrl.toString(),
                    song.moreInfo?.kbps320 ?: false
                )
            )
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title?.perfect())
                    .setArtworkUri(Uri.parse(song.image))
                    .setSubtitle(song.subtitle?.perfect())
                    .setArtist(artist?.perfect())
                    .setAlbumTitle(song.moreInfo?.album?.perfect() ?: song.title?.perfect() )
                    .setDurationMs(song.moreInfo?.duration?.toLong())
                    .build()
            )
            .build()

        exoPlayer.addMediaItem(mediaItem)

        android.util.Log.d("testcase", "playlist size ${_playlist.value!!.size}, ,,  player current index ${exoPlayer.currentMediaItemIndex} ,,,, current Index ${currentIndex.value}")
    }

    @UnstableApi
    private fun startForegroundService() {
        val intent = Intent(application, AudioService::class.java).apply {
            action = ACTIONS.START.toString()
        }
        application.startService(intent)
    }

    private fun decodeDES(input: String, kbps320: Boolean): String {

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


        val pattern = Pattern.compile("\\.mp4.*")
        val matcher = pattern.matcher(decoded)
        decoded = matcher.replaceAll(".mp4")

        decoded = decoded.replace("http:", "https:")
        if(preferencesManager.getStreamQuality() == "320"){
            if(kbps320){
                decoded = decoded.replace("96.mp4", "${preferencesManager.getStreamQuality()}.mp4")

            }
        } else{
            decoded = decoded.replace("96.mp4","${preferencesManager.getStreamQuality()}.mp4")

        }

        return decoded
    }


    fun setNewTrack(song: SongResponse) {
        if(currentPlaylistId.value != "history"){
            _playlist.value = emptyList()
            exoPlayer.clearMediaItems()
            _currentPlaylistId.value = "history"
        }
        if (song.moreInfo?.encryptedMediaUrl.isNullOrEmpty()) {
            makePerfectSong(song) { perfectSong ->
                addSongInPlaylist(perfectSong)
            }
        } else {
            addSongInPlaylist(song)
        }
        if(!showBottomSheet.value){
            showBottomSheet.value = true
        }
    }
    fun makePerfectSong(song: SongResponse, onCallback: (SongResponse) -> Unit) {
        coroutineScope.launch {
            val perfectSong = songDetailsRepository.fetchSongDetails(song.id.toString())
            onCallback(perfectSong)
        }
    }

    @OptIn(UnstableApi::class)
    private fun addSongInPlaylist(song: SongResponse) {
        _playlist.value = playlist.value.orEmpty() + song
        val artist = song.moreInfo?.artistMap?.artists?.distinctBy{it.name}?.joinToString(", "){it.name.toString()}

        val mediaItem = MediaItem.Builder()
            .setUri(decodeDES(
                song.moreInfo?.encryptedMediaUrl.toString(),
                song.moreInfo?.kbps320 ?: false
            ))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title?.perfect())
                    .setArtworkUri(Uri.parse(song.image))
                    .setSubtitle(song.subtitle?.perfect())
                    .setArtist(artist?.perfect())
                    .setAlbumTitle(song.moreInfo?.album?.perfect() ?: song.title?.perfect() )
                    .setDurationMs(song.moreInfo?.duration?.toLong())
                    .build()
            )
            .build()

        exoPlayer.addMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.seekToDefaultPosition(exoPlayer.mediaItemCount - 1)
    }

    @OptIn(UnstableApi::class)
    fun setPlaylist(newPlaylist: List<SongResponse>, playlistId: String) {
        _playlist.value = newPlaylist
        _currentIndex.value = 0
        _currentPlaylistId.value = playlistId

        val mediaItems = newPlaylist.map { song ->
            val artist = song.moreInfo?.artistMap?.artists?.distinctBy{it.name}?.joinToString(", "){it.name.toString()}
            MediaItem.Builder()
                .setUri(decodeDES(
                    song.moreInfo?.encryptedMediaUrl.toString(),
                    song.moreInfo?.kbps320 ?: false
                ))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title?.perfect())
                        .setArtworkUri(Uri.parse(song.image))
                        .setSubtitle(song.subtitle?.perfect())
                        .setArtist(artist?.perfect())
                        .setAlbumTitle(song.moreInfo?.album?.perfect() ?: song.title?.perfect() )
                        .setDurationMs(song.moreInfo?.duration?.toLong())
                        .build()
                )
                .build()
        }
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun changeSong(index: Int) {
        _playlist.value.let { playlist ->
            if (index >= 0 && index < playlist.size) {
                _currentIndex.value = index
                exoPlayer.seekToDefaultPosition(index)
                seekTo(0)
                exoPlayer.play()
            }
        }
    }
    fun skipNext() {
        exoPlayer.seekToNextMediaItem()
    }

    fun skipPrevious() {
        exoPlayer.seekToPreviousMediaItem()
    }

    fun removeTrack(index: Int) {
        val currentList = playlist.value

        if (index in currentList.indices) {
            val updatedList = currentList.toMutableList().apply {
                removeAt(index)
            }
            _playlist.value = updatedList
        }

        val mediaItemCount = exoPlayer.mediaItemCount
        if (index in 0 until mediaItemCount) {
            exoPlayer.removeMediaItem(index)
            _currentIndex.value = exoPlayer.currentMediaItemIndex
        }

    }


    fun replaceIndex(add: Int, remove: Int) {
        val mediaItem = exoPlayer.getMediaItemAt(remove)
        var currentList = _playlist.value
        val track = currentList[remove]

        if (remove in currentList.indices) {
            val updatedList = currentList.toMutableList().apply {
                removeAt(remove)
            }
            currentList = updatedList
        }


        val newList = currentList.toMutableList().apply {
            if (add in 0..size) {
                add(add, track)
            }
        }
        _playlist.value = newList.distinct()

        exoPlayer.removeMediaItem(remove)
        exoPlayer.addMediaItem(add,mediaItem)
        _currentIndex.value = exoPlayer.currentMediaItemIndex


        exoPlayer.prepare()

    }


    private fun updateLyric() {
        val currentPosition = exoPlayer.currentPosition
        _currentLyricIndex.value = getLyricForPosition(currentPosition)
    }

    private fun getLyricForPosition(position: Long): Int {
        val index = lyricsData.value.indexOfLast {
            it.first <= position
        }
        return index
    }


    private fun updateLastSession(){
        job?.cancel()
        val playStartTime = System.currentTimeMillis()
        job = coroutineScope.launch(Dispatchers.IO) {

            currentSong.value?.let {
                lastSessionRepository.insertLastSession(
                    songResponse = it,
                    playCount = 0,
                    skipCount = 1
                )
            }
            delay(20000)
            val elapsedTime = System.currentTimeMillis() - playStartTime
            val playCount = if (elapsedTime >= 20000) 1 else 0
            val skipCount = -1
            android.util.Log.d("LastSessionService", "Elapsed time: $elapsedTime, playCount: $playCount, skipCount: $skipCount")

            currentSong.value?.let {
                lastSessionRepository.insertLastSession(
                    songResponse = it,
                    playCount = playCount,
                    skipCount = skipCount
                )
            }

            if(currentPlaylistId.value == "history"){
                try {
                    getRecommendations(playlist.value[currentIndex.value].id.toString())
                    job?.cancel()
                } catch (e: Exception) {
                    android.util.Log.d("reco", "${e.message}")
                }
            }
        }

    }
}