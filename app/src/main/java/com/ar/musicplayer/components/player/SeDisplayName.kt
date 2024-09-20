@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ar.musicplayer.components.player

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun SeDisplayName(
    trackName: String,
    artistName: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
){
    Column(modifier = modifier) {
        Text(
            text = trackName,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier
                .basicMarquee(
                    animationMode = MarqueeAnimationMode.Immediately,
                    repeatDelayMillis = 2000,
                    initialDelayMillis = 2000,
                    iterations = Int.MAX_VALUE
                ),
            style  = textStyle,
            overflow = TextOverflow.Ellipsis,

            )

        Text(
            text = artistName,
            color = Color.LightGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .basicMarquee(
                    animationMode = MarqueeAnimationMode.Immediately,
                    repeatDelayMillis = 2000,
                    initialDelayMillis = 2000
                )

        )
    }

}