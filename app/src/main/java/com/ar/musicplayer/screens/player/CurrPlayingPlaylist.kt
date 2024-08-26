package com.ar.musicplayer.screens.player

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.components.player.NextPlaylist
import com.ar.musicplayer.viewmodel.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun CurrPlayingPlaylist(){
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val lazyListState = rememberLazyListState()
    val currentIndex by playerViewModel.currentIndex.collectAsState(0)
    val playlist by playerViewModel.playlist.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState(false)
    val currentSong by playerViewModel.currentSong.collectAsState()

    LaunchedEffect(Unit) {
        lazyListState.scrollToItem(currentIndex)
    }

    Scaffold(
        containerColor = Color.Transparent,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            state = lazyListState,
        ) {
            itemsIndexed(playlist ?: emptyList()){ index, item ->
                NextPlaylist(
                    songResponse = item,
                    isPlaying = isPlaying,
                    currentSong = {
                        currentSong
                    },
                    onClick = { playerViewModel.changeSong(index) }
                )

            }
        }
    }
}