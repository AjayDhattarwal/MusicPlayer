package com.ar.musicplayer.utils.download

import android.content.Context
import android.os.Environment
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.utils.PreferencesManager
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
        songResponse: SongResponse,
        onProgress: (Int) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            handleMp4ToMp3Conversion(
                context = context,
                songResponse = songResponse,
                downloadQuality = downloadQuality,
                downloadPath = downloadPath,
                onProgress = {
                    onProgress(it)
                },
                onComplete = {

                })
        }
    }

    fun deleteSong(songResponse: SongResponse) {
        val file = File(getFilePath(songResponse))
        if (file.exists()) {
            file.delete()
        }
    }

    fun isFileExist(songResponse: SongResponse, onFileExist: (Boolean) -> Unit) {
        val file = File(getFilePath(songResponse)).exists()
        onFileExist(file)
    }

    private fun getFilePath(songResponse: SongResponse): String {
        val musicFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath
        return "$musicFolderPath/${songResponse.title} - ${songResponse.moreInfo?.artistMap?.artists?.get(0)?.name}.mp3"
    }


}
