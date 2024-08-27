package com.ar.musicplayer.screens.player

import androidx.annotation.OptIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.components.modifier.DragDropState
import com.ar.musicplayer.components.modifier.DraggableItem
import com.ar.musicplayer.components.modifier.SwipeToDeleteContainer
import com.ar.musicplayer.components.modifier.dragContainer
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

    val dragDropState =
        rememberDragDropState(lazyListState) { fromIndex, toIndex ->
            playerViewModel.replaceIndex(
                add = toIndex,
                remove = fromIndex
            )
        }

    LaunchedEffect(Unit) {
        lazyListState.scrollToItem(currentIndex)
    }

    Scaffold(
        containerColor = Color.Transparent,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .dragContainer(dragDropState),
            state = lazyListState,
        ) {
            itemsIndexed(
                playlist,
                key = { index, item -> item.id + index }
            ){ index, item ->

                DraggableItem(dragDropState, index) { isDragging ->

                    SwipeToDeleteContainer(
                        item = item,
                        onRemove = {
                            playerViewModel.removeTrack(index)
                        }
                    ) {
                        NextPlaylist(
                            songResponse = item,
                            isPlaying = isPlaying,
                            showAnim = currentIndex == index,
                            onClick = { playerViewModel.changeSong(index) },
                            modifier = Modifier
                        )
                    }
                }


            }
        }
    }
}

@Composable
fun rememberDragDropState(lazyListState: LazyListState, onMove: (Int, Int) -> Unit): DragDropState {
    val scope = rememberCoroutineScope()
    val state =
        remember(lazyListState) {
            DragDropState(state = lazyListState, onMove = onMove, scope = scope)
        }
    LaunchedEffect(state) {
        while (true) {
            val diff = state.scrollChannel.receive()
            lazyListState.scrollBy(diff)
        }
    }
    return state
}