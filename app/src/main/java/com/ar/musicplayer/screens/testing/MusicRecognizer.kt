package com.ar.musicplayer.screens.testing

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.ar.musicplayer.components.home.HomeScreenRowCard
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.TrackRecognition
import com.ar.musicplayer.utils.AudioRecorder.AndroidAudioRecorder
import com.ar.musicplayer.utils.permission.hasPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun MusicRecognizer(playSong: (SongResponse) -> Unit,togglePlaying: () -> Unit, backHandler: @Composable () -> Unit) {

    val context = LocalContext.current

    val viewModel: MusicRecognizerViewModel = viewModel()

    val pair by viewModel.trackResponse.collectAsState()
    val track by remember { derivedStateOf { pair?.first } }
    val relatedTracks by remember { derivedStateOf { pair?.second } }


    if(track != null){
        BackHandler {
            viewModel.clearResult()
        }
    }else{
        backHandler()
    }
    val scrollState = rememberScrollState()

    var isPlayed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        if (track == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Box(Modifier.size(300.dp)){
                    RotatingBallsCanvas(context){
                        viewModel.recognizeSong(it)
                    }
                }
            }
        }else{
            Column(
                modifier = Modifier
                    .drawBehind {
                        drawRect(
                            color = Color.Black,
                        )
                    }
                    .verticalScroll(scrollState)
                    .fillMaxSize()
            ) {
                EnhancedTrackDetailsUI(
                    track = track,
                    togglePlaying = togglePlaying
                ){
                    if(!isPlayed){
                        if(it.title != null){
                            playSong(SongResponse(title = it.title, subtitle = it.subtitle, isYoutube = true))
                            isPlayed = true
                        }
                    }
                }
                if(track?.relatedTracksUrl != null) {
                    Text(
                        text = "Related",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                LazyRow(
                    contentPadding = PaddingValues(vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Spacer(Modifier.width(1.dp))
                    }

                    itemsIndexed(relatedTracks?.tracks ?: emptyList(), key = { index, item ->  item.key ?: index }){ index , track ->

                        HomeScreenRowCard(
                            item = track,
                            isRadio = false,
                            subtitle = track.subtitle ?: "",
                            cornerRadius = 0,
                            imageUrl = track.images?.coverart ?: track.images?.coverarthq ?: track.images?.background,
                            title = track.title ?: "",
                            size = 170,
                            onClick = { _, _ ->

                            }
                        )
                    }
                }
                Spacer(Modifier.height(120.dp))
            }

        }

    }
}

@Composable
fun EnhancedTrackDetailsUI(track: TrackRecognition?, togglePlaying: () -> Unit,  onPlayPause: (TrackRecognition) -> Unit = {}) {
    val images = track?.images
    var isPlaying by remember { mutableStateOf(false) }

    Column(modifier = Modifier) {
        Box {

            Image(
                painter = rememberAsyncImagePainter(images?.background ?: images?.coverarthq ?: images?.coverart),
                contentDescription = "Background Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = 0.4f
                    }
                    .background(Color.Black)
                    .height(300.dp),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .offset {
                        IntOffset(0, 180)
                    },
                verticalAlignment = Alignment.CenterVertically
            ){

                TrackImageWithPlayPause(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    isPlaying = { isPlaying },
                    onPlayPauseClick = {
                        track?.let { onPlayPause(it) }
                        togglePlaying()
                        isPlaying = !isPlaying
                    } ,
                    imageUrl = images?.coverart ?: images?.coverarthq ?: images?.background
                )

                Column(
                    Modifier.offset {
                        IntOffset(0, 120)
                    }
                ) {
                    Text(
                        text = track?.title ?: "Unknown Title",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track?.subtitle ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        maxLines = 1,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Text(
                        text = "Genre: ${track?.genres?.primary ?: "Unknown"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.LightGray,
                        maxLines = 1,
                        modifier = Modifier
                    )
                }
            }

            // Main Cover Art overlapping the background
        }

        Spacer(modifier = Modifier.height(70.dp))


        track?.sections?.forEach { section ->
            if(section.tabname != "Related"){
                Text(
                    text = if(section.tabname == "Song") "Details" else section.tabname ?: "Section",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                section.metadata?.forEach { metadata ->
                    Text(
                        text = "${metadata.title}: ${metadata.text}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        modifier = Modifier.padding(start = 36.dp, top = 4.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
fun TrackImageWithPlayPause(
    modifier: Modifier = Modifier,
    isPlaying: ()  -> Boolean,
    onPlayPauseClick: () -> Unit,
    imageUrl: String?
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onPlayPauseClick() }
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = "Cover Art",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Icon(
            imageVector = if (isPlaying()) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = "PlayPause",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .padding(8.dp)
        )
    }
}




@Composable
fun RotatingBallsCanvas(
    context: Context,
    modifier: Modifier = Modifier.fillMaxSize(),
    numBalls: Int = 180,
    baseRadius: Float = 250f,
    viewingDistance: Float = 400f,
    waveFrequency: Float = 2f,
    onRecorded: (String) -> Unit = {}
) {
    var waveAmplitude by remember { mutableStateOf(0f) }
    val audioScope = rememberCoroutineScope()
    val recorder by lazy {
        AndroidAudioRecorder(context.applicationContext)
    }

    LaunchedEffect(Unit) {
        val file = File(context.cacheDir, "audio.mp3").also {
            recorder.start(it)
        }
        delay(10000)
        recorder.stop()
        onRecorded(file.absolutePath)
    }


    DisposableEffect(Unit) {
        val job = audioScope.launch(Dispatchers.IO) {
            val audioRecord = setupAudioRecord(context)
            if (audioRecord != null) {
                val bufferSize = AudioRecord.getMinBufferSize(
                    44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val buffer = ShortArray(bufferSize)

                audioRecord.startRecording()
                val startTime = System.currentTimeMillis()

                try {
                    while (isActive && (System.currentTimeMillis() - startTime) < 10000) { // Check for 10 seconds
                        val read = audioRecord.read(buffer, 0, buffer.size)
                        if (read > 0) {
                            waveAmplitude = (calculateAmplitude(buffer) / 300).coerceIn(0f, 70f)
                        }
                    }
                    waveAmplitude = 0f

                } finally {
                    waveAmplitude = 0f
                    audioRecord.stop()
                    audioRecord.release()
                }
            }
        }

        onDispose {
            job.cancel()
        }
    }

    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }


    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        val goldenAngle = Math.PI * (3.0 - Math.sqrt(5.0)) // ~137.5 degrees

        for (i in 0 until numBalls) {
            val theta = i * goldenAngle
            val phi = Math.acos(1 - 2 * (i + 0.5) / numBalls)

            val distanceFromCenter = baseRadius
            val waveOffset = waveAmplitude * sin(
                (distanceFromCenter / baseRadius) * waveFrequency
            )
            val dynamicRadius = baseRadius + waveOffset

            // 3D position on the sphere
            val x = dynamicRadius * sin(phi) * cos(theta)
            val y = dynamicRadius * sin(phi) * sin(theta)
            val z = dynamicRadius * cos(phi)

            // Apply rotation
            val rotatedX = x * cos(rotation.value.toRadians()) - z * sin(rotation.value.toRadians())
            val rotatedZ = x * sin(rotation.value.toRadians()) + z * cos(rotation.value.toRadians())
            val ballRadius = ( 0.5f * waveAmplitude).coerceIn(5f,8f)

            // Project 3D position to 2D
            val screenX = (rotatedX * viewingDistance / (rotatedZ + viewingDistance)) + centerX
            val screenY = (y * viewingDistance / (rotatedZ + viewingDistance)) + centerY

            val color = Color(
                red = (0.5f + 0.5f * (rotatedZ / baseRadius)).toFloat(),
                green = 0.2f + (waveOffset / waveAmplitude * 0.3f).coerceIn(0f,0.3f),
                blue = 0.7f + 0.3f * (rotatedZ / baseRadius).toFloat(),
                alpha = 1f
            )

            // Draw the ball
            drawCircle(
                center = Offset(screenX.toFloat(), screenY.toFloat()),
                radius = ballRadius,
                color = color,
                style = Fill
            )
        }
    }

}

private fun Float.toRadians() = (this * Math.PI / 180).toFloat()




fun setupAudioRecord(context: Context): AudioRecord? {
    val sampleRate = 44100
    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
    if (ActivityCompat.checkSelfPermission(
           context,
           Manifest.permission.RECORD_AUDIO
       ) != PackageManager.PERMISSION_GRANTED
   ) {
       return null
   }
   return AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSize
    )
}


fun calculateAmplitude(buffer: ShortArray): Float {
    var sum = 0f
    for (sample in buffer) {
        sum += Math.abs(sample.toFloat())
    }
    return sum / buffer.size
}






private fun requestPermissions(context: ComponentActivity) {
    val permissions = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    if (!hasPermissions(context, *permissions)) {
        ActivityCompat.requestPermissions(context, permissions, 0)
    }
}



