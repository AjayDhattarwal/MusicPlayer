package com.ar.musicplayer.components.player

import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ar.musicplayer.R


@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit
) {
    IconButton(
        onClick = onPlayPauseClick,
        modifier = Modifier
            .padding(end = 5.dp)
            .indication(remember { MutableInteractionSource() }, null)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(if (isPlaying) R.drawable.ic_pause_24 else R.drawable.ic_play_arrow_24),
            contentDescription = "Play/Pause",
            tint = Color.White
        )
    }
}

@Composable
fun PlayPauseLargeButton(
    size : Dp = 100.dp,
    isPlaying: State<Boolean>,
    onPlayPauseClick: () -> Unit
) {
    IconButton(
        onClick = onPlayPauseClick,
        modifier = Modifier
            .size(size)
            .indication(remember { MutableInteractionSource() }, null)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(if (isPlaying.value) R.drawable.ic_pause_circle else R.drawable.ic_play_circle),
            contentDescription = "Play/Pause",
            modifier = Modifier.size((100 / 1.5f).dp),
            tint = Color.White,

            )
    }
}


@Composable
fun FavToggleButton(
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    onFavClick: () -> Unit
) {
    IconButton(
        onClick = onFavClick,
        modifier = modifier
            .padding(end = 5.dp)
            .indication(remember { MutableInteractionSource() }, null)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(if(isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border) ,
            contentDescription = "Like",
            tint = if(isFavorite) Color.Red else Color.White
        )
    }
}


@Composable
fun SkipNextButton(
    onSkipNext: () -> Unit
) {
    IconButton(
        onClick = onSkipNext,
        modifier = Modifier
            .padding(end = 10.dp)
            .indication(remember { MutableInteractionSource() }, null)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_skip_next_24),
            contentDescription = "SkipNext",
            tint = Color.White
        )
    }
}

