package com.ar.musicplayer.utils.notification

import android.app.PendingIntent
import android.content.Context
import androidx.media3.ui.PlayerNotificationManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import coil.ImageLoader
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Size
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@UnstableApi
class NotificationAdapter @Inject constructor(
    private val context: Context,
    private val pendingIntent: PendingIntent?
) : PlayerNotificationManager.MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): CharSequence =
        player.mediaMetadata.albumTitle ?: "Unknown"

    override fun createCurrentContentIntent(player: Player): PendingIntent? = pendingIntent

    override fun getCurrentContentText(player: Player): CharSequence =
        player.mediaMetadata.displayTitle ?: "Unknown"

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = fetchBitmapWithRetry(
                context = context,
                uri = player.mediaMetadata.artworkUri.toString()
            )

            if (bitmap != null) {
                callback.onBitmap(bitmap)
            } else {
                val fallbackBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                callback.onBitmap(fallbackBitmap)
            }
        }
        return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    }

    private suspend fun fetchBitmapWithRetry(
        context: Context,
        uri: String,
        maxRetries: Int = 3,
        retryDelayMillis: Long = 1000
    ): Bitmap? {
        var currentRetry = 0
        while (currentRetry < maxRetries) {
            try {
                val imageLoader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .size(Size.ORIGINAL)
                    .build()

                val result = withContext(Dispatchers.IO) {
                    imageLoader.execute(request)
                }

                return when (result) {
                    is SuccessResult -> {
                        (result.drawable as BitmapDrawable).bitmap
                    }
                    is ErrorResult -> {
                        null
                    }
                }
            } catch (e: Exception) {
                currentRetry++
                if (currentRetry >= maxRetries) {
                    return null
                }
                // Wait before retrying
                delay(retryDelayMillis)
            }
        }
        return null
    }
}
