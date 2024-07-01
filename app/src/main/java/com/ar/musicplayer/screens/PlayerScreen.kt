package com.ar.musicplayer.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.ar.musicplayer.components.ControlButton
import com.ar.musicplayer.components.TrackSlider
import com.ar.musicplayer.components.convertToText
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionEvent
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionViewModel
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.utils.MusicPlayer
import com.ar.musicplayer.utils.playerHelper.DownloadEvent
import com.ar.musicplayer.utils.playerHelper.DownloaderViewModel
import com.ar.musicplayer.viewmodel.ImageColorGradient
import com.ar.musicplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@UnstableApi
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    playerViewModel: PlayerViewModel,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    bottomSheetState: BottomSheetScaffoldState,
    musicPlayer: MusicPlayer,
    lastSessionViewModel: LastSessionViewModel,
    favViewModel: FavoriteViewModel,
    downloaderViewModel: DownloaderViewModel,
) {
    val coroutineScope = rememberCoroutineScope()
    val UpNextSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
    )

    val context = LocalContext.current
    val player = musicPlayer.getPlayer()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val imageColorGradientViewModel: ImageColorGradient = viewModel()
    val playlist by playerViewModel.playlist.collectAsState()
    val currentIndex by playerViewModel.currentIndex.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()

    val favButtonClick  = remember {
        mutableStateOf(false)
    }
    val isFavourite = remember { mutableStateOf(false) }

    val isDownloaded = remember { mutableStateOf(false) }
    val isDownloading = remember { mutableStateOf(false) }
    val downloadProgress by downloaderViewModel.songProgress.observeAsState()
    val currentDownloading by downloaderViewModel.currentDownloading.observeAsState()



    Log.d("daw",isDownloading.value.toString())
    LaunchedEffect(currentDownloading) {
        if(currentDownloading == currentSong){
            isDownloading.value =  true
            isDownloaded.value = true
        }
        if(currentDownloading == null && downloadProgress == 0 ){
            isDownloading.value = false
        }
        else if(currentDownloading != currentSong  ){
            isDownloading.value = false
        }

    }

    LaunchedEffect(key1 = currentSong) {
        if(currentSong != null){
            downloaderViewModel.isAllReadyDownloaded(currentSong!!) { it ->
                isDownloaded.value = it
            }
        }
    }




    LaunchedEffect(key1 = currentSong?.id, key2 = favButtonClick) {
        var isFavVar : Flow<Boolean>? = null
        favViewModel.onEvent(
            FavoriteSongEvent.isFavoriteSong(currentSong?.id.toString()) { isFav ->
                isFavVar = isFav
            }
        )

        launch { // Launch a coroutine to collect the Flow
            isFavVar?.collect { isFav ->
                isFavourite.value = isFav // Update the state in the composable
            }
        }
    }
    LaunchedEffect(isFavourite.value) {
        playerViewModel.isFavorite(isFavourite.value)
    }

    val bitmap by imageColorGradientViewModel.bitmapState.collectAsState()
    bitmap?.let { playerViewModel.bitmapload(it) }
    DisposableEffect(Unit) {
        musicPlayer.onSongChanged = { song ->
            playerViewModel.updateCurrentSong(song)
            playerViewModel.play()
        }
        onDispose {
            musicPlayer.release()
        }
    }
    LaunchedEffect(playlist) {
        playlist.let {
            musicPlayer.playPlaylist(it)
        }
    }
    currentSong?.let { song ->
        LaunchedEffect(song) {
            musicPlayer.play(song)
        }
    }
    LaunchedEffect(currentSong){
        currentSong?.image?.let { imageColorGradientViewModel.loadImage(it,context) }
        if(currentSong != null && playerViewModel.isPlaying.value ){
                Log.d("lastSession","songInserted")
                lastSessionViewModel.onEvent(LastSessionEvent.InsertLastPlayedData(currentSong!!))
        }
    }

    val verticalGradient = remember { mutableStateOf(
        Brush.verticalGradient(listOf(
            Color(0xFF0E0E0E),
            Color(0xFF000000),
        ))) }

    val perfectColor =  imageColorGradientViewModel.colorForBackGround.collectAsState()

    val showShimmer = remember { mutableStateOf(true) }

    val largeShowShimmer = remember { mutableStateOf(true) }

    val qualityImgUrl = currentSong?.image?.replace("150x150", "350x350")

    val artistsNames = currentSong?.moreInfo?.artistMap?.artists?.distinctBy { it.name }?.joinToString(", ") { it.name.toString() }


    LaunchedEffect(perfectColor.value) {
        verticalGradient.value = Brush.verticalGradient(listOf(
            perfectColor.value ?: Color(0xFF161616), Color(0xFF0E0E0E),
            Color(0xFF000000)
        ))
        Log.d("color", "color is  ${perfectColor.value}")
    }




    val currentPosition = remember {
        mutableLongStateOf(0)
    }

    val sliderPosition = remember {
        mutableLongStateOf(0)
    }

    val totalDuration = remember {
        mutableLongStateOf(0)
    }

    LaunchedEffect(key1 = player.currentPosition, key2 = player.isPlaying) {
        delay(1000)
        currentPosition.longValue = player.currentPosition
    }

    LaunchedEffect(currentPosition.longValue) {
        sliderPosition.longValue = currentPosition.longValue
    }

    LaunchedEffect(player.duration) {
        if (player.duration > 0) {
            totalDuration.longValue = player.duration
        }
    }


    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = verticalGradient.value)
                .clickable { onExpand() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(75.dp)
            ) {
                AnimatedVisibility(
                    visible = bottomSheetState.bottomSheetState.progress == 1.0f && !bottomSheetState.bottomSheetState.isExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = currentSong?.image,
                            contentDescription = "image",
                            modifier = Modifier
                                .size(70.dp)
                                .padding(10.dp)
                                .background(brush = shimmerEffectfun(showShimmer.value))
                                .clip(RoundedCornerShape(4.dp)),
                            onSuccess = { showShimmer.value = false },
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 20.dp)
                        ) {
                            currentSong?.title?.let {
                                Text(
                                    text = it.replace("&quot;",""),
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.basicMarquee(
                                        animationMode = MarqueeAnimationMode.Immediately,
                                        repeatDelayMillis = 2000,
                                        initialDelayMillis = 2000,
                                        iterations = Int.MAX_VALUE
                                    )
                                )
                            }

                            artistsNames?.let {
                                Text(
                                    text = it.replace("&quot;",""),
                                    fontSize = 14.sp,
                                    color = Color.LightGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.basicMarquee(
                                        animationMode = MarqueeAnimationMode.Immediately,
                                        repeatDelayMillis = 2000,
                                        initialDelayMillis = 2000,

                                    )
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                currentSong?.let {
                                    FavoriteSongEvent.toggleFavSong(
                                        it
                                    )
                                }?.let { favViewModel.onEvent(it) }

                                favButtonClick.value = !favButtonClick.value

                            },
                            modifier = Modifier.padding(end = 5.dp)
                        ) {
                            Icon(
                                imageVector = if(isFavourite.value) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if(isFavourite.value) Color.Red else Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    playerViewModel.pause()
                                } else {
                                    playerViewModel.starter.value = false
                                    playerViewModel.play()
                                }
                            },
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .indication(remember {
                                    MutableInteractionSource()
                                }, null)

                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White
                            )
                        }


                        IconButton(
                            onClick = { musicPlayer.skipToNext() },
                            modifier = Modifier.padding(end = 10.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Play/Pause",
                                tint = Color.White
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = !(bottomSheetState.bottomSheetState.progress == 1.0f && !bottomSheetState.bottomSheetState.isExpanded),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = SpaceBetween
                    ) {
                        IconButton(onClick = onCollapse, modifier = Modifier) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Back",
                                tint = Color.White
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

            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            BottomSheetScaffold(
                scaffoldState = UpNextSheetState,
                sheetPeekHeight = 70.dp,
                sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                sheetContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .padding(top = 4.dp)

                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(vertical = 8.dp)
                                .width(40.dp)
                                .height(4.dp)
                                .background(Color.White, shape = RoundedCornerShape(2.dp))
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(4.dp)
                        ) {
                            Text(
                                text = "Up Next",
                                fontSize = 22.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        LazyColumn(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxSize()
                        ) {
                            Log.d("playlist", "${playlist.size},${currentIndex}")
                            if(playlist.size != 0 && currentIndex > -1 && currentIndex < playlist.size){
                                if(currentIndex > playlist.size){
                                    playerViewModel.setCurrentSongIndex(0)
                                    Log.d("playlist", "after update ${playlist.size},${currentIndex}")
                                }
                                playlist.subList(currentIndex , playlist.size ).let {
                                    items(it){ songResponse->
                                        UpnextPlaylist(songResponse,playerViewModel,favViewModel,downloaderViewModel)
                                    }
                                }
                            }
                        }

                    }

                },
                sheetBackgroundColor = Color(0xC4000000),
                backgroundColor = Color.Transparent,
            ) {


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 20.dp)
                        .background(Color.Transparent)
                ) {
                    AnimatedVisibility(
                        visible = !(bottomSheetState.bottomSheetState.progress == 1.0f && !bottomSheetState.bottomSheetState.isExpanded),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {

                                AsyncImage(
                                    model = qualityImgUrl,
                                    contentDescription = "image",
                                    modifier = Modifier
                                        .size(310.dp)
                                        .background(brush = shimmerEffectfun(largeShowShimmer.value))
                                        .clip(RoundedCornerShape(10.dp)),
                                    onSuccess = { largeShowShimmer.value = false },
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center
                                )

                            }
                            Column(
                                modifier = Modifier.padding(top = 30.dp, bottom = 30.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {


                                Text(
                                    text = currentSong?.title.toString().replace("&quot;", ""),
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
                            TrackSlider(
                                value = sliderPosition.longValue.toFloat(),
                                onValueChange = {
                                    sliderPosition.longValue = it.toLong()
                                },
                                onValueChangeFinished = {
                                    currentPosition.longValue = sliderPosition.longValue
                                    player.seekTo(sliderPosition.longValue)
                                },
                                songDuration = totalDuration.longValue.toFloat()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                            ) {

                                Text(
                                    text = (currentPosition.longValue).convertToText(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp),
                                    color = Color.White,
                                    style = TextStyle(fontWeight = FontWeight.Bold)
                                )

                                val remainTime = totalDuration.longValue - currentPosition.longValue
                                Text(
                                    text = if (remainTime >= 0) remainTime.convertToText() else "",
                                    modifier = Modifier
                                        .padding(end = 8.dp),
                                    color = Color.White,
                                    style = TextStyle(fontWeight = FontWeight.Bold)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                            ) {
                                ControlButton(
                                    icon = Icons.Default.Shuffle,
                                    size = 30.dp,
                                    onClick = {

                                    },
                                    tint = Color.LightGray
                                )
                                Spacer(modifier = Modifier.width(20.dp))

                                ControlButton(
                                    icon = Icons.Default.SkipPrevious,
                                    size = 50.dp,
                                    onClick = {
                                        musicPlayer.skipToPrevious()
                                    })
                                Spacer(modifier = Modifier.width(10.dp))

                                ControlButton(
                                    icon = if (isPlaying) Icons.Filled.PauseCircle else Icons.Default.PlayCircle,
                                    size = 100.dp,
                                    onClick = {
                                        if (isPlaying) {
                                            playerViewModel.pause()
                                        } else {
                                            playerViewModel.starter.value = false
                                            playerViewModel.play()
                                        }
                                    }
                                )


                                Spacer(modifier = Modifier.width(10.dp))
                                ControlButton(
                                    icon = Icons.Default.SkipNext,
                                    size = 50.dp,
                                    onClick = {
                                        musicPlayer.skipToNext()
                                    }
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                                ControlButton(
                                    icon = Icons.Default.Repeat,
                                    size = 30.dp,
                                    onClick = {

                                    },
                                    tint = Color.LightGray
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = {
                                        currentSong?.let {
                                            FavoriteSongEvent.toggleFavSong(
                                                it
                                            )
                                        }?.let { favViewModel.onEvent(it) }

                                        favButtonClick.value = !favButtonClick.value

                                    },
                                ) {
                                    Icon(
                                        imageVector = if (isFavourite.value) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isFavourite.value) Color.Red else Color.White
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (!isDownloaded.value) {
                                            if (currentSong != null) {
                                                downloaderViewModel.onEvent(
                                                    DownloadEvent.downloadSong(
                                                        currentSong!!
                                                    )
                                                )
                                            }
                                        }
                                    }) {
                                    if (isDownloading.value) {
                                        CircularProgressIndicator(
                                            modifier = Modifier,
                                            progress = downloadProgress?.div(100.toFloat()) ?: 0f,
                                            color = Color.LightGray
                                        )
                                        Text(
                                            text = "${downloadProgress}%",
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    } else {
                                        Icon(
                                            modifier = Modifier.weight(1f),
                                            imageVector = if (isDownloaded.value) Icons.Filled.Check else Icons.Default.FileDownload,
                                            contentDescription = "Download",
                                            tint = Color.White
                                        )
                                    }
                                }

                            }

                        }
                    }
                }
            }
        }
    }
}



@Composable
fun UpnextPlaylist(
    apiData: SongResponse,
    playerViewModel: PlayerViewModel,
    favViewModel: FavoriteViewModel,
    downloaderViewModel: DownloaderViewModel,
)
{
    val context = LocalContext.current
    val showShimmer = remember { mutableStateOf(true) }
    val imageColorViewModel = viewModel<ImageColorGradient>()
    val currentSong by playerViewModel.currentSong.collectAsState()

    val favButtonClick  = remember {
        mutableStateOf(false)
    }

    val isFavourite = remember { mutableStateOf(false) }

    val isDownloaded = remember { mutableStateOf(false) }
    val isDownloading = remember { mutableStateOf(false) }
    val downloadProgress by downloaderViewModel.songProgress.observeAsState()
    val currentDownloading by downloaderViewModel.currentDownloading.observeAsState()


    Log.d("daw",isDownloading.value.toString())
    LaunchedEffect(currentDownloading) {
        if(currentDownloading == apiData){
            isDownloading.value =  true
            isDownloaded.value = true
        }
        if(currentDownloading == null && downloadProgress == 0 ){
            isDownloading.value = false
        }
        else if(currentDownloading != apiData  ){
            isDownloading.value = false
        }

    }

    LaunchedEffect(key1 = Unit) {
        downloaderViewModel.isAllReadyDownloaded(apiData) { it ->
            isDownloaded.value = it
        }
    }

    LaunchedEffect(key1 = Unit, key2 = favButtonClick) {
        var isFavVar : Flow<Boolean>? = null
        val flow = favViewModel.onEvent(
            FavoriteSongEvent.isFavoriteSong(apiData.id.toString()) { isFav ->
                isFavVar = isFav
            }
        )

        launch { // Launch a coroutine to collect the Flow
            isFavVar?.collect { isFav ->
                isFavourite.value = isFav // Update the state in the composable
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 5.dp, top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = apiData.image,
            contentDescription = "image",
            modifier = Modifier
                .size(50.dp)
                .padding(4.dp)
                .background(brush = shimmerEffectfun(showShimmer.value))
                .clip(RoundedCornerShape(3.dp)),
            onSuccess = { showShimmer.value = false },
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        Column(
            modifier = Modifier
                .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                .weight(1f)
                .clickable {
                    playerViewModel.starter.value = false
                    apiData.image?.let {
                        imageColorViewModel.loadImage(
                            it,
                            context
                        )
                    }
                    playerViewModel.updateCurrentSong(
                        apiData
                    )
                    playerViewModel.isPlayingHistory.value = false
                }
        ) {
            Text(
                text = apiData.title ?: "null",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 2.dp),
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = apiData.subtitle ?: "unknown",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }
        if(currentSong != apiData){
            IconButton(
                onClick = {
                    if(!isDownloaded.value){
                        downloaderViewModel.onEvent(DownloadEvent.downloadSong(apiData))
                    }
                }) {
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
                        imageVector = if(isDownloaded.value) Icons.Default.Check else Icons.Default.FileDownload,
                        contentDescription = "Download",
                        tint = Color.White
                    )
                }
            }

            IconButton(onClick = {
                favViewModel.onEvent(FavoriteSongEvent.toggleFavSong(apiData))
                favButtonClick.value = !favButtonClick.value
            }) {
                Icon(
                    imageVector = if(isFavourite.value) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if(isFavourite.value) Color.Red else Color.White
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
        else{
            Text(
                text = "Now Playing...",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}
