package com.ar.musicplayer.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ar.musicplayer.api.ApiConfig
import com.ar.musicplayer.data.models.RadioSongs
import com.ar.musicplayer.data.models.RadioSongItem
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.StationResponse
import com.ar.musicplayer.utils.events.RadioStationEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RadioStationViewModel () : ViewModel() {

    private val _radioStation = MutableLiveData<List<SongResponse>>()
    val radioStation : MutableLiveData<List<SongResponse>> get() = _radioStation

    private val _stationId = MutableLiveData<StationResponse>()
    val stationId : MutableLiveData<StationResponse> get() = _stationId

    fun onEvent(event: RadioStationEvent){
        when(event){
            is RadioStationEvent.LoadRadioStationData ->
                LoadCurrentStation(
                    call = event.call,
                    k = event.k,
                    next = event.next,
                    name = event.name,
                    query = event.query,
                    radioStationType = event.radioStationType,
                    language = event.language
                )
        }
    }





    private fun LoadCurrentStation(call: String, k: String, next: String, name: String, query: String,radioStationType: String,language:String) {

        val clientToGetStationId =
            if(radioStationType == "artist"){
                ApiConfig.getApiService().getArtistStationId(
                    call = "webradio.createArtistStation",
                    name = name,
                    query = query
                )
            }
        else{
            ApiConfig.getApiService().getFeaturedStationId(
                call = "webradio.createFeaturedStation",
                name = name,
                language = language
            )

        }

        // Send API request using Retrofit
        clientToGetStationId.enqueue(object : Callback<StationResponse> {

            override fun onResponse(
                retrofitCall: Call<StationResponse>,
                response: Response<StationResponse>
            ) {
                val responseBody = response.body()
                if (!response.isSuccessful || responseBody == null) {
                    return
                }

                var stationId = response.body()

                val client = stationId?.let {
                    ApiConfig.getApiService().getRadioSongs(
                        call = call,
                        k = k,
                        next = next,
                        stationid = it.stationId
                    )
                }

                // Send API request using Retrofit
                client?.enqueue(object : Callback<RadioSongs> {
                    override fun onResponse(call: Call<RadioSongs>, response: Response<RadioSongs>) {
                        if (response.isSuccessful) {
                            Log.d("song", "${response.body()}")

                            val songResponses = mutableListOf<SongResponse?>()
                            val responseBody = response.body()

                            if (responseBody != null) {
                                for (i in 0..19) {
                                    val fieldName = "song$i"
                                    try {
                                        val songField = responseBody.javaClass.getDeclaredField(fieldName)
                                        songField.isAccessible = true
                                        val songItem = songField.get(responseBody) as? RadioSongItem
                                        songResponses.add(songItem?.song)
                                    } catch (e: NoSuchFieldException) {
                                        Log.e("Error", "Field $fieldName not found", e)
                                        songResponses.add(null) // Add null if the field does not exist
                                    } catch (e: IllegalAccessException) {
                                        Log.e("Error", "Field $fieldName not accessible", e)
                                        songResponses.add(null) // Add null if the field is not accessible
                                    }
                                }
                            }

                            // Now songResponses list contains all the song responses
                            _radioStation.postValue(songResponses as List<SongResponse>)
                        }
                    }
                    override fun onFailure(call: Call<RadioSongs>, t: Throwable) {
                        Log.e("Failure", "Network call failed: ${t.message}")
                    }
                })
            }
            override fun onFailure(call: Call<StationResponse>, t: Throwable) {

                t.printStackTrace()
            }

        })

    }
}