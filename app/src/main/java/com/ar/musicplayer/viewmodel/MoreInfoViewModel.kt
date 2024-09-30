package com.ar.musicplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.data.models.ArtistResponse
import com.ar.musicplayer.data.models.HomeData
import com.ar.musicplayer.data.models.HomeListItem
import com.ar.musicplayer.data.models.InfoScreenModel
import com.ar.musicplayer.data.repository.PlaylistRepository
import com.ar.musicplayer.data.models.PlaylistResponse
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.repository.SearchRepository
import com.ar.musicplayer.data.repository.YoutubeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MoreInfoViewModel @Inject constructor(
    private val repository: PlaylistRepository,
    private val youtubeRepository: YoutubeRepository,
    private val searchRepository: SearchRepository
) : ViewModel() {
    private val _playlistData = MutableLiveData<PlaylistResponse?>()
    val playlistData: LiveData<PlaylistResponse?> get() = _playlistData

    private val _artistData = MutableStateFlow<ArtistResponse?>(null)
    val artistData: StateFlow<ArtistResponse?> = _artistData.asStateFlow()


    private val _isLoading = MutableStateFlow<Boolean>(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> get() = _isError

    var errorMessage: String = ""
        private set

    fun fetchPlaylistData(token: String, type: String, totalSong: Int, q: String, call: String) {
        viewModelScope.launch {
            repository.fetchPlaylistDataTwice(
                token = token,
                type = type,
                initialSongCount = totalSong,
                onSuccess = { response ->
                    _playlistData.postValue(response)
                    _isLoading.value = false
                },
                onError = { message ->
                    handleError(message)
                }
            )
        }
    }

    fun fetchArtistData(token: String, type: String, nSong: Int, nAlbum: Int, call: String){
        _isLoading.value = true
        _isError.value = false

        viewModelScope.launch {
            repository.fetchArtistData(
                token = token,
                type = type,
                nAlbum = nAlbum,
                nSong = nSong,
                onSuccess = { response ->
                    _artistData.value = response
                    _isLoading.value = false
                },
                onError = { message ->
                    handleError(message)
                }
            )
        }
    }

    fun fetchPlaylist(data: InfoScreenModel){
        _isLoading.value = true
        _isError.value = false

        if(!data.isYoutube){
            fetchPlaylistData(
                data.token,
                data.type,
                data.songCount,
                "1",
                "webapi.get"
            )
        } else {
            viewModelScope.launch{
                withContext(Dispatchers.IO){
                    val songsList = getYTPlaylistSongs(data.id)

                    _playlistData.postValue(
                        PlaylistResponse(
                            title = data.title,
                            id = data.id,
                            image = data.image,
                            permaUrl = "https://www.youtube.com/playlist?list=${data.id}",
                            type = data.type,
                            list = songsList ?: emptyList(),
                            listCount = songsList?.size ?: 0,
                            subtitle = data.subtitle,
                        )
                    )
                    _isLoading.value = false
                }
            }
        }
    }

    suspend fun getYTPlaylistSongs(id: String): List<SongResponse>? {
        val list = youtubeRepository.ytPlaylistSongs(id)
        return list
    }

    private fun handleError(message: String) {
        errorMessage = "ERROR: $message. Some data may not be displayed properly."
        _isError.value = true
        _isLoading.value = false
    }
}
