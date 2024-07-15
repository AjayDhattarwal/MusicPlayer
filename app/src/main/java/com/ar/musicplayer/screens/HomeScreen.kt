
package com.ar.musicplayer.screens

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.ArcMode
import androidx.compose.animation.core.ExperimentalAnimationSpecApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FloatAnimationSpec
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ar.musicplayer.models.HomeListItem
import com.ar.musicplayer.viewmodel.NetworkViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.DefaultTranslationX
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.error
import androidx.compose.ui.tooling.data.position
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavHostController
import androidx.wear.compose.material.placeholder
import coil.Coil
import coil.ImageLoader
import coil.compose.rememberImagePainter
import com.bumptech.glide.*
import com.ar.musicplayer.R
import com.ar.musicplayer.components.TopProfileBar
import com.ar.musicplayer.di.roomdatabase.homescreendb.HomeDataEvent
import com.ar.musicplayer.di.roomdatabase.homescreendb.HomeRoomViewModel
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionEvent
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionViewModel
import com.ar.musicplayer.models.HomeData
import com.ar.musicplayer.models.ModulesOfHomeScreen
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.navigation.InfoScreenObj
import com.ar.musicplayer.navigation.SettingsScreenObj
import com.ar.musicplayer.utils.events.RadioStationEvent
import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.viewmodel.ImageColorGradient
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.viewmodel.RadioStationViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import kotlinx.serialization.json.Json

data class ItemWithPosition(val item: String, val position: Int)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    homeRoomViewModel: HomeRoomViewModel,
    radioStationViewModel: RadioStationViewModel,
    playerViewModel: PlayerViewModel,
    lastSessionViewModel: LastSessionViewModel,
    imageColorViewModel: ImageColorGradient,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val homeData by homeViewModel.homeData.observeAsState()
    val viewModel: NetworkViewModel = viewModel()
    val isConnected by viewModel.isConnected.observeAsState(initial = false)
    val homeDataByRoom by homeRoomViewModel.homeData.collectAsState()
    val lastSession by lastSessionViewModel.lastSession.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(isConnected) {
        if (isConnected) {
            homeViewModel.refresh()
        }
    }

    LaunchedEffect(Unit) {
        homeRoomViewModel.onEvent(HomeDataEvent.LoadHomeData)
        lastSessionViewModel.onEvent(LastSessionEvent.LoadLastSessionData)
    }

    if (isConnected && homeData != null) {
        homeRoomViewModel.onEvent(HomeDataEvent.InsertHomeData(homeData!!))
    }

    if (homeData == null && homeDataByRoom != null) {
        homeViewModel._homeData.value = homeDataByRoom
    }

    val blackToGrayGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF000000), Color(0xFF161616)),
        startY = Float.POSITIVE_INFINITY,
        endY = 0f
    )

    Log.d("title","${homeData?.modules?.a8}")
    Log.d("title","${homeData?.cityMod}")


//    val homeDataList = remember(homeData) {
//        homeData?.let {
//            mapOf(
//                "Trending" to it.newTrending,
//                "Top Playlist" to it.topPlaylist,
//                "Albums" to it.newAlbums,
//                "Artist" to it.artistRecos,
////                "City Mod" to it.cityMod,
//                "Charts" to it.charts,
//                "Radio" to it.radio,
//                "Discover" to it.browserDiscover,
//            ).toList()
//        } ?: emptyList()
//    }

    val homeDataList = remember(homeData) {
        homeData?.let {
            homeData?.modules?.let { it1 ->
                getMappedHomeData(it, it1).toList()
            }
        } ?: emptyList()
    }

//    Log.d("title","${homeDataItems}")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(blackToGrayGradient),
        containerColor = Color.Transparent,
        content = { padding ->
            Column(modifier = Modifier
                .padding(padding)
                .fillMaxSize()) {
                LazyColumn(modifier = Modifier) {
                    item {
                        TopProfileBar(
                            modifier = Modifier.padding(16.dp),
                            onClick = { navController.navigate(SettingsScreenObj) }
                        )
                    }

                    lastSession?.takeIf { it.isNotEmpty() }?.let {
                        item {
                            LastSessionGridLayout(
                                modifier = Modifier.padding(bottom = 30.dp),
                                playerViewModel = playerViewModel,
                                imageColorViewModel = imageColorViewModel,
                                lastSessionList = it.reversed()
                            )
                        }
                    }

                    items(homeDataList) { (key, dataList) ->
                        LazyRowItem(
                            modifier = Modifier.padding(bottom = 30.dp),
                            heading = key ?: "unknown",
                            songItems = dataList,
                            onMoreButtonClick = {},
                            navController = navController,
                            radioStationViewModel = radioStationViewModel,
                            playerViewModel = playerViewModel,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedContentScope = animatedContentScope
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    )
}


@Composable
fun LastSessionGridLayout(
    modifier: Modifier = Modifier,
    imageColorViewModel: ImageColorGradient,
    playerViewModel: PlayerViewModel,
    lastSessionList: List<SongResponse>
) {
    val context = LocalContext.current
    val showShimmer = remember { mutableStateOf(true) }
    val gridHeight = if (lastSessionList.size < 2) 100.dp else if (lastSessionList.size in 2..4) 220.dp else 300.dp
    val gridCells = if (lastSessionList.size < 2) 1 else if (lastSessionList.size in 2..4) 2 else 4

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Last Session",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        LazyHorizontalGrid(
            rows = GridCells.Fixed(gridCells),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.height(gridHeight)
        ) {

            items(lastSessionList) { songResponse ->
                Card(
                    modifier = Modifier
                        .height(80.dp)
                        .width(250.dp),
                    colors = CardColors(
                        containerColor = Color(0xBC383838),
                        contentColor = Color.Transparent,
                        disabledContentColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = songResponse.image,
                            contentDescription = "image",
                            modifier = Modifier
                                .size(100.dp)
                                .background(brush = shimmerEffectfun(showShimmer.value)),
                            onSuccess = { showShimmer.value = false },
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                        Column(
                            modifier = Modifier
                                .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                                .weight(1f)
                                .clickable {
                                    playerViewModel.starter.value = false
                                    songResponse.image?.let {
                                        imageColorViewModel.loadImage(
                                            it,
                                            context
                                        )
                                    }
                                    playerViewModel.updateCurrentSong(
                                        songResponse
                                    )
                                    playerViewModel.isPlayingHistory.value = false
                                }
                        ) {
                            Text(
                                text = songResponse.title ?: "null",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 2.dp),
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = songResponse.subtitle ?: "unknown",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@SuppressLint("RememberReturnType")
fun LazyRowItem(
    modifier: Modifier = Modifier,
    heading: String,
    songItems: List<HomeListItem>,
    onMoreButtonClick: () -> Unit,
    navController: NavHostController,
    radioStationViewModel: RadioStationViewModel,
    playerViewModel: PlayerViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = heading,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )

        }
        LazyRow(
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            items(songItems) { songItem ->
                if(songItem.id != ""){
                    SongItemView(
                        heading=  heading,
                        homeListItem = songItem,
                        navController = navController,
                        radioStationViewModel = radioStationViewModel,
                        playerViewModel = playerViewModel,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope = animatedContentScope
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalAnimationSpecApi::class)
@Composable
fun SongItemView(
    heading: String,
    homeListItem: HomeListItem,
    navController: NavHostController,
    radioStationViewModel: RadioStationViewModel,
    playerViewModel: PlayerViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val context = LocalContext.current
    val moreInfo = homeListItem.moreInfoHomeList
    val artistMap = moreInfo?.artistMap
    var allArtist = homeListItem.subtitle
    val radioSongResponse by radioStationViewModel.radioStation.observeAsState()
    var radioStationSelection = remember {
        mutableStateOf(false)
    }

    if(radioStationSelection.value) {
        Log.d("radio", "is active ")
        if (radioSongResponse != null) {
            Log.d("radio", "${radioSongResponse?.size}")
            playerViewModel.playPlaylist(radioSongResponse!!, "radio")
            radioStationSelection.value = false
        }
    }

    if (artistMap != null && artistMap.artists?.isNotEmpty() == true) {
        val artists = artistMap.artists.mapNotNull { it.name }
        allArtist = artists.joinToString(", ").takeIf { it.isNotBlank() } ?: "Unknown Artist"
    }

    val serialized = remember { Json.encodeToString(HomeListItem.serializer(), homeListItem) }
    val showShimmer = remember { mutableStateOf(true) }


    val boundsTransform = BoundsTransform { initialBounds, targetBounds ->
        keyframes {
            durationMillis = 2000
            initialBounds at 0 using ArcMode.ArcBelow using FastOutSlowInEasing
            targetBounds at 1500
        }
    }

    val textBoundsTransform = { _: Rect, _: Rect -> tween<Rect>(1500) }

    with(sharedTransitionScope) {
        Box(
            modifier = Modifier
                .padding(end = 15.dp)
                .width(150.dp)
                .clickable {
                    if (homeListItem.type == "radio_station") {
                        radioStationViewModel.onEvent(
                            RadioStationEvent.LoadRadioStationData(
                                call = "webradio.getSong",
                                k = "20",
                                next = "1",
                                name = homeListItem.moreInfoHomeList?.query.toString(),
                                query = homeListItem.moreInfoHomeList?.query.toString()
                            )
                        )
                        radioStationSelection.value = true
                        playerViewModel.isPlayingHistory.value = false
                        playerViewModel.starter.value = false
                    } else {
                        navController.navigate(InfoScreenObj(serialized, heading))
                    }
                },

            contentAlignment = Alignment.BottomCenter
        ) {

            Column {
                AsyncImage(
                    model = homeListItem.image,
                    contentDescription = "image",
//                     imageLoader = imgeLoader,
                    modifier = Modifier
                        .size(150.dp)
                        .background(brush = shimmerEffectfun(showShimmer.value))
                        .sharedElement(
                            sharedTransitionScope.rememberSharedContentState(key = "image-${heading}-${homeListItem.id}"),
                            animatedVisibilityScope = animatedContentScope,
                            boundsTransform = boundsTransform
                        ),
                    onSuccess = { showShimmer.value = false },
                    contentScale = ContentScale.Crop,

                    alignment = Alignment.Center,
                )

                Text(
                    text = homeListItem.title.orEmpty(),
                    color = Color.White,
                    maxLines = 1,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 4.dp)
                        .fillMaxWidth()
                        .sharedElement(
                            sharedTransitionScope.rememberSharedContentState(key = "title-${heading}-${homeListItem.id}"),
                            animatedVisibilityScope = animatedContentScope,
                            boundsTransform = textBoundsTransform
                        ),
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = allArtist.orEmpty(),
                    color = Color.Gray,
                    maxLines = 1,
                    fontSize = 12.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                            .sharedElement(
                                sharedTransitionScope.rememberSharedContentState(key = "subtitle-${heading}-${homeListItem.id}"),
                                animatedVisibilityScope = animatedContentScope,
                                boundsTransform =  textBoundsTransform

                            )
                )

            }
        }
    }
}



@SuppressLint("RememberReturnType")
@Composable
fun LazyRowDummyItem(
    modifier: Modifier
) {
    val showShimmer = remember { mutableStateOf(true) }
    Column (modifier = modifier){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, end = 16.dp, start = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(brush = shimmerEffectfun(showShimmer.value)),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier){
                Text(text = " ", color = Color.LightGray, fontSize = 14.sp)
            }
        }
        LazyRow(
            modifier = Modifier.padding(top = 8.dp, start = 15.dp, end = 15.dp)
        ){
            items(10){ item ->
                Box(modifier = Modifier
                    .padding(end = 15.dp)
                    .width(150.dp), contentAlignment = Alignment.BottomCenter){
                    Column {
                        AsyncImage(
                            model = item,
                            contentDescription = "image",
                            modifier = Modifier
                                .size(150.dp)
                                .background(brush = shimmerEffectfun(showShimmer.value))
                                .clip(RoundedCornerShape(40.dp)),
                            onSuccess = { showShimmer.value = false },
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                        Text(
                            text = "",
                            color = Color.White,
                            maxLines = 1,
                            fontSize = 14.sp,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 8.dp)
                                .width(70.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(brush = shimmerEffectfun(true)),

                        )
                        Text(
                            text = "",
                            color = Color.Gray,
                            maxLines = 1,
                            fontSize = 12.sp,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .width(100.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(brush = shimmerEffectfun(true))
                        )
                    }
                }
            }
        }

    }
}



//@Composable
//fun shimmerEffectfun(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
//    return if (showShimmer) {
//        // Colors for the shimmer effect
//        val shimmerColors = listOf(
//            Color.LightGray.copy(alpha = 0.6f),
//            Color.LightGray.copy(alpha = 0.5f),
//            Color.LightGray.copy(alpha = 0.6f),
//        )
//
//        // Start the animation transition
//        val transition = InfiniteTransition
//        val translateAnimation = transition.animateFloat(
//            initialValue = 0f,
//            targetValue = targetValue,
//            animationSpec = infiniteRepeatable(
//                animation = tween(1000), repeatMode = RepeatMode.Restart
//            ), label = ""
//        )
//
//        // Return a linear gradient brush
//        Brush.linearGradient(
//            colors = shimmerColors,
//            start = Offset.Zero,
//            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
//        )
//    } else {
//        // If shimmer is turned off, return a transparent brush
//        Brush.linearGradient(
//            colors = listOf(Color.Transparent, Color.Transparent),
//            start = Offset.Zero,
//            end = Offset.Zero
//        )
//    }
//}

fun shimmerEffectfun(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.5f),
            Color.LightGray.copy(alpha = 0.6f),
        )

        var currentOffset = 0f


        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(x = currentOffset, y = 0f),
            end = Offset(x = currentOffset + targetValue, y = 0f)
        )

    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = 0.0f),
                Color.Transparent
            )
        )
    }
}


fun createSortedSourceTitleMap(modules: ModulesOfHomeScreen): Map<String?, String?> {
    return listOf(
        modules.a1, modules.a2, modules.a3, modules.a4, modules.a5,
        modules.a6, modules.a7, modules.a8, modules.a9, modules.a10,
        modules.a11, modules.a12, modules.a13, modules.a14,modules.a15,
        modules.a16, modules.a17
    ).filterNotNull()
        .sortedBy {
        it.position?.toIntOrNull() ?: Int.MAX_VALUE
    }.associate { it.source to it.title  }
}



fun getMappedHomeData(homeData: HomeData, modules: ModulesOfHomeScreen): Map<String?, List<HomeListItem>> {
    val sortedSourceTitleMap = createSortedSourceTitleMap(modules)

    val sourceToListMap = mapOf(
        "new_trending" to homeData.newTrending,
        "top_playlists" to homeData.topPlaylist,
        "new_albums" to homeData.newAlbums,
        "charts" to homeData.charts,
        "radio" to homeData.radio,
        "artist_recos" to homeData.artistRecos,
        "city_mod" to homeData.cityMod,
        "tag_mixes" to homeData.tagMixes,
        "promo:vx:data:68" to homeData.data68,
        "promo:vx:data:76" to homeData.data76,
        "promo:vx:data:185" to homeData.data185,
        "promo:vx:data:107" to homeData.data107,
        "promo:vx:data:113" to homeData.data113,
        "promo:vx:data:114" to homeData.data114,
        "promo:vx:data:116" to homeData.data116,
        "promo:vx:data:145" to homeData.data144,
        "promo:vx:data:211" to homeData.data211,
        "browser_discover" to homeData.browserDiscover,

    )

    return sortedSourceTitleMap.mapNotNull { (source, title) ->
        sourceToListMap[source]?.let { title to it }
    }.toMap()
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun HomeScreenPreview() {

}