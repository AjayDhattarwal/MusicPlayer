package com.ar.musicplayer.di.roomdatabase.homescreendb

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.di.roomdatabase.dbmodels.HomeDataEntity
import com.ar.musicplayer.models.HomeData
import com.ar.musicplayer.models.HomeListItem
import com.ar.musicplayer.models.ModulesOfHomeScreen
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeRoomViewModel @Inject constructor(
    private val homeDataDao: HomeDataDao
) : ViewModel() {
//    var homeData = MutableStateFlow<HomeData?>(null)
    val _homeData = MutableLiveData<HomeData?>()
    val homeData: LiveData<HomeData?> = _homeData

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
            _homeData.value = homeDataEntity?.toHomeData()
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
                cityMod = Gson().toJson(homeData.cityMod),
                tagMixes = Gson().toJson(homeData.tagMixes),
                data68 = Gson().toJson(homeData.data68),
                data76 = Gson().toJson(homeData.data76),
                data185 = Gson().toJson(homeData.data185),
                data107 = Gson().toJson(homeData.data107),
                data113 = Gson().toJson(homeData.data113),
                data114 = Gson().toJson(homeData.data114),
                data116 = Gson().toJson(homeData.data116),
                data144 = Gson().toJson(homeData.data144),
                data211 = Gson().toJson(homeData.data211),
                modules = Gson().toJson(homeData.modules)
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
            cityMod = parseJsonArray(cityMod),
            tagMixes = parseJsonArray(tagMixes),
            data68 = parseJsonArray(data68),
            data76 = parseJsonArray(data76),
            data185 = parseJsonArray(data185),
            data107 = parseJsonArray(data107),
            data113 = parseJsonArray(data113),
            data114 = parseJsonArray(data114),
            data116 = parseJsonArray(data116),
            data144 = parseJsonArray(data144),
            data211 = parseJsonArray(data211),
            modules = Gson().fromJson(modules, ModulesOfHomeScreen::class.java)

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