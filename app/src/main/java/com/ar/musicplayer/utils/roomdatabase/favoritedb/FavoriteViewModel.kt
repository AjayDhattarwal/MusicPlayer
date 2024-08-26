package com.ar.musicplayer.utils.roomdatabase.favoritedb

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.data.repository.FavoriteDataRepository
import com.ar.musicplayer.data.models.SongResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject




@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val repository: FavoriteDataRepository
) : ViewModel() {


    fun onEvent(event: FavoriteSongEvent){
        when(event){
            is FavoriteSongEvent.ToggleFavSong -> addFavorite(event.songResponse)
            is FavoriteSongEvent.RemoveFromFav -> removeFavorite(event.songId)
        }
    }

    val favSongList: StateFlow<List<SongResponse>> = repository.favSongsStateFlow

    val songsByArtist: StateFlow<Map<String, List<SongResponse>>>  = repository.songsByArtist

    val songsByAlbum: StateFlow<Map<String, List<SongResponse>>> = repository.songsByAlbum




    fun isFavoriteSong(songId: String): Flow<Boolean> {
        return repository.isFavouriteSong(songId)
    }

    fun removeFavorite(songId: String) {
        viewModelScope.launch {
            repository.deleteFavSong(songId)
        }
    }

    fun addFavorite(songResponse: SongResponse) {
        viewModelScope.launch {
            repository.toggleFavorite(songResponse)
        }
    }
}