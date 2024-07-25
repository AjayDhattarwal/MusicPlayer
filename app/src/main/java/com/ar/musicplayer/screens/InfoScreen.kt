package com.ar.musicplayer.screens


import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ar.musicplayer.models.HomeListItem
import com.ar.musicplayer.viewmodel.ApiCallViewModel
import com.ar.musicplayer.viewmodel.ImageColorGradient
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.ar.musicplayer.components.info.SongListWithTopBar
import com.ar.musicplayer.components.info.TopOver
import com.ar.musicplayer.components.mix.AnimatedToolBar
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.ui.theme.DarkBlackThemeColor
import com.ar.musicplayer.ui.theme.black
import com.ar.musicplayer.utils.helper.PaletteExtractor
import com.ar.musicplayer.utils.playerHelper.DownloaderViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel

private const val TAG: String = "PlayListDetail"

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterialApi::class)
@ExperimentalFoundationApi
@Composable
fun InfoScreen(
    homeListItem: HomeListItem,
    playerViewModel: PlayerViewModel,
    context: Context,
    favViewModel: FavoriteViewModel,
    downloaderViewModel: DownloaderViewModel,
    imageColorViewModel: ImageColorGradient,
    onBackPressed: () -> Unit,
) {

    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val playlistId by playerViewModel.playlistId.collectAsState()
    val apiCallViewModel: ApiCallViewModel = viewModel()

    val moreInfo = homeListItem.moreInfoHomeList
    val artistMap = moreInfo?.artistMap
    var allArtist = homeListItem.subtitle
    if (artistMap != null && artistMap.artists?.isNotEmpty() == true) {
        val artists = artistMap.artists.mapNotNull { it.name }
        allArtist = "Artists: " + artists.joinToString(", ").takeIf { it.isNotBlank() }
    }

    LaunchedEffect(homeListItem.image) {
        val totalSongs = homeListItem.count ?: moreInfo?.songCount
        Log.d("total","$totalSongs")
        Log.d("item","${homeListItem.moreInfoHomeList}")
        apiCallViewModel.getApiData(
            homeListItem.permaUrl?.substringAfterLast('/') ?: "",
            homeListItem.type?:"song",
            totalSongs.toString(),
            "1",
            "webapi.get"
        )

    }

    var isAllReadyPlaying by remember {
        mutableStateOf(false)
    }
    val playlistResponse by apiCallViewModel.apiLiveData.observeAsState()

    val colors = remember {
        mutableStateOf(arrayListOf<Color>(black, DarkBlackThemeColor, black))
    }
    val paletteExtractor = PaletteExtractor()

    homeListItem.image?.let {
        val shade = paletteExtractor.getColorFromSwatch(it)
        shade.observeForever { shadeColor ->
            shadeColor?.let { col ->
                colors.value = arrayListOf(col, DarkBlackThemeColor)
            }

        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors.value.toList(),
                    endY = 1000f
                )
            )

    ) {

        val scrollState = rememberLazyListState()

        SongListWithTopBar(
            mainImage  = homeListItem.image.toString(),
            scrollState = scrollState,
            color = colors.value[0],
            subtitle = allArtist.toString(),
            data = playlistResponse,
            favViewModel = favViewModel,
            downloaderViewModel = downloaderViewModel,
            imageColorViewModel =  imageColorViewModel,
            onFollowClicked = { },
            isPlaying =  if (playlistId == playlistResponse?.id)  isPlaying  else false ,
            onPlayPause = {
                if (!isAllReadyPlaying) {
                    playlistResponse?.list?.let { it1 ->
                        playerViewModel.playPlaylist(
                            it1,
                            playlistResponse?.id ?: ""
                        )
                    }
                    isAllReadyPlaying = true
                    playerViewModel.starter.value = false
                    playerViewModel.play()
                    playerViewModel.isPlayingHistory.value = false
                }
                if (playlistId == playlistResponse?.id) {
                    if (isPlaying) {
                        playerViewModel.pause()
                    } else {
                        playerViewModel.play()
                    }
                }
            },
            onSongClicked = { index ->
                playerViewModel.starter.value = false
                playlistResponse?.list?.get(index)?.image?.let {
                    imageColorViewModel.loadImage(
                        it,
                        context
                    )
                }
                playerViewModel.updateCurrentSong(
                    playlistResponse?.list!![index]
                )
                playerViewModel.isPlayingHistory.value = false
            },
            onBackPressed = {onBackPressed()}
        )
    }
}
