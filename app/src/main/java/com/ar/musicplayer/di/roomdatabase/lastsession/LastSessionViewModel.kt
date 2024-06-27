package com.ar.musicplayer.di.roomdatabase.lastsession

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.di.roomdatabase.dbmodels.LastSessionDataEntity
import com.ar.musicplayer.models.SongResponse
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LastSessionViewModel @Inject constructor(
    private val lastSessionDao: LastSessionDao
) : ViewModel(){

    val lastSession = MutableLiveData<List<SongResponse>>()
    val listeningHistory: LiveData<List<SongResponse>> =
        lastSessionDao.getHistory().map { lastSessionDataEntities ->
            lastSessionDataEntities.map { lastSession ->
                Gson().fromJson(lastSession.lastSession, SongResponse::class.java)
            }
        }.asLiveData()

    fun onEvent(event: LastSessionEvent){
        when(event){
            is LastSessionEvent.LoadLastSessionData -> loadLastSession()
            is LastSessionEvent.InsertLastPlayedData -> insertLastSession(event.songResponse)
            is LastSessionEvent.DeleteHistoryById -> deleteLastSession(event.id)
            is LastSessionEvent.DeleteAll -> deleteAllLastSession()
        }
    }


    private fun deleteAllLastSession() {
        viewModelScope.launch {
            lastSessionDao.deleteAllSongs()
        }
    }

    private fun deleteLastSession(id: Int) {
        viewModelScope.launch {
            lastSessionDao.deleteLastSession(id)
        }
    }

    private fun insertLastSession(songResponse: SongResponse) {
        viewModelScope.launch {
            val lastSessionDataEntity = LastSessionDataEntity(lastSession = Gson().toJson(songResponse))
            lastSessionDao.insertLastSession(lastSessionDataEntity)
        }
    }

    fun loadLastSession() {
        viewModelScope.launch {
            val lastSessionDataEntities = withContext(Dispatchers.IO) {
                lastSessionDao.getLastSession()
            }
            var list : List<SongResponse> = emptyList()
            if(lastSessionDataEntities != null){
                lastSessionDataEntities.forEach {
                    list = list + it.toSongResponse()
                    Log.d("lastSession"," ${ list.size }")

                }
            }
            lastSession.value = list.reversed()
            Log.d("lastSession","${lastSessionDataEntities.size},, ${lastSession.value}")
        }
    }

    private fun LastSessionDataEntity.toSongResponse(): SongResponse {
        return Gson().fromJson(this.lastSession, SongResponse::class.java)
    }


}