package com.ar.musicplayer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.api.ApiService
import com.ar.musicplayer.data.models.AiResponse
import com.ar.musicplayer.data.models.Artist
import com.ar.musicplayer.data.models.SearchResults
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.toArtist
import com.ar.musicplayer.data.repository.GenerativeAiRepository
import com.ar.musicplayer.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    private val repository: GenerativeAiRepository,
    private val apiService : ApiService,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _aiSongResults = MutableStateFlow<List<SongResponse>>(emptyList())
    val aiSongResults = _aiSongResults.asStateFlow()

    private val _aiArtistResults = MutableStateFlow<List<Artist>>(emptyList())
    val aiArtistResults = _aiArtistResults.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _other = MutableStateFlow("")
    val other = _other.asStateFlow()

    private val _type = MutableStateFlow("")
    val type = _type.asStateFlow()

    private val _genre = MutableStateFlow("")
    val genre = _genre.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val isLoading = _loading.asStateFlow()


    fun getAiResponse(prompt: String) {
        _aiSongResults.value = emptyList()
        _aiArtistResults.value = emptyList()
        _description.value = ""
        _other.value = ""
        _type.value = ""
        _genre.value = ""

        _loading.value = true
        viewModelScope.launch {

            val result = repository.getAiResponse(prompt)
            val jsonText = result?.text
                ?.replace("```json","")
                ?.replace("```","").toString()

            val aiResponse =  parseJsonToAiResponse(jsonText)

            if(aiResponse.type == "song"){
                aiResponse.songs?.forEach { song ->
                    if(song.title != null || song.title != ""){
                        getSearchResult(
                            call = "search.getResults",
                            query = "${song.title} by ${song.artist}",
                            page = "1",
                            totalResults = "1"
                        )
                    }
                }
            }else{
                aiResponse.artists?.forEach { artist ->
                    if(artist.name != null || artist.name != ""){
                        getSearchResult(
                            call = "search.getArtistResults",
                            query = "${artist.name}",
                            page = "1",
                            totalResults = "1"
                        )
                    }
                }
            }

            _description.value = aiResponse.description.toString()
            _other.value = aiResponse.other.toString()
            _type.value = aiResponse.type.toString()
            _genre.value = aiResponse.type.toString()

        }
    }

    private fun parseJsonToAiResponse(jsonString: String): AiResponse {
        return Json.decodeFromString(jsonString)
    }

    private fun getSearchResult(call: String, query: String, page: String, totalResults: String) {
        searchRepository.getSearchResultForAi(
            call = call ,
            query = query,
            page = page,
            totalResults = totalResults,
            onResult = {
                resultMapper(it, call)
            },
            onError = {}
        )
    }

    private fun resultMapper(searchResults: SearchResults, call: String) {
        when (call) {
            "search.getResults" ->  addSearchResults(searchResults)
            "search.getArtistResults" -> addArtistSearchResults(searchResults)
            else -> Log.d("search", "search condition error")
        }
        _loading.value = false
    }

    private fun addSearchResults(searchResults: SearchResults) {
        _aiSongResults.update { currentList ->
            currentList + (searchResults.results ?: emptyList())
        }
    }

    private fun addArtistSearchResults(searchResults: SearchResults) {
        _aiArtistResults.update { currentList ->
            currentList + (searchResults.results?.map { it.toArtist() } ?: emptyList())
        }
    }


}



