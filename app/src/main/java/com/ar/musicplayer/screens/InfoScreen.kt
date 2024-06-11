package com.ar.musicplayer.screens


import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.ar.musicplayer.R
import com.ar.musicplayer.api.createApiUrl
import com.ar.musicplayer.models.Artist
import com.ar.musicplayer.models.ArtistMap
import com.ar.musicplayer.models.MoreInfoHomeList
import com.ar.musicplayer.models.HomeListItem
import com.ar.musicplayer.viewmodel.ApiCallViewModel
import com.ar.musicplayer.viewmodel.ImageColorGradient
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.api.ApiService
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.navigation.PlayerScreenObj
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


@SuppressLint("UnrememberedMutableState")
@Composable
fun InfoScreen(navController: NavHostController, homeListItem: HomeListItem) {

    val context = LocalContext.current
    val viewModel: ImageColorGradient = viewModel()

    val apiCallViewModel: ApiCallViewModel = viewModel()

    LaunchedEffect(homeListItem.image) {
        if (homeListItem.image != null) {
            viewModel.loadImage(homeListItem.image, context)
        }
        apiCallViewModel.getApiData(
            homeListItem.permaUrl?.substringAfterLast('/') ?: "",
            homeListItem.type?:"song",
            homeListItem.moreInfoHomeList?.songCount.toString(),
            "1",
            "webapi.get"
        )

    }
    val apiData by apiCallViewModel.apiLiveData.observeAsState()

    val bitmapState = viewModel.bitmapState.observeAsState()
    val imageColorState = viewModel.imageColor.observeAsState()



    val blackToGrayGradient = remember {
        mutableStateOf(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF000000), Color(0xFF161616)),
                startY = Float.POSITIVE_INFINITY,
                endY = 0f
            )
        )
    }

    LaunchedEffect(bitmapState.value, imageColorState.value) {
        if (bitmapState.value != null && imageColorState.value != null) {
            blackToGrayGradient.value = Brush.verticalGradient(
                colors = listOf(Color(0xFF000000), Color(0xFF1F1E1E), imageColorState.value!!),
                startY = Float.POSITIVE_INFINITY,
                endY = 0f
            )
        }
    }


    val showShimmer = remember { mutableStateOf(true) }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(blackToGrayGradient.value),
        containerColor = Color.Transparent,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            homeListItem.image?.let {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { navController.popBackStack() },
                        modifier = Modifier
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier,
                            tint = Color.White
                        )
                    }

                    AsyncImage(
                        model = homeListItem.image,
                        contentDescription = "image",
                        modifier = Modifier
                            .size(250.dp)
                            .padding(top = 20.dp, start = 30.dp, end = 30.dp)
                            .weight(1f)
                            .background(brush = ShimmerEffect(showShimmer.value))
                            .clip(RoundedCornerShape(10.dp)),
                        onSuccess = { showShimmer.value = false },
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )

                    IconButton(onClick = { /* Handle menu button click */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            modifier = Modifier,
                            tint = Color.White
                        )
                    }
                }


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 40.dp, end = 20.dp)
                ) {
                    homeListItem.title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 10.dp),
                            maxLines = 2,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if(homeListItem.type != "playlist"){
                        Text(
                            text = "Artists: ${homeListItem.moreInfoHomeList?.artistMap?.artists?.joinToString { it.name ?: "" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            maxLines = 1,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Row {
                        if(homeListItem.type != "album"){
                            Text(
                                text = "${homeListItem.moreInfoHomeList?.songCount} Songs, ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray,
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        Text(
                            text = "Type: ${homeListItem.type}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            maxLines = 1,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Row(modifier = Modifier) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { /* Handle share button click */ }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_play_circle_filled_24),
                                        contentDescription = "play",
                                        modifier = Modifier.size(300.dp)
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { /* Handle share button click */ }) {
                            Icon(
                                Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { /* Handle download button click */ }) {
                            Icon(
                                Icons.Default.FileDownload,
                                contentDescription = "Download",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { /* Handle share button click */ }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }


                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Songs", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(apiData?.list ?: emptyList()) { apiData ->
                    val showShimmer = remember { mutableStateOf(true) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 5.dp, top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = apiData.image,
                            contentDescription = "image",
                            modifier = Modifier
                                .size(50.dp)
                                .padding(4.dp)
                                .background(brush = ShimmerEffect(showShimmer.value))
                                .clip(RoundedCornerShape(3.dp)),
                            onSuccess = { showShimmer.value = false },
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                        val senderData = Json.encodeToString(SongResponse.serializer(),apiData)
                        Column(
                            modifier = Modifier
                                .padding(15.dp, top = 5.dp, bottom = 5.dp, end = 10.dp)
                                .weight(1f)
                                .clickable {
                                    navController.navigate(
                                        PlayerScreenObj(senderData)
                                    )
                                }
                        ) {
                            Text(
                                text = apiData.title ?: "null",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 2.dp),
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = apiData.subtitle ?: "unknown",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(onClick = { /* Handle download button click */ }) {
                            Icon(
                                Icons.Default.FileDownload,
                                contentDescription = "Download",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { /* Handle share button click */ }) {
                            Icon(
                                Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { /* Handle menu button click */ }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}





