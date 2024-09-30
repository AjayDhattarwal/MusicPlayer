package com.ar.musicplayer.screens.player

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.components.mix.PlaylistSelectionSheet
import com.ar.musicplayer.components.player.AnimatedPager
import com.ar.musicplayer.components.player.CollapseBar
import com.ar.musicplayer.components.player.CollapsingBoxWithPadding
import com.ar.musicplayer.components.player.CollapsingImageLayout
import com.ar.musicplayer.components.player.ControlButton
import com.ar.musicplayer.components.player.LyricsCard
import com.ar.musicplayer.components.player.MiniPlayerControls
import com.ar.musicplayer.components.player.PlayPauseLargeButton
import com.ar.musicplayer.components.player.TrackSlider
import com.ar.musicplayer.components.player.convertToText
import com.ar.musicplayer.data.models.sanitizeString
import com.ar.musicplayer.navigation.currentFraction
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.download.DownloadEvent
import com.ar.musicplayer.utils.download.DownloadStatus
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.utils.helper.PaletteExtractor
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.viewmodel.ImportViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import kotlinx.collections.immutable.toPersistentList


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@UnstableApi
@Stable
@Composable
fun MusicPlayerScreen(
    playerViewModel: PlayerViewModel,
    localPlaylistViewModel: ImportViewModel,
    bottomSheetState: BottomSheetScaffoldState,
    onExpand: () -> Unit ,
    onCollapse: () -> Unit ,
    paletteExtractor: PaletteExtractor,
    downloaderViewModel: DownloaderViewModel,
    favoriteViewModel: FavoriteViewModel,
    preferencesManager: PreferencesManager,
    modifier: Modifier = Modifier,
){

    val currentSong by playerViewModel.currentSong.collectAsState()
    val playlist by playerViewModel.playlist.collectAsState(emptyList())
    val currentIndex by playerViewModel.currentIndex.collectAsState()

    val isPlaying = playerViewModel.isPlaying.collectAsState(false)
    val isBuffering by playerViewModel.isBuffering.collectAsState(false)

    val currentPosition = playerViewModel.currentPosition.collectAsState(0L)
    val duration by playerViewModel.duration.collectAsState(0L)

    val repeatMode by playerViewModel.repeatMode.observeAsState(Player.REPEAT_MODE_OFF)
    val shuffleModeEnabled by playerViewModel.shuffleModeEnabled.observeAsState(false)

    var showCurrentPlaylist by remember { mutableStateOf(false) }

    val title = remember(currentSong?.title){
        currentSong?.title?.sanitizeString().toString()
    }
    val artistsNames = remember(currentSong?.title){
        currentSong?.moreInfo?.artistMap?.artists
            ?.distinctBy { it.name }
            ?.joinToString(", ") { it.name.toString() }
            ?.sanitizeString()
            .toString()
    }

    val persistentList = remember{
        derivedStateOf{
            playlist.toPersistentList()
        }
    }

    val pagerState = rememberPagerState(pageCount = {persistentList.value.size})

    LaunchedEffect(playlist) {
        pagerState.scrollToPage(currentIndex)
    }

    LaunchedEffect(pagerState.currentPage) {
        if (currentIndex != pagerState.currentPage) {
            playerViewModel.changeSong(pagerState.currentPage)
        }
    }

    val isFavoriteFlow by remember {
        derivedStateOf {
            favoriteViewModel.isFavoriteSong(currentSong?.id.toString())
        }
    }
    val isFavourite = isFavoriteFlow.collectAsState(false)

    val colors = remember {
        mutableStateOf(arrayListOf<Color>(Color.Black,Color.Black))
    }


    LaunchedEffect(currentSong) {
        Log.d("launch", "called")
        currentSong?.image?.let {
            val shade = paletteExtractor.getColorFromImg(it)
            shade.observeForever { shadeColor ->
                shadeColor?.let { col ->
                    playerViewModel.setCurrentSongColor(col)
                    colors.value = arrayListOf(col, Color.Black)
                }
            }
        }
        if (currentIndex != pagerState.currentPage) {
            pagerState.scrollToPage(currentIndex)
        }
    }

    var isDownloaded by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    val downloadProgress by downloaderViewModel.songProgress.collectAsState()

    val songDownloadStatus by downloaderViewModel.songDownloadStatus.observeAsState(emptyMap())

    val status =
        songDownloadStatus[currentSong?.id.toString()] ?: DownloadStatus.NOT_DOWNLOADED

    var inDownloadQueue by remember { mutableStateOf(false) }

    LaunchedEffect(status) {
        when (status) {
            DownloadStatus.NOT_DOWNLOADED -> {
                isDownloaded = false
                inDownloadQueue = false
                isDownloading = false
            }
            DownloadStatus.WAITING -> {
                isDownloaded = false
                inDownloadQueue = true
                isDownloading = false
            }
            DownloadStatus.DOWNLOADING -> {
                isDownloaded = false
                inDownloadQueue = true
                isDownloading = true
            }
            DownloadStatus.DOWNLOADED -> {
                isDownloaded = true
                inDownloadQueue = false
                isDownloading = false
            }
            DownloadStatus.PAUSED -> {
                isDownloaded = false
                inDownloadQueue = true
                isDownloading = false
            }
        }
    }

    LaunchedEffect(key1 = currentSong?.id) {
        if((currentSong?.id ?: "") != ""){
            downloaderViewModel.onEvent(DownloadEvent.isDownloaded(currentSong!!) {
                isDownloaded = it
            })
        }
    }

    val state = rememberScrollState()


    val lazyListState = rememberLazyListState()
    val isLyricsLoading = playerViewModel.isLyricsLoading.collectAsState()
    val lyricsData = playerViewModel.lyricsData.collectAsState()
    val currentLyricIndex = playerViewModel.currentLyricIndex.collectAsState()


    val localPlaylist by localPlaylistViewModel.localPlaylists.collectAsState()
    var isSelectPlaylist by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(
                state = state,
            )
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors.value.toList()
                    )
                )
            }
            .clickable {
                onExpand()
            },
        verticalArrangement = Arrangement.Top
    ) {
        CollapseBar(
            fraction = {
                bottomSheetState.currentFraction
            },
            isFavorite = isFavourite,
            onCollapse = onCollapse,
            onFavClick = remember{
                {
                    currentSong.let {
                        favoriteViewModel.onEvent(
                            FavoriteSongEvent.ToggleFavSong(it!!)
                        )
                    }
                }
            },
            addToPlaylist = remember{
                {
                    isSelectPlaylist = true
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(),
        ) {


            CollapsingImageLayout(
                collapseFractionProvider = {
                    bottomSheetState.currentFraction
                },
                modifier = Modifier
            ) {
                AnimatedPager(
                    pagerState = pagerState,
                    items = persistentList
                )
            }
            CollapsingBoxWithPadding(
                collapseFractionProvider = {
                    bottomSheetState.currentFraction
                },
                modifier = Modifier
            ){
                MiniPlayerControls(
                    fraction = {
                        bottomSheetState.currentFraction
                    },
                    title = title,
                    artistsNames = artistsNames,
                    isFavourite = isFavourite,
                    isPlaying = isPlaying,
                    onFavClick = remember{
                        {
                            currentSong.let {
                                favoriteViewModel.onEvent(
                                    FavoriteSongEvent.ToggleFavSong(it!!)
                                )
                            }
                        }
                    },
                    onSkipNextClick = remember{
                        {
                            playerViewModel.skipNext()
                        }
                    },
                    onPlayPauseClick = remember{
                        {
                            playerViewModel.playPause()
                        }
                    },
                )
            }
        }

        val visibility by remember {
            derivedStateOf {
                bottomSheetState.currentFraction > 0.6f
            }
        }


        AnimatedVisibility(
            visible = visibility,
            enter = fadeIn(animationSpec = tween(1000)),
            exit = fadeOut(animationSpec = tween(1000))
        ) {
            Column {
                MaxPlayerTitle(
                    title = title,
                    artistsNames = artistsNames
                )
                TrackSlider(
                    value = currentPosition,
                    onValueChange = { newValue ->
                        playerViewModel.seekTo(newValue.toLong())
                    },
                    onValueChangeFinished = {
                    },
                    songDuration = duration.toFloat()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(Modifier.fillMaxWidth()) {
                    DurationText(
                        currentPosition = currentPosition,
                    )
                    Text(
                        text = duration.convertToText(),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp),
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold)
                    )
                }


                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp)
                ) {
                    ControlButton(
                        icon = Icons.Default.Shuffle,
                        size = 30.dp,
                        onClick = remember {
                            {
                                playerViewModel.toggleShuffleMode()
                            }
                        },
                        tint = if (shuffleModeEnabled) Color(preferencesManager.getAccentColor()) else Color.LightGray
                    )
                    Spacer(modifier = Modifier.width(20.dp))

                    ControlButton(
                        icon = Icons.Default.SkipPrevious,
                        size = 50.dp,
                        onClick = remember {
                            {
                                playerViewModel.skipPrevious()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    Box(contentAlignment = Alignment.Center) {
                        PlayPauseLargeButton(
                            isPlaying = isPlaying,
                            onPlayPauseClick = remember {
                                {
                                    playerViewModel.playPause()
                                }
                            }
                        )
                        if (isBuffering) {
                            CircularProgressIndicator()
                        }
                    }


                    Spacer(modifier = Modifier.width(10.dp))
                    ControlButton(
                        icon = Icons.Default.SkipNext,
                        size = 50.dp,
                        onClick = remember {
                            {
                                playerViewModel.skipNext()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    ControlButton(
                        icon = Icons.Default.Repeat,
                        size = 30.dp,
                        onClick = remember {
                            {
                                playerViewModel.setRepeatMode((repeatMode + 1) % 3)
                            }
                        },
                        tint = Color.LightGray
                    )
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    IconButton(
                        onClick = { showCurrentPlaylist = true }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "CurrentPlaylist",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = remember {
                            {
                                if (!isDownloaded) {
                                    downloaderViewModel.onEvent(
                                        DownloadEvent.downloadSong(
                                            currentSong!!
                                        )
                                    )
                                    inDownloadQueue = true
                                }
                            }
                        }
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                progress = { downloadProgress.div(100.toFloat()) ?: 0f },
                                modifier = Modifier,
                                color = Color.LightGray,
                            )
                            Text(
                                text = "${downloadProgress}%",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        } else {
                            Icon(
                                modifier = Modifier.weight(1f),
                                imageVector = if (isDownloaded) Icons.Default.DownloadDone else if (inDownloadQueue) Icons.Filled.HourglassTop else Icons.Default.FileDownload,
                                contentDescription = "Download",
                                tint = Color.White
                            )
                        }
                    }

                }

                Spacer(modifier = Modifier.height(10.dp))

                LyricsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    preferencesManager = preferencesManager,
                    colors = colors,
                    isLyricsLoading = isLyricsLoading,
                    lazyListState = lazyListState,
                    lyricsData = lyricsData,
                    currentLyricIndex = currentLyricIndex,
                    onLyricsClick = remember { {
                        playerViewModel.seekTo(it.toLong())
                    } }
                )

            }
        }



        if (showCurrentPlaylist) {
            ModalBottomSheet(
                onDismissRequest = { showCurrentPlaylist = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color.Black.copy(0.8f),
                dragHandle = {},
                contentWindowInsets = { WindowInsets(0,0,0,0) }
            ) {
                CurrPlayingPlaylist(playerViewModel = playerViewModel)
            }
        }

        if(isSelectPlaylist){
            ModalBottomSheet(
                sheetState =  rememberModalBottomSheetState(skipPartiallyExpanded = false),
                onDismissRequest = {
                    isSelectPlaylist = false
                }
            ){
                PlaylistSelectionSheet(
                    playlists = localPlaylist,
                    onPlaylistSelected = { selectedPlaylist ->
                        localPlaylistViewModel.addSongToPlaylist(currentSong!!, selectedPlaylist)
                        isSelectPlaylist = false
                    },
                    onCreatePlaylist = {
                        localPlaylistViewModel.createPlaylist(title = it, description = null)
                    }
                )
            }
        }

    }

}

@Composable
fun DurationText(currentPosition: State<Long>) {
    Text(
        text = currentPosition.value.convertToText(),
        modifier = Modifier
            .padding(start = 8.dp),
        color = Color.White,
        style = TextStyle(fontWeight = FontWeight.Bold)
    )
}





@UnstableApi
@Composable
fun MaxPlayerTitle(
    title: String,
    artistsNames: String,
) {
    Column {
        Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp)) {
            Column(
                modifier = Modifier
                    .padding(
                        bottom = 20.dp,
                        start = 20.dp,
                        end = 20.dp
                    )
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = title,
                    modifier = Modifier.basicMarquee(
                        animationMode = MarqueeAnimationMode.Immediately,
                        repeatDelayMillis = 2000,
                        initialDelayMillis = 2000
                    ),
                    color = Color.White,
                    fontSize = 30.sp,
                    maxLines = 1
                )
                Text(
                    text = artistsNames ,
                    modifier = Modifier.basicMarquee(
                        animationMode = MarqueeAnimationMode.Immediately,
                        repeatDelayMillis = 2000,
                        initialDelayMillis = 2000
                    ),
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }




//
//            LyricsCard(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(400.dp),
//                background = color,
//                playerViewModel = playerViewModel
//            )
//
//            Spacer(Modifier.height(30.dp))
//        }
//    }
//
//    if (showCurrentPlaylist) {
//        ModalBottomSheet(
//            onDismissRequest = { showCurrentPlaylist = false },
//            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
//            containerColor = color,
//            dragHandle = {},
//            contentWindowInsets = { WindowInsets(0,0,0,0) }
//        ) {
//            CurrPlayingPlaylist(playerViewModel = playerViewModel)
        }
    }
}





    @Composable
fun getStatusBarHeight(): Dp {
    val view = LocalView.current
    val insets = ViewCompat.getRootWindowInsets(view)
    val statusBarHeightPx = insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
    val density = LocalDensity.current

    return with(density) { statusBarHeightPx.toDp() }
}

