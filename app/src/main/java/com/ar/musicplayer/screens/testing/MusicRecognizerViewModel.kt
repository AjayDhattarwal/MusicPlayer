package com.ar.musicplayer.screens.testing

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.data.repository.MusicRecognizerRepository
import com.ar.musicplayer.api.ApiConfig
import com.ar.musicplayer.data.models.SongRecognitionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.awaitResponse
import java.io.File

class MusicRecognizerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MusicRecognizerRepository
    val loggingInterceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)
    private val client = OkHttpClient().newBuilder().addInterceptor(loggingInterceptor).build()

    init {
        val apiService = ApiConfig.getMusicRecognizer()
        repository = MusicRecognizerRepository(apiService)
    }


    fun recognizeSong(filePath: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = uploadAudioFile(filePath)
            onResult(result)
        }
    }


    private suspend fun uploadAudioFile(filePath: String): String {
        return withContext(Dispatchers.IO) {
            val file = File(filePath)

            val fileRequestBody = file.asRequestBody("audio/wav".toMediaTypeOrNull())

//            val multipartBody = MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("upload_file", file.name, fileRequestBody)
//                .build()
            val multipartBodyPart = MultipartBody.Part.createFormData("audio", file.name, fileRequestBody)

            try {
                val response: Response<SongRecognitionResponse> = repository.recognizeSong(multipartBodyPart).awaitResponse()
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.track?.title ?: "No response"
                } else {
                    Log.e("UploadAudioFile", "Failed to recognize song: ${response.code()} - ${response.errorBody()?.string()}")
                    "Failed to recognize song: ${response.code()} - ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("UploadAudioFile", "Exception: ${e.message}")
                "Failed to recognize song"
            }
        }
    }


//    @OptIn(ExperimentalEncodingApi::class)
//    fun recognizeSong(filePath: String) {
//
//        val file = File(filePath)
//
//        // Ensure the file exists
//        if (!file.exists()) {
//            Log.e("ShazamApi", "File not found: $filePath")
//            return
//        }
//
//
//// Create a RequestBody for the file
//        val fileRequestBody = file.asRequestBody("audio/wav".toMediaTypeOrNull())
//
//// Create the multipart body
//        val multipartBody = MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("upload_file", file.name, fileRequestBody)
//            .build()
//
//// Build the request
//        val request = Request.Builder()
//            .url("https://shazam-api-free.p.rapidapi.com/shazam/recognize/")
//            .post(multipartBody)
//            .addHeader("x-rapidapi-key", "ba4894b8d6msh953c4cb62591594p19706ajsn9daa2035e36e")
//            .addHeader("x-rapidapi-host", "shazam-api-free.p.rapidapi.com")
//            .build()
//
//
//        // Log the request for debugging
//        Log.d("ShazamApi", "Request: $request")
//
//        // Execute the request
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e("ShazamApi", "Request failed: ${e.message}", e)
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                response.use {
//                    if (!it.isSuccessful) {
//                        Log.e("ShazamApi", "Unexpected code $response")
//                        Log.e("ShazamApi", "Response body: ${it.body?.string()}")
//                        return
//                    }
//                    val responseData = response.body?.string()
//                    Log.d("ShazamApi", "Response: $responseData")
//                }
//            }
//        })
//    }
}
