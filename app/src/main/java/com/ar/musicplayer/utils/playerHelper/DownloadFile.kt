package com.ar.musicplayer.utils.playerHelper

import android.content.Context
import android.os.Environment
import androidx.media3.decoder.Buffer
import com.ar.musicplayer.models.SongResponse
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
    songResponse: SongResponse,
    onProgress: (Int) -> Unit,
    onComplete: (Boolean) -> Unit
) {
    val mp4Url = decodeDES(songResponse.moreInfo?.encryptedMediaUrl.toString())
    val title = songResponse.title
    val artist = songResponse.moreInfo?.artistMap?.artists?.get(0)?.name ?: ""
    val artist2 = songResponse.moreInfo?.artistMap?.artists?.get(1)?.name ?: ""
    val finalArtist = if (artist.isEmpty()) songResponse.moreInfo?.artistMap?.primaryArtists?.get(0)?.name ?: "" else artist
    val album = songResponse.moreInfo?.album ?: ""
    val genre = songResponse.role ?: ""
    val imageFile = songResponse.image ?: ""

    // Directory to store music files
    val musicFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath

    // Generate a unique temporary filename for conversion
    val tempFileName = "${title} - ${finalArtist}_temp.mp3"

    // Full path to the temporary MP3 file
    val tempFilePath = "$musicFolderPath/$tempFileName"



    val downloadPath = File(context.filesDir, "downloaded.mp4").absolutePath
    val mp3Path = tempFilePath

    val mp4File = File(downloadPath)

    downloadFile(mp4Url, mp4File,
        onProgress = { progress ->
            // Update your progress bar or display the progress here
            println("Download progress: $progress%")
            onProgress(progress)
        },
        onComplete = { success ->
            if(success){
                convertMp4ToMp3(mp4File.absolutePath, mp3Path) { conversionSuccess ->
                    if (conversionSuccess) {
                        // Step 3: Add Metadata to MP3
                        addMetadataToMp3(tempFilePath, title.toString(), artist.toString(), album.toString(),genre.toString(),imageFile.toString())
                    }
                }
            }
        }
    )
}

fun convertMp4ToMp3(inputPath: String, outputPath: String, onComplete: (Boolean) -> Unit) {

    val command = "-i \"$inputPath\" -q:a 0 -map a \"$outputPath\""

    FFmpegKit.executeAsync(command) { session ->
        val returnCode = session.returnCode
        onComplete(ReturnCode.isSuccess(returnCode))

    }
}

fun addMetadataToMp3(tempFilePath: String, title: String, artist: String, album: String, genre: String, imageUrl: String?) {
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

        val finalFilePath  = tempFilePath.replace("_temp","")

        // Save the MP3 file with updated metadata to the final file location
        mp3File.save(finalFilePath)

        // Delete the temporary file
        File(tempFilePath).delete()

        println("Metadata added successfully to MP3: $finalFilePath")

    } catch (e: Exception) {
        // Print stack trace if an error occurs during metadata addition
        e.printStackTrace()
    }
}




private fun decodeDES(input: String): String {
    val key = "38346591"
    val algorithm = "DES/ECB/PKCS5Padding"

    val keyFactory = SecretKeyFactory.getInstance("DES")
    val desKeySpec = DESKeySpec(key.toByteArray(StandardCharsets.UTF_8))
    val secretKey = keyFactory.generateSecret(desKeySpec)

    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.DECRYPT_MODE, secretKey)

    val encryptedBytes = Base64.getDecoder().decode(input)
    val decryptedBytes = cipher.doFinal(encryptedBytes)
    var decoded = String(decryptedBytes, StandardCharsets.UTF_8)

    // Replace ".mp4" pattern
    val pattern = Pattern.compile("\\.mp4.*")
    val matcher = pattern.matcher(decoded)
    decoded = matcher.replaceAll(".mp4")

    // Replace "http:" with "https:"
    decoded = decoded.replace("http:", "https:")

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





