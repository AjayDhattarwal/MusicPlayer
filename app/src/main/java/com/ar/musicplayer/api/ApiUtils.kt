package com.ar.musicplayer.api

fun createApiUrl(url: String, type: String, n: Int, p: Int, __call: String): String {
    val subString = "/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0&"

    // Extract token from the provided URL
    val token = url.substringAfterLast('/')

    // Combine base URL with dynamic parameters
    val apiUrl = subString +
            "token=$token" +
            "&type=$type" +
            "&n=$n" +
            "&p=$p" +
            "&__call=$__call"

    return apiUrl
}
