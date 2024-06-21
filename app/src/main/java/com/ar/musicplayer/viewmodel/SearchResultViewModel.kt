package com.ar.musicplayer.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ar.musicplayer.di.ApiConfig
import com.ar.musicplayer.models.BasicSongInfo
import com.ar.musicplayer.models.SearchResults
import com.ar.musicplayer.models.TopSearchResults
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SearchResultViewModel() : ViewModel() {

    private val _topSearchResults = MutableLiveData<TopSearchResults?>()
    val searchTopLiveData: MutableLiveData<TopSearchResults?> get() = _topSearchResults

    private val _searchTopResults = MutableLiveData<SearchResults?>()
    val searchSongLiveData: MutableLiveData<SearchResults?> get() = _searchTopResults

    private val _searchAlbumsResults = MutableLiveData<SearchResults?>()
    val searchAlbumsLiveData: MutableLiveData<SearchResults?> get() = _searchAlbumsResults

    private val _searchArtistResults = MutableLiveData<SearchResults?>()
    val searchArtistsLiveData: MutableLiveData<SearchResults?> get() = _searchArtistResults

    private val _searchPlaylistResults = MutableLiveData<SearchResults?>()
    val searchPlaylistLiveData: MutableLiveData<SearchResults?> get() = _searchPlaylistResults


    private val _topSearch = MutableLiveData<List<BasicSongInfo>?>()
    val trendingSearchResults: MutableLiveData<List<BasicSongInfo>?> get() = _topSearch



    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> get() = _isError

    var errorMessage: String = ""
        private set

    fun getSearchResult(  call: String,query: String,page: String, totalSong: String ) {
        _isLoading.value = true
        _isError.value = false

        val client = ApiConfig.getApiService().getSearchResults(
            call = call,
            query = query,
            page = page,
            totalSong = totalSong
        )

        // Send API request using Retrofit
        client.enqueue(object : Callback<SearchResults> {

            override fun onResponse(
                retrofitCall: Call<SearchResults>,
                response: Response<SearchResults>
            ) {
                val responseBody = response.body()
                if (!response.isSuccessful || responseBody == null) {
                    onError("Data Processing Error")
                    return
                }

                _isLoading.value = false
                when (call) {
                    "search.getResults" -> {
                        _searchTopResults.postValue(responseBody)
                    }
                    "search.getAlbumResults" -> {
                        _searchAlbumsResults.postValue(responseBody)
                    }
                    "search.getArtistResults" -> {
                        _searchArtistResults.postValue(responseBody)
                    }
                    "search.getPlaylistResults" -> {
                        _searchPlaylistResults.postValue(responseBody)
                    }
                    else -> {
                        Log.d("search","search condition error")
                    }
                }

            }

            override fun onFailure(call: Call<SearchResults>, t: Throwable) {
                onError(t.message)
                t.printStackTrace()
            }

        })
    }

    fun getTopDataResult(  call: String, query: String,cc: String, includeMetaTags: String ) {
        _isLoading.value = true
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

                _isLoading.value = false

                _topSearchResults.postValue(responseBody)


            }

            override fun onFailure(call: Call<TopSearchResults>, t: Throwable) {
                onError(t.message)
                t.printStackTrace()
            }

        })
    }



    fun getTopSearchResult(call: String) {

        _isLoading.value = true
        _isError.value = false

        val client = ApiConfig.getApiService().getTopSearch(
            call = call
        )

        // Send API request using Retrofit
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

                _isLoading.value = false
                _topSearch.postValue(responseBody)
            }

            override fun onFailure(call: Call<List<BasicSongInfo>>, t: Throwable) {
                onError(t.message)
                t.printStackTrace()
            }

        })
    }

    private fun onError(inputMessage: String?) {

        val message = if (inputMessage.isNullOrBlank() or inputMessage.isNullOrEmpty()) "Unknown Error"
        else inputMessage

        errorMessage = StringBuilder("ERROR: ")
            .append("$message some data may not displayed properly").toString()

        _isError.value = true
        _isLoading.value = false
    }

}