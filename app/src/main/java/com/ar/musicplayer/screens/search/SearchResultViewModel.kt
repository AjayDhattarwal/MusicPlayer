package com.ar.musicplayer.screens.search




import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.api.ApiConfig
import com.ar.musicplayer.models.BasicSongInfo
import com.ar.musicplayer.models.SearchResults
import com.ar.musicplayer.models.TopSearchResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(FlowPreview::class)
class SearchResultViewModel() : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _trendingSearchResults = MutableStateFlow<List<BasicSongInfo>?>(null)
    val trendingSearchResults = _trendingSearchResults.asStateFlow()

    private val _searchSongResults = MutableStateFlow<SearchResults?>(null)
    val searchSongResults = _searchSongResults.asStateFlow()

    private val _searchAlbumsResults = MutableStateFlow<SearchResults?>(null)
    val searchAlbumsResults = _searchAlbumsResults.asStateFlow()

    private val _searchArtistResults = MutableStateFlow<SearchResults?>(null)
    val searchArtistResults = _searchArtistResults.asStateFlow()

    private val _searchPlaylistResults = MutableStateFlow<SearchResults?>(null)
    val searchPlaylistResults = _searchPlaylistResults.asStateFlow()

    private val _topSearchResults = MutableStateFlow<TopSearchResults?>(null)
    val topSearchResults = _topSearchResults.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    var errorMessage: String = ""
        private set

    init {
        handleSearchQueries()
    }

    private fun handleSearchQueries() {
        viewModelScope.launch {
            searchText
                .debounce(500L)
                .onEach { _isSearching.update { true } }
                .distinctUntilChanged()
                .onEach { query ->
                    if (query.isNotBlank()) {
                        getTopDataResult("autocomplete.get", query, "in", "1")
                        getSpecificSearchResult("search.getResults", query, "1", "15")
                        getSpecificSearchResult("search.getAlbumResults", query, "1", "15")
                        getSpecificSearchResult("search.getArtistResults", query, "1", "15")
                        getSpecificSearchResult("search.getPlaylistResults", query, "1", "15")
                    } else {
                        getTrendingResult("content.getTopSearches")
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect()
        }
    }



    fun onSearchTextChange(text: String) {
        _searchText.value = text
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

    fun getSpecificSearchResult(call: String, query: String, page: String, totalSong: String) {
        _isError.update { false }
        val client = ApiConfig.getApiService().getSearchResults(
            call = call,
            query = query,
            page = page,
            totalSong = totalSong
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
                    "search.getResults" -> _searchSongResults.value = responseBody
                    "search.getAlbumResults" -> _searchAlbumsResults.value = responseBody
                    "search.getArtistResults" -> _searchArtistResults.value = responseBody
                    "search.getPlaylistResults" -> _searchPlaylistResults.value = responseBody
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

    private fun onError(inputMessage: String?) {
        val message = inputMessage?.takeIf { it.isNotBlank() } ?: "Unknown Error"
        errorMessage = "ERROR: $message. Some data may not be displayed properly."
        _isError.update { true }
        _isSearching.update { false }
    }
}