//
//import io.ktor.client.HttpClient
//import io.ktor.client.engine.cio.CIO
//import io.ktor.client.plugins.HttpTimeout
//import io.ktor.client.request.forms.formData
//import io.ktor.client.request.forms.submitFormWithBinaryData
//import io.ktor.client.statement.bodyAsText
//import io.ktor.http.ContentType.MultiPart.FormData
//import io.ktor.http.Headers
//import io.ktor.http.HttpHeaders
//import io.ktor.http.append
//import java.io.File
//
//
//
//suspend fun recognizeAudio(file: File, startTime: Int) {
//    // Initialize the Ktor client
//    val client = HttpClient(CIO) {
//        install(HttpTimeout){
//            requestTimeoutMillis = 20000
//            socketTimeoutMillis = 20000
//            connectTimeoutMillis = 20000
//        }
//    }
//    try {
//        // Make the POST request
//        val response = client.submitFormWithBinaryData(
//            url = "https://musikerkennung.com/recognize-audio",
//            formData = formData {
//                append("startTime", startTime.toString())
//
//                // Add the file as a binary form part
//                append("videoFile", file.readBytes(), Headers.build {
//                    append(HttpHeaders.ContentType, FormData)
//                    append(
//                        HttpHeaders.ContentDisposition,
//                        "filename=${file.name}"
//                    )
//                })
//            }
//        )
//        // Parse the response as JSON
//        val responseBody = response.bodyAsText()
//
//        // Example of handling the response:
//        if (responseBody.contains("track")) {
//            // Handle the response that contains track information
//            println("Track recognized successfully: $responseBody")
//        } else {
//            println("No track found. $responseBody")
//        }
//    } catch (e: Exception) {
//        // Handle errors
//        println("Error during HTTP request: ${e.localizedMessage}")
//    } finally {
//        // Close the client
//        client.close()
//    }
//}
//
