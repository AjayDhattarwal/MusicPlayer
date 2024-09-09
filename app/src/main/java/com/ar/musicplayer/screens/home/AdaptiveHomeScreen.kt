@file:kotlin.OptIn(ExperimentalFoundationApi::class)

package com.ar.musicplayer.screens.home

import android.app.Activity
import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.data.models.HomeListItem
import com.ar.musicplayer.data.models.InfoScreenModel
import com.ar.musicplayer.data.models.toInfoScreenModel
import com.ar.musicplayer.navigation.InfoScreenObj
import com.ar.musicplayer.navigation.SettingsScreenObj
import com.ar.musicplayer.screens.info.InfoScreen
import com.ar.musicplayer.ui.theme.WindowInfoVM
import com.ar.musicplayer.utils.events.RadioStationEvent
import com.ar.musicplayer.viewmodel.HomeViewModel
import com.ar.musicplayer.viewmodel.MoreInfoViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.adaptive.TwoPaneStrategy
import com.google.accompanist.adaptive.VerticalTwoPaneStrategy
import com.google.accompanist.adaptive.calculateDisplayFeatures
import kotlinx.serialization.json.Json

@OptIn(UnstableApi::class, )
@Composable
fun AdaptiveHomeScreen(
    windowInfoVM: WindowInfoVM,
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    moreInfoViewModel: MoreInfoViewModel,
    listState: LazyListState,
    navigateSetting: () -> Unit,
    onItemClick: (Boolean, HomeListItem) -> Unit
) {
    val showPreviewScreen by windowInfoVM.showPreviewScreen.collectAsState()
    val selectedItem by windowInfoVM.selectedItem.collectAsState()
    val isPreviewVisible by windowInfoVM.isPreviewVisible.collectAsState()
    val context = LocalContext.current

    TwoPane(
        displayFeatures = calculateDisplayFeatures(context as Activity),
        first = {
            HomeScreen(
                windowInfoVM = windowInfoVM,
                homeViewModel = homeViewModel,
                playerViewModel = playerViewModel,
                listState = listState,
                navigateSetting =navigateSetting,
                onItemClick = { isRadio, data ->
                    windowInfoVM.onItemSelected(data.toInfoScreenModel())
                    onItemClick(isRadio, data)
                }
            )
        },
        second = {
            if (isPreviewVisible && selectedItem != null && showPreviewScreen) {
                InfoScreen(
                    moreInfoViewModel = moreInfoViewModel,
                    data = selectedItem!!,
                    onBackPressed = remember{
                        { windowInfoVM.closePreview() }
                    }
                )
            }
        },
        strategy = HorizontalTwoPaneStrategy(if(isPreviewVisible && showPreviewScreen) 0.6f else 1f),
    )
}