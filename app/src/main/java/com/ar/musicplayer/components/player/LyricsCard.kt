package com.ar.musicplayer.components.player

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.ar.musicplayer.components.CircularProgress
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.helper.darkenColor
import com.ar.musicplayer.utils.helper.isColorLight
import com.ar.musicplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

@OptIn(UnstableApi::class)
@Composable
fun LyricsCard(
    modifier: Modifier = Modifier,
    background: Color,
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {

    val isLyricsLoading by playerViewModel.isLyricsLoading.collectAsState()
    val lyricsData by playerViewModel.lyricsData.collectAsState()
    val perfectBackground =
        if(background.isColorLight()){
            background.darkenColor(0.7f)
        } else{
            background
        }


    val context = LocalContext.current
    val preferencesManager =  remember{ PreferencesManager(context) }

    val color = Color(preferencesManager.getAccentColor())
    val lazyListState = rememberLazyListState()

    Card(
        modifier = modifier.padding(20.dp),
        shape = RoundedCornerShape(4),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ){
        Box(
            modifier = Modifier
                .background(perfectBackground)
                .fillMaxSize()
        ) {

            if(isLyricsLoading){
                CircularProgress(background = Color.Transparent)
            } else{
                if(lyricsData.isNotEmpty()){
                    LyricsContent(
                        accentColor = color,
                        playerViewModel = playerViewModel,
                        lazyListState = lazyListState
                    ){
                        playerViewModel.seekTo(it.toLong())
                    }
                }
                else{
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Lyrics Not Available",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                }
            }
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                perfectBackground,
                                perfectBackground,
                                perfectBackground.copy(0.8f),
                                Color.Transparent,
                            )
                        )
                    )
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center
            ){
                Row{
                    Text(
                        text = "Lyrics",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier= Modifier.weight(1f)
                    )
                    AsyncImage(
                        model = "https://lrclib.net/assets/lrclib-370c57eb.png",
                        contentDescription = "lrclib.net",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(5))
                            .clickable {
                                val intent =
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://lrclib.net"))
                                context.startActivity(intent)
                            },
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(60.dp))
            }

        }

    }

}

@OptIn(UnstableApi::class)
@Composable
fun LyricsContent(
    accentColor: Color,
    playerViewModel: PlayerViewModel,
    lazyListState: LazyListState,
    onLyricsClick: (Int) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val lyricsData by playerViewModel.lyricsData.collectAsState()
        val currentLyricIndex by playerViewModel.currentLyricIndex.collectAsState(0)


        LaunchedEffect(currentLyricIndex) {
            if (currentLyricIndex >= 0) {
                lazyListState.animateScrollToItem(index = currentLyricIndex, scrollOffset = - 150)
            } else {
                lazyListState.scrollToItem(0)
            }
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.padding(top = 40.dp, start = 10.dp, end = 10.dp)
        ) {
            item {
                Spacer(Modifier.height(30.dp))
            }
            itemsIndexed(lyricsData, key = { index, _ -> index }) { index, (duration, lyrics) ->
                val isHighlighted = currentLyricIndex == index
                val textColor = if (isHighlighted) accentColor else Color.LightGray

                Text(
                    text = lyrics.trim(),
                    color = textColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onLyricsClick(duration)
                        },
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Start,
                    softWrap = true,
                    maxLines = 3,
                )
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}
