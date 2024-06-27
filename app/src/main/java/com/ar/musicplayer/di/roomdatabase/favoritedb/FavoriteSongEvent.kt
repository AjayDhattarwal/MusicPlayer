package com.ar.musicplayer.di.roomdatabase.favoritedb

import com.ar.musicplayer.models.SongResponse
import kotlinx.coroutines.flow.Flow

sealed interface FavoriteSongEvent {
    data class toggleFavSong(val songResponse: SongResponse): FavoriteSongEvent
    data class removeFromFav(val songId: String): FavoriteSongEvent
    data class isFavoriteSong(val songId: String, val callback: (Flow<Boolean>) -> Unit) : FavoriteSongEvent
}