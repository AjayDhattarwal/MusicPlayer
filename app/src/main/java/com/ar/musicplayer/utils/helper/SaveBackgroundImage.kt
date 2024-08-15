package com.ar.musicplayer.utils.helper

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

fun saveImageToInternalStorage(context: Context, bitmap: Bitmap,): String? {
    val file = File(context.filesDir, "backgroundImage")
    return try {
        val stream: OutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}