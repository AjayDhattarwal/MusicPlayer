package com.ar.musicplayer.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ar.musicplayer.data.models.Artist
import com.ar.musicplayer.data.models.InfoScreenModel
import com.ar.musicplayer.data.models.PlaylistResponse
import com.ar.musicplayer.screens.home.HomeScreen
import com.ar.musicplayer.screens.info.ArtistInfoScreen
import com.ar.musicplayer.screens.info.InfoScreen
import com.ar.musicplayer.screens.library.LibraryScreen
import com.ar.musicplayer.screens.library.favorite.FavoriteScreen
import com.ar.musicplayer.screens.library.history.ListeningHistoryScreen
import com.ar.musicplayer.screens.library.mymusic.MyMusicScreen
import com.ar.musicplayer.screens.library.mymusic.SearchMyMusic
import com.ar.musicplayer.screens.library.playlist.LocalPlaylistInfoScreen
import com.ar.musicplayer.screens.library.playlist.PlaylistManagerScreen
import com.ar.musicplayer.screens.library.viewmodel.LocalSongsViewModel
import com.ar.musicplayer.screens.search.SearchScreen
import com.ar.musicplayer.screens.settings.SettingsScreen
import com.ar.musicplayer.screens.settings.subscreens.DownloadSettingsScreen
import com.ar.musicplayer.screens.settings.subscreens.LanguageSettingsScreen
import com.ar.musicplayer.screens.settings.subscreens.PlaybackSettingsScreen
import com.ar.musicplayer.screens.settings.subscreens.StorageSettingScreen
import com.ar.musicplayer.screens.settings.subscreens.ThemeSettingsScreen
import com.ar.musicplayer.ui.MusicAppState
import com.ar.musicplayer.ui.WindowInfoVM
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.viewmodel.ImportViewModel
import com.ar.musicplayer.viewmodel.MoreInfoViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@UnstableApi
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun NavigationGraph(
    appState: MusicAppState,
    windowInfoVm: WindowInfoVM,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    moreInfoViewModel: MoreInfoViewModel,
    preferencesManager: PreferencesManager,
    localSongsViewModel: LocalSongsViewModel,
    favViewModel: FavoriteViewModel,
    downloaderViewModel: DownloaderViewModel,
    themeViewModel: ThemeViewModel,
    importViewModel: ImportViewModel,
    bottomSheetState: BottomSheetScaffoldState
) {
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalContext.current as? Activity
    NavHost(
        navController = appState.navController,
        startDestination = HomeScreenObj,
    ) {

        composable<HomeScreenObj> { backStackEntry ->

            HomeScreen(
                windowInfoVm = windowInfoVm,
                homeViewModel =  homeViewModel,
                playerViewModel = playerViewModel,
                moreInfoViewModel = moreInfoViewModel,
                downloaderViewModel = downloaderViewModel,
                favViewModel = favViewModel,
                navigate = remember{
                    { appState.navigate(it, backStackEntry) }
                }
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                }else{
                    moveAppToBackground(activity)
                }
            }
        }

        composable<LargeScreenPlayerObj> {
            Box(modifier = Modifier.fillMaxSize())
        }


        composable<SearchScreenObj> { backStackEntry ->
            SearchScreen(
                playerViewModel = playerViewModel,
                onArtistClick = remember{
                    { artist ->
                        val senderData = Json.encodeToString(Artist.serializer(), artist)
                        appState.navigate(
                            ArtistInfoScreenObj(senderData), backStackEntry
                        )
                    }
                },
                onPlaylistClick = remember {
                    { infoScreenModel ->
                        val senderData = Json.encodeToString(InfoScreenModel.serializer(), infoScreenModel)
                        appState.navigate(InfoScreenObj(senderData), backStackEntry)
                    }
                }
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }

        }


        composable<LibraryScreenObj> { backStackEntry ->
            LibraryScreen(
                onScreenSelect = {
                    appState.navigate(it, backStackEntry)
                }
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }
        }


        composable<SettingsScreenObj> { backStackEntry ->
            SettingsScreen(
                onBackPressed = appState::navigateBack,
                onNavigate = { appState.navigate(it, backStackEntry) }
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }

        }


        composable<InfoScreenObj> { backStackEntry ->
            val args = backStackEntry.toRoute<InfoScreenObj>()
            val data = Json.decodeFromString(InfoScreenModel.serializer(), args.data)
            InfoScreen(
                playerViewModel = playerViewModel,
                favViewModel = favViewModel,
                downloaderViewModel = downloaderViewModel,
                moreInfoViewModel = moreInfoViewModel,
                data = data,
                onBackPressed = appState::navigateBack
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }
        }


        composable<FavoriteScreenObj> { backStackEntry ->
            FavoriteScreen(
                playerViewModel= playerViewModel,
                favViewModel = favViewModel,
                onBackPressed = appState::navigateBack
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }

        }


        composable<ListeningHisScreenObj> { backStackEntry ->
            ListeningHistoryScreen(
                playerViewModel = playerViewModel,
                onBackPressed = appState::navigateBack
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }

        }


        composable<MyMusicScreenObj> { backStackEntry ->
            MyMusicScreen(
                localSongsViewModel = localSongsViewModel,
                onBackPressed = appState::navigateBack,
                onNavigate = { appState.navigate(it, backStackEntry) },
                onSongClick =  remember {
                    {
                        playerViewModel.setNewTrack(it)
                    }
                }
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }

        }


        composable<SearchMyMusicObj> { backStackEntry ->
            SearchMyMusic(
                playerViewModel = playerViewModel,
                localSongsViewModel = localSongsViewModel,
                onBackPressed = appState::navigateBack
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }

        }


        composable<DetailsScreenObj> { backStackEntry ->
            val args = backStackEntry.toRoute<DetailsScreenObj>()
            val playlistResponse =
                Json.decodeFromString(PlaylistResponse.serializer(), args.playlistResponse)
//            DetailsScreen(
//                appState = appState,
//                playlistResponse = playlistResponse,
//                playerViewModel = playerViewModel
//            )

            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }

        }
        composable<ThemeSettingObj> { backStackEntry ->
            ThemeSettingsScreen(
                themeViewModel = themeViewModel,
                onBackClick = appState::navigateBack
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }
        }
        composable<DownloadSettingsScreenObj> { backStackEntry ->
            DownloadSettingsScreen(
                onBackClick = appState::navigateBack
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }

        }
        composable<LanguageSettingsScreenObj> { backStackEntry ->
            LanguageSettingsScreen(
                preferencesManager = preferencesManager,
                onBackClick = { appState.navigateBack() }
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }

        }
        composable<PlaybackSettingsScreenObj> { backStackEntry ->
            PlaybackSettingsScreen(
                preferencesManager = preferencesManager,
                onBackClick = { appState.navigateBack() }
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }

        }
        composable<StorageSettingScreenObj> { backStackEntry ->
            StorageSettingScreen(
                onBackClick = remember{ { appState.navigateBack() } }
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }
        }
        composable<ArtistInfoScreenObj> {
            val args = it.toRoute<ArtistInfoScreenObj>()
            val artistInfo = Json.decodeFromString(Artist.serializer(), args.artistInfo)
            ArtistInfoScreen(
                artistInfo = artistInfo,
                onArtistClick = remember { { _,_ ->

                }},
                onPlaylistClick = remember { { _,_ ->

                } },
                onAlbumClick =  remember { { _,_ ->

                } },
                onSongClick = remember { { song ->
                    playerViewModel.setNewTrack(song)
                } },
                onBackPressed = remember { {
                    appState.navigateBack()
                } }
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }

        }

        composable<PlaylistFetchScreenObj> { backStackEntry ->
            PlaylistManagerScreen(
                importViewModel = importViewModel,
                onBackPress = appState::navigateBack,
                onNavigate = { appState.navigate(it, backStackEntry) }
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }

        }

        composable<LocalPlaylistInfoObj> { backStackEntry ->
            val args = backStackEntry.toRoute<InfoScreenObj>()
            val data = Json.decodeFromString(PlaylistResponse.serializer(), args.data)
            LocalPlaylistInfoScreen(
                playerViewModel = playerViewModel,
                playlistResponse = data,
                favViewModel = favViewModel,
                downloaderViewModel = downloaderViewModel,
                onBackPressed = appState::navigateBack
            )
            BackHandler {
                if(bottomSheetState.bottomSheetState.isExpanded){
                    coroutineScope.launch {
                        bottomSheetState.bottomSheetState.collapse()
                    }
                } else{
                    appState.navigateBack()
                }
            }
        }

    }

}



fun moveAppToBackground(activity: Activity?) {
    activity?.moveTaskToBack(true)  // This moves the task (app) to the background
}