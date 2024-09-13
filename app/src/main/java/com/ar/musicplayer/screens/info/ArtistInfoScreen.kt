package com.ar.musicplayer.screens.info

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.ar.musicplayer.components.CircularProgress
import com.ar.musicplayer.components.home.Heading
import com.ar.musicplayer.components.home.HomeScreenRowCard
import com.ar.musicplayer.components.info.artistInfo.ArtistAlbumLazyRow
import com.ar.musicplayer.components.info.artistInfo.ArtistPlaylistLazyRow
import com.ar.musicplayer.components.info.artistInfo.ArtistSinglesLazyRow
import com.ar.musicplayer.components.info.artistInfo.ArtistsLazyRow
import com.ar.musicplayer.components.mix.AnimatedTopBar
import com.ar.musicplayer.data.models.Album
import com.ar.musicplayer.data.models.Artist
import com.ar.musicplayer.data.models.Playlist
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.screens.library.mymusic.toPx
import com.ar.musicplayer.viewmodel.MoreInfoViewModel


@Composable
fun ArtistInfoScreen(
    artistInfo : Artist,
    lazyListState: LazyListState = rememberLazyListState(),
    onBackPressed: () -> Unit,
    onSongClick: (SongResponse) -> Unit,
    onPlaylistClick: (Boolean, Playlist) -> Unit,
    onArtistClick: (Boolean, Artist) -> Unit,
    onAlbumClick: (Boolean, Album) -> Unit
) {

    val defaultImg = listOf(
        "artist-default-music.png",
        "artist-default-film.png"
    )

    val isDefaultImg =  defaultImg.any { name ->
        artistInfo.image?.substringAfterLast("/") == name
    }
    val minImgSize = 0.dp
    val maxImgSize =  170.dp
    var currentImgSize by remember { mutableStateOf(maxImgSize.toPx()) }
    val animatedScale by animateFloatAsState(targetValue = (currentImgSize/ maxImgSize.toPx()+ 0.1f)
        .coerceAtLeast(1f))

    val clampedDarkness = (currentImgSize / maxImgSize.toPx() + 0.2f).coerceIn(0f, 1f)

    // Calculate the blended color
//    val blackishColor = Color.Black.copy(1f- clampedDarkness)

    val nestedScrollConnection = remember {

        object : NestedScrollConnection{
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if(lazyListState.firstVisibleItemIndex < 1) {
                    val delta = available.y.toInt()
                    val newImgSize = currentImgSize + delta
                    val previousImgSize = currentImgSize

                    currentImgSize = newImgSize.coerceIn(minImgSize.toPx(), maxImgSize.toPx())

                    val consumed = currentImgSize - previousImgSize
                    return Offset(0f, consumed)
                }
                else{
                    return Offset.Zero
                }
            }
        }
    }
    val moreInfoViewModel = hiltViewModel<MoreInfoViewModel>()
    val artistData by moreInfoViewModel.artistData.collectAsStateWithLifecycle()
    val isLoading by moreInfoViewModel.isLoading.collectAsStateWithLifecycle()

    val nameParts = artistInfo.name?.split(" ")
    val displayName = if ((nameParts?.size ?: 0) > 2) {
        "${nameParts?.get(0)}\n${nameParts?.drop(1)?.joinToString(" ")}"
    } else {
        artistInfo.name
    }


    LaunchedEffect(Unit){
        moreInfoViewModel.fetchArtistData(
            token = artistInfo.permaUrl?.substringAfterLast('/') ?: "",
            type = "artist",
            nSong = 5,
            nAlbum = 20,
            call = "webapi.get"
        )
    }



    Box(
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
    ){
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = clampedDarkness
                    clip = true

                }
                .height(310.dp)

        ){
            SubcomposeAsyncImage(
                model = artistInfo.image.toString().replace("50x50","350x350"),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = if (isDefaultImg) 0.5f else clampedDarkness - 0.4f
                    }
//                    .alpha()
                    .scale(animatedScale)
                    .fillMaxSize()
            ) {
                val state = painter.state
                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                    CircularProgress()
                } else {
                    SubcomposeAsyncImageContent()
                }
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                        )
                    )
            )

        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ){

            if(lazyListState.firstVisibleItemIndex < 1) {
                IconButton(
                    onClick = { onBackPressed() },
                    modifier = Modifier
                        .drawBehind {
                            drawCircle(
                                color = Color.Black.copy(0.3f),
                            )
                        }
                        .padding(start = 8.dp, end = 8.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.ArrowBack, tint = Color.White,
                        contentDescription = "back",
                        modifier = Modifier

                    )

                }


                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .drawBehind {
                            drawCircle(
                                color = Color.Black.copy(0.3f),
                            )
                        }
                        .padding(start = 8.dp, end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert, tint = Color.White,
                        contentDescription = "back",
                        modifier = Modifier

                    )
                }
            }
        }
        if(isLoading){
            CircularProgress(background = Color.Black)
        } else{
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(0, currentImgSize.toInt())
                    },
                contentPadding = PaddingValues(start = 10.dp)

            ) {

                item{
                    Spacer(Modifier.height(60.dp))
                }

                item{
                    Text(
                        text = displayName.toString() ,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle.Default.copy(fontSize = 50.sp)
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = artistData?.subtitle.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                }

                if(artistData?.topSongs?.isNotEmpty() != false) {
                    item{ Heading(title = "Top Songs") }
                    items(artistData?.topSongs ?: emptyList()){ songResponse ->
                        val artistName = songResponse.moreInfo?.artistMap?.artists?.distinctBy { it.name }?.joinToString(", "){it.name.toString()}
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 5.dp, top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            AsyncImage(
                                model = songResponse.image,
                                contentDescription = "image",
                                modifier = Modifier
                                    .size(50.dp)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center
                            )

                            Column(
                                modifier = Modifier
                                    .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                                    .weight(1f)
                                    .clickable {
//                                playerViewModel.setNewTrack(songResponse)
                                    }
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
                    }   //topSongs
                }

                item{
                    if(artistData?.featuredPlaylist?.isNotEmpty() != false) {
                        artistData?.featuredPlaylist?.let {
                            Spacer(Modifier.height(10.dp))
                            ArtistPlaylistLazyRow(
                                title = "Featured In",
                                playlist = it,
                                onItemClick = onPlaylistClick
                            )
                        }
                    }
                }

                item{
                    if(artistData?.singles?.isNotEmpty() != false) {
                        artistData?.singles?.let {
                            ArtistSinglesLazyRow(
                                title = "Singles",
                                singlesList = it,
                                onItemClick = remember{
                                    { _, song ->
                                        onSongClick(song)
                                    }
                                }
                            )
                        }
                    }
                }

                if(artistData?.latestRelease?.isNotEmpty() != false) {
                    item{
                        Heading(title = "Latest Release")
                    }
                    items(artistData?.latestRelease ?: emptyList()){ songResponse ->
                        val artistName = songResponse.moreInfo?.artistMap?.artists?.distinctBy { it.name }?.joinToString(", "){it.name.toString()}
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 5.dp, top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            AsyncImage(
                                model = songResponse.image,
                                contentDescription = "image",
                                modifier = Modifier
                                    .size(50.dp)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center
                            )

                            Column(
                                modifier = Modifier
                                    .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                                    .weight(1f)
                                    .clickable {
//                                playerViewModel.setNewTrack(songResponse)
                                    }
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

                item{
                    if(artistData?.topSongs?.isNotEmpty() != false) {
                        artistData?.topAlbums?.let {
                            Spacer(Modifier.height(10.dp))
                            ArtistAlbumLazyRow(
                                title = "Top Albums",
                                albumList = it,
                                onItemClick = onAlbumClick
                            )
                        }
                    }
                }

                item{
                    if(artistData?.dedicatedPlaylist?.isNotEmpty() != false) {
                        artistData?.dedicatedPlaylist!!.let {
                            ArtistPlaylistLazyRow(
                                title = "Just ${artistData?.name.toString()}",
                                playlist = it,
                                onItemClick = onPlaylistClick
                            )
                        }
                    }
                }

                item{
                    if(artistData?.similarArtist?.isNotEmpty() != false){
                        artistData?.similarArtist?.let {
                            ArtistsLazyRow(
                                title = "Similar Artist",
                                artistList = it,
                                onItemClick = onArtistClick
                            )
                        }
                    }
                }

                item{
                    Spacer(Modifier.height(125.dp))
                }

            }
        }
        AnimatedTopBar(
            title = artistInfo.name.toString(),
            scrollState = lazyListState,
            skip = true,
            color = Color.Black.copy(0.5f),
            onBackPressed = onBackPressed
        )
    }

}


class SlopedShape(private val slopeHeight: Float) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, size.height - 0.1f)
            lineTo(size.width, size.height - slopeHeight)
            lineTo(size.width, 0f)
            lineTo(0f, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}
