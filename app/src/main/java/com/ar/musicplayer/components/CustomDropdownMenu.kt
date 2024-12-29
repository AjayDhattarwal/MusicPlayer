package com.ar.musicplayer.components

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.ar.musicplayer.R


@Composable
fun PlayerDropDownMenu(
    expended: Boolean,
    onDismissRequest: () -> Unit,
    addToPlaylist: () -> Unit
){
        DropdownMenu(
            expanded = expended,
            onDismissRequest = {onDismissRequest()},
            modifier = Modifier.background(Color.Black)
        ) {
            DropdownMenuItem(
                text = { Text(text = "View Album") },
                onClick = { /*TODO*/ },
                leadingIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_album),
                        contentDescription = "album"
                    )
                },
                colors = MenuDefaults.itemColors(
                    textColor = Color.White,
                    disabledTextColor = Color.White,
                    leadingIconColor = Color.White
                )
            )
            DropdownMenuItem(
                text = { Text(text = "Add to Playlist") },
                onClick = addToPlaylist,
                leadingIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_playlist_add),
                        contentDescription = "Add to Playlist"
                    )
                },
                colors = MenuDefaults.itemColors(
                    textColor = Color.White,
                    disabledTextColor = Color.White,
                    leadingIconColor = Color.White
                )
            )
            DropdownMenuItem(
                text = { Text(text = "Sleep Timer") },
                onClick = { /*TODO*/ },
                leadingIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_av_timer),
                        contentDescription = "Sleep timer"
                    )
                },
                colors = MenuDefaults.itemColors(
                    textColor = Color.White,
                    disabledTextColor = Color.White,
                    leadingIconColor = Color.White
                )
            )
            DropdownMenuItem(
                text = { Text(text = "Song Info", color = Color.White) },
                onClick = { /*TODO*/ },
                leadingIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_info),
                        contentDescription = "Song Info"
                    )
                },
                colors = MenuDefaults.itemColors(
                    textColor = Color.White,
                    disabledTextColor = Color.White,
                    leadingIconColor = Color.White
                )
            )
        }


}


@Composable
fun InfoDropdownMenu(expended: Boolean, onDismissRequest: () -> Unit){
    DropdownMenu(
        expanded = expended,
        onDismissRequest = {onDismissRequest()},
        modifier = Modifier.background(Color.Black)
    ) {
        DropdownMenuItem(
            text = { Text(text = "Add To Library") },
            onClick = { /*TODO*/ },
            leadingIcon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_library_add),
                    contentDescription = "add to Library"
                )
            },
            colors = MenuDefaults.itemColors(
                textColor = Color.White,
                disabledTextColor = Color.White,
                leadingIconColor = Color.White
            )
        )

        DropdownMenuItem(
            text = { Text(text = "Add To Queue") },
            onClick = { /*TODO*/ },
            leadingIcon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_queue),
                    contentDescription = "Add tp Queue"
                )
            },
            colors = MenuDefaults.itemColors(
                textColor = Color.White,
                disabledTextColor = Color.White,
                leadingIconColor = Color.White
            )
        )
    }


}


@Composable
fun PlaylistDropdownMenu(
    expended: Boolean,
    onDismissRequest: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    DropdownMenu(
        expanded = expended,
        onDismissRequest = { onDismissRequest() },
        modifier = Modifier.background(Color.Black)
    ) {
        DropdownMenuItem(
            text = { Text(text = "Edit") },
            onClick = onEdit,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Playlist"
                )
            },
            colors = MenuDefaults.itemColors(
                textColor = Color.White,
                disabledTextColor = Color.White,
                leadingIconColor = Color.White
            )
        )

        DropdownMenuItem(
            text = { Text(text = "Delete") },
            onClick =  onDelete ,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Playlist"
                )
            },
            colors = MenuDefaults.itemColors(
                textColor = Color.White,
                disabledTextColor = Color.White,
                leadingIconColor = Color.White
            )
        )
    }


}

