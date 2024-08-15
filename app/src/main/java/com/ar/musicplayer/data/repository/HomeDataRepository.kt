package com.ar.musicplayer.data.repository

import android.util.Log
import com.ar.musicplayer.api.ApiService
import com.ar.musicplayer.data.models.HomeData
import com.ar.musicplayer.data.models.toHomeData
import com.ar.musicplayer.data.models.toHomeDataEntity
import com.ar.musicplayer.utils.roomdatabase.homescreendb.HomeDataDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class HomeDataRepository @Inject constructor(
    private val apiService: ApiService,
    private val homeDataDao: HomeDataDao
) {

    fun getHomeScreenData(): Flow<HomeData?> = flow{
        emit(homeDataDao.getHomeDataById(1)?.toHomeData())

        val response = apiService.getHomeData()

        if(response.isSuccessful){
            response.body()?.let { data ->
               homeDataDao.upsertHomeData(data.toHomeDataEntity())
                emit(data)
            }
        }

    }.catch { e ->
        // Handle error
        Log.e("HomeScreenRepository", "Error fetching data", e)
    }

}