package com.ar.musicplayer.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.traceEventEnd
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
    private val exoPlayer: ExoPlayer,
    private val lastSessionRepository: LastSessionRepository,
    private val songDetailsRepository: SongDetailsRepository
) : AndroidViewModel(application) {

    private val _currentLyricIndex = MutableLiveData<Int>(0)
    val currentLyricIndex: LiveData<Int> = _currentLyricIndex

    private val _lyricsData = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val lyricsData: StateFlow<List<Pair<Int, String>>> = _lyricsData

    private val handler = Handler(Looper.getMainLooper())
    private val updateLyricsRunnable = object : Runnable {
        override fun run() {
            updateLyric()
            // Schedule the next update after 0.5 second
            handler.postDelayed(this, 500L)
        }
    }

    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading = _isLyricsLoading.asStateFlow()

    private var isServiceStarted = false

    val listeningHistory = lastSessionRepository.listeningHistory

    val lastSession = lastSessionRepository.lastSession

    private val preferencesManager = PreferencesManager(application.applicationContext)

    private var job: Job? = null
    private var lastSessionJob: Job? = null

    val showBottomSheet = MutableStateFlow<Boolean>(false)

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

    private val _isPlaying = MutableLiveData(exoPlayer.isPlaying)
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _currentPosition = MutableLiveData(exoPlayer.currentPosition)
    val currentPosition: LiveData<Long> get() = _currentPosition

    private val _duration = MutableLiveData(exoPlayer.duration.takeIf { it > 0 } ?: 0L)
    val duration: LiveData<Long> get() = _duration

    private val _repeatMode = MutableLiveData(exoPlayer.repeatMode)
    val repeatMode: LiveData<Int> get() = _repeatMode

    private val _shuffleModeEnabled = MutableLiveData(exoPlayer.shuffleModeEnabled)
    val shuffleModeEnabled: LiveData<Boolean> get() = _shuffleModeEnabled




    val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val newPosition = exoPlayer.currentPosition
            val newDuration = exoPlayer.duration.takeIf { it > 0 } ?: 0L

            if (_currentPosition.value != newPosition) {
                _currentPosition.postValue(newPosition)
            }
            if (_duration.value != newDuration) {
                _duration.postValue(newDuration)
            }

        }
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val newIndex = exoPlayer.currentMediaItemIndex
            _lyricsData.value = emptyList()
            if (_currentIndex.value != newIndex) {
                _currentIndex.value = newIndex
            }
            if(mediaItem?.mediaMetadata?.title != null){
                val title = exoPlayer.currentMediaItem?.mediaMetadata?.title.toString()
                val artistList = exoPlayer.currentMediaItem?.mediaMetadata?.artist
                    ?.split(",", "&amp;", "with","&quot;")
                    ?.map { it.trim() }
                    ?: listOf()

                val albumName = exoPlayer.currentMediaItem?.mediaMetadata?.albumTitle.toString()

                val duration = exoPlayer.currentMediaItem?.mediaMetadata?.durationMs

                _isLyricsLoading.value = true
                fetchLyrics(
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
                updateLastSession()
            }

        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.postValue(isPlaying)
            if(isPlaying){
                if (!isServiceStarted && (playlist.value?.size ?: 0) > 0) {
                    startForegroundService()
                    isServiceStarted = true
                }
            }
        }

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


    private fun loadLastSession() {
        lastSessionJob = viewModelScope.launch{
            val lastSession = lastSessionRepository.getLastSessionForPlaying()
            if (lastSession.isNotEmpty()) {
                _currentPlaylistId.postValue("history")
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
                getRecommendations(playlist.value!![currentIndex.value!!].id.toString())
            }
        }
    }



    fun playPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }


    fun changeSong(index: Int) {
        _playlist.value?.let { playlist ->
            if (index >= 0 && index < playlist.size) {
                _currentIndex.value = index
                exoPlayer.seekToDefaultPosition(index)
                seekTo(0)
                exoPlayer.play()
            }
        }
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
        _currentPosition.postValue(exoPlayer.currentPosition)
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

    fun toggleFavourite() {
//        updateNotification(preloadedImage.value)
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
        viewModelScope.launch {
            val perfectSong = songDetailsRepository.fetchSongDetails(song.id.toString())
            onCallback(perfectSong)
        }
    }

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



    fun skipNext() {
        val currentIndex = _currentIndex.value ?: return
        val playlistSize = _playlist.value?.size ?: return

        if (currentIndex < playlistSize - 1) {
            changeSong(currentIndex + 1)
        } else {
            changeSong(0)
        }
    }

    fun skipPrevious() {
        val currentIndex = _currentIndex.value ?: return

        if (currentIndex > 0) {
            changeSong(currentIndex - 1)
        }
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


    private fun updateLastSession(){
        job?.cancel()
        val playStartTime = System.currentTimeMillis()
        job = viewModelScope.launch(Dispatchers.IO) {

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
            Log.d("LastSessionService", "Elapsed time: $elapsedTime, playCount: $playCount, skipCount: $skipCount")

            currentSong.value?.let {
                lastSessionRepository.insertLastSession(
                    songResponse = it,
                    playCount = playCount,
                    skipCount = skipCount
                )
            }

            if(currentPlaylistId.value == "history"){
                try {
                    getRecommendations(playlist.value!![currentIndex.value!!].id.toString())
                    job?.cancel()
                } catch (e: Exception) {
                    Log.d("reco", "${e.message}")
                }
            }
        }

    }


    fun getRecommendations(id: String, call: String = "reco.getreco") {
        val client = ApiConfig.getApiService().getRecoSongs(
            pid = id,
            call = call
        )

        client.enqueue(object : Callback<List<SongResponse>> {
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
                Log.d("reco", "${t.message}")
            }
        })
    }

    private fun addRecoSongInPlaylist(song: SongResponse) {

        if(_playlist.value?.size!! > 100){
            _playlist.value = _playlist.value!!.takeLast(100)
            val lastIndex = exoPlayer.mediaItemCount - _playlist.value!!.size
            exoPlayer.removeMediaItems(0, lastIndex)
            exoPlayer.prepare()
            _currentIndex.value = exoPlayer.currentMediaItemIndex
        }

        _playlist.value  = playlist.value?.plus(song)?.distinct()

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

        Log.d("testcase", "playlist size ${_playlist.value!!.size}, ,,  player current index ${exoPlayer.currentMediaItemIndex} ,,,, current Index ${currentIndex.value}")
    }

    private fun startForegroundService() {
        val context = getApplication<Application>().applicationContext
        val intent = Intent(context, AudioService::class.java).apply {
            action = ACTIONS.START.toString()
        }
        context.startService(intent)
    }


    fun fetchLyrics(
        artistList: List<String>,
        trackName: String,
        albumName: String,
        duration: Int,
        onSuccess: (List<Pair<Int, String>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        var count = 0

        fun tryFetch() {
            if (count >= artistList.size) {
                onError(Exception("No matching lyrics found for any artist."))
                return
            }

            lyricsFinder(
                artistList = artistList,
                artistName = artistList[count],
                trackName = trackName,
                albumName = albumName,
                duration = duration,
                onSuccess = onSuccess,
                onError = onError,
                onTryAgain = {
                    count++
                    tryFetch()
                }
            )
        }

        tryFetch()
    }


    fun lyricsFinder(
        artistList: List<String>,
        artistName: String,
        trackName: String,
        albumName: String,
        duration: Int,
        onSuccess: (List<Pair<Int, String>>) -> Unit,
        onError: (Exception) -> Unit,
        onTryAgain: () -> Unit
    ){
        val client = ApiConfig.getLyricsByLrclib().getLyricsLrclib(
            trackName = trackName,
            artistName = artistName
        )

        Log.d("fetchLyrics","track name $trackName, album name $albumName, duration $duration, artistList $artistList")

        client.enqueue(object : Callback<List<LyricsResponse>> {
            override fun onResponse(
                call: Call<List<LyricsResponse>> ,
                response: Response<List<LyricsResponse>>
            ) {
                if(response.isSuccessful){
                    val results = response.body()?.filter { it.syncedLyrics != "null" }

                    results?.forEach { result ->
                        Log.d("fetchLyrics","response form api track name ${result.trackName}, album name ${result.albumName}, duration ${ result.duration }, artistList ${result.artistName}")
                    }
                    val matchedResult = results?.minByOrNull { result ->
                        var score = 0
                        if (!artistList.contains(result.artistName))  score++
                        if (result.albumName != albumName)  score++
                        if (result.duration?.toInt() == duration) {
                            score += (duration - result.duration.toInt()).absoluteValue
                        }
                        score
                    }
                    if (matchedResult != null) {
                        fetchTranslation(
                            matchedResult.syncedLyrics.toString(),
                            onSuccess = onSuccess,
                            onError = onError
                        )
                    } else {
                        onTryAgain()
                    }
                }
            }
            override fun onFailure(call: Call<List<LyricsResponse>> , t: Throwable) {
                onError((t.cause ?: Exception("Unknown error")) as Exception)
            }
        })
    }

    fun fetchTranslation(
        text: String,
        onSuccess: (List<Pair<Int, String>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val client = ApiConfig.getTranslation().getTranslatedLyrics(
            text = text
        )

        client.enqueue(object : Callback<TranslationResponse> {
            override fun onResponse(
                call: Call<TranslationResponse>,
                response: Response<TranslationResponse>
            ) {
                if (response.isSuccessful) {
                    onSuccess(
                        parseLrc(
                            response.body()?.text.toString()
                        )
                    )
                } else{
                    onSuccess(
                        parseLrc(
                            text
                        )
                    )
                }

            }

            override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {
                onSuccess(
                    parseLrc(
                        text
                    )
                )
                onError((t.cause ?: Exception("Unknown error")) as Exception)
            }
        })

    }

    fun parseLrc(syncedLyrics: String): List<Pair<Int, String>> {
        val lrcLines = mutableListOf<Pair<Int, String>>()
        val pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2})](.*)")

        syncedLyrics.lines().forEach { line ->
            val matcher = pattern.matcher(line)
            if (matcher.matches()) {
                val minutes = matcher.group(1).toInt()
                val seconds = matcher.group(2).toInt()
                val centiseconds = matcher.group(3).toInt()
                val text = matcher.group(4)
                val timeInMs = (minutes * 60 + seconds) * 1000 + centiseconds * 10
                lrcLines.add(Pair(timeInMs, text))
            }
        }
        return lrcLines
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


    override fun onCleared() {
        val context = getApplication<Application>().applicationContext
        super.onCleared()
        exoPlayer.release()
        exoPlayer.removeListener(playerListener)

        Intent(context, AudioService::class.java).also {
            it.action = ACTIONS.STOP.toString()
            context.stopService(it)
        }
    }


}
