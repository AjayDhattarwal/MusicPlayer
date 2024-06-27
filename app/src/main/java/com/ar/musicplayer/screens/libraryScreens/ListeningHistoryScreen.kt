package com.ar.musicplayer.screens.libraryScreens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionViewModel
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.screens.shimmerEffectfun
import com.ar.musicplayer.viewmodel.PlayerViewModel

@Composable
fun ListeningHistoryScreen(
    playerViewModel: PlayerViewModel,
    favViewModel: FavoriteViewModel,
    lastSessionViewModel: LastSessionViewModel
) {
    val songResponseList by lastSessionViewModel.listeningHistory.observeAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Recently played", color = Color.White)},
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                backgroundColor = Color.Transparent,
                modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            songResponseList?.let {
                ListeningSongLazy(
                    songResponseList = it,
                    playerViewModel = playerViewModel ,
                    favViewModel = favViewModel
                )
            }
        }

    }
}

@Composable
fun ListeningSongLazy(
    songResponseList: List<SongResponse>,
    playerViewModel: PlayerViewModel,
    favViewModel: FavoriteViewModel
) {
    val context = LocalContext.current
    val showShimmer = remember { mutableStateOf(true) }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn {
            items(songResponseList) { songResponse ->
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
                                playerViewModel.starter.value = false
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
                            text = "${songResponse.type} . ${songResponse.moreInfo?.artistMap?.artists?.get(0)?.name}",
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
            item{
                Spacer(modifier = Modifier.height(125.dp))
            }
        }
    }
}