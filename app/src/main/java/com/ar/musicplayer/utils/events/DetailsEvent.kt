package com.ar.musicplayer.utils.events

import com.ar.musicplayer.data.models.SongResponse
import kotlinx.coroutines.flow.Flow

sealed interface DetailsEvent {
    data class getSongDetails(val id: String,val call: String,val callback: (SongResponse) -> Unit):DetailsEvent
}