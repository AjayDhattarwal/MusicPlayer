package com.ar.musicplayer.di.roomdatabase.lastsession

import com.ar.musicplayer.models.SongResponse

sealed interface LastSessionEvent {
    object LoadLastSessionData : LastSessionEvent
    data class InsertLastPlayedData(val songResponse: SongResponse) : LastSessionEvent
    data class DeleteHistoryById(val id: Int) : LastSessionEvent
    object DeleteAll : LastSessionEvent
}