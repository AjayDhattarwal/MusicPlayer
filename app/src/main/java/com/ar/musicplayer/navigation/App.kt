package com.ar.musicplayer.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.ar.musicplayer.screens.player.MusicPlayerScreen
import com.ar.musicplayer.screens.player.PlayerViewModel
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.di.roomdatabase.homescreendb.HomeRoomViewModel
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionViewModel
import com.ar.musicplayer.models.HomeListItem
import com.ar.musicplayer.models.PlaylistResponse
import com.ar.musicplayer.screens.home.HomeScreen
import com.ar.musicplayer.screens.info.InfoScreen
import com.ar.musicplayer.screens.library.LibraryScreen
import com.ar.musicplayer.screens.search.SearchScreen
import com.ar.musicplayer.screens.settings.SettingsScreen
import com.ar.musicplayer.screens.library.favorite.FavoriteScreen
import com.ar.musicplayer.screens.library.history.ListeningHistoryScreen
import com.ar.musicplayer.screens.library.mymusic.DetailsScreen
import com.ar.musicplayer.screens.library.mymusic.MyMusicScreen
import com.ar.musicplayer.screens.library.mymusic.SearchMyMusic
import com.ar.musicplayer.ui.theme.MusicPlayerTheme
import com.ar.musicplayer.utils.helper.PaletteExtractor
import com.ar.musicplayer.utils.playerHelper.DownloaderViewModel
import com.ar.musicplayer.screens.home.HomeViewModel
import com.ar.musicplayer.screens.library.viewmodel.LocalSongsViewModel
import com.ar.musicplayer.screens.home.RadioStationViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    homeRoomViewModel: HomeRoomViewModel,
    modifier: Modifier = Modifier,
    lastSessionViewModel: LastSessionViewModel,
    favViewModel: FavoriteViewModel,
    radioStationViewModel: RadioStationViewModel,
    downloaderViewModel: DownloaderViewModel
) {
    val blackToGrayGradient =
        Brush.verticalGradient(
            colors = listOf(Color(0xFF000000),Color(0xFF161616)),
            startY = Float.POSITIVE_INFINITY,
            endY = 0f
        )
    val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(Collapsed)
    )
    Log.d("recompose", " recompose called for App Function")

    MusicPlayerTheme() {
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
        ) { innerPadding ->
            PlayerScreenWithBottomNav(
                navController = navController,
                homeViewModel = homeViewModel,
                playerViewModel = playerViewModel,
                blackToGrayGradient = blackToGrayGradient,
                homeRoomViewModel = homeRoomViewModel,
                lastSessionViewModel = lastSessionViewModel,
                radioStationViewModel = radioStationViewModel,
                downloaderViewModel = downloaderViewModel,
                favViewModel = favViewModel,
                bottomSheetState = bottomSheetState
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerScreenWithBottomNav(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    blackToGrayGradient: Brush,
    homeRoomViewModel: HomeRoomViewModel,
    lastSessionViewModel: LastSessionViewModel,
    favViewModel: FavoriteViewModel,
    radioStationViewModel: RadioStationViewModel,
    downloaderViewModel: DownloaderViewModel,
    bottomSheetState: BottomSheetScaffoldState,
    localSongsViewModel: LocalSongsViewModel = hiltViewModel()
) {
    val upNextSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(Collapsed)
    )

    val listState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()

    Log.d("recompose", " recompose called for PlayerScreenWithBottomNav Function")

    BottomSheetScaffold(
        scaffoldState = bottomSheetState,
        sheetPeekHeight = 125.dp,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {

            val paletteExtractor = remember { PaletteExtractor() }
            val upNextLazyListState = rememberLazyListState()


            MusicPlayerScreen(
                playerViewModel = playerViewModel,
                bottomSheetState = bottomSheetState,
                upNextSheetState = upNextSheetState,
                onExpand = { coroutineScope.launch { bottomSheetState.bottomSheetState.expand() } },
                onCollapse = { coroutineScope.launch { bottomSheetState.bottomSheetState.collapse() } },
                upNextLazyListState = upNextLazyListState,
                paletteExtractor = paletteExtractor
            )
        },
        sheetBackgroundColor = Color.Transparent,
        modifier = Modifier.navigationBarsPadding(),
    ) {
        BackHandler {
            coroutineScope.launch {
                if (bottomSheetState.bottomSheetState.isExpanded) {
                    bottomSheetState.bottomSheetState.collapse()
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = HomeScreenObj,
            modifier = Modifier.padding().fillMaxSize(),
        ) {
            composable<HomeScreenObj> {
                HomeScreen(
                    navController = navController,
                    listState = listState,
                    homeViewModel = homeViewModel,
                    homeRoomViewModel = homeRoomViewModel,
                    radioStationViewModel = radioStationViewModel,
                    playerViewModel = playerViewModel,
                    lastSessionViewModel = lastSessionViewModel,
                )
            }
            composable<SearchScreenObj> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Log.d("recompose", " recompose called for SearchScreen Function")
                    SearchScreen(navController, playerViewModel)
                }
            }
            composable<LibraryScreenObj> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LibraryScreen(
                        navController = navController,
                        brush = blackToGrayGradient,
                    )
                }
            }
            composable<SettingsScreenObj> {
                SettingsScreen()
            }

            composable<InfoScreenObj> {
                val args = it.toRoute<InfoScreenObj>()
                val deSerialized =
                    Json.decodeFromString(HomeListItem.serializer(), args.serialized)
                val sharedKey = args.sharedKey

                InfoScreen(
                    homeListItem = deSerialized,
                    playerViewModel = playerViewModel,
                    favViewModel = favViewModel,
                    downloaderViewModel = downloaderViewModel,
                ){
                    navController.navigateUp()
                }

            }
            composable<FavoriteScreenObj> {
                FavoriteScreen(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    favViewModel = favViewModel,
                )
            }
            composable<ListeningHisScreenObj> {
                ListeningHistoryScreen(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    favViewModel = favViewModel,
                    lastSessionViewModel = lastSessionViewModel
                )
            }
            composable<MyMusicScreenObj> {
                MyMusicScreen(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    favViewModel = favViewModel,
                    localSongsViewModel = localSongsViewModel
                )
            }
            composable<SearchMyMusicObj> {
                SearchMyMusic(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    localSongsViewModel = localSongsViewModel
                )
            }
            composable<DetailsScreenObj> {
                val args = it.toRoute<DetailsScreenObj>()
                val playlistResponse = Json.decodeFromString(PlaylistResponse.serializer(), args.playlistResponse)
                DetailsScreen(
                    navController = navController,
                    playlistResponse = playlistResponse,
                    playerViewModel = playerViewModel
                )
            }
//                composable<MusicRecognizerObj> {
//                    MusicRecognizer(
//                        navController = navController,
//                        playerViewModel = playerViewModel,
//                        favViewModel = favViewModel
//                    )
//                }
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
        backgroundColor = MaterialTheme.colors.onBackground,
        modifier =  modifier
            .graphicsLayer {
                translationY = size.height * currentFraction
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
                        style = MaterialTheme.typography.body2,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color.Gray
                    )
                },
                selectedContentColor = MaterialTheme.colors.primary,
                unselectedContentColor = MaterialTheme.colors.onBackground
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
