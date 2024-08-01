package com.ar.musicplayer.di.roomdatabase.favoritedb

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.di.roomdatabase.dbmodels.FavSongResponseEntity
import com.ar.musicplayer.models.MoreInfoResponse
import com.ar.musicplayer.models.SongResponse
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favDao: FavDao
): ViewModel() {

    val favSongList: LiveData<List<SongResponse>> =
        favDao.favSongList().map { favSongList ->
            favSongList.map { favSongResponse ->
                SongResponse(
                    id = favSongResponse.songId,
                    title = favSongResponse.title ,
                    subtitle = favSongResponse.subtitle,
                    type = favSongResponse.type,
                    permaUrl = favSongResponse.permaUrl,
                    image =  favSongResponse.image,
                    language= favSongResponse.language,
                    year= favSongResponse.year,
                    playCount= favSongResponse.playCount,
                    explicitContent= favSongResponse.explicitContent,
                    listCount= favSongResponse.listCount,
                    listType= favSongResponse.listType,
                    list= favSongResponse.list,
                    moreInfo= Gson().fromJson(favSongResponse.moreInfo,MoreInfoResponse::class.java),
                    name= favSongResponse.name,
                    ctr= favSongResponse.ctr,
                    entity= favSongResponse.entity,
                    role= favSongResponse.role
                )
            }
        }.asLiveData()

    fun onEvent(event: FavoriteSongEvent){
        when(event){
            is FavoriteSongEvent.ToggleFavSong -> insertAsFavSong(event.songResponse)
            is FavoriteSongEvent.RemoveFromFav -> deleteFavSong(event.songId)
            is FavoriteSongEvent.IsFavoriteSong -> isFavouriteSong(event.songId, event.callback)
        }
    }

    private fun deleteFavSong(songId: String) {
        viewModelScope.launch {
            favDao.removeFromFav(songId)
        }
    }

    private fun insertAsFavSong(songResponse: SongResponse) {
        viewModelScope.launch {
            val moreInfoResponse = Gson().toJson(songResponse.moreInfo)
            val favSongResponse = FavSongResponseEntity(
                id = null,
                songId = songResponse.id.toString(),
                title = songResponse.title.toString() ,
                subtitle = songResponse.subtitle.toString(),
                type = songResponse.type.toString(),
                permaUrl = songResponse.permaUrl.toString(),
                image =  songResponse.image.toString(),
                language= songResponse.language.toString(),
                year= songResponse.year.toString(),
                playCount= songResponse.playCount.toString(),
                explicitContent= songResponse.explicitContent.toString(),
                listCount= songResponse.listCount.toString(),
                listType= songResponse.listType.toString(),
                list= songResponse.list.toString(),
                moreInfo= moreInfoResponse,
                name= songResponse.name.toString(),
                ctr= songResponse.ctr.toString(),
                entity= songResponse.entity.toString(),
                role= songResponse.role.toString()
            )
            favDao.toggleFavorite(favSongResponse)
        }
    }


    private fun isFavouriteSong(songId: String, onCallBack: (Flow<Boolean>) -> Unit){
        viewModelScope.launch {
            val isFav = favDao.isFavorite(songId)
            onCallBack(isFav)
        }
    }
}