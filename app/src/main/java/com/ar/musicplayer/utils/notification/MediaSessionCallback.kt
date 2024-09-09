package com.ar.musicplayer.utils.notification

import android.os.Bundle
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.ar.musicplayer.PlayNow.Companion.REMOVE_FROM_FAVORITES
import com.ar.musicplayer.PlayNow.Companion.SAVE_TO_FAVORITES
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import javax.inject.Inject

class MediaSessionCallback() : MediaSession.Callback {


    @UnstableApi
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {

        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
            .setAvailablePlayerCommands(
                MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                    .build()
            )
            .setAvailableSessionCommands(
                MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                    .add(SessionCommand(SAVE_TO_FAVORITES, Bundle.EMPTY))
                    .build()
            )
            .build()
    }


    @UnstableApi
    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        return when (customCommand.customAction) {
            SAVE_TO_FAVORITES -> {
                // Custom logic for saving to favorites
                saveToFavorites(session.player.currentMediaItem)
                return Futures.immediateFuture(
                    SessionResult(SessionResult.RESULT_SUCCESS)
                )
            }
            REMOVE_FROM_FAVORITES -> {
                // Custom logic for removing from favorites
                removeFromFavorites(session.player.currentMediaItem)

                return Futures.immediateFuture(
                    SessionResult(SessionResult.RESULT_SUCCESS)
                )
            }
            else ->
                return Futures.immediateFuture(
                    SessionResult(SessionError.ERROR_IO)
                )
        }
    }

    private fun saveToFavorites(currentMediaItem: MediaItem?) {
        Log.d("call", "setfav")
    }

    private fun removeFromFavorites(currentMediaItem: MediaItem?) {
        Log.d("call", "removeFav")
    }
}

