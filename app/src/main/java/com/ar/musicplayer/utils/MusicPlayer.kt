package com.ar.musicplayer.utils

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.utils.notification.NotificationManager
import com.ar.musicplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private val exoPlayer: ExoPlayer
) {
    private var playlist = listOf<SongResponse>()
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
                playlist = newPlaylist
                currentIndex = if(viewModel.isPlayingHistory.value == true) newPlaylist.size - 1 else 0
                playCurrentSong()
            }
        }
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
            }
        })


    }

    private fun updateNotification() {
        if (playlist.isNotEmpty()) {
            val currentSong = viewModel.currentSong.value
            val bitmap = viewModel.bitmapImg.value
            coroutineScope.launch {
                Log.d("bitmap",bitmap?.height.toString())
                bitmap.let {
                    currentSong?.let { it1 -> notificationManager.showNotification(it1, exoPlayer.isPlaying, it) }
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
        val decodedUrl = decodeDES(song.moreInfo?.encryptedMediaUrl ?: "")
        Log.d("url",decodedUrl)
        val mediaItem = MediaItem.fromUri(decodedUrl)
        if (!playlist.contains(song)) {
            playlist = playlist + song
        }
        currentIndex = playlist.indexOf(song)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        if(viewModel.starter.value == true) exoPlayer.pause() else exoPlayer.play()
        onSongChanged?.invoke(song)


    }
    fun playPlaylist(songs: List<SongResponse>) {
        playlist = songs
        currentIndex = if(viewModel.isPlayingHistory.value == true) playlist.size - 1 else 0
        playCurrentSong()
    }

    private fun playCurrentSong() {
        if (playlist.isNotEmpty()) {
            val song = playlist[currentIndex]
            val encodedUrl = song.moreInfo?.encryptedMediaUrl ?: " "
            val decodedUrl = decodeDES(encodedUrl)
            exoPlayer.setMediaItem(MediaItem.fromUri(decodedUrl))
            exoPlayer.prepare()
            currentIndex  = if(viewModel.isPlayingHistory.value == true) playlist.size - 1 else 0
            onSongChanged?.invoke(song)
        }
    }
    fun skipToNext() {
        if (currentIndex < playlist.size - 1) {
            currentIndex++
            val song = playlist[currentIndex]
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
            val song = playlist[currentIndex]
            val decodedUrl = decodeDES(song.moreInfo?.encryptedMediaUrl ?: "")
            exoPlayer.setMediaItem(MediaItem.fromUri(decodedUrl))
            exoPlayer.prepare()
            if(viewModel.starter.value == true) exoPlayer.pause() else exoPlayer.play()
            onSongChanged?.invoke(song)
            viewModel.starter.value = false
        }
    }
    fun getViewModel() = viewModel
    fun getPlayer() = exoPlayer

    fun release() {
        exoPlayer.release()
    }

    fun addListener(notificationManager: NotificationManager) {
        this.notificationManager = notificationManager
    }


}

