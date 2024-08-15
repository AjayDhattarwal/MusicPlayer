package com.ar.musicplayer.components.info

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ar.musicplayer.utils.PreferencesManager

@Composable
fun AnimatedPlayPauseButton(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onPlayPauseToggle: (Boolean) -> Unit,
) {
    val preferencesManager = PreferencesManager(LocalContext.current)
    val scale = animateFloatAsState(if (isPlaying) 0.9f else 0.8f)
    val color = Color(preferencesManager.getAccentColor())

    Box(
        modifier = modifier
            .size(52.dp)
            .background(color, shape = CircleShape)
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
    val icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow
    Icon(
        imageVector = icon,
        contentDescription = if (isPlaying) "Pause" else "Play",
        tint = Color.Black,
        modifier = Modifier.scale(scale).size(40.dp)
    )
}