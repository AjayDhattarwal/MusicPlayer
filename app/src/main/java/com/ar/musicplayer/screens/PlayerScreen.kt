package com.ar.musicplayer.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.viewmodel.DecoderViewModel

@Composable
fun PlayerScreen(navController: NavHostController, songResponse: SongResponse) {


    var viewModel: DecoderViewModel = viewModel()

    songResponse.moreInfo?.encryptedMediaUrl?.let { viewModel.decode(it) }

    val url by  viewModel.decodedOutput.collectAsState()

    Column{
        url.let {
            Text(text = it, fontSize = 16.sp, modifier = Modifier.padding(30.dp))
        }
        songResponse.moreInfo?.encryptedMediaUrl?.let { Text(text = it, fontSize = 16.sp) }
    }

}


@Preview
@Composable
fun PreviewPlayerScreen() {
//    PlayerScreen()
}