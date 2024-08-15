package com.ar.musicplayer.components.info.artistInfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ar.musicplayer.components.home.Heading
import com.ar.musicplayer.components.home.HomeScreenRowCard
import com.ar.musicplayer.data.models.Album
import com.ar.musicplayer.data.models.Artist
import com.ar.musicplayer.data.models.Playlist
import com.ar.musicplayer.data.models.SongResponse


@Composable
fun ArtistPlaylistLazyRow(title: String,playlist: List<Playlist>) {

    Column(Modifier) {

        Heading(title = title)

        LazyRow(
            contentPadding = PaddingValues(vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(playlist) { item ->

                HomeScreenRowCard(
                    isRadio = false,
                    subtitle = item.subtitle.toString(),
                    cornerRadius = 0,
                    imageUrl = item.image.toString(),
                    title = item.title.toString(),
                    size = 170,
                    onClick = { }
                )
            }
        }
    }
}


@Composable
fun ArtistAlbumLazyRow(title: String, albumList: List<Album>){
    Column(Modifier) {

        Heading(title = title)

        LazyRow(
            contentPadding = PaddingValues(vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(albumList) { album ->
                HomeScreenRowCard(
                    isRadio = false,
                    subtitle = album.subtitle.toString(),
                    cornerRadius = 0,
                    imageUrl = album.image.toString(),
                    title = album.title.toString(),
                    size = 170,
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun ArtistsLazyRow(title: String, artistList: List<Artist> ){
    Column(Modifier) {

        Heading(title = title)

        LazyRow(
            contentPadding = PaddingValues(vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(artistList) { artist ->
                HomeScreenRowCard(
                    isRadio = false,
                    subtitle = "",
                    cornerRadius = 50,
                    imageUrl = artist.image.toString(),
                    title = artist.name.toString(),
                    size = 170,
                    onClick = { }
                )
            }
        }
    }
}


@Composable
fun ArtistSinglesLazyRow(title: String, singlesList: List<SongResponse>){
    Column(Modifier) {

        Heading(title = title)

        LazyRow(
            contentPadding = PaddingValues(vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(singlesList) { single ->
                HomeScreenRowCard(
                    isRadio = false,
                    subtitle = single.subtitle.toString(),
                    cornerRadius = 0,
                    imageUrl = single.image.toString(),
                    title = single.title.toString(),
                    size = 170,
                    onClick = { }
                )
            }
        }
    }
}