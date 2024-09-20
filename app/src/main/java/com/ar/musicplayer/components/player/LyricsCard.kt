package com.ar.musicplayer.components.player

import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.ar.musicplayer.components.CircularProgress
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.helper.darkenColor
import com.ar.musicplayer.utils.helper.isColorLight
import com.ar.musicplayer.viewmodel.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun LyricsCard(
    modifier: Modifier = Modifier,
    preferencesManager: PreferencesManager,
    colors: MutableState<ArrayList<Color>>,
    isLyricsLoading: State<Boolean>,
    lyricsData: State<List<Pair<Int, String>>>,
    lazyListState: LazyListState = rememberLazyListState(),
    onLyricsClick: (Int) -> Unit,
    currentLyricIndex: State<Int>
) {


    val perfectBackground =
        if(colors.value[0].isColorLight()){
            colors.value[0].darkenColor(0.7f)
        } else{
            colors.value[0]
        }


    val context = LocalContext.current

    Card(
        modifier = modifier
            .padding(20.dp)
            .aspectRatio(16f / 9f)
            .requiredHeightIn(min = 400.dp),
        shape = RoundedCornerShape(4),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ){
        Box(
            modifier = Modifier
                .background(perfectBackground)
        ) {

            if(isLyricsLoading.value){
                CircularProgress(background = Color.Transparent)
            } else{
                if(lyricsData.value.isNotEmpty()){
                    LyricsContent(
                        lazyListState = lazyListState,
                        currentLyricIndex = currentLyricIndex,
                        lyricsData = lyricsData,
                        onLyricsClick = onLyricsClick,
                        preferencesManager = preferencesManager
                    )
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
    lyricsData: State<List<Pair<Int, String>>>,
    currentLyricIndex: State<Int>,
    lazyListState: LazyListState,
    onLyricsClick: (Int) -> Unit,
    preferencesManager: PreferencesManager
) {

    val accentColor = Color(preferencesManager.getAccentColor())
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        LaunchedEffect(currentLyricIndex.value) {
            if (currentLyricIndex.value >= 0) {
                lazyListState.animateScrollToItem(index = currentLyricIndex.value, scrollOffset = - 150)
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
            itemsIndexed(lyricsData.value, key = { index, _ -> index }) { index, (duration, lyrics) ->
                val isHighlighted = currentLyricIndex.value == index
                val textColor = if (isHighlighted) accentColor else Color.LightGray

                LyricsText(
                    duration = duration,
                    text = lyrics.trim(),
                    color = textColor,
                    onLyricsClick = onLyricsClick
                )
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun LyricsText(
    duration: Int,
    text: String,
    color: Color,
    onLyricsClick: (Int) -> Unit,
){
    Text(
        text = text,
        color = color,
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
}
