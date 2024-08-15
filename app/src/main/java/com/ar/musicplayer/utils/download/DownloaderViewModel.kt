package com.ar.musicplayer.utils.download

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.data.models.SongResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.LinkedList
import java.util.Queue

@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val repository: MusicDownloadRepository,
) : ViewModel() {

    val currentDownloading = MutableLiveData<SongResponse?>(null)
    val songProgress = MutableLiveData<Int>(0)
    private val downloadQueue: Queue<SongResponse> = LinkedList()

    fun onEvent(event: DownloadEvent) {
        when (event) {
            is DownloadEvent.downloadSong -> queueDownloadSong(event.songResponse)
            is DownloadEvent.isDownloaded -> isAllReadyDownloaded(event.songResponse, event.onCallback)
        }
    }

    private fun queueDownloadSong(songResponse: SongResponse) {
        downloadQueue.add(songResponse)
        if(currentDownloading.value == null && downloadQueue.size == 1){
            proceedToNextDownload()
        }
    }

    private fun startDownload(songResponse: SongResponse) {
        viewModelScope.launch(Dispatchers.IO) {
            currentDownloading.postValue(songResponse)
            repository.downloadSong(songResponse) { progress ->
                songProgress.postValue(progress)
                if (progress == 100) {
                    currentDownloading.postValue(null)
                    songProgress.postValue(0)
                    proceedToNextDownload()
                }
            }
        }
    }

    private fun proceedToNextDownload() {
        val nextSong = downloadQueue.poll()
        if (nextSong != null) {
            startDownload(nextSong)
        }
    }

    fun isAllReadyDownloaded(songResponse: SongResponse, onCallback: (Boolean) -> Unit) {
        if (songResponse != null) {
            repository.isFileExist(songResponse) {
                onCallback(it)
            }
        }
    }

    fun deleteSong(songResponse: SongResponse) {
        repository.deleteSong(songResponse)
    }
}
