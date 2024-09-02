package com.ar.musicplayer.components.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.components.PlayerDropDownMenu
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.navigation.currentFraction
import com.ar.musicplayer.screens.player.getStatusBarHeight
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel

@UnstableApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CollapseBar(
    bottomSheetState: BottomSheetScaffoldState,
    currentSong: SongResponse,
    playerViewModel: PlayerViewModel,
    favoriteViewModel: FavoriteViewModel,
    onCollapse: () -> Unit,
) {

    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()

    val isFavoriteFlow by remember {
        derivedStateOf {
            favoriteViewModel.isFavoriteSong(currentSong?.id.toString())
        }
    }
    val isFavourite by isFavoriteFlow.collectAsState(false)


    val sizeofCollapseBar by animateDpAsState(targetValue = androidx.compose.ui.unit.lerp(
        0.dp,
        30.dp,
        bottomSheetState.currentFraction
    ), label = ""
    )
    val dynamicPaddingValues by animateDpAsState(targetValue = androidx.compose.ui.unit.lerp(
        0.dp,
        getStatusBarHeight(),
        bottomSheetState.currentFraction
    ), label = ""
    )



    var isMoreExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dynamicPaddingValues.coerceAtLeast(1.dp))
            .height(sizeofCollapseBar),
    ) {
        AnimatedVisibility(
            visible = (bottomSheetState.currentFraction > 0.7f),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row {
                IconButton(onClick = { onCollapse() }) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(
                    Modifier
                        .weight(1f)
                        .height(1.dp))

                FavToggleButton(
                    isFavorite = isFavourite,
                    onFavClick = remember {
                        {
                            if (currentSong?.id != "") {
                                favoriteViewModel.onEvent(FavoriteSongEvent.ToggleFavSong(songResponse = currentSong!!))
                            }

                        }
                    }
                )

                IconButton(onClick = { isMoreExpanded = !isMoreExpanded }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                    PlayerDropDownMenu(
                        expended = isMoreExpanded,
                        onDismissRequest = remember{
                            {
                                isMoreExpanded = false
                            }
                        }
                    )
                }
            }
        }
    }

}
