package com.ar.musicplayer.data.repository

import com.ar.musicplayer.api.ApiConfig
import com.ar.musicplayer.api.ApiService
import com.ar.musicplayer.data.models.ImportPlaylistResponse
import com.ar.musicplayer.data.models.SearchResults
import com.ar.musicplayer.data.models.SongResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse
import javax.inject.Inject

class SongDetailsRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun fetchSongDetails(pids: String): SongResponse? {
        val response = apiService.getSongDetails(pids).awaitResponse()
        if (response.isSuccessful) {
            return response.body()?.songs?.firstOrNull()
        } else {
            return null
        }
    }

    suspend fun searchSingleSong(title: String): SongResponse?{
        val response = apiService.getSearchResultsSuspended(
            call = "search.getResults",
            query = title,
            page = "1",
            totalSong = "5"
        )
        val results = response.results ?: return null

        if (results.isEmpty()) {
            return null
        }

        val cleanedTitle = title.replace(" ", "").lowercase()

        val exactMatch = results.firstOrNull {
            it.title?.substringBefore("(")
                ?.replace(" ", "")
                ?.equals(cleanedTitle, ignoreCase = true) == true
        }

        val closestMatch = results.takeLastWhile {
            it.title?.substringBefore("(")
                ?.replace(" ", "")
                ?.contains(cleanedTitle, ignoreCase = true) == true
        }.minByOrNull {
            it.title?.length?.minus(title.length) ?: Int.MAX_VALUE
        }

        return exactMatch ?: closestMatch ?: results[0]
    }

    suspend fun searchPlaylist(
        importPlaylistResponse: ImportPlaylistResponse
    ): List<SongResponse> = withContext(Dispatchers.IO) {
        importPlaylistResponse.items.mapNotNull { playlistItem ->
            searchSingleSong(playlistItem.name)
        }
    }
}