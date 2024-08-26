package com.ar.musicplayer.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.ar.musicplayer.data.models.MoreInfoResponse
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.utils.roomdatabase.dbmodels.FavSongResponseEntity
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavDao
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class FavoriteDataRepository @Inject constructor(
    private val favDao: FavDao

) {
    private val _favSongsStateFlow = MutableStateFlow<List<SongResponse>>(emptyList())
    val favSongsStateFlow: StateFlow<List<SongResponse>> get() = _favSongsStateFlow

    private val _songsByArtist = MutableStateFlow<Map<String, List<SongResponse>>>(emptyMap())
    val songsByArtist: StateFlow<Map<String, List<SongResponse>>> get() = _songsByArtist

    private val _songsByAlbum = MutableStateFlow<Map<String, List<SongResponse>>>(emptyMap())
    val songsByAlbum: StateFlow<Map<String, List<SongResponse>>> get() = _songsByAlbum


    init {
        startObservingFavSongs()
    }

    private fun startObservingFavSongs() {
        favDao.favSongList()
            .map { favSongList ->
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
                        moreInfo = Gson().fromJson(
                            favSongResponse.moreInfo,
                            MoreInfoResponse::class.java
                        ),
                        name = favSongResponse.name,
                        ctr = favSongResponse.ctr,
                        entity = favSongResponse.entity,
                        role = favSongResponse.role
                    )
                }
            }
            .onEach { songResponseList ->
                _favSongsStateFlow.value = songResponseList
                groupSongsByArtist()
                groupSongsByAlbum()
            }
            .launchIn(CoroutineScope(Dispatchers.IO)) // Replace coroutineContext with your actual CoroutineContext
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

    private fun groupSongsByArtist() {
        val groupedByArtist = favSongsStateFlow.value
            .flatMap { song ->
                song.moreInfo?.artistMap?.artists?.mapNotNull { artist ->
                    artist.name?.let { it to song }
                } ?: emptyList()
            }
            .groupBy({ it.first }, { it.second })
            .toSortedMap()

        _songsByArtist.value = groupedByArtist
    }

    private fun groupSongsByAlbum() {
        val groupedByAlbum = favSongsStateFlow.value
            .filterNotNull()
            .filter { it.moreInfo?.album != null }
            .groupBy { it.moreInfo?.album!! }
        _songsByAlbum.value = groupedByAlbum
    }

}
