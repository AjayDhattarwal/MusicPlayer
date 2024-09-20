package com.ar.musicplayer.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp


private val CollapsedSize = 70.dp
private val CollapsedBoxPadding = CollapsedSize - 5.dp
private val ExpandedHeight = 330.dp

@Composable
fun CollapsingImageLayout(
    collapseFractionProvider: () -> Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        check(measurables.size == 1)

        val collapseFraction = collapseFractionProvider()

        // Minimum size when collapsed (70.dp) and maximum height (300.dp when expanded)
        val imageMinSize = CollapsedSize.roundToPx()
        val imageMaxWidth = constraints.maxWidth
        val imageMaxHeight = ExpandedHeight.roundToPx()

        // Ensure image size is always valid
        val imageHeight = lerp(imageMinSize, imageMaxHeight, collapseFraction)
        val imageWidth = lerp(imageMinSize, imageMaxWidth, collapseFraction)

        // Measure the content with interpolated size
        val imagePlaceable = measurables[0].measure(Constraints.fixed(imageWidth, imageHeight))

        // Y position is optional, here we keep it fixed at 0 (no vertical movement)
        val imageY = 0

        // Place the image centered horizontally and fill the width
        layout(
            width = imageMaxWidth,
            height = imageHeight
        ) {
            imagePlaceable.placeRelative(0, imageY) // No need for imageX as it's full width
        }
    }
}


@Composable
fun CollapsingBoxWithPadding(
    collapseFractionProvider: () -> Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->

        val collapseFraction = collapseFractionProvider()

        check(measurables.size == 1)

        val maxWidth = constraints.maxWidth

        // Measure the content as usual
        val minPadding = CollapsedBoxPadding.roundToPx()
        val interpolatedPadding = lerp(minPadding,maxWidth, collapseFraction)


        val imageMinSize = CollapsedSize.roundToPx()
        val imageMaxHeight = ExpandedHeight.roundToPx()

        val imageHeight = lerp(imageMinSize, imageMaxHeight, collapseFraction)

        val placeable = measurables[0].measure(Constraints.fixed(maxWidth,imageHeight))


        layout(
            width = maxWidth,
            height = imageHeight
        ) {
            // Place the content with the calculated padding from the start
            placeable.placeRelative(interpolatedPadding, 0)
        }
    }
}
