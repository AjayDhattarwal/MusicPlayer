@file:kotlin.OptIn(ExperimentalFoundationApi::class)

package com.ar.musicplayer.screens.home

import android.app.Activity
import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.window.layout.DisplayFeature
import com.ar.musicplayer.data.models.HomeListItem
import com.ar.musicplayer.data.models.InfoScreenModel
import com.ar.musicplayer.data.models.toInfoScreenModel
import com.ar.musicplayer.data.models.toSongResponse
import com.ar.musicplayer.navigation.InfoScreenObj
import com.ar.musicplayer.navigation.SettingsScreenObj
import com.ar.musicplayer.screens.info.InfoScreen
import com.ar.musicplayer.ui.WindowInfoVM
import com.ar.musicplayer.utils.download.DownloaderViewModel
import com.ar.musicplayer.utils.events.RadioStationEvent
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.viewmodel.ImportViewModel
import com.ar.musicplayer.viewmodel.MoreInfoViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.ar.musicplayer.viewmodel.RadioStationViewModel
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.adaptive.calculateDisplayFeatures
import kotlinx.serialization.json.Json



@Composable
fun HomeScreen(
    windowInfoVm: WindowInfoVM,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    moreInfoViewModel: MoreInfoViewModel,
    importViewModel: ImportViewModel,
    downloaderViewModel: DownloaderViewModel,
    favViewModel: FavoriteViewModel,
    navigate: (Any) -> Unit
) {
    val context = LocalContext.current
    val radioStationViewModel: RadioStationViewModel = hiltViewModel()
    val radioSongResponse by radioStationViewModel.radioStation.collectAsState()
    val showPreviewScreen by windowInfoVm.showPreviewScreen.collectAsStateWithLifecycle()
    val radioStationSelection = remember { mutableStateOf(false) }
    val displayFeatures = calculateDisplayFeatures(context as Activity)

    // Only remember the current state of radioSongResponse to prevent unnecessary recompositions
    val currentRadioSongResponse by rememberUpdatedState(radioSongResponse)

    LaunchedEffect(currentRadioSongResponse) {
        if (currentRadioSongResponse.isNotEmpty() && radioStationSelection.value) {
            playerViewModel.setPlaylist(currentRadioSongResponse, "radio")
            radioStationSelection.value = false
        }
    }

    // Cache the navigation and item click lambdas to avoid recomposing
    val navigateSetting = remember {
        { navigate(SettingsScreenObj) }
    }

    val onItemClick = remember {
        { radio: Boolean, data: HomeListItem ->
            if (radio) {
                val query = data.moreInfoHomeList?.query.takeIf { it?.isNotBlank() == true } ?: data.title
                radioStationViewModel.onEvent(
                    RadioStationEvent.LoadRadioStationData(
                        call = "webradio.getSong",
                        k = "20",
                        next = "1",
                        name = query.toString(),
                        query = query.toString(),
                        radioStationType = data.moreInfoHomeList?.stationType.orEmpty(),
                        language = data.moreInfoHomeList?.language.orEmpty()
                    )
                )
                radioStationSelection.value = true
            } else {
                if(data.type == "Video" || data.type == "song"){
                    windowInfoVm.closePreview()
                    playerViewModel.setNewTrack(data.toSongResponse())

                }else{
                    if (!showPreviewScreen) {
                        val serializedData = Json.encodeToString(
                            InfoScreenModel.serializer(),
                            data.toInfoScreenModel()
                        )
                        navigate(InfoScreenObj(serializedData))
                    }
                }
            }
        }
    }

    AdaptiveHomeScreen(
        windowInfoVM = windowInfoVm,
        homeViewModel = homeViewModel,
        playerViewModel = playerViewModel,
        moreInfoViewModel = moreInfoViewModel,
        downloaderViewModel = downloaderViewModel,
        importViewModel = importViewModel,
        favViewModel = favViewModel,
        navigateSetting = navigateSetting,
        displayFeatures = displayFeatures,
        onItemClick = onItemClick
    )
}



@Composable
fun AdaptiveHomeScreen(
    windowInfoVM: WindowInfoVM,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    favViewModel: FavoriteViewModel,
    downloaderViewModel: DownloaderViewModel,
    importViewModel: ImportViewModel,
    moreInfoViewModel: MoreInfoViewModel,
    displayFeatures: List<DisplayFeature>,
    navigateSetting: () -> Unit,
    onItemClick: (Boolean, HomeListItem) -> Unit
) {
    val showPreviewScreen by windowInfoVM.showPreviewScreen.collectAsState()
    val selectedItem by windowInfoVM.selectedItem.collectAsState()
    val isPreviewVisible by windowInfoVM.isPreviewVisible.collectAsState()
    val context = LocalContext.current


    // Cache onItemClick lambda to avoid unnecessary recompositions
    val rememberedOnItemClick = rememberUpdatedState(onItemClick)

    TwoPane(
        displayFeatures = displayFeatures,
        first = {
            HomeScreen(
                homeViewModel = homeViewModel,
                playerViewModel = playerViewModel,
                navigateSetting = navigateSetting,
                onItemClick = remember {
                    { isRadio, data ->
                        if (!isRadio) {
                            windowInfoVM.onItemSelected(data.toInfoScreenModel())
                        }
                        rememberedOnItemClick.value(isRadio, data)
                    }
                }
            )
        },
        second = {
            if (isPreviewVisible && selectedItem != null && showPreviewScreen) {
                InfoScreen(
                    moreInfoViewModel = moreInfoViewModel,
                    playerViewModel = playerViewModel,
                    favViewModel = favViewModel,
                    downloaderViewModel = downloaderViewModel,
                    importViewModel = importViewModel,
                    data = selectedItem!!,
                    onBackPressed = remember {
                        { windowInfoVM.closePreview() }
                    }
                )
            }
        },
        strategy = remember(isPreviewVisible, showPreviewScreen) {
            HorizontalTwoPaneStrategy(if (isPreviewVisible && showPreviewScreen) 0.6f else 1f)
        }
    )
}