package com.ar.musicplayer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat

import androidx.core.view.WindowCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.compose.rememberNavController
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.di.roomdatabase.homescreendb.HomeRoomViewModel
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionViewModel
import com.ar.musicplayer.navigation.App
import com.ar.musicplayer.utils.MusicPlayer
import com.ar.musicplayer.utils.notification.MusicPlayerService
import com.ar.musicplayer.utils.playerHelper.DownloaderViewModel

import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.viewmodel.RadioStationViewModel
import com.google.accompanist.systemuicontroller.SystemUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.absoluteValue


@AndroidEntryPoint
@UnstableApi
class MainActivity : ComponentActivity() {


    private val viewModel: HomeViewModel by viewModels()
    val homeRoomViewModel: HomeRoomViewModel by viewModels()
    val lastSessionViewModel: LastSessionViewModel by viewModels()
    val favViewModel : FavoriteViewModel by viewModels()
    val radioStationViewModel: RadioStationViewModel by viewModels()
    val downloaderViewModel: DownloaderViewModel by viewModels()

    @Inject lateinit var musicPlayer: MusicPlayer
    @Inject lateinit var playerViewModel: PlayerViewModel
    @Inject lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)


        setContent {
            val navController = rememberNavController()

            App(
                navController = navController,
                homeViewModel = viewModel,
                playerViewModel = playerViewModel,
                homeRoomViewModel = homeRoomViewModel,
                lastSessionViewModel = lastSessionViewModel,
                favViewModel = favViewModel,
                radioStationViewModel = radioStationViewModel,
                downloaderViewModel = downloaderViewModel,
                musicPlayer = musicPlayer
            )
        }
        val intent = Intent(this, MusicPlayerService::class.java)
        startService(intent)

    }
    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, MusicPlayerService::class.java)
        stopService(intent)
        musicPlayer.release()
    }
}


