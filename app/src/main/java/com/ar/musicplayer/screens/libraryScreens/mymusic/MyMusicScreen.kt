package com.ar.musicplayer.screens.libraryScreens.mymusic

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import com.ar.musicplayer.di.permission.PermissionHandler
import com.ar.musicplayer.di.permission.PermissionModel
import com.ar.musicplayer.di.permission.hasPermissions
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.models.PlaylistResponse
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.navigation.DetailsScreenObj
import com.ar.musicplayer.navigation.SearchMyMusicObj
import com.ar.musicplayer.viewmodel.ImageColorGradient
import com.ar.musicplayer.viewmodel.LocalSongsViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


@OptIn(ExperimentalMaterialApi::class)
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

    val pullToRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            localSongsViewModel.fetchLocalSongs()
        }
    )


    val context = LocalContext.current
    val imageColorGradient: ImageColorGradient = viewModel()
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
                backgroundColor = Color.Transparent,
                modifier = Modifier.padding(bottom = 10.dp).statusBarsPadding()
            )

        },
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerpadding ->

        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerpadding)
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
                if(!hasPermissions(context as ComponentActivity, android.Manifest.permission.RECORD_AUDIO)){
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
                    0 -> LocalSongsScreen(
                        songResponseList,
                        playerViewModel,
                        favViewModel,
                        imageColorGradient,
                        modifier = Modifier.pullRefresh(pullToRefreshState),
                        localSongsViewModel,
                        pullToRefreshState
                    )

                    1 -> LocalAlbumsScreen(songsByAlbum, navController)
                    2 -> LocalArtistsScreen(songsByArtist)
                    3 -> LocalGenresScreen()
                }
            }

        }

    }

}



@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LocalSongsScreen(
    songResponse: List<SongResponse>,
    playerViewModel: PlayerViewModel,
    favViewModel: FavoriteViewModel,
    imageColorViewModel: ImageColorGradient,
    modifier: Modifier = Modifier,
    localSongsViewModel: LocalSongsViewModel,
    pullToRefreshState: PullRefreshState
) {
    val isLoading by localSongsViewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val showShimmer = remember { mutableStateOf(true) }
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn {
            items(songResponse) { songResponse ->
                val artistName = songResponse.moreInfo?.artistMap?.artists?.distinctBy { it.name }?.joinToString(", "){it.name.toString()}
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 5.dp, top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    AsyncImage(
                        model = songResponse.image,
                        contentDescription = "image",
                        modifier = Modifier
                            .size(50.dp)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(3.dp)),
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
                            text = artistName?: "unknown",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            maxLines = 1,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis
                        )
                    }


                    IconButton(onClick = { /* Handle menu button click */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                }
            }
        }
        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}




@Composable
fun LocalAlbumsScreen(songsByAlbum: Map<String, List<SongResponse>>, navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize()) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2) ,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp),
        ) {
            songsByAlbum.forEach { (album, songs) ->
                item {
                    AlbumSection(album, songs, navController = navController )
                }
            }
        }
    }
}

@Composable
fun AlbumSection(album: String, songs: List<SongResponse>,navController: NavHostController) {
    val context = LocalContext.current
    val showShimmer = remember { mutableStateOf(true) }

    val artists = songs.first().moreInfo?.artistMap?.artists?.distinctBy { it.name }?.joinToString(", "){it.name.toString()}

    val playlistResponse = PlaylistResponse(
        title = album,
        image = songs.first().image,
        list = songs
    )

    val serialized = remember { Json.encodeToString(PlaylistResponse.serializer(), playlistResponse) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 10.dp)
            .clickable { navController.navigate(DetailsScreenObj(playlistResponse = serialized))  },
        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        AsyncImage(
            model = songs.first().image,
            contentDescription = "image",
            modifier = Modifier
                .size(150.dp)
                
                .clip(RoundedCornerShape(5)),
            onSuccess = { showShimmer.value = false },
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        Column(
            modifier = Modifier
                .padding(top = 5.dp, bottom = 5.dp)
                .width(150.dp),
            horizontalAlignment = Alignment.Start

        ) {
            Text(
                text = album,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 2.dp),
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = artists.toString() ,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

}



@Composable
fun LocalArtistsScreen(songsByArtist: Map<String, List<SongResponse>>) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            songsByArtist.forEach { (artist, songs) ->
                item {
                    ArtistSection(artist, songs)
                }
            }
        }
    }

}

@Composable
fun ArtistSection(artist: String, songs: List<SongResponse>) {
    val context = LocalContext.current
    val showShimmer = remember { mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 5.dp, top = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = songs.first().image,
            contentDescription = "image",
            modifier = Modifier
                .size(50.dp)
                .padding(4.dp)
                
                .clip(CircleShape),
            onSuccess = { showShimmer.value = false },
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        Text(
            text = artist,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier.padding(start = 10.dp, bottom = 2.dp, end = 10.dp),
            maxLines = 1,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )

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

//•