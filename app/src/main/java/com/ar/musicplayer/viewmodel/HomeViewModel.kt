package com.ar.musicplayer.viewmodel


import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.api.ApiService
import com.ar.musicplayer.di.roomdatabase.homescreendb.HomeRoomViewModel
import com.ar.musicplayer.models.HomeData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
                } else {
                    _errorMessage.value = "Error loading data: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading data: ${e.message}"
            }
        }
    }

}