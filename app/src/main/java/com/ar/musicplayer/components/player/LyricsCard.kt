package com.ar.musicplayer.components.player

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ar.musicplayer.components.CircularProgress
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.helper.darkenColor
import com.ar.musicplayer.utils.helper.isColorLight

@Composable
fun LyricsCard(
    lyricsData: List<Pair<Int, String>>,
    currentLyricIndex: State<Int?>,
    isLyricsLoading: Boolean,
    modifier: Modifier,
    background: Color,
    lyricClicked: (Int) -> Unit
){
    val perfectBackground =
        if(background.isColorLight()){
            background.darkenColor(0.7f)
        } else{
            background
        }

    val preferencesManager = PreferencesManager(LocalContext.current)

    val context = LocalContext.current
    val color = Color(preferencesManager.getAccentColor())
    val lyricsLazyListState = rememberLazyListState()

    LaunchedEffect(currentLyricIndex.value) {

        if((currentLyricIndex.value ?: 0) >= 0){

            lyricsLazyListState.animateScrollToItem(
                index = currentLyricIndex.value!!,
                scrollOffset = -150
            )
        } else{
            lyricsLazyListState.scrollToItem(0)
        }
    }

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
                    LazyColumn(
                        state = lyricsLazyListState,
                        modifier = Modifier.padding(top = 40.dp, start = 10.dp, end = 10.dp)
                    ) {

                        item {
                            Spacer(Modifier.height(30.dp))
                        }
                        itemsIndexed(lyricsData){ index, (duration, lyrics) ->
                            val mainIndex = if((currentLyricIndex.value?:0) <= 0) 0 else currentLyricIndex.value
                            val perfectColor = if(mainIndex == index) color else Color.LightGray
                            Text(
                                text = lyrics.trim(),
                                color = perfectColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        lyricClicked(duration)
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
                modifier = Modifier.fillMaxWidth()
                    .background(brush = Brush.verticalGradient(
                        colors = listOf(
                            perfectBackground,
                            perfectBackground,
                            perfectBackground.copy(0.8f),
                            Color.Transparent,
                        )
                    ))
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
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://lrclib.net"))
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