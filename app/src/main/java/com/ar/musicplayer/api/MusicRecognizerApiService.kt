package com.ar.musicplayer.api

import com.ar.musicplayer.models.SongRecognitionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MusicRecognizerApiService {


    @Multipart
    @POST("songs/recognize-song") // Assuming this is the correct endpoint
    @Headers(
        "x-rapidapi-key: ba4894b8d6msh953c4cb62591594p19706ajsn9daa2035e36e", // Replace with your actual API key
        "x-rapidapi-host: shazam-api7.p.rapidapi.com",
    )
    fun recognizeSong(
        @Part file: MultipartBody.Part
    ): Call<SongRecognitionResponse>


}