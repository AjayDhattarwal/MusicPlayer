package com.ar.musicplayer.components.player

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import androidx.media3.common.util.UnstableApi
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.ar.musicplayer.components.CircularProgress
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.navigation.currentFraction
import com.ar.musicplayer.screens.library.mymusic.toPx
import com.ar.musicplayer.utils.PreferencesManager
import kotlin.math.absoluteValue

@UnstableApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AnimatedHorizontalPager(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    bottomSheetState: BottomSheetScaffoldState,
    playlist: List<SongResponse>
) {

    val size by animateDpAsState(targetValue = lerp(
        70.dp,
        310.dp,
        bottomSheetState.currentFraction
    ))

    val dynamicImgBoxSize by animateDpAsState(targetValue = lerp(
        70.dp,
        LocalConfiguration.current.screenWidthDp.dp,
        bottomSheetState.currentFraction
    ))

    HorizontalPager(
        state = pagerState,
        beyondViewportPageCount = 2,
        pageSize = PageSize.Fill,
        pageSpacing = 10.dp,
        modifier = modifier
            .animateContentSize()
            .size(dynamicImgBoxSize)
            .padding(10.dp)
            .background(Color.Transparent)
    ) { page ->
        if (page in playlist.indices) {
            val imageUrl = playlist[page].image.toString().replace("150x150", "350x350")

            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .size(300.dp.toPx().toInt(), 300.dp.toPx().toInt()) // Request the image size
                    .build()
            )

            val pageOffset = (
                    (pagerState.currentPage - page) + pagerState
                        .currentPageOffsetFraction
                    ).absoluteValue

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
                        .size(size)
                        .graphicsLayer {
                            val scale = lerp(1f, 1.7f, pageOffset)
                            scaleX *= scale
                            scaleY *= scale
                        }
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

