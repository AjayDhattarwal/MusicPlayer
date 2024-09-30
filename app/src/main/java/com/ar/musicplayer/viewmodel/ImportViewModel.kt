package com.ar.musicplayer.viewmodel

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.data.models.ImportPlaylistResponse
import com.ar.musicplayer.data.models.PlaylistResponse
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.getArtistsString
import com.ar.musicplayer.data.models.toPlaylistResponse
import com.ar.musicplayer.data.repository.ImportPlaylistRepository
import com.ar.musicplayer.data.repository.LocalPlaylistsRepository
import com.ar.musicplayer.data.repository.SearchRepository
import com.ar.musicplayer.data.repository.SongDetailsRepository
import com.ar.musicplayer.utils.roomdatabase.playlistdb.LocalPlaylistSong
import com.ar.musicplayer.utils.roomdatabase.playlistdb.RoomPlaylist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val importPlaylistRepository: ImportPlaylistRepository,
    private val songDetailsRepository: SongDetailsRepository,
    private val playlistRepository: LocalPlaylistsRepository

): ViewModel() {

    private val _playlists = MutableStateFlow<List<PlaylistResponse>>(emptyList())
    val playlists : StateFlow<List<PlaylistResponse>> get() = _playlists.asStateFlow()
        .onStart { fetchAllPlaylists() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    private val _localPlaylists = MutableStateFlow<List<RoomPlaylist>>(emptyList())
    val localPlaylists : StateFlow<List<RoomPlaylist>> get() = _localPlaylists.asStateFlow()
        .onStart { fetchLocalPlaylists() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentImportingPlaylist = MutableStateFlow<ImportPlaylistResponse?>(null)
    val currentImportingPlaylist : StateFlow<ImportPlaylistResponse?>
        get() = _currentImportingPlaylist.asStateFlow()

    var deleteJob: Job? = null


    fun importPlaylist(url: String) {
       viewModelScope.launch {
           val playlistType = getPlaylistType(url)
           if(playlistType == "Unknown"){
               return@launch
           }
           val response =
               when(playlistType){
                   "Spotify" -> importPlaylistRepository.importSpotifyPlaylist(url)
                   "Apple Music" -> importPlaylistRepository.importApplePlaylist(url)
                   else -> null
               }

           _currentImportingPlaylist.value = response

           response?.let { searchPlaylist(it) }

           _currentImportingPlaylist.value = null

           Timber.tag("ImportViewModel").d("Response: $response")
       }
    }

    private fun getPlaylistType(url: String): String {

        val countryCodePattern = """
            /(us|ca|gb|au|de|fr|it|es|nl|se|no|dk|fi|jp|br|mx|in|sg|hk|my|tw|pl|ru|za|ae|kr)/playlist/
        """.trimIndent().toRegex()

        return when {
            url.startsWith("https://open.spotify.com/playlist/", ignoreCase = true)
                    && url.substringAfterLast("/").isNotEmpty() -> "Spotify"
            url.startsWith("https://music.apple.com/", ignoreCase = true)
                    && countryCodePattern.containsMatchIn(url)
                    && url.substringAfterLast("/").isNotEmpty() -> "Apple Music"
            else -> "Unknown"
        }
    }

    private suspend fun searchPlaylist(importPlaylistResponse: ImportPlaylistResponse) {
        val response = songDetailsRepository.searchPlaylist(importPlaylistResponse)

        val localSongs = response.toLocalPlaylistSong(importPlaylistResponse.id)

        val playlist = importPlaylistResponse.toRoomPlaylist()

        insertImportPlaylist(playlist = playlist)

        localSongs.forEach { song ->
            insertImportSong(song)
        }
    }


    private fun insertImportPlaylist(playlist: RoomPlaylist) {
        viewModelScope.launch {
            playlistRepository.insertPlaylist(playlist = playlist)
        }
    }

    private fun insertImportSong(song: LocalPlaylistSong) {
        viewModelScope.launch {
            playlistRepository.insertSong(song)
        }
    }

    fun fetchAllPlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllPlaylists().collect { playlistWithSongs ->
                _playlists.value = playlistWithSongs.toPlaylistResponse()
            }
        }
    }

    fun fetchLocalPlaylists() {
        viewModelScope.launch {
             playlistRepository.getPlaylistWithOutSongs().collect{ playlistWithOutSongs ->
                 _localPlaylists.value = playlistWithOutSongs
             }
        }
    }



    fun createPlaylist(title: String, description: String?){
        viewModelScope.launch {
            playlistRepository.insertPlaylist(RoomPlaylist(
                id = generateRandomId(),
                name = title,
                description = description ?: "Playlist Created By You",
                songCount = 0,
                image = "",
            ))
        }
    }

    fun addSongToPlaylist(song: SongResponse, playlistIds: List<String>){
        viewModelScope.launch{
            playlistIds.forEach { playlistId ->
                playlistRepository.insertSong(song.toLocalPlaylistSong(playlistId))
                updatePlaylist(
                    playlistId = playlistId,
                    title = null,
                    description =  null
                )
            }
        }
    }

    fun updatePlaylist(playlistId: String, title: String?, description: String?){
        viewModelScope.launch {
            playlistRepository.updatePlaylist(
                playlistId = playlistId,
                title = title,
                description = description,
            )
        }
    }

    fun deletePlaylist(playlistId: String?){
        deleteJob = viewModelScope.launch {
            delay(6000)
            playlistId?.let { playlistRepository.deletePlaylist(it) }
        }

    }
    fun undoDeleteTask(){
        deleteJob?.cancel()
    }

}


private fun generateRandomId(length: Int = 7): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun ImportPlaylistResponse.toRoomPlaylist(): RoomPlaylist {
    return RoomPlaylist(
        id = this.id,
        name = this.name,
        description = this.description,
        songCount = this.pagingInfo.totalCount,
        image = this.image,
        pageUrl = this.pageUrl
    )
}

fun List<SongResponse>.toLocalPlaylistSong(playlistId: String): List<LocalPlaylistSong>{
    return this.map { item ->
         item.toLocalPlaylistSong(playlistId)
    }
}

fun SongResponse.toLocalPlaylistSong(playlistId: String): LocalPlaylistSong{
    return LocalPlaylistSong(
        playlistId = playlistId,
        songId = id.toString(),
        title =  title.toString(),
        artist =  getArtistsString().toString(),
        album = moreInfo?.album.toString(),
        duration = moreInfo?.duration?.toLong()?: 0,
        image =  image.toString(),
        encryptedUrl = moreInfo?.encryptedMediaUrl.toString()
    )
}