package com.ar.musicplayer.components.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ar.musicplayer.components.mix.ArtistItem
import com.ar.musicplayer.data.models.Artist
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.screens.library.components.history.SimpleSongItem
import com.ar.musicplayer.viewmodel.AiViewModel

@Composable
fun AnimatedAIFloatingActionButton(
    aiViewModel: AiViewModel = hiltViewModel<AiViewModel>(),
    onSongClick: (SongResponse) -> Unit,
    onArtistClick: (Artist) -> Unit
) {
    val context = LocalContext.current

    val isLoading by aiViewModel.isLoading.collectAsState()

    var maxExpanded by remember { mutableStateOf( false ) }

    var isListening by remember { mutableStateOf(false) }
    var musicPreference by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }


    val recognizerListener = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                musicPreference = matches[0]
//                aiViewModel.getAiResponse(musicPreference)
            }
            isListening = false
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!partialMatches.isNullOrEmpty()) {
                musicPreference = partialMatches[0]
            }
        }

        override fun onError(error: Int) {
            isListening = false
        }

        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    val micIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 4000L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000L)
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }


    LaunchedEffect(Unit) {
        speechRecognizer.setRecognitionListener(recognizerListener)
    }

    val isImeVisible by keyboardAsState()

    val width = LocalConfiguration.current.screenWidthDp/4
    val size by animateDpAsState(
        targetValue = if (expanded) if(maxExpanded) (width * 1.5).dp else width.dp else 56.dp,
        animationSpec = tween(durationMillis = 300)
    )

    val color by animateColorAsState(
        targetValue = if(expanded) Color.White else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 300)
    )


    val modifier =
        if (expanded) {
            Modifier.pointerInput(Unit) {
                detectTapGestures(onTap = {
                    if(isImeVisible){
                        focusManager.clearFocus()
                    } else{
                        speechRecognizer.stopListening()
                        expanded = false
                    }
                })
            }
        } else{
            Modifier
        }

    val imeBottom = (WindowInsets.ime.getBottom(LocalDensity.current).dp / 2) + 100.dp


    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = imeBottom),
        contentAlignment = Alignment.BottomEnd
    ) {



        FloatingActionButton(
            onClick = {
                if (isListening) {
                    speechRecognizer.stopListening()
                } else {
                    if (!expanded) {
                        if (!maxExpanded) {
                            speechRecognizer.startListening(micIntent)
                            isListening = true
                        }
                        expanded = true
                    }
                }
            },
            modifier = Modifier.size(size),
            containerColor = color
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Voice Input",
                tint = Color.White
            )
            AnimatedVisibility(
                visible = expanded,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box{
                    Column{
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.inverseOnSurface)
                                .padding(5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini Icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "What's your vibe?",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .padding(start = 5.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "keyBoard",
                                    tint = Color.Black
                                )
                            }
                        }

                        TextField(
                            value =  musicPreference ,
                            onValueChange = { musicPreference = it },
                            modifier = Modifier
                                .fillMaxSize(),
                            textStyle = MaterialTheme.typography.headlineSmall,
                            placeholder = {
                                Text(
                                    text = "Type or talk your music request...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedPlaceholderColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary),
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        IconButton(
                            onClick = {
                                speechRecognizer.stopListening()
                                keyboardController?.show()
                                focusRequester.requestFocus()
                            },
                            modifier = Modifier
                                .focusRequester(focusRequester)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = "keyBoard",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                speechRecognizer.startListening(micIntent)
                                isListening = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "keyBoard",
                                tint = Color.White
                            )
                        }
                    }


                    IconButton(
                        onClick = {
                            aiViewModel.startLoading()
                            maxExpanded = true
                            keyboardController?.hide()
                            aiViewModel.getAiResponse(musicPreference)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "keyBoard",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                }


                AnimatedVisibility(
                    visible = maxExpanded,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.background(Color.White)
                ) {

                    if(isLoading){
                        AiLoadingAnimation(
                            onBackClick = remember {
                                {
                                    maxExpanded = false
                                    aiViewModel.clearResponse()
                                }
                            }
                        )
                    } else{
                        AiResponseDisplay(
                            aiViewModel = aiViewModel,
                            onArtistClick = onArtistClick,
                            onSongClick = onSongClick,
                            onBackClick = remember {
                                {
                                    maxExpanded = false
                                }
                            }
                        )
                    }


                }
            }
        }


    }
}



@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}




@Composable
fun AiResponseDisplay(
    aiViewModel: AiViewModel,
    onBackClick: () -> Unit,
    onArtistClick: (Artist) -> Unit,
    onSongClick: (SongResponse) -> Unit
) {

    val songResponseList by aiViewModel.aiSongResults.collectAsState()
    val artistResponseList by aiViewModel.aiArtistResults.collectAsState()
    val description by aiViewModel.description.collectAsState()
    val type by aiViewModel.type.collectAsState()
    val other by aiViewModel.other.collectAsState()
    val genre by aiViewModel.genre.collectAsState()
    val isLoading by aiViewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(start = 20.dp, top = 50.dp, end = 20.dp)
                .fillMaxSize()
        ) {
            item {
                if(description.isNotBlank()){
                    Row(verticalAlignment = Alignment.Top) {
                        if(songResponseList.isNotEmpty() || artistResponseList.isNotEmpty()){
                            Text(
                                text = "Description: ",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color.White
                            )
                        }
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.White
                        )
                    }
                }
            }

            item {
                if(genre.isNotBlank()){
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Genre: ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.White
                        )
                        Text(
                            text = genre,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.White
                        )
                    }
                }
            }



            item {
                if(other.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "Other: ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.White
                        )
                        Text(
                            text = other,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.White
                        )
                    }
                }
            }

            item {
                if(type.isNotEmpty()){
                    Row( verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Type: ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.White
                        )
                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.White
                        )
                    }
                }

            }


            items(songResponseList) { song ->
                SimpleSongItem(
                    songResponse = song,
                    onTrackSelect = {},
                    onClick = { onSongClick(song) }
                )
            }


            items(artistResponseList) { artist ->
                ArtistItem(
                    artist = artist.name.toString(),
                    artistImage = artist.image.toString(),
                    onClick = { onArtistClick(artist) }
                )
            }
        }


        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "back",
                tint = Color.White
            )
        }
    }
}


@Composable
fun AiLoadingAnimation(
    onBackClick: () -> Unit
){
    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        Box(modifier = Modifier.fillMaxWidth()){
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back"
                )
            }
        }
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(30))
                .background(
                    shimmerEffect(
                        targetValue = 900f,
                        duration = 4000
                    )
                )
        )
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(20.dp))
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(30))
                .background(
                    shimmerEffect(
                        targetValue = 900f,
                        duration = 3000
                    )
                )
        )
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(20.dp))
        Spacer(
            Modifier
                .width(200.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(30))
                .background(
                    shimmerEffect(
                        targetValue = 900f,
                        duration = 3000
                    )
                )
        )
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(20.dp))

    }
}



@Composable
fun shimmerEffect(showShimmer: Boolean = true, targetValue: Float = 1000f, duration: Int = 2000): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color.White.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            Color.White.copy(alpha = 0.6f),
        )


        val transition = rememberInfiniteTransition(label = "")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart
            ), label = ""
        )


        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {

        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}


