package com.ar.musicplayer.screens.settings.subscreens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.ar.musicplayer.components.settings.FullScreenColorPickerDialog
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.helper.saveImageToInternalStorage
import com.ar.musicplayer.viewmodel.ThemeViewModel
import java.io.File
import java.io.InputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    themeViewModel: ThemeViewModel,
    preferencesManager: PreferencesManager,
    onBackClick: () -> Unit,
) {
    val backgroundGradient by themeViewModel.blackToGrayGradient.collectAsState()
    val context = LocalContext.current
    var accentColor by remember { mutableStateOf(Color(preferencesManager.getAccentColor())) }

    var isSystemTheme by remember { mutableStateOf(false) }
    var isImageAsBackGround by remember { mutableStateOf(preferencesManager.isImageAsBackground()) }
    val backgroundBitmap by themeViewModel.imageBackground.collectAsState()

    var linearColors by remember { mutableStateOf(preferencesManager.getLinearColors()) }
    val fetchedColors by remember {
        derivedStateOf {
            linearColors
                .takeIf { it.isNotBlank() }
                ?.split(",")
                ?.mapNotNull { colorString ->
                    colorString.toIntOrNull()?.let { Color(it) }
                }
                ?.takeIf { it.isNotEmpty() }
                ?: listOf(Color.Black, Color.White) // Fallback to default colors
        }
    }

    var showAccentColorPicker by remember { mutableStateOf(false) }
    var showGradientColorPicker1 by remember { mutableStateOf(false) }
    var showGradientColorPicker2 by remember { mutableStateOf(false) }
    var showGradientColorPicker3 by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    var selectedImageUri by remember { mutableStateOf(preferencesManager.getBackgroundImagePath()) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val inputStream: InputStream? = uri?.let { context.contentResolver.openInputStream(it) }
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val imagePath = saveImageToInternalStorage(context,bitmap)
        themeViewModel.updateBackgroundImage(bitmap)
        imagePath?.let { preferencesManager.saveBackgroundImagePath(it) }
        selectedImageUri = uri.toString()
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Theme", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.padding(bottom = 10.dp)
            )

        },
//        modifier = Modifier.background(backgroundGradient),
        containerColor = Color.Transparent
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp)) {

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "System Theme Color",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Color(${argbToColorHexString(accentColor.toArgb())})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isSystemTheme,
                        onCheckedChange = { isSystemTheme = it },
                        modifier = Modifier.padding(start = 16.dp),
                        colors = SwitchDefaults.colors(
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray,
                            checkedThumbColor = Color.LightGray,
                            checkedBorderColor = Color(preferencesManager.getAccentColor()),
                            checkedTrackColor = Color(preferencesManager.getAccentColor())
                        )
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Accent Color",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Color(${argbToColorHexString(accentColor.toArgb())})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .drawBehind {
                                val radius = size.minDimension / 2.2f
                                drawCircle(
                                    color = accentColor,
                                    radius = radius,
                                    center = center
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.3f),
                                    radius = radius * 1.1f,
                                    center = center,
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                            .clickable { showAccentColorPicker = true }
                    ) {
                        if (showAccentColorPicker) {
                            FullScreenColorPickerDialog(
                                selectedColor = accentColor,
                                onColorSelected = {
                                    accentColor = it
                                    preferencesManager.saveAccentColor(accentColor.toArgb())
                                },
                                onDismissRequest = { showAccentColorPicker = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Background Gradient",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Used as Background",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .drawBehind {
                                    val radius = size.minDimension / 2.2f
                                    drawCircle(
                                        brush = backgroundGradient,
                                        radius = radius,
                                        center = center
                                    )
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.3f),
                                        radius = radius * 1.1f,
                                        center = center,
                                        style = Stroke(width = 3.dp.toPx())
                                    )
                                }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    expanded = !expanded
                                }
                        )
                    }
                    AnimatedVisibility(
                        visible = expanded,
                        enter = slideInVertically {
                            with(density) { -40.dp.roundToPx() }
                        } + expandVertically (
                            expandFrom = Alignment.Top
                        ) + fadeIn(
                            initialAlpha = 0.3f
                        ),
                        exit = slideOutVertically() + shrinkVertically() + fadeOut()
                    ) {
                        val defaultColors =
                            listOf(Color.Black, Color.White, Color.Gray) // Default colors
                        val colors = remember { mutableStateListOf(*fetchedColors.toTypedArray()) }

                        while (colors.size < 3) {
                            colors.add(defaultColors[colors.size % defaultColors.size])
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)){
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .drawBehind {
                                        val radius = size.minDimension / 2.2f
                                        drawCircle(
                                            color = colors[0],
                                            radius = radius,
                                            center = center
                                        )
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.1f),
                                            radius = radius * 1.1f,
                                            center = center,
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                    }
                                    .clickable { showGradientColorPicker1 = true }
                            ) {
                                if (showGradientColorPicker1) {
                                    FullScreenColorPickerDialog(
                                        selectedColor = colors[0],
                                        onColorSelected = {
                                            colors[0] = it
                                            linearColors = colors.joinToString(",") { color ->
                                                color.toArgb().toString()
                                            }
                                            preferencesManager.saveLinearGradientBrush(
                                                colors,
                                                Offset.Zero,
                                                Offset.Infinite
                                            )
                                            val brush = preferencesManager.getLinearGradientBrush()
                                            themeViewModel.updateBackgroundColors(colors)
                                            themeViewModel.updateGradient(brush)
                                        },
                                        onDismissRequest = { showGradientColorPicker1 = false }
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .drawBehind {
                                        val radius = size.minDimension / 2.2f
                                        drawCircle(
                                            color = colors[1],
                                            radius = radius,
                                            center = center
                                        )
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.1f),
                                            radius = radius * 1.1f,
                                            center = center,
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                    }
                                    .clickable { showGradientColorPicker2 = true }
                            ) {
                                if (showGradientColorPicker2) {
                                    FullScreenColorPickerDialog(
                                        selectedColor = colors[1],
                                        onColorSelected = {
                                            colors[1] = it
                                            linearColors = colors.joinToString(",") { color ->
                                                color.toArgb().toString()
                                            }
//                                        preferencesManager.saveLinearColors(linearColors)
                                            preferencesManager.saveLinearGradientBrush(
                                                colors,
                                                Offset.Zero,
                                                Offset.Infinite
                                            )
                                            val brush = preferencesManager.getLinearGradientBrush()
                                            themeViewModel.updateBackgroundColors(colors)
                                            themeViewModel.updateGradient(brush)
                                        },
                                        onDismissRequest = { showGradientColorPicker2 = false }
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .drawBehind {
                                        val radius = size.minDimension / 2.2f
                                        drawCircle(
                                            color = colors[2],
                                            radius = radius,
                                            center = center
                                        )
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.1f),
                                            radius = radius * 1.1f,
                                            center = center,
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                    }
                                    .clickable { showGradientColorPicker3 = true }
                            ) {
                                if (showGradientColorPicker3) {
                                    FullScreenColorPickerDialog(
                                        selectedColor = colors[2],
                                        onColorSelected = {
                                            colors[2] = it
                                            linearColors = colors.joinToString(",") { color ->
                                                color.toArgb().toString()
                                            }
//                                        preferencesManager.saveLinearColors(linearColors)
                                            preferencesManager.saveLinearGradientBrush(
                                                colors,
                                                Offset.Zero,
                                                Offset.Infinite
                                            )
                                            val brush = preferencesManager.getLinearGradientBrush()
                                            themeViewModel.updateBackgroundColors(colors)
                                            themeViewModel.updateGradient(brush)
                                        },
                                        onDismissRequest = { showGradientColorPicker3 = false }
                                    )
                                }
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.height(20.dp))

//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically
//                ){
//                    Column(modifier = Modifier.weight(1f)) {
//                        Text(
//                            "Set Custom Background Image",
//                            style = MaterialTheme.typography.titleMedium,
//                            color = MaterialTheme.colorScheme.onPrimary
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            text = "Let Pick You Own Epic Background",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                    Switch(
//                        checked = isImageAsBackGround,
//                        onCheckedChange = {
//                            isImageAsBackGround = it
//                            preferencesManager.setImageAsBackGround(it)
//                        },
//                        modifier = Modifier.padding(start = 16.dp),
//                        colors = SwitchDefaults.colors(
//                            uncheckedThumbColor = Color.White,
//                            uncheckedTrackColor = Color.LightGray,
//                            checkedThumbColor = Color.LightGray,
//                            checkedBorderColor = Color(preferencesManager.getAccentColor()),
//                            checkedTrackColor = Color(preferencesManager.getAccentColor())
//                        )
//                    )
//                }
//
//                AnimatedVisibility(
//                    visible = isImageAsBackGround,
//                    enter = slideInVertically {
//                        with(density) { -40.dp.roundToPx() }
//                    } + expandVertically (
//                        expandFrom = Alignment.Top
//                    ) + fadeIn(
//                        initialAlpha = 0.3f
//                    ),
//                    exit = slideOutVertically() + shrinkVertically() + fadeOut()
//                ) {
//
//
//                    Column(modifier = Modifier.fillMaxWidth(), ){
//
//                        Box(
//                            modifier = Modifier
//                                .size(30.dp)
//                                .clickable {imagePickerLauncher.launch("image/*")}
//                        ) {
//                            Text("Pick an Image", color = Color.White)
//                        }
//                        if(selectedImageUri != "" ){
//                            Image(
//                                bitmap = backgroundBitmap.asImageBitmap(),
//                                contentDescription = null,
//                                modifier = Modifier.size(150.dp),
//                                contentScale = ContentScale.Crop
//                            )
//
//                        }
//                    }
//                }

                Spacer(modifier = Modifier.height(20.dp))


            }
        }
    }
}


fun argbToColorHexString(argb: Int): String {
    // Extract the alpha, red, green, and blue components
    val alpha = (argb shr 24) and 0xFF
    val red = (argb shr 16) and 0xFF
    val green = (argb shr 8) and 0xFF
    val blue = argb and 0xFF

    // Format the components as a hexadecimal string
    return "0x%02X%02X%02X%02X".format(alpha, red, green, blue)
}


@Preview
@Composable
fun ThemeSettingsScreenPreview() {
    val preferencesManager = PreferencesManager(LocalContext.current)
    val blackToGrayGradient =
        Brush.linearGradient(
            colors = listOf(MaterialTheme.colorScheme.primaryContainer,Color.Black,Color.Black,Color.Black,),
            start = Offset.Zero
        )
//    ThemeSettingsScreen( ThemeViewModel(LocalContext.current),preferencesManager,{})
}

