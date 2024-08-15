package com.ar.musicplayer.components.mix

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedTopBar(
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

    AnimatedVisibility(
        visible = dynamicAlpha == 1f,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically()+ fadeOut()
    ) {
//        TopBar(
//            alpha = dynamicAlpha,
//            title = title,
//            onBackPressed = { onBackPressed()},
//            backgroundColor = color
//        )
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = title ?: "",
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(16.dp)
//                        .alpha(
//                            alpha = alpha
//                        )
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {onBackPressed()}
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, tint = Color.White,
                        contentDescription = "back",
                        modifier = Modifier
//                            .alpha(alpha)
//                            .padding(start = paddingStart.dp)
                    )
                }
            },

            modifier = Modifier,
//            .alpha(alpha = if (skip && alpha != 1.0f) 0f else 1f),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = color,
            )
        )

    }

//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(
//                brush = Brush.linearGradient(
//                    listOf(
//                        color,color,
//                    )
//                ), alpha = dynamicAlpha
//            )
//    ) {
//
//
//    }
}