package com.ar.musicplayer.api

import com.ar.musicplayer.data.models.ArtistResponse
import com.ar.musicplayer.data.models.BasicSongInfo
import com.ar.musicplayer.data.models.HomeData
import com.ar.musicplayer.data.models.PlaylistResponse
import com.ar.musicplayer.data.models.RadioSongs
import com.ar.musicplayer.data.models.SearchResults
import com.ar.musicplayer.data.models.SongDetails
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.StationResponse
import com.ar.musicplayer.data.models.TopSearchResults
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



    @GET("/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0")
    fun getArtistStationId(
        @Query("__call") call: String = "webradio.createArtistStation",
        @Query("name") name: String = "",
        @Query("query") query: String = "",
    ):Call<StationResponse>

    @GET("/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0")
    fun getFeaturedStationId(
        @Query("__call") call: String = "webradio.createFeaturedStation",
        @Query("name") name: String = "",
        @Query("language") language: String = "",
    ):Call<StationResponse>


    @GET("/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0")
    fun getRadioSongs(
        @Query("k") k: String = "20",
        @Query("__call") call: String = "webradio.getSong",
        @Query("next") next: String = "1",
        @Query("stationid") stationid: String = "",
    ): Call <RadioSongs>


    @GET("/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0")
    fun getSongDetails(
        @Query("pids") pids: String = "",
        @Query("__call") call: String = "song.getDetails"
    ):Call<SongDetails>

    @GET("/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0")
    fun getRecoSongs(
        @Query("pid") pid: String = "",
        @Query("__call") call: String = "reco.getreco"
    ):Call<List<SongResponse>>

    @GET("/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0")
    fun getArtistData(
        @Query("token") token: String = "",
        @Query("type") type: String = "artist",
        @Query("n_album") nAlbum: Int = 20,
        @Query("n_song") nSong: Int = 5,
        @Query("__call") call: String = "webapi.get"
    ): Call<ArtistResponse>


}
