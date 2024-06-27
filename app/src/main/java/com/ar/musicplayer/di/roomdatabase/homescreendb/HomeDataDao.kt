package com.ar.musicplayer.di.roomdatabase.homescreendb

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ar.musicplayer.di.roomdatabase.dbmodels.HomeDataEntity

@Dao
interface HomeDataDao {

    @Upsert
    suspend fun upsertHomeData(homeData: HomeDataEntity)

    @Query("DELETE FROM HomeDataEntity WHERE id = :homeDataId")
    suspend fun deleteHomeData(homeDataId: Int)

    @Query("SELECT * FROM HomeDataEntity WHERE id = :id")
    suspend fun getHomeDataById(id: Int): HomeDataEntity?
}