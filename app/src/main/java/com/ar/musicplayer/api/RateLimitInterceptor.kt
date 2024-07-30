package com.ar.musicplayer.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class RateLimitInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response = chain.proceed(request)

        if (response.code == 429) {
            val retryAfter = response.header("retry-after")?.toLongOrNull() ?: 0L
            if (retryAfter > 0) {
                try {
                    TimeUnit.SECONDS.sleep(retryAfter)
                } catch (e: InterruptedException) {
                    throw IOException("Rate limit wait interrupted", e)
                }
                response.close()
                response = chain.proceed(request) // Retry the request
            }
        }
        return response
    }
}
