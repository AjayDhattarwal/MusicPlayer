package com.ar.musicplayer.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.ViewModel
import com.ar.musicplayer.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    context: Context
)  : ViewModel() {
    private val preferencesManager = PreferencesManager(context)
    private val _blackToGrayGradient = mutableStateOf(preferencesManager.getLinearGradientBrush())
    val blackToGrayGradient: State<Brush> = _blackToGrayGradient
    private val _imageBackground = MutableStateFlow<Bitmap>(Bitmap.createBitmap(500,500,Bitmap.Config.ALPHA_8))
    val imageBackground: MutableStateFlow<Bitmap> = _imageBackground

    init {
        val path = preferencesManager.getBackgroundImagePath()
        if(path != ""){
            val file = File(path)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            _imageBackground.value = bitmap
        }
    }

    fun updateGradient(newGradient: Brush) {
        _blackToGrayGradient.value = newGradient
    }
    fun updateBackgroundImage(bitmap: Bitmap){
        _imageBackground.value = bitmap
    }
}