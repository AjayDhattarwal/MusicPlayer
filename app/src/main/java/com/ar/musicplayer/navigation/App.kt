package com.ar.musicplayer.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.ar.musicplayer.data.models.Artist
import com.ar.musicplayer.screens.settings.subscreens.DownloadSettingsScreen
import com.ar.musicplayer.screens.settings.subscreens.LanguageSettingsScreen
import com.ar.musicplayer.screens.settings.subscreens.PlaybackSettingsScreen
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.screens.settings.subscreens.StorageSettingScreen
import com.ar.musicplayer.screens.settings.subscreens.ThemeSettingsScreen
import com.ar.musicplayer.screens.player.MusicPlayerScreen
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.data.models.InfoScreenModel
import com.ar.musicplayer.data.models.PlaylistResponse
import com.ar.musicplayer.data.models.toInfoScreenModel
import com.ar.musicplayer.screens.home.HomeScreen
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
import com.ar.musicplayer.viewmodel.RadioStationViewModel
import com.ar.musicplayer.ui.theme.AppTheme
import com.ar.musicplayer.ui.theme.onPrimaryDark
import com.ar.musicplayer.utils.events.RadioStationEvent
import com.ar.musicplayer.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


@UnstableApi
@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    downloaderViewModel: DownloaderViewModel
) {

    val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(Collapsed)
    )
    Log.d("recompose", " recompose called for App Function")
    AppTheme {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    navController = navController,
                    bottomSheetState = bottomSheetState,
                    modifier = Modifier
                        .systemBarsPadding()
                )

            },
            modifier = modifier
        ) { _ ->
            AppMainScreen(
                navController = navController,
                homeViewModel = homeViewModel,
                playerViewModel = playerViewModel,
                downloaderViewModel = downloaderViewModel,
                bottomSheetState = bottomSheetState
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun AppMainScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    downloaderViewModel: DownloaderViewModel,
    bottomSheetState: BottomSheetScaffoldState,
    localSongsViewModel: LocalSongsViewModel = hiltViewModel()
) {

    val themeViewModel = hiltViewModel<ThemeViewModel>()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val preferencesManager = PreferencesManager(LocalContext.current)


    val showPlayer by playerViewModel.showBottomSheet.collectAsState()

    val paletteExtractor = remember { PaletteExtractor() }


    Log.d("recompose", " recompose called for PlayerScreenWithBottomNav Function")

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
                )
            }
        },
        sheetBackgroundColor = Color.Transparent,
        modifier = Modifier.navigationBarsPadding(),

    ) {
        key(homeViewModel) {
            MainScreenContent(
                navController = navController,
                listState = listState,
                playerViewModel = playerViewModel,
                preferencesManager = preferencesManager,
                localSongsViewModel = localSongsViewModel,
                themeViewModel = themeViewModel
            )
        }
    }
}


@UnstableApi
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreenContent(
    navController: NavHostController,
    listState: LazyListState,
    playerViewModel: PlayerViewModel,
    preferencesManager: PreferencesManager,
    localSongsViewModel: LocalSongsViewModel,
    themeViewModel: ThemeViewModel
) {
    val radioStationSelection = remember {
        mutableStateOf(false)
    }


    val backgroundBrush by themeViewModel.blackToGrayGradient

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


            LaunchedEffect (radioSongResponse) {
                Log.d("radio", "is active ")
                if (radioSongResponse.isNotEmpty() && radioStationSelection.value) {
                    playerViewModel.setPlaylist(radioSongResponse, "radio")
                    radioStationSelection.value = false
                }
            }
            HomeScreen(
                background = backgroundBrush,
                listState = listState,
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
                            val serializedData = Json.encodeToString(InfoScreenModel.serializer(), data.toInfoScreenModel())
                            navController.navigate(InfoScreenObj(serializedData))
                        }
                    }
                }
            )

        }


        composable<SearchScreenObj> {
            SearchScreen(
                background = backgroundBrush,
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
                }

            )

        }


        composable<LibraryScreenObj> {
            LibraryScreen(
                background = backgroundBrush,
                onScreenSelect = remember {
                    { path ->
                        navController.navigate(path)
                    }
                }
            )
        }


        composable<SettingsScreenObj> {
            SettingsScreen(
                background = backgroundBrush,
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
                data = data,
                onBackPressed = remember {
                    { navController.navigateUp() }
                }
            )
        }


        composable<FavoriteScreenObj> {
            FavoriteScreen(
                background = backgroundBrush,
                onBackPressed = remember {
                    { navController.navigateUp() }
                }
            )

        }


        composable<ListeningHisScreenObj> {
            ListeningHistoryScreen(
                background = backgroundBrush,
                onBackPressed = remember {
                    {
                        navController.navigateUp()
                    }
                },
            )

        }


        composable<MyMusicScreenObj> {
            MyMusicScreen(
                background = backgroundBrush,
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
            ThemeSettingsScreen(backgroundBrush, themeViewModel, preferencesManager) {
                navController.navigateUp()
            }

        }
        composable<DownloadSettingsScreenObj> {
            DownloadSettingsScreen(
                preferencesManager = preferencesManager,
                onBackClick = {
                    navController.navigateUp()
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
            StorageSettingScreen(onBackClick = { navController.navigateUp() })

        }
        composable<ArtistInfoScreenObj> {
            val args = it.toRoute<ArtistInfoScreenObj>()
            val artistInfo = Json.decodeFromString(Artist.serializer(), args.artistInfo)
            ArtistInfoScreen(
                artistInfo = artistInfo
            ) {
                navController.navigateUp()
            }

        }
        composable<PlaylistFetchScreenObj> {
            PlaylistFetchScreen(
                navController = navController,
                background = backgroundBrush
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
                onPrimaryDark
            }
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomScreens.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.obj::class.qualifiedName } == true
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
            )
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

