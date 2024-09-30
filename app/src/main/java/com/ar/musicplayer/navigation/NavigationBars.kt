package com.ar.musicplayer.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ar.musicplayer.ui.MusicAppState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomNavigationBar(
    appState: MusicAppState,
    bottomSheetState: BottomSheetScaffoldState,
    modifier: Modifier = Modifier
) {
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
        val navBackStackEntry = appState.navController.currentBackStackEntryAsState()


        bottomScreens.forEach { screen ->
            val currentDestination = navBackStackEntry.value?.destination
            val isSelected = currentDestination?.hierarchy?.any {
                it.route == screen.obj::class.qualifiedName
            } == true
            BottomNavigationItem(
                selected = isSelected,
                onClick = remember{ { appState.navigateToBottomBarRoute(screen.obj) } },
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
                modifier = Modifier.background(color = Color.Transparent),
                selectedContentColor = Color.White
            )
        }
    }
}

@Composable
fun NavigationRailBar(
    appState: MusicAppState,
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
        val navBackStackEntry by appState.navController.currentBackStackEntryAsState()
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
                            appState.navigateToBottomBarRoute(screen.obj)
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


sealed class BottomNavItem<T>(val obj: T, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem<HomeScreenObj>( HomeScreenObj, Icons.Default.Home, "Home")
    object Search : BottomNavItem<SearchScreenObj>( SearchScreenObj, Icons.Default.Search, "Search")
    object Library: BottomNavItem<LibraryScreenObj>(LibraryScreenObj, Icons.Default.LibraryMusic, "Library")
}




