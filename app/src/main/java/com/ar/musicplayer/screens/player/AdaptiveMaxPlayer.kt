@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ar.musicplayer.screens.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.RuntimeShader
import android.media.AudioManager
import android.os.Build
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.ar.musicplayer.R
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
import kotlinx.coroutines.delay
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

        // Precompute wave offsets
        float waveX = time * 0.6;
        float waveY = time * 0.8;

        // Apply wave turbulence with optimized sine and cosine
        uv += 0.05 * sin(uv.yx * 10.0 + waveX);
        uv += 0.03 * cos(uv.yx * 20.0 - waveY);

        // Simplify rotational distortion
        float angle = sin(time * 0.3) * 1.5;
        float s = sin(angle);
        float c = cos(angle);
        uv = float2(
            c * (uv.x - 0.5) - s * (uv.y - 0.5) + 0.5,
            s * (uv.x - 0.5) + c * (uv.y - 0.5) + 0.5
        );

        // Simplify noise calculations
        float2 center1 = float2(0.3, 0.7);
        float2 center2 = float2(0.7, 0.3);
        float2 center3 = float2(0.5, 0.5);

        float noise1 = 0.5 + 0.5 * sin(dot(uv - center1, uv - center1) * 15.0 + time);
        float noise2 = 0.5 + 0.5 * cos(dot(uv - center2, uv - center2) * 15.0 - time * 1.5);
        float noise3 = 0.5 + 0.5 * sin(dot(uv - center3, uv - center3) * 10.0 + time * 1.3);

        // Blend colors with optimized mixing
        half4 colorMix1 = mix(color0, color1, noise1);
        half4 colorMix2 = mix(color2, color3, noise2);
        half4 finalColor = mix(colorMix1, colorMix2, noise3 * 0.8);

        // Add depth modulation with optimized sine wave
        finalColor *= 0.8 + 0.2 * sin(time * 2.0 + uv.x * 5.0 + uv.y * 5.0);

        return finalColor;
    }
""".trimIndent()



@UnstableApi
@Composable
fun AdaptiveMaxPlayer(
    onBack: () -> Unit,
    height: Dp,
    playerViewModel : PlayerViewModel,
    favoriteViewModel: FavoriteViewModel,
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

    val isPlaying = playerViewModel.isPlaying.collectAsState()
    val isBuffering by playerViewModel.isBuffering.collectAsState()
    val position by playerViewModel.currentPosition.collectAsState(0L)
    val duration by playerViewModel.duration.collectAsState(0L)


    var sliderPosition by remember(position) {
        mutableLongStateOf(position)
    }
    val currentPosition = remember(sliderPosition) {
        derivedStateOf{
            sliderPosition
        }
    }

    val repeatMode by playerViewModel.repeatMode.observeAsState(Player.REPEAT_MODE_OFF)
    val shuffleModeEnabled by playerViewModel.shuffleModeEnabled.observeAsState(false)


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
        currentSong?.image?.let { image ->

            val shadeLiveData = paletteExtractor.getColorsFromImg(image)
            shadeLiveData.let { shadeColor ->
                backgroundColors = shadeColor.map { it }
            }
        }
    }





    val recomposeScope = currentRecomposeScope

    var time by remember {
        mutableStateOf(0f)
    }

    LaunchedEffect(Unit){
        while (true){
            recomposeScope.invalidate()
            time += 0.01f
            delay(16)
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
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it },
                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                ),
//            contentAlignment = Alignment.BottomStart
        ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                ){

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .height(height)
                                .aspectRatio(2 / 3f)
                        ) {
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
                            textStyle = MaterialTheme.typography.headlineLarge,
                        )


                    }

                    Column(
                        modifier = Modifier.weight(0.3f),

                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 76.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = currentPosition.value.convertToText(),
                                color = Color.White,
                                style = TextStyle(fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .padding(start = 10.dp, end = 8.dp)
                            )

                            TrackSlider(
                                modifier = Modifier.weight(1f),
                                value = currentPosition,
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

                        Row(
                            modifier = Modifier.padding(top = 5.dp)
                                .fillMaxHeight(1f)
                                .fillMaxWidth()
                                .padding(top = 5.dp, start = 20.dp, end = 20.dp),
                        ) {

                            Box(modifier = Modifier.weight(0.3f)){
                                FavToggleButton(
                                    isFavorite = isFavourite,
                                    onFavClick = remember {
                                        {
                                            if (currentSong?.id != "") {
                                                favoriteViewModel.onEvent(
                                                    FavoriteSongEvent.ToggleFavSong(
                                                        songResponse = currentSong!!
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                )
                            }


                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(0.7f),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                ControlButton(
                                    icon = ImageVector.vectorResource(R.drawable.ic_shuffle),
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
                                    icon = ImageVector.vectorResource(R.drawable.ic_skip_previous_24),
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
                                    icon = ImageVector.vectorResource(R.drawable.ic_skip_next_24),
                                    size = 40.dp,
                                    onClick = remember {
                                        {
                                            playerViewModel.skipNext()
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(10.dp))

                                ControlButton(
                                    icon = ImageVector.vectorResource(R.drawable.ic_repeat),
                                    size = 30.dp,
                                    onClick = remember {
                                        {
                                            playerViewModel.setRepeatMode((repeatMode + 1) % 3)
                                        }
                                    },
                                    tint = Color.LightGray
                                )
                            }


                            Row(modifier = Modifier.weight(0.3f)) {
                                VolumeControl(
                                    modifier = Modifier
                                        .width(150.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                IconButton(onClick = onBack) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_cloase_full_screen),
                                        contentDescription = "exit Full Screen",
                                        tint = Color.White
                                    )
                                }
                            }


                        }


                    }
                    Spacer(modifier = Modifier.height(10.dp))

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
            ImageVector.vectorResource(R.drawable.ic_volume_up)
        } else if(sliderPosition.toInt() == 0){
            ImageVector.vectorResource(R.drawable.ic_volume_mute)
        } else{
            ImageVector.vectorResource(R.drawable.ic_volume_down)
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



