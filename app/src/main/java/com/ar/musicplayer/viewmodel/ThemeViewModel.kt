package com.ar.musicplayer.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.ar.musicplayer.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    context: Application
)  : ViewModel() {

    private val preferencesManager = PreferencesManager(context.applicationContext)

    private val _blackToGrayGradient = MutableStateFlow(preferencesManager.getLinearGradientBrush())
    val blackToGrayGradient: StateFlow<Brush> = _blackToGrayGradient

    private val _backgroundColors = MutableStateFlow(preferencesManager.getBackgroundColors())
    val backgroundColors: StateFlow<List<Color>> = _backgroundColors


    fun updateBackgroundColors(colors: List<Color>){
        _backgroundColors.value = colors
    }

    fun updateGradient(newGradient: Brush) {
        _blackToGrayGradient.value = newGradient
    }
}