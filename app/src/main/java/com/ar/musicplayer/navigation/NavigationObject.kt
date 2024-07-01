package com.ar.musicplayer.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeScreenObj

@Serializable
object SearchScreenObj

@Serializable
object SettingsScreenObj

@Serializable
object LibraryScreenObj


@Serializable
data class InfoScreenObj(  //
    val serialized : String
)

@Serializable
data class PlayerScreenObj(
    val songResponse: String
)

@Serializable
object FavoriteScreenObj

@Serializable
object ListeningHisScreenObj
