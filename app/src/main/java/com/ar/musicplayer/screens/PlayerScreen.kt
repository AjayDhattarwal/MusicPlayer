package com.ar.musicplayer.screens

import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.ar.musicplayer.components.ControlButton
import com.ar.musicplayer.components.DownloadButton
import com.ar.musicplayer.components.FavToggleButton
import com.ar.musicplayer.components.MusicPlayingAnimation
import com.ar.musicplayer.components.PlayPauseButton
import com.ar.musicplayer.components.PlayPauseLargeButton
import com.ar.musicplayer.components.SkipNextButton
import com.ar.musicplayer.components.TrackSlider
import com.ar.musicplayer.components.convertToText
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionEvent
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionViewModel
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.navigation.currentFraction
import com.ar.musicplayer.utils.MusicPlayer
import com.ar.musicplayer.utils.playerHelper.DownloadEvent
import com.ar.musicplayer.utils.playerHelper.DownloaderViewModel
import com.ar.musicplayer.viewmodel.ImageColorGradient
import com.ar.musicplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
) {val coroutineScope = rememberCoroutineScope()
    val UpNextSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
    )
    val lastSessionPlaylist by lastSessionViewModel.lastSession.observeAsState()
    val context = LocalContext.current
    val player = musicPlayer.getPlayer()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val imageColorGradientViewModel: ImageColorGradient = viewModel()
    val playlist by playerViewModel.playlist.collectAsState()
    val currentIndex by playerViewModel.currentIndex.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val UpNextPlaylist = musicPlayer.getPlaylist().collectAsState()

    val favButtonClick = remember { mutableStateOf(false) }

    val isFavourite = remember { mutableStateOf(false) }

    val isDownloaded = remember { mutableStateOf(false) }
    val isDownloading = remember { mutableStateOf(false) }
    val downloadProgress by downloaderViewModel.songProgress.observeAsState()
    val currentDownloading by downloaderViewModel.currentDownloading.observeAsState()

    val listState = rememberLazyListState()

    val statusBarPadding = getStatusBarHeight()

    val size by animateDpAsState(targetValue = lerp(70.dp, 310.dp, bottomSheetState.currentFraction))
    val sizeofCollapseBar by animateDpAsState(targetValue = lerp(0.dp, 30.dp, bottomSheetState.currentFraction))
    val dynamicPaddingValues by animateDpAsState(targetValue = lerp(0.dp, statusBarPadding, bottomSheetState.currentFraction))
    val dynamicImgBoxSize by animateDpAsState(targetValue = lerp(70.dp, LocalConfiguration.current.screenWidthDp.dp, bottomSheetState.currentFraction))
    val dynamicSongTitleTopPadding by animateDpAsState(targetValue = lerp(100.dp, 10.dp, bottomSheetState.currentFraction))
    LaunchedEffect(currentDownloading) {
        if (currentDownloading == currentSong) {
            isDownloading.value = true
            isDownloaded.value = true
        }
        if (currentDownloading == null && downloadProgress == 0) {
            isDownloading.value = false
        } else if (currentDownloading != currentSong) {
            isDownloading.value = false
        }
    }

    val showShimmer = remember { mutableStateOf(true) }

    LaunchedEffect(currentSong) {
        if (currentSong != null) {
            downloaderViewModel.isAllReadyDownloaded(currentSong!!) { it ->
                isDownloaded.value = it
            }
        }
    }

    LaunchedEffect(currentSong?.id, favButtonClick) {
        var isFavVar: Flow<Boolean>? = null
        favViewModel.onEvent(
            FavoriteSongEvent.isFavoriteSong(currentSong?.id.toString()) { isFav ->
                isFavVar = isFav
            }
        )

        launch {
            isFavVar?.collect { isFav ->
                isFavourite.value = isFav
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
            showShimmer.value = false
            musicPlayer.play(song)
        }
    }

    LaunchedEffect(currentSong) {
        showShimmer.value = false
        currentSong?.image?.let { imageColorGradientViewModel.loadImage(it, context) }
        if (currentSong != null && playerViewModel.isPlaying.value) {
            if (lastSessionPlaylist?.contains(currentSong) == false) {
                Log.d("lastSession", "songInserted")
                lastSessionViewModel.onEvent(LastSessionEvent.InsertLastPlayedData(currentSong!!))
            }
        }

        coroutineScope.launch {
            if (UpNextPlaylist.value.isNotEmpty() && currentIndex > 0) {
                listState.animateScrollToItem(currentIndex)
            }
        }
    }

    val verticalGradient = remember {
        mutableStateOf(
            Brush.verticalGradient(listOf(
                Color(0xFF0E0E0E),
                Color(0xFF000000),
            ))
        )
    }

    val perfectColor = imageColorGradientViewModel.colorForBackGround.collectAsState()

    val qualityImgUrl = currentSong?.image?.replace("150x150", "350x350")
    var imageDrawable: Drawable? by remember { mutableStateOf(null) }
    LaunchedEffect(qualityImgUrl) {
        imageDrawable = withContext(Dispatchers.IO) { qualityImgUrl?.let { prefetchImage(context, it) } }
    }

    val songName = currentSong?.title.toString().replace("&quot;", "").replace("&amp;", ",")

    val artistsNames = currentSong?.moreInfo?.artistMap?.artists?.distinctBy { it.name }?.joinToString(", ") { it.name.toString() }

    LaunchedEffect(perfectColor.value) {
        verticalGradient.value = Brush.verticalGradient(listOf(
            perfectColor.value ?: Color(0xFF161616), Color(0xFF0E0E0E),
            Color(0xFF000000)
        ))
        Log.d("color", "color is  ${perfectColor.value}")
    }

    val currentPosition = remember { mutableLongStateOf(0) }
    val sliderPosition = remember { mutableLongStateOf(0) }
    val totalDuration = remember { mutableLongStateOf(0) }

    LaunchedEffect(player.currentPosition, player.isPlaying) {
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

            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            BottomSheetScaffold(
                scaffoldState = UpNextSheetState,
                sheetPeekHeight = 70.dp,
                sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                sheetContent = {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((LocalConfiguration.current.screenHeightDp / 2).dp)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
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
                                .fillMaxSize(),
                            state = listState,
                        ) {

                            if(UpNextPlaylist.value.size != 0){
                                items(UpNextPlaylist.value){
                                    UpNextPlaylist(it,playerViewModel,favViewModel,downloaderViewModel)
                                }
                            }
                        }

                    }

                },
                sheetBackgroundColor = Color(0xDC000000),
                backgroundColor = Color.Transparent
            ) {


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    verticalArrangement = Arrangement.Top

                ) {

                    Column(modifier = Modifier.fillMaxWidth()) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = dynamicPaddingValues)
                                .height(sizeofCollapseBar),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(dynamicImgBoxSize)
                                    .background(Color.Transparent)
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = imageDrawable,
                                    contentDescription = "image",
                                    modifier = Modifier
                                        .size(size)
                                        .background(brush = shimmerEffectfun(true))
                                        .clip(RoundedCornerShape(4.dp)),
                                    onSuccess = { showShimmer.value = false },
                                    contentScale = ContentScale.FillBounds,
                                    alignment = Alignment.Center,
                                )

                            }


                            AnimatedVisibility(
                                visible = bottomSheetState.currentFraction < 0.9f,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(){
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
                                    FavToggleButton(
                                        isFavorite = isFavourite.value,
                                        onFavClick = remember{
                                            {
                                                favViewModel.onEvent(
                                                    FavoriteSongEvent.toggleFavSong(
                                                        songResponse = currentSong!!
                                                    )
                                                )
                                                favButtonClick.value = !favButtonClick.value
                                            }
                                        }
                                    )

                                    PlayPauseButton(
                                        isPlaying = isPlaying ,
                                        onPlayPauseClick = remember {
                                            {
                                                if (isPlaying) {
                                                    playerViewModel.pause()
                                                } else {
                                                    playerViewModel.starter.value = false
                                                    playerViewModel.play()
                                                }
                                            }
                                        }
                                    )

                                    SkipNextButton(
                                        onSkipNext = remember {
                                            { musicPlayer.skipToNext() }
                                        }
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = bottomSheetState.currentFraction > 0,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column (modifier = Modifier.padding(start = 10.dp, end = 10.dp)){
                                Column(
                                    modifier = Modifier
                                        .padding(
                                            top = dynamicSongTitleTopPadding,
                                            bottom = 30.dp,
                                            start = 20.dp,
                                            end = 20.dp
                                        )
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {


                                    Text(
                                        text = songName ,
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
                                    onValueChange = { newValue ->
                                       sliderPosition.longValue = newValue.toLong()
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                ) {
                                    ControlButton(
                                        icon = Icons.Default.Shuffle,
                                        size = 30.dp,
                                        onClick = remember {
                                            {
                                                ////// Shuffle
                                            }
                                        },
                                        tint = Color.LightGray
                                    )
                                    Spacer(modifier = Modifier.width(20.dp))

                                    ControlButton(
                                        icon = Icons.Default.SkipPrevious,
                                        size = 50.dp,
                                        onClick = remember {
                                            {
                                                musicPlayer.skipToPrevious()
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))

                                    PlayPauseLargeButton (
                                        isPlaying = isPlaying ,
                                        onPlayPauseClick = remember {
                                            {
                                                if (isPlaying) {
                                                    playerViewModel.pause()
                                                } else {
                                                    playerViewModel.starter.value = false
                                                    playerViewModel.play()
                                                }
                                            }
                                        }
                                    )


                                    Spacer(modifier = Modifier.width(10.dp))
                                    ControlButton(
                                        icon = Icons.Default.SkipNext,
                                        size = 50.dp,
                                        onClick = remember {
                                            {
                                                musicPlayer.skipToNext()
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(20.dp))
                                    ControlButton(
                                        icon = Icons.Default.Repeat,
                                        size = 30.dp,
                                        onClick = remember {
                                            {
                                                //// Repeat
                                            }
                                        },
                                        tint = Color.LightGray
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    FavToggleButton(
                                        isFavorite = isFavourite.value,
                                        onFavClick = remember{
                                            {
                                                favViewModel.onEvent(
                                                    FavoriteSongEvent.toggleFavSong(
                                                        songResponse = currentSong!!
                                                    )
                                                )
                                                favButtonClick.value = !favButtonClick.value
                                            }
                                        }
                                    )

                                    DownloadButton(
                                        isDownloaded = isDownloaded.value,
                                        isDownloading = isDownloading.value,
                                        downloadProgress = downloadProgress?.toFloat(),
                                        onDownloadClick = remember(currentSong) {
                                            {
                                                if (!isDownloaded.value) {
                                                    currentSong?.let {
                                                        downloaderViewModel.onEvent(
                                                            DownloadEvent.downloadSong(it)
                                                        )
                                                    }
                                                }
                                            }
                                        }
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



@Composable
fun UpNextPlaylist(
    songResponse: SongResponse,
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
    val isPlaying by  playerViewModel.isPlaying.collectAsState()
    val isFavourite = remember { mutableStateOf(false) }

    val isDownloaded = remember { mutableStateOf(false) }
    val isDownloading = remember { mutableStateOf(false) }
    val downloadProgress by downloaderViewModel.songProgress.observeAsState()
    val currentDownloading by downloaderViewModel.currentDownloading.observeAsState()


    LaunchedEffect(currentDownloading) {
        if(currentDownloading == songResponse){
            isDownloading.value =  true
            isDownloaded.value = true
        }
        if(currentDownloading == null && downloadProgress == 0 ){
            isDownloading.value = false
        }
        else if(currentDownloading != songResponse  ){
            isDownloading.value = false
        }

    }

    LaunchedEffect(key1 = Unit) {
        downloaderViewModel.isAllReadyDownloaded(songResponse) { it ->
            isDownloaded.value = it
        }
    }

    LaunchedEffect(key1 = Unit, key2 = favButtonClick) {
        var isFavVar : Flow<Boolean>? = null
        val flow = favViewModel.onEvent(
            FavoriteSongEvent.isFavoriteSong(songResponse.id.toString()) { isFav ->
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
            model = songResponse.image,
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
                    songResponse.image?.let {
                        imageColorViewModel.loadImage(
                            it,
                            context
                        )
                    }
                    playerViewModel.updateCurrentSong(
                        songResponse
                    )
                    playerViewModel.isPlayingHistory.value = false
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
                text = songResponse.subtitle ?: "unknown",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }
        if(currentSong != songResponse){

            DownloadButton(
                isDownloaded = isDownloaded.value,
                isDownloading = isDownloading.value,
                downloadProgress = downloadProgress?.toFloat(),
                onDownloadClick = remember(songResponse) {
                    {
                        if (!isDownloaded.value) {
                            songResponse.let {
                                downloaderViewModel.onEvent(
                                    DownloadEvent.downloadSong(it)
                                )
                            }
                        }
                    }
                }
            )
            FavToggleButton(
                isFavorite = isFavourite.value,
                onFavClick = remember{
                    {
                        favViewModel.onEvent(
                            FavoriteSongEvent.toggleFavSong(
                                songResponse = songResponse
                            )
                        )
                        favButtonClick.value = !favButtonClick.value
                    }
                }
            )

            IconButton(onClick = { /* Handle menu button click */ }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
        }
        else{
            MusicPlayingAnimation(isPlaying = isPlaying, modifier = Modifier.padding(start = 10.dp, end = 10.dp).width(80.dp).height(30.dp))
        }
    }
}


suspend fun prefetchImage(context: Context, url: String): Drawable? {
    val imageLoader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(url)
        .build()

    while (true) {
        try {
            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                return result.drawable
            }
        } catch (e: Exception) {
            // Log error if needed
        }
        delay(1000) // Wait for a second before retrying
    }
}

@Composable
fun getStatusBarHeight(): Dp {
    val view = LocalView.current
    val insets = ViewCompat.getRootWindowInsets(view)
    val statusBarHeightPx = insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
    val density = LocalDensity.current

    return with(density) { statusBarHeightPx.toDp() }
}


