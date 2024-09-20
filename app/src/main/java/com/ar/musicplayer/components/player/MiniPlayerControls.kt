package com.ar.musicplayer.components.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.data.models.SongInfo

@OptIn(ExperimentalMaterialApi::class)
@UnstableApi
@Composable
fun MiniPlayerControls(
    fraction: () -> Float,
    title: String,
    artistsNames: String,
    isFavourite: State<Boolean>,
    onFavClick: () -> Unit,
    isPlaying: State<Boolean>,
    onPlayPauseClick: () -> Unit,
    onSkipNextClick: () -> Unit
){

    val miniPlayerVisibility by remember {
        derivedStateOf {
           fraction() < 0.9f
        }
    }


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize()
    ){
        AnimatedVisibility(
            visible = miniPlayerVisibility,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                Modifier.fillMaxWidth()
            ){

                Column(
                    modifier = Modifier
                        .padding(end = 20.dp)
                        .weight(0.45f)
                ) {

                    Text(
                        text = title,
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

                    Text(
                        text = artistsNames,
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

                Row(
                    Modifier.weight(0.55f),
                ) {
                    FavToggleButton(
                        isFavorite = isFavourite.value,
                        onFavClick = onFavClick
                    )

                    PlayPauseButton(
                        isPlaying = isPlaying.value,
                        onPlayPauseClick = onPlayPauseClick
                    )

                    SkipNextButton(
                        onSkipNext = onSkipNextClick

                    )
                }
            }
        }

    }
}