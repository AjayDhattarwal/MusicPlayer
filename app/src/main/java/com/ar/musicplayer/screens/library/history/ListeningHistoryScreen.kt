package com.ar.musicplayer.screens.library.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.screens.library.components.history.HistorySongList

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningHistoryScreen(
    navController:NavHostController,
    playerViewModel: PlayerViewModel,
    favViewModel: FavoriteViewModel,
) {
    val songResponseList by playerViewModel.listeningHistory.observeAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Recently played", color = Color.White)},
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp()}) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(Color.Black)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            songResponseList?.let {
                HistorySongList(
                    songResponseList = it,
                    playerViewModel = playerViewModel ,
                    favViewModel = favViewModel,
                )
            }
        }

    }
}

