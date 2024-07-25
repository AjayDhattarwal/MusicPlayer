package com.ar.musicplayer.components.info

import android.graphics.RenderEffect
import android.graphics.Shader
import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.ar.musicplayer.components.CircularProgress
import com.ar.musicplayer.components.mix.AnimatedToolBar
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.models.PlaylistResponse
import com.ar.musicplayer.screens.libraryScreens.mymusic.toDp
import com.ar.musicplayer.screens.libraryScreens.mymusic.toPx
import com.ar.musicplayer.utils.playerHelper.DownloaderViewModel
import com.ar.musicplayer.viewmodel.ImageColorGradient
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.glide.rememberGlidePainter
import kotlin.math.absoluteValue
import kotlin.math.min

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalGlideComposeApi::class)
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun SongListWithTopBar(
    mainImage: String,
    scrollState: LazyListState,
    color: Color,
    subtitle: String,
    data: PlaylistResponse?,
    favViewModel: FavoriteViewModel,
    downloaderViewModel: DownloaderViewModel,
    imageColorViewModel: ImageColorGradient,
    onFollowClicked: () -> Unit,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSongClicked: (Int) -> Unit,
    onBackPressed: () -> Unit,
) {
    val dynamicAlpha =  calculateDynamicAlpha(scrollState)


    val dynamicSize by animateDpAsState(targetValue = lerp(250.dp,0.dp,dynamicAlpha))


    LazyColumn(state = scrollState, modifier = Modifier.statusBarsPadding()) {
        item {
            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                IconButton(
                    onClick = {onBackPressed()}
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, tint = Color.White,
                        contentDescription = "back",
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
                    )
                }
                SubcomposeAsyncImage(
                    model = mainImage,
                    contentDescription = "",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(dynamicSize)
                        .clip(RoundedCornerShape(2))
                ){
                    val state = painter.state
                    if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                        CircularProgress()
                    } else {
                        SubcomposeAsyncImageContent()
                    }
                }

                IconButton(
                    onClick = {onBackPressed()}
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert, tint = Color.White,
                        contentDescription = "back",
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
                    )
                }
            }
        }
        stickyHeader {

            Box(
                contentAlignment = Alignment.CenterEnd, modifier = Modifier
                    .fillMaxWidth()
            ) {
                if(dynamicAlpha >= 1f){
                    AnimatedToolBar(
                        skip = true,
                        title = data?.title ?: "",
                        scrollState = scrollState,
                        onBackPressed = {onBackPressed()},
                        color = color
                    )
                }else{
                    Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp)) {
                        Text(
                            text = data?.title.toString(),
                            style = typography.h5.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .basicMarquee()
                        )
                        Text(
                            text = subtitle,
                            color = Color.LightGray,
                            maxLines = 1,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(4.dp),
                        )
                    }
                }

                AnimatedPlayPauseButton(
                    isPlaying = isPlaying,
                    onPlayPauseToggle = {
                        onPlayPause()
                    },
                    modifier = Modifier
                        .offset(y = 28.dp)
                        .padding(end = 10.dp)
                )
            }
            Spacer(
                modifier = Modifier
                    .height(16.dp)
            )

        }


        data?.list?.let { item ->
            itemsIndexed(item) { index, track ->

                if (track != null) {
                    SongItemRepresentation(
                        track = track,
                        index = index,
                        favViewModel = favViewModel,
                        downloaderViewModel = downloaderViewModel,
                        imageColorViewModel = imageColorViewModel,
                        onTrackClicked = {
                            onSongClicked(index)
                        }
                    )
                }
            }

        }
        item {
            Spacer(
                modifier = Modifier
                    .height(125.dp)
            )
        }

    }

}

fun calculateDynamicAlpha(scrollState: LazyListState): Float {
    val offset = scrollState.firstVisibleItemScrollOffset
    val index = scrollState.firstVisibleItemIndex

    val rawAlpha = if (index < 1) {
        (min(offset, 400) * 0.0025).toFloat() // Scale factor adjusted for smooth transition
    } else {
        1.0f
    }

    return rawAlpha
}

