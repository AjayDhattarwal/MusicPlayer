package com.ar.musicplayer.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import com.ar.musicplayer.components.settings.getHSVFromColor
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class PreferencesManager(context: Context) {
    private val sharedPreferences:
            SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)


    companion object {
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_FONT_STYLE = "font_style"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_BRIGHTNESS = "brightness"
        private const val KEY_DARK_MODE_VARIATION = "dark_mode_variation"
        private const val KEY_ANIMATIONS_ENABLED = "animations_enabled"
        private const val KEY_IS_IMAGE_AS_BACKGROUND = "is_image_as_background"
        private const val KEY_BACKGROUND_IMAGE = "background_image"



        private const val KEY_BRUSH_TYPE = "brush_type"
        private const val KEY_LINEAR_COLORS = "linear_colors"
        private const val KEY_LINEAR_START_X = "linear_start_x"
        private const val KEY_LINEAR_START_Y = "linear_start_y"
        private const val KEY_LINEAR_END_X = "linear_end_x"
        private const val KEY_LINEAR_END_Y = "linear_end_y"

        private const val KEY_USERNAME = "username"

        private const val KEY_STREAM_QUALITY = "stream_quality"
        private const val KEY_CROSSFADE_DURATION = "crossfade_duration"
        private const val KEY_IS_GAPLESS_PLAYBACK_ENABLED = "is_gapless_playback_enabled"
        private const val KEY_IS_GESTURES_ENABLED = "is_gestures_enabled"
        private const val KEY_IS_VOLUME_LEVELING_ENABLED = "is_volume_leveling_enabled"
        private const val KEY_IS_AUTO_PLAY = "is_auto_play"

        private const val KEY_DOWNLOAD_QUALITY = "download_quality"
        private const val KEY_DOWNLOAD_LOCATION = "download_location"
        private const val KEY_EFFORTLESS_ORGANIZE = "effortlessly_organize"
        private const val KEY_DOWNLOAD_LYRICS = "download_lyrics"

    }

    fun setImageAsBackGround(boolean: Boolean){
        sharedPreferences.edit().putBoolean(KEY_IS_IMAGE_AS_BACKGROUND, boolean).apply()
    }

    fun saveBackgroundImagePath(path: String){
        sharedPreferences.edit().putString(KEY_BACKGROUND_IMAGE,path).apply()
    }

    fun setDownloadQuality(quality: String){
        sharedPreferences.edit().putString(KEY_DOWNLOAD_QUALITY,quality).apply()
    }

    fun setDownloadLocation(location: String){
        sharedPreferences.edit().putString(KEY_DOWNLOAD_LOCATION, location).apply()
    }
    fun setEffortlesslyOrganize(enabled: Boolean){
        sharedPreferences.edit().putBoolean(KEY_EFFORTLESS_ORGANIZE, enabled).apply()
    }
    fun setDownloadLyrics(enabled: Boolean){
        sharedPreferences.edit().putBoolean(KEY_DOWNLOAD_LYRICS, enabled).apply()
    }

    fun setStreamQuality(quality: String) {
        sharedPreferences.edit().putString(KEY_STREAM_QUALITY, quality).apply()
    }
    fun setCrossfadeDuration(duration: Int) {
        sharedPreferences.edit().putInt(KEY_CROSSFADE_DURATION, duration).apply()
    }
    fun setGaplessPlaybackEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_GAPLESS_PLAYBACK_ENABLED, enabled).apply()
    }
    fun setGesturesEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_GESTURES_ENABLED, enabled).apply()
    }
    fun setVolumeLevelingEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_VOLUME_LEVELING_ENABLED, enabled).apply()
    }
    fun setAutoPlay(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_AUTO_PLAY, enabled).apply()
    }



    fun setUsername(userName: String){
        sharedPreferences.edit().putString(KEY_USERNAME,userName).apply()
    }

    fun saveAccentColor(color: Int) {
        sharedPreferences.edit().putInt(KEY_ACCENT_COLOR, color).apply()
    }

    fun saveFontStyle(style: String) {
        sharedPreferences.edit().putString(KEY_FONT_STYLE, style).apply()
    }

    fun saveFontSize(size: Int) {
        sharedPreferences.edit().putInt(KEY_FONT_SIZE, size).apply()
    }

    fun saveBrightness(brightness: Float) {
        sharedPreferences.edit().putFloat(KEY_BRIGHTNESS, brightness).apply()
    }

    fun saveDarkModeVariation(variation: String) {
        sharedPreferences.edit().putString(KEY_DARK_MODE_VARIATION, variation).apply()
    }

    fun saveAnimationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ANIMATIONS_ENABLED, enabled).apply()
    }


    fun saveLinearGradientBrush(colors: List<Color>, start: Offset, end: Offset) {
        sharedPreferences.edit().apply {
            putString(KEY_BRUSH_TYPE, "linear")
            putString(KEY_LINEAR_COLORS, colors.joinToString(",") { it.toArgb().toString() })
            putFloat(KEY_LINEAR_START_X, start.x)
            putFloat(KEY_LINEAR_START_Y, start.y)
            putFloat(KEY_LINEAR_END_X, end.x)
            putFloat(KEY_LINEAR_END_Y, end.y)
        }.apply()
    }




    fun isImageAsBackground(): Boolean = sharedPreferences.getBoolean(KEY_IS_IMAGE_AS_BACKGROUND, false)
    fun getBackgroundImagePath(): String = sharedPreferences.getString(KEY_BACKGROUND_IMAGE,"") ?: ""


    fun getLinearColors(): String = sharedPreferences.getString(KEY_LINEAR_COLORS, null) ?: ""

    fun getAccentColor(): Int = sharedPreferences.getInt(KEY_ACCENT_COLOR, Color(0xFF0AA705).toArgb())
    fun getFontStyle(): String = sharedPreferences.getString(KEY_FONT_STYLE, "Default") ?: "Default"
    fun getFontSize(): Int = sharedPreferences.getInt(KEY_FONT_SIZE, 16)
    fun getBrightness(): Float = sharedPreferences.getFloat(KEY_BRIGHTNESS, 1f)
    fun getDarkModeVariation(): String = sharedPreferences.getString(KEY_DARK_MODE_VARIATION, "Standard Dark") ?: "Standard Dark"
    fun areAnimationsEnabled(): Boolean = sharedPreferences.getBoolean(KEY_ANIMATIONS_ENABLED, true)


    fun getLinearGradientBrush(): Brush {
        val brushType = sharedPreferences.getString(KEY_BRUSH_TYPE, "linear")

        return when (brushType) {
            "linear" -> {
                val colorsString = sharedPreferences.getString(KEY_LINEAR_COLORS, null)
                val colors = colorsString?.split(",")?.map { Color(it.toInt()) } ?: listOf(Color.Gray, Color.Black,Color.Black)
                val startX = sharedPreferences.getFloat(KEY_LINEAR_START_X, 0f)
                val startY = sharedPreferences.getFloat(KEY_LINEAR_START_Y, 0f)
                val endX = sharedPreferences.getFloat(KEY_LINEAR_END_X, 0f)
                val endY = sharedPreferences.getFloat(KEY_LINEAR_END_Y, 0f)
                Brush.linearGradient(colors, start = Offset(startX, startY), end = Offset(endX, endY))
            }
            else -> {
                // Default to a simple Linear Gradient if the type is not recognized
                Brush.linearGradient(listOf(Color.Black, Color.White), start = Offset.Zero, end = Offset(1f, 1f))
            }
        }
    }

    fun getUsername(): String = sharedPreferences.getString(KEY_USERNAME,"UserName") ?: "UserName"

    fun getStreamQuality(): String = sharedPreferences.getString(KEY_STREAM_QUALITY, "320") ?: "320"
    fun getCrossfadeDuration(): Int = sharedPreferences.getInt(KEY_CROSSFADE_DURATION, 0)
    fun isGaplessPlaybackEnabled(): Boolean = sharedPreferences.getBoolean(
        KEY_IS_GAPLESS_PLAYBACK_ENABLED, false)
    fun isGesturesEnabled(): Boolean = sharedPreferences.getBoolean(KEY_IS_GESTURES_ENABLED, false)
    fun isVolumeLevelingEnabled(): Boolean = sharedPreferences.getBoolean(
        KEY_IS_VOLUME_LEVELING_ENABLED, false)
    fun isAutoPlay(): Boolean = sharedPreferences.getBoolean(KEY_IS_AUTO_PLAY, true)

    fun getDownloadQuality(): String = sharedPreferences.getString(KEY_DOWNLOAD_QUALITY, "320") ?: "320"
    fun getDownloadLocation(): String = sharedPreferences.getString(KEY_DOWNLOAD_LOCATION, "/storage/emulated/0/Music") ?: "/storage/emulated/0/Music"
    fun isEffortlesslyOrganize(): Boolean = sharedPreferences.getBoolean(KEY_EFFORTLESS_ORGANIZE, false)
    fun isDownloadLyrics(): Boolean = sharedPreferences.getBoolean(KEY_DOWNLOAD_LYRICS,false)

}