package com.ar.musicplayer.screens.player

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.components.PlayerDropDownMenu
import com.ar.musicplayer.components.player.AnimatedHorizontalPager
import com.ar.musicplayer.components.player.ControlButton
import com.ar.musicplayer.components.player.FavToggleButton
import com.ar.musicplayer.components.player.LyricsCard
import com.ar.musicplayer.components.player.PlayPauseButton
import com.ar.musicplayer.components.player.PlayPauseLargeButton
import com.ar.musicplayer.components.player.SkipNextButton
import com.ar.musicplayer.components.player.TrackSlider
import com.ar.musicplayer.components.player.convertToText
import com.ar.musicplayer.data.models.perfect
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.navigation.currentFraction
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.download.DownloadEvent
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.utils.helper.PaletteExtractor
import com.ar.musicplayer.viewmodel.PlayerViewModel


@UnstableApi
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    playerViewModel: PlayerViewModel,
    bottomSheetState: BottomSheetScaffoldState,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    paletteExtractor: PaletteExtractor,
    downloaderViewModel: DownloaderViewModel,
    preferencesManager: PreferencesManager,
) {
    val context = LocalContext.current
    Log.d("recompose", " recompose called for MusicPlayerScreen Function")
    val coroutineScope = rememberCoroutineScope()

    val favourite = hiltViewModel<FavoriteViewModel>()

    val isPlaying by playerViewModel.isPlaying.collectAsState(false)
    val currentSong by playerViewModel.currentSong.collectAsState()
    val currentPosition by playerViewModel.currentPosition.collectAsState(0L)
    val duration by playerViewModel.duration.collectAsState(0L)
    val lyricsData by playerViewModel.lyricsData.collectAsState()
    val currentLyricIndex = playerViewModel.currentLyricIndex.observeAsState(0)
    val isLyricsLoading by playerViewModel.isLyricsLoading.collectAsStateWithLifecycle()

    val repeatMode by playerViewModel.repeatMode.observeAsState(Player.REPEAT_MODE_OFF)
    val shuffleModeEnabled by playerViewModel.shuffleModeEnabled.observeAsState(false)

    val playlist by playerViewModel.playlist.collectAsState(emptyList())
    val currentIndex by playerViewModel.currentIndex.collectAsState(0)

    val isFavouriteFlow by remember {
        derivedStateOf {
            favourite.isFavoriteSong(currentSong?.id.toString())
        }
    }

    val isFavourite by isFavouriteFlow.collectAsState(false)

    var isDownloaded by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    val downloadProgress by downloaderViewModel.songProgress.observeAsState()
    val currentDownloading by downloaderViewModel.currentDownloading.observeAsState()
    var inDownloadQueue by remember { mutableStateOf(false) }
    var isMoreExpanded by remember { mutableStateOf(false) }

    var showCurrentPlaylist by remember { mutableStateOf(false) }

    LaunchedEffect(currentDownloading) {
        if(currentDownloading == currentSong && currentSong?.id != null){
            isDownloading =  true
            isDownloaded = true
            inDownloadQueue = false
        }
        if(currentDownloading == null && downloadProgress == 0 ){
            isDownloading = false
        }
        else if(currentDownloading != currentSong){
            isDownloading = false
        }
    }

    val songName = currentSong?.title.toString().perfect()
    val artistsNames = currentSong?.moreInfo?.artistMap?.artists
        ?.distinctBy { it.name }
        ?.joinToString(", ") { it.name.toString() }
        ?.perfect()


    val pagerState = rememberPagerState(pageCount = {playlist.size})
    val colors = remember {
        mutableStateOf(arrayListOf<Color>(Color.Black,Color.Black))
    }

    LaunchedEffect(playlist) {
        pagerState.scrollToPage(currentIndex)
    }


    LaunchedEffect(pagerState.currentPage) {
        if (currentIndex != pagerState.currentPage) {
            playerViewModel.changeSong(pagerState.currentPage)
        }
    }

    val miniPlayerVisibility by remember {
        derivedStateOf {
            bottomSheetState.currentFraction < 0.9f && playlist.isNotEmpty()
        }
    }


    LaunchedEffect(currentSong) {
        Log.d("launch", "called")
        currentSong?.image?.let {
            val shade = paletteExtractor.getColorFromSwatch(it)
            shade.observeForever { shadeColor ->
                shadeColor?.let { col ->
                    colors.value = arrayListOf(col, Color.Black)
                }
            }
        }
        if (currentIndex != pagerState.currentPage) {
            pagerState.scrollToPage(currentIndex)
        }
    }

    val sizeofCollapseBar by animateDpAsState(targetValue = androidx.compose.ui.unit.lerp(
        0.dp,
        30.dp,
        bottomSheetState.currentFraction
    ))
    val dynamicPaddingValues by animateDpAsState(targetValue = androidx.compose.ui.unit.lerp(
        0.dp,
        getStatusBarHeight(),
        bottomSheetState.currentFraction
    ))
    val state = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = state, enabled = bottomSheetState.bottomSheetState.isExpanded)
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dynamicPaddingValues.coerceAtLeast(1.dp))
                .height(sizeofCollapseBar),
        ) {
            AnimatedVisibility(
                visible = (bottomSheetState.currentFraction > 0.7f),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row {
                    IconButton(onClick = { onCollapse() }) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.weight(1f).height(1.dp))

                    FavToggleButton(
                        isFavorite = isFavourite,
                        onFavClick = remember {
                            {
                                if(currentSong?.id != ""){
                                    favourite.onEvent(FavoriteSongEvent.ToggleFavSong(songResponse = currentSong!!))
                                    playerViewModel.toggleFavourite()
                                }

                            }
                        }
                    )

                    IconButton(onClick = { isMoreExpanded = !isMoreExpanded }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                        PlayerDropDownMenu(
                            expended = isMoreExpanded,
                            onDismissRequest = {isMoreExpanded = false}
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {


            AnimatedHorizontalPager(
                pagerState = pagerState,
                modifier = Modifier,
                bottomSheetState = bottomSheetState,
                playlist = playlist
            )

            AnimatedVisibility(
                visible = miniPlayerVisibility,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Row() {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 20.dp)
                    ) {
                        songName.let {
                            Text(
//                                        text = if (waitForPlayer) "Loading..." else it.replace(
//                                            "&quot;",
//                                            ""
//                                        ),
                                text = it.replace("&quot;", ""),
                                fontSize = 16.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.basicMarquee(
                                    animationMode = MarqueeAnimationMode.Immediately,
                                    repeatDelayMillis = 2000,
                                    initialDelayMillis = 2000,
                                    iterations = Int.MAX_VALUE
                                )
                            )
                        }

                        artistsNames?.let {
                            Text(
//                                        text = if (waitForPlayer) "" else it.replace("&quot;", ""),
                                text = it.replace("&quot;", ""),
                                fontSize = 14.sp,
                                color = Color.LightGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.basicMarquee(
                                    animationMode = MarqueeAnimationMode.Immediately,
                                    repeatDelayMillis = 2000,
                                    initialDelayMillis = 2000
                                )
                            )
                        }
                    }
                    FavToggleButton(
                        isFavorite = isFavourite,
                        onFavClick = remember {
                            {
                                if(currentSong?.id != ""){
                                    favourite.onEvent(FavoriteSongEvent.ToggleFavSong(songResponse = currentSong!!))
                                    playerViewModel.toggleFavourite()
                                }
                            }
                        }
                    )

                    PlayPauseButton(
                        isPlaying = isPlaying,
                        onPlayPauseClick = remember {
                            {
                                playerViewModel.playPause()
                            }
                        }
                    )

                    SkipNextButton(
                        onSkipNext = remember {
                            { playerViewModel.skipNext() }
                        }
                    )

                }
            }
        }
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
                    text = songName,
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
                    text = artistsNames.toString(),
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
            TrackSlider(
                value =  2f,
                onValueChange = { newValue ->
                    playerViewModel.seekTo(newValue.toLong())
                },
//                            onValueChangeFinished = {
//                                currentPosition.longValue = sliderPosition.longValue
//                                player.seekTo(sliderPosition.longValue)
//                            },
                songDuration = duration.toFloat()
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {

                Text(
                    text = (currentPosition).convertToText(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    color = Color.White,
                    style = TextStyle(fontWeight = FontWeight.Bold)
                )

                Text(
                    text =  duration.convertToText(),
                    modifier = Modifier
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
                    onClick = remember{
                        {
                            playerViewModel.toggleShuffleMode()
                        }
                    },
                    tint = if(shuffleModeEnabled) Color(preferencesManager.getAccentColor())  else Color.LightGray
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

                PlayPauseLargeButton(
                    isPlaying = isPlaying,
                    onPlayPauseClick = remember {
                        {
                            playerViewModel.playPause()
                        }
                    }
                )


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
                    onClick = {showCurrentPlaylist = true}
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "CurrentPlaylist",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = remember{
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
                    if(isDownloading){
                        CircularProgressIndicator(
                            modifier = Modifier,
                            progress = downloadProgress?.div(100.toFloat()) ?: 0f,
                            color = Color.LightGray
                        )
                        Text(text = "${downloadProgress}%", color = Color.White, fontSize = 14.sp)
                    }

                    else{
                        Icon(
                            modifier = Modifier.weight(1f),
                            imageVector = if(isDownloaded) Icons.Default.DownloadDone else if (inDownloadQueue) Icons.Filled.HourglassTop else Icons.Default.FileDownload,
                            contentDescription = "Download",
                            tint = Color.White
                        )
                    }
                }

            }

        }

        Spacer(modifier = Modifier.height(10.dp))

        LyricsCard(
            lyricsData = lyricsData,
            currentLyricIndex = currentLyricIndex,
            isLyricsLoading = isLyricsLoading,
            modifier = Modifier.fillMaxWidth().height(400.dp),
            background = colors.value[0],
            lyricClicked = {
                playerViewModel.seekTo(it.toLong())
            }
        )

        Spacer(Modifier.height(30.dp))
        if (showCurrentPlaylist) {

            ModalBottomSheet(
                onDismissRequest = { showCurrentPlaylist = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = colors.value[0],
                dragHandle = {},
                windowInsets = WindowInsets(0,0,0,0)
            ) {
                CurrPlayingPlaylist()
            }
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

