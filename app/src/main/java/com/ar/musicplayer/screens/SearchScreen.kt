package com.ar.musicplayer.screens

import android.annotation.SuppressLint
import android.widget.FrameLayout
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ar.musicplayer.models.Album
import com.ar.musicplayer.models.ArtistResult
import com.ar.musicplayer.models.Playlist
import com.ar.musicplayer.models.SearchResults
import com.ar.musicplayer.models.Song
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.models.TopSearchResults
import com.ar.musicplayer.navigation.PlayerScreenObj
import com.ar.musicplayer.viewmodel.ImageColorGradient
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.viewmodel.SearchResultViewModel
import kotlinx.serialization.json.Json

@SuppressLint("ClickableViewAccessibility")
@Composable
fun SearchScreen(navController: NavHostController,playerViewModel: PlayerViewModel){
    val keyboardController = LocalSoftwareKeyboardController.current
    val rootView = LocalView.current

    AndroidView(
        factory = { context ->
            FrameLayout(context).apply {
                setOnTouchListener { _, _ ->
                    keyboardController?.hide()
                    false
                }
            }
        }
    )

    val context = LocalContext.current
    val blackToGrayGradient =
        Brush.verticalGradient(
            colors = listOf(Color(0xFF000000),Color(0xFF161616)),
            startY = Float.POSITIVE_INFINITY,
            endY = 0f
        )

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
    var selectedType = rememberSaveable {
        mutableStateOf("Top")
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(blackToGrayGradient)){

        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            searchResultViewModel = searchViewModel,
            selectedType = selectedType,
            keyboardController = keyboardController
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
                    val showShimmer = remember { mutableStateOf(true) }
                    val imageColorGradient: ImageColorGradient = viewModel()
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
                                .background(brush = shimmerEffectfun(showShimmer.value))
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

                        Column(
                            modifier = Modifier
                                .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                                .weight(1f)
                                .clickable {
                                    if(item.type == "song"){
                                        playerViewModel.updateCurrentSong(songResponse)
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
                                if (selectedType.value == searchType) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(percent = 50)
                            )
                            .clickable { selectedType.value = searchType }
                    ) {
                        Text(
                            text = searchType,
                            modifier = Modifier
                                .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp),
                            fontSize = 14.sp,
                            color = if(selectedType.value == searchType) Color.Black else Color.White
                        )
                    }
                }
            }

            if(isSearchForResult){
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedType.value) {
                    "Songs" -> {
                        SearchResultDisplay(
                            searchResults = searchResults,
                            navController = navController,
                            playerViewModel = playerViewModel
                        )
                    }

                    "Artists" -> {
                        SearchResultDisplay(
                            searchResults = searchArtistResults,
                            navController = navController,
                            playerViewModel = playerViewModel
                        )
                    }

                    "Playlists" -> {
                        SearchResultDisplay(
                            searchResults = searchPlaylistResults,
                            navController = navController,
                            playerViewModel = playerViewModel
                        )
                    }

                    "Albums" -> {
                        SearchResultDisplay(
                            searchResults = searchAlbumsResults,
                            navController = navController,
                            playerViewModel = playerViewModel
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

@Composable
fun TopSearchDisplay(
    navController: NavHostController,
    searchResults: TopSearchResults?,
    playerViewModel: PlayerViewModel
){

    LazyColumn {
        items(searchResults?.artists?.data ?: emptyList()) { item ->
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
                        .background(brush = shimmerEffectfun(showShimmer.value))
                        .clip(RoundedCornerShape(3.dp)),
                    onSuccess = { showShimmer.value = false },
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
                val senderData = Json.encodeToString(ArtistResult.serializer(), item)
                Column(
                    modifier = Modifier
                        .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                        .weight(1f)
                        .clickable {
//                            navController.navigate(
//                                PlayerScreenObj(senderData)
//                            )
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
                        .background(brush = shimmerEffectfun(showShimmer.value))
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
                                playerViewModel.updateCurrentSong(song = songResponse)
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
                        .background(brush = shimmerEffectfun(showShimmer.value))
                        .clip(RoundedCornerShape(3.dp)),
                    onSuccess = { showShimmer.value = false },
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
                val senderData = Json.encodeToString(Album.serializer(), item)
                Column(
                    modifier = Modifier
                        .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                        .weight(1f)
                        .clickable {
                            navController.navigate(
                                PlayerScreenObj(senderData)
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
                        .background(brush = shimmerEffectfun(showShimmer.value))
                        .clip(RoundedCornerShape(3.dp)),
                    onSuccess = { showShimmer.value = false },
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
                val senderData = Json.encodeToString(Playlist.serializer(), item)
                Column(
                    modifier = Modifier
                        .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                        .weight(1f)
                        .clickable {
                            navController.navigate(
                                PlayerScreenObj(senderData)
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

@Composable
fun SearchResultDisplay(searchResults: SearchResults?, navController: NavHostController,playerViewModel: PlayerViewModel) {
    LazyColumn {
        items(searchResults?.results ?: emptyList()) { item ->

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
                        .background(brush = shimmerEffectfun(showShimmer.value))
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
                            playerViewModel.updateCurrentSong(item)
                        }
                ) {
                    (if(item.type == "artist")item.name else item.title)?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 2.dp),
                            maxLines = 1,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    (if(item.type == "artist") item.type
                        else if(item.type == "playlist") "${capitalizeFirstLetter(item.moreInfo?.language!!)}.${item.moreInfo.songCount} Songs"
                        else item.subtitle)?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            maxLines = 1,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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

fun capitalizeFirstLetter(text: String): String {
    return text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    modifier: Modifier,
    searchResultViewModel: SearchResultViewModel,
    selectedType: MutableState<String>,
    keyboardController: SoftwareKeyboardController?,

    ) {

    val searchText by searchResultViewModel.searchText.collectAsState()

    Row(
        modifier = modifier.padding(top = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = searchText,
            onValueChange =  searchResultViewModel::onSearchTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(percent = 10)
                )
                .onFocusChanged {
                    if(!it.isFocused){
                        keyboardController?.hide()
                    }
                },
            placeholder = { Text(text = "Music, Artists, and Podcasts", fontSize = 14.sp, color = Color.Gray)},
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                )
            },
            trailingIcon = {
                if(searchText.isNotEmpty()){
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "clear",
                        modifier = Modifier.clickable { searchResultViewModel.onSearchTextChange("") }
                    )
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.DarkGray,
                unfocusedTextColor = Color.Gray,
                focusedLeadingIconColor = Color.DarkGray,
                unfocusedLeadingIconColor = Color.Gray,
                focusedTrailingIconColor = Color.DarkGray,
                unfocusedTrailingIconColor = Color.Gray,

            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                }
            ),
            singleLine = true,

        )

    }
}


@Preview
@Composable
fun SearchScreenPreview(){
//    SearchBar(Modifier)
}


