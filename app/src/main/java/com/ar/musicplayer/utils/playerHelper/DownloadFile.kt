package com.ar.musicplayer.utils.playerHelper

import android.content.Context
import android.os.Environment
import com.ar.musicplayer.models.SongResponse
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.mpatric.mp3agic.ID3v2
import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

fun downloadFile(url: String, outputFile: File, onComplete: (Boolean) -> Unit) {
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            onComplete(false)
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
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


fun handleMp4ToMp3Conversion(
    context: Context,
    songResponse: SongResponse
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

    // Generate the final output filename based on title and artist
    val outputFileName = "${title} - ${finalArtist}.mp3"

    // Full path to the final output MP3 file
    val outputFilePath = "$musicFolderPath/$outputFileName"

    // Step 1: Convert MP4 to MP3
    convertMp4ToMp3(mp4Url, tempFilePath) { conversionSuccess ->
        if (conversionSuccess) {
            // Step 2: Add Metadata to MP3 and move to final location
            addMetadataToMp3(tempFilePath, outputFilePath, title.toString(), finalArtist, album, genre, imageFile)
        } else {
            // Handle conversion failure
            println("Failed to convert MP4 to MP3.")
        }
    }
}

fun convertMp4ToMp3(inputPath: String, outputPath: String, onComplete: (Boolean) -> Unit) {
    val command = "-i \"$inputPath\" -q:a 0 -map a \"$outputPath\""
    FFmpegKit.executeAsync(command) { session ->
        val returnCode = session.returnCode
        onComplete(ReturnCode.isSuccess(returnCode))
    }
}

fun addMetadataToMp3(tempFilePath: String, finalFilePath: String, title: String, artist: String, album: String, genre: String, imageUrl: String?) {
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