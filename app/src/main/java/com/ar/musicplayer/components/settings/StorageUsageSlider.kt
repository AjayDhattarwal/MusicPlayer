package com.ar.musicplayer.components.settings

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

data class StorageSegment(
    val label: String,
    val sizeInBytes: Long,
    val color: Color
)

@Composable
fun StorageUsageSlider(
    segments: List<StorageSegment>,
    totalStorage: Long, // Total storage in bytes
    minSegmentWidth: Float = 8f
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Storage Usage",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Draw the segmented slider
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color.Gray.copy(alpha = 0.2f), MaterialTheme.shapes.small)
                .clip(RoundedCornerShape(50))
        ) {
            val canvasWidth = size.width
            var start = 0f
            segments.forEach { segment ->
                val percentage = segment.sizeInBytes.toFloat() / totalStorage
                val segmentWidth = max(canvasWidth * percentage, minSegmentWidth)

                drawRoundRect(
                    color = segment.color,
                    topLeft = Offset(start, 0f),
                    size = Size(segmentWidth, size.height),
                )
                start += segmentWidth
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display segment labels with sizes
        segments.forEach { segment ->
            Row(verticalAlignment = Alignment.CenterVertically){
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .drawBehind {
                            drawCircle(
                                color = segment.color,
                            )
                        }

                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = segment.label,
                        color =  MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = formatSize(segment.sizeInBytes),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

fun formatSize(sizeInBytes: Long): String {
    val sizeInGB = sizeInBytes / (1024.0 * 1024.0 * 1024.0)
    return if (sizeInGB >= 1) {
        "${(sizeInGB * 100).roundToInt() / 100.0} GB"
    } else {
        val sizeInMB = sizeInBytes / (1024.0 * 1024.0)
        "${(sizeInMB * 100).roundToInt() / 100.0} MB"
    }
}

@Composable
@Preview
fun StorageUsageSliderPreview() {
    val segments = listOf(
        StorageSegment("Other Apps", 35L * 1024 * 1024 * 1024, Color.Blue.copy(0.5f)),
        StorageSegment("Cache", 15L * 1024 * 1024, Color.Red.copy(0.5f)),
        StorageSegment("Music Folder", 3000L * 1024 * 1024, Color.Green.copy(0.4f)),
        StorageSegment("Free Space", 20L * 1024 * 1024 * 1024, Color.Gray)
    )
    val totalStorage = 50L * 1024 * 1024 * 1024

    StorageUsageSlider(segments = segments, totalStorage = totalStorage)
}


fun getTotalStorage(): Long {
    val stat = StatFs(Environment.getDataDirectory().path)
    return stat.blockSizeLong * stat.blockCountLong
}

fun getFreeSpace(): Long {
    val stat = StatFs(Environment.getDataDirectory().path)
    return stat.blockSizeLong * stat.availableBlocksLong
}

fun getCacheSize(context: Context): Long {
    val cacheDir = context.cacheDir
    return getFolderSize(cacheDir)
}

fun getFolderSize(directory: File): Long {
    var size: Long = 0
    directory.listFiles()?.forEach { file ->
        size += if (file.isDirectory) getFolderSize(file) else file.length()
    }
    return size
}

fun getMusicFolderSize(musicDir: File): Long {
    return getFolderSize(musicDir)
}
