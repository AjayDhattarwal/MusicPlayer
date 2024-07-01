package com.ar.musicplayer.components

import android.icu.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ar.musicplayer.screens.shimmerEffectfun

@Composable
fun TopProfileBar(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
){

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "",
            contentDescription = "Profile",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(brush = shimmerEffectfun(true))

        )
        Column(
            modifier = Modifier.padding(10.dp).weight(1f)
        ) {
            Text(
                text = getGreeting(),
                modifier = Modifier.wrapContentSize(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight =  FontWeight.Normal
            )
            Text(
                text = "Ajay Dhattarwal",
                modifier = Modifier.wrapContentSize(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )
        }
        IconButton(
            onClick = {onClick()},
            modifier = Modifier.padding(start = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = Color.White
            )
        }

    }

}

fun getGreeting(): String{
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)

    return when (hour){
        in 0 .. 11 -> "Good Morning"
        in 12.. 16 -> "Good AfterNoon"
        in 17.. 23 -> "Good Evening"
        else -> "Hello"
    }
}


@Preview
@Composable
fun TopProfileBarPreview(){
    TopProfileBar(onClick = {})
}