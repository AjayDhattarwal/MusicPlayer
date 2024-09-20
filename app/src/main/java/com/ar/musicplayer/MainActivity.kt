package com.ar.musicplayer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.utils.permission.PermissionHandler
import com.ar.musicplayer.utils.permission.PermissionModel
import com.ar.musicplayer.navigation.App
import com.ar.musicplayer.ui.theme.AppTheme
import com.ar.musicplayer.ui.WindowInfoVM
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.utils.notification.AudioService
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel

import com.ar.musicplayer.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
@UnstableApi
class MainActivity  : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, AudioService::class.java)
        startService(intent)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

            val homeViewModel = hiltViewModel<HomeViewModel>()
            val playerViewModel = hiltViewModel<PlayerViewModel>()
            val downloaderViewModel = hiltViewModel<DownloaderViewModel>()
            val windowInfoVm = viewModel<WindowInfoVM>()
            val favoriteViewModel = hiltViewModel<FavoriteViewModel>()

            windowInfoVm.updateWindowWidthSizeClass(windowSizeClass.windowWidthSizeClass)
            windowInfoVm.updateWindowHeightSizeClass(windowSizeClass.windowHeightSizeClass)

            AppTheme {
                PermissionHandler(
                    permissions = listOf(
                        PermissionModel(
                            permission = "android.permission.POST_NOTIFICATIONS",
                            maxSDKVersion = Int.MAX_VALUE,
                            minSDKVersion = 31,
                            rational = "Access to Notification is required"
                        ),
                        PermissionModel(
                            permission = "android.permission.RECORD_AUDIO",
                            maxSDKVersion = Int.MAX_VALUE,
                            minSDKVersion = 33,
                            rational = "To Access Ai Functionality"
                        )
                    ),
                    askPermission = true
                )

                App(
                    homeViewModel = homeViewModel,
                    playerViewModel = playerViewModel,
                    downloaderViewModel = downloaderViewModel,
                    windowInfoVm = windowInfoVm,
                    favoriteViewModel = favoriteViewModel
                )
            }

//            PhoneAuthScreen( activity = this)
        }


    }


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