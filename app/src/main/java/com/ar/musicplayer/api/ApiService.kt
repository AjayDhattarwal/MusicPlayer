package com.ar.musicplayer.api

import com.ar.musicplayer.models.HomeData
import com.ar.musicplayer.models.PlaylistResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface ApiService {

    @GET("/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0&__call=webapi.getLaunchData")
    suspend fun getHomeData(): Response<HomeData>



    @GET("/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0")
    fun getApiData(
        @Query("token") token: String = "",
        @Query("type") type: String = "",
        @Query("n") totalSong: String = "5",
        @Query("q") q: String = "1",
        @Query("__call") call: String = "webapi.get"
    ): Call<PlaylistResponse>


}



//    @GET("/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0&__call=content.getTopSearches")
//    suspend fun getTopSearches(@QueryMap params: Map<String, String>): TopSearches
//
//    @GET("/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0&__call=webapi.get")
//    suspend fun getFromToken(@QueryMap params: Map<String, String>): FromToken