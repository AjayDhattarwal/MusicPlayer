package com.ar.musicplayer.data.repository

import com.ar.musicplayer.api.ApiService
import com.ar.musicplayer.data.models.SongResponse
import retrofit2.awaitResponse
import javax.inject.Inject

class SongDetailsRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun fetchSongDetails(pids: String): SongResponse {
        val response = apiService.getSongDetails(pids).awaitResponse()
        if (response.isSuccessful) {
            return response.body()?.songs?.get(0) ?: throw Exception("No song details found")
        } else {
            throw Exception("Failed to fetch song details: ${response.errorBody()?.string()}")
        }
    }
}