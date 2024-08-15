package com.ar.musicplayer.components.info

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveFromQueue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.utils.download.DownloadEvent
import com.ar.musicplayer.utils.download.DownloaderViewModel
import kotlinx.coroutines.launch


@Composable
fun SongItemRepresentation(
    track: SongResponse,
    index: Int,
    favViewModel: FavoriteViewModel,
    downloaderViewModel: DownloaderViewModel,
    onTrackClicked: () -> Unit
)
{
    val coroutineScope = rememberCoroutineScope()

    val isDownloaded = remember { mutableStateOf(false) }
    val isDownloading = remember { mutableStateOf(false) }
    val downloadProgress by downloaderViewModel.songProgress.observeAsState()
    val currentDownloading by downloaderViewModel.currentDownloading.observeAsState()
    var inDownloadQueue by remember { mutableStateOf(false) }
    val isFavouriteFlow by remember {
        derivedStateOf {
            favViewModel.isFavoriteSong(track.id.toString())
        }
    }

    val isFavourite by isFavouriteFlow.collectAsState(false)


    Log.d("daw",isDownloading.value.toString())
    LaunchedEffect(currentDownloading) {
        if(currentDownloading == track){
            isDownloading.value =  true
            isDownloaded.value = true
            inDownloadQueue = false
        }
        if(currentDownloading == null && downloadProgress == 0 ){
            isDownloading.value = false
        }
        else if(currentDownloading != track  ){
            isDownloading.value = false
        }
    }

    LaunchedEffect(key1 = track.id) {
        downloaderViewModel.isAllReadyDownloaded(track) { it ->
            isDownloaded.value = it
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 5.dp, top = 10.dp)
            .clickable {
                onTrackClicked()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(modifier = Modifier.size(50.dp)){
            AsyncImage(
                model = track.image,
                contentDescription = "image",
                modifier = Modifier
                    .size(50.dp)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(2)),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )
        }

        Column(
            modifier = Modifier
                .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                .weight(1f)
        ) {
            Text(
                text = track.title ?: "null",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 2.dp),
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.subtitle ?: "unknown",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = {
                if(!isDownloaded.value){
                    downloaderViewModel.onEvent(DownloadEvent.downloadSong(track))
                    inDownloadQueue = true
                }
            }
        ) {
            if(isDownloading.value){
                CircularProgressIndicator(
                    modifier = Modifier,
                    progress = downloadProgress?.div(100.toFloat()) ?: 0f,
                    color = Color.LightGray
                )
                Text(text = "${downloadProgress}%", color = Color.White, fontSize = 14.sp)
            }

            else{
                Icon(
                    modifier = Modifier.weight(1f),
                    imageVector = if(isDownloaded.value) Icons.Default.DownloadDone else if (inDownloadQueue) Icons.Filled.HourglassTop else Icons.Default.FileDownload,
                    contentDescription = "Download",
                    tint = Color.White
                )
            }
        }

        IconButton(onClick = {
            favViewModel.onEvent(FavoriteSongEvent.ToggleFavSong(track))
        }) {
            Icon(
                imageVector = if(isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                tint = if(isFavourite) Color.Red else Color.White
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


