package com.ar.musicplayer.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

import com.ar.musicplayer.models.HomeListItem
import com.ar.musicplayer.screens.HomeScreen
import kotlinx.serialization.json.Json

import androidx.navigation.compose.NavHost
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.screens.InfoScreen
import com.ar.musicplayer.screens.PlayerScreen
import com.ar.musicplayer.viewmodel.HomeViewModel

@Composable
fun App(navController: NavHostController, homeViewModel: HomeViewModel,) {
    NavHost(
        navController = navController,
        startDestination = HomeScreenObj
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
