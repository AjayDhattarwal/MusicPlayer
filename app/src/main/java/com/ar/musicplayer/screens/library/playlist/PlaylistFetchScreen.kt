package com.ar.musicplayer.screens.library.playlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ar.musicplayer.R
import com.ar.musicplayer.components.library.PlaylistDialog
import com.ar.musicplayer.navigation.FavoriteScreenObj
import com.ar.musicplayer.navigation.LanguageSettingsScreenObj
import com.ar.musicplayer.navigation.PlaybackSettingsScreenObj
import com.ar.musicplayer.navigation.StorageSettingScreenObj

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistFetchScreen(
    navController: NavHostController,
    background: Brush
){
    var showDialog by remember {
        mutableStateOf(false)
    }
    var title by remember {
        mutableStateOf("")
    }
    var returnedText by rememberSaveable {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Playlists",
                        color = Color.White,
                        modifier = Modifier,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {navController.navigateUp()}) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
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
        containerColor = Color.Transparent,
        modifier = Modifier.background(brush = background)
    ) { innerPadding ->

        PlaylistDialog(
            title = title,
            onDismissRequest = {
                showDialog = false
                title = ""
            },
            showDialog = showDialog
        ) { string ->
            returnedText = string
            showDialog = false
        }

        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()){
            LazyColumn(
                contentPadding = PaddingValues(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clickable {
                                title = "Create Playlist"
                                showDialog = true
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Create Playlist",
                            tint = Color.White,
                            modifier = Modifier.padding(10.dp)
                        )
                        Text(
                            text = "Create Playlist",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier
                                .padding(start = 20.dp)
                                .weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clickable {
//                                navController.navigate(PlaybackSettingsScreenObj)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Input,
                            contentDescription = "Import Playlist",
                            tint = Color.White,
                            modifier = Modifier.padding(10.dp)
                        )
                        Text(
                            text = "Import Playlist",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier
                                .padding(start = 20.dp)
                                .weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clickable {
//                                navController.navigate(StorageSettingScreenObj)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MergeType,
                            contentDescription = "Merge Playlist",
                            tint = Color.White,
                            modifier = Modifier.padding(10.dp)
                        )
                        Text(
                            text = "Merge Playlist",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier
                                .padding(start = 20.dp)
                                .weight(1f)
                        )
                    }
                }

                item{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clickable {

                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clickable {
                                navController.navigate(FavoriteScreenObj)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier.size(50.dp)
                                .clip(RoundedCornerShape(5))
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ){
                            Image(
                                painter = painterResource(R.drawable.ic_music_note_24),
                                contentDescription = "image",
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center,
                                colorFilter = ColorFilter.tint(Color.LightGray)
                            )
                        }

                        Text(
                            text = "Favorite Songs",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.padding(start = 15.dp).weight(1f),
                            maxLines = 1,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis
                        )


                        IconButton(onClick = { /* Handle menu button click */ }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

    }

}

@Preview
@Composable
fun PlaylistFetchScreenPreview(){
    PlaylistFetchScreen(
        rememberNavController(),
        Brush.verticalGradient(
            listOf(
                Color(0xFF000000),
                Color(0xFF000000)
            )
        )
    )
}