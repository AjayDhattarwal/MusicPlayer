package com.ar.musicplayer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.api.ApiConfig
import com.ar.musicplayer.api.ApiService
import com.ar.musicplayer.api.SpotifyApiService
import com.ar.musicplayer.api.SpotifyAuth
import com.ar.musicplayer.data.models.SearchResults
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.utils.events.RecommendationEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SpotifyRecoViewModel @Inject constructor(

) : ViewModel() {


   val recommendedSongs =  MutableStateFlow<List<SongResponse>>(emptyList())

    private val spotifyApiService: SpotifyApiService = ApiConfig.getSpotifyApiService()
    private val jioSaavnApiService: ApiService = ApiConfig.getApiService()

    fun onEvent(event: RecommendationEvent){
        when(event){
            is RecommendationEvent.GetRecommendations -> getRecommendations(songName = event.songName)
        }
    }

    private fun getRecommendations(songName:String){
        viewModelScope.launch {
            try{
                val accessToken = SpotifyAuth.getAccessToken()

                val searchResponse = withContext(Dispatchers.IO) {
                    spotifyApiService.searchTrack(
                        authorization = "Bearer $accessToken",
                        query = songName
                    )
                }
                if(searchResponse.isSuccessful){
                    val trackId = searchResponse.body()?.tracks?.items?.firstOrNull()?.id
                    val artistId = searchResponse.body()?.tracks?.items?.firstOrNull()?.artists?.firstOrNull()?.id
                    if(trackId != null && artistId != null){
                        val recommendationsResponse = withContext(Dispatchers.IO) {
                            spotifyApiService.getRecommendations(
                                authorization = "Bearer $accessToken",
                                seedTracks = trackId,
                                seedArtist = artistId,
                                market = "IN"
                            )
                        }

                        if(recommendationsResponse.isSuccessful){
                            val recommendedTracks = recommendationsResponse.body()?.tracks ?: emptyList()
                            val responseList = mutableListOf<SongResponse>()
                            recommendedTracks.mapNotNull { track ->
                                getSearchResult(
                                    "search.getResults",
                                    track.name,
                                    "1",
                                    "1",
                                    songResponseCallback = { item ->
                                        responseList.add(item)
                                        recommendedSongs.value = responseList
                                    }
                                )
                            }

                        } else {
                            Log.d("recom" ,"failure to get recommendations")
                        }
                    } else {
                        Log.d("recom","failure to get track id")
                    }
                } else {
                    Log.d("recom","failure to search track")
                }
            } catch (e: Exception){
                Log.d("recom","${e.message}")
            }
        }


    }

    private suspend fun getSearchResult(
        call: String,
        query: String,
        page: String,
        numResults: String,
        songResponseCallback: (SongResponse) -> Unit
    ) {
        try {
            val client = withContext(Dispatchers.IO) {
                jioSaavnApiService.getSearchResults(
                    call = call,
                    query = query,
                    totalSong = numResults,
                    page = page
                )
            }
            client.enqueue(object : Callback<SearchResults> {
                override fun onResponse(call: Call<SearchResults>, response: Response<SearchResults>) {
                    response.body()?.results?.firstOrNull()?.let { songResponse ->
                        songResponseCallback(songResponse)
                    }
                }

                override fun onFailure(call: Call<SearchResults>, t: Throwable) {
                    Log.d("recom", "Search request failed: ${t.message}")
                }
            })
        } catch (e: Exception) {
            Log.d("recom", "${e.message}")
        }
    }

}