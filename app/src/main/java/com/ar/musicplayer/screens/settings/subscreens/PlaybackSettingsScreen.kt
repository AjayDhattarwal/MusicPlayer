package com.ar.musicplayer.screens.settings.subscreens

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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.unit.dp
import com.ar.musicplayer.utils.PreferencesManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSettingsScreen(
    onBackClick: () -> Unit,
    preferencesManager: PreferencesManager
) {
    var selectedQuality by remember { mutableStateOf(preferencesManager.getStreamQuality()) }
    var qualityExpanded by remember { mutableStateOf(false) }

    var bassLevel by remember { mutableStateOf(0f) }
    var trebleLevel by remember { mutableStateOf(0f) }
    var playbackSpeed by remember { mutableStateOf(1f) }
    var isGaplessPlaybackEnabled by remember { mutableStateOf(preferencesManager.isGaplessPlaybackEnabled()) }
    var crossfadeDuration by remember { mutableStateOf(preferencesManager.getCrossfadeDuration()) }
    var isGesturesEnabled by remember { mutableStateOf(preferencesManager.isGesturesEnabled()) }
    var isVolumeLevelingEnabled by remember { mutableStateOf(false) }
    var isAutoPlay by remember { mutableStateOf(preferencesManager.isAutoPlay()) }

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
        ) {
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
                            "Streaming Quality",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Better Quality, More Data Usage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clickable {  qualityExpanded = true },
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = selectedQuality.plus(" kbps"),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "download Quality Selector",
                            tint = Color.White,

                            )
                        DropdownMenu(
                            expanded = qualityExpanded,
                            onDismissRequest = {qualityExpanded = false},
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    preferencesManager.setStreamQuality("96")
                                    selectedQuality = "96"

                                },
                                text = { Text("96 kbps") },
                                trailingIcon = {},
                                leadingIcon = {},
                                contentPadding = PaddingValues(0.dp)
                            )
                            DropdownMenuItem(
                                onClick = {
                                    preferencesManager.setStreamQuality("160")
                                    selectedQuality = "160"
                                },
                                text = { Text("160 kbps") },
                                trailingIcon = {},
                                leadingIcon = {},
                                contentPadding = PaddingValues(0.dp)
                            )
                            DropdownMenuItem(
                                onClick = {
                                    preferencesManager.setStreamQuality("320")
                                    selectedQuality = "320"
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Equalizer",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fine-Tune Your Sound Experience",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowRight,
                        contentDescription = "download Quality Selector",
                        tint = Color.White
                    )


                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Crossfade Duration",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Smooth Transitions Between Tracks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    Column(modifier = Modifier) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (crossfadeDuration > 0) {
                                        crossfadeDuration--
                                        preferencesManager.setCrossfadeDuration (crossfadeDuration)
                                    }
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "add",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text("$crossfadeDuration s", color = Color.White)
                            IconButton(
                                onClick = {
                                    if(crossfadeDuration < 10) {
                                        crossfadeDuration++
                                        preferencesManager.setCrossfadeDuration(crossfadeDuration)
                                    }
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "add",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                        }
                    }

                }


                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Gapless Playback",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enjoy Uninterrupted Music Listening",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    Switch(
                        checked = isGaplessPlaybackEnabled,
                        onCheckedChange = {
                            isGaplessPlaybackEnabled = it
                            preferencesManager.setGaplessPlaybackEnabled(it)
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

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "PlayerScreen Gestures",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Control Your Music with Simple Gestures",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    Switch(
                        checked = isGesturesEnabled,
                        onCheckedChange = {
                            isGesturesEnabled = it
                            preferencesManager.setGesturesEnabled(it)
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

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Auto Play",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Let the Music Play On Its Own",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    Switch(
                        checked = isAutoPlay,
                        onCheckedChange = {
                            isAutoPlay = it
                            preferencesManager.setAutoPlay(it)
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