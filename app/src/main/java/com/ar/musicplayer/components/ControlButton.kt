package com.ar.musicplayer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp

@Composable
fun ControlButton(icon: ImageVector, size: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(indication = null, interactionSource = remember {
                MutableInteractionSource()
            }) {
                onClick()
            }, contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            modifier = Modifier.size(size / 1.5f),
            tint = Color.White,
            contentDescription = null
        )
    }
}