package com.ar.musicplayer.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
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
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.models.HomeListItem
import com.ar.musicplayer.di.roomdatabase.homescreendb.HomeRoomViewModel
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionEvent
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionViewModel
import com.ar.musicplayer.screens.HomeScreen
import com.ar.musicplayer.screens.InfoScreen
import com.ar.musicplayer.screens.LibraryScreen
import com.ar.musicplayer.screens.PlayerScreen
import com.ar.musicplayer.screens.SettingsScreen
import com.ar.musicplayer.screens.SearchScreen
import com.ar.musicplayer.screens.libraryScreens.FavoriteScreen
import com.ar.musicplayer.screens.libraryScreens.ListeningHistoryScreen
import com.ar.musicplayer.ui.theme.MusicPlayerTheme
import com.ar.musicplayer.utils.MusicPlayer
import com.ar.musicplayer.utils.playerHelper.DownloaderViewModel
import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.viewmodel.ImageColorGradient
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.viewmodel.RadioStationViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


@UnstableApi
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

    MusicPlayerTheme() {
        Scaffold(
            bottomBar = {
                    AnimatedVisibility(
                        visible =  isBottomNavVisible ?: true,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        BottomNavigationBar(navController = navController)
                    }
            },
            modifier = modifier
        ) {
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
            )
        }
    }
}


@UnstableApi
@OptIn(ExperimentalMaterialApi::class)
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
) {

    val imageColorGradient = viewModel<ImageColorGradient>()

    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
    )
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
        sheetBackgroundColor = Color.Transparent
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
                .fillMaxSize()
        ) {

            composable<HomeScreenObj> {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HomeScreen(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        homeRoomViewModel = homeRoomViewModel,
                        radioStationViewModel =  radioStationViewModel,
                        playerViewModel = playerViewModel,
                        lastSessionViewModel = lastSessionViewModel,
                        imageColorViewModel = imageColorGradient
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


                InfoScreen(
                    navController = navController,
                    homeListItem = deSerialized,
                    playerViewModel = playerViewModel,
                    colorViewModel = imageColorGradient,
                    context = LocalContext.current,
                    favViewModel = favViewModel,
                    downloaderViewModel = downloaderViewModel
                )

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
                    playerViewModel = playerViewModel,
                    favViewModel = favViewModel,
                    lastSessionViewModel = lastSessionViewModel
                )
            }
        }
    }
}





@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Library,
//        BottomNavItem.Profile
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    BottomNavigation(
        backgroundColor = Color(0xE4000000),
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
    object Library: BottomNavItem<LibraryScreenObj>(LibraryScreenObj,Icons.Default.LibraryMusic, "Library")
//    object Profile : BottomNavItem<ProfileScreenObj>(ProfileScreenObj, Icons.Default.Person, "Profile")
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(navController = NavController(LocalContext.current))
}