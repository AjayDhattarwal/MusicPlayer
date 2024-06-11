package com.ar.musicplayer.navigation

import com.ar.musicplayer.models.SongResponse
import kotlinx.serialization.Serializable

@Serializable
object HomeScreenObj

@Serializable
data class InfoScreenObj(  //
    val serialized : String
)

@Serializable
data class PlayerScreenObj(
    val songResponse: String
)