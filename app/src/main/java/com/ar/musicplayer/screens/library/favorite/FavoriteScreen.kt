package com.ar.musicplayer.screens.library.favorite

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.data.models.SongResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    favViewModel: FavoriteViewModel,
) {

    val songResponseList by favViewModel.favSongList.observeAsState()
    val scope = rememberCoroutineScope()
    val titles = listOf("Songs", "Albums", "Artists", "Genres")
    val pagerState = rememberPagerState(initialPage = 0,pageCount = {titles.size})
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Favorite Songs", color = Color.White)},
                navigationIcon = {
                    IconButton(onClick = {navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions =  {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Download, contentDescription = "Download",tint = Color.White)
                    }
                    IconButton(onClick = {  }) {
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
        containerColor = Color.Black
    ) { innerpadding->
        Column(
            modifier = Modifier
                .padding(innerpadding)
                .fillMaxSize()
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
                titles.forEachIndexed{ index,title ->
                    Tab(
                        selected = pagerState.currentPage == index ,
                        onClick = { scope.launch {
                            pagerState.animateScrollToPage(index)
                        } },
                        content = { Text(text = title, color = Color.LightGray, modifier = Modifier.padding(bottom = 5.dp)) },
                        selectedContentColor = Color.White,
                    )
                }
            }
            Log.d("size","size in fav screen:   ${songResponseList?.size}")
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->

                when (page) {
                    0 -> songResponseList?.let { SongsScreen(it,playerViewModel) }
                    1 -> AlbumsScreen()
                    2 -> ArtistsScreen()
                    3 -> GenresScreen()
                }
            }

        }
    }
}

@Composable
fun SongsScreen(
    songResponse: List<SongResponse>,
    playerViewModel: PlayerViewModel,
) {
    val context = LocalContext.current
    val showShimmer = remember { mutableStateOf(true) }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn {
            items(songResponse) { songResponse ->
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
                                playerViewModel.setNewTrack(
                                    songResponse
                                )
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

                    IconButton(onClick = { /* Handle download button click */ }) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = "Download",
                            tint = Color.White
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
            item{
                Spacer(modifier = Modifier.height(125.dp))
            }
        }
    }
}

@Composable
fun AlbumsScreen() {
    Text("Albums content goes here",color = Color.White)
}

@Composable
fun ArtistsScreen() {
    Text("Artists content goes here", color = Color.White)
}

@Composable
fun GenresScreen() {
    Text("Genres content goes here", color = Color.White)
}


@Preview
@Composable
fun PreviewFavScreen(){
}