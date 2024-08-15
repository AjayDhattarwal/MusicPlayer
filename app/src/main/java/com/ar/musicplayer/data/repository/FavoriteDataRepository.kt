package com.ar.musicplayer.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.ar.musicplayer.data.models.MoreInfoResponse
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.utils.roomdatabase.dbmodels.FavSongResponseEntity
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavDao
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteDataRepository @Inject constructor(
    private val favDao: FavDao

) {

    fun mapFavSongsToSongResponse(): LiveData<List<SongResponse>> {
        return favDao.favSongList().map { favSongList ->
            favSongList.map { favSongResponse ->
                SongResponse(
                    id = favSongResponse.songId,
                    title = favSongResponse.title,
                    subtitle = favSongResponse.subtitle,
                    type = favSongResponse.type,
                    permaUrl = favSongResponse.permaUrl,
                    image = favSongResponse.image,
                    language = favSongResponse.language,
                    year = favSongResponse.year,
                    playCount = favSongResponse.playCount,
                    explicitContent = favSongResponse.explicitContent,
                    listCount = favSongResponse.listCount,
                    listType = favSongResponse.listType,
                    list = favSongResponse.list,
                    moreInfo = Gson().fromJson(favSongResponse.moreInfo, MoreInfoResponse::class.java),
                    name = favSongResponse.name,
                    ctr = favSongResponse.ctr,
                    entity = favSongResponse.entity,
                    role = favSongResponse.role
                )
            }
        }.asLiveData()
    }

    private val favSongIds: Flow<List<String>> = favDao.favSongList().map { favSongList ->
        favSongList.map { favSongResponse ->
            favSongResponse.songId
        }
    }

    suspend fun deleteFavSong(songId: String) {
        favDao.removeFromFav(songId)
    }

    fun isFavouriteSong(songId: String): Flow<Boolean> {
        return favSongIds.map { favSongIds ->
            favSongIds.contains(songId)
        }
    }

    suspend fun toggleFavorite(songResponse: SongResponse) {
        val moreInfoResponse = Gson().toJson(songResponse.moreInfo)
        val favSongResponse = FavSongResponseEntity(
            id = null,
            songId = songResponse.id.toString(),
            title = songResponse.title.orEmpty(),
            subtitle = songResponse.subtitle.orEmpty(),
            type = songResponse.type.orEmpty(),
            permaUrl = songResponse.permaUrl.orEmpty(),
            image = songResponse.image.orEmpty(),
            language = songResponse.language.orEmpty(),
            year = songResponse.year.orEmpty(),
            playCount = songResponse.playCount.orEmpty(),
            explicitContent = songResponse.explicitContent.orEmpty(),
            listCount = songResponse.listCount ?: 0,
            listType = songResponse.listType.orEmpty(),
            list = songResponse.list.orEmpty(),
            moreInfo = moreInfoResponse,
            name = songResponse.name.orEmpty(),
            ctr = songResponse.ctr ?: 0,
            entity = songResponse.entity.orEmpty(),
            role = songResponse.role.orEmpty()
        )
        favDao.toggleFavorite(favSongResponse)
    }

    suspend fun toggleCurrentSong(songId: String){

    }

}
