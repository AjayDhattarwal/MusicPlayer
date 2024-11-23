package com.ar.musicplayer.data.repository

import android.util.Log
import com.ar.musicplayer.api.ApiService
import com.ar.musicplayer.data.models.HomeData
import com.ar.musicplayer.data.models.toHomeData
import com.ar.musicplayer.data.models.toHomeDataEntity
import com.ar.musicplayer.utils.roomdatabase.homescreendb.HomeDataDao
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.client.utils.EmptyContent.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject


class HomeDataRepository @Inject constructor(
    private val apiService: ApiService,
    private val homeDataDao: HomeDataDao
) {

    fun getHomeScreenData(): Flow<HomeData?> = flow{
        emit(homeDataDao.getHomeDataById(1)?.toHomeData())
    }.catch { e ->
        Log.e("HomeScreenRepository", "Error fetching data", e)
    }

    suspend fun updateRefreshedData(): HomeData? = withContext(Dispatchers.IO){
        val client = HttpClient(Android){
            engine {
                connectTimeout = 100_000
                socketTimeout = 100_000
            }
            install(Logging){
                level = LogLevel.ALL
            }
        }

        val response = client.get("https://jiosaavn.com/api.php?_format=json&_marker=0&api_version=4&ctx=web6dot0&__call=webapi.getLaunchData").bodyAsText()


        return@withContext try {
            val homeData = Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            }.decodeFromString<HomeData>(response)


            homeData

        } catch (e: NoTransformationFoundException) {
           Log.e("HomeScreenRepository", "Error updating noTranformation data: $e")
            null
        } catch (e: Exception){
            Log.e("HomeScreenRepository", "Error updating data $e")
            null
        }

    }

    suspend fun updateDataInRoom(homeData: HomeData){
      withContext(Dispatchers.IO){
          homeDataDao.upsertHomeData(homeData.toHomeDataEntity())
      }
    }

}