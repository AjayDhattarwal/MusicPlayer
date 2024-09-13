@file:kotlin.OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalSharedTransitionApi::class
)

package com.ar.musicplayer.screens.player

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.components.player.ControlButton
import com.ar.musicplayer.components.player.PlayPauseLargeButton
import com.ar.musicplayer.components.player.SeDisplayName
import com.ar.musicplayer.components.player.SharedElementPager
import com.ar.musicplayer.components.player.TrackSlider
import com.ar.musicplayer.components.player.convertToText
import com.ar.musicplayer.data.models.sanitizeString
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.download.DownloadStatus
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.utils.helper.PaletteExtractor
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class, )
@Composable
fun AdaptiveMiniPlayer(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel,
    onCollapse: () -> Unit,
    paletteExtractor: PaletteExtractor,
    downloaderViewModel: DownloaderViewModel,
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
    animatedVisibilityScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope
){

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentSong by playerViewModel.currentSong.collectAsState()



    val songName = currentSong?.title.toString().sanitizeString()
    val artistsNames = currentSong?.moreInfo?.artistMap?.artists
        ?.distinctBy { it.name }
        ?.joinToString(", ") { it.name.toString() }
        ?.sanitizeString().toString()



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

    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val isBuffering by playerViewModel.isBuffering.collectAsState()
    val position by playerViewModel.currentPosition.collectAsState(0L)

    var sliderPosition by remember(position) {
        mutableLongStateOf(position)
    }
    val currentPosition by remember(sliderPosition) {
        derivedStateOf{
            sliderPosition
        }
    }


    val duration by playerViewModel.duration.collectAsState(0L)

    val repeatMode by playerViewModel.repeatMode.observeAsState(Player.REPEAT_MODE_OFF)
    val shuffleModeEnabled by playerViewModel.shuffleModeEnabled.observeAsState(false)


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
    var swipeEnded by remember { mutableStateOf(false) }

    LaunchedEffect(swipeEnded) {
        if (swipeEnded) {
            delay(200)
            swipeEnded = false
        }
    }

    with(sharedTransitionScope) {

        Row(
            modifier = modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors.value.toList()
                        )
                    )
                }
                .sharedBounds(
                    rememberSharedContentState(key = "bounds"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                )
                .height(100.dp)
                .swipeableGesture(
                    onSwipeLeft = { playerViewModel.skipNext() },
                    onSwipeRight = { playerViewModel.skipPrevious() }
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(90.dp)) {
                SharedElementPager(
                    playerViewModel = playerViewModel,
                    animatedVisibilityScope = animatedVisibilityScope,
                    sharedTransitionScope = sharedTransitionScope
                )
            }

            val width = LocalConfiguration.current.screenWidthDp / 2.7

            Row{
                SeDisplayName(
                    trackName = songName,
                    artistName = artistsNames,
                    animatedVisibilityScope = animatedVisibilityScope,
                    sharedTransitionScope = sharedTransitionScope,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(width.dp)
                )

            }


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
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
                    Spacer(modifier = Modifier.width(10.dp))

                    ControlButton(
                        icon = Icons.Default.SkipPrevious,
                        size = 40.dp,
                        onClick = remember {
                            {
                                playerViewModel.skipPrevious()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    Box(contentAlignment = Alignment.Center) {
                        PlayPauseLargeButton(
                            size = 50.dp,
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
                        size = 40.dp,
                        onClick = remember {
                            {
                                playerViewModel.skipNext()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
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
                    modifier = Modifier
                        .width(width.dp)
                        .padding(bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentPosition.convertToText(),
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .padding(start = 10.dp, end = 8.dp)
                    )

                    TrackSlider(
                        modifier = Modifier.weight(1f),
                        bufferedProgress = currentPosition.toFloat() + 10f,
                        value = currentPosition.toFloat(),
                        onValueChange = { newValue ->
                            sliderPosition = newValue.toLong()
                        },
                        onValueChangeFinished = {
                            playerViewModel.seekTo(sliderPosition)
                        },
                        songDuration = duration.toFloat()
                    )

                    Text(
                        text = duration.convertToText(),
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .padding(start = 8.dp, end = 10.dp)
                    )

                }

            }


            Box() {
                content()
            }
        }
    }






}


fun Modifier.swipeableGesture(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    threshold: Float = 20f
): Modifier = composed {
    pointerInput(Unit) {
        coroutineScope {
            awaitEachGesture {
                // Await the first down event
                val down = awaitFirstDown()

                // Await until user crosses touch slop in horizontal direction
                val drag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
                    change.consumePositionChange()
                }

                // If drag is null, the touch slop wasn't crossed
                if (drag != null) {
                    val velocityTracker = VelocityTracker()

                    // Track the drag distance
                    drag.consumeAllChanges()
                    velocityTracker.addPosition(drag.uptimeMillis, drag.position)

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == drag.id }
                        if (change == null || !change.pressed) break

                        val horizontalDragAmount = change.positionChange().x
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        change.consumePositionChange()

                        if (horizontalDragAmount != 0f) {
                            if (horizontalDragAmount > threshold) {
                                onSwipeRight()
                                break
                            } else if (horizontalDragAmount < -threshold) {
                                onSwipeLeft()
                                break
                            }
                        }
                    }
                }
            }
        }
    }
}