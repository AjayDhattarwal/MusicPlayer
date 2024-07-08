package com.ar.musicplayer.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

data class AccessTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)
@Serializable
data class SearchResponseForReco(
    @SerializedName("tracks") val tracks: Tracks = Tracks(emptyList())
)

@Serializable
data class Tracks(
    @SerializedName("items") val items: List<Track> = emptyList()
)

@Serializable
data class Track(
    @SerializedName("id")val id: String = "",
    @SerializedName("name")val name: String = "",
    @SerializedName("artists") val artists: List<Artist> = emptyList()
)

@Serializable
data class RecommendationsResponse(
    val tracks: List<Track>
)