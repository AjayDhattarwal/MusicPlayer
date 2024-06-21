package com.ar.musicplayer.screens

import MusicPlayer
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.ar.musicplayer.components.ControlButton
import com.ar.musicplayer.components.TrackSlider
import com.ar.musicplayer.components.convertToText
import com.ar.musicplayer.viewmodel.ImageColorGradient
import com.ar.musicplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

@UnstableApi
@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlayerContentUi(
    playerViewModel: PlayerViewModel,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    bottomSheetState: BottomSheetScaffoldState,
    musicPlayer: MusicPlayer
) {
    val context = LocalContext.current
    val player = musicPlayer.getPlayer()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val imageColorGradientViewModel: ImageColorGradient = viewModel()
    val playlist by playerViewModel.playlist.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()

    val bitmap by imageColorGradientViewModel.bitmapState.collectAsState()
    bitmap?.let { playerViewModel.bitmapload(it) }
    DisposableEffect(Unit) {
        musicPlayer.onSongChanged = { song ->
            playerViewModel.updateCurrentSong(song)
            playerViewModel.play()
        }
        onDispose {
            musicPlayer.release()
        }
    }
    LaunchedEffect(playlist) {
        playlist.let {
            musicPlayer.playPlaylist(it)
        }
    }
    currentSong?.let { song ->
        LaunchedEffect(song) {
            musicPlayer.play(song)
        }
    }
    LaunchedEffect(currentSong){
        currentSong?.image?.let { imageColorGradientViewModel.loadImage(it,context) }
    }

    val verticalGradient = remember { mutableStateOf(
        Brush.verticalGradient(listOf(
            Color(0xFF0E0E0E),
            Color(0xFF000000),
        ))) }

    val perfectColor =  imageColorGradientViewModel.colorForBackGround.collectAsState()

    val showShimmer = remember { mutableStateOf(true) }



    val qualityImgUrl = currentSong?.image?.replace("150x150", "250x250")

    LaunchedEffect(perfectColor.value) {
        verticalGradient.value = Brush.verticalGradient(listOf(
            perfectColor.value ?: Color(0xFF161616), Color(0xFF0E0E0E),
            Color(0xFF000000)
        ))
        Log.d("color", "color is  ${perfectColor.value}")
    }



    val currentPosition = remember {
        mutableLongStateOf(0)
    }

    val sliderPosition = remember {
        mutableLongStateOf(0)
    }

    val totalDuration = remember {
        mutableLongStateOf(0)
    }

    LaunchedEffect(key1 = player.currentPosition, key2 = player.isPlaying) {
        delay(1000)
        currentPosition.longValue = player.currentPosition
    }

    LaunchedEffect(currentPosition.longValue) {
        sliderPosition.longValue = currentPosition.longValue
    }

    LaunchedEffect(player.duration) {
        if (player.duration > 0) {
            totalDuration.longValue = player.duration
        }
    }


    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = verticalGradient.value)
                .clickable { onExpand() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(75.dp)
            ) {
                AnimatedVisibility(
                    visible = bottomSheetState.bottomSheetState.progress == 1.0f && !bottomSheetState.bottomSheetState.isExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = currentSong?.image,
                            contentDescription = "image",
                            modifier = Modifier
                                .size(70.dp)
                                .padding(10.dp)
                                .background(brush = shimmerEffectfun(showShimmer.value))
                                .clip(RoundedCornerShape(4.dp)),
                            onSuccess = { showShimmer.value = false },
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 20.dp)
                        ) {
                            currentSong?.title?.let {
                                Text(
                                    text = it,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            currentSong?.moreInfo?.artistMap?.artists?.get(1)?.name?.let {
                                Text(
                                    text = it,
                                    fontSize = 14.sp,
                                    color = Color.LightGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(
                            onClick = { /*TODO*/ },
                            modifier = Modifier.padding(end = 5.dp)
                        ) {
                            Icon(
                                Icons.Default.FavoriteBorder,
                                contentDescription = "Play/Pause",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    playerViewModel.pause()
                                } else {
                                    playerViewModel.play()
                                }
                            },
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .indication(remember {
                                    MutableInteractionSource()
                                }, null)

                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White
                            )
                        }


                        IconButton(
                            onClick = { musicPlayer.skipToNext() },
                            modifier = Modifier.padding(end = 10.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Play/Pause",
                                tint = Color.White
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = !(bottomSheetState.bottomSheetState.progress == 1.0f && !bottomSheetState.bottomSheetState.isExpanded),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = SpaceBetween
                    ) {
                        IconButton(onClick = onCollapse, modifier = Modifier) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        IconButton(onClick = { /* Handle menu button click */ }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()

            ) {
                AnimatedVisibility(
                    visible = !(bottomSheetState.bottomSheetState.progress == 1.0f && !bottomSheetState.bottomSheetState.isExpanded),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {

                        AsyncImage(
                            model = qualityImgUrl,
                            contentDescription = "image",
                            modifier = Modifier
                                .padding(start = 50.dp, end = 50.dp)
                                .width(250.dp)
                                .height(250.dp)
                                .background(brush = shimmerEffectfun(showShimmer.value))
                                .clip(RoundedCornerShape(10.dp)),
                            onSuccess = { showShimmer.value = false },
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )

                    }

                }
                TrackSlider(
                    value = sliderPosition.longValue.toFloat(),
                    onValueChange = {
                        sliderPosition.longValue = it.toLong()
                    },
                    onValueChangeFinished = {
                        currentPosition.longValue = sliderPosition.longValue
                        player.seekTo(sliderPosition.longValue)
                    },
                    songDuration = totalDuration.longValue.toFloat()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {

                    Text(
                        text = (currentPosition.longValue).convertToText(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold)
                    )

                    val remainTime = totalDuration.longValue - currentPosition.longValue
                    Text(
                        text = if (remainTime >= 0) remainTime.convertToText() else "",
                        modifier = Modifier
                            .padding(8.dp),
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ControlButton(icon = Icons.Default.SkipPrevious, size = 40.dp, onClick = {
                        musicPlayer.skipToPrevious()
                    })
                    Spacer(modifier = Modifier.width(20.dp))
                    ControlButton(
                        icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        size = 100.dp,
                        onClick = {
                            if (isPlaying) {
                                playerViewModel.pause()
                            } else {
                                playerViewModel.play()
                            }
                        })
                    Spacer(modifier = Modifier.width(20.dp))
                    ControlButton(
                        icon = Icons.Default.SkipNext,
                        size = 40.dp,
                        onClick = {
                            musicPlayer.skipToNext()
                        }
                    )
                }
            }
        }
    }
}



@Composable
fun Player(player: ExoPlayer, isPlaying: Boolean,onClick: (Boolean) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControlButton(icon = Icons.Default.SkipPrevious, size = 40.dp, onClick = {
            player.seekToPreviousMediaItem()
        })
        Spacer(modifier = Modifier.width(20.dp))
        ControlButton(
            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            size = 100.dp,
            onClick = {
                onClick(player.isPlaying)
            })
        Spacer(modifier = Modifier.width(20.dp))
        ControlButton(icon = Icons.Default.SkipNext, size = 40.dp, onClick = {
            player.seekToNextMediaItem()
        })
    }
}

