package com.ar.musicplayer.components

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun PlayerDropDownMenu(expended: Boolean, onDismissRequest: () -> Unit){
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
                        imageVector = Icons.Default.Album,
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
                onClick = { /*TODO*/ },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
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
                        imageVector = Icons.Default.AvTimer,
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
                        imageVector = Icons.Default.Info,
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
                    imageVector = Icons.Default.LibraryAdd,
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
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
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
