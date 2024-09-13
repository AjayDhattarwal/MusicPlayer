@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ar.musicplayer.screens.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.RuntimeShader
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.components.player.ControlButton
import com.ar.musicplayer.components.player.FavToggleButton
import com.ar.musicplayer.components.player.PlayPauseLargeButton
import com.ar.musicplayer.components.player.SeDisplayName
import com.ar.musicplayer.components.player.SharedElementPager
import com.ar.musicplayer.components.player.TrackSlider
import com.ar.musicplayer.components.player.convertToText
import com.ar.musicplayer.data.models.sanitizeString
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.helper.PaletteExtractor
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteSongEvent
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.viewmodel.PlayerViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.intellij.lang.annotations.Language

@Language("AGSL")
val CUSTOM_SHADER = """
    uniform float time;
    uniform float2 resolution;
    layout(color) uniform half4 color0;
    layout(color) uniform half4 color1;
    layout(color) uniform half4 color2;
    layout(color) uniform half4 color3;

    half4 main(in float2 fragCoord) {
        float2 uv = fragCoord / resolution.xy;
        
        // Simulate wave turbulence with sine and cosine functions
        uv += 0.05 * sin(uv.yx * 10.0 + time * 0.6);
        uv += 0.03 * cos(uv.yx * 20.0 - time * 0.8);

        // Add rotational distortion to mimic the flow of a wave
        float angle = sin(time * 0.3) * 1.5;
        float s = sin(angle);
        float c = cos(angle);
        uv = mat2(c, -s, s, c) * (uv - 0.5) + 0.5;

        float noise1 = 0.5 + 0.5 * sin(distance(uv, vec2(0.3, 0.7)) * 15.0 + time * 1.0);
        float noise2 = 0.5 + 0.5 * cos(distance(uv, vec2(0.7, 0.3)) * 15.0 - time * 1.5);
        float noise3 = 0.5 + 0.5 * sin(distance(uv, vec2(0.5, 0.5)) * 10.0 + time * 1.3);

        half4 colorMix1 = mix(color0, color1, noise1);
        half4 colorMix2 = mix(color2, color3, noise2);
        half4 finalColor = mix(colorMix1, colorMix2, noise3 * 0.8);

        // Enhance the depth by modulating with a sine wave
        finalColor *= 0.8 + 0.2 * sin(time * 2.0 + uv.x * 5.0 + uv.y * 5.0);

        return finalColor;
    }
""".trimIndent()




val black = Color(0xFFB40EFC)


@UnstableApi
@Composable
fun AdaptiveMaxPlayer(
    onBack: () -> Unit,
    playerViewModel : PlayerViewModel,
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    animatedVisibilityScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope
) {
    val context = LocalContext.current
    val paletteExtractor = PaletteExtractor()
    val currentSong by playerViewModel.currentSong.collectAsState()
    var backgroundColors by remember {
        mutableStateOf(listOf(Color.Black, Color.Black, Color.Black, Color.Black))
    }

    val trackName = currentSong?.title.toString().sanitizeString()
    val artistName = currentSong?.moreInfo?.artistMap?.artists
        ?.distinctBy { it.name }
        ?.joinToString(", ") { it.name.toString() }
        ?.sanitizeString().toString()

    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val isBuffering by playerViewModel.isBuffering.collectAsState()
    val position by playerViewModel.currentPosition.collectAsState(0L)
    val duration by playerViewModel.duration.collectAsState(0L)


    var sliderPosition by remember(position) {
        mutableLongStateOf(position)
    }
    val currentPosition by remember(sliderPosition) {
        derivedStateOf{
            sliderPosition
        }
    }

    val repeatMode by playerViewModel.repeatMode.observeAsState(Player.REPEAT_MODE_OFF)
    val shuffleModeEnabled by playerViewModel.shuffleModeEnabled.observeAsState(false)

    var isDownloaded by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }

    val preferencesManager = remember {
        PreferencesManager(context)
    }

    val isFavouriteFlow by remember {
        derivedStateOf {
            favoriteViewModel.isFavoriteSong(currentSong?.id.toString())
        }
    }
    val isFavourite by isFavouriteFlow.collectAsState(false)



    LaunchedEffect(currentSong) {
        Log.d("launch", "called")
        currentSong?.image?.let { image ->

            val shadeLiveData = paletteExtractor.getColorsFromImg(image)
            shadeLiveData.let { shadeColor ->
                backgroundColors = shadeColor.map { it }
            }
        }
    }


    val systemUiController = rememberSystemUiController()

    LaunchedEffect(Unit) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent
        )
        systemUiController.isSystemBarsVisible = false
    }


    val time by produceState (0f) {
        while (true) {
            withInfiniteAnimationFrameMillis {
                value = it / 1000f * 0.5f
            }
        }
    }

    with(sharedTransitionScope) {
        Box(
            modifier =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Modifier
                    .drawWithCache {
                        val shader = RuntimeShader(CUSTOM_SHADER)
                        val shaderBrush = ShaderBrush(shader)
                        shader.setFloatUniform("resolution", size.width, size.height)
                        onDrawBehind {
                            shader.setFloatUniform("time", time)
                            backgroundColors.forEachIndexed { index, color ->
                                // Check for null before setting color uniform
                                if (color != null) {
                                    shader.setColorUniform(
                                        "color$index",
                                        android.graphics.Color.valueOf(
                                            color.red,
                                            color.green,
                                            color.blue,
                                            color.alpha
                                        )
                                    )
                                }
                            }
                            drawRect(shaderBrush)
                        }
                    }
            } else {
                Modifier
            }
                .fillMaxSize()
                .sharedBounds(
                    rememberSharedContentState(key = "bounds"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(){

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier
                        .height(400.dp)
                        .width(350.dp)) {
                        SharedElementPager(
                            modifier = Modifier,
                            playerViewModel = playerViewModel,
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }
                    SeDisplayName(
                        trackName = trackName,
                        artistName = artistName,
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope,
                        textStyle = MaterialTheme.typography.headlineLarge,
                    )
                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .weight(1f)
                    )

                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 76.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentPosition.convertToText(),
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .padding(start = 10.dp, end = 8.dp)
                    )

                    TrackSlider(
                        modifier = Modifier.weight(1f),
                        bufferedProgress = currentPosition.toFloat() + 10f,
                        value = currentPosition.toFloat(),
                        onValueChange = { newValue ->
                            sliderPosition = newValue.toLong()
                        },
                        onValueChangeFinished = {
                            playerViewModel.seekTo(sliderPosition)
                        },
                        songDuration = duration.toFloat()
                    )

                    Text(
                        text = duration.convertToText(),
                        color = Color.White,
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .padding(start = 8.dp, end = 10.dp)
                    )

                }


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp, start = 20.dp, end = 20.dp)
                ) {

                    FavToggleButton(
                        isFavorite = isFavourite,
                        onFavClick = remember {
                            {
                                if (currentSong?.id != "") {
                                    favoriteViewModel.onEvent(FavoriteSongEvent.ToggleFavSong(songResponse = currentSong!!))
                                }
                            }
                        }
                    )


                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.Center)
                    ){
                        ControlButton(
                            icon = Icons.Default.Shuffle,
                            size = 30.dp,
                            onClick = remember {
                                {
                                    playerViewModel.toggleShuffleMode()
                                }
                            },
                            tint = if (shuffleModeEnabled) Color(preferencesManager.getAccentColor()) else Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        ControlButton(
                            icon = Icons.Default.SkipPrevious,
                            size = 40.dp,
                            onClick = remember {
                                {
                                    playerViewModel.skipPrevious()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        Box(contentAlignment = Alignment.Center) {
                            PlayPauseLargeButton(
                                size = 50.dp,
                                isPlaying = isPlaying,
                                onPlayPauseClick = remember {
                                    {
                                        playerViewModel.playPause()
                                    }
                                }
                            )
                            if (isBuffering) {
                                CircularProgressIndicator()
                            }
                        }


                        Spacer(modifier = Modifier.width(10.dp))

                        ControlButton(
                            icon = Icons.Default.SkipNext,
                            size = 40.dp,
                            onClick = remember {
                                {
                                    playerViewModel.skipNext()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        ControlButton(
                            icon = Icons.Default.Repeat,
                            size = 30.dp,
                            onClick = remember {
                                {
                                    playerViewModel.setRepeatMode((repeatMode + 1) % 3)
                                }
                            },
                            tint = Color.LightGray
                        )
                    }


                    Row(modifier = Modifier.align(Alignment.CenterEnd)){
                        VolumeControl(
                            modifier = Modifier
                                .width(150.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.CloseFullscreen,
                                contentDescription = "exit Full Screen",
                                tint = Color.White
                            )
                        }
                    }


                }


                Spacer(
                    modifier = Modifier
                        .height(60.dp)

                )

            }


        }
    }
}


@Composable
fun VolumeControl(
    modifier: Modifier = Modifier
) {
    val audioManager = LocalContext.current.getSystemService(AudioManager::class.java)

    val currentVolume by remember {
        mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
    }
    var sliderPosition by remember { mutableStateOf(currentVolume.toFloat()) }

    val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
                val newVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                sliderPosition = newVolume.toFloat()
            }
        }
    }


    val context = LocalContext.current
    DisposableEffect(context) {
        val filter = IntentFilter().apply {
            addAction("android.media.VOLUME_CHANGED_ACTION")
        }
        context.registerReceiver(volumeReceiver, filter)

        onDispose { context.unregisterReceiver(volumeReceiver) }
    }

    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)


    val icon =
        if(sliderPosition >= maxVolume/2){
            Icons.Default.VolumeUp
        } else if(sliderPosition.toInt() == 0){
            Icons.Default.VolumeOff
        } else{
            Icons.Default.VolumeDown
        }

    var lastVolume by remember {
        mutableStateOf(currentVolume)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        IconButton(
            onClick = remember {
                {
                    if(sliderPosition.toInt() == 0){
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, lastVolume, 0)
                    } else{
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                    }
                }
            }
        ) {
            Icon(
                imageVector = icon ,
                contentDescription = "volume",
                tint = Color.White
            )
        }
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sliderPosition.toInt(), 0)
            },
            valueRange = 0f..maxVolume.toFloat(),
            onValueChangeFinished =  remember{
                {
                    if(sliderPosition.toInt() != 0){
                        lastVolume = sliderPosition.toInt()
                    }
                }
            },
            colors = SliderDefaults.colors(
                disabledThumbColor = Color.Transparent,
                activeTrackColor = Color.White,
                activeTickColor = Color.Transparent,
                thumbColor = Color.White,
                inactiveTrackColor = Color.Gray,
                inactiveTickColor = Color.Transparent,

            )
        )
    }
}



