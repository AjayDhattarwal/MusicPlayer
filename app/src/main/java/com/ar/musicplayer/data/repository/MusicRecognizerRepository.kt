@file:OptIn(InternalAPI::class)

package com.ar.musicplayer.data.repository

import com.ar.musicplayer.data.models.RelatedTracks
import com.ar.musicplayer.data.models.TrackResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.MultiPart.FormData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.append
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class MusicRecognizerRepository {



    suspend fun recognizeSong(file: File, startTime: Int): TrackResponse? {

        val client = HttpClient(Android) {
            install(HttpTimeout){
                requestTimeoutMillis = 20000
                socketTimeoutMillis = 20000
                connectTimeoutMillis = 20000
            }
            followRedirects = true
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                    }
                )
            }

        }

        if(!file.exists()){
            println("file not exist ")
        }else{
            println(file.name)
        }

        return withContext(Dispatchers.IO){
            return@withContext try {
                val response = client.submitFormWithBinaryData(
                    url = "https://musikerkennung.com/recognize-audio",
                    formData = formData {
                        append("startTime", startTime.toString())

                        append("videoFile", file.readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, FormData)
                            append(
                                HttpHeaders.ContentDisposition,
                                "filename=${file.name}"
                            )
                        })
                    }
                )


                println("response : ${response.bodyAsText()}")

                response.body<TrackResponse>()


            } catch (e: Exception) {
                println("Error during HTTP request: ${e.localizedMessage}")
                null
            }
        }
    }

    suspend fun fetchRelatedSongs(url: String): RelatedTracks? {
        val client = HttpClient(Android) {

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }

            // Content negotiation to handle JSON
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                    }
                )
            }

        }
        println(url)
        return withContext(Dispatchers.IO){
            try {
                client.get(url).body<RelatedTracks>()
            } catch (e: Exception){
                println("Error during HTTP request: ${e.localizedMessage}")
                null
            }
        }
    }

}
