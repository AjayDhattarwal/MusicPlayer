package com.ar.musicplayer.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.api.ApiConfig
import com.ar.musicplayer.data.models.Album
import com.ar.musicplayer.data.models.Artist
import com.ar.musicplayer.data.models.ArtistResult
import com.ar.musicplayer.data.models.BasicSongInfo
import com.ar.musicplayer.data.models.PlaylistResponse
import com.ar.musicplayer.data.models.SearchResults
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.TopSearchResults
import com.ar.musicplayer.data.models.toAlbumResponse
import com.ar.musicplayer.data.models.toArtist
import com.ar.musicplayer.data.models.toPlaylistResponse
import com.ar.musicplayer.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    private val  repository: SearchRepository
) : ViewModel() {

    var searchText by mutableStateOf("")
        private set


    private val _isSearching = repository._isSearching
    val isSearching = _isSearching.asStateFlow()

    val trendingSearchResults = repository.trendingSearchResults

    val searchSongResults = repository.searchSongResults


    val searchAlbumsResults = repository.searchAlbumsResults


    val searchArtistResults = repository.searchArtistResults

    val searchPlaylistResults = repository.searchPlaylistResults


    val topSearchResults = repository.topSearchResults


    val isError = repository.isError


    init {
        handleSearchQueries()
    }

    private fun handleSearchQueries() {
        viewModelScope.launch {
            snapshotFlow { searchText }
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
        searchText = text
    }

    fun getTopDataResult(  call: String, query: String,cc: String, includeMetaTags: String ) {
        repository.getTopDataResult(call, query, cc, includeMetaTags)
    }

    fun getSpecificSearchResult(call: String, query: String, page: String, totalResults: String) {
       repository.getSpecificSearchResult(call, query, page, totalResults)
    }

    fun getTrendingResult(call: String) {
        repository.getTrendingResult(call)
    }

}