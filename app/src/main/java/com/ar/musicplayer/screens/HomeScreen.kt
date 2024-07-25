
package com.ar.musicplayer.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.ar.musicplayer.components.home.TopProfileBar
import com.ar.musicplayer.components.home.HomeScreenRow
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
import com.bumptech.glide.integration.compose.placeholder
import kotlinx.serialization.json.Json

@Composable
fun HomeScreen(
    navController: NavHostController,
    listState: LazyListState,
    homeViewModel: HomeViewModel,
    homeRoomViewModel: HomeRoomViewModel,
    radioStationViewModel: RadioStationViewModel,
    playerViewModel: PlayerViewModel,
    lastSessionViewModel: LastSessionViewModel,
    imageColorViewModel: ImageColorGradient,
) {
    val homeData by homeViewModel.homeData.observeAsState()
    val viewModel: NetworkViewModel = viewModel()
    val isConnected by viewModel.isConnected.observeAsState(initial = false)
    val homeDataByRoom by homeRoomViewModel.homeData.observeAsState()
    val lastSession by lastSessionViewModel.lastSession.observeAsState()
    val radioSongResponse by radioStationViewModel.radioStation.observeAsState()
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

    Log.d("title","${homeData?.topPlaylist?.first()}")


    val homeDataList = remember(homeData) {
        homeData?.let {
            homeData?.modules?.let { it1 ->
                getMappedHomeData(it, it1).toList()
            }
        } ?: emptyList()
    }
    val radioStationSelection = remember {
        mutableStateOf(false)
    }
    LaunchedEffect (radioSongResponse) {
        Log.d("radio", "is active ")
        if (radioSongResponse != null && radioStationSelection.value) {
            playerViewModel.isPlayingHistory.value = false
            playerViewModel.playPlaylist(radioSongResponse!!, "radio")
            radioStationSelection.value = false
        }
    }

    val columnState = rememberScrollState()


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(blackToGrayGradient),
        containerColor = Color.Transparent,
        content = { padding ->
            Column(modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(state = columnState )
            ) {
//                LazyColumn(modifier = Modifier,state = listState) {
//                    item(key = "item0") {
//                        TopProfileBar(
//                            modifier = Modifier.padding(16.dp),
//                            onClick = { navController.navigate(SettingsScreenObj) }
//                        )
//                    }
//
//                    lastSession?.takeIf { it.isNotEmpty() }?.let {
//                        item(key = "item1") {
//                            LastSessionGridLayout(
//                                modifier = Modifier.padding(bottom = 30.dp),
//                                playerViewModel = playerViewModel,
//                                imageColorViewModel = imageColorViewModel,
//                                lastSessionList = it.reversed()
//                            )
//                        }
//                    }

//                    itemsIndexed(homeDataList, key = {index, item -> index }) { index, (key, dataList) ->
//                        HomeScreenRow(
//                            title = key?: "unknown",
//                            data = dataList,
//                            onCardClicked = { radio, data ->
//                                if(radio){
//                                    val item = Json.decodeFromString(HomeListItem.serializer(), data)
//                                    radioStationViewModel.onEvent(
//                                        RadioStationEvent.LoadRadioStationData(
//                                            call = "webradio.getSong",
//                                            k = "20",
//                                            next = "1",
//                                            name = item.moreInfoHomeList?.query.toString(),
//                                            query = item.moreInfoHomeList?.query.toString()
//                                        )
//                                    )
//                                    radioStationSelection.value = true
//                                    playerViewModel.isPlayingHistory.value = false
//                                    playerViewModel.starter.value = false
//                                } else{
//                                    navController.navigate(InfoScreenObj(data, sharedKey = index ))
//                                }
//                            }
//                        )
//                    }
//                     item(key = "item3"){
//                        Spacer(modifier = Modifier.height(80.dp))
//                     }
                TopProfileBar(
                    modifier = Modifier.padding(16.dp),
                    onClick = { navController.navigate(SettingsScreenObj) }
                )
                lastSession?.takeIf { it.isNotEmpty() }?.let {
                    LastSessionGridLayout(
                        modifier = Modifier.padding(bottom = 30.dp),
                        playerViewModel = playerViewModel,
                        imageColorViewModel = imageColorViewModel,
                        lastSessionList = it.reversed()
                    )
                }
                repeat(homeDataList.size){index ->
                    HomeScreenRow(
                        title = homeDataList[index].first?: "unknown",
                        data = homeDataList[index].second,
                        onCardClicked = { radio, data ->
                            if(radio){
                                playerViewModel.waitForPlay(true)
                                val item = Json.decodeFromString(HomeListItem.serializer(), data)
                                val query = if(item.moreInfoHomeList?.query != "") item.moreInfoHomeList?.query else item.title
                                Log.d("radio","$item")
                                radioStationViewModel.onEvent(
                                    RadioStationEvent.LoadRadioStationData(
                                        call = "webradio.getSong",
                                        k = "20",
                                        next = "1",
                                        name = query.toString(),
                                        query = query.toString(),
                                        radioStationType = item.moreInfoHomeList?.stationType.toString(),
                                        language = item.moreInfoHomeList?.language.toString()
                                    )
                                )
                                radioStationSelection.value = true
                            } else{
                                navController.navigate(InfoScreenObj(data, sharedKey = index ))
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(120.dp))

//                }
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
    val gridHeight = if (lastSessionList.size < 2) 80.dp else if (lastSessionList.size in 2..4) 180.dp else 280.dp
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
                        .height(50.dp)
                        .width(250.dp)
                        .clip(RoundedCornerShape(1.dp))
                    ,
                    colors = CardColors(
                        containerColor = Color(0xBC383838),
                        contentColor = Color.Transparent,
                        disabledContentColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = songResponse.image,
                            contentDescription = "image",
                            modifier = Modifier
                                .size(80.dp),
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