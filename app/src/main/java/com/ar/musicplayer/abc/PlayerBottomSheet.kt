package com.ar.musicplayer.abc

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ar.musicplayer.abc.BottomSheetState.Companion.rememberBottomSheetState


@Composable
fun PlayerBottomSheet(
    modifier: Modifier = Modifier,
    peekHeight: Dp = 120.dp,
    sheetContent: @Composable () -> Unit,
    sheetState: BottomSheetState = rememberBottomSheetState()
) {
    val screenHeight = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val peekHeightPx = with(LocalDensity.current) { peekHeight.toPx() }

    Box(modifier = modifier.fillMaxSize()) {
        val animatedHeight by animateFloatAsState(
            targetValue = if (sheetState.isExpanded) screenHeight else peekHeightPx
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { animatedHeight.toDp() })
                .background(MaterialTheme.colorScheme.surface)
                .align(Alignment.BottomCenter)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { /* Handle drag start if needed */ },
                        onDragEnd = {
                            // Update the expanded state based on the final position
                            sheetState.setExpanded(animatedHeight > screenHeight / 2)
                        },
                        onDragCancel = { /* Handle drag cancellation if needed */ },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            // Calculate the new fraction and update state
                            val newHeight = (animatedHeight - dragAmount).coerceIn(
                                peekHeightPx,
                                screenHeight
                            )
                            sheetState.currentFraction = newHeight / screenHeight
                        }
                    )
                }
        ) {
            sheetContent()
        }
    }
}

class BottomSheetState {
    var currentFraction by mutableStateOf(0f)
    val isExpanded: Boolean
        get() = currentFraction == 1f
    val isCollapsed: Boolean
        get() = currentFraction == 0f

    fun setExpanded(expanded: Boolean) {
        currentFraction = if (expanded) 1f else 0f
    }

    companion object {
        @Composable
        fun rememberBottomSheetState() = remember { BottomSheetState() }
    }
}


