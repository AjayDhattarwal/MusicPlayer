package com.ar.musicplayer.components.info

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.ar.musicplayer.R
import com.ar.musicplayer.utils.PreferencesManager

@Composable
fun AnimatedPlayPauseButton(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onPlayPauseToggle: () -> Unit,
) {
    val preferencesManager = PreferencesManager(LocalContext.current)

    val color = Color(preferencesManager.getAccentColor())


    Box(
        modifier = modifier
            .size(52.dp)
            .background(color, shape = CircleShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onPlayPauseToggle() },

        contentAlignment = Alignment.Center
    ) {
        AnimatedPlayPauseIcon(isPlaying = isPlaying)
    }
}

@Composable
fun AnimatedPlayPauseIcon(isPlaying: Boolean ) {
    val scale by  animateFloatAsState(if (isPlaying) 0.9f else 0.8f)
    val icon = if (isPlaying) R.drawable.ic_pause_24 else R.drawable.ic_play_arrow_24
    Icon(
        imageVector = ImageVector.vectorResource(icon),
        contentDescription = if (isPlaying) "Pause" else "Play",
        tint = Color.Black,
        modifier = Modifier
            .scale(scale)
            .size(40.dp)
    )
}