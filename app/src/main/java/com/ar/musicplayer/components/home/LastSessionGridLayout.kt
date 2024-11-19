package com.ar.musicplayer.components.home

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.viewmodel.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun LastSessionGridLayout(
    modifier: Modifier = Modifier,
    onRecentSongClicked: (SongResponse) -> Unit,
    playerViewModel: PlayerViewModel
) {

    val lastSession by playerViewModel.lastSession.collectAsState()
    val lastSessionList by remember {
        derivedStateOf {
            lastSession
        }
    }
    val gridHeight by remember {
        derivedStateOf {
            if (lastSessionList.size < 2) 80.dp else if (lastSessionList.size in 2..6) 180.dp else 280.dp
        }
    }

    val gridCells by remember {
        derivedStateOf {
            if (lastSessionList.size < 2) 1 else if (lastSessionList.size in 2..6) 2 else 3
        }
    }

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Last Session",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        LazyHorizontalGrid(
            rows = GridCells.Fixed(gridCells),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier
                .animateContentSize()
                .height(gridHeight)
        ) {

            itemsIndexed(lastSessionList, key = {index, item -> (item.second.id + index) }) { index,songResponse ->
                Card(
                    modifier = Modifier
                        .height(50.dp)
                        .width(250.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .clickable {
                            onRecentSongClicked(songResponse.second)
                        },
                    colors = CardColors(
                        containerColor = Color(0xBC383838),
                        contentColor = Color.Transparent,
                        disabledContentColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = songResponse.second.image,
                            contentDescription = "image",
                            modifier = Modifier
                                .size(80.dp),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                        Column(
                            modifier = Modifier
                                .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                                .weight(1f)

                        ) {
                            Text(
                                text = songResponse.second.title ?: "null",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 2.dp),
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = songResponse.second.subtitle ?: "unknown",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}