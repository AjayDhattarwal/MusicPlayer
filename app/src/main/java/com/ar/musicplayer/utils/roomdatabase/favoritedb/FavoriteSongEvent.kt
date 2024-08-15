package com.ar.musicplayer.utils.roomdatabase.favoritedb

import com.ar.musicplayer.data.models.SongResponse
import kotlinx.coroutines.flow.Flow

sealed interface FavoriteSongEvent {
    data class ToggleFavSong(val songResponse: SongResponse): FavoriteSongEvent
    data class RemoveFromFav(val songId: String): FavoriteSongEvent
}