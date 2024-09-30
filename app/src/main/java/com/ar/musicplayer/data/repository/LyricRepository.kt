package com.ar.musicplayer.data.repository

import android.util.Log
import com.ar.musicplayer.api.ApiService
import com.ar.musicplayer.api.LyricsByLrclib
import com.ar.musicplayer.api.Translate
import com.ar.musicplayer.data.models.LyricsResponse
import com.ar.musicplayer.data.models.TranslationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.math.absoluteValue

class LyricRepository @Inject constructor(
    private val apiService: LyricsByLrclib,
    private val translationService: Translate
) {

    fun fetchLyrics(
        artistList: List<String>,
        trackName: String,
        albumName: String,
        duration: Int,
        onSuccess: (List<Pair<Int, String>>) -> Unit,
        onError: (Exception) -> Unit
    ) {

        var count = 0

        fun tryFetch() {
            if (count >= artistList.size) {
                onError(Exception("No matching lyrics found for any artist."))
                return
            }

            lyricsFinder(
                artistList = artistList,
                artistName = artistList[count],
                trackName = trackName,
                albumName = albumName,
                duration = duration,
                onSuccess = onSuccess,
                onError = onError,
                onTryAgain = {
                    count++
                    tryFetch()
                }
            )
        }

        tryFetch()
    }

    private fun lyricsFinder(
        artistList: List<String>,
        artistName: String,
        trackName: String,
        albumName: String,
        duration: Int,
        onSuccess: (List<Pair<Int, String>>) -> Unit,
        onError: (Exception) -> Unit,
        onTryAgain: () -> Unit
    ) {

        apiService.getLyricsLrclib(trackName, artistName).enqueue(object :
            Callback<List<LyricsResponse>> {
            override fun onResponse(call: Call<List<LyricsResponse>>, response: Response<List<LyricsResponse>>) {
                if (response.isSuccessful) {
                    val results = response.body()?.filter { it.syncedLyrics != "null" }
                    val matchedResult = results?.minByOrNull { result ->
                        var score = 0
                        if (!artistList.contains(result.artistName)) score++
                        if (result.albumName != albumName) score++
                        if (result.duration?.toInt() == duration) {
                            score += (duration - result.duration.toInt()).absoluteValue
                        }
                        score
                    }
                    if (matchedResult != null) {
                        fetchTranslation(
                            matchedResult.syncedLyrics.toString(),
                            onSuccess = onSuccess,
                            onError = onError
                        )
                    } else {
                        onTryAgain()
                    }

                }
            }


            override fun onFailure(call: Call<List<LyricsResponse>>, t: Throwable) {
                onError((t.cause ?: Exception("Unknown error")) as Exception)
            }
        })
    }

    fun fetchTranslation(
        text: String,
        onSuccess: (List<Pair<Int, String>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val request = TransliterateRequest(
            text = text.replace("\"", "")
        )
        val call = translationService.postTranslatedLyrics(request)
        call.enqueue(object : Callback<TranslationResponse> {
            override fun onResponse(call: Call<TranslationResponse>, response: Response<TranslationResponse>) {
                if (response.isSuccessful) {
                    val translatedText = response.body()?.text
                    onSuccess(parseLrc(translatedText.toString()))
                    println("Translated Text: $translatedText")
                } else {
                    onSuccess(parseLrc(text))
                }
            }

            override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {
                onSuccess(parseLrc(text))
                onError((t.cause ?: Exception("Unknown error")) as Exception)
            }
        })
    }

    private fun parseLrc(syncedLyrics: String): List<Pair<Int, String>> {
        val lrcLines = mutableListOf<Pair<Int, String>>()
        val pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2})](.*)")
        syncedLyrics.lines().forEach { line ->
            val matcher = pattern.matcher(line)
            if (matcher.matches()) {
                val minutes = matcher.group(1).toInt()
                val seconds = matcher.group(2).toInt()
                val centiseconds = matcher.group(3).toInt()
                val text = matcher.group(4)
                val timeInMs = (minutes * 60 + seconds) * 1000 + centiseconds * 10
                lrcLines.add(Pair(timeInMs, text))
            }
        }
        return lrcLines
    }
}


data class TransliterateRequest(
    val text: String
)
