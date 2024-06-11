package com.ar.musicplayer.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
