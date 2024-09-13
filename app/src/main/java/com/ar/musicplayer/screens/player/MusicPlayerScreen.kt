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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.components.player.AnimatedHorizontalPager
import com.ar.musicplayer.components.player.CollapseBar
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
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@UnstableApi
@Stable
@Composable
fun MusicPlayerScreen(
    playerViewModel: PlayerViewModel,
    bottomSheetState: BottomSheetScaffoldState,
    onExpand: () -> Unit ,
    onCollapse: () -> Unit ,
    paletteExtractor: PaletteExtractor,
    downloaderViewModel: DownloaderViewModel,
    favoriteViewModel: FavoriteViewModel,
    modifier: Modifier = Modifier
){
    val context = LocalContext.current
    Log.d("recomposeM", " recompose called for MusicPlayerScreen Function")


    val currentSong by playerViewModel.currentSong.collectAsState()

    

    val songName = currentSong?.title.toString().sanitizeString()
    val artistsNames = currentSong?.moreInfo?.artistMap?.artists
        ?.distinctBy { it.name }
        ?.joinToString(", ") { it.name.toString() }
        ?.sanitizeString()



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
    }

    val state = rememberScrollState()


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
        currentSong?.let {
            CollapseBar(
                bottomSheetState = bottomSheetState,
                favoriteViewModel = favoriteViewModel,
                playerViewModel = playerViewModel,
                currentSong = it,
                onCollapse = onCollapse
            )
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            AnimatedHorizontalPager(
                playerViewModel = playerViewModel,
                modifier = Modifier,
                bottomSheetState = bottomSheetState,
            )

            MiniPlayerControls(
                bottomSheetState = bottomSheetState,
                playerViewModel = playerViewModel,
                favoriteViewModel = favoriteViewModel,
                songName = songName,
                artistsNames = artistsNames.toString(),
            )

        }
        
        MaxPlayerControls(
            songName = songName,
            artistsNames = artistsNames.toString(),
            playerViewModel = playerViewModel,
            downloaderViewModel = downloaderViewModel,
            color = colors.value[0],
            bottomSheetState = bottomSheetState
        )

    }

}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@UnstableApi
@Composable
fun MaxPlayerControls(
    songName: String,
    artistsNames: String,
    playerViewModel: PlayerViewModel,
    downloaderViewModel: DownloaderViewModel,
    color: Color,
    bottomSheetState: BottomSheetScaffoldState
){
    val context = LocalContext.current
    
    val isPlaying by playerViewModel.isPlaying.collectAsState(false)
    val isBuffering by playerViewModel.isBuffering.collectAsState(false)

    val currentPosition by playerViewModel.currentPosition.collectAsState(0L)
    val duration by playerViewModel.duration.collectAsState(0L)

    val repeatMode by playerViewModel.repeatMode.observeAsState(Player.REPEAT_MODE_OFF)
    val shuffleModeEnabled by playerViewModel.shuffleModeEnabled.observeAsState(false)

    val currentSong by playerViewModel.currentSong.collectAsState()

    var isDownloaded by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }

    val downloadProgress by downloaderViewModel.songProgress.collectAsState()
    val currentDownloading by downloaderViewModel.currentDownloading.collectAsState()
    val songDownloadStatus by downloaderViewModel.songDownloadStatus.observeAsState()

    var inDownloadQueue by remember { mutableStateOf(false) }

    var showCurrentPlaylist by remember { mutableStateOf(false) }

    val preferencesManager = remember {
        PreferencesManager(context)
    }

    val visibility by remember {
        derivedStateOf {
            bottomSheetState.currentFraction > 0.6f
        }
    }

    LaunchedEffect( key1 = currentDownloading, key2 = isDownloading) {
        val status = downloaderViewModel.getSongStatus(currentSong?.id ?: "")
        when(status){
            DownloadStatus.NOT_DOWNLOADED -> isDownloaded = false
            DownloadStatus.WAITING -> inDownloadQueue = true
            DownloadStatus.DOWNLOADING -> isDownloading = true
            DownloadStatus.DOWNLOADED -> isDownloaded = true
            DownloadStatus.PAUSED -> isDownloading = true
        }
    }

    
    AnimatedVisibility(
        visible = visibility,
        enter = fadeIn(animationSpec = tween(1000)),
        exit = fadeOut(animationSpec = tween(1000))
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
                    value = currentPosition.toFloat(),
                    onValueChange = { newValue ->
                        playerViewModel.seekTo(newValue.toLong())
                    },
                    onValueChangeFinished = {
                    },
                    bufferedProgress = currentPosition.toFloat() + 3f,
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

                    Box(contentAlignment = Alignment.Center){
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
                                progress = { downloadProgress.div(100.toFloat()) ?: 0f },
                                modifier = Modifier,
                                color = Color.LightGray,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                background = color,
            )

            Spacer(Modifier.height(30.dp))
        }
    }

    if (showCurrentPlaylist) {
        ModalBottomSheet(
            onDismissRequest = { showCurrentPlaylist = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = color,
            dragHandle = {},
            contentWindowInsets = { WindowInsets(0,0,0,0) }
        ) {
            CurrPlayingPlaylist(playerViewModel = playerViewModel)
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

