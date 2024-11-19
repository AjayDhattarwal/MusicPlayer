package com.ar.musicplayer.components.mix


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.ar.musicplayer.components.library.PLAYLIST_ACTIONS
import com.ar.musicplayer.components.library.PlaylistDialog
import com.ar.musicplayer.utils.roomdatabase.playlistdb.RoomPlaylist


@Composable
fun PlaylistSelectionSheet(
    playlists: List<RoomPlaylist>,
    onPlaylistSelected: (List<String>) -> Unit,
    onCreatePlaylist: (String) -> Unit // Callback to create a new playlist
) {
    val selectedPlaylists = remember { mutableStateListOf<String>() }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    Column(Modifier.padding(start = 16.dp, end = 16.dp)){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,

        ) {
            Text(
                text = "Select Playlists",
                fontSize = 20.sp,
            )
            Button(
                onClick = {
                    onPlaylistSelected(selectedPlaylists.toList())
                },
                modifier = Modifier
            ) {
                Text(text = "Done")
            }
        }
        Button(
            onClick = { showCreatePlaylistDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Create")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Create Playlist")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = Color.LightGray)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            playlists.forEach { playlist ->
                val isSelected = selectedPlaylists.contains(playlist.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isSelected) {
                                selectedPlaylists.remove(playlist.id)
                            } else {
                                selectedPlaylists.add(playlist.id)
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(playlist.image),
                        contentDescription = "playlist image",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(5))
                            .background(Color.LightGray.copy(0.7f))
                    )

                    Text(
                        text = playlist.name,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp, end = 8.dp)
                    )

                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            if (isSelected) {
                                selectedPlaylists.remove(playlist.id)
                            } else {
                                selectedPlaylists.add(playlist.id)
                            }
                        }
                    )
                }
                Divider(color = Color.LightGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

        }

    }

    PlaylistDialog(
        title = "Create Playlist",
        placeholder = "Enter Playlist Name",
        action = PLAYLIST_ACTIONS.CREATE,
        onDismissRequest = {
            showCreatePlaylistDialog = false
        },
        showDialog = showCreatePlaylistDialog,
        returnedText = { string , action  ->
             if(action == PLAYLIST_ACTIONS.CREATE && string.isNotEmpty()){
                onCreatePlaylist(string)
            }
        },
    )

}