package com.ar.musicplayer.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.ar.musicplayer.utils.loadImageBitmapFromUrl
import kotlinx.coroutines.launch

class ImageColorGradient  : ViewModel() {


    private val _bitmapState = MutableLiveData<Bitmap?>()
    val bitmapState: LiveData<Bitmap?> = _bitmapState

    private val _imageColor = MutableLiveData<Color?>()
    val imageColor: LiveData<Color?> = _imageColor

    fun loadImage(url: String,context: Context) {
        viewModelScope.launch {
            val bitmap = loadImageBitmapFromUrl(url, context)
            _bitmapState.value = bitmap
            if (bitmap != null) {
                _imageColor.value = getDominantColor(bitmap)
            }
        }
    }

    fun getDominantColor(bitmap: Bitmap): Color {
        // Convert the bitmap to a compatible format
        val compatibleBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Generate the Palette
        val palette = Palette.from(compatibleBitmap).generate()

        // Retrieve the dominant color
        val dominantSwatch = palette.dominantSwatch ?: palette.vibrantSwatch ?: palette.mutedSwatch

        // Check if dominantSwatch is null
        return if (dominantSwatch != null) {
            Color(dominantSwatch.rgb)
        } else {
            // Return a default color if dominantSwatch is null
            Color.White
        }
    }

}