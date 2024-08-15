package com.ar.musicplayer.components.mix

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ar.musicplayer.data.models.Artist
import com.ar.musicplayer.data.models.SongResponse

@Composable
fun ArtistsLazyColumn(songsByArtist: Map<String, List<SongResponse>>) {
    val list = songsByArtist.toList()
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(list){ (artist, songs) ->
                ArtistSection(artist, songs.first().image.toString(), onClick = {})
            }
            item {
                Spacer(modifier = Modifier.height(125.dp))
            }
        }
    }

}

@Composable
fun SearchArtistResults(artistResults: List<Artist>, onClick: (Artist) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(artistResults){ artist ->
                ArtistSection(artist.name.toString(), artist.image.toString(), onClick = {onClick(artist)})
            }
            item {
                Spacer(modifier = Modifier.height(125.dp))
            }
        }
    }
}


@Composable
fun ArtistSection(artist: String, artistImage : String, onClick: () -> Unit) {
    val context = LocalContext.current
    val showShimmer = remember { mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 5.dp, top = 20.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = artistImage,
            contentDescription = "image",
            modifier = Modifier
                .size(50.dp)
                .padding(4.dp)
                .clip(CircleShape),
            onSuccess = { showShimmer.value = false },
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        Text(
            text = artist,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier.padding(start = 10.dp, bottom = 2.dp, end = 10.dp),
            maxLines = 1,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )

    }
}