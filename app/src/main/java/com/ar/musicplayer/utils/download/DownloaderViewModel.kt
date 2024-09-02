package com.ar.musicplayer.utils.download

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.toSongDownloadEntity
import com.ar.musicplayer.utils.helper.NetworkConnectivityObserver
import com.ar.musicplayer.utils.roomdatabase.dbmodels.SongDownloadEntity
import com.ar.musicplayer.utils.roomdatabase.downloadDb.DownloadDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.LinkedList
import java.util.Queue

@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val repository: MusicDownloadRepository,
    private val networkConnectivityObserver: NetworkConnectivityObserver,
    private val downloadDao: DownloadDao
) : ViewModel() {

    private val _currentDownloading = MutableStateFlow<SongDownloadEntity?>(null)
    val currentDownloading: StateFlow<SongDownloadEntity?> get() = _currentDownloading

    private val _songProgress = MutableStateFlow<Int>(0)
    val songProgress: StateFlow<Int> get() = _songProgress

    private val downloadQueue: MutableList<SongDownloadEntity> = mutableListOf()

    private val _songDownloadStatus = MutableLiveData<Map<String, DownloadStatus>>(emptyMap())
    val songDownloadStatus: LiveData<Map<String, DownloadStatus>> = _songDownloadStatus

    init {
        viewModelScope.launch {
            networkConnectivityObserver.observe().collect { isConnected ->
                if (isConnected) {
                    loadQueueFromDatabase()
                    checkAndResumeDownloads()
                }
            }
        }
    }

    fun onEvent(event: DownloadEvent) {
        when (event) {
            is DownloadEvent.downloadSong -> queueDownloadSong(event.songResponse)
            is DownloadEvent.isDownloaded -> {
                isAlreadyDownloaded(
                    event.songResponse.toSongDownloadEntity(),
                    event.onCallback
                )
            }
        }
    }

    private fun queueDownloadSong(songResponse: SongResponse) {
        val entity = songResponse.toSongDownloadEntity()

        if (!downloadQueue.contains(entity)) {
            downloadQueue.add(entity)
            updateSongStatus(entity, DownloadStatus.WAITING)
            saveQueueToDatabase()  // Persist the updated queue
        }
        if (currentDownloading.value == null) {
            proceedToNextDownload()
        }
    }

    private fun startDownload(entity: SongDownloadEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            _currentDownloading.value = entity
            updateSongStatus(entity, DownloadStatus.DOWNLOADING)
            repository.downloadSong(
                songResponse = entity,
                onProgress = { progress ->
                    _songProgress.value = progress
                    updateSongProgress(entity, progress)
                    if(progress == 100){
                        updateSongStatus(entity, DownloadStatus.DOWNLOADED)
                    }
                }
            ) {
                _currentDownloading.value = null
                _songProgress.value = 0
                removeSongFromQueue(entity)
                updateSongStatus(entity, DownloadStatus.DOWNLOADED){
                    proceedToNextDownload()
                }
            }
        }
    }

    private fun proceedToNextDownload() {
        val nextSong = downloadQueue.firstOrNull()
        if (nextSong != null) {
            startDownload(nextSong)
        }
    }

    fun isAlreadyDownloaded(entity: SongDownloadEntity, onCallback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val isDownloaded = repository.isDownloaded(entity)
            if (isDownloaded) {
                updateSongStatus(entity, DownloadStatus.DOWNLOADED)
            } else {
                updateSongStatus(entity, DownloadStatus.NOT_DOWNLOADED)
            }
            onCallback(isDownloaded)
        }
    }

    private fun updateSongStatus(entity: SongDownloadEntity, status: DownloadStatus, onCallback: () -> Unit = {}) {
        val updatedStatusMap = _songDownloadStatus.value?.toMutableMap() ?: mutableMapOf()
        updatedStatusMap[entity.id] = status
        _songDownloadStatus.postValue(updatedStatusMap)
        saveQueueToDatabase()
        onCallback()
    }

    private fun updateSongProgress(entity: SongDownloadEntity, progress: Int) {
        viewModelScope.launch {
            downloadDao.updateProgress(entity.id, progress)
        }
    }

    private fun loadQueueFromDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val savedQueue = downloadDao.getAllDownloads()
            downloadQueue.clear()
            downloadQueue.addAll(savedQueue)
            savedQueue.forEach { song ->
                if (song.status == DownloadStatus.DOWNLOADING) {
                    _currentDownloading.value = song
                } else if (song.status == DownloadStatus.WAITING) {
                    downloadQueue.add(song)
                }
            }
        }
    }

    private fun saveQueueToDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            downloadDao.clearAll()
            downloadDao.insertAll(downloadQueue)
        }
    }

    private fun removeSongFromQueue(entity: SongDownloadEntity) {
        downloadQueue.remove(entity)
        saveQueueToDatabase()  // Persist the updated queue
    }

    private fun checkAndResumeDownloads() {
        viewModelScope.launch {
            networkConnectivityObserver.observe().collect { isConnected ->
                if (isConnected && currentDownloading.value == null) {
                    proceedToNextDownload()
                }
            }
        }
    }

    fun getSongStatus(id: String): DownloadStatus {
        return _songDownloadStatus.value?.get(id) ?: DownloadStatus.NOT_DOWNLOADED
    }
}

enum class DownloadStatus {
    NOT_DOWNLOADED,
    WAITING,
    DOWNLOADING,
    PAUSED,
    DOWNLOADED
}
