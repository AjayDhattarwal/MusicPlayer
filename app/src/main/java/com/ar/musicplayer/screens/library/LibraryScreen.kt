package com.ar.musicplayer.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ar.musicplayer.R
import com.ar.musicplayer.navigation.FavoriteScreenObj
import com.ar.musicplayer.navigation.ListeningHisScreenObj
import com.ar.musicplayer.navigation.MyMusicScreenObj
import com.ar.musicplayer.navigation.PlaylistFetchScreenObj
import com.ar.musicplayer.navigation.SettingsScreenObj

@Composable
fun  LibraryScreen(
    onScreenSelect :  (Any) -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding( start = 10.dp, top = 40.dp, end = 10.dp )
    ){
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp), verticalAlignment = Alignment.CenterVertically){
            Text(
                text = "My Library",
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
            IconButton(onClick = { onScreenSelect(SettingsScreenObj) }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clickable {
                    onScreenSelect(FavoriteScreenObj)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_favorite),
                contentDescription = "Liked",
                tint = Color.White,
                modifier = Modifier.padding(10.dp)
            )
            Text(
                text = "Favorite Songs",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp).weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_album),
                contentDescription = "Albums",
                tint = Color.White,
                modifier = Modifier.padding(10.dp)
            )
            Text(
                text = "Albums",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp).weight(1f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clickable {  onScreenSelect(ListeningHisScreenObj) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_history),
                contentDescription = "Last Session",
                tint = Color.White,
                modifier = Modifier.padding(10.dp)
            )
            Text(
                text = "Listening History",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp).weight(1f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clickable {
                    onScreenSelect(MyMusicScreenObj)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_my_music),
                contentDescription = "My Music",
                tint = Color.White,
                modifier = Modifier.padding(10.dp)
            )
            Text(
                text = "My Music",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp).weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_download_done),
                contentDescription = "Downloads",
                tint = Color.White,
                modifier = Modifier.padding(10.dp)
            )
            Text(
                text = "Downloads",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp).weight(1f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clickable {
                    onScreenSelect(PlaylistFetchScreenObj)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_playlist_play),
                contentDescription = "Playlists",
                tint = Color.White,
                modifier = Modifier.padding(10.dp)
            )
            Text(
                text = "Playlists",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp).weight(1f)
            )
        }

    }
}

@Preview
@Composable
fun LibraryScreenPreview(){
    val blackToGrayGradient =
        Brush.verticalGradient(
            colors = listOf(Color(0xFF000000),Color(0xFF161616)),
            startY = Float.POSITIVE_INFINITY,
            endY = 0f
        )
    LibraryScreen{ _-> }
}