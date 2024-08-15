package com.ar.musicplayer.components.mix

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.viewmodel.PlayerViewModel


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsLazyColumn(
    songResponse: List<SongResponse>,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val showShimmer = remember { mutableStateOf(true) }
    val focusManager = LocalFocusManager.current
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn {
            items(songResponse) { songResponse ->
                val artistName = songResponse.moreInfo?.artistMap?.artists?.distinctBy { it.name }?.joinToString(", "){it.name.toString()}
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 5.dp, top = 10.dp)
                        .clickable {
                            focusManager.clearFocus()
                            playerViewModel.setNewTrack(songResponse)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    AsyncImage(
                        model = songResponse.image,
                        contentDescription = "image",
                        modifier = Modifier
                            .size(50.dp)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        onSuccess = { showShimmer.value = false },
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )

                    Column(
                        modifier = Modifier
                            .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                            .weight(1f)

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
                            text = artistName?: "unknown",
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
        }
    }
}