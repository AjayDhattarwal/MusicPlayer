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


    private val _imageBackground = MutableStateFlow<Bitmap>(Bitmap.createBitmap(500,500,Bitmap.Config.ALPHA_8))
    val imageBackground: MutableStateFlow<Bitmap> = _imageBackground

//    init {
//        val path = preferencesManager.getBackgroundImagePath()
//        if(path != ""){
//            val file = File(path)
//            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
//            _imageBackground.value = bitmap
//        }
//    }

    fun updateBackgroundColors(colors: List<Color>){
        _backgroundColors.value = colors
    }

    fun updateGradient(newGradient: Brush) {
        _blackToGrayGradient.value = newGradient
    }
    fun updateBackgroundImage(bitmap: Bitmap){
        _imageBackground.value = bitmap
    }
}