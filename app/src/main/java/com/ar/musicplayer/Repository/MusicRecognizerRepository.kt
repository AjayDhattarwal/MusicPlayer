package com.ar.musicplayer.Repository

import com.ar.musicplayer.api.MusicRecognizerApiService
import com.ar.musicplayer.models.SongRecognitionResponse
import okhttp3.MultipartBody
import retrofit2.Call

class MusicRecognizerRepository (private val apiService: MusicRecognizerApiService) {

    fun recognizeSong(file: MultipartBody.Part): Call<SongRecognitionResponse> {
        return apiService.recognizeSong(file)
    }


}