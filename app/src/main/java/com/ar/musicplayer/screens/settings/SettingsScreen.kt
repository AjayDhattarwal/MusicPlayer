package com.ar.musicplayer.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(){

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        color = Color.White,
                        modifier = Modifier,
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                },
                colors = TopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Transparent,
                    navigationIconContentColor = Color.Transparent,
                    actionIconContentColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
            )
        },
        containerColor = Color.Black,
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()){
            Column(
                modifier = Modifier.padding(10.dp).fillMaxSize(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clickable {

                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Spellcheck,
                        contentDescription = "Languages",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = "Languages",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 20.dp).weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Equalizer,
                        contentDescription = "Audio Quality",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = "Audio Quality ",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 20.dp).weight(1f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clickable {

                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Music Playback ",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = "Music Playback",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 20.dp).weight(1f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download & Storage",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = "Download & Storage",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 20.dp).weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DownloadDone,
                        contentDescription = "Downloads",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = "Downloads",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 20.dp).weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Help",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = "Help",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 20.dp).weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "About",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = "About",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 20.dp).weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share App",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = "Share App",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 20.dp).weight(1f)
                    )
                }



            }
        }

    }

}

@Preview
@Composable
fun PreviewSettingScreen(){
    SettingsScreen()
}