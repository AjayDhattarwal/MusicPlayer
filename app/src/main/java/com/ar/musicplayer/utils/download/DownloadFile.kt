package com.ar.musicplayer.utils.download

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import com.ar.musicplayer.data.models.sanitizeFileName
import com.ar.musicplayer.data.models.sanitizeString
import com.ar.musicplayer.utils.roomdatabase.dbmodels.SongDownloadEntity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import okhttp3.Response
import okhttp3.ResponseBody
import okio.*
import java.net.URL
import java.nio.ByteBuffer
import kotlin.text.toByteArray

fun handleMp4ToMp3Conversion(
    context: Context,
    entity: SongDownloadEntity,
    downloadQuality: String,
    downloadPath: String,
    onProgress: (Int) -> Unit,
    onComplete: (Boolean) -> Unit
) {

    val mp4Url = decodeDES(entity.url,entity.is320kbps, downloadQuality = downloadQuality)

    val tempFileName = "temp.aac"
    val mp3Path = File(context.cacheDir, tempFileName).absolutePath

    val tempMp4 = File(context.filesDir, "downloaded.mp4").absolutePath
    val mp4File = File(tempMp4)

    val trackMetaData = TrackMetaData(
        id = entity.id,
        tempFilePath = mp3Path,
        title = entity.title.sanitizeString().sanitizeFileName(),
        artist = entity.artist.sanitizeString().sanitizeFileName(),
        album = entity.album.sanitizeString().sanitizeFileName(),
        genre = entity.genre,
        imageUrl = entity.imageUrl,
        savePath = downloadPath

    )

    downloadFile(mp4Url, mp4File,
        onProgress = { progress ->
            onProgress(progress)
        },
        onComplete = { success ->
            if(success){
                convertMp4ToAAC(mp4File.absolutePath, mp3Path) { conversionSuccess ->
                    if (conversionSuccess) {
                        addMetadataToAAC(
                            trackMetaData = trackMetaData,
                            context = context,
                            onComplete = onComplete
                        )
                    }
                }
            }
        }
    )
}

@SuppressLint("WrongConstant")
fun convertMp4ToAAC(inputFilePath: String, outputFilePath: String, onComplete: (Boolean) -> Unit) {
    val extractor = MediaExtractor()
    val muxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

    try {
        extractor.setDataSource(inputFilePath)
        val trackCount = extractor.trackCount

        for (i in 0 until trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)


            if (mime?.startsWith("audio/") == true) {
                extractor.selectTrack(i)

                val trackIndex = muxer.addTrack(format)
                muxer.start()

                val buffer = ByteBuffer.allocate(1024 * 1024)
                while (true) {
                    val bufferInfo = MediaCodec.BufferInfo()
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) {
                        break
                    }

                    bufferInfo.offset = 0
                    bufferInfo.size = sampleSize
                    bufferInfo.presentationTimeUs = extractor.sampleTime
                    bufferInfo.flags = extractor.sampleFlags

                    muxer.writeSampleData(trackIndex, buffer, bufferInfo)
                    extractor.advance()
                }
                break
            }
        }

        muxer.stop()
        muxer.release()
        extractor.release()
        Log.d("download", "completed ")
        onComplete(true)
    } catch (e: Exception) {

        Log.e("download", "error : $e")
        e.printStackTrace()
        onComplete(false) // Extraction failed
    }
}



fun addMetadataToAAC(
    trackMetaData: TrackMetaData,
    context: Context,
    onComplete: (Boolean) -> Unit
) {
    try {

        val imageBytes = URL(trackMetaData.imageUrl).openStream().readBytes()
        val coverImageFile = File(context.cacheDir, "temp.jpg")

        if(saveImageBytesToFile(imageBytes, coverImageFile)){

            val newPath = getFilePath(trackMetaData.title, trackMetaData.artist, trackMetaData.savePath)


            val command = arrayOf(
                "-i", trackMetaData.tempFilePath,
                "-i", coverImageFile.absolutePath,
                "-map", "0:a",
                "-map", "1:v",
                "-c:a", "copy",
                "-c:v", "mjpeg",
                "-disposition:v:0", "attached_pic",
                "-metadata", "artist=${trackMetaData.artist}",
                "-metadata", "title=${trackMetaData.title}",
                "-metadata", "album=${trackMetaData.album}",
                "-metadata", "genre=${trackMetaData.genre}",
                "-metadata", "comment=${trackMetaData.id}=song_id",
                "-b:a", "320k",
                "-id3v2_version", "3",
                "-f", "ipod",
                newPath
            )






            FFmpegKit.executeAsync(command.joinToString(" ")) { session ->
                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)) {

                    println("Metadata added successfully to MP3: ${trackMetaData.savePath}")
                    coverImageFile.delete()

                    File(trackMetaData.tempFilePath).delete()

                    onComplete(true)
                } else {
                    Log.e("download", "Failed to add metadata: $returnCode")
                }
            }
        }

    } catch (e: Exception) {
        e.printStackTrace()
        onComplete(false)
    }
}


fun saveImageBytesToFile(imageBytes: ByteArray, tempFile: File): Boolean {
    return try {
        val outputStream = FileOutputStream(tempFile)
        outputStream.write(imageBytes)
        outputStream.flush()
        outputStream.close()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}



@SuppressLint("GetInstance")
private fun decodeDES(input: String, kbps320: Boolean, downloadQuality: String): String {
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

    val filePath = "$downloadPath/${title}_by_$artist.m4a"

    // Log the file path for debugging purposes
    println("Generated file path: $filePath")

    val file = File(filePath)

    // Check if the file path exists (for debugging purposes)
    if (!file.exists()) {
        println("File does not exist: $filePath")
    } else {
        println("File exists: $filePath")
    }

    return filePath
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



data class TrackMetaData(
    val id: String,
    val tempFilePath: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val imageUrl: String?,
    val savePath: String,
)


