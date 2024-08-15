package com.ar.musicplayer.screens.library.components.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.data.models.SongResponse

@Composable
fun HistorySongList(
    songResponseList: List<Pair<Int?, SongResponse>>,
    playerViewModel: PlayerViewModel,
    favViewModel: FavoriteViewModel,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn {
            items(songResponseList.toList()) {(id,songResponse) ->
                HistorySongItem(
                    songResponse = songResponse,
                    onTrackSelect = {
                        if(id != null){
//                            lastSessionViewModel.onEvent(LastSessionEvent.DeleteHistoryById(id))
                        }
                    },
                    onClick = {
                        playerViewModel.setNewTrack(
                            songResponse
                        )
                    }
                )
            }
            item{
                Spacer(modifier = Modifier.height(125.dp))
            }
        }
    }
}

