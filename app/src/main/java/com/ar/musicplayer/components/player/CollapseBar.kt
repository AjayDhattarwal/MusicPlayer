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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.components.PlayerDropDownMenu
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.screens.player.getStatusBarHeight
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel

@UnstableApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CollapseBar(
    onCollapse: () -> Unit,
    fraction: () -> Float,
    onFavClick: () -> Unit,
    isFavorite: State<Boolean>,
) {

    val currentFraction = fraction()


    val sizeofCollapseBar by animateDpAsState(targetValue = androidx.compose.ui.unit.lerp(
        0.dp,
        30.dp,
        currentFraction
    ), label = ""
    )
    val dynamicPaddingValues by animateDpAsState(targetValue = androidx.compose.ui.unit.lerp(
        0.dp,
        getStatusBarHeight(),
        currentFraction
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
            visible = (currentFraction > 0.7f),
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
                    isFavorite = isFavorite.value,
                    onFavClick = onFavClick
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
