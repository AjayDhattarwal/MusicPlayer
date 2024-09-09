
package com.ar.musicplayer.screens.home

import android.graphics.RenderEffect
import android.util.Log
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ar.musicplayer.data.models.HomeListItem
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.components.home.EditUsernameDialog
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.components.home.TopProfileBar
import com.ar.musicplayer.components.home.HomeScreenRow
import com.ar.musicplayer.components.home.LastSessionGridLayout
import com.ar.musicplayer.data.models.HomeData
import com.ar.musicplayer.data.models.ModulesOfHomeScreen
import com.ar.musicplayer.navigation.InfoScreenObj
import com.ar.musicplayer.ui.theme.WindowInfoVM
import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.viewmodel.RadioStationViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun HomeScreen(
    windowInfoVM: WindowInfoVM,
    listState: LazyListState,
    homeViewModel: HomeViewModel ,
    playerViewModel: PlayerViewModel ,
    navigateSetting: () -> Unit,
    onItemClick: (Boolean,HomeListItem) -> Unit

) {
    Log.d("recompose", "homeScreen recompose")


    val context = LocalContext.current
    val preferencesManager = remember{ PreferencesManager(context = context) }

    val homeData by homeViewModel.homeData.collectAsState()
    val lastSession by playerViewModel.lastSession.collectAsState()

    val showLastSession by remember {
        derivedStateOf {
            lastSession.isNotEmpty()
        }
    }

    val homeDataList by remember {
        derivedStateOf {
            homeData?.let {
                homeData?.modules?.let { it1 ->
                    getMappedHomeData(it, it1).toList()
                }
            }
        }
    }



    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
//                drawRect(brush = background)
            }
    ){
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            containerColor = Color.Transparent,
            content = { innerPadding ->
                Column(modifier = Modifier
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier,
                        state = listState,
                        verticalArrangement = Arrangement.SpaceBetween,
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        item(key = "item2") {
                            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                        }
                        item(key = "item0") {
                            TopProfileBar(
                                title = preferencesManager.getUsername(),
                                color = Color(preferencesManager.getAccentColor()),
                                modifier = Modifier,
                                onClick = {
                                    navigateSetting()
                                },
                                onUserFiledClick = remember{
                                    { showDialog = true }
                                }
                            )
                            if(showDialog){
                                EditUsernameDialog(
                                    initialUsername = preferencesManager.getUsername(),
                                    onDismissRequest = remember {
                                        { showDialog = false }
                                    },
                                    onUsernameChange = remember {
                                        { newUsername ->
                                            preferencesManager.setUsername(newUsername)
                                        }
                                    }
                                )
                            }
                        }
                        item(key = "item1") {
                            if(showLastSession){
                                LastSessionGridLayout(
                                    modifier = Modifier,
                                    playerViewModel = playerViewModel,
                                    onRecentSongClicked = remember {
                                        { item ->
                                            playerViewModel.setNewTrack(item)
                                        }
                                    }
                                )
                            }
                        }

                        item(key = "item4") {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        itemsIndexed(homeDataList ?: emptyList(), key = { index, item -> (item.first + index) }) { index, (key, dataList) ->
                            HomeScreenRow(
                                title = key ?: "unknown",
                                data = dataList,
                                onCardClicked = onItemClick
                            )
                        }
                        item(key = "item3"){
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
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