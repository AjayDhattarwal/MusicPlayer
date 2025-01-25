@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ar.musicplayer.components.player

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.ar.musicplayer.navigation.currentFraction
import com.ar.musicplayer.screens.library.mymusic.toPx
import com.ar.musicplayer.viewmodel.PlayerViewModel
import kotlin.math.absoluteValue

@UnstableApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SharedElementPager(
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val playlist by playerViewModel.playlist.collectAsState(emptyList())
    val currentIndex by playerViewModel.currentIndex.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()

    val pagerState = rememberPagerState(pageCount = {playlist.size})

    LaunchedEffect(playlist) {
        pagerState.scrollToPage(currentIndex)
    }

    LaunchedEffect(pagerState.currentPage) {
        if (currentIndex != pagerState.currentPage) {
            playerViewModel.changeSong(pagerState.currentPage)
        }
    }

    LaunchedEffect(currentSong) {
        if (currentIndex != pagerState.currentPage) {
            pagerState.scrollToPage(currentIndex)
        }
    }

    with(sharedTransitionScope){
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 2,
            modifier = modifier
                .background(Color.Transparent)
                .sharedBounds(
                    rememberSharedContentState(key = "image"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
            pageSize = PageSize.Fill,
            pageSpacing = 10.dp
        ) { page ->
            if (page in playlist.indices) {
                val imageUrl = playlist[page].image.toString().replace("150x150", "350x350")

                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .size(
                            300.dp.toPx().toInt(),
                            300.dp.toPx().toInt()
                        ) // Request the image size
                        .build()
                )

                val pageOffset = (
                        (pagerState.currentPage - page) + pagerState
                            .currentPageOffsetFraction
                        ).absoluteValue

                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = modifier
                            .graphicsLayer {
                                val scale = lerp(1f, 1.1f, pageOffset)
                                scaleX *= scale
                                scaleY *= scale
                            }
                            .sizeIn(maxWidth = 400.dp, maxHeight = 400.dp, minHeight = 90.dp, minWidth = 90.dp)
                            .padding(10.dp)
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
}

