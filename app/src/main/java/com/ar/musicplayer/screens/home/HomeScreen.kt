
package com.ar.musicplayer.screens.home

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ar.musicplayer.data.models.HomeListItem
import androidx.compose.runtime.setValue
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
import com.ar.musicplayer.viewmodel.HomeViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    navigateSetting: () -> Unit,
    onItemClick: (Boolean, HomeListItem) -> Unit
) {

    val context = LocalContext.current
    val preferencesManager = remember{ PreferencesManager(context = context) }

    val homeDataList by homeViewModel.homeData.collectAsState()
    val lastSession by playerViewModel.lastSession.collectAsState()

    val showLastSession by remember {
        derivedStateOf {
            lastSession.isNotEmpty()
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
//                        state = listState,
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

@Preview
@Composable
fun HomeScreenPreview() {

}