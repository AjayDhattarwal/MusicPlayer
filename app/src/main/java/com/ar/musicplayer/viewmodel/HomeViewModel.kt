package com.ar.musicplayer.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.data.repository.HomeDataRepository
import com.ar.musicplayer.data.models.HomeData
import com.ar.musicplayer.utils.helper.NetworkConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeDataRepository: HomeDataRepository,
    private  val networkConnectivityObserver: NetworkConnectivityObserver
) : ViewModel(){


    private val _homeScreenData = MutableStateFlow<HomeData?>(null)
    val homeData: StateFlow<HomeData?> = _homeScreenData.asStateFlow()

    private val _isDataRefreshed = MutableStateFlow(false)


    init {
        viewModelScope.launch {
            homeDataRepository.getHomeScreenData().collect { data ->
                _homeScreenData.value = data
            }
        }

        viewModelScope.launch {
            networkConnectivityObserver.observe().collect { isConnected ->
                if (isConnected && !_isDataRefreshed.value) {
                    refreshData()
                }
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            homeDataRepository.getHomeScreenData()
                .collect { data ->
                    _homeScreenData.value = data
                }
            _isDataRefreshed.value = true
        }
    }
}