package com.ar.musicplayer

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.compose.rememberNavController
import com.ar.musicplayer.navigation.App
import com.ar.musicplayer.utils.notification.MusicPlayerService

import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
@UnstableApi
class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()
    val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            App(navController = navController, viewModel,playerViewModel)
        }
        val intent = Intent(this, MusicPlayerService::class.java)
        startService(intent)

    }
    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, MusicPlayerService::class.java)
        stopService(intent)
    }
}




//composable<ScreenC>(typeMap = mapOf(typeOf<SongItem>() to SongType )) {
//    val args = it.toRoute<ScreenC>()
//    val songItem = args.songItem
//    Column(modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        songItem.title?.let { it1 -> Text(text = it1) }
//    }
//}

//@Serializable
//data class ScreenC(
//    val songItem: SongItem
//)

//val SongType = object : NavType<SongItem>(
//    isNullableAllowed = false
//) {
//    override fun get(bundle: Bundle, key: String): SongItem? =
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            bundle.getParcelable(key, SongItem::class.java)
//        } else {
//            @Suppress("DEPRECATION") // for backwards compatibility
//            bundle.getParcelable(key)
//        }
//
//    override fun put(bundle: Bundle, key: String, value: SongItem) =
//        bundle.putParcelable(key, value)
//
//    override fun parseValue(value: String): SongItem = Json.decodeFromString(value)
//
//    override fun serializeAsValue(value: SongItem): String = Json.encodeToString(value)
//    override val name: String = "SongItem"
//}
