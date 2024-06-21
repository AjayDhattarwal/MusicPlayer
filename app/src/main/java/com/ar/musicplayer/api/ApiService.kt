package com.ar.musicplayer.api

import com.ar.musicplayer.models.BasicSongInfo
import com.ar.musicplayer.models.HomeData
import com.ar.musicplayer.models.PlaylistResponse
import com.ar.musicplayer.models.SearchResults
import com.ar.musicplayer.models.TopSearchResults
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


    @GET("/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0")
    fun getTopSearch(
        @Query("n") totalSong: String = "15",
        @Query("__call") call: String = "content.getTopSearches"
    ): Call<List<BasicSongInfo>>


    @GET("/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0")
    fun getSearchResults(
        @Query("__call") call: String = "search.getResults",
        @Query("q") query: String,
        @Query("n") totalSong: String = "5",
        @Query("p") page: String = "5",

    ): Call<SearchResults>

    @GET("/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0")
    fun getTopSearchType(
        @Query("cc") cc: String = "in",
        @Query("__call") call: String = "autocomplete.get",
        @Query("query") query: String = "",
        @Query("includeMetaTags") includeMetaTags: String = "1",
    ):Call<TopSearchResults>


}
