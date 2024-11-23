package com.ar.musicplayer.screens.testing

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.data.repository.MusicRecognizerRepository
import com.ar.musicplayer.api.ApiConfig
import com.ar.musicplayer.data.models.RelatedTracks
import com.ar.musicplayer.data.models.SongRecognitionResponse
import com.ar.musicplayer.data.models.TrackRecognition
import com.ar.musicplayer.data.models.TrackResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.awaitResponse
import java.io.File

class MusicRecognizerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRecognizerRepository()

    private val _track = MutableStateFlow<Pair<TrackRecognition?, RelatedTracks?>?>(null)
    val trackResponse =  _track.asStateFlow()


    fun clearResult(){
        _track.value = null
    }

    fun recognizeSong(filePath: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val file = File(filePath)
                val track = repository.recognizeSong(file, 0)?.track
                _track.value = Pair(track, null)

                track?.relatedTracksUrl?.let {
                     val relatedTracks = repository.fetchRelatedSongs(it)
                    _track.value = Pair(track, relatedTracks)
                }

            }
        }
    }


}
