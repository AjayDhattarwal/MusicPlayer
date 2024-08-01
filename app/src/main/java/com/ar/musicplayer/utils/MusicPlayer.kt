//package com.ar.musicplayer.utils
//
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.media3.common.MediaItem
//import androidx.media3.common.Player
//import androidx.media3.exoplayer.ExoPlayer
//import com.ar.musicplayer.models.SongResponse
//import com.ar.musicplayer.utils.events.DetailsEvent
//import com.ar.musicplayer.utils.events.RecommendationEvent
//import com.ar.musicplayer.utils.notification.MusicPlayerService
//import com.ar.musicplayer.screens.player.DetailsViewModel
//import com.ar.musicplayer.viewmodel.PlayerViewModel
//import com.ar.musicplayer.screens.player.RecommendationViewModel
//import kotlinx.coroutines.*
//import kotlinx.coroutines.flow.MutableStateFlow
//import java.nio.charset.StandardCharsets
//import java.util.Base64
//import java.util.regex.Pattern
//import javax.crypto.Cipher
//import javax.crypto.SecretKeyFactory
//import javax.crypto.spec.DESKeySpec
//import javax.inject.Inject
//
//
//class MusicPlayer @Inject constructor(
//    private val context: Context,
//    private val viewModel: PlayerViewModel,
//    private val exoPlayer: ExoPlayer,
//    private val detailsViewModel: DetailsViewModel,
//    private val recommendationViewModel: RecommendationViewModel
//) {
//    private var playlist = MutableStateFlow<List<SongResponse>>(emptyList())
//    private var currentIndex = 0
//    private val coroutineScope = CoroutineScope(Dispatchers.Main)
//
//    init {
//        coroutineScope.launch {
//            viewModel.isPlaying.collect { isPlaying ->
//                if (isPlaying) {
//                    val currentSong = viewModel.currentSong.value
//                    currentSong?.let {
//                        MusicPlayerService.startService(context)
//                    }
//                }
//            }
//        }
//        coroutineScope.launch {
//            viewModel.playlist.collect { newPlaylist ->
//                playlist.value = newPlaylist
//                currentIndex = if(viewModel.isPlayingHistory.value == true) newPlaylist.size - 1 else 0
//                viewModel.setCurrentSongIndex(currentIndex)
//                playCurrentSong()
//            }
//        }
//
//
//        exoPlayer.addListener(object : Player.Listener {
//            override fun onIsPlayingChanged(isPlaying: Boolean) {
////                updateNotification()
//                if(currentIndex > playlist.value.size - 4) {
//                    recommendationViewModel.onEvent(RecommendationEvent.GetRecommendations(viewModel.currentSong.value?.title.toString()))
//                }
//            }
//            override fun onPlaybackStateChanged(state: Int) {
//                if (state == Player.STATE_ENDED) {
//                    skipToNext()
//                }
//            }
//        })
//    }
//
//
//    var onSongChanged: ((SongResponse) -> Unit)? = null
//
//    fun decodeDES(input: String): String {
//        Log.d("input","$input")
//        val key = "38346591"
//        val algorithm = "DES/ECB/PKCS5Padding"
//
//        val keyFactory = SecretKeyFactory.getInstance("DES")
//        val desKeySpec = DESKeySpec(key.toByteArray(StandardCharsets.UTF_8))
//        val secretKey = keyFactory.generateSecret(desKeySpec)
//
//        val cipher = Cipher.getInstance(algorithm)
//        cipher.init(Cipher.DECRYPT_MODE, secretKey)
//
//        val encryptedBytes = Base64.getDecoder().decode(input.replace("\\",""))
//        val decryptedBytes = cipher.doFinal(encryptedBytes)
//        var decoded = String(decryptedBytes, StandardCharsets.UTF_8)
//
//        // Replace ".mp4" pattern
//        val pattern = Pattern.compile("\\.mp4.*")
//        val matcher = pattern.matcher(decoded)
//        decoded = matcher.replaceAll(".mp4")
//
//        // Replace "http:" with "https:"
//        decoded = decoded.replace("http:", "https:")
//
//        return decoded
//    }
//
//    fun play(song: SongResponse) {
//        if(song.uri != ""){
//            val mediaItem = MediaItem.fromUri(song.uri.toString())
//            if (!playlist.value.contains(song)) {
//                playlist.value += song
//            }
//            currentIndex = playlist.value.indexOf(song)
//            viewModel.setCurrentSongIndex(currentIndex)
//            exoPlayer.setMediaItem(mediaItem)
//            exoPlayer.prepare()
//            if(viewModel.starter.value == true) exoPlayer.pause() else exoPlayer.play()
//            onSongChanged?.invoke(song)
//        }
//        else{
//            if(song.moreInfo?.encryptedMediaUrl == "" || song.moreInfo?.encryptedMediaUrl.isNullOrEmpty()){
//                Log.d("invoked","${song.id}")
//                detailsViewModel.onEvent(DetailsEvent.getSongDetails(song.id.toString(),"song.getDetails",callback = {
//                    if(it != null){
//                        val decodedUrl = decodeDES(it.moreInfo?.encryptedMediaUrl ?: "")
//                        Log.d("url",decodedUrl)
//                        val mediaItem = MediaItem.fromUri(decodedUrl)
//                        if (!playlist.value.contains(it)) {
//                            playlist.value = playlist.value.plus(it)
//                        }
//                        currentIndex = playlist.value.indexOf(it)
//                        viewModel.setCurrentSongIndex(currentIndex)
//                        exoPlayer.setMediaItem(mediaItem)
//                        exoPlayer.prepare()
//                        if(viewModel.starter.value == true) exoPlayer.pause() else exoPlayer.play()
//                        onSongChanged?.invoke(it)
//                    }
//                }))
//            }
//            else{
//                Log.d("invoked","else invoked")
//                val decodedUrl = decodeDES(song.moreInfo?.encryptedMediaUrl ?: "")
//                Log.d("url",decodedUrl)
//                val mediaItem = MediaItem.fromUri(decodedUrl)
//                if (!playlist.value.contains(song)) {
//                    playlist.value += song
//                }
//                currentIndex = playlist.value.indexOf(song)
//                viewModel.setCurrentSongIndex(currentIndex)
//                exoPlayer.setMediaItem(mediaItem)
//                exoPlayer.prepare()
//                if(viewModel.starter.value == true) exoPlayer.pause() else exoPlayer.play()
//                onSongChanged?.invoke(song)
//            }
//            if (currentIndex >= playlist.value.size - 3) {
//                viewModel.currentSong.value?.let { fetchAndAddRecommendedSongs(it) }
//            }
//        }
//
//    }
//
//
//
//
//
//
//
//
//
//
//
//    private fun fetchAndAddRecommendedSongs(currentSong: SongResponse) {
//        coroutineScope.launch {
//            recommendationViewModel.onEvent(RecommendationEvent.GetRecommendations(currentSong.title.toString()))
//            recommendationViewModel.recommendedSongs.collect { recommendedSongs ->
//                if (recommendedSongs.isNotEmpty()) {
//                    Log.d("reco","${recommendedSongs}")
//                    recommendedSongs.forEach { songResponse ->
//                        if(!playlist.value.contains(songResponse)){
//                            playlist.value = playlist.value.plus(songResponse)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//}
