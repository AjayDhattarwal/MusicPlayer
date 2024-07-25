package com.ar.musicplayer.components.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.min

@Composable
fun TopOver(scrollState: LazyListState, surfaceGradient: ArrayList<Color>) {

    val dynamicAlpha =
        if (scrollState.firstVisibleItemIndex < 1) {
            ((min(scrollState.firstVisibleItemScrollOffset, 400) * 0.2) / 100).toFloat()
        } else {
            1f
        }
    val adjustedColors = surfaceGradient.map { it.copy(alpha = dynamicAlpha) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = adjustedColors,
                    endY = 1000f
                )
            )
    )
}