package com.ar.musicplayer.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.utils.roomdatabase.dbmodels.LastSessionDataEntity
import com.ar.musicplayer.utils.roomdatabase.lastsession.LastSessionDao
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class LastSessionRepository @Inject constructor(
    private val lastSessionDao: LastSessionDao
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Convert to StateFlow
    val listeningHistory: StateFlow<List<Pair<Int?, SongResponse>>> =
        lastSessionDao.getHistory()
            .map { lastSessionDataEntities ->
                lastSessionDataEntities.map { history ->
                    history.id to Gson().fromJson(history.lastSession, SongResponse::class.java)
                }
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val lastSession: StateFlow<List<Pair<Int?, SongResponse>>> =
        lastSessionDao.getLastSession()
            .map { lastSessionDataEntities ->
                lastSessionDataEntities.map { history ->
                    history.id to Gson().fromJson(history.lastSession, SongResponse::class.java)
                }
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    suspend fun getLastSessionForPlaying(): List<Pair<Int?, SongResponse>> {
        return lastSessionDao.getLastSessionForPlaying().map { history ->
            history.id to Gson().fromJson(history.lastSession, SongResponse::class.java)
        }
    }

    suspend fun insertLastSession(songResponse: SongResponse, playCount: Int, skipCount: Int) {
        val lastSessionDataEntity = LastSessionDataEntity(
            lastSession = Gson().toJson(songResponse),
            title = songResponse.title.toString(),
            genres = "",
            playCount = playCount,
            skipCount = skipCount
        )
        lastSessionDao.insertLastSession(lastSessionDataEntity)
    }

    suspend fun deleteLastSession(id: Int) {
        lastSessionDao.deleteLastSession(id)
    }

    suspend fun deleteAllLastSession() {
        lastSessionDao.deleteAllSongs()
    }

}