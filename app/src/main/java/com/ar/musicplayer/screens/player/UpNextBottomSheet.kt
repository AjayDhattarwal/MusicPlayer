package com.ar.musicplayer.screens.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ar.musicplayer.screens.player.BottomSheetState.Companion.rememberBottomSheetState
import kotlin.ranges.coerceIn


@Composable
fun UpNextBottomSheet(
    modifier: Modifier = Modifier,
    sheetExpandedHeight: Dp = LocalConfiguration.current.screenHeightDp.dp,
    peekHeight: Dp = 60.dp,
    sheetContent: @Composable () -> Unit,
    dragHandler: @Composable () -> Unit,
    sheetState: BottomSheetState = rememberBottomSheetState(),
    sheetBackgroundColor: Color = MaterialTheme.colorScheme.background,
) {
    val density = LocalDensity.current
    val screenHeight = with(density) { sheetExpandedHeight.toPx() }
    val peekHeightPx = with(density) { peekHeight.toPx() }

    if (sheetState.currentFraction == 0f) {
        sheetState.updateFraction(peekHeightPx / screenHeight)
    }

    val animatedHeight by animateFloatAsState(
        targetValue = maxOf(peekHeightPx,(sheetState.currentFraction * screenHeight)) ,
        animationSpec = tween(durationMillis = 300)
    )



    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { animatedHeight.toDp() })
                .background(sheetBackgroundColor)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .draggable(
                    state = rememberDraggableState { delta ->
                        sheetState.updateFractionbyDelta(delta, screenHeight)
                    },
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity ->
                        val targetFraction = sheetState.calculateTargetFraction()
                        sheetState.updateFraction(targetFraction)
                    }
                )
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clickable {
                            if(sheetState.isExpanded){
                                sheetState.setExpanded(false)
                            } else{
                                sheetState.setExpanded(true)
                            }
                        }
                        .background(Color.White, shape = RoundedCornerShape(2.dp))
                )
                dragHandler()

                sheetContent()
            }
        }
    }
}



class BottomSheetState {

    private val _currentFraction = mutableStateOf(0f)
    val currentFraction: Float
        get() = _currentFraction.value

    val isExpanded: Boolean
        get() = currentFraction == 1f
    val isCollapsed: Boolean
        get() = currentFraction == 0f

    fun setExpanded(expanded: Boolean) {
        _currentFraction.value = if (expanded) 1f else 0f
    }

    fun updateFraction(fraction: Float) {
        _currentFraction.value = fraction.coerceIn(0f, 1f)
    }

    fun updateFractionbyDelta(deltaY: Float, screenHeight: Float) {
        val newFraction = (_currentFraction.value - deltaY / screenHeight).coerceIn(0f, 1f)
        _currentFraction.value = newFraction
    }


    fun calculateTargetFraction(): Float {
        // Implement logic to determine the target fraction based on the current fraction
        // For example:
        val threshold = 0.5f
        return if (currentFraction >= threshold) 1f else 0f
    }

    companion object {
        @Composable
        fun rememberBottomSheetState() = remember { BottomSheetState() }
    }
}
