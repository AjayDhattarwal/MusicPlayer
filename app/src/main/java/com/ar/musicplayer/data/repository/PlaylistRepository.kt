package com.ar.musicplayer.data.repository

import com.ar.musicplayer.api.ApiService
import com.ar.musicplayer.data.models.ArtistResponse
import com.ar.musicplayer.data.models.PlaylistResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class PlaylistRepository @Inject constructor(private val apiService: ApiService) {

    fun fetchPlaylistDataTwice(
        token: String,
        type: String,
        initialSongCount: Int,
        onSuccess: (PlaylistResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val firstCall = apiService.getApiData(
            token = token,
            type = type,
            totalSong = initialSongCount.toString(),
            q = "1",
            call = "webapi.get"
        )

        firstCall.enqueue(object : Callback<PlaylistResponse> {
            override fun onResponse(
                call: Call<PlaylistResponse>,
                response: Response<PlaylistResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {

                    if (
                        responseBody.type != "album" && (responseBody.listCount ?: 0) > initialSongCount
                    ) {
                        // Second API call
                        fetchAdditionalPlaylistData(
                            token,
                            type,
                            responseBody.listCount ?: 0,
                            onSuccess,
                            onError
                        )
                    } else{
                        onSuccess(responseBody)
                    }
                } else {
                    onError("Data Processing Error")
                }
            }

            override fun onFailure(call: Call<PlaylistResponse>, t: Throwable) {
                onError(t.message ?: "Unknown Error")
            }
        })
    }

    private fun fetchAdditionalPlaylistData(
        token: String,
        type: String,
        updatedSongCount: Int,
        onSuccess: (PlaylistResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val secondCall = apiService.getApiData(
            token = token,
            type = type,
            totalSong = updatedSongCount.toString(),
            q = "1",
            call = "webapi.get"
        )

        secondCall.enqueue(object : Callback<PlaylistResponse> {
            override fun onResponse(
                call: Call<PlaylistResponse>,
                response: Response<PlaylistResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    onSuccess(responseBody)
                } else {
                    onError("Data Processing Error")
                }
            }

            override fun onFailure(call: Call<PlaylistResponse>, t: Throwable) {
                onError(t.message ?: "Unknown Error")
            }
        })
    }


    fun fetchArtistData(
        token: String,
        type: String = "artist",
        nAlbum: Int,
        nSong: Int,
        onSuccess: (ArtistResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val secondCall = apiService.getArtistData(
            token = token,
            type = type,
            nSong = nSong,
            nAlbum = nAlbum,
            call = "webapi.get"
        )

        secondCall.enqueue(object : Callback<ArtistResponse> {
            override fun onResponse(
                call: Call<ArtistResponse>,
                response: Response<ArtistResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    onSuccess(responseBody)
                } else {
                    onError("Data Processing Error")
                }
            }

            override fun onFailure(call: Call<ArtistResponse>, t: Throwable) {
                onError(t.message ?: "Unknown Error")
            }
        })
    }


}
