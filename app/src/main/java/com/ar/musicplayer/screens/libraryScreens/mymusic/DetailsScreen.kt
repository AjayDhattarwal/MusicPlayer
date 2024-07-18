package com.ar.musicplayer.screens.libraryScreens.mymusic

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ar.musicplayer.R

@Composable
fun DetailsScreen() {
    val imageHeight = 250.dp
    val lazyListState = rememberLazyListState()

    val imageSize by remember {
        derivedStateOf {
            val offset = lazyListState.firstVisibleItemScrollOffset
            when {
                offset >= imageHeight.toPx() -> 0.dp
                else -> imageHeight - offset.toFloat().toDp()
            }
        }
    }
    val imageAlpha by remember {
        derivedStateOf {
            val offset = lazyListState.firstVisibleItemScrollOffset
            when {
                offset >= imageHeight.toPx() -> 0f
                else -> 1f - (offset / imageHeight.toPx())
            }
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "Top Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(imageSize)
                        .align(Alignment.Center)
                        .alpha(imageAlpha)
                )
            }
        }
        items(50) { index ->
            Text(
                text = "Song $index",
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            Divider(color = Color.Gray, thickness = 1.dp)
        }
    }
}

fun Float.toDp() = (this / Resources.getSystem().displayMetrics.density).dp
fun Dp.toPx() = (this.value * Resources.getSystem().displayMetrics.density)

@Preview(showBackground = true)
@Composable
fun DetailsScreenPreview() {
    DetailsScreen()
}