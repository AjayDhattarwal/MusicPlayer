package com.ar.musicplayer.utils.playerHelper

import android.content.Context
import android.os.Environment
import com.ar.musicplayer.models.Song
import com.ar.musicplayer.models.SongResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.inject.Inject

class MusicDownloadRepository @Inject constructor(
    context: Context
){
    val context = context
    suspend fun downloadSong(
        songResponse: SongResponse,
        onProgress: (Int) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            handleMp4ToMp3Conversion(
                context = context,
                songResponse = songResponse,
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
