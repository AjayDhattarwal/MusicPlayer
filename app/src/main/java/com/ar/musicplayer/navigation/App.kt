package com.ar.musicplayer.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationDefaults
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

import com.ar.musicplayer.models.HomeListItem
import com.ar.musicplayer.screens.HomeScreen
import kotlinx.serialization.json.Json

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.screens.InfoScreen
import com.ar.musicplayer.screens.PlayerScreen
import com.ar.musicplayer.ui.theme.MusicPlayerTheme
import com.ar.musicplayer.viewmodel.HomeViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App(navController: NavHostController, homeViewModel: HomeViewModel) {
    val blackToGrayGradient =
        Brush.verticalGradient(
            colors = listOf(Color(0xFF000000),Color(0xFF161616)),
            startY = Float.POSITIVE_INFINITY,
            endY = 0f
        )

    MusicPlayerTheme() {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            },
        ){
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
                        Text(text = "search Screen")
                    }
                }
                composable<LibraryScreenObj> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Music Library Screen")
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
                    val deSerialized = Json.decodeFromString(HomeListItem.serializer(), args.serialized)

                    InfoScreen(navController = navController, homeListItem = deSerialized)

                }
                composable<PlayerScreenObj> {
                    val args = it.toRoute<PlayerScreenObj>()
                    val deSerialized = Json.decodeFromString(SongResponse.serializer(), args.songResponse)

                    PlayerScreen(navController = navController, songResponse = deSerialized )

                }
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
        backgroundColor = Color(0xC8000000),
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
                    Text(screen.label,
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