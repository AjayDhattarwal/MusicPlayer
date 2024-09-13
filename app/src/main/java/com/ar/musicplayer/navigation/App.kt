@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package com.ar.musicplayer.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetValue.Collapsed
import androidx.compose.material.BottomSheetValue.Expanded
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.ar.musicplayer.components.home.AnimatedAIFloatingActionButton
import com.ar.musicplayer.components.modifier.shader
import com.ar.musicplayer.data.models.Artist
import com.ar.musicplayer.screens.settings.subscreens.DownloadSettingsScreen
import com.ar.musicplayer.screens.settings.subscreens.LanguageSettingsScreen
import com.ar.musicplayer.screens.settings.subscreens.PlaybackSettingsScreen
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.screens.settings.subscreens.StorageSettingScreen
import com.ar.musicplayer.screens.settings.subscreens.ThemeSettingsScreen
import com.ar.musicplayer.screens.player.MusicPlayerScreen
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.data.models.InfoScreenModel
import com.ar.musicplayer.data.models.PlaylistResponse
import com.ar.musicplayer.data.models.toInfoScreenModel
import com.ar.musicplayer.screens.home.AdaptiveHomeScreen
import com.ar.musicplayer.screens.info.ArtistInfoScreen
import com.ar.musicplayer.screens.info.InfoScreen
import com.ar.musicplayer.screens.library.LibraryScreen
import com.ar.musicplayer.screens.search.SearchScreen
import com.ar.musicplayer.screens.settings.SettingsScreen
import com.ar.musicplayer.screens.library.favorite.FavoriteScreen
import com.ar.musicplayer.screens.library.history.ListeningHistoryScreen
import com.ar.musicplayer.screens.library.mymusic.DetailsScreen
import com.ar.musicplayer.screens.library.mymusic.MyMusicScreen
import com.ar.musicplayer.screens.library.mymusic.SearchMyMusic
import com.ar.musicplayer.screens.library.playlist.PlaylistFetchScreen
import com.ar.musicplayer.utils.helper.PaletteExtractor
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.screens.library.viewmodel.LocalSongsViewModel
import com.ar.musicplayer.screens.player.AdaptiveDetailsPlayer
import com.ar.musicplayer.screens.player.AdaptiveMiniPlayer
import com.ar.musicplayer.screens.player.CurrPlayingPlaylist
import com.ar.musicplayer.screens.player.AdaptiveMaxPlayer
import com.ar.musicplayer.ui.theme.WindowInfoVM
import com.ar.musicplayer.viewmodel.RadioStationViewModel
import com.ar.musicplayer.ui.theme.onPrimaryDark
import com.ar.musicplayer.utils.events.RadioStationEvent
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.viewmodel.AiViewModel
import com.ar.musicplayer.viewmodel.MoreInfoViewModel
import com.ar.musicplayer.viewmodel.ThemeViewModel
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.adaptive.calculateDisplayFeatures
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


@UnstableApi
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class
)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    downloaderViewModel: DownloaderViewModel,
    favoriteViewModel: FavoriteViewModel,
    windowInfoVm: WindowInfoVM
) {
    val scope = rememberCoroutineScope()
    val themeViewModel = hiltViewModel<ThemeViewModel>()
    val backgroundBrush by themeViewModel.blackToGrayGradient.collectAsState()
    val backgroundColors by themeViewModel.backgroundColors.collectAsState()

    val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(Collapsed)
    )

    val paletteExtractor = remember { PaletteExtractor() }

    val showBottomBar by windowInfoVm.showBottomBar.collectAsStateWithLifecycle()
    val showPreviewScreen by windowInfoVm.showPreviewScreen.collectAsState()
    val isMusicDetailsVisible by windowInfoVm.isMusicDetailsVisible.collectAsState()
    val isFullScreenPlayer by windowInfoVm.isFullScreenPlayer.collectAsState()

    val showPlayer by playerViewModel.showBottomSheet.collectAsState()

    val context = LocalContext.current


    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
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
                        navController = navController,
                        bottomSheetState = bottomSheetState,
                        modifier = Modifier
                            .systemBarsPadding()

                    )
                }
            },
            floatingActionButton = {
                if(!showBottomBar){
                    AnimatedAIFloatingActionButton(

                        onArtistClick = remember{
                            { artist ->
                                val senderData = Json.encodeToString(Artist.serializer(), artist)
                                navController.navigate(
                                    ArtistInfoScreenObj(senderData)
                                )
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
                    if (!showBottomBar && !isFullScreenPlayer) {
                        NavigationRailBar(navController)
                    }
                    TwoPane(
                        displayFeatures = calculateDisplayFeatures(context as Activity),
                        first = {
                            AppMainScreen(
                                showPlayerSheet = showBottomBar,
                                navController = navController,
                                homeViewModel = homeViewModel,
                                playerViewModel = playerViewModel,
                                downloaderViewModel = downloaderViewModel,
                                favoriteViewModel = favoriteViewModel,
                                bottomSheetState = bottomSheetState,
                                windowInfoVm = windowInfoVm
                            )
                        },
                        second = {
                            if (showPlayer && isMusicDetailsVisible && showPreviewScreen ) {
                                var showCurrentPlaylist by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 100.dp)
                                ) {
                                    Box(modifier = Modifier
                                        .width(3.dp)
                                        .height(100.dp)
                                        .align(Alignment.CenterVertically)
                                        .background(Color.LightGray)
                                        .draggable(
                                            orientation = Orientation.Horizontal,
                                            state = rememberDraggableState { delta ->
                                                val newOffset =
                                                    (offsetX + delta / screenWidth).coerceIn(
                                                        0.4f,
                                                        0.7f
                                                    )
                                                offsetX = newOffset
                                            }
                                        )
                                        .pointerInput(true) {
                                            detectTapGestures(
                                                onTap = {
                                                    offsetX = 0.6f
                                                }
                                            )
                                        }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
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
                                        onCollapse = { /*TODO*/ },
                                        animatedVisibilityScope = this@AnimatedContent,
                                        sharedTransitionScope = this@SharedTransitionLayout,
                                        paletteExtractor = paletteExtractor,
                                        downloaderViewModel = downloaderViewModel,
                                        modifier = Modifier.clickable {
                                            if (!isMusicDetailsVisible)
                                                windowInfoVm.showMusicPreview()
                                        },
                                        content = {
                                            IconButton(
                                                modifier = Modifier,
                                                onClick = remember {
                                                    {
                                                        navController.navigate(LargeScreenPlayerObj)
                                                        windowInfoVm.toFullScreen()
                                                    }
                                                }) {
                                                Icon(
                                                    imageVector = Icons.Default.OpenInFull,
                                                    contentDescription = "preview",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    )
                                }
                                else {
                                    AdaptiveMaxPlayer(
                                        playerViewModel = playerViewModel,
                                        onBack = remember{
                                            {
                                                navController.navigateUp()
                                                windowInfoVm.closeFullScreen()
                                            }
                                        },
                                        animatedVisibilityScope = this@AnimatedContent,
                                        sharedTransitionScope = this@SharedTransitionLayout
                                    )
                                    BackHandler {
                                        navController.navigateUp()
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

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun AppMainScreen(
    showPlayerSheet: Boolean,
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    downloaderViewModel: DownloaderViewModel,
    bottomSheetState: BottomSheetScaffoldState,
    localSongsViewModel: LocalSongsViewModel = hiltViewModel(),
    windowInfoVm: WindowInfoVM,
    favoriteViewModel: FavoriteViewModel
) {

    val themeViewModel = hiltViewModel<ThemeViewModel>()
    val moreInfoViewModel = hiltViewModel<MoreInfoViewModel>()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val preferencesManager = remember {  PreferencesManager(context)}


    val showPlayer by playerViewModel.showBottomSheet.collectAsState()


    val paletteExtractor = remember { PaletteExtractor() }


    Log.d("recompose", " recompose called for PlayerScreenWithBottomNav Function")

    if(showPlayerSheet){
        BottomSheetScaffold(
            scaffoldState = bottomSheetState,
            sheetPeekHeight = if(showPlayer) 125.dp else 0.dp,
            sheetContent = {
                key(showPlayer) {
                    MusicPlayerScreen(
                        playerViewModel = playerViewModel,
                        bottomSheetState = bottomSheetState,
                        onExpand = remember{
                            {
                                if (bottomSheetState.bottomSheetState.isCollapsed) {
                                    coroutineScope.launch { bottomSheetState.bottomSheetState.expand() }
                                }
                            }
                        },
                        onCollapse = remember{ { coroutineScope.launch { bottomSheetState.bottomSheetState.collapse() } } },
                        paletteExtractor = paletteExtractor,
                        downloaderViewModel = downloaderViewModel,
                        favoriteViewModel = favoriteViewModel
                    )
                }
            },
            sheetBackgroundColor = Color.Transparent,
            sheetContentColor = Color.Transparent,
            backgroundColor = Color.Transparent,
            modifier = Modifier.navigationBarsPadding()
        ) {
            MainScreenContent(
                navController = navController,
                listState = listState,
                windowInfoVm = windowInfoVm,
                homeViewModel = homeViewModel,
//                playerViewModel = playerViewModel,
                moreInfoViewModel = moreInfoViewModel,
                preferencesManager = preferencesManager,
                localSongsViewModel = localSongsViewModel,
                themeViewModel = themeViewModel
            )
        }

    } else{
        MainScreenContent(
            navController = navController,
            listState = listState,
            windowInfoVm = windowInfoVm,
            homeViewModel = homeViewModel,
//            playerViewModel = playerViewModel,
            moreInfoViewModel = moreInfoViewModel,
            preferencesManager = preferencesManager,
            localSongsViewModel = localSongsViewModel,
            themeViewModel = themeViewModel
        )
    }
}


@UnstableApi
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreenContent(
    navController: NavHostController,
    listState: LazyListState,
    windowInfoVm: WindowInfoVM,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    moreInfoViewModel: MoreInfoViewModel,
    preferencesManager: PreferencesManager,
    localSongsViewModel: LocalSongsViewModel,
    themeViewModel: ThemeViewModel
) {
    val radioStationSelection = remember {
        mutableStateOf(false)
    }

    NavHost(
        navController = navController,
        startDestination = HomeScreenObj,
        modifier = Modifier
            .padding()
            .fillMaxSize(),
    ) {

        composable<HomeScreenObj> {
            val radioStationViewModel = hiltViewModel<RadioStationViewModel>()
            val radioSongResponse by radioStationViewModel.radioStation.collectAsState()
            val showPreviewScreen by windowInfoVm.showPreviewScreen.collectAsStateWithLifecycle()

            LaunchedEffect (radioSongResponse) {
                if (radioSongResponse.isNotEmpty() && radioStationSelection.value) {
                    playerViewModel.setPlaylist(radioSongResponse, "radio")
                    radioStationSelection.value = false
                }
            }


            AdaptiveHomeScreen(
                windowInfoVM = windowInfoVm,
                homeViewModel = homeViewModel,
                playerViewModel = playerViewModel,
                moreInfoViewModel = moreInfoViewModel,
//                listState = listState,
                navigateSetting = remember {
                    {
                        navController.navigate(SettingsScreenObj)
                    }
                },
                onItemClick = remember {
                    { radio, data ->
                        if(radio){
                            val query = if(data.moreInfoHomeList?.query != "") data.moreInfoHomeList?.query else data.title
                            radioStationViewModel.onEvent(
                                RadioStationEvent.LoadRadioStationData(
                                    call = "webradio.getSong",
                                    k = "20",
                                    next = "1",
                                    name = query.toString(),
                                    query = query.toString(),
                                    radioStationType = data.moreInfoHomeList?.stationType.toString(),
                                    language = data.moreInfoHomeList?.language.toString()
                                ))
                            radioStationSelection.value = true
                        } else{

                            if(!showPreviewScreen){
                                val serializedData = Json.encodeToString(
                                    InfoScreenModel.serializer(),
                                    data.toInfoScreenModel()
                                )
                                navController.navigate(InfoScreenObj(serializedData))
                            }
                        }
                    }
                }
            )

        }

        composable<LargeScreenPlayerObj> {
//            AdaptiveMaxPlayer({
//                showDetails = false
//            }, this@AnimatedContent, this@SharedTransitionLayout)
//            BackHandler {
//                navController.navigateUp()
//                windowInfoVm.closeFullScreen()
//            }
        }


        composable<SearchScreenObj> {
            SearchScreen(
                playerViewModel = playerViewModel,
                onArtistClick = remember{
                    { artist ->
                        val senderData = Json.encodeToString(Artist.serializer(), artist)
                        navController.navigate(
                            ArtistInfoScreenObj(senderData)
                        )
                    }
                },
                onPlaylistClick = remember {
                    { infoScreenModel ->
                        val senderData = Json.encodeToString(InfoScreenModel.serializer(), infoScreenModel)
                        navController.navigate(InfoScreenObj(senderData))
                    }
                },

            )

        }


        composable<LibraryScreenObj> {
            LibraryScreen(
                onScreenSelect = remember {
                    { path ->
                        navController.navigate(path)
                    }
                }
            )
        }


        composable<SettingsScreenObj> {
            SettingsScreen(
                onBackPressed = remember {
                    { navController.navigateUp() }
                },
                onNavigate = remember {
                    { path ->
                        navController.navigate(path)
                    }
                }
            )

        }


        composable<InfoScreenObj> {
            val args = it.toRoute<InfoScreenObj>()
            val data = Json.decodeFromString(InfoScreenModel.serializer(), args.data)
            InfoScreen(
                moreInfoViewModel = moreInfoViewModel,
                data = data,
                onBackPressed = remember {
                    { navController.navigateUp() }
                }
            )
        }


        composable<FavoriteScreenObj> {
            FavoriteScreen(
                onBackPressed = remember {
                    { navController.navigateUp() }
                }
            )

        }


        composable<ListeningHisScreenObj> {
            ListeningHistoryScreen(
                onBackPressed = remember {
                    {
                        navController.navigateUp()
                    }
                },
            )

        }


        composable<MyMusicScreenObj> {
            MyMusicScreen(
                onBackPressed = remember {
                    {
                        navController.navigateUp()
                    }
                },
                onNavigate = remember {
                    {
                        navController.navigate(it)
                    }
                }
            )

        }


        composable<SearchMyMusicObj> {
            SearchMyMusic(
                onBackPressed = remember {
                    { navController.navigateUp() }
                }
            )

        }


        composable<DetailsScreenObj> {
            val args = it.toRoute<DetailsScreenObj>()
            val playlistResponse =
                Json.decodeFromString(PlaylistResponse.serializer(), args.playlistResponse)
            DetailsScreen(
                navController = navController,
                playlistResponse = playlistResponse,
                playerViewModel = playerViewModel
            )

        }
        composable<ThemeSettingObj> {
            ThemeSettingsScreen(
                themeViewModel = themeViewModel,
                onBackClick = remember{ { navController.navigateUp() } }
            )
        }
        composable<DownloadSettingsScreenObj> {
            DownloadSettingsScreen(
                onBackClick = remember {
                    {
                        navController.navigateUp()
                    }
                }
            )

        }
        composable<LanguageSettingsScreenObj> {
            LanguageSettingsScreen(
                preferencesManager = preferencesManager,
                onBackClick = { navController.navigateUp() }
            )

        }
        composable<PlaybackSettingsScreenObj> {
            PlaybackSettingsScreen(
                preferencesManager = preferencesManager,
                onBackClick = { navController.navigateUp() }
            )

        }
        composable<StorageSettingScreenObj> {
            StorageSettingScreen(onBackClick = remember{ { navController.navigateUp() } })
        }
        composable<ArtistInfoScreenObj> {
            val args = it.toRoute<ArtistInfoScreenObj>()
            val artistInfo = Json.decodeFromString(Artist.serializer(), args.artistInfo)
            ArtistInfoScreen(
                artistInfo = artistInfo,
                onArtistClick = remember { { _,_ ->

                }},
                onPlaylistClick = remember { { _,_ ->

                } },
                onAlbumClick =  remember { { _,_ ->

                } },
                onSongClick = remember { { song ->
                    playerViewModel.setNewTrack(song)
                } },
                onBackPressed = remember { {
                    navController.navigateUp()
                } }
            )

        }

        composable<PlaylistFetchScreenObj> {
            PlaylistFetchScreen(
                navController = navController
            )

        }

    }

}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomNavigationBar(navController: NavController, bottomSheetState: BottomSheetScaffoldState, modifier: Modifier = Modifier) {
    val bottomScreens = remember {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Search,
            BottomNavItem.Library,
        )
    }

    val currentFraction by remember {
        derivedStateOf {
            bottomSheetState.currentFraction
        }
    }


    BottomNavigation(
        backgroundColor = Color.Black,
        modifier =  modifier
            .graphicsLayer {
                translationY = size.height * currentFraction
//                onPrimaryDark
            }
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomScreens.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.route == screen.obj::class.qualifiedName
            } == true

            BottomNavigationItem(
                selected = isSelected,
                onClick = remember {
                    {
                        navController.navigate(screen.obj) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        tint =  if (isSelected) Color.White else Color.Gray,
                    )
                },
                label = {
                    Text(
                        text = screen.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color =  if (isSelected) Color.White else Color.Gray,
                    )
                },
                modifier = Modifier.background(color = Color.Transparent)
            )
        }
    }
}

@Composable
fun NavigationRailBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val railScreens = remember {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Search,
            BottomNavItem.Library,
        )
    }

    NavigationRail(
        containerColor = Color.Transparent,
        contentColor = Color.Transparent,
        modifier = modifier,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            railScreens.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.obj::class.qualifiedName } == true
                NavigationRailItem(
                    selected = isSelected,
                    onClick = remember {
                        {
                            navController.navigate(screen.obj) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.label,
                        )
                    },
                    label = {
                        Text(
                            text = screen.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}


sealed class BottomNavItem<T>(  val obj: T, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem<HomeScreenObj>( HomeScreenObj, Icons.Default.Home, "Home")
    object Search : BottomNavItem<SearchScreenObj>( SearchScreenObj, Icons.Default.Search, "Search")
    object Library: BottomNavItem<LibraryScreenObj>(LibraryScreenObj,Icons.Default.LibraryMusic, "Library")
}




@OptIn(ExperimentalMaterialApi::class)
val BottomSheetScaffoldState.currentFraction: Float
    get() {
        val fraction = this.bottomSheetState.progress
        val targetValue = this.bottomSheetState.targetValue
        val currentValue = this.bottomSheetState.currentValue
        return when {
            fraction != 1f && currentValue == Collapsed -> fraction
            currentValue == Collapsed && targetValue == Collapsed -> 0f
            currentValue == Expanded && targetValue == Expanded -> 1f
            currentValue == Collapsed && targetValue == Expanded -> fraction
            else -> 1f - fraction
        }
    }

