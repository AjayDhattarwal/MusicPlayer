package com.ar.musicplayer.components


import android.text.style.LineBackgroundSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ar.musicplayer.ui.theme.DarkBlackThemeColor

@Composable
fun CircularProgress(modifier: Modifier = Modifier,background: Color = Color.LightGray){
    Column(
        Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = modifier)
        }
    }
}