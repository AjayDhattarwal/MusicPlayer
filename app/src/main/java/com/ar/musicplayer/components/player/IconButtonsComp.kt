package com.ar.musicplayer.components.player

import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


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
            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
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
            if (isPlaying.value) Icons.Filled.PauseCircle else Icons.Default.PlayCircle,
            contentDescription = "Play/Pause",
            modifier = Modifier.size((100 / 1.5f).dp),
            tint = Color.White
        )
    }
}


@Composable
fun FavToggleButton(
    isFavorite: Boolean,
    onFavClick: () -> Unit
) {
    IconButton(
        onClick = onFavClick,
        modifier = Modifier
            .padding(end = 5.dp)
            .indication(remember { MutableInteractionSource() }, null)
    ) {
        Icon(
            imageVector = if(isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
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
            Icons.Default.SkipNext,
            contentDescription = "SkipNext",
            tint = Color.White
        )
    }
}

