package com.ar.musicplayer.screens.library.mymusic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ar.musicplayer.screens.player.PlayerViewModel
import com.ar.musicplayer.components.search.SearchTopAppBar
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.screens.library.viewmodel.LocalSongsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMyMusic(
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    localSongsViewModel: LocalSongsViewModel
) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    val songResponseList by localSongsViewModel.songResponseList.collectAsState()
    val searchResults by remember {
        derivedStateOf {
            searchSongs(songResponseList, searchText)
        }
    }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        SearchTopAppBar(
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            onCloseClicked = {
                if (searchText == "") {
                    navController.navigateUp()
                } else {
                    searchText = ""
                    scope.launch {
                        keyboardController?.hide()
                    }
                }
            },
            keyboardController = keyboardController,
            modifier = Modifier
                .statusBarsPadding()
                .padding(10.dp)
        )
        if (searchText.isEmpty()) {
            Text(
                text = "Search History",
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            SearchResultsOfMyMusic(searchResults,playerViewModel)
        }
    }
}

@Composable
fun SearchResultsOfMyMusic(searchResults: List<SongResponse>, playerViewModel: PlayerViewModel) {
    LazyColumn {
        items(searchResults) { item ->
            val artistName = item.moreInfo?.artistMap?.artists?.distinctBy { it.name }?.joinToString(", "){it.name.toString()}
            val showShimmer = remember { mutableStateOf(true) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 5.dp, top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = item.image,
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
                            playerViewModel.setNewTrack(item)
                        }
                ) {

                    Text(
                        text = item.title.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 2.dp),
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = artistName.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )

                }

                IconButton(onClick = { /* Handle menu button click */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color.White
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(125.dp).navigationBarsPadding())
        }
    }
}


fun searchSongs(songs: List<SongResponse>, query: String): List<SongResponse> {
    val lowercaseQuery = query.lowercase()
    return songs.filter { song ->
        song.title?.lowercase( )?.contains( lowercaseQuery)  == true ||
        song.moreInfo?.artistMap?.artists?.any { artist ->
            artist.name?.lowercase( )?.contains( lowercaseQuery)  == true } == true ||
            song.moreInfo?.album?.lowercase( )?. contains( lowercaseQuery)  == true }
}


@Preview(showBackground = true)
@Composable
fun SearchMyMusicPreview() {
    val viewModel = viewModel<LocalSongsViewModel>()
    val playerViewModel = viewModel<PlayerViewModel>()
    SearchMyMusic(
        navController = NavHostController(LocalContext.current),
        playerViewModel = playerViewModel,
        localSongsViewModel = viewModel
    )
}