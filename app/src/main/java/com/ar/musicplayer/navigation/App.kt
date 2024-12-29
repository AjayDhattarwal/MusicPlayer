@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package com.ar.musicplayer.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetValue.Collapsed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.ar.musicplayer.R
import com.ar.musicplayer.components.home.AnimatedAIFloatingActionButton
import com.ar.musicplayer.components.modifier.shader
import com.ar.musicplayer.data.models.Artist
import com.ar.musicplayer.screens.library.viewmodel.LocalSongsViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.utils.helper.PaletteExtractor
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.screens.player.AdaptiveDetailsPlayer
import com.ar.musicplayer.screens.player.AdaptiveMiniPlayer
import com.ar.musicplayer.screens.player.CurrPlayingPlaylist
import com.ar.musicplayer.screens.player.AdaptiveMaxPlayer
import com.ar.musicplayer.ui.MusicAppState
import com.ar.musicplayer.ui.WindowInfoVM
import com.ar.musicplayer.ui.rememberAppState
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.viewmodel.ImportViewModel
import com.ar.musicplayer.viewmodel.ThemeViewModel
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.adaptive.calculateDisplayFeatures
import kotlinx.serialization.json.Json
import kotlin.ranges.coerceIn


@UnstableApi
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class
)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App(
    appState: MusicAppState = rememberAppState(),
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    downloaderViewModel: DownloaderViewModel,
    favoriteViewModel: FavoriteViewModel,
    windowInfoVm: WindowInfoVM,
    localSongsViewModel: LocalSongsViewModel,
    importViewModel: ImportViewModel
) {
    val themeViewModel = hiltViewModel<ThemeViewModel>()
    val backgroundBrush by themeViewModel.blackToGrayGradient.collectAsState()
    val backgroundColors by themeViewModel.backgroundColors.collectAsState()
    val height by windowInfoVm.maxPlayerImageHeight.collectAsState()

    val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(Collapsed)
    )


    val paletteExtractor = remember { PaletteExtractor() }

    val showBottomBar by windowInfoVm.showBottomBar.collectAsStateWithLifecycle()
    val isCompatHeight by windowInfoVm.isCompatHeight.collectAsState()
    val isCompatWidth by windowInfoVm.isCompatWidth.collectAsState()
    val showPreviewScreen by windowInfoVm.showPreviewScreen.collectAsState()
    val isMusicDetailsVisible by windowInfoVm.isMusicDetailsVisible.collectAsState()
    val isFullScreenPlayer by windowInfoVm.isFullScreenPlayer.collectAsState()

    val showPlayer by playerViewModel.showBottomSheet.collectAsState()

    val context = LocalContext.current

    var offsetX by remember { mutableStateOf(0.6f) }



    Box(
        modifier =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Modifier.shader(backgroundColors)
            } else {
                Modifier.drawBehind { drawRect(brush = backgroundBrush) }
            }
    ){

        Scaffold(
            bottomBar = {
                if(showBottomBar){
                    BottomNavigationBar(
                        appState = appState,
                        bottomSheetState = bottomSheetState,
                        modifier = Modifier
                            .systemBarsPadding()

                    )
                }
            },
            floatingActionButton = {
                if(!isCompatHeight){
                    AnimatedAIFloatingActionButton(
                        isCompatWidth = isCompatWidth,
                        onArtistClick = remember{
                            { artist ->
                                val senderData = Json.encodeToString(Artist.serializer(), artist)
                                appState.navigate(ArtistInfoScreenObj(senderData))
                            }
                        },
                        onSongClick = remember {
                            {
                                playerViewModel.setNewTrack(it)
                            }
                        }
                    )
                }
            },
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
        ){ _ ->
            Box(){
                Row {
                    if (!showBottomBar) {
                        NavigationRailBar(appState)
                    }
                    TwoPane(
                        displayFeatures = calculateDisplayFeatures(context as Activity),
                        first = {
                            AppMainScreen(
                                showPlayerSheet = showBottomBar,
                                appState = appState,
                                homeViewModel = homeViewModel,
                                playerViewModel = playerViewModel,
                                downloaderViewModel = downloaderViewModel,
                                favoriteViewModel = favoriteViewModel,
                                bottomSheetState = bottomSheetState,
                                windowInfoVm = windowInfoVm,
                                localSongsViewModel = localSongsViewModel,
                                importViewModel = importViewModel
                            )
                        },
                        second = {
                            if (showPlayer && isMusicDetailsVisible && showPreviewScreen ) {
                                var showCurrentPlaylist by remember { mutableStateOf(false) }
                                Surface(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Transparent)
                                        .padding(bottom = 100.dp),
                                    color = Color.Transparent
                                ) {
                                    DraggableVerticalLine(
                                        onPositionChange = {
                                            offsetX = it
                                        }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(start = 3.dp)
                                            .drawBehind {
                                                drawRect(color = Color(0x1E999999))
                                            },
                                    ) {
                                        if (!showCurrentPlaylist) {
                                            AdaptiveDetailsPlayer(
                                                playerViewModel = playerViewModel,
                                                isAdaptive = true,
                                                onExpand = { /*TODO*/ },
                                                onCollapse = remember { { windowInfoVm.closeMusicPreview() } },
                                                onQueue = { showCurrentPlaylist = true },
                                                paletteExtractor = paletteExtractor,
                                                downloaderViewModel = downloaderViewModel,
                                                favoriteViewModel = favoriteViewModel,
                                                modifier = Modifier
                                            )
                                        } else {
                                            Column {
                                                Box {
                                                    CenterAlignedTopAppBar(
                                                        title = {
                                                            Text(
                                                                text = "Current Playing",
                                                                color = Color.White
                                                            )
                                                        },
                                                        navigationIcon = {
                                                            IconButton(onClick = remember {
                                                                {
                                                                    showCurrentPlaylist = false
                                                                }
                                                            }) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Clear,
                                                                    contentDescription = "Close",
                                                                    tint = Color.White
                                                                )
                                                            }
                                                        },
                                                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                                            containerColor = Color.Transparent
                                                        )

                                                    )
                                                }
                                                CurrPlayingPlaylist(playerViewModel = playerViewModel)
                                            }

                                        }

                                    }
                                }

                            }
                        },
                        strategy = HorizontalTwoPaneStrategy(if (showPlayer && isMusicDetailsVisible && showPreviewScreen) offsetX else 1f)
                    )
                }

                if (showPlayer && showPreviewScreen ) {

                    Box(modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .systemBarsPadding()){
                        SharedTransitionLayout {
                            AnimatedContent(
                                isFullScreenPlayer,
                                label = "fullScreen_transition"
                            ) { targetState ->
                                if (!targetState) {
                                    AdaptiveMiniPlayer(
                                        playerViewModel = playerViewModel,
                                        animatedVisibilityScope = this@AnimatedContent,
                                        sharedTransitionScope = this@SharedTransitionLayout,
                                        paletteExtractor = paletteExtractor,
                                        onExpand = remember { {
                                            if (!isMusicDetailsVisible) {
                                                windowInfoVm.showMusicPreview()
                                            }
                                        } },
                                        content = {
                                            IconButton(
                                                modifier = Modifier,
                                                onClick = remember {
                                                    {
                                                        windowInfoVm.toFullScreen()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = ImageVector.vectorResource(R.drawable.ic_open_in_full),
                                                    contentDescription = "preview",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    )
                                }
                                else {
                                    if(!appState.navController.isMaxPlayer() && isFullScreenPlayer){
                                        windowInfoVm.closeMusicPreview()
                                        appState.navigate(LargeScreenPlayerObj)
                                    }
                                    AdaptiveMaxPlayer(
                                        playerViewModel = playerViewModel,
                                        height = height,
                                        onBack = remember{
                                            {
                                                appState.navigateBack()
                                                windowInfoVm.closeFullScreen()
                                            }
                                        },
                                        animatedVisibilityScope = this@AnimatedContent,
                                        sharedTransitionScope = this@SharedTransitionLayout,
                                        favoriteViewModel = favoriteViewModel
                                    )
                                    BackHandler {
                                        appState.navigateBack()
                                        windowInfoVm.closeFullScreen()
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



fun NavHostController.isMaxPlayer(): Boolean {
    return this.currentDestination
        ?.route
        ?.substringAfterLast(".") == "LargeScreenPlayerObj"
}



@Composable
fun DraggableVerticalLine(
    initialPosition: Float = 0.6f,
    minPosition: Float = 0.4f,
    maxPosition: Float = 0.7f,
    onPositionChange: (Float) -> Unit = {}
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()

    var linePosition by remember { mutableStateOf(initialPosition) }
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(modifier = Modifier
            .width(3.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.LightGray)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    val newOffset = linePosition + delta / screenWidth
                    linePosition = newOffset.coerceIn(minPosition, maxPosition)
                    onPositionChange(linePosition)
                }
            )
            .pointerInput(Unit) {
                detectTapGestures {
                    linePosition = initialPosition
                    onPositionChange(linePosition)
                }
            }
        )
    }
}



