package com.ar.musicplayer.utils.download

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.utils.PreferencesManager
import com.ar.musicplayer.utils.roomdatabase.dbmodels.SongDownloadEntity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import okhttp3.Response
import okhttp3.ResponseBody
import okio.*

fun handleMp4ToMp3Conversion(
    context: Context,
    entity: SongDownloadEntity,
    downloadQuality: String,
    downloadPath: String,
    onProgress: (Int) -> Unit,
    onComplete: (Boolean) -> Unit
) {

    val mp4Url = decodeDES(entity.url,entity.is320kbps, downloadQuality = downloadQuality)

    val tempFileName = "${entity.title} - ${entity.artist}_temp.mp3"
    val tempFilePath = File(context.cacheDir, tempFileName).absolutePath
    val mp3Path = tempFilePath

    val tempMp4 = File(context.filesDir, "downloaded.mp4").absolutePath
    val mp4File = File(tempMp4)

    downloadFile(mp4Url, mp4File,
        onProgress = { progress ->
            onProgress(progress)
        },
        onComplete = { success ->
            if(success){
                convertMp4ToMp3(mp4File.absolutePath, mp3Path) { conversionSuccess ->
                    if (conversionSuccess) {
                        addMetadataToMp3(
                            tempFilePath,
                            entity.title,
                            entity.artist,
                            entity.album,
                            entity.genre,
                            entity.imageUrl,
                            downloadPath,
                            onComplete = onComplete
                        )
                    }
                }
            }
        }
    )
}

fun convertMp4ToMp3(inputPath: String, outputPath: String, onComplete: (Boolean) -> Unit) {
    val outputFile = File(outputPath)

    // Check if the output file exists and delete it if necessary
    if (outputFile.exists()) {
        if (!outputFile.delete()) {
            Log.e("FileError", "Failed to delete existing file: $outputPath")
            onComplete(false)
            return
        }
    }

    // FFmpeg command for converting MP4 to MP3
    val command = "-i \"$inputPath\" -q:a 0 -map a \"$outputPath\""

    // Execute FFmpeg command
    FFmpegKit.executeAsync(command) { session ->
        val returnCode = session.returnCode
        val logs = session.allLogsAsString
        if (ReturnCode.isSuccess(returnCode)) {
            Log.i("FFmpegKit", "Conversion successful. Output file: $outputPath")
            onComplete(true)
        } else {
            Log.e("FFmpegKit", "Command failed with state ${session.state} and returnCode $returnCode. Logs: $logs")
            onComplete(false)
        }
    }
}

fun addMetadataToMp3(
    tempFilePath: String,
    title: String,
    artist: String,
    album: String,
    genre: String,
    imageUrl: String?,
    savePath: String,
    onComplete: (Boolean) -> Unit
) {
    try {
        println("Adding metadata to MP3: $tempFilePath")

        // Load the existing MP3 file
        val mp3File = Mp3File(tempFilePath)

        // Initialize or get existing ID3v2 tag
        val id3v2Tag = if (mp3File.hasId3v2Tag()) {
            mp3File.id3v2Tag
        } else {
            ID3v24Tag()
        }

        // Set basic metadata
        id3v2Tag.title = title
        id3v2Tag.artist = artist
        id3v2Tag.album = album
        id3v2Tag.genreDescription = genre

        // Embed image if provided via URL
        imageUrl?.let {
            try {
                val imageBytes = URL(imageUrl).openStream().readBytes()
                id3v2Tag.setAlbumImage(imageBytes, "image/jpeg") // Assuming JPEG format, adjust as needed
            } catch (e: Exception) {
                println("Failed to fetch or embed image from URL: $imageUrl")
                e.printStackTrace()
            }
        }

        // Set the ID3v2 tag back to the MP3 file
        mp3File.id3v2Tag = id3v2Tag

        val newPath = getFilePath(title, artist, savePath)
        val newFile = File(newPath)
        val parentDir = newFile.parentFile

        if (parentDir != null && !parentDir.exists()) {
            try {
                parentDir.mkdirs()
            } catch (e: IOException) {
                Log.e("FileError", "Failed to create directory: ${parentDir.path}", e)
                return
            }
        }


        mp3File.save(newPath)

        File(tempFilePath).delete()

        println("Metadata added successfully to MP3: $savePath")

        onComplete(true)

    } catch (e: Exception) {
        e.printStackTrace()
        onComplete(false)
    }
}




private fun decodeDES(input: String, kbps320: Boolean,downloadQuality: String): String {
    Log.d("input","$input")
    val key = "38346591"
    val algorithm = "DES/ECB/PKCS5Padding"

    val keyFactory = SecretKeyFactory.getInstance("DES")
    val desKeySpec = DESKeySpec(key.toByteArray(StandardCharsets.UTF_8))
    val secretKey = keyFactory.generateSecret(desKeySpec)

    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.DECRYPT_MODE, secretKey)

    val encryptedBytes = Base64.getDecoder().decode(input.replace("\\",""))
    val decryptedBytes = cipher.doFinal(encryptedBytes)
    var decoded = String(decryptedBytes, StandardCharsets.UTF_8)


    val pattern = Pattern.compile("\\.mp4.*")
    val matcher = pattern.matcher(decoded)
    decoded = matcher.replaceAll(".mp4")

    // Replace "http:" with "https:"
    decoded = decoded.replace("http:", "https:")
    if(downloadQuality == "320"){
        if(kbps320){
            decoded = decoded.replace("96.mp4", "${downloadQuality}.mp4")
            Log.d("320", "its 320 decoded: $decoded")
        }
    } else{
        decoded = decoded.replace("96.mp4","${downloadQuality}.mp4")
        Log.d("320", " not 320 decoded: $decoded")
    }
    return decoded
}


fun downloadFile(url: String, outputFile: File, onProgress: (Int) -> Unit, onComplete: (Boolean) -> Unit) {

    val client = OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            originalResponse.newBuilder()
                .body(ProgressResponseBody(originalResponse.body!!, onProgress))
                .build()
        }
        .build()

    val request = Request.Builder().url(url).build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            e.printStackTrace()
            onComplete(false)
        }

        override fun onResponse(call: okhttp3.Call, response: Response) {
            if (response.isSuccessful) {
                response.body?.let { body ->
                    val inputStream = body.byteStream()
                    val outputStream = FileOutputStream(outputFile)
                    inputStream.copyTo(outputStream)
                    outputStream.close()
                    onComplete(true)
                } ?: onComplete(false)
            } else {
                onComplete(false)
            }
        }
    })
}

private fun getFilePath(title: String, artist: String, downloadPath: String): String {
    val musicFolderPath = downloadPath
    return "$musicFolderPath/${title} - ${artist}.mp3"
}

private class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val progressListener: (Int) -> Unit
) : ResponseBody() {

    private var bufferedSource: BufferedSource? = null

    override fun contentType(): okhttp3.MediaType? {
        return responseBody.contentType()
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = source(responseBody.source()).buffer()
        }
        return bufferedSource!!
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            override fun read(sink: okio.Buffer, byteCount: Long): Long {

                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0

                val progress = (100 * totalBytesRead / responseBody.contentLength()).toInt()
                progressListener(progress)
                return bytesRead
            }
        }
    }
}





