
package com.ar.musicplayer.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.ar.musicplayer.di.roomdatabase.homescreendb.HomeDataEvent
import com.ar.musicplayer.di.roomdatabase.homescreendb.HomeRoomViewModel
import com.ar.musicplayer.navigation.InfoScreenObj
import com.ar.musicplayer.utils.events.RadioStationEvent
import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.viewmodel.RadioStationViewModel
import kotlinx.serialization.json.Json

@UnstableApi
@SuppressLint("UnrememberedMutableState")
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    homeRoomViewModel: HomeRoomViewModel,
    radioStationViewModel: RadioStationViewModel,
    playerViewModel: PlayerViewModel
) {
    val homeData by homeViewModel.homeData.collectAsState(initial = null)
    val viewModel: NetworkViewModel = viewModel()
    val isConnected by viewModel.isConnected.observeAsState(initial = false)
    val homeDataByRoom by homeRoomViewModel.homeData.collectAsState()

    if(isConnected){
        if(homeData != null){
            Log.d("room","homeData Uploaded")
            homeRoomViewModel.onEvent(HomeDataEvent.InsertHomeData(homeData!!))
        }
    }
    else{
        homeDataByRoom?.let {
            if(homeData == null){
                homeViewModel.homeData.value =  it
            }
        }
    }
    LaunchedEffect(isConnected) {
        if (isConnected) {
            Log.d("internet", "internet connection ${isConnected}")
            homeViewModel.refresh()
        }else{
            homeRoomViewModel.onEvent(HomeDataEvent.LoadHomeData)
        }
    }

    val blackToGrayGradient =
        Brush.verticalGradient(
            colors = listOf(Color(0xFF000000),Color(0xFF161616)),
            startY = Float.POSITIVE_INFINITY,
            endY = 0f
        )

    val homeDataMap: Map<String, List<HomeListItem>?> = mapOf(
        "Trending" to homeData?.newTrending,
        "Top Playlist" to homeData?.topPlaylist,
        "Albums" to homeData?.newAlbums,
        "Artist" to homeData?.artistRecos,
//        "charts" to homeData?.charts,
//        "CityMod" to homeData?.cityMod,
        "Radio" to homeData?.radio,
        "Discover" to homeData?.browserDiscover,

    )
    val homeDataList = homeDataMap.toList()



    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(blackToGrayGradient),
        containerColor = Color.Transparent,
        content = { padding ->
            Column(modifier = Modifier
                .padding(padding)
                .fillMaxSize()) {

                LazyColumn(modifier = Modifier){
                    items(homeDataList) { (key, dataList) ->
                        dataList?.let {
                            LazyRowItem(
                                modifier = Modifier.padding(bottom = 30.dp),
                                heading = key ,
                                songItems = it,
                                onMoreButtonClick = {},
                                navController= navController,
                                radioStationViewModel,
                                playerViewModel
                            )
                        }
                    }
                    item{
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    )
}


@Composable
@SuppressLint("RememberReturnType")
fun LazyRowItem(
    modifier: Modifier = Modifier,
    heading: String,
    songItems: List<HomeListItem>,
    onMoreButtonClick: () -> Unit,
    navController: NavHostController,
    radioStationViewModel: RadioStationViewModel,
    playerViewModel: PlayerViewModel
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
            TextButton(onClick = onMoreButtonClick) {
                Text(text = "View All", color = Color.LightGray, fontSize = 14.sp)
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "more", tint = Color.LightGray)
            }
        }
        LazyRow(
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            items(songItems) { songItem ->
                SongItemView(homeListItem = songItem, navController = navController, radioStationViewModel = radioStationViewModel, playerViewModel = playerViewModel)
            }
        }
    }
}
@Composable
fun SongItemView(
    homeListItem: HomeListItem,
    navController: NavHostController,
    radioStationViewModel: RadioStationViewModel,
    playerViewModel: PlayerViewModel
) {
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
    val perfectImg = if(homeListItem.image?.contains("150x150") == true){
        homeListItem.image!!.replace("150x150","350x350")
    }else{
        homeListItem.image
    }
    homeListItem.image = perfectImg
    val serialized = remember { Json.encodeToString(HomeListItem.serializer(), homeListItem) }
    val showShimmer = remember { mutableStateOf(true) }

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
                    navController.navigate(InfoScreenObj(serialized))
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column {

            AsyncImage(
                model = perfectImg,
                contentDescription = "image",
                modifier = Modifier
                    .size(150.dp)
                    .background(brush = shimmerEffectfun(showShimmer.value)),
                onSuccess = { showShimmer.value = false },
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )
            Text(
                text = homeListItem.title.orEmpty(),
                color = Color.White,
                maxLines = 1,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = allArtist.orEmpty(),
                color = Color.Gray,
                maxLines = 1,
                fontSize = 12.sp,
                overflow = TextOverflow.Ellipsis,
            )
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



@Composable
fun shimmerEffectfun(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        // Colors for the shimmer effect
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.5f),
            Color.LightGray.copy(alpha = 0.6f),
        )

        // Start the animation transition
        val transition = rememberInfiniteTransition(label = "")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(1000), repeatMode = RepeatMode.Restart
            ), label = ""
        )

        // Return a linear gradient brush
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        // If shimmer is turned off, return a transparent brush
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}



@Preview
@Composable
fun HomeScreenPreview() {
//    HomeScreen(homedata)
}