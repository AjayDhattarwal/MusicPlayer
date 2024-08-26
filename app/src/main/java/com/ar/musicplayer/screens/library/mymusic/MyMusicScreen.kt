package com.ar.musicplayer.screens.library.mymusic

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ar.musicplayer.components.mix.AlbumsLazyVGrid
import com.ar.musicplayer.components.mix.ArtistsLazyColumn
import com.ar.musicplayer.components.mix.SongsLazyColumn
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.utils.permission.PermissionHandler
import com.ar.musicplayer.utils.permission.PermissionModel
import com.ar.musicplayer.utils.permission.hasPermissions
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.navigation.SearchMyMusicObj
import com.ar.musicplayer.screens.library.viewmodel.LocalSongsViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MyMusicScreen(
    localSongsViewModel: LocalSongsViewModel,
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    favViewModel: FavoriteViewModel,
) {
    val isLoading by localSongsViewModel.isLoading.collectAsState()
    val songResponseList by localSongsViewModel.songResponseList.collectAsState()
    val songsByAlbum by localSongsViewModel.songsByAlbum.collectAsState()
    val songsByArtist by localSongsViewModel.songsByArtist.collectAsState()

    val pullRefreshState = rememberPullRefreshState(isLoading, { localSongsViewModel.fetchLocalSongs() })


    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val titles = listOf("Songs", "Albums", "Artists", "Genres")
    val pagerState = rememberPagerState(initialPage = 0,pageCount = {titles.size})

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "My Music", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions =  {
                    IconButton(onClick = { navController.navigate(SearchMyMusicObj)}) {
                        Icon(Icons.Filled.Search, contentDescription = "Search",tint = Color.White)
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

        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
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
                },
                containerColor = Color.Transparent,
                divider = {},
            ) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
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
                modifier = Modifier.fillMaxSize().navigationBarsPadding().padding( top = 8.dp, bottom = 125.dp)
            ) { page ->
                if(!hasPermissions(context as ComponentActivity, Manifest.permission.RECORD_AUDIO)){
                    PermissionHandler(
                        permissions = listOf(
                            PermissionModel(
                                permission = "android.permission.READ_MEDIA_AUDIO",
                                maxSDKVersion = Int.MAX_VALUE,
                                minSDKVersion = 33,
                                rational = "Access to audios is required"
                            )
                        ),
                        askPermission = true
                    )
                }
                when (page) {
                    0 -> SongsLazyColumn(
                        songResponseList,
                        playerViewModel,
                        modifier = Modifier.pullRefresh(pullRefreshState),
                    )

                    1 -> AlbumsLazyVGrid(songsByAlbum)
                    2 -> ArtistsLazyColumn(songsByArtist)
                    3 -> LocalGenresScreen()
                }
            }

        }

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
