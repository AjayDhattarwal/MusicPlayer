package com.ar.musicplayer.screens.settings.subscreens

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ar.musicplayer.components.settings.StorageSegment
import com.ar.musicplayer.components.settings.StorageUsageSlider
import com.ar.musicplayer.components.settings.getCacheSize
import com.ar.musicplayer.components.settings.getFreeSpace
import com.ar.musicplayer.components.settings.getMusicFolderSize
import com.ar.musicplayer.components.settings.getTotalStorage
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageSettingScreen(onBackClick: () -> Unit){
    val context = LocalContext.current

    val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)


    val totalStorage = getTotalStorage()
    val freeSpace = getFreeSpace()
    var cacheSize by remember { mutableStateOf(getCacheSize(context)) }
    val musicFolderSize = getMusicFolderSize(musicDir)
    val otherAppSize = totalStorage - freeSpace - cacheSize - musicFolderSize


    val segments = listOf(
        StorageSegment("Other Apps", otherAppSize, Color.Blue.copy(0.5f)),
        StorageSegment("Cache", cacheSize, Color.Red.copy(0.5f)),
        StorageSegment("Music Folder", musicFolderSize, Color.Green.copy(0.4f)),
        StorageSegment("Free Space", freeSpace, Color.Gray)
    )
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Music Playback",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(Color.Black)
    ) { innerPadding ->

        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                StorageUsageSlider(segments = segments, totalStorage = getTotalStorage())

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {

                        Text(
                            text = "You can free up storage by clearing your cache. Your downloads won't be deleted",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    Button(
                        onClick = {
                            clearAppCache(context)
                            cacheSize = 0L
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.onPrimary)
                    ){
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }


                }

            }

        }
    }
}


fun clearAppCache(context: Context) {
    val cacheDir = context.cacheDir
    deleteDir(cacheDir)
}

fun deleteDir(dir: File?): Boolean {
    if (dir != null && dir.isDirectory) {
        val children = dir.list()
        if (children != null) {
            for (child in children) {
                val success = deleteDir(File(dir, child))
                if (!success) {
                    return false
                }
            }
        }
    }
    return dir?.delete() ?: false
}