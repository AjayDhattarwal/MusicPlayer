package com.ar.musicplayer.di.roomdatabase.lastsession

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ar.musicplayer.di.roomdatabase.dbmodels.LastSessionDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LastSessionDao {


    @Transaction
    suspend fun insertLastSession(lastSessionDataEntity: LastSessionDataEntity) {
        val lastTenEntries = getLastSession()

        val existingEntry = lastTenEntries.find { it.lastSession == lastSessionDataEntity.lastSession }

        if (existingEntry != null) {
            delete(existingEntry)
        }

        insert(lastSessionDataEntity)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lastSessionDataEntity: LastSessionDataEntity )

    @Delete
    suspend fun delete(lastSessionDataEntity: LastSessionDataEntity)

    @Query("SELECT * FROM lastSessionDataEntity ORDER BY id DESC LIMIT 25")
    fun getLastSession(): List<LastSessionDataEntity>

    @Query("SELECT * FROM lastSessionDataEntity ORDER BY id DESC")
    fun getHistory(): Flow<List<LastSessionDataEntity>>

    @Query("DELETE FROM lastSessionDataEntity WHERE id = :id")
    suspend fun deleteLastSession(id: Int)

    @Query("DELETE FROM lastSessionDataEntity")
    suspend fun deleteAllSongs()
}