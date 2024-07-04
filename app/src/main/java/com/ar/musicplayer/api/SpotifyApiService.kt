package com.ar.musicplayer.api

import com.ar.musicplayer.models.AccessTokenResponse
import com.ar.musicplayer.models.RecommendationsResponse
import com.ar.musicplayer.models.SearchResponseForReco
import retrofit2.Response
import retrofit2.http.*

interface SpotifyApiService {


    @GET("v1/search")
    suspend fun searchTrack(
        @Header("Authorization") authorization: String,
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("limit") limit: Int = 1,
        @Query("market") market: String = "IN"
    ): Response<SearchResponseForReco>

    @GET("v1/recommendations")
    suspend fun getRecommendations(
        @Header("Authorization") authorization: String,
        @Query("seed_tracks") seedTracks: String,
        @Query("limit") limit: Int = 10,
        @Query("market") market: String = "IN",
        @Query("seed_artists") seedArtist: String,
    ): Response<RecommendationsResponse>
}
