package com.ar.musicplayer.utils

import MusicPlayer
import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.viewmodel.PlayerViewModel

object MusicPlayerSingleton {
    private lateinit var instance: MusicPlayer

    fun initialize(context: Context, viewModel: PlayerViewModel, exoPlayer: ExoPlayer) {
        instance = MusicPlayer(context, viewModel, exoPlayer)
    }

    fun getInstance(): MusicPlayer {
        return instance
    }
}
