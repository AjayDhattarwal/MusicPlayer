package com.ar.musicplayer.screens.search

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ar.musicplayer.components.mix.SearchAlbumsLazyVGrid
import com.ar.musicplayer.components.mix.SearchArtistResults
import com.ar.musicplayer.components.mix.SearchPlaylistLazyVGrid
import com.ar.musicplayer.components.mix.SongsLazyColumn
import com.ar.musicplayer.components.search.SearchBar
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.data.models.Album
import com.ar.musicplayer.data.models.Artist
import com.ar.musicplayer.data.models.ArtistResult
import com.ar.musicplayer.data.models.InfoScreenModel
import com.ar.musicplayer.data.models.Playlist
import com.ar.musicplayer.data.models.PlaylistResponse
import com.ar.musicplayer.data.models.Song
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.TopSearchResults
import com.ar.musicplayer.data.models.toInfoScreenModel
import com.ar.musicplayer.navigation.ArtistInfoScreenObj
import com.ar.musicplayer.navigation.InfoScreenObj
import com.ar.musicplayer.screens.info.InfoScreen
import com.ar.musicplayer.viewmodel.SearchResultViewModel
import kotlinx.serialization.json.Json

@OptIn(UnstableApi::class)
@SuppressLint("ClickableViewAccessibility")
@Composable
fun SearchScreen(
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    background: Brush
){
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current


    val context = LocalContext.current

    val searchViewModel: SearchResultViewModel = viewModel()

    val isError by searchViewModel.isError.collectAsState()
    val isSearchForResult by searchViewModel.isSearching.collectAsState()
    val searchText by searchViewModel.searchText.collectAsState()
    val trendingSearchResult by searchViewModel.trendingSearchResults.collectAsState()

    val topSearchResults by searchViewModel.topSearchResults.collectAsState()
    val searchResults by searchViewModel.searchSongResults.collectAsState()
    val searchAlbumsResults by searchViewModel.searchAlbumsResults.collectAsState()
    val searchArtistResults by searchViewModel.searchArtistResults.collectAsState()
    val searchPlaylistResults by searchViewModel.searchPlaylistResults.collectAsState()


    val searchType = listOf(
        "Top",
        "Songs",
        "Artists",
        "Playlists",
        "Albums"
    )
    var selectedType by rememberSaveable {
        mutableStateOf("Top")
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(background)
        .clickable { focusManager.clearFocus() }
    ){

        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            searchResultViewModel = searchViewModel,
            keyboardController = keyboardController,
        )



        if(searchText.isBlank()){
            Text(
                text = "Trending",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp)
            )
            LazyColumn {
                items(trendingSearchResult ?: emptyList()) { item ->
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
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )

                        val songResponse = SongResponse(
                            id = item.id,
                            title = item.title,
                            subtitle = item.subtitle,
                            type = item.type,
                            image = item.image,
                            permaUrl = item.permaUrl
                        )

                        Column(
                            modifier = Modifier
                                .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                                .weight(1f)
                                .clickable {
                                    focusManager.clearFocus()
                                    if(item.type == "song"){
                                        playerViewModel.setNewTrack(songResponse)
                                    }
                                }
                        ) {
                            Text(
                                text = item.title ?: "null",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 2.dp),
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.subtitle ?: "unknown",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                        }


                        IconButton(onClick = { /* Handle menu button click */ }) {
                            Icon(
                                if(item.subtitle == "song") Icons.Default.MoreVert else Icons.Default.KeyboardArrowRight,
                                contentDescription = "More",
                                tint = Color.White
                            )
                        }
                    }
                }
                item{
                    Spacer(modifier = Modifier.height(125.dp))
                }
            }
        } else{

            LazyRow (modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp)){
                items(searchType){ searchType ->
                    Box(
                        modifier = Modifier
                            .padding(5.dp)
                            .border(
                                width = 1.dp,
                                color = Color.Gray,
                                shape = RoundedCornerShape(percent = 50)
                            )
                            .background(
                                if (selectedType == searchType) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(percent = 50)
                            )
                            .clickable {
                                focusManager.clearFocus()
                                selectedType = searchType
                            }
                    ) {
                        Text(
                            text = searchType,
                            modifier = Modifier
                                .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp),
                            fontSize = 14.sp,
                            color = if(selectedType == searchType) Color.Black else Color.White
                        )
                    }
                }
            }

            if(isSearchForResult){
                Box(
                    modifier = Modifier.fillMaxSize().clickable { focusManager.clearFocus() },
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedType) {
                    "Songs" -> {
                        SongsLazyColumn(
                            songResponse = searchResults,
                            playerViewModel = playerViewModel,
                        )
                    }

                    "Artists" -> {
                        SearchArtistResults(
                            artistResults = searchArtistResults,
                            onClick = { artist ->
                                val senderData = Json.encodeToString(Artist.serializer(), artist)
                                navController.navigate(
                                    ArtistInfoScreenObj(senderData)
                                )
                            }
                        )
                    }

                    "Playlists" -> {
                        SearchPlaylistLazyVGrid(
                            playlistList = searchPlaylistResults,
                            onClick = { playlistResponse ->
                                val senderData = Json.encodeToString(InfoScreenModel.serializer(), playlistResponse.toInfoScreenModel())
                                navController.navigate(
                                    InfoScreenObj(senderData)
                                )
                            }
                        )
                    }

                    "Albums" -> {
                        SearchAlbumsLazyVGrid(
                            albumList = searchAlbumsResults,
                            onClick = { album ->
                                val senderData = Json.encodeToString(InfoScreenModel.serializer(), album.toInfoScreenModel())
                                navController.navigate(
                                    InfoScreenObj(senderData)
                                )
                            }
                        )
                    }

                    else -> {
                        TopSearchDisplay(
                            searchResults = topSearchResults,
                            navController = navController,
                            playerViewModel = playerViewModel
                        )
                    }

                }
            }

        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun TopSearchDisplay(
    navController: NavHostController,
    searchResults: TopSearchResults?,
    playerViewModel: PlayerViewModel
){

    LazyColumn {
//        items(searchResults?.artists?.data ?: emptyList()) { item ->
//            val showShimmer = remember { mutableStateOf(true) }
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(start = 10.dp, end = 5.dp, top = 10.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                AsyncImage(
//                    model = item.image,
//                    contentDescription = "image",
//                    modifier = Modifier
//                        .size(50.dp)
//                        .padding(4.dp)
//
//                        .clip(RoundedCornerShape(3.dp)),
//                    onSuccess = { showShimmer.value = false },
//                    contentScale = ContentScale.Crop,
//                    alignment = Alignment.Center
//                )
////                val senderData = Json.encodeToString(ArtistResult.serializer(), item)
//                Column(
//                    modifier = Modifier
//                        .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
//                        .weight(1f)
//                        .clickable {
////                            Log.d("artist","${item}")
////                            navController.navigate(ArtistInfoScreenObj(senderData))
//                        }
//                ) {
//                    Text(
//                        text = item.title ?: "null",
//                        style = MaterialTheme.typography.labelLarge,
//                        color = Color.White,
//                        modifier = Modifier.padding(bottom = 2.dp),
//                        maxLines = 1,
//                        softWrap = true,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                    Text(
//                        text = item.type ?: "unknown",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = Color.Gray,
//                        maxLines = 1,
//                        softWrap = true,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
//
//                IconButton(onClick = { /* Handle menu button click */ }) {
//                    Icon(
//                        if (item.type == "song") Icons.Default.MoreVert else Icons.Default.KeyboardArrowRight,
//                        contentDescription = "More",
//                        tint = Color.White
//                    )
//                }
//            }
//        }

        items(searchResults?.songs?.data ?: emptyList()) { item ->
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
                val songResponse = SongResponse(
                    id = item.id,
                    title = item.title,
                    subtitle = item.subtitle,
                    type = item.type,
                    image = item.image,
                    permaUrl = item.permaUrl
                )
                val senderData = Json.encodeToString(Song.serializer(), item)
                Column(
                    modifier = Modifier
                        .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                        .weight(1f)
                        .clickable {
                            if(item.type == "song"){
                                playerViewModel.setNewTrack(songResponse)
                            }
                        }
                ) {
                    Text(
                        text = item.title ?: "null",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 2.dp),
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.type ?: "unknown",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { /* Handle menu button click */ }) {
                    Icon(
                        if (item.type == "song") Icons.Default.MoreVert else Icons.Default.KeyboardArrowRight,
                        contentDescription = "More",
                        tint = Color.White
                    )
                }
            }
        }
        items(searchResults?.albums?.data ?: emptyList()) { item ->
            val senderData = Json.encodeToString(InfoScreenModel.serializer(), item.toInfoScreenModel())
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
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )

                Column(
                    modifier = Modifier
                        .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                        .weight(1f)
                        .clickable {
                            navController.navigate(
                                InfoScreenObj(senderData)
                            )
                        }
                ) {
                    Text(
                        text = item.title ?: "null",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 2.dp),
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.type ?: "unknown",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { /* Handle menu button click */ }) {
                    Icon(
                        if (item.type == "song") Icons.Default.MoreVert else Icons.Default.KeyboardArrowRight,
                        contentDescription = "More",
                        tint = Color.White
                    )
                }
            }
        }
        items(searchResults?.playlists?.data ?: emptyList()) { item ->

            val data = item.toInfoScreenModel()
            val senderData = Json.encodeToString(InfoScreenModel.serializer(), data)
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
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
                Column(
                    modifier = Modifier
                        .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                        .weight(1f)
                        .clickable {
                            navController.navigate(
                                InfoScreenObj(senderData)
                            )
                        }
                ) {
                    Text(
                        text = item.title ?: "null",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 2.dp),
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.type ?: "unknown",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { /* Handle menu button click */ }) {
                    Icon(
                        if (item.type == "song") Icons.Default.MoreVert else Icons.Default.KeyboardArrowRight,
                        contentDescription = "More",
                        tint = Color.White
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(125.dp))
        }
    }
}



