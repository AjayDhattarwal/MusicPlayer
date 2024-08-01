package com.ar.musicplayer.di.roomdatabase.favoritedb

import com.ar.musicplayer.models.SongResponse
import kotlinx.coroutines.flow.Flow

sealed interface FavoriteSongEvent {
    data class ToggleFavSong(val songResponse: SongResponse): FavoriteSongEvent
    data class RemoveFromFav(val songId: String): FavoriteSongEvent
    data class IsFavoriteSong(val songId: String, val callback: (Flow<Boolean>) -> Unit) : FavoriteSongEvent
}