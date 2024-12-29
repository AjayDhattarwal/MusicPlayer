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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.ar.musicplayer.R
import com.ar.musicplayer.components.PlaylistDropdownMenu
import com.ar.musicplayer.components.library.CountdownSnackbar
import com.ar.musicplayer.components.library.PLAYLIST_ACTIONS
import com.ar.musicplayer.components.library.PlaylistDialog
import com.ar.musicplayer.components.library.UndoSnackbarWithTimer
import com.ar.musicplayer.data.models.PlaylistResponse
import com.ar.musicplayer.navigation.FavoriteScreenObj
import com.ar.musicplayer.navigation.LocalPlaylistInfoObj
import com.ar.musicplayer.screens.library.mymusic.toPx
import com.ar.musicplayer.viewmodel.ImportViewModel
import kotlinx.serialization.json.Json


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistManagerScreen(
    importViewModel: ImportViewModel,
    onBackPress : () -> Unit,
    onNavigate: (Any) -> Unit,
){

    var showDialog by rememberSaveable  {
        mutableStateOf(false)
    }
    var title by rememberSaveable {
        mutableStateOf("")
    }

    var playlistActions by rememberSaveable {
        mutableStateOf(PLAYLIST_ACTIONS.CREATE)
    }

    var placeholder by rememberSaveable {
        mutableStateOf("Enter Playlist Name")
    }

    val importState by importViewModel.playlists.collectAsStateWithLifecycle()
    val currentImporting by importViewModel.currentImportingPlaylist.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }
    var showSnackBar by remember { mutableStateOf(false) }


    Scaffold (
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
                    IconButton(onClick = onBackPress ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState,
                modifier = Modifier.padding(bottom = 110.dp)
            ){ data ->
                CountdownSnackbar(data)
            }
        },
        containerColor = Color.Transparent,
    ) { innerPadding ->

        PlaylistDialog(
            title = title,
            placeholder = placeholder,
            action = playlistActions,
            onDismissRequest = {
                showDialog = false
                title = ""
            },
            showDialog = showDialog,
            returnedText = { string , action  ->
                if(action == PLAYLIST_ACTIONS.IMPORT && string.isNotEmpty()){
                    importViewModel.importPlaylist(string)
                } else if(action == PLAYLIST_ACTIONS.CREATE && string.isNotEmpty()){
                    importViewModel.createPlaylist(title = string, description = null)
                }
            },
        )

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
                                placeholder = "Enter Playlist Name"
                                playlistActions = PLAYLIST_ACTIONS.CREATE
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
                                title = "Import Playlist"
                                placeholder = "Enter Playlist Url"
                                playlistActions = PLAYLIST_ACTIONS.IMPORT
                                showDialog = true
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_input),
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
                item{
                    PlaylistItem(
                        image = R.drawable.ic_favorite,
                        title = "Favourite Songs",
                        onNavigate = { onNavigate(FavoriteScreenObj) },
                        onEdit = {},
                        onDelete = {}
                    )
                }

                items(importState){ item ->
                    item.let{
                        PlaylistItem(
                            image = it.image ?: "",
                            title = it.title ?: "",
                            onNavigate = {
                                val data = Json.encodeToString(PlaylistResponse.serializer(), item)
                                onNavigate(LocalPlaylistInfoObj(data))
                            },
                            onDelete = {
                                showSnackBar = true
                                importViewModel.deletePlaylist(it.id)
                            },
                            onEdit = {}
                        )
                    }
                }
                item{
                    currentImporting?.let{
                        PlaylistItem(
                            title = it.name,
                            onNavigate = {},
                            image = it.image,
                            isLoading = true,
                            onDelete = {},
                            onEdit = {}
                        )
                    }
                }
            }
        }

        if(showSnackBar){
            UndoSnackbarWithTimer(
                snackBarHostState = snackBarHostState,
                message = "Playlist Deleted",
                actionLabel = "Undo",
                onUndo = {
                    importViewModel.undoDeleteTask()
                    showSnackBar = false
                },
                onTimerFinished = {
                    showSnackBar = false
                }
            )
        }

    }

}



@Composable
fun PlaylistItem(
    image: Any,
    title: String,
    onNavigate: () -> Unit,
    isLoading: Boolean = false,
    onDelete: () -> Unit ,
    onEdit: () -> Unit
){
    val context = LocalContext.current
    val colorFilter = ColorFilter.tint(Color.LightGray)
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(image)
            .size(30.dp.toPx().toInt(), 30.dp.toPx().toInt())
            .build(),
    )
    var isMoreInfo by remember{ mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clickable {
                onNavigate()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(5))
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painter,
                contentDescription = "Spotify Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            if(isLoading) {
                CircularProgressIndicator()
            }
        }


        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier
                .padding(start = 15.dp)
                .weight(1f),
            maxLines = 1,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )


        IconButton(onClick = {isMoreInfo = !isMoreInfo}) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint = Color.White
            )
            PlaylistDropdownMenu(
                expended = isMoreInfo,
                onDismissRequest = { isMoreInfo = false },
                onEdit = {
                    isMoreInfo = false
                    onEdit()
                },
                onDelete = {
                    isMoreInfo = false
                    onDelete()
                }
            )
        }
    }
}
