import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ar.musicplayer.screens.libraryScreens.mymusic.toDp
import kotlinx.coroutines.launch


@Composable
fun MusicPlayingAni(
    isPlaying: Boolean,
    animatables: List<Animatable<Float, AnimationVector1D>>,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            animatables.forEachIndexed { index, animatable ->
                launch {
                    animatable.animateTo(
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(index * 100)
                        )
                    )
                }
            }
        } else {
            animatables.forEach { it.stop() }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        animatables.forEach { animatable ->
            MusicBars(heightFraction = animatable.value)
        }
    }
}

@Composable
fun MusicBars(heightFraction: Float) {
    val barHeight by remember(heightFraction) {
        derivedStateOf {
            100 *  heightFraction
        }
    }
    Box(
        modifier = Modifier
            .width(5.dp)
            .fillMaxHeight()
            .drawBehind {
                drawRect(
                    color = Color.Blue,
                    topLeft = Offset(0f, size.height - barHeight),
                    size = Size(size.width, barHeight)
                )
            }

    )

}



@Preview(showBackground = true)
@Composable
fun showanim(){
    val animatables = remember { List(5) { Animatable(0f) } }
    Box{
        MusicPlayingAni(isPlaying = true, animatables = animatables, modifier = Modifier
            .padding(start = 10.dp, end = 10.dp)
            .width(80.dp)
            .height(30.dp))
    }
}