package com.ar.musicplayer.components.player

import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun TrackSlider(
    value: State<Long>,
    onValueChange: (Float) -> Unit,
    songDuration: Float,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
        Slider(
            value = value.value.toFloat(),
            onValueChange = {
                onValueChange(it)
            },
            onValueChangeFinished = {

                onValueChangeFinished()

            },
            valueRange = 0f..songDuration,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.Gray,
            ),
            modifier = modifier

        )
}



