package com.ar.musicplayer.screens.library.mymusic

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ar.musicplayer.components.mix.AlbumsLazyVGrid
import com.ar.musicplayer.components.mix.ArtistsLazyColumn
import com.ar.musicplayer.components.mix.SongsLazyColumn
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.utils.permission.PermissionHandler
import com.ar.musicplayer.utils.permission.PermissionModel
import com.ar.musicplayer.utils.permission.hasPermissions
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.navigation.SearchMyMusicObj
import com.ar.musicplayer.screens.library.viewmodel.LocalSongsViewModel
import kotlinx.coroutines.launch


@androidx.media3.common.util.UnstableApi
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MyMusicScreen(
    playerViewModel: PlayerViewModel,
    localSongsViewModel: LocalSongsViewModel,
    onBackPressed: () -> Unit,
    onNavigate: (Any) -> Unit,
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val titles = listOf("Songs", "Albums", "Artists", "Genres")
    val pagerState = rememberPagerState(initialPage = 0,pageCount = {titles.size})

    if(!hasPermissions(context as ComponentActivity, Manifest.permission.RECORD_AUDIO)){
        PermissionHandler(
            permissions = listOf(
                PermissionModel(
                    permission = "android.permission.READ_MEDIA_AUDIO",
                    maxSDKVersion = Int.MAX_VALUE,
                    minSDKVersion = 33,
                    rational = "Access is required for Internal Audio Files"
                )
            ),
            askPermission = true
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "My Music", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate(SearchMyMusicObj) }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.padding(bottom = 10.dp)
            )

        },
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->


        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = remember {
                    { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier =
                            Modifier
                                .tabIndicatorOffset(
                                    tabPositions[pagerState.currentPage]
                                )
                                .padding(start = 15.dp, end = 15.dp)
                                .clip(RoundedCornerShape(50)),
                            color = Color.LightGray,
                        )
                    }
                },
                containerColor = Color.Transparent,
                divider = {},
            ) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = remember {
                            {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        },
                        content = {
                            Text(
                                text = title,
                                color = Color.LightGray,
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                        },
                        selectedContentColor = Color.White,
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { page ->

                MyMusicDisplay(
                    page = page,
                    localSongsViewModel = localSongsViewModel,
                    onSongClick = remember {
                        {
                            playerViewModel.setNewTrack(it)
                        }
                    }
                )

            }

        }

    }

}


@Composable
fun MyMusicDisplay(
    page: Int,
    localSongsViewModel: LocalSongsViewModel,
    onSongClick: (SongResponse) -> Unit
){
    val isLoading by localSongsViewModel.isLoading.collectAsState()
    val songResponseList by localSongsViewModel.songResponseList.collectAsState()
    val songsByAlbum by localSongsViewModel.songsByAlbum.collectAsState()
    val songsByArtist by localSongsViewModel.songsByArtist.collectAsState()


    when (page) {
        0 -> SongsLazyColumn(
            songResponseList,
            onSongClick = onSongClick
        )

        1 -> AlbumsLazyVGrid(songsByAlbum)
        2 -> ArtistsLazyColumn(songsByArtist)
        3 -> LocalGenresScreen()
    }
}




@Composable
fun LocalGenresScreen() {
    Text("Genres content goes here", color = Color.White)
}
@Composable
@Preview
fun PreviewMyMusic() {

}
