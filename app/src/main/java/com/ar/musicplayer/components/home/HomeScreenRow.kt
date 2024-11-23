package com.ar.musicplayer.components.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ar.musicplayer.data.models.HomeListItem


@Composable
fun HomeScreenRow(
    title: String,
    data: List<HomeListItem>?,
    size: Int = 170,
    onCardClicked: (Boolean, HomeListItem) -> Unit,
) {


    if(data.isNullOrEmpty()){
        return
    }

    val list by remember {
        derivedStateOf {
            data
        }
    }

    Column(Modifier) {

        Heading(title = title)

        LazyRow(
            contentPadding = PaddingValues(vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            itemsIndexed(list) { index, item ->

                val cornerRadius = remember {
                    if (item.type == "radio_station" || item.type == "artist") 50 else 0
                }
                val radioOrNot = remember { item.type == "radio_station" }
                val subtitle = remember {
                    item.subtitle?.ifEmpty { item.moreInfoHomeList?.artistMap?.artists?.getOrNull(0)?.name.toString() }
                }

                HomeScreenRowCard(
                    item = item,
                    isRadio = radioOrNot,
                    subtitle = subtitle.toString(),
                    cornerRadius = cornerRadius,
                    imageUrl = item.image.toString(),
                    title = item.title.toString(),
                    size = size,
                    onClick = onCardClicked
                )
            }

        }
    }
}

@Composable
fun <T> HomeScreenRowCard(
    modifier: Modifier = Modifier,
    isRadio: Boolean,
    subtitle: String,
    cornerRadius: Int = 0,
    imageUrl: String?,
    title: String,
    size: Int,
    onClick: (Boolean, T) -> Unit,
    item: T
) {


    Column(
        modifier
            .width(size.dp)
    ) {
        Card(
            modifier = modifier
                .size((size).dp)
                .clickable { onClick(isRadio, item) },
            shape = RoundedCornerShape(percent = cornerRadius)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = title,
            color = Color.White,
            maxLines = 1,
            fontSize = 14.sp,
            textAlign = if (cornerRadius == 0) TextAlign.Left else TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 4.dp)
                .fillMaxWidth(),
            overflow = TextOverflow.Ellipsis,
        )
        if (cornerRadius == 0) {
            Text(
                text = subtitle,
                color = Color.Gray,
                maxLines = 1,
                fontSize = 12.sp,
                overflow = TextOverflow.Ellipsis
            )
        }

    }


}
