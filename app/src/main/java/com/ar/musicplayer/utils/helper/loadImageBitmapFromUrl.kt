package com.ar.musicplayer.utils.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

suspend fun loadImageBitmapFromUrl(url: String,context: Context): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val imageLoader = ImageLoader.Builder(context)
                .build()

            val request = ImageRequest.Builder(context)
                .data(url)
                .build()

            val result = (imageLoader.execute(request) as SuccessResult).drawable
            return@withContext result.toBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

private fun getBitmapFromURL(src: String?): Bitmap? {
    return try {
        val url = URL(src)
        val connection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val input = connection.inputStream
        BitmapFactory.decodeStream(input)
    } catch (e: IOException) {
        e.printStackTrace()
        null

    }
}