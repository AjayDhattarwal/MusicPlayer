package com.ar.musicplayer.di.roomdatabase.homescreendb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.di.roomdatabase.dbmodels.HomeDataEntity
import com.ar.musicplayer.models.HomeData
import com.ar.musicplayer.models.HomeListItem
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeRoomViewModel @Inject constructor(
    private val homeDataDao: HomeDataDao
) : ViewModel() {
    var homeData = MutableStateFlow<HomeData?>(null)

    fun onEvent(event: HomeDataEvent) {
        when (event) {
            is HomeDataEvent.LoadHomeData -> loadHomeData()
            is HomeDataEvent.InsertHomeData -> insertHomeData(event.homeData)
            is HomeDataEvent.DeleteHomeData -> deleteHomeData(event.id)
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            val homeDataEntity = homeDataDao.getHomeDataById(1)
            homeData.value = homeDataEntity?.toHomeData()
        }
    }

    private fun insertHomeData(homeData: HomeData) {
        viewModelScope.launch {
            val homeDataEntity = HomeDataEntity(
                history = Gson().toJson(homeData.history),
                newTrending = Gson().toJson(homeData.newTrending),
                topPlaylist = Gson().toJson(homeData.topPlaylist),
                newAlbums = Gson().toJson(homeData.newAlbums),
                browserDiscover = Gson().toJson(homeData.browserDiscover),
                charts = Gson().toJson(homeData.charts),
                radio = Gson().toJson(homeData.radio),
                artistRecos = Gson().toJson(homeData.artistRecos),
                cityMod = Gson().toJson(homeData.cityMod)
            )
            homeDataDao.upsertHomeData(homeDataEntity)
        }
    }

    private fun deleteHomeData(id: Int) {
        viewModelScope.launch {
            homeDataDao.deleteHomeData(id)
        }
    }

    private fun HomeDataEntity.toHomeData(): HomeData {
        return HomeData(
            history = parseJsonArray(history),
            newTrending = parseJsonArray(newTrending),
            topPlaylist = parseJsonArray(topPlaylist),
            newAlbums = parseJsonArray(newAlbums),
            browserDiscover = parseJsonArray(browserDiscover),
            charts = parseJsonArray(charts),
            radio = parseJsonArray(radio),
            artistRecos = parseJsonArray(artistRecos),
            cityMod = parseJsonArray(cityMod)
        )
    }

    private fun parseJsonArray(json: String?): List<HomeListItem> {
        return try {
            if (!json.isNullOrEmpty()) {
                Gson().fromJson(json, Array<HomeListItem>::class.java).toList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}