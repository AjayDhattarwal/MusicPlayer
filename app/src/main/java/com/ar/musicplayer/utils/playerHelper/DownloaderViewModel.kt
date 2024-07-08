package com.ar.musicplayer.utils.playerHelper

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.models.HomeData
import com.ar.musicplayer.models.SongResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val repository: MusicDownloadRepository,
) : ViewModel() {

    val currentDownloading = MutableLiveData<SongResponse?>(null)
    val songProgress = MutableLiveData<Int>(0)

    fun onEvent(event: DownloadEvent) {
        when (event) {
            is DownloadEvent.downloadSong -> downloadSong(event.songResponse)
            is DownloadEvent.isDownloaded -> isAllReadyDownloaded(event.songResponse,event.onCallback)
        }
    }

    fun isAllReadyDownloaded(songResponse: SongResponse, onCallback: (Boolean) -> Unit) {
        if(songResponse != null){
            repository.isFileExist(songResponse = songResponse){
                onCallback(it)
            }
        }
    }


    fun downloadSong(songResponse: SongResponse) {
        viewModelScope.launch(Dispatchers.IO) {
            currentDownloading.postValue(songResponse)
            repository.downloadSong( songResponse) { progress ->
                songProgress.postValue(progress)
                if (progress == 100) {
                    currentDownloading.postValue(null)
                    songProgress.postValue(0)
                }
            }
        }
    }

    fun deleteSong(songResponse: SongResponse) {
        repository.deleteSong(songResponse)
    }

}