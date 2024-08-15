package com.ar.musicplayer.components.home

import android.util.Log
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.ar.musicplayer.data.models.HomeListItem
import com.ar.musicplayer.data.models.SongResponse
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import kotlinx.serialization.json.Json


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
    Column(Modifier) {

        Heading(title = title)

        LazyRow(
            contentPadding = PaddingValues(vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            itemsIndexed(data) { index, item ->

                val cornerRadius = remember {
                    if (item.type == "radio_station" || item.type == "artist") 50 else 0
                }
                val radioOrNot = remember { item.type == "radio_station" }
                val subtitle = remember {
                    item.subtitle?.ifEmpty { item.moreInfoHomeList?.artistMap?.artists?.getOrNull(0)?.name.toString() }
                }

                HomeScreenRowCard(
                    isRadio = radioOrNot,
                    subtitle = subtitle.toString(),
                    cornerRadius = cornerRadius,
                    imageUrl = item.image.toString(),
                    title = item.title.toString(),
                    size = size,
                    onClick =  { onCardClicked(it, item)}
                )
            }

        }
    }
}

@Composable
fun HomeScreenRowCard(
    isRadio: Boolean,
    subtitle: String,
    cornerRadius: Int = 0,
    imageUrl: String,
    title: String,
    size: Int,
    onClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {


    Column(
        modifier
            .width(size.dp)
    ) {
        Card(
            modifier = modifier
                .size((size).dp)
                .clickable { onClick(isRadio) },
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
