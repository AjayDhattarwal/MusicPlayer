//package com.ar.musicplayer.utils.notification
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.support.v4.media.MediaMetadataCompat
//import androidx.core.app.NotificationCompat
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.session.MediaSession
//import androidx.media3.session.MediaSessionService
//import androidx.media3.session.MediaStyleNotificationHelper
//import com.ar.musicplayer.MainActivity
//import com.ar.musicplayer.PlayNow.Companion.CHANNEL_ID
//import com.ar.musicplayer.PlayNow.Companion.NOTIFICATION_ID
//import com.ar.musicplayer.R
//import javax.inject.Inject
//
//
//class NotificationManager @Inject constructor(
//    private val context: Context,
//) {
//
//    val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//
//    @UnstableApi
//    fun createNotification(mediaSession: MediaSession): Notification {
//
//        val openAppIntent = Intent(context, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//        }
//
//        val contentPendingIntent = PendingIntent.getActivity(
//            context,
//            0,
//            openAppIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        return NotificationCompat.Builder(context, CHANNEL_ID)
//            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            .setSmallIcon(R.drawable.ic_music_note_24)
//            .setContentIntent(contentPendingIntent)
//            .setStyle(
//                 MediaStyleNotificationHelper.MediaStyle(mediaSession)
//            )
//            .build()
//    }
//
//    @UnstableApi
//    fun updateMediaStyleNotification(mediaSession: MediaSession) {
//
//        val notification = createNotification(mediaSession)
//        notificationManager.notify(NOTIFICATION_ID, notification)
//    }
//
//
//
//
//
//}
//
//
