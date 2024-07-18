package com.ar.musicplayer.screens

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ar.musicplayer.di.permission.hasPermissions
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.utils.AudioRecorder.AndroidAudioRecorder
import com.ar.musicplayer.viewmodel.MusicRecognizerViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun MusicRecognizer(
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    favViewModel: FavoriteViewModel
) {

    val context = LocalContext.current

    val recorder by lazy {
        AndroidAudioRecorder(context.applicationContext)
    }
    var audioFile: File? = null
    val viewModel: MusicRecognizerViewModel = viewModel()
    var recordingState by remember { mutableStateOf(false) }
    var recognitionResult by remember { mutableStateOf("") }
    val audioFilePath = "${context.filesDir.absolutePath}/record.wav"

    LaunchedEffect(Unit) {
        requestPermissions(context as ComponentActivity)
        File(context.cacheDir, "test.mp3").also {
            recorder.start(it)
            audioFile = it
        }
        delay(10000)

        recorder.stop()

        viewModel.recognizeSong(audioFile?.absolutePath.toString()) { result ->
            recognitionResult = result
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
//                if (recordingState) {
//                    recorder.stop()
//                    recordingState = false
//                    viewModel.recognizeSong(audioFile?.absolutePath.toString()) { result ->
//                        recognitionResult = result
//                    }
//                } else {
//                    File(context.cacheDir, "test.mp3").also {
//                        recorder.start(it)
//                        audioFile = it
//                    }
//                    recordingState = true
//                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(if (recordingState) "Stop Recording" else "Start Recording")
        }
        Text(text = recognitionResult)
    }
}




private fun requestPermissions(context: ComponentActivity) {
    val permissions = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    if (!hasPermissions(context, *permissions)) {
        ActivityCompat.requestPermissions(context, permissions, 0)
    }
}

