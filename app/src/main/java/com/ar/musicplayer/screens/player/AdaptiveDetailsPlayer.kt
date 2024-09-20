@file:kotlin.OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalLayoutApi::class
)

package com.ar.musicplayer.screens.player

import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import coil.compose.rememberImagePainter
import com.ar.musicplayer.components.player.AnimatedPager
import com.ar.musicplayer.components.player.LyricsCard
import com.ar.musicplayer.data.models.Artist
import com.ar.musicplayer.data.models.getArtistList
import com.ar.musicplayer.data.models.sanitizeString
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.download.DownloadEvent
import com.ar.musicplayer.utils.download.DownloadStatus
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.utils.helper.PaletteExtractor
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import kotlinx.collections.immutable.toPersistentList

@OptIn(UnstableApi::class )
@Composable
fun AdaptiveDetailsPlayer(
    modifier: Modifier = Modifier,
    isAdaptive: Boolean,
    playerViewModel: PlayerViewModel,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    paletteExtractor: PaletteExtractor,
    downloaderViewModel: DownloaderViewModel,
    favoriteViewModel: FavoriteViewModel,
    onQueue: () -> Unit
){
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }

    val currentSong by playerViewModel.currentSong.collectAsState()
    val currentIndex by playerViewModel.currentIndex.collectAsState()
    val playlist by playerViewModel.playlist.collectAsState()

    val persistentList = remember{
        derivedStateOf{
            playlist.toPersistentList()
        }
    }

    val pagerState = rememberPagerState(pageCount = {persistentList.value.size})


    val songName = currentSong?.title.toString().sanitizeString()
    val artistMap = currentSong?.moreInfo?.artistMap
    val artistsNames = artistMap?.artists
        ?.distinctBy { it.name }
        ?.joinToString(", ") { it.name.toString() }
        ?.sanitizeString()


    val artistList = artistMap?.getArtistList()
        ?.sortedBy { it.name?.replace(" ", "")?.length }


    val colors = remember {
        mutableStateOf(arrayListOf<Color>(Color.Black,Color.Black))
    }

    LaunchedEffect(persistentList) {
        pagerState.scrollToPage(currentIndex)
    }

    LaunchedEffect(pagerState.currentPage) {
        if (currentIndex != pagerState.currentPage) {
            playerViewModel.changeSong(pagerState.currentPage)
        }
    }

    LaunchedEffect(currentSong) {
        currentSong?.image?.let {
            val shade = paletteExtractor.getColorFromImg(it)
            shade.observeForever { shadeColor ->
                shadeColor?.let { col ->
                    playerViewModel.setCurrentSongColor(col)
                    colors.value = arrayListOf(col, Color.Black)
                }
            }
        }
        if (currentIndex != pagerState.currentPage) {
            pagerState.scrollToPage(currentIndex)
        }
    }

    val lazyListState = rememberLazyListState()
    val isLyricsLoading = playerViewModel.isLyricsLoading.collectAsState()
    val lyricsData = playerViewModel.lyricsData.collectAsState()
    val currentLyricIndex = playerViewModel.currentLyricIndex.collectAsState()

    var isDownloaded by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    val downloadProgress by downloaderViewModel.songProgress.collectAsState()

    val songDownloadStatus by downloaderViewModel.songDownloadStatus.observeAsState(emptyMap())

    val status =
        songDownloadStatus[currentSong?.id.toString()] ?: DownloadStatus.NOT_DOWNLOADED

    var inDownloadQueue by remember { mutableStateOf(false) }


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

    LaunchedEffect(key1 = currentSong?.id) {
        if(currentSong?.id != ""){
            downloaderViewModel.onEvent(DownloadEvent.isDownloaded(currentSong!!) {
                isDownloaded = it
            })
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors.value.toList()
                    )
                )
            },
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedPager(
                    pagerState = pagerState,
                    items = persistentList
                )
            }

            Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp)) {
                Column(
                    modifier = Modifier
                        .padding(
                            bottom = 20.dp,
                            start = 20.dp,
                            end = 20.dp
                        )
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = songName,
                        modifier = Modifier.basicMarquee(
                            animationMode = MarqueeAnimationMode.Immediately,
                            repeatDelayMillis = 2000,
                            initialDelayMillis = 2000
                        ),
                        color = Color.White,
                        fontSize = 30.sp,
                        maxLines = 1
                    )
                    Text(
                        text = artistsNames.toString(),
                        modifier = Modifier.basicMarquee(
                            animationMode = MarqueeAnimationMode.Immediately,
                            repeatDelayMillis = 2000,
                            initialDelayMillis = 2000
                        ),
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { onQueue() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "CurrentPlaylist",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = remember {
                            {
                                if (!isDownloaded) {
                                    downloaderViewModel.onEvent(
                                        DownloadEvent.downloadSong(
                                            currentSong!!
                                        )
                                    )
                                    inDownloadQueue = true
                                }
                            }
                        }
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                progress = { downloadProgress.div(100.toFloat()) ?: 0f },
                                modifier = Modifier,
                                color = Color.LightGray,
                            )
                            Text(
                                text = "${downloadProgress}%",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        } else {
                            Icon(
                                modifier = Modifier.weight(1f),
                                imageVector = if (isDownloaded) Icons.Default.DownloadDone else if (inDownloadQueue) Icons.Filled.HourglassTop else Icons.Default.FileDownload,
                                contentDescription = "Download",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Artists
            FlowRow(
                horizontalArrangement = Arrangement.SpaceEvenly,
                maxItemsInEachRow = 2,
                modifier = Modifier.fillMaxWidth()
            ) {
                artistList?.forEach { artist ->
                    ArtistListItem(
                        color = colors.value[0],
                        artist = artist,
                        onFollowClick = { },
                        modifier = Modifier.widthIn(max = 500.dp)
                    )
                }
            }


            LyricsCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                preferencesManager = preferencesManager,
                colors = colors,
                isLyricsLoading = isLyricsLoading,
                lazyListState = lazyListState,
                lyricsData = lyricsData,
                currentLyricIndex = currentLyricIndex,
                onLyricsClick = remember { {
                    playerViewModel.seekTo(it.toLong())
                } }
            )

            Spacer(Modifier.height(30.dp))
        }


        IconButton(
            onClick =  onCollapse,
            modifier = Modifier
                .size(70.dp)
                .padding(10.dp)
                .clip(CircleShape)
                .background(Color(0x1E999999))
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "hide",
                tint = Color.White,
            )
        }
    }

}


@Composable
fun ArtistListItem(
    color: Color,
    artist: Artist,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberImagePainter(artist.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
            ) {
                Text(
                    text = artist.name ?: "",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text("Follow")
            }
        }
    }
}
