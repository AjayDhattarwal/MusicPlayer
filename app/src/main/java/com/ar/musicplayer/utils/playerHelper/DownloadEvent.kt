package com.ar.musicplayer.utils.playerHelper

import com.ar.musicplayer.models.SongResponse
import retrofit2.Callback

sealed interface DownloadEvent {
    data class downloadSong(val songResponse: SongResponse) : DownloadEvent
    data class isDownloaded(val songResponse: SongResponse, val onCallback: (Boolean) -> Unit) :DownloadEvent
}