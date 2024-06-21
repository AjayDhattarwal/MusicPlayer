package com.ar.musicplayer.navigation

import MusicPlayer
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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.ar.musicplayer.models.HomeListItem
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.screens.HomeScreen
import com.ar.musicplayer.screens.InfoScreen
import com.ar.musicplayer.screens.LibraryScreen
import com.ar.musicplayer.screens.PlayerContentUi
import com.ar.musicplayer.screens.PlayerScreen
import com.ar.musicplayer.screens.SearchScreen
import com.ar.musicplayer.ui.theme.MusicPlayerTheme
import com.ar.musicplayer.utils.MusicPlayerSingleton
import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.viewmodel.ImageColorGradient
import com.ar.musicplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App(navController: NavHostController, homeViewModel: HomeViewModel,  playerViewModel: PlayerViewModel,modifier: Modifier = Modifier ) {
    val context  = LocalContext.current
    val player = ExoPlayer.Builder(context).build()
    MusicPlayerSingleton.initialize(context, playerViewModel, player)
    val musicPlayer = MusicPlayerSingleton.getInstance()

    val blackToGrayGradient =
        Brush.verticalGradient(
            colors = listOf(Color(0xFF000000),Color(0xFF161616)),
            startY = Float.POSITIVE_INFINITY,
            endY = 0f
        )
    var isBottomSheetExpanded by remember { mutableStateOf(false) }
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
        )
        {
            PlayerScreenWithBottomNav(navController,homeViewModel, playerViewModel,blackToGrayGradient,musicPlayer)


        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlayerScreenWithBottomNav(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    blackToGrayGradient: Brush,
    musicPlayer: MusicPlayer
) {
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
    )
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
            PlayerContentUi(
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
                musicPlayer = musicPlayer
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
                    HomeScreen(navController, homeViewModel)
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
            composable<ProfileScreenObj> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Profile Screen")
                }
            }

            composable<InfoScreenObj> {
                val args = it.toRoute<InfoScreenObj>()
                val deSerialized =
                    Json.decodeFromString(HomeListItem.serializer(), args.serialized)
                val colorViewModel = viewModel<ImageColorGradient>()

                InfoScreen(
                    navController = navController,
                    homeListItem = deSerialized,
                    playerViewModel = playerViewModel,
                    colorViewModel = colorViewModel,
                    context = LocalContext.current,
                    musicPlayer = musicPlayer
                )

            }
            composable<PlayerScreenObj> {
                val args = it.toRoute<PlayerScreenObj>()
                val deSerialized =
                    Json.decodeFromString(SongResponse.serializer(), args.songResponse)

                PlayerScreen(navController = navController, songResponse = deSerialized)

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
        BottomNavItem.Profile
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
    object Profile : BottomNavItem<ProfileScreenObj>(ProfileScreenObj, Icons.Default.Person, "Profile")
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(navController = NavController(LocalContext.current))
}