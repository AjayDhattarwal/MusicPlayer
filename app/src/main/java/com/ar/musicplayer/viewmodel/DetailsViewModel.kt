package com.ar.musicplayer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ar.musicplayer.api.ApiConfig
import com.ar.musicplayer.models.SongDetails
import com.ar.musicplayer.models.SongResponse
import com.ar.musicplayer.utils.events.DetailsEvent
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response

class DetailsViewModel() :ViewModel() {

    var errorMessage: String = ""
        private set

    fun onEvent(event: DetailsEvent){
        when(event){
            is DetailsEvent.getSongDetails -> getSongDetailsFromApi(event.id,event.call,event.callback)
        }
    }

    fun getSongDetailsFromApi(id:String, call:String, onCallback : (SongResponse) -> Unit) {

        val client = ApiConfig.getApiService().getSongDetails(
            pids = id,
            call = call
        )

        client.enqueue(object : Callback<SongDetails>{
            override fun onResponse(call: Call<SongDetails>, response: Response<SongDetails>) {
                val responseBody = response.body()?.songs?.get(0)
                if(response.isSuccessful) {
                    if (responseBody != null) {
                        Log.d("songResponse","${responseBody}")
                        onCallback(responseBody)
                    }

                }
            }

            override fun onFailure(call: Call<SongDetails>, t: Throwable) {
                TODO("Not yet implemented")
            }



        })
    }

    private fun onError(inputMessage: String?) {

        val message = if (inputMessage.isNullOrBlank() or inputMessage.isNullOrEmpty()) "Unknown Error"
        else inputMessage

        errorMessage = StringBuilder("ERROR: ")
            .append("$message some data may not displayed properly").toString()

    }
}