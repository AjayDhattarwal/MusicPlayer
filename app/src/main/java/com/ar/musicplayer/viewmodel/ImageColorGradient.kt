package com.ar.musicplayer.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.ar.musicplayer.utils.helper.loadImageBitmapFromUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ImageColorGradient  : ViewModel() {


    val bitmapState = MutableStateFlow<Bitmap?>(null)

    val colorForBackGround = MutableStateFlow<Color?>(null)

    fun loadImage(url: String,context: Context) {
        viewModelScope.launch {
            val bitmap = loadImageBitmapFromUrl(url, context)
            bitmapState.value = bitmap
            if (bitmap != null) {
                colorForBackGround.value = getDominantColor(bitmap)
            }
        }
    }

    fun getDominantColor(bitmap: Bitmap): Color {
        // Convert the bitmap to a compatible format
        val compatibleBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Generate the Palette
        val palette = Palette.from(compatibleBitmap).generate()

        // Retrieve the dominant color
        val dominantSwatch = palette.dominantSwatch ?: palette.darkMutedSwatch ?: palette.darkVibrantSwatch

        // Check if dominantSwatch is null
        val color = if (dominantSwatch != null) {
            Color(dominantSwatch.rgb)
        } else {
            // Return a default color if dominantSwatch is null
            Color.White
        }

        // Function to determine if a color is light
        fun isColorLight(color: Color): Boolean {
            val darkness = 1 - (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
            return darkness < 0.5
        }

        // Function to darken a color
        fun darkenColor(color: Color, factor: Float): Color {
            val r = (color.red * factor).coerceAtLeast(0f)
            val g = (color.green * factor).coerceAtLeast(0f)
            val b = (color.blue * factor).coerceAtLeast(0f)
            return Color(r, g, b, color.alpha)
        }

        // If the color is light, darken it
        return if (isColorLight(color)) {
            darkenColor(color, 0.7f) // Darken by 30%
        } else {
            color
        }
    }


}