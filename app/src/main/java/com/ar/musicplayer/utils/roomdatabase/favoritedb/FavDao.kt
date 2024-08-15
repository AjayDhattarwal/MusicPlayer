package com.ar.musicplayer.utils.roomdatabase.favoritedb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.ar.musicplayer.utils.roomdatabase.dbmodels.FavSongResponseEntity
import com.ar.musicplayer.data.models.MoreInfoResponse
import com.ar.musicplayer.data.models.SongResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Dao
interface FavDao {

    @Transaction
    suspend fun toggleFavorite(favSongResponseEntity: FavSongResponseEntity) {
        val isFavourite = isFavorite(favSongResponseEntity.songId).first()
        if (isFavourite) {
            removeFromFav(favSongResponseEntity.songId)
        } else {
            setAsFav(favSongResponseEntity)
        }
    }



    @Insert
    suspend fun setAsFav(favSongResponseEntity: FavSongResponseEntity)

    @Query("DELETE FROM FavSongResponseEntity WHERE songId = :id ")
    suspend fun removeFromFav(id: String)

    @Query("SELECT EXISTS(SELECT * FROM FavSongResponseEntity WHERE songId = :id )" )
    fun isFavorite(id: String): Flow<Boolean>

    @Query("SELECT * FROM FavSongResponseEntity ORDER BY id DESC ")
    fun favSongList(): Flow<List<FavSongResponseEntity>>
}