package com.ar.musicplayer.components.mix

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ar.musicplayer.data.models.Album
import com.ar.musicplayer.data.models.InfoScreenModel
import com.ar.musicplayer.data.models.PlaylistResponse
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.toInfoScreenModel


@Composable
fun AlbumsLazyVGrid(songsByAlbum: Map<String, List<SongResponse>> ) {
    val list by remember {
        derivedStateOf {
            songsByAlbum.toList()
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2) ,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp),
    ) {
        items(list){ (title,songs) ->
            val artists = songs.first().moreInfo?.artistMap?.artists?.distinctBy { it.name }?.joinToString(", "){it.name.toString()}
            VGridItem(
                title = title,
                image = songs.first().image.toString(),
                subtitle = artists.toString(),
                onClick = {}
            )
        }
        item {
            Spacer(modifier = Modifier.height(125.dp))
        }
    }

}


@Composable
fun SearchAlbumsLazyVGrid(albumList: List<Album>, onClick: (InfoScreenModel) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2) ,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp),
        ) {
            items(albumList){
                VGridItem(
                    title = it.title.toString(),
                    image = it.image.toString(),
                    subtitle = it.subtitle.toString(),
                    onClick = {onClick(it.toInfoScreenModel())}
                )
            }
            item {
                Spacer(modifier = Modifier.height(350.dp))
            }
        }
    }
}


@Composable
fun SearchPlaylistLazyVGrid(playlistList: List<PlaylistResponse>, onClick: (InfoScreenModel) -> Unit ) {
    Column(modifier = Modifier.fillMaxSize()) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2) ,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp),
        ) {
            items(playlistList){
                VGridItem(
                    title = it.title.toString(),
                    image = it.image.toString(),
                    subtitle = it.subtitle.toString(),
                    onClick = { onClick(it.toInfoScreenModel()) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(350.dp))
            }
        }
    }
}

@Composable
fun VGridItem(
    title: String,
    image: String,
    subtitle: String,
    onClick: () -> Unit,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 10.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        AsyncImage(
            model = image,
            contentDescription = "image",
            modifier = Modifier
                .size(150.dp)

                .clip(RoundedCornerShape(5)),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        Column(
            modifier = Modifier
                .padding(top = 5.dp, bottom = 5.dp)
                .width(150.dp),
            horizontalAlignment = Alignment.Start

        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 2.dp),
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle ,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

}

