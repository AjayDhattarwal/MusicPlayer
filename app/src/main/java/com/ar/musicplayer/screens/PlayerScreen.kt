package com.ar.musicplayer.screens

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.viewmodel.DecoderViewModel
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlayerScreen(navController: NavHostController, songResponse: SongResponse) {
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
    )


    var viewModel: DecoderViewModel = viewModel()

    LaunchedEffect(Unit){
        songResponse.moreInfo?.encryptedMediaUrl?.let { viewModel.decode(it) }
    }

    
}


