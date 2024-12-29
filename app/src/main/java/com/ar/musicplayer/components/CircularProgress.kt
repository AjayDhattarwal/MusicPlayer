package com.ar.musicplayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.ar.musicplayer.utils.PreferencesManager

@Composable
fun CircularProgress(modifier: Modifier = Modifier,background: Color = Color.LightGray){
    val preference = PreferencesManager(LocalContext.current)
    val color = Color(preference.getAccentColor())
    Column(
        Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = modifier,color = color)
        }
    }
}