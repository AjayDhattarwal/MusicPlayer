package com.ar.musicplayer.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.screens.library.viewmodel.LocalSongsViewModel
import com.ar.musicplayer.screens.player.MusicPlayerScreen
import com.ar.musicplayer.ui.MusicAppState
import com.ar.musicplayer.ui.WindowInfoVM
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.utils.helper.PaletteExtractor
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.viewmodel.MoreInfoViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun AppMainScreen(
    showPlayerSheet: Boolean,
    appState: MusicAppState,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    downloaderViewModel: DownloaderViewModel,
    bottomSheetState: BottomSheetScaffoldState,
    localSongsViewModel: LocalSongsViewModel = hiltViewModel(),
    windowInfoVm: WindowInfoVM,
    favoriteViewModel: FavoriteViewModel
) {
    if(showPlayerSheet && appState.navController.isMaxPlayer()) {
        appState.navigateBack()
    }

    val themeViewModel = hiltViewModel<ThemeViewModel>()
    val moreInfoViewModel = hiltViewModel<MoreInfoViewModel>()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val preferencesManager = remember {  PreferencesManager(context) }


    val showPlayer by playerViewModel.showBottomSheet.collectAsState()


    val paletteExtractor = remember { PaletteExtractor() }


    Timber.tag("recompose").d(" recompose called for PlayerScreenWithBottomNav Function")

    if(showPlayerSheet){
        BottomSheetScaffold(
            scaffoldState = bottomSheetState,
            sheetPeekHeight = if(showPlayer) 125.dp else 0.dp,
            sheetContent = {
                key(showPlayer) {
                    MusicPlayerScreen(
                        playerViewModel = playerViewModel,
                        bottomSheetState = bottomSheetState,
                        onExpand = remember{
                            {
                                if (bottomSheetState.bottomSheetState.isCollapsed) {
                                    coroutineScope.launch { bottomSheetState.bottomSheetState.expand() }
                                }
                            }
                        },
                        onCollapse = remember{ { coroutineScope.launch { bottomSheetState.bottomSheetState.collapse() } } },
                        paletteExtractor = paletteExtractor,
                        downloaderViewModel = downloaderViewModel,
                        favoriteViewModel = favoriteViewModel,
                        preferencesManager = preferencesManager
                    )
                }
            },
            sheetBackgroundColor = Color.Transparent,
            sheetContentColor = Color.Transparent,
            backgroundColor = Color.Transparent,
            modifier = Modifier.navigationBarsPadding()
        ) {
            MainScreenContent(
                appState = appState,
//                listState = listState,
                windowInfoVm = windowInfoVm,
                homeViewModel = homeViewModel,
                playerViewModel = playerViewModel,
                moreInfoViewModel = moreInfoViewModel,
                preferencesManager = preferencesManager,
                localSongsViewModel = localSongsViewModel,
                themeViewModel = themeViewModel,
                downloaderViewModel = downloaderViewModel,
                favViewModel = favoriteViewModel,
            )
        }

    } else{
        MainScreenContent(
            appState = appState,
//            listState = listState,
            windowInfoVm = windowInfoVm,
            homeViewModel = homeViewModel,
            playerViewModel = playerViewModel,
            moreInfoViewModel = moreInfoViewModel,
            preferencesManager = preferencesManager,
            localSongsViewModel = localSongsViewModel,
            themeViewModel = themeViewModel,
            downloaderViewModel = downloaderViewModel,
            favViewModel = favoriteViewModel,
        )
    }
}