package com.ar.musicplayer.utils.helper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import com.ar.musicplayer.ui.theme.onBackgroundDark
import com.ar.musicplayer.ui.theme.onSurfaceDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class PaletteExtractor() {


    fun getColorFromSwatch(imageUrl: String): MutableLiveData<Color> {
        val color = MutableLiveData<Color>()
        if (imageUrl.isEmpty()) {
            color.postValue(onBackgroundDark)
        } else {
            GlobalScope.launch {
                val bitmap = getBitmapFromURL(imageUrl)
                withContext(Dispatchers.Main) {
                    if (bitmap != null && !bitmap.isRecycled) {
                        val palette: Palette = Palette.from(bitmap).generate()


                        val dominant = palette.dominantSwatch?.rgb?.let { color ->
                            arrayListOf(color.red, color.green, color.blue)

                        }
                        val composeColor =
                            dominant?.get(0)?.let { it1 ->
                                Color(
                                    red = it1,
                                    green = dominant[1],
                                    blue = dominant[2]
                                )
                            }

                        if(composeColor != null){
                            if(isColorLight(composeColor)){
                                color.postValue(darkenColor(composeColor,0.7f))
                            } else{
                                color.postValue(composeColor!!)
                            }
                        }

                    }

                }
            }
        }

        return color
    }

    private fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null

        }
    }

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

    companion object {
        private const val TAG = "PaletteExtractor"
    }
}