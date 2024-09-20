package com.ar.musicplayer.components.player

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.toLargeImg
import com.ar.musicplayer.screens.library.mymusic.toPx
import kotlinx.collections.immutable.PersistentList
import kotlin.math.absoluteValue


@Composable
fun AnimatedPager(
    pagerState: PagerState,
    items: State<PersistentList<SongResponse>>
){
    HorizontalPager(
        state = pagerState,
        beyondViewportPageCount = 2,
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(10.dp)
            .background(Color.Transparent),
        pageSize = PageSize.Fill,
        pageSpacing = 10.dp
    ) { page ->
        if (page in items.value.indices) {
            val imageUrl = items.value[page].image?.toLargeImg()

            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .size(300.dp.toPx().toInt(), 300.dp.toPx().toInt()) // Request the image size
                    .build()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .animateContentSize()
                        .graphicsLayer {
                            val pageOffset = (
                                    (pagerState.currentPage - page) + pagerState
                                        .currentPageOffsetFraction
                                    ).absoluteValue

                            val scale = lerp(1f, 1.7f, pageOffset)
                            scaleX *= scale
                            scaleY *= scale
                        }
                        .sizeIn(70.dp, 310.dp)
                        .background(Color.Transparent)
                        .clip(RoundedCornerShape(5)),
                    contentScale = ContentScale.Crop
                )
            }

        } else {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray)
            )
        }
    }
}




