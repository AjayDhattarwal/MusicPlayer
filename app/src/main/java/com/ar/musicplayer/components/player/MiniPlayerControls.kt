package com.ar.musicplayer.components.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.navigation.currentFraction
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterialApi::class)
@UnstableApi
@Composable
fun MiniPlayerControls(
    bottomSheetState: BottomSheetScaffoldState,
    playerViewModel: PlayerViewModel,
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    songName: String,
    artistsNames: String
){
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()

    val isFavouriteFlow by remember {
        derivedStateOf {
            favoriteViewModel.isFavoriteSong(currentSong?.id.toString())
        }
    }

    val miniPlayerVisibility by remember {
        derivedStateOf {
            bottomSheetState.currentFraction < 0.9f
        }
    }

    val isFavourite by isFavouriteFlow.collectAsState(false)

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
//                    text = if (waitForPlayer) "Loading..." else it.replace(
//                        "&quot;",
//                        ""
//                    ),
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

                artistsNames.let {
                    Text(
//                    text = if (waitForPlayer) "" else it.replace("&quot;", ""),
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
                        if (currentSong?.id != "") {
                            favoriteViewModel.onEvent(FavoriteSongEvent.ToggleFavSong(songResponse = currentSong!!))
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