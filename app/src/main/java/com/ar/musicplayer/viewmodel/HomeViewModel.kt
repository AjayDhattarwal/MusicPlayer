package com.ar.musicplayer.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.data.repository.HomeDataRepository
import com.ar.musicplayer.data.models.HomeData
import com.ar.musicplayer.data.models.HomeListItem
import com.ar.musicplayer.data.models.ModulesOfHomeScreen
import com.ar.musicplayer.data.models.MoreInfoHomeList
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.repository.Item
import com.ar.musicplayer.data.repository.YoutubeRepository
import com.ar.musicplayer.utils.helper.NetworkConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeDataRepository: HomeDataRepository,
    private val youtubeRepository: YoutubeRepository,
    private  val networkConnectivityObserver: NetworkConnectivityObserver
) : ViewModel(){

    private val _data = MutableStateFlow<HomeData?>(null)


    private val _youtubeData = MutableStateFlow<List<Pair<String?, List<HomeListItem>>>?>(null)

    val combinedData: Flow<List<Pair<String?, List<HomeListItem>>>> = combine(
        _youtubeData.asStateFlow(), _data.asStateFlow()
    ){ youtubeData, homeData ->
        (if (homeData != null) {
            getMappedHomeData(homeData, homeData.modules, youtubeData)
        } else{
            emptyList()
        })
    }

    val homeData: StateFlow<List<Pair<String?, List<HomeListItem>>>?> = combinedData
        .onStart {
           loadHomeScreenData()
        }
        .stateIn(viewModelScope, WhileSubscribed(5000), null)

    private val _isDataRefreshed = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            networkConnectivityObserver.observe().collect { isConnected ->
                if (isConnected && !_isDataRefreshed.value) {
                   withContext(Dispatchers.IO){
                       refreshData()
                   }
                }
            }
        }
    }

    fun loadHomeScreenData() {
        viewModelScope.launch {
            launch {
                homeDataRepository.getHomeScreenData()
                    .collect { homeData ->
                        _data.value = homeData
                    }
            }
            launch(Dispatchers.IO){
                val data = homeDataRepository.updateRefreshedData()

                _data.value = data
                data?.let { homeDataRepository.updateDataInRoom(it) }
            }
            launch(Dispatchers.IO) {
                getYoutubeMapData()
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
             homeDataRepository.getHomeScreenData()
            _isDataRefreshed.value = true
        }
    }


    suspend fun createSortedSourceTitleMap(modules: ModulesOfHomeScreen): Map<String?, String?> = withContext(Dispatchers.IO) {
        return@withContext listOf(
            modules.a1, modules.a2, modules.a3, modules.a4, modules.a5,
            modules.a6, modules.a7, modules.a8, modules.a9, modules.a10, modules.a11,
            modules.a12, modules.a13, modules.a14,modules.a15,
            modules.a16, modules.a17
        ).filterNotNull()
            .sortedBy {
                it.position?.toIntOrNull() ?: Int.MAX_VALUE
            }.associate { it.source to it.title  }
    }

    suspend fun getYoutubeMapData(){
        withContext(Dispatchers.IO){
            val list =  youtubeRepository.getMusicHome()

            val items = list.map { map ->
                async{
                    val title = map["title"] as String
                    val playlists = map["playlists"] as List<Item>

                    title to playlists.toHomeListItem()
                }
            }
            _youtubeData.value = items.awaitAll()
        }

    }

    suspend fun List<Item>.toHomeListItem(): List<HomeListItem> = withContext(Dispatchers.IO) {
        return@withContext this@toHomeListItem.map{ item ->
            HomeListItem(
                id = item.id,
                title = item.title,
                subtitle = item.description,
                type = item.type,
                image = item.image,
                moreInfoHomeList = MoreInfoHomeList(
                    isYoutube = true,
                    viewCount = item.count
                )
            )
        }
    }



    suspend fun getMappedHomeData(
        homeData: HomeData,
        modules: ModulesOfHomeScreen,
        youtubeData: List<Pair<String?, List<HomeListItem>>>?
    ): List<Pair<String?, List<HomeListItem>>> = withContext(Dispatchers.Main) {
        Log.d("HomeViewmodle " , "getMappedHomeData: $youtubeData")
        Log.d("HomeViewmodle", "getmodules: $modules" )
        Log.d("HomeViewmodle", "getHomeData: $homeData" )

        val sortedSourceTitleMap = createSortedSourceTitleMap(modules)

        val sourceToListMap = mapOf(
            "new_trending" to homeData.newTrending,
            "top_playlists" to homeData.topPlaylist,
            "new_albums" to homeData.newAlbums,
            "charts" to homeData.charts,
            "radio" to homeData.radio,
            "artist_recos" to homeData.artistRecos,
            "city_mod" to homeData.cityMod,
            "tag_mixes" to homeData.tagMixes,
            "promo:vx:data:68" to homeData.data68,
            "promo:vx:data:76" to homeData.data76,
            "promo:vx:data:185" to homeData.data185,
            "promo:vx:data:107" to homeData.data107,
            "promo:vx:data:113" to homeData.data113,
            "promo:vx:data:114" to homeData.data114,
            "promo:vx:data:116" to homeData.data116,
            "promo:vx:data:145" to homeData.data144,
            "promo:vx:data:211" to homeData.data211,
            "browser_discover" to homeData.browserDiscover
        )

        val list = sortedSourceTitleMap.mapNotNull { (source, title) ->
            sourceToListMap[source]?.let { title to it }
        }.toMap().toList()



        val sortedList = if(youtubeData != null){
            list + youtubeData
        } else{
            list
        }

        return@withContext sortedList
    }
}