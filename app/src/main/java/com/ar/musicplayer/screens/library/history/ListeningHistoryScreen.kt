package com.ar.musicplayer.screens.library.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.screens.library.components.history.SimpleSongItem
import com.ar.musicplayer.viewmodel.PlayerViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningHistoryScreen(
    playerViewModel: PlayerViewModel,
    onBackPressed: () -> Unit,
) {
    val songResponseList by playerViewModel.listeningHistory.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Recently played", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn {
                items(
                    songResponseList,
                    key = { item -> item.second.id ?: "" }) { (id, songResponse) ->
                    SimpleSongItem(
                        songResponse = songResponse,
                        onTrackSelect = {
//                        if(id != null){
////                            lastSessionViewModel.onEvent(LastSessionEvent.DeleteHistoryById(id))
//                        }
                        },
                        onClick = remember {
                            { playerViewModel.setNewTrack(songResponse) }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(125.dp))
                }
            }
        }

    }
}

