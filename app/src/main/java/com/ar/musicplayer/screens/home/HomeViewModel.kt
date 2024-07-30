package com.ar.musicplayer.screens.home


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.api.ApiService
import com.ar.musicplayer.models.HomeData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: ApiService,
    application: Application,
) : AndroidViewModel(application) {

    val _homeData = MutableLiveData<HomeData?>()
    val homeData: LiveData<HomeData?> = _homeData


    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadHomeData()
    }
    fun refresh() {
        loadHomeData()
    }
    private fun loadHomeData() {

        viewModelScope.launch {
            try {
                val response = apiService.getHomeData()
                if (response.isSuccessful) {
                    _homeData.value = response.body()
                    Log.d("Home","${response.body()?.modules}")
                } else {
                    _errorMessage.value = "Error loading data: ${response.message()}"
                    Log.d("Home","${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading data: ${e.message}"
                Log.d("Home","${e.message}")

            }
        }
    }



}