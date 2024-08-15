package com.ar.musicplayer.screens.player

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.components.PlayerDropDownMenu
import com.ar.musicplayer.components.player.AnimatedHorizontalPager
import com.ar.musicplayer.components.player.ControlButton
import com.ar.musicplayer.components.player.FavToggleButton
import com.ar.musicplayer.components.player.NextPlaylist
import com.ar.musicplayer.components.player.PlayPauseButton
import com.ar.musicplayer.components.player.PlayPauseLargeButton
import com.ar.musicplayer.components.player.SkipNextButton
import com.ar.musicplayer.components.player.TrackSlider
import com.ar.musicplayer.components.player.convertToText
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.navigation.currentFraction
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.download.DownloadEvent
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.utils.helper.PaletteExtractor
import com.ar.musicplayer.viewmodel.PlayerViewModel


@UnstableApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MusicPlayerScreen(
    playerViewModel: PlayerViewModel,
    bottomSheetState: BottomSheetScaffoldState,
    upNextSheetState: BottomSheetScaffoldState,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    upNextLazyListState: LazyListState,
    paletteExtractor: PaletteExtractor,
    downloaderViewModel: DownloaderViewModel,
    preferencesManager: PreferencesManager
) {
    val context = LocalContext.current
    Log.d("recompose", " recompose called for MusicPlayerScreen Function")
    val coroutineScope = rememberCoroutineScope()

    val favourite = hiltViewModel<FavoriteViewModel>()

    val isPlaying by playerViewModel.isPlaying.observeAsState(false)
    val currentSong by playerViewModel.currentSong.observeAsState()
    val currentPosition by playerViewModel.currentPosition.observeAsState(0L)
    val duration by playerViewModel.duration.observeAsState(0L)

    val repeatMode by playerViewModel.repeatMode.observeAsState(Player.REPEAT_MODE_OFF)
    val shuffleModeEnabled by playerViewModel.shuffleModeEnabled.observeAsState(false)

    val playlist by playerViewModel.playlist.observeAsState(emptyList())
    val currentIndex by playerViewModel.currentIndex.observeAsState(0)

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

    val songName = currentSong?.title.toString().replace("&quot;", "").replace("&amp;", ",")
    val artistsNames = currentSong?.moreInfo?.artistMap?.artists?.distinctBy { it.name }?.joinToString(", ") { it.name.toString() }


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

    LaunchedEffect(upNextSheetState.bottomSheetState.isCollapsed) {
        upNextLazyListState.scrollToItem(currentIndex)
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


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .clickable { onExpand() }
    ) {

        BottomSheetScaffold(
            scaffoldState = upNextSheetState,
            sheetPeekHeight = 40.dp,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            sheetContent = {
                Log.d("composed","inside sheet composed  ")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(top = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {

                        Column(Modifier.wrapContentHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Surface(
                                modifier = Modifier,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = MaterialTheme.shapes.extraLarge
                            ) {
                                Box(
                                    Modifier
                                        .size(
                                            width = 32.dp,
                                            height = 4.dp
                                        )
                                )
                            }
                            Text(
                                text = "Up Next",
                                fontSize = 22.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                    }
                    LazyColumn(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxSize(),
                        state = upNextLazyListState,
                    ) {
                        itemsIndexed(playlist){ index, item ->
                            NextPlaylist(
                                songResponse = item,
                                isPlaying = isPlaying,
                                currentSong = {
                                    currentSong
                                },
                                onClick = { playerViewModel.changeSong(index) }
                            )

                        }
                    }
                }

            },
            sheetBackgroundColor = Color.Black.copy(0.9f),
            backgroundColor = Color.Transparent,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors.value.toList()
                            )
                        )
                    },
                verticalArrangement = Arrangement.Top
            ) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dynamicPaddingValues.coerceAtLeast(1.dp))
                        .height(sizeofCollapseBar),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { onCollapse() }, modifier = Modifier) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {


                    AnimatedHorizontalPager(
                        pagerState = pagerState,
                        modifier = Modifier,
                        bottomSheetState = bottomSheetState,
                        playlist = playlist,
                    )

                    AnimatedVisibility(
                        visible = miniPlayerVisibility,
//                        true,
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
                                top = 10.dp,
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


//