package com.ar.musicplayer.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.api.ApiConfig
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.getArtistsNameList
import com.ar.musicplayer.data.models.getArtistsString
import com.ar.musicplayer.data.models.sanitizeString
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.notification.ACTIONS
import com.ar.musicplayer.utils.notification.AudioService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(
    private val application: Context,
    private val exoPlayer: ExoPlayer,
    private val lyricRepository: LyricRepository,
    private val lastSessionRepository: LastSessionRepository,
    private val songDetailsRepository: SongDetailsRepository,
) {
    private val MAX_PLAYLIST_SIZE = 100

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

    private val _isBuffering = MutableStateFlow(exoPlayer.isLoading)
    val isBuffering: StateFlow<Boolean> = _isBuffering

    private val mutablePlaylist = MutableStateFlow<List<SongResponse>>(emptyList())
    val playlist: StateFlow<List<SongResponse>> get() = mutablePlaylist

    val currentSong: StateFlow<SongResponse?> = combine(currentIndex, playlist) { index, songs ->
        songs.getOrNull(index)
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    private val _currentPlaylistId = MutableStateFlow("history")
    val currentPlaylistId: StateFlow<String> get() = _currentPlaylistId


    private val _repeatMode = MutableLiveData(exoPlayer.repeatMode)
    val repeatMode: LiveData<Int> get() = _repeatMode

    private val _shuffleModeEnabled = MutableLiveData(exoPlayer.shuffleModeEnabled)
    val shuffleModeEnabled: LiveData<Boolean> get() = _shuffleModeEnabled


    private val _lyricsData = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val lyricsData: StateFlow<List<Pair<Int, String>>> = _lyricsData

    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading: StateFlow<Boolean> = _isLyricsLoading

    private val _currentLyricIndex = MutableStateFlow(0)
    val currentLyricIndex: StateFlow<Int> = _currentLyricIndex


    private val handler = Handler(Looper.getMainLooper())

    private val updateLyricsRunnable = object : Runnable {
        override fun run() {
            updateLyric()
            // Schedule the next update after 0.5 second
            handler.postDelayed(this, 500L)
        }

    }

    private var isServiceStarted = false

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet


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
            updateNotification()
            if (mediaItem?.mediaMetadata?.title != null) {
                val title = exoPlayer.currentMediaItem?.mediaMetadata?.title.toString()
                val artistList = exoPlayer.getArtistsNameList()

                val albumName = exoPlayer.currentMediaItem?.mediaMetadata?.albumTitle.toString()
                val duration = exoPlayer.currentMediaItem?.mediaMetadata?.durationMs

                _isLyricsLoading.value = true

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
                        _lyricsData.value = emptyList()
                        _isLyricsLoading.value = false
                    }
                )
                updateLastSession()
            }
            if(!showBottomSheet.value){
                _showBottomSheet.value = true
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) {
                if (!isServiceStarted && (playlist.value.isNotEmpty()) ) {
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

        override fun onIsLoadingChanged(isLoading: Boolean) {
            _isBuffering.value = isLoading
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Timber.tag("playerError").d("ExoPlayer error: ${error.message}")
        }

    }

    init {
        exoPlayer.addListener(playerListener)
        coroutineScope.launch {
            loadLastSession()
        }
        coroutineScope.launch {
            while (true) {
                if(exoPlayer.isPlaying){
                    _currentPosition.value = exoPlayer.currentPosition
                }
                delay(1000L)
            }
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


    @OptIn(UnstableApi::class)
    private fun createMediaItem(song: SongResponse): MediaItem {
        val artist = song.getArtistsString()
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(song.title?.sanitizeString())
            .setArtworkUri(Uri.parse(song.image))
            .setSubtitle(song.subtitle?.sanitizeString())
            .setArtist(artist)
            .setAlbumTitle(song.moreInfo?.album?.sanitizeString() ?: song.title?.sanitizeString() )
            .setDurationMs(song.moreInfo?.duration?.toLong())
            .build()

        return MediaItem.Builder()
            .setUri(decodeDES(song.moreInfo?.encryptedMediaUrl.toString(), song.moreInfo?.kbps320 ?: false))
            .setMediaMetadata(mediaMetadata)
            .build()
    }


    @OptIn(UnstableApi::class)
    private suspend fun loadLastSession() {
        val lastSession = lastSessionRepository.getLastSessionForPlaying()
        if (lastSession.isNotEmpty()) {
            _currentPlaylistId.value = "history"
            mutablePlaylist.value = lastSession.reversed().map { (_, songResponse) -> songResponse }
            val mediaItems = lastSession.reversed().map { (_, song) ->
                createMediaItem(song)
            }
            exoPlayer.setMediaItems(mediaItems, mediaItems.size - 1, 0)
            exoPlayer.prepare()
            _showBottomSheet.value = true
            exoPlayer.pause()
            delay(2000)
            getRecommendations(playlist.value[currentIndex.value].id.toString())
        }
    }

    fun getRecommendations(id: String) {
        val client = ApiConfig.getApiService().getRecoSongs(
            pid = id
        )

        client.enqueue(object : Callback<List<SongResponse>> {
            @OptIn(UnstableApi::class)
            override fun onResponse(
                call: Call<List<SongResponse>>,
                response: Response<List<SongResponse>>
            ) {
                if (response.isSuccessful) {

                    response.body()?.forEach { song ->
                        if (!playlist.value.any { it.id == song.id }) {
                            addRecoSongInPlaylist(song)
                        }
                    }

                }
            }

            override fun onFailure(call: Call<List<SongResponse>>, t: Throwable) {
                Log.d("reco", "${t.message}")
            }
        })
    }

    @UnstableApi
    private fun addRecoSongInPlaylist(song: SongResponse) {

        if(mutablePlaylist.value.size > MAX_PLAYLIST_SIZE){

            mutablePlaylist.value =
                mutablePlaylist.value
                    .subList(
                        mutablePlaylist.value.size - MAX_PLAYLIST_SIZE,
                        mutablePlaylist.value.size
                    ).toMutableList()

            val itemsToRemove = exoPlayer.mediaItemCount - mutablePlaylist.value.size
            if (itemsToRemove > 0) {
                exoPlayer.removeMediaItems(0, itemsToRemove)
                exoPlayer.prepare()
            }
        }

        mutablePlaylist.value  = playlist.value.plus(song).distinct()


        val mediaItem = createMediaItem(song)

        exoPlayer.addMediaItem(mediaItem)
        _currentIndex.value = exoPlayer.currentMediaItemIndex

        Timber.d(
            "Added song to playlist: ${song.title}," +
                    " Playlist size: ${mutablePlaylist.value.size}," +
                    " Player index: ${exoPlayer.currentMediaItemIndex}"
        )

    }

    fun setNewTrack(song: SongResponse) {
        if(currentPlaylistId.value != "history"){
            mutablePlaylist.value = emptyList()
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
    }

    fun makePerfectSong(song: SongResponse, onCallback: (SongResponse) -> Unit) {
        coroutineScope.launch {
            val perfectSong = songDetailsRepository.fetchSongDetails(song.id.toString())
            onCallback(perfectSong)
        }
    }

    @OptIn(UnstableApi::class)
    private fun addSongInPlaylist(song: SongResponse) {

        if (song in playlist.value) {
            val index = playlist.value.indexOf(song)
            removeTrack(index)
        }
        Timber.d("mediaItems count:   ${exoPlayer.mediaItemCount}   ${exoPlayer.currentMediaItemIndex}  ")
        if (mutablePlaylist.value.isNotEmpty()) {
            mutablePlaylist.value = mutablePlaylist.value
                .subList(0, exoPlayer.currentMediaItemIndex + 1)

            exoPlayer.removeMediaItems(exoPlayer.currentMediaItemIndex + 1, exoPlayer.mediaItemCount)
        }

        Timber.d("mediaItems count after remove:   ${exoPlayer.mediaItemCount}   ${exoPlayer.currentMediaItemIndex}  ")

        exoPlayer.prepare()
        updatePlaylist { currentPlaylist -> currentPlaylist + song }
        addSongToPlayer(song)

        exoPlayer.seekToDefaultPosition(exoPlayer.mediaItemCount - 1)

    }

    private fun updatePlaylist(update: (List<SongResponse>) -> List<SongResponse>) {
        mutablePlaylist.value = update(mutablePlaylist.value)
    }

    private fun addSongToPlayer(song: SongResponse) {
        val mediaItem = createMediaItem(song)
        exoPlayer.addMediaItem(mediaItem)
        if (exoPlayer.playbackState == Player.STATE_IDLE) {
            exoPlayer.prepare()
        }
        exoPlayer.play()
    }

    @OptIn(UnstableApi::class)
    fun setPlaylist(newPlaylist: List<SongResponse>, playlistId: String) {
        mutablePlaylist.value = newPlaylist
        _currentIndex.value = 0
        _currentPlaylistId.value = playlistId

        val mediaItems = newPlaylist.mapNotNull { song ->
            song.moreInfo?.let { moreInfo ->
                createMediaItem(song)
            }
        }

        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun changeSong(index: Int) {
        playlist.value.let { playlist ->
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
            mutablePlaylist.value = currentList.toMutableList().also { it.removeAt(index) }
        }

        val mediaItemCount = exoPlayer.mediaItemCount
        if (index in 0 until mediaItemCount) {
            exoPlayer.removeMediaItem(index)

            if (index <= exoPlayer.currentMediaItemIndex) {
                exoPlayer.prepare()
            }

            if (index < _currentIndex.value) {
                _currentIndex.value = exoPlayer.currentMediaItemIndex
            }
        }

    }


    fun replaceIndex(add: Int, remove: Int) {
        val mediaItem = exoPlayer.getMediaItemAt(remove)
        var currentList = mutablePlaylist.value
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
        mutablePlaylist.value = newList.distinct()

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
            Timber.d( "Elapsed time: $elapsedTime, playCount: $playCount, skipCount: $skipCount")

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



    @UnstableApi
    private fun startForegroundService() {
        val intent = Intent(application, AudioService::class.java).apply {
            action = ACTIONS.START.toString()
        }
        application.startService(intent)
    }

    @UnstableApi
    private fun updateNotification(){
        val intent = Intent(application, AudioService::class.java).apply {
            action = ACTIONS.UPDATE.toString()
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

    fun destroy(){
        exoPlayer.removeListener(playerListener)
    }
}