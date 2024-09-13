package com.ar.musicplayer.utils.download

import android.content.Context
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.sanitizeString
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.roomdatabase.dbmodels.SongDownloadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class MusicDownloadRepository @Inject constructor(
    context: Context
){
    val context = context
    val preferencesManager = PreferencesManager(context)
    val downloadQuality = preferencesManager.getDownloadQuality()
    val downloadPath  = preferencesManager.getDownloadLocation()
    suspend fun downloadSong(
        songResponse: SongDownloadEntity,
        onProgress: (Int) -> Unit,
        onCompleted: () -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            handleMp4ToMp3Conversion(
                context = context,
                entity = songResponse,
                downloadQuality = downloadQuality,
                downloadPath = downloadPath,
                onProgress = {
                    onProgress(it)
                },
                onComplete = {
                    onCompleted()
                })
        }
    }


    fun deleteSong(songResponse: SongResponse) {
        val artist = songResponse.moreInfo?.artistMap?.artists
            ?.distinctBy { it.name }
            ?.joinToString ( "," ) {it.name.toString()}
            ?.sanitizeString()
            .toString()
        val file = File(getFilePath(songResponse.title.toString(), artist))
        if (file.exists()) {
            file.delete()
        }
    }

    fun isDownloaded(downloadEntity: SongDownloadEntity): Boolean {
        val file = File(getFilePath(downloadEntity.title, downloadEntity.artist)).exists()
        return file
    }

    private fun getFilePath(title: String, artist: String): String {
        val musicFolderPath = downloadPath
        return "$musicFolderPath/${title} - ${artist}.mp3"
    }


}
