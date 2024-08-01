package com.ar.musicplayer.di.roomdatabase.lastsession

import com.ar.musicplayer.models.SongResponse

sealed interface LastSessionEvent {
    data class InsertLastPlayedData(val songResponse: SongResponse,val playCount: Int,val skipCount: Int) : LastSessionEvent
    data class DeleteHistoryById(val id: Int) : LastSessionEvent
    object DeleteAll : LastSessionEvent
}