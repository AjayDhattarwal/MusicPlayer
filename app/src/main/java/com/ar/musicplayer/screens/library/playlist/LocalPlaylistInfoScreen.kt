package com.ar.musicplayer.screens.library.playlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ar.musicplayer.components.info.SongListWithTopBar
import com.ar.musicplayer.data.models.PlaylistResponse
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.utils.helper.PaletteExtractor
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.viewmodel.ImportViewModel
import com.ar.musicplayer.viewmodel.MoreInfoViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel

@Composable
fun LocalPlaylistInfoScreen(
    scrollState: LazyListState = rememberLazyListState(),
    playlistResponse: PlaylistResponse,
    playerViewModel: PlayerViewModel,
    favViewModel: FavoriteViewModel,
    downloaderViewModel: DownloaderViewModel,
    importViewModel: ImportViewModel,
    onBackPressed: () -> Unit,
){
    val image = playlistResponse.image.toString()
    val colors = remember {
        mutableStateOf(arrayListOf<Color>(Color.Black, Color.Black))
    }
    val paletteExtractor = remember {
        PaletteExtractor()
    }

    image.let {
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
        SongListWithTopBar(
            mainImage = image,
            scrollState = scrollState,
            color = colors.value[0],
            subtitle = playlistResponse.subtitle.toString(),
            data = playlistResponse,
            favViewModel = favViewModel,
            downloaderViewModel = downloaderViewModel,
            playerViewModel = playerViewModel,
            onFollowClicked = { TODO() },
            onBackPressed = onBackPressed,
            onShare = { TODO() },
            localPlaylistViewModel = importViewModel,
        )
    }
}