package com.ar.musicplayer.screens.library.mymusic

import android.content.res.Resources
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.components.info.AnimatedPlayPauseButton
import com.ar.musicplayer.models.PlaylistResponse
import com.ar.musicplayer.viewmodel.ImageColorGradient
import kotlin.math.absoluteValue

@Composable
fun DetailsScreen(
    navController: NavHostController,
    playlistResponse: PlaylistResponse,
    playerViewModel: PlayerViewModel,
) {
    val context = LocalContext.current
    val imageColorViewModel: ImageColorGradient = viewModel()
    val songResponseList = playlistResponse.list
    val imageHeight = 250.dp
    val scrollState = rememberScrollState()
    val isPlaying = remember { mutableStateOf(false) }

    val imageSize by remember {
        derivedStateOf {
            val offset = scrollState.value
            when {
                offset >= imageHeight.toPx() -> 0.dp
                else -> imageHeight - offset.absoluteValue.toFloat().toDp()
            }
        }
    }

    val imageAlpha by remember {
        derivedStateOf {
            val offset = scrollState.value
            when {
                offset >= imageHeight.toPx() -> 0f
                else -> 1f - (offset / imageHeight.toPx())
            }
        }
    }

    val showTopBar by remember {
        derivedStateOf {
            scrollState.value > 640
        }
    }
    Log.d("offset", "${ songResponseList }")

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }
        AnimatedVisibility(
            visible = showTopBar,
            enter = fadeIn(animationSpec = tween(durationMillis = 400)),
            exit = fadeOut(animationSpec = tween(durationMillis = 400))
        ) {

            TopAppBar(
                title = { Text(text = "Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                backgroundColor = MaterialTheme.colorScheme.primary
            )

        }

        Column{
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 10.dp)
                    .statusBarsPadding()
            ) {
                Box(Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = playlistResponse.image,
                        contentDescription = "image",
                        modifier = Modifier
                            .clip(RoundedCornerShape(5))
                            .size(imageSize)
                            .alpha(imageAlpha)
                            .align(Alignment.Center),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )

                    Text(text = playlistResponse.title.toString(), color = Color.White , modifier = Modifier.align(Alignment.BottomCenter))

                }

                AnimatedPlayPauseButton(
                    isPlaying = isPlaying.value,
                    onPlayPauseToggle = { isPlaying.value = it },
                    modifier = Modifier
                        .offset(-(26).dp)
                        .align(Alignment.End)
                )
            }
            Column (modifier = Modifier.verticalScroll(scrollState)) {
                songResponseList?.let {
                    repeat(it.size) { index ->
                        val artistName = songResponseList[index].moreInfo?.artistMap?.artists?.distinctBy { it.name }?.joinToString(", "){it.name.toString()}
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 5.dp, top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            AsyncImage(
                                model = songResponseList[index].image,
                                contentDescription = "image",
                                modifier = Modifier
                                    .size(50.dp)
                                    .padding(4.dp)

                                    .clip(RoundedCornerShape(3.dp)),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center
                            )

                            Column(
                                modifier = Modifier
                                    .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                                    .weight(1f)
                                    .clickable {
                                        playerViewModel.setNewTrack(
                                            songResponseList[index]
                                        )
                                    }
                            ) {
                                Text(
                                    text = songResponseList[index].title ?: "null",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 2.dp),
                                    maxLines = 1,
                                    softWrap = true,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = artistName?: "unknown",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    softWrap = true,
                                    overflow = TextOverflow.Ellipsis
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
            }
        }
    }
}


fun Float.toDp() = (this / Resources.getSystem().displayMetrics.density).dp
fun Dp.toPx() = (this.value * Resources.getSystem().displayMetrics.density)

@Preview(showBackground = true)
@Composable
fun DetailsScreenPreview() {
//    DetailsScreen()
}



