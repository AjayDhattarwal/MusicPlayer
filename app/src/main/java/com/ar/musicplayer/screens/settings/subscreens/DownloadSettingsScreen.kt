package com.ar.musicplayer.screens.settings.subscreens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.ar.musicplayer.utils.PreferencesManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadSettingsScreen(preferencesManager: PreferencesManager, onBackClick: () -> Boolean){

    val context = LocalContext.current
    var downloadQuality by remember { mutableStateOf(preferencesManager.getDownloadQuality()) }
    var downloadQualitySelector by remember { mutableStateOf(false) }

    var musicDirectory by remember { mutableStateOf<String>(preferencesManager.getDownloadLocation()) }

    val defaultMusicDirectory by remember {
        mutableStateOf( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC))
    }
    Log.d("uri","default uri is  ${defaultMusicDirectory}")

    val musicDirectoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            musicDirectory = getPerfectPath(it)
            preferencesManager.setDownloadLocation(getPerfectPath(it))
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
    }


    var effortlesslyOrganize by remember { mutableStateOf(preferencesManager.isEffortlesslyOrganize()) }
    var downloadLyrics by remember { mutableStateOf(preferencesManager.isDownloadLyrics()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Download",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onBackClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
        ){
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Download Quality",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Better Quality, More Storage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clickable { downloadQualitySelector = true },
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = "${downloadQuality} kbps",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "download Quality Selector",
                            tint = Color.White,

                            )
                        DropdownMenu(
                            expanded = downloadQualitySelector,
                            onDismissRequest = {downloadQualitySelector = false},
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    downloadQuality = "96"
                                    preferencesManager.setDownloadQuality("96")
                                },
                                text = { Text("96 kbps") },
                                trailingIcon = {},
                                leadingIcon = {},
                                contentPadding = PaddingValues(0.dp)
                            )
                            DropdownMenuItem(
                                onClick = {
                                    downloadQuality = "160"
                                    preferencesManager.setDownloadQuality("160")
                                },
                                text = { Text("160 kbps") },
                                trailingIcon = {},
                                leadingIcon = {},
                                contentPadding = PaddingValues(0.dp)
                            )
                            DropdownMenuItem(
                                onClick = {
                                    downloadQuality = "320"
                                    preferencesManager.setDownloadQuality("320")
                                },
                                text = { Text("320 kbps") },
                                trailingIcon = {},
                                leadingIcon = {},
                                contentPadding = PaddingValues(0.dp)
                            )
                        }
                    }


                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).clickable { musicDirectoryPicker.launch(null) }) {
                        Text(
                            "Download Location",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = musicDirectory.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            musicDirectory = defaultMusicDirectory.toString()
                            preferencesManager.setDownloadLocation(defaultMusicDirectory.toString())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    ){
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }


                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Effortlessly Organize",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Create Folders for Album & Playlist Downloads",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    Switch(
                        checked = effortlesslyOrganize,
                        onCheckedChange = {
                            effortlesslyOrganize = it
                            preferencesManager.setEffortlesslyOrganize(it)
                        },
                        modifier = Modifier.padding(end = 10.dp),
                        colors = SwitchDefaults.colors(
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray,
                            checkedThumbColor = Color.LightGray,
                            checkedBorderColor = Color(preferencesManager.getAccentColor()),
                            checkedTrackColor = Color(preferencesManager.getAccentColor())
                        )
                    )


                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Seamless Lyrics Download",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Get Lyrics Along with Your Songs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    Switch(
                        checked = downloadLyrics,
                        onCheckedChange = {
                            downloadLyrics = it
                            preferencesManager.setDownloadLyrics(it)
                        },
                        modifier = Modifier.padding(end = 10.dp),
                        colors = SwitchDefaults.colors(
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray,
                            checkedThumbColor = Color.LightGray,
                            checkedBorderColor = Color(preferencesManager.getAccentColor()),
                            checkedTrackColor = Color(preferencesManager.getAccentColor())
                        )
                    )


                }

            }
        }

    }
}


fun getPerfectPath(uri: Uri): String {
    val defaultMusicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
    if(uri == defaultMusicDirectory.toUri()){
        return defaultMusicDirectory.toString()
    }

    val downloadLocation = uri.toString()
    Log.d("uri", "URI: $downloadLocation")

    val treeDocumentId = DocumentsContract.getTreeDocumentId(uri)
    val displayPath = treeDocumentId.split(":").lastOrNull()
    Log.d("uri", "Path: $displayPath")

    val perfectDisplayPath = "/storage/emulated/0/"+displayPath
    return perfectDisplayPath
}