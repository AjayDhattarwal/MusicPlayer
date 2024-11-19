package com.ar.musicplayer.screens.info


import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.drawBehind
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.components.CircularProgress
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.components.info.SongListWithTopBar
import com.ar.musicplayer.data.models.InfoScreenModel
import com.ar.musicplayer.data.models.toLargeImg
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.utils.helper.PaletteExtractor
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.viewmodel.ImportViewModel
import com.ar.musicplayer.viewmodel.MoreInfoViewModel


@OptIn(UnstableApi::class)
@ExperimentalFoundationApi
@Composable
fun InfoScreen(
    playerViewModel: PlayerViewModel,
    moreInfoViewModel: MoreInfoViewModel,
    favViewModel: FavoriteViewModel,
    downloaderViewModel: DownloaderViewModel,
    data: InfoScreenModel,
    importViewModel: ImportViewModel,
    onBackPressed: () -> Unit,
) {

    val playlistResponse by moreInfoViewModel.playlistData.observeAsState()
    val isLoading by moreInfoViewModel.isLoading.collectAsStateWithLifecycle()
    val scrollState = rememberLazyListState()

    LaunchedEffect(data.image) {
        moreInfoViewModel.fetchPlaylist(data)
        scrollState.scrollToItem(0)
    }


    val colors = remember {
        mutableStateOf(arrayListOf<Color>(Color.Black, Color.Black))
    }
    val paletteExtractor = remember {
        PaletteExtractor()
    }

    data.image.let {
        val shade = paletteExtractor.getColorFromImg(it)
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
            val image = data.image.toLargeImg()
            AnimatedVisibility(
                visible = playlistResponse?.list?.isNotEmpty() == true,
                enter = fadeIn()
            ) {
                SongListWithTopBar(
                    mainImage  = image,
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
                    localPlaylistViewModel = importViewModel,
                    onFollowClicked = { TODO() },
                    onBackPressed = onBackPressed,
                    onShare = { TODO() }
                )

            }
        }
    }
}



