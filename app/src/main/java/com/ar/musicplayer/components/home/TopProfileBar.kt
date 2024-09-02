package com.ar.musicplayer.components.home

import android.icu.util.Calendar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

@Composable
fun TopProfileBar(
    modifier: Modifier = Modifier,
    title: String = "UserName",
    color: Color = Color.White,
    onClick: () -> Unit,
    onUserFiledClick: () -> Unit
){

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
//        AsyncImage(
//            model = "",
//            contentDescription = "Profile",
//            modifier = Modifier
//                .size(50.dp)
//                .clip(CircleShape)
//
//        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
                .clickable { onUserFiledClick() }
        ) {
            Text(
                text = "${getGreeting()},",
                modifier = Modifier.wrapContentSize(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                color = color,
            )
            Spacer(modifier =Modifier.height(2.dp))
            Text(
                text = title,
                modifier = Modifier.wrapContentSize(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                maxLines = 1,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )
        }
        IconButton(
            onClick = onClick,
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
    TopProfileBar(onClick = {}, onUserFiledClick = {})
}