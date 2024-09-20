package com.ar.musicplayer.api

import com.ar.musicplayer.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Base64

object SpotifyAuth {
    private const val CLIENT_ID = BuildConfig.CLIENT_ID
    private const val CLIENT_SECRET = BuildConfig.CLIENT_SECRET
    private const val TOKEN_URL = "https://accounts.spotify.com/api/token"

    private val client = OkHttpClient.Builder()
        .addInterceptor(RateLimitInterceptor())
        .build()

    suspend fun getAccessToken(): String {
        return withContext(Dispatchers.IO) {
            val authHeader = "Basic " + Base64.getEncoder().encodeToString("$CLIENT_ID:$CLIENT_SECRET".toByteArray())
            val formBody = FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build()

            val request = Request.Builder()
                .url(TOKEN_URL)
                .post(formBody)
                .addHeader("Authorization", authHeader)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()

            val response: Response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: throw IOException("Failed to get access token: empty response")
                val json = JSONObject(responseBody)
                json.getString("access_token")
            } else {
                throw IOException("Failed to get access token: ${response.message}")
            }
        }
    }
}
