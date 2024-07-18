package com.ar.musicplayer.screens


import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.ArcMode
import androidx.compose.animation.core.ExperimentalAnimationSpecApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.ar.musicplayer.models.HomeListItem
import com.ar.musicplayer.viewmodel.ApiCallViewModel
import com.ar.musicplayer.viewmodel.ImageColorGradient
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.components.AnimatedPlayPauseButton
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.utils.playerHelper.DownloadEvent
import com.ar.musicplayer.utils.playerHelper.DownloaderViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalAnimationSpecApi::class)
@Composable
fun InfoScreen(
    heading: String,
    navController: NavHostController,
    homeListItem: HomeListItem,
    playerViewModel: PlayerViewModel,
    context: Context,
    favViewModel: FavoriteViewModel,
    downloaderViewModel: DownloaderViewModel,
    imageColorViewModel: ImageColorGradient,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {

    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val playlistId by playerViewModel.playlistId.collectAsState()
    val apiCallViewModel: ApiCallViewModel = viewModel()
    val colorViewModel: ImageColorGradient = viewModel()

    val moreInfo = homeListItem.moreInfoHomeList
    val artistMap = moreInfo?.artistMap
    var allArtist = homeListItem.subtitle
    if (artistMap != null && artistMap.artists?.isNotEmpty() == true) {
        val artists = artistMap.artists.mapNotNull { it.name }
        allArtist = "Artists: " + artists.joinToString(", ").takeIf { it.isNotBlank() }
    }


    LaunchedEffect(homeListItem.image) {

        colorViewModel.loadImage(homeListItem.image.toString(), context)
        apiCallViewModel.getApiData(
            homeListItem.permaUrl?.substringAfterLast('/') ?: "",
            homeListItem.type?:"song",
            homeListItem.moreInfoHomeList?.songCount.toString(),
            "1",
            "webapi.get"
        )

    }
    var isAllreadyPlaying by remember {
        mutableStateOf(false)
    }
    val apiData by apiCallViewModel.apiLiveData.observeAsState()

    val bitmapState = colorViewModel.bitmapState.collectAsState()
    val imageColorState by colorViewModel.colorForBackGround.collectAsState()


    val blackToGrayGradient = remember {
        mutableStateOf(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF000000), Color(0xFF161616)),
                startY = Float.POSITIVE_INFINITY,
                endY = 0f
            )
        )
    }


    LaunchedEffect(bitmapState.value, imageColorState) {
        if (bitmapState.value != null && imageColorState != null) {
            blackToGrayGradient.value = Brush.verticalGradient(
                colors = listOf(Color(0xFF000000), Color(0xFF1F1E1E), imageColorState!!),
                startY = Float.POSITIVE_INFINITY,
                endY = 0f
            )
        }
        Log.d("color", "${imageColorState}")
    }

    val boundsTransform = BoundsTransform { initialBounds, targetBounds ->
        keyframes {
            durationMillis = 2000
            initialBounds at 0 using ArcMode.ArcBelow using FastOutSlowInEasing
            targetBounds at 2000
        }
    }

    val textBoundsTransform = { _: Rect, _: Rect -> tween<Rect>(1000) }

    val showShimmer = remember { mutableStateOf(true) }
    with(sharedTransitionScope) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)

        ){
            Column(
                modifier = Modifier
                    .background(blackToGrayGradient.value)
                    .fillMaxSize()
            ) {
                homeListItem.image?.let {
                    Row(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                        IconButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier,
                                tint = Color.White
                            )
                        }
                        AsyncImage(
                            model = homeListItem.image,
                            contentDescription = "image",
                            modifier = Modifier
                                .height(300.dp)
                                .width(250.dp)
                                .padding(top = 10.dp)
                                .weight(1f)
                                .background(brush = shimmerEffectfun(showShimmer.value))
                                .clip(RoundedCornerShape(10.dp))
                                .sharedElement(
                                    sharedTransitionScope.rememberSharedContentState(key = "image-${heading}-${homeListItem.id}"),
                                    animatedVisibilityScope = animatedContentScope,
//                                    boundsTransform = boundsTransform
                                ),
                            onSuccess = { showShimmer.value = false },
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )

                        IconButton(onClick = { /* Handle menu button click */ }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                modifier = Modifier,
                                tint = Color.White
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, top = 40.dp, end = 20.dp)
                    ) {
                        homeListItem.title?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                maxLines = 2,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .padding(bottom = 10.dp)
                                    .fillMaxWidth()
                                        .sharedElement(
                                            sharedTransitionScope.rememberSharedContentState(key = "title-${heading}-${homeListItem.id}"),
                                            animatedVisibilityScope = animatedContentScope,
                                            boundsTransform = textBoundsTransform
                                        ),
                            )
                        }

                        Text(
                            text = allArtist.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            maxLines = 1,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 6.dp)
                                    .sharedElement(
                                        sharedTransitionScope.rememberSharedContentState(key = "subtitle-${heading}-${homeListItem.id}"),
                                        animatedVisibilityScope = animatedContentScope,
                                        boundsTransform =  textBoundsTransform
                                    ),
                        )

                        Row {
                            if (homeListItem.type != "album") {
                                Text(
                                    text = "${homeListItem.moreInfoHomeList?.songCount} Songs, ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.LightGray,
                                    maxLines = 1,
                                    softWrap = true,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            Text(
                                text = "Type: ${homeListItem.type}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray,
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        Row(modifier = Modifier) {

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                AnimatedPlayPauseButton(
                                    isPlaying = if (playlistId == apiData?.id) {
                                        isPlaying
                                    } else false,
                                ) {
                                    if (!isAllreadyPlaying) {
                                        apiData?.list?.let { it1 ->
                                            playerViewModel.playPlaylist(
                                                it1,
                                                apiData?.id ?: ""
                                            )
                                        }
                                        isAllreadyPlaying = true
                                        playerViewModel.starter.value = false
                                        playerViewModel.play()
                                        playerViewModel.isPlayingHistory.value = false
                                    }
                                    if (playlistId == apiData?.id) {
                                        if (isPlaying) {
                                            playerViewModel.pause()
                                        } else {
                                            playerViewModel.play()
                                        }
                                    }
                                }

                            }

                            IconButton(onClick = { /* Handle share button click */ }) {
                                Icon(
                                    Icons.Default.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = { /* Handle download button click */ }) {
                                Icon(
                                    Icons.Default.FileDownload,
                                    contentDescription = "Download",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = { /* Handle share button click */ }) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White
                                )
                            }


                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Songs", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(apiData?.list ?: emptyList()) { apiData ->
                        InfoSongRepresent(
                            apiData,
                            playerViewModel,
                            favViewModel,
                            downloaderViewModel,
                            imageColorViewModel
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(125.dp))
                    }
                }
            }
        }

    }
}


@Composable
fun InfoSongRepresent(
    apiData: SongResponse,
    playerViewModel: PlayerViewModel,
    favViewModel: FavoriteViewModel,
    downloaderViewModel: DownloaderViewModel,
    imageColorViewModel: ImageColorGradient
)
{
    val context = LocalContext.current
    val showShimmer = remember { mutableStateOf(true) }
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
}




