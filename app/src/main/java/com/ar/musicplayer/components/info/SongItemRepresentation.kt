package com.ar.musicplayer.components.info

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ar.musicplayer.R
import com.ar.musicplayer.components.PlayerDropDownMenu
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.sanitizeString
import com.ar.musicplayer.utils.download.DownloadEvent
import com.ar.musicplayer.utils.download.DownloadStatus
import com.ar.musicplayer.utils.download.DownloaderViewModel


@Composable
fun SongItemRepresentation(
    track: SongResponse,
    favViewModel: FavoriteViewModel = hiltViewModel() ,
    downloaderViewModel: DownloaderViewModel,
    onTrackClicked: () -> Unit,
    addToPlaylist: () -> Unit
) {

    val isFavouriteFlow by remember {
        derivedStateOf {
            favViewModel.isFavoriteSong(track.id.toString())
        }
    }

    val isFavourite by isFavouriteFlow.collectAsState(false)

    var isDownloaded by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    val downloadProgress by downloaderViewModel.songProgress.collectAsState()

    val songDownloadStatus by downloaderViewModel.songDownloadStatus.observeAsState(emptyMap())

    val status =
        songDownloadStatus[track.id.toString()] ?: DownloadStatus.NOT_DOWNLOADED

    var inDownloadQueue by remember { mutableStateOf(false) }
    var isMoreExpanded by remember { mutableStateOf(false) }


    LaunchedEffect(status) {
        when (status) {
            DownloadStatus.NOT_DOWNLOADED -> {
                isDownloaded = false
                inDownloadQueue = false
                isDownloading = false
            }
            DownloadStatus.WAITING -> {
                isDownloaded = false
                inDownloadQueue = true
                isDownloading = false
            }
            DownloadStatus.DOWNLOADING -> {
                isDownloaded = false
                inDownloadQueue = true
                isDownloading = true
            }
            DownloadStatus.DOWNLOADED -> {
                isDownloaded = true
                inDownloadQueue = false
                isDownloading = false
            }
            DownloadStatus.PAUSED -> {
                isDownloaded = false
                inDownloadQueue = true
                isDownloading = false
            }
        }
    }

    LaunchedEffect(key1 = track.id) {
        downloaderViewModel.onEvent(DownloadEvent.isDownloaded(track) { it ->
            isDownloaded = it
        })
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

        Box(
            modifier = Modifier
                .size(50.dp)
                .padding(4.dp)
                .clip(RoundedCornerShape(2))
                .background(Color.Gray.copy(alpha = 0.2f))
        ) {
            AsyncImage(
                model = track.image,
                contentDescription = "image",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
            )
        }

        Column(
            modifier = Modifier
                .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                .weight(1f)
        ) {
            Text(
                text = track.title?.sanitizeString().toString(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 2.dp),
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.subtitle?.sanitizeString().toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }

        if(track.type != "Youtube" || track.isYoutube == false){
            IconButton(
                onClick = remember{
                    {
                        if (!isDownloaded) {
                            downloaderViewModel.onEvent(DownloadEvent.downloadSong(track))
                            inDownloadQueue = true
                        }
                    }
                }
            ) {
                if(isDownloading){
                    CircularProgressIndicator(
                        modifier = Modifier,
                        color = Color.LightGray,
                        progress = { downloadProgress / 100f }

                    )
                    Text(text = "${downloadProgress}%", color = Color.White, fontSize = 14.sp)
                }

                else{
                    val downloadIcon =  if(isDownloaded) R.drawable.ic_download_done else if (inDownloadQueue) R.drawable.ic_hourglass_top else R.drawable.ic_download
                    Icon(
                        modifier = Modifier.weight(1f),
                        imageVector = ImageVector.vectorResource(downloadIcon),
                        contentDescription = "Download",
                        tint = Color.White
                    )
                }
            }
        }

        IconButton(
            onClick = remember{
                { favViewModel.onEvent(FavoriteSongEvent.ToggleFavSong(track)) }
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(if(isFavourite) R.drawable.ic_favorite else R.drawable.ic_favorite_border),
                contentDescription = "Like",
                tint = if(isFavourite) Color.Red else Color.White
            )
        }
        IconButton(onClick = { isMoreExpanded = !isMoreExpanded }) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_more_horiz),
                contentDescription = "Menu",
                tint = Color.White
            )
            PlayerDropDownMenu(
                expended = isMoreExpanded,
                onDismissRequest = remember{
                    {
                        isMoreExpanded = false
                    }
                },
                addToPlaylist = addToPlaylist
            )
        }
    }
}


