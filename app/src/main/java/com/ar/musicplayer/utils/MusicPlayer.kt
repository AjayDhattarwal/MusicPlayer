package com.ar.musicplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.models.RecommendationsResponse
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.utils.events.DetailsEvent
import com.ar.musicplayer.utils.events.RecommendationEvent
import com.ar.musicplayer.utils.helper.loadImageBitmapFromUrl
import com.ar.musicplayer.utils.notification.NotificationManager
import com.ar.musicplayer.viewmodel.DetailsViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.viewmodel.RecommendationViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.inject.Inject

class MusicPlayer @Inject constructor(
    private val context: Context,
    private val viewModel: PlayerViewModel,
    private val exoPlayer: ExoPlayer,
    private val detailsViewModel: DetailsViewModel,
    private val recommendationViewModel: RecommendationViewModel
) {
    private var playlist = MutableStateFlow<List<SongResponse>>(emptyList())
    private var currentIndex = 0
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    @Inject lateinit var notificationManager: NotificationManager

    init {
        coroutineScope.launch {
            viewModel.isPlaying.collect { isPlaying ->
                if (isPlaying) {
                    exoPlayer.play()
                } else {
                    exoPlayer.pause()
                }
                updateNotification()
            }
        }
        coroutineScope.launch {
            viewModel.playlist.collect { newPlaylist ->
                playlist.value = newPlaylist
                currentIndex = if(viewModel.isPlayingHistory.value == true) newPlaylist.size - 1 else 0
                viewModel.setCurrentSongIndex(currentIndex)
                playCurrentSong()
            }
        }


        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
                if(currentIndex > playlist.value.size - 4) {
                    recommendationViewModel.onEvent(RecommendationEvent.GetRecommendations(viewModel.currentSong.value?.title.toString()))
                }
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    skipToNext()
                }
            }
        })
    }

    private fun updateNotification() {
        Log.d("notification","update")

        if (playlist.value.isNotEmpty()) {
            val currentSong = viewModel.currentSong.value
            coroutineScope.launch {
                val bitmap = currentSong?.image?.let {
                    loadImageBitmapFromUrl(it, context)
                } ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                withContext(Dispatchers.Main) {
                    currentSong?.let {
                        viewModel.isFavorite.value?.let { it1 ->
                            notificationManager.showNotification(it,
                                it1,bitmap)
                        }
                    }
                }
            }
        }
    }

    var onSongChanged: ((SongResponse) -> Unit)? = null

    fun decodeDES(input: String): String {
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

    fun play(song: SongResponse) {
        var updatedSongResponse = song
        if(song.moreInfo?.encryptedMediaUrl == "" || song.moreInfo?.encryptedMediaUrl.isNullOrEmpty()){
            Log.d("invoked","${song.id}")
            detailsViewModel.onEvent(DetailsEvent.getSongDetails(song.id.toString(),"song.getDetails",callback = {
                Log.d("invoked","invoked ${it}")
                if(it != null){
                    val decodedUrl = decodeDES(it.moreInfo?.encryptedMediaUrl ?: "")
                    Log.d("url",decodedUrl)
                    val mediaItem = MediaItem.fromUri(decodedUrl)
                    if (!playlist.value.contains(it)) {
                        playlist.value = playlist.value.plus(it)
                    }
                    currentIndex = playlist.value.indexOf(it)
                    viewModel.setCurrentSongIndex(currentIndex)
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    if(viewModel.starter.value == true) exoPlayer.pause() else exoPlayer.play()
                    onSongChanged?.invoke(it)
                }
            }))
        }
        else{
            Log.d("invoked","else invoked")
            val decodedUrl = decodeDES(song.moreInfo?.encryptedMediaUrl ?: "")
            Log.d("url",decodedUrl)
            val mediaItem = MediaItem.fromUri(decodedUrl)
            if (!playlist.value.contains(song)) {
                playlist.value += song
            }
            currentIndex = playlist.value.indexOf(song)
            viewModel.setCurrentSongIndex(currentIndex)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            if(viewModel.starter.value == true) exoPlayer.pause() else exoPlayer.play()
            onSongChanged?.invoke(song)
        }
        if (currentIndex >= playlist.value.size - 3) {
            viewModel.currentSong.value?.let { fetchAndAddRecommendedSongs(it) }
        }
    }
    fun playPlaylist(songs: List<SongResponse>) {
        playlist.value = songs
        currentIndex = if(viewModel.isPlayingHistory.value == true) playlist.value.size - 1 else 0
        viewModel.setCurrentSongIndex(currentIndex)
    }

    private fun playCurrentSong() {
        if (playlist.value.isNotEmpty()) {
            val song = playlist.value[currentIndex]
            val encodedUrl = song.moreInfo?.encryptedMediaUrl ?: " "
            val decodedUrl = decodeDES(encodedUrl)
            exoPlayer.setMediaItem(MediaItem.fromUri(decodedUrl))
            exoPlayer.prepare()
            currentIndex  = if(viewModel.isPlayingHistory.value == true) playlist.value.size - 1 else 0
            viewModel.setCurrentSongIndex(currentIndex)
            onSongChanged?.invoke(song)
        }
    }
    fun skipToNext() {
        if (currentIndex < playlist.value.size - 1) {
            currentIndex++
            viewModel.setCurrentSongIndex(currentIndex)
            val song = playlist.value[currentIndex]
            val decodedUrl = decodeDES(song.moreInfo?.encryptedMediaUrl ?: "")
            exoPlayer.setMediaItem(MediaItem.fromUri(decodedUrl))
            exoPlayer.prepare()
            if(viewModel.starter.value == true) exoPlayer.pause() else exoPlayer.play()
            onSongChanged?.invoke(song)
            viewModel.starter.value = false
        }
    }

    fun skipToPrevious() {
        if (currentIndex > 0) {
            currentIndex--
            viewModel.setCurrentSongIndex(currentIndex)
            val song = playlist.value[currentIndex]
            val decodedUrl = decodeDES(song.moreInfo?.encryptedMediaUrl ?: "")
            exoPlayer.setMediaItem(MediaItem.fromUri(decodedUrl))
            exoPlayer.prepare()
            if(viewModel.starter.value == true) exoPlayer.pause() else exoPlayer.play()
            onSongChanged?.invoke(song)
            viewModel.starter.value = false
        }
    }

    fun getPlayer() = exoPlayer

    fun release() {
        exoPlayer.release()
    }

    fun getPlaylist() = playlist

    fun addListener(notificationManager: NotificationManager) {
        this.notificationManager = notificationManager
    }

    private fun fetchAndAddRecommendedSongs(currentSong: SongResponse) {
        coroutineScope.launch {
            recommendationViewModel.onEvent(RecommendationEvent.GetRecommendations(currentSong.title.toString()))
            recommendationViewModel.recommendedSongs.collect { recommendedSongs ->
                if (recommendedSongs.isNotEmpty()) {
                    Log.d("reco","${recommendedSongs}")
                    recommendedSongs.forEach { songResponse ->
                        if(!playlist.value.contains(songResponse)){
                            playlist.value = playlist.value.plus(songResponse)
                        }
                    }
                }
            }
        }
    }

}
