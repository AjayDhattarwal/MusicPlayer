package com.ar.musicplayer.utils.download

import com.ar.musicplayer.data.models.SongResponse

sealed interface DownloadEvent {
    data class downloadSong(val songResponse: SongResponse) : DownloadEvent
    data class isDownloaded(val songResponse: SongResponse, val onCallback: (Boolean) -> Unit) :DownloadEvent
}