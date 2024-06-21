package com.ar.musicplayer.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedPlayPauseButton(
    isPlaying: Boolean,
    onPlayPauseToggle: (Boolean) -> Unit
) {
    val scale = animateFloatAsState(if (isPlaying) 1f else 0.8f)

    Box(
        modifier = Modifier
            .size(52.dp)
            .background(Color.LightGray, shape = CircleShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onPlayPauseToggle(!isPlaying) },

        contentAlignment = Alignment.Center
    ) {
        AnimatedPlayPauseIcon(isPlaying = isPlaying, scale = scale.value)
    }
}

@Composable
fun AnimatedPlayPauseIcon(isPlaying: Boolean, scale: Float) {
    val icon = if (isPlaying) Icons.Filled.PauseCircleFilled else Icons.Filled.PlayCircleFilled
    Icon(
        imageVector = icon,
        contentDescription = if (isPlaying) "Pause" else "Play",
        tint = Color.Black,
        modifier = Modifier.scale(scale).size(40.dp)
    )
}