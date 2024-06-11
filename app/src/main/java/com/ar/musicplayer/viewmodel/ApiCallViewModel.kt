package com.ar.musicplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ar.musicplayer.di.ApiConfig
import com.ar.musicplayer.models.PlaylistResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ApiCallViewModel() : ViewModel() {

    private val _apiData = MutableLiveData<PlaylistResponse?>()
    val apiLiveData: MutableLiveData<PlaylistResponse?> get() = _apiData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> get() = _isError

    var errorMessage: String = ""
        private set

    fun getApiData(token: String, type: String, totalSong: String, q: String, call: String) {

        _isLoading.value = true
        _isError.value = false

        val client = ApiConfig.getApiService().getApiData(
            token = token,
            type = type,
            totalSong = totalSong,
            q = q,
            call = call
        )

        // Send API request using Retrofit
        client.enqueue(object : Callback<PlaylistResponse> {

            override fun onResponse(
                call: Call<PlaylistResponse>,
                response: Response<PlaylistResponse>
            ) {
                val responseBody = response.body()
                if (!response.isSuccessful || responseBody == null) {
                    onError("Data Processing Error")
                    return
                }

                _isLoading.value = false
                _apiData.postValue(responseBody)
            }

            override fun onFailure(call: Call<PlaylistResponse>, t: Throwable) {
                onError(t.message)
                t.printStackTrace()
            }

        })
    }

    private fun onError(inputMessage: String?) {

        val message = if (inputMessage.isNullOrBlank() or inputMessage.isNullOrEmpty()) "Unknown Error"
        else inputMessage

        errorMessage = StringBuilder("ERROR: ")
            .append("$message some data may not displayed properly").toString()

        _isError.value = true
        _isLoading.value = false
    }

}