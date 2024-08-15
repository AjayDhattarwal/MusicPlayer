package com.ar.musicplayer.utils.notification

import android.app.PendingIntent
import android.content.Context
import androidx.media3.ui.PlayerNotificationManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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
        val request = ImageRequest.Builder(context)
            .data(player.mediaMetadata.artworkUri)
            .size(Size.ORIGINAL) // You can specify the size as needed
            .target(
                onSuccess = { result ->
                    val bitmap = (result as BitmapDrawable).bitmap
                    callback.onBitmap(bitmap)
                },
                onError = { placeholder: Drawable? ->
                    // Handle error state if necessary
                }
            )
            .build()

        context.imageLoader.enqueue(request)

        return null
    }
}
