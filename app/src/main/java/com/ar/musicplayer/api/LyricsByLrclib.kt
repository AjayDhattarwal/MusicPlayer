package com.ar.musicplayer.api

import com.ar.musicplayer.data.models.LyricsResponse
import com.ar.musicplayer.data.models.TranslationResponse
import com.ar.musicplayer.data.repository.TransliterateRequest
import com.google.gson.JsonObject
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface LyricsByLrclib {

    @GET("api/search")
    fun getLyricsLrclib(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String
    ): Call<List<LyricsResponse>>

}



interface Translate{

    @Headers("Content-Type: application/json", "accept: application/json")
    @POST("transliterate/")
    fun postTranslatedLyrics(
        @Body request: TransliterateRequest
    ): Call<TranslationResponse>

}

interface ImportSpotifyPlaylist{

    @Headers(
        "app-platform: WebPlayer",
        "Content-Type: application/json"
    )
    @GET("/pathfinder/v1/query")
    suspend fun fetchPlaylistContents(
        @Header("Authorization") authorization: String,
        @Query("operationName") operationName: String = "fetchPlaylistContents",
        @Query("variables") variables: String,
        @Query("extensions") extensions: String
    ): JsonObject
}


interface YouTubeApi{
    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; rv:96.0) Gecko/20100101 Firefox/96.0")
    @GET("{path}")
    suspend fun getYouTubeData(
        @Path("path") path: String
    ): ResponseBody


    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; rv:96.0) Gecko/20100101 Firefox/96.0")
    @GET("playlist")
    suspend fun getPlaylistData(
        @Query("list") id: String
    ): ResponseBody

}