package com.ar.musicplayer.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.BottomSheetValue
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.di.roomdatabase.homescreendb.HomeRoomViewModel
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionEvent
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionViewModel
import com.ar.musicplayer.models.HomeListItem
import com.ar.musicplayer.models.PlaylistResponse
import com.ar.musicplayer.screens.HomeScreen
import com.ar.musicplayer.screens.InfoScreen
import com.ar.musicplayer.screens.LibraryScreen
import com.ar.musicplayer.screens.PlayerScreen
import com.ar.musicplayer.screens.SearchScreen
import com.ar.musicplayer.screens.SettingsScreen
import com.ar.musicplayer.screens.libraryScreens.FavoriteScreen
import com.ar.musicplayer.screens.libraryScreens.ListeningHistoryScreen
import com.ar.musicplayer.screens.libraryScreens.mymusic.DetailsScreen
import com.ar.musicplayer.screens.libraryScreens.mymusic.MyMusicScreen
import com.ar.musicplayer.screens.libraryScreens.mymusic.SearchMyMusic
import com.ar.musicplayer.ui.theme.MusicPlayerTheme
import com.ar.musicplayer.utils.MusicPlayer
import com.ar.musicplayer.utils.playerHelper.DownloaderViewModel
import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.viewmodel.ImageColorGradient
import com.ar.musicplayer.viewmodel.LocalSongsViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.viewmodel.RadioStationViewModel
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
    musicPlayer: MusicPlayer,
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
    val isBottomNavVisible by playerViewModel.isBottomNavVisible.collectAsState()
    val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed, animationSpec = tween(durationMillis = 600))
    )

    MusicPlayerTheme() {
        Scaffold(
            bottomBar = {
                    AnimatedVisibility(
                        visible = bottomSheetState.currentFraction < 0.05 ,
                        enter = fadeIn( animationSpec = tween(durationMillis = 400)),
                        exit =  fadeOut(animationSpec = tween(durationMillis = 400))
                    ) {
                        BottomNavigationBar(navController = navController , modifier =  Modifier.systemBarsPadding())
                    }
            },
            modifier = modifier
        ) { innerPadding ->
            PlayerScreenWithBottomNav(
                navController = navController,
                homeViewModel = homeViewModel,
                playerViewModel = playerViewModel,
                blackToGrayGradient = blackToGrayGradient,
                musicPlayer = musicPlayer,
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


@OptIn(ExperimentalMaterialApi::class, ExperimentalSharedTransitionApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun PlayerScreenWithBottomNav(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    blackToGrayGradient: Brush,
    musicPlayer: MusicPlayer,
    homeRoomViewModel: HomeRoomViewModel,
    lastSessionViewModel: LastSessionViewModel,
    favViewModel: FavoriteViewModel,
    radioStationViewModel: RadioStationViewModel,
    downloaderViewModel: DownloaderViewModel,
    bottomSheetState: BottomSheetScaffoldState,
) {

    val listState = rememberLazyListState()

    val imageColorGradient = viewModel<ImageColorGradient>()

    val coroutineScope = rememberCoroutineScope()

    val localSongsViewModel = viewModel<LocalSongsViewModel>()

    val lastSession by lastSessionViewModel.lastSession.observeAsState()

    val songResponse by playerViewModel.currentSong.collectAsState()

    if(songResponse == null){
        lastSession?.let {
            if (it.isNotEmpty()) {
                Log.d("lastSession","value assigned")
                coroutineScope.launch {
                    playerViewModel.playlist.value = it
                    playerViewModel.isPlayingHistory.value = true
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        if(songResponse == null){
            Log.d("lastSession","startLoading")
            lastSessionViewModel.onEvent(LastSessionEvent.LoadLastSessionData)
        }
    }

    Log.d("bottomSheetState", "${bottomSheetState} vs  target  :::   ")


    val isExpanded by remember { derivedStateOf { bottomSheetState.bottomSheetState.isExpanded } }

    LaunchedEffect(isExpanded) {
        playerViewModel.setBottomNavVisibility(!isExpanded)
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetState,
        sheetPeekHeight = 125.dp,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            PlayerScreen(
                playerViewModel = playerViewModel,
                onExpand = {
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.expand()
                    }
                },
                onCollapse = {
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                },
                bottomSheetState = bottomSheetState,
                musicPlayer = musicPlayer,
                lastSessionViewModel = lastSessionViewModel,
                favViewModel = favViewModel,
                downloaderViewModel  = downloaderViewModel
            )
        },
        sheetBackgroundColor = Color.Transparent,
        modifier = Modifier.navigationBarsPadding()
    ){

        if (bottomSheetState.bottomSheetState.isExpanded) {
            BackHandler {
                coroutineScope.launch {
                    bottomSheetState.bottomSheetState.collapse()
                }
            }
        }
        NavHost(
            navController = navController,
            startDestination = HomeScreenObj,
            modifier = Modifier
                .padding()
                .fillMaxSize(),
        ) {
            composable<HomeScreenObj> {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HomeScreen(
                        navController = navController,
                        listState = listState,
                        homeViewModel = homeViewModel,
                        homeRoomViewModel = homeRoomViewModel,
                        radioStationViewModel =  radioStationViewModel,
                        playerViewModel = playerViewModel,
                        lastSessionViewModel = lastSessionViewModel,
                        imageColorViewModel = imageColorGradient,
                    )
                }

            }
            composable<SearchScreenObj> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SearchScreen(navController,playerViewModel)
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
                        playerViewModel = playerViewModel
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
                    context = LocalContext.current,
                    favViewModel = favViewModel,
                    downloaderViewModel = downloaderViewModel,
                    imageColorViewModel =  imageColorGradient,
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
                    localSongsViewModel = localSongsViewModel,
                    playerViewModel = playerViewModel,
                    favViewModel = favViewModel
                )
            }
            composable<SearchMyMusicObj> {
                SearchMyMusic(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    localSongsViewModel = localSongsViewModel
//                        favViewModel = favViewModel
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





@Composable
fun BottomNavigationBar(navController: NavController, modifier: Modifier = Modifier) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
//        BottomNavItem.MusicRecognizer,
        BottomNavItem.Library,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    BottomNavigation(
        backgroundColor = Color(0xE4000000),
        modifier =  modifier
    ) {
        items.forEach { screen ->
            val isSelected = currentDestination?.route.toString() == screen.obj.toString().substringBefore("@")
            BottomNavigationItem(
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = screen.label,
                        tint = if (isSelected) Color.White else Color.Gray,
                        modifier = Modifier.padding(top = 10.dp)
                    ) },


                label = {
                    Text(
                        screen.label,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 10.sp,
                    ) },


                onClick = {
                    navController.navigate(screen.obj) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                selected = currentDestination?.route.toString() == screen.obj.toString().substringBefore("@")
            )
        }
    }

}



sealed class BottomNavItem<T>(val obj: T, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem<HomeScreenObj>(HomeScreenObj, Icons.Default.Home, "Home")
    object Search : BottomNavItem<SearchScreenObj>(SearchScreenObj, Icons.Default.Search, "Search")
//    object MusicRecognizer : BottomNavItem<MusicRecognizerObj>(MusicRecognizerObj, Icons.Default.Mic, "Recognizer")
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

@Preview
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(navController = NavController(LocalContext.current))
}