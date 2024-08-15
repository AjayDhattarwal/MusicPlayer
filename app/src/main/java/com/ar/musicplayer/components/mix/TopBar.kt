package com.ar.musicplayer.components.mix

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    showHeart: Boolean = true,
    skip: Boolean = false,
    alpha: Float = 1f,
    title: String? = "Album",
    onBackPressed: () -> Unit,
    onMoreOptionClicked: () -> Unit = {},
    onLikeButtonClicked: () -> Unit = {},
    backgroundColor: Color,
    paddingStart: Int = 8,
    liked: Boolean = false,
    showThreeDots: Boolean = true
) {

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title ?: "",
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(16.dp)
                    .alpha(
                        alpha = alpha
                    )
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {onBackPressed()}
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, tint = Color.White,
                    contentDescription = "back",
                    modifier = Modifier
                        .alpha(alpha)
                        .padding(start = paddingStart.dp)
                )
            }
        },

        modifier = Modifier,
//            .alpha(alpha = if (skip && alpha != 1.0f) 0f else 1f),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
        )
    )

//    Row(
//        Modifier
//            .padding(5.dp)
//            .fillMaxWidth()
//            .background(backgroundColor)
//            .alpha(alpha = if (skip && alpha != 1.0f) 0f else 1f),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        IconButton(
//            onClick = {onBackPressed()}
//        ) {
//            Icon(
//                imageVector = Icons.Default.ArrowBack, tint = Color.White,
//                contentDescription = "back",
//                modifier = Modifier
//                    .alpha(alpha)
//                    .padding(start = paddingStart.dp)
//            )
//        }
//
//        Text(
//            text = title ?: "",
//            color = Color.White,
//            modifier = Modifier
//                .padding(16.dp)
//                .alpha(
//                    alpha = alpha
//                )
//                .weight(1f)
//        )
//
//        Row {
//            if (showHeart) {
//                Icon(
//                    imageVector = Icons.Default.Favorite,
//                    contentDescription = null,
//                    tint = if (liked) Color.Red else Color.Transparent,
//                    modifier = Modifier
//                        .padding(4.dp)
//                        .size(20.dp)
//                        .clickable {
//                            onLikeButtonClicked()
//                        },
//                )
//            }
//
//            if (showThreeDots && !skip)
//                Icon(
//                    imageVector = Icons.Default.MoreVert, tint = Color.White,
//                    contentDescription = null,
//                    modifier = Modifier.padding(end = paddingStart.dp).clickable {
//                        onMoreOptionClicked()
//                    }
//                )
//
//        }
//
//    }
}