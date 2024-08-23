package com.ar.musicplayer.api

import com.ar.musicplayer.data.models.LyricsResponse
import com.ar.musicplayer.data.models.TranslationResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface LyricsByLrclib {

    @GET("api/search")
    fun getLyricsLrclib(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String
    ): Call<List<LyricsResponse>>

}

interface Translate{

    @GET("transliterate/")
    fun getTranslatedLyrics(
        @Query("text") text: String
    ): Call<TranslationResponse>

}
