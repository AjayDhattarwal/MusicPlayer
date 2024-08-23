package com.ar.musicplayer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.core.view.WindowCompat

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.utils.permission.PermissionHandler
import com.ar.musicplayer.utils.permission.PermissionModel
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.navigation.App
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.utils.notification.ACTIONS
import com.ar.musicplayer.utils.notification.AudioService

import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.viewmodel.RadioStationViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
@UnstableApi
class MainActivity  : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            val homeViewModel = hiltViewModel<HomeViewModel>()
            val playerViewModel = hiltViewModel<PlayerViewModel>()
            val downloaderViewModel = hiltViewModel<DownloaderViewModel>()
            val radioStationViewModel = viewModel<RadioStationViewModel>()
            val favViewModel = hiltViewModel<FavoriteViewModel>()

            PermissionHandler(
                permissions = listOf(
                    PermissionModel(
                        permission = "android.permission.POST_NOTIFICATIONS",
                        maxSDKVersion = Int.MAX_VALUE,
                        minSDKVersion = 33,
                        rational = "Access to Notification is required"
                    ),
                ),
                askPermission = true
            )

            App(
                navController = navController,
                homeViewModel = homeViewModel,
                playerViewModel = playerViewModel,
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



//
//Box(
//modifier = Modifier.fillMaxSize()
//.background(background)
//.blur(20.dp)
//){
//    AsyncImage(
//        model = "https://wallpaper.forfun.com/fetch/63/63da5cd7c95d494cb847c450dcbd1412.jpeg?download=music-ukulele-gitara-zakat-plyazh-sumerki-139741.jpeg",
//        contentDescription = "",
//        modifier = Modifier.fillMaxSize(),
//        contentScale = ContentScale.Crop
//    )
//}