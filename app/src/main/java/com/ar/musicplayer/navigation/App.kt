package com.ar.musicplayer.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.ar.musicplayer.data.models.ArtistResult
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
import com.ar.musicplayer.screens.player.CurrPlayingPlaylist
import com.ar.musicplayer.viewmodel.RadioStationViewModel
import com.ar.musicplayer.ui.theme.AppTheme
import com.ar.musicplayer.ui.theme.onPrimaryDark
import com.ar.musicplayer.viewmodel.ThemeViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.internal.decodeStringToJsonTree


@UnstableApi
@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    favViewModel: FavoriteViewModel,
    radioStationViewModel: RadioStationViewModel,
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
            PlayerScreenWithBottomNav(
                navController = navController,
                homeViewModel = homeViewModel,
                playerViewModel = playerViewModel,
                radioStationViewModel = radioStationViewModel,
                downloaderViewModel = downloaderViewModel,
                favViewModel = favViewModel,
                bottomSheetState = bottomSheetState
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerScreenWithBottomNav(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    favViewModel: FavoriteViewModel,
    radioStationViewModel: RadioStationViewModel,
    downloaderViewModel: DownloaderViewModel,
    bottomSheetState: BottomSheetScaffoldState,
    localSongsViewModel: LocalSongsViewModel = hiltViewModel()
) {
    val upNextSheetState = rememberBottomSheetScaffoldState(
        rememberBottomSheetState(Collapsed)
    )

    val themeViewModel = hiltViewModel<ThemeViewModel>()

    val blackToGrayGradient by themeViewModel.blackToGrayGradient
    val listState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()

    val preferencesManager = PreferencesManager(LocalContext.current)


    val showPlayer by playerViewModel.showBottomSheet.collectAsState()


    Log.d("recompose", " recompose called for PlayerScreenWithBottomNav Function")

    BottomSheetScaffold(
        scaffoldState = bottomSheetState,
        sheetPeekHeight = if(showPlayer) 125.dp else 0.dp,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {

            val paletteExtractor = remember { PaletteExtractor() }
            val upNextLazyListState = rememberLazyListState()
            MusicPlayerScreen(
                playerViewModel = playerViewModel,
                bottomSheetState = bottomSheetState,
                onExpand = {
                    if(bottomSheetState.bottomSheetState.isCollapsed){
                        coroutineScope.launch { bottomSheetState.bottomSheetState.expand() }
                    }
                },
                onCollapse = { coroutineScope.launch { bottomSheetState.bottomSheetState.collapse() } },
                paletteExtractor = paletteExtractor,
                downloaderViewModel = downloaderViewModel,
                preferencesManager = preferencesManager,
            )
        },
        sheetBackgroundColor = Color.Transparent,
        modifier = Modifier.navigationBarsPadding(),
        sheetGesturesEnabled = upNextSheetState.bottomSheetState.isCollapsed
    ) {


        NavHost(
            navController = navController,
            startDestination = HomeScreenObj,
            modifier = Modifier
                .padding()
                .fillMaxSize(),
        ) {
            val handleBackNavigation: suspend () -> Unit = {
                when {
                    upNextSheetState.bottomSheetState.isExpanded -> upNextSheetState.bottomSheetState.collapse()
                    bottomSheetState.bottomSheetState.isExpanded -> bottomSheetState.bottomSheetState.collapse()
                }
            }


            composable<HomeScreenObj> {
                HomeScreen(
                    navController = navController,
                    listState = listState,
                    homeViewModel = homeViewModel,
                    radioStationViewModel = radioStationViewModel,
                    playerViewModel = playerViewModel,
                    background = blackToGrayGradient,
                    preferencesManager = preferencesManager
                )
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }


            composable<SearchScreenObj> {
                SearchScreen(navController, playerViewModel, blackToGrayGradient)
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }


            composable<LibraryScreenObj> {
                LibraryScreen(
                    navController = navController,
                    brush = blackToGrayGradient,
                )
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }


            composable<SettingsScreenObj> {
                SettingsScreen(navController,blackToGrayGradient)
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }


            composable<InfoScreenObj> {
                val args = it.toRoute<InfoScreenObj>()
                val data = Json.decodeFromString(InfoScreenModel.serializer(), args.data)

                InfoScreen(
                    data = data,
                    playerViewModel = playerViewModel,
                    favViewModel = favViewModel,
                    downloaderViewModel = downloaderViewModel,
                ){
                    navController.navigateUp()
                }
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }


            composable<FavoriteScreenObj> {
                FavoriteScreen(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    favViewModel = favViewModel,
                )
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }


            composable<ListeningHisScreenObj> {
                ListeningHistoryScreen(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    favViewModel = favViewModel,
                )
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }


            composable<MyMusicScreenObj> {
                MyMusicScreen(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    favViewModel = favViewModel,
                    localSongsViewModel = localSongsViewModel
                )
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }


            composable<SearchMyMusicObj> {
                SearchMyMusic(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    localSongsViewModel = localSongsViewModel
                )
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }


            composable<DetailsScreenObj> {
                val args = it.toRoute<DetailsScreenObj>()
                val playlistResponse = Json.decodeFromString(PlaylistResponse.serializer(), args.playlistResponse)
                DetailsScreen(
                    navController = navController,
                    playlistResponse = playlistResponse,
                    playerViewModel = playerViewModel
                )
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }
            composable<ThemeSettingObj> {
                ThemeSettingsScreen(blackToGrayGradient, themeViewModel,preferencesManager){
                    navController.navigateUp()
                }
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }
            composable<DownloadSettingsScreenObj> {
                DownloadSettingsScreen(
                    preferencesManager = preferencesManager,
                    onBackClick = {
                        navController.navigateUp()
                    }
                )
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }
            composable<LanguageSettingsScreenObj> {
                LanguageSettingsScreen(
                    preferencesManager = preferencesManager,
                    onBackClick = { navController.navigateUp() }
                )
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }
            composable<PlaybackSettingsScreenObj> {
                PlaybackSettingsScreen(
                    preferencesManager = preferencesManager,
                    onBackClick = {navController.navigateUp()}
                )
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }
            composable<StorageSettingScreenObj> {
                StorageSettingScreen(onBackClick = {navController.navigateUp()})
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }
            composable<ArtistInfoScreenObj> {
                val args = it.toRoute<ArtistInfoScreenObj>()
                val artistInfo = Json.decodeFromString(Artist.serializer(), args.artistInfo)
                ArtistInfoScreen(
                    artistInfo = artistInfo
                ){
                    navController.navigateUp()
                }
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }
            composable<PlaylistFetchScreenObj> {
                PlaylistFetchScreen(
                    navController = navController,
                    background = blackToGrayGradient
                )
                BackHandler(
                    enabled = bottomSheetState.bottomSheetState.isExpanded ||
                            upNextSheetState.bottomSheetState.isExpanded
                ) {
                    coroutineScope.launch { handleBackNavigation() }
                }
            }

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
                onClick = {
                    navController.navigate(screen.obj) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
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
        val fraction = bottomSheetState.progress
        val targetValue = bottomSheetState.targetValue
        val currentValue = bottomSheetState.currentValue
        return when {
            fraction != 1f && currentValue == Collapsed-> fraction
            currentValue == Collapsed && targetValue == Collapsed -> 0f
            currentValue == Expanded && targetValue == Expanded -> 1f
            currentValue == Collapsed && targetValue == Expanded -> fraction
            else -> 1f - fraction
        }
    }
