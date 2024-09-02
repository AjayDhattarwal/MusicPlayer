package com.ar.musicplayer.screens.info


import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.draw.drawBehind
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.components.CircularProgress
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.components.info.SongListWithTopBar
import com.ar.musicplayer.data.models.InfoScreenModel
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.utils.helper.PaletteExtractor
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.viewmodel.MoreInfoViewModel

private const val TAG: String = "PlayListDetail"

@OptIn(UnstableApi::class)
@ExperimentalFoundationApi
@Composable
fun InfoScreen(
    data: InfoScreenModel,
    onBackPressed: () -> Unit,
) {

    val moreInfoViewModel = hiltViewModel<MoreInfoViewModel>()
    val playlistResponse by moreInfoViewModel.playlistData.observeAsState()
    val isLoading by moreInfoViewModel.isLoading.collectAsStateWithLifecycle()
    val scrollState = rememberLazyListState()

    LaunchedEffect(data.image) {
        moreInfoViewModel.fetchPlaylistData(
            data.token,
            data.type,
            data.songCount,
            "1",
            "webapi.get"
        )
    }


    val colors = remember {
        mutableStateOf(arrayListOf<Color>(Color.Black, Color.Black))
    }
    val paletteExtractor = remember {
        PaletteExtractor()
    }

    data.image.let {
        val shade = paletteExtractor.getColorFromSwatch(it)
        shade.observeForever { shadeColor ->
            shadeColor?.let { col ->
                colors.value = arrayListOf(col, Color.Black)
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors.value.toList(),
                        endY = 850f
                    )
                )
            }
    ) {
        if(isLoading){
            CircularProgress(background = Color.Transparent)
        } else{
            val favViewModel = hiltViewModel<FavoriteViewModel>()
            val downloaderViewModel = hiltViewModel<DownloaderViewModel>()
            val playerViewModel = hiltViewModel<PlayerViewModel>()
            AnimatedVisibility(
                visible = playlistResponse?.list?.isNotEmpty() == true,
                enter = fadeIn()
            ) {
                SongListWithTopBar(
                    mainImage  = data.image,
                    scrollState = scrollState,
                    color = colors.value[0],
                    subtitle =
                    if(playlistResponse?.subtitle != "" && data.type == "playlist"){
                        playlistResponse?.subtitle.toString()
                    } else "Artists: "+ playlistResponse?.subtitle,
                    data = playlistResponse,
                    favViewModel = favViewModel,
                    downloaderViewModel = downloaderViewModel,
                    playerViewModel = playerViewModel,
                    onFollowClicked = { },
                    onBackPressed = onBackPressed

                )

            }
        }
    }
}
