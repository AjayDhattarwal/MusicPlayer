package com.ar.musicplayer.components.settings

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.window.Dialog
import com.ar.musicplayer.screens.library.mymusic.toDp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.ranges.coerceIn

@Composable
fun CustomColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val (selectedHue, selectedSaturation, selectedBrightness) = getHSVFromColor(selectedColor)
    var hue by remember { mutableStateOf(selectedHue) }
    var saturation by remember { mutableStateOf(selectedSaturation) }
    var brightness by remember { mutableStateOf(selectedBrightness) }
    var alpha by remember { mutableStateOf(selectedColor.alpha) }

    Column {

        SaturationBrightnessSquare(
            hue = hue,
            saturation = saturation,
            brightness = brightness,
            onSaturationBrightnessChanged = { s, b ->
                saturation = s
                brightness = b
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        HueSlider(hue = hue, onHueChanged = { hue = it })
        Spacer(modifier = Modifier.height(16.dp))
        AlphaSlider(color = Color.hsv(hue, saturation, brightness),alpha = alpha, onAlphaChanged = { alpha = it })
        onColorSelected(Color.hsv(hue, saturation, brightness, alpha))
    }
}

@Composable
fun HueSlider(hue: Float, onHueChanged: (Float) -> Unit) {
    var handlePosition by remember { mutableStateOf(hue) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        val sliderWidth = constraints.maxWidth

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newHue = (offset.x / sliderWidth) * 360
                        val clampedHue = min(max(newHue, 0f), 360f)
                        onHueChanged(clampedHue)
                        handlePosition = clampedHue
                    }
                }
        ) {
            val hueColors = listOf(
                Color.Red,
                Color.Magenta,
                Color.Blue,
                Color.Cyan,
                Color.Green,
                Color.Yellow,
                Color.Red
            ).reversed()
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = hueColors
                ),
                size = size,
            )
        }
        val handleOffset = (handlePosition / 360f) * sliderWidth
        Box(
            modifier = Modifier
                .offset(x = handleOffset.toDp() - 8.dp, y = 0.dp)
                .requiredHeight(40.dp)
                .width(8.dp)
                .border(color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(50), width = 1.dp)
                .background(Color.White)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newHandlePosition = (handleOffset + delta) / sliderWidth
                        val newHue = newHandlePosition * 360
                        val clampedHue = min(max(newHue, 0f), 360f)
                        onHueChanged(clampedHue)
                        handlePosition = clampedHue
                    }
                )
        )
    }
}

@Composable
fun SaturationBrightnessSquare(
    hue: Float,
    saturation: Float,
    brightness: Float,
    onSaturationBrightnessChanged: (Float, Float) -> Unit
) {
    var currentSaturation by remember { mutableStateOf(saturation) }
    var currentBrightness by remember { mutableStateOf(brightness) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color.Gray)
    ) {
        val hsvColor = Color.hsv(hue, 1f, 1f)

        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val s = offset.x / constraints.maxWidth
                    val b = 1f - (offset.y / constraints.maxHeight)
                    val clampedS = s.coerceIn(0f, 1f)
                    val clampedB = b.coerceIn(0f, 1f)
                    currentSaturation = clampedS
                    currentBrightness = clampedB
                    onSaturationBrightnessChanged(clampedS, clampedB)
                }
            }
        ) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.White, hsvColor)
                ),
                size = size
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black)
                ),
                size = size
            )
        }

        // Draw the color picker handle
        val handleOffset = Offset(
            x = currentSaturation * constraints.maxWidth,
            y = (1f - currentBrightness) * constraints.maxHeight
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures{ offset ->
                        val s = offset.x / constraints.maxWidth
                        val b = 1f - (offset.y / constraints.maxHeight)
                        val clampedS = s.coerceIn(0f, 1f)
                        val clampedB = b.coerceIn(0f, 1f)
                        currentSaturation = clampedS
                        currentBrightness = clampedB
                        onSaturationBrightnessChanged(clampedS, clampedB)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        val dragX = dragAmount.x / constraints.maxWidth
                        val dragY = -dragAmount.y / constraints.maxHeight

                        var newSaturation = currentSaturation + dragX
                        var newBrightness = currentBrightness + dragY

                        newSaturation = newSaturation.coerceIn(0f, 1f)
                        newBrightness = newBrightness.coerceIn(0f, 1f)

                        currentSaturation = newSaturation
                        currentBrightness = newBrightness
                        onSaturationBrightnessChanged(newSaturation, newBrightness)
                    }
                }
        ) {
            drawCircle(
                color = Color.Black.copy(alpha = 0.6f),
                radius = 10.dp.toPx(),
                center = handleOffset
            )
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = handleOffset
            )
        }
    }
}



@Composable
fun AlphaSlider(alpha: Float, onAlphaChanged: (Float) -> Unit, color: Color) {

    var handlePosition by remember { mutableStateOf(alpha) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        val sliderWidth = constraints.maxWidth

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newAlpha = (offset.x / sliderWidth) * 1f
                        val clampedAlpha = min(max(newAlpha, 0f), 1f)
                        onAlphaChanged(clampedAlpha)
                        handlePosition = clampedAlpha
                    }
                }
        ) {
            val hueColors = listOf(
                Color.White.copy(alpha = 0f),
                color
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = hueColors
                ),
                size = size,
            )
        }
        val handleOffset = (handlePosition / 1f) * sliderWidth
        Box(
            modifier = Modifier
                .offset(x = handleOffset.toDp() - 8.dp, y = 0.dp)
                .requiredHeight(40.dp)
                .width(8.dp)
                .border(color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(50), width = 1.dp)
                .background(Color.White)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newHandlePosition = (handleOffset + delta) / sliderWidth
                        val newAlpha = newHandlePosition * 1f
                        val clampedAlpha = min(max(newAlpha, 0f), 1f)
                        onAlphaChanged(clampedAlpha)
                        handlePosition = clampedAlpha
                    }
                )
        )

    }
}

@Composable
fun FullScreenColorPickerDialog(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                CustomColorPicker(selectedColor, onColorSelected)
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding( 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor =  selectedColor)

                ) {
                    Text("Save", color = Color.LightGray)
                }
            }
        }
    }
}


fun getHSVFromColor(color: Color): Triple<Float, Float, Float> {
    // Convert Jetpack Compose Color to ARGB
    val argb = color.toArgb()
    val r = AndroidColor.red(argb)
    val g = AndroidColor.green(argb)
    val b = AndroidColor.blue(argb)

    // Convert RGB to HSV
    val hsv = FloatArray(3)
    AndroidColor.RGBToHSV(r, g, b, hsv)

    return Triple(hsv[0], hsv[1], hsv[2])
}
