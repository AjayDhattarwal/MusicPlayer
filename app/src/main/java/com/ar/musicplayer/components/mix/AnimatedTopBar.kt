package com.ar.musicplayer.components.mix

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ar.musicplayer.ui.theme.DarkBlackThemeColor
import kotlin.math.min

@Composable
fun AnimatedToolBar(
    title: String,
    scrollState: LazyListState,
    onBackPressed: () -> Unit,
    skip: Boolean = false,
    color: Color
) {
    val dynamicAlpha =
        if(skip){
            if (scrollState.firstVisibleItemIndex < 1) {
                0f
            } else {
                1.0f
            }
        }else{
            0f
        }


    Row(

        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        color,color,
                    )
                ), alpha = dynamicAlpha
            )
    ) {

        TopBar(showHeart = false, skip = skip, alpha = dynamicAlpha, title = title, onBackPressed = { onBackPressed()})
    }
}