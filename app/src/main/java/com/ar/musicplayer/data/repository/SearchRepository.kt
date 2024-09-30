package com.ar.musicplayer.data.repository

import android.util.Log
import com.ar.musicplayer.api.ApiConfig
import com.ar.musicplayer.data.models.Album
import com.ar.musicplayer.data.models.Artist
import com.ar.musicplayer.data.models.BasicSongInfo
import com.ar.musicplayer.data.models.ImportPlaylistResponse
import com.ar.musicplayer.data.models.PlaylistResponse
import com.ar.musicplayer.data.models.SearchResults
import com.ar.musicplayer.data.models.Song
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.TopSearchResults
import com.ar.musicplayer.data.models.toAlbumResponse
import com.ar.musicplayer.data.models.toArtist
import com.ar.musicplayer.data.models.toPlaylistResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class SearchRepository @Inject constructor(

) {

    val _isSearching = MutableStateFlow(false)

    private val _trendingSearchResults = MutableStateFlow<List<BasicSongInfo>>(emptyList())
    val trendingSearchResults = _trendingSearchResults.asStateFlow()

    private val _searchSongResults = MutableStateFlow<List<SongResponse>>(emptyList())
    val searchSongResults = _searchSongResults.asStateFlow()

    private val _importPlaylist = MutableStateFlow<List<SongResponse>>(emptyList())
    val importPlaylist = _importPlaylist.asStateFlow()

    private val _searchAlbumsResults = MutableStateFlow<List<Album>>(emptyList())
    val searchAlbumsResults = _searchAlbumsResults.asStateFlow()

    private val _searchArtistResults = MutableStateFlow<List<Artist>>(emptyList())
    val searchArtistResults = _searchArtistResults.asStateFlow()

    private val _searchPlaylistResults = MutableStateFlow<List<PlaylistResponse>>(emptyList())
    val searchPlaylistResults = _searchPlaylistResults.asStateFlow()

    private val _topSearchResults = MutableStateFlow<TopSearchResults?>(null)
    val topSearchResults = _topSearchResults.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    var errorMessage: String = ""
        private set

    fun getSpecificSearchResult(call: String, query: String, page: String, totalResults: String) {
        _isError.update { false }
        val client = ApiConfig.getApiService().getSearchResults(
            call = call,
            query = query,
            page = page,
            totalSong = totalResults
        )

        client.enqueue(object : Callback<SearchResults> {
            override fun onResponse(retrofitCall: Call<SearchResults>, response: Response<SearchResults>) {
                val responseBody = response.body()
                if (!response.isSuccessful || responseBody == null) {
                    onError("Data Processing Error")
                    return
                }

                _isSearching.update { false }
                when (call) {
                    "search.getResults" -> _searchSongResults.value = responseBody.results ?: emptyList()
                    "search.getAlbumResults" -> _searchAlbumsResults.value = responseBody.results?.map { it.toAlbumResponse() } ?: emptyList()
                    "search.getArtistResults" -> _searchArtistResults.value = responseBody.results?.map { it.toArtist() } ?: emptyList()
                    "search.getPlaylistResults" -> _searchPlaylistResults.value = responseBody.results?.map { it.toPlaylistResponse() } ?: emptyList()
                    else -> Log.d("search", "search condition error")
                }
            }

            override fun onFailure(retrofitCall: Call<SearchResults>, t: Throwable) {
                onError(t.message)
                t.printStackTrace()
            }
        })
    }

    fun getTrendingResult(call: String) {
        _isError.value = false

        val client = ApiConfig.getApiService().getTopSearch(
            call = call
        )

        client.enqueue(object : Callback<List<BasicSongInfo>> {

            override fun onResponse(
                call: Call<List<BasicSongInfo>>,
                response: Response<List<BasicSongInfo>>
            ) {
                val responseBody = response.body()

                if (!response.isSuccessful || responseBody == null) {
                    onError("Data Processing Error")
                    return
                }


                _isSearching.update { false }
                _trendingSearchResults.value = responseBody
            }

            override fun onFailure(call: Call<List<BasicSongInfo>>, t: Throwable) {
                onError(t.message)
                t.printStackTrace()
            }

        })
    }


    fun getTopDataResult(  call: String, query: String,cc: String, includeMetaTags: String ) {
        _isError.value = false

        val client = ApiConfig.getApiService().getTopSearchType(
            call = call,
            query = query,
            cc = cc,
            includeMetaTags = includeMetaTags
        )

        // Send API request using Retrofit
        client.enqueue(object : Callback<TopSearchResults> {

            override fun onResponse(
                retrofitCall: Call<TopSearchResults>,
                response: Response<TopSearchResults>
            ) {
                val responseBody = response.body()
                if (!response.isSuccessful || responseBody == null) {
                    onError("Data Processing Error")
                    return
                }

                _isSearching.update { false }

                _topSearchResults.value = responseBody


            }

            override fun onFailure(call: Call<TopSearchResults>, t: Throwable) {
                onError(t.message)
                t.printStackTrace()
            }

        })
    }

    fun getSearchResultForAi(
        call: String,
        query: String,
        page: String,
        totalResults: String,
        onResult: (SearchResults) -> Unit,
        onError: (String) -> Unit
    ) {
        val client = ApiConfig.getApiService().getSearchResults(
            call = call,
            query = query,
            page = page,
            totalSong = totalResults
        )

        client.enqueue(object : Callback<SearchResults> {
            override fun onResponse(retrofitCall: Call<SearchResults>, response: Response<SearchResults>) {
                val responseBody = response.body()
                if (!response.isSuccessful || responseBody == null) {
                    onError("Data Processing Error")
                    return
                }
                onResult(responseBody)
            }

            override fun onFailure(retrofitCall: Call<SearchResults>, t: Throwable) {
                onError(t.message)
                t.printStackTrace()
            }
        })
    }

    private fun onError(inputMessage: String?) {
        val message = inputMessage?.takeIf { it.isNotBlank() } ?: "Unknown Error"
        errorMessage = "ERROR: $message. Some data may not be displayed properly."
        _isError.update { true }
        _isSearching.update { false }
    }





}
