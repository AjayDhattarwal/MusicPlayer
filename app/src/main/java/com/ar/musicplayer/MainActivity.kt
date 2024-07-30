package com.ar.musicplayer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.ar.musicplayer.screens.player.PlayerViewModel
import com.ar.musicplayer.di.permission.PermissionHandler
import com.ar.musicplayer.di.permission.PermissionModel
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.di.roomdatabase.homescreendb.HomeRoomViewModel
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionViewModel
import com.ar.musicplayer.navigation.App
import com.ar.musicplayer.utils.playerHelper.DownloaderViewModel

import com.ar.musicplayer.screens.home.HomeViewModel
import com.ar.musicplayer.screens.home.RadioStationViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity  : ComponentActivity() {


    private val homeViewModel: HomeViewModel by viewModels()
    private val homeRoomViewModel: HomeRoomViewModel by viewModels()
    private val lastSessionViewModel: LastSessionViewModel by viewModels()
    private val favViewModel : FavoriteViewModel by viewModels()
    private val radioStationViewModel: RadioStationViewModel by viewModels()
    private val downloaderViewModel: DownloaderViewModel by viewModels()

    @Inject
    lateinit var playerViewModel: PlayerViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            PermissionHandler(
                permissions = listOf(
                    PermissionModel(
                        permission = "android.permission.POST_NOTIFICATIONS",
                        maxSDKVersion = Int.MAX_VALUE,
                        minSDKVersion = 33,
                        rational = "Access to Notification is required"
                    )
                ),
                askPermission = true
            )

            App(
                navController = navController,
                homeViewModel = homeViewModel,
                playerViewModel = playerViewModel,
                homeRoomViewModel = homeRoomViewModel,
                lastSessionViewModel = lastSessionViewModel,
                favViewModel = favViewModel,
                radioStationViewModel = radioStationViewModel,
                downloaderViewModel = downloaderViewModel
            )


        }

    }

}


fun Activity.openAppSettings(){
    Intent(
        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        android.net.Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}