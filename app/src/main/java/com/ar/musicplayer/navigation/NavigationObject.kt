package com.ar.musicplayer.navigation

import androidx.compose.runtime.Stable
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
data class InfoScreenObj(
    val data: String
)


@Serializable
object FavoriteScreenObj

@Serializable
object ListeningHisScreenObj

@Serializable
object MyMusicScreenObj

@Serializable
object MusicRecognizerObj

@Serializable
object SearchMyMusicObj

@Serializable
data class DetailsScreenObj(
    val playlistResponse: String,
)

@Serializable
object ThemeSettingObj

@Serializable
object DownloadSettingsScreenObj

@Serializable
object LanguageSettingsScreenObj

@Serializable
object PlaybackSettingsScreenObj

@Serializable
object StorageSettingScreenObj

@Serializable
data class ArtistInfoScreenObj(
    val artistInfo: String
)

@Serializable
object PlaylistFetchScreenObj

@Serializable
object CurrPlayingPlaylistObj

@Serializable
object LargeScreenPlayerObj