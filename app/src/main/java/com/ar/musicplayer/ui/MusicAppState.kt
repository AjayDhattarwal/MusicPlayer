package com.ar.musicplayer.ui

import android.content.Context
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetValue.Collapsed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController


@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController(),
) = remember(navController) {
    MusicAppState(navController)
}

class MusicAppState(
    val navController: NavHostController,
) {


    fun navigate(any: Any, from: NavBackStackEntry) {
        if (from.lifecycleIsResumed()) {
            navController.navigate(any){
            }
        }
    }
    fun navigate(any: Any){
        navController.navigate(any)
    }

    fun navigateToBottomBarRoute(route: Any) {
        if (route::class.qualifiedName != navController.currentDestination?.route) {
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
            }
        }
    }

    fun navigateBack() {
        navController.navigateUp()
    }

}

private fun NavBackStackEntry.lifecycleIsResumed() =
    this.lifecycle.currentState == Lifecycle.State.RESUMED



