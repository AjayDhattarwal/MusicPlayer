package com.ar.musicplayer.utils.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import com.ar.musicplayer.R

@UnstableApi
class FavoriteActionReceiver(
    private val context: Context
) : PlayerNotificationManager.CustomActionReceiver {

    companion object {
        const val ACTION_FAVORITE = "ACTION_FAVORITE"
        const val EXTRA_FAVORITE = "EXTRA_FAVORITE"
    }

    override fun createCustomActions(context: Context, instanceId: Int): Map<String, NotificationCompat.Action> {
        val icon = R.drawable.ic_favorite // Replace with your favorite icon
        val title = context.getString(R.string.favorite)
        val intent = Intent(ACTION_FAVORITE).setPackage(context.packageName)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            instanceId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val action = NotificationCompat.Action(icon, title, pendingIntent)

        return mapOf(ACTION_FAVORITE to action)
    }

    override fun getCustomActions(player: Player): List<String> {
        return listOf(ACTION_FAVORITE)
    }

    override fun onCustomAction(player: Player, action: String, intent: Intent) {
        if (action == ACTION_FAVORITE) {

        }
    }
}
