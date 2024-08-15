package com.ar.musicplayer.utils.roomdatabase.lastsession

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ar.musicplayer.utils.roomdatabase.dbmodels.LastSessionDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LastSessionDao {


    @Transaction
    suspend fun insertLastSession(lastSessionDataEntity: LastSessionDataEntity) {
        val lastSessionEntries = getLastSessionForPlaying()

        val existingEntry = lastSessionEntries.find { it.lastSession == lastSessionDataEntity.lastSession || it.title == lastSessionDataEntity.title }
        if (existingEntry != null) {
            val playCount = existingEntry.playCount + lastSessionDataEntity.playCount
            val skipCount = existingEntry.skipCount + lastSessionDataEntity.skipCount
            delete(existingEntry)
            val perfectDataEntry = LastSessionDataEntity(title = lastSessionDataEntity.title,
                genres = lastSessionDataEntity.genres,
                playCount = playCount,
                skipCount = skipCount,
                lastSession = lastSessionDataEntity.lastSession
            )

            insert(perfectDataEntry)

        }else{
            Log.d("exist", "${existingEntry}")
            insert(lastSessionDataEntity)
        }
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lastSessionDataEntity: LastSessionDataEntity)

    @Delete
    suspend fun delete(lastSessionDataEntity: LastSessionDataEntity)



    @Query("SELECT * FROM lastSessionDataEntity ORDER BY id DESC LIMIT 25")
    suspend fun getLastSessionForPlaying(): List<LastSessionDataEntity>

    @Query("SELECT * FROM lastSessionDataEntity ORDER BY id DESC LIMIT 25")
    fun getLastSession(): Flow<List<LastSessionDataEntity>>

    @Query("SELECT * FROM lastSessionDataEntity ORDER BY id DESC")
    fun getHistory(): Flow<List<LastSessionDataEntity>>

    @Query("DELETE FROM lastSessionDataEntity WHERE id = :id")
    suspend fun deleteLastSession(id: Int)

    @Query("DELETE FROM lastSessionDataEntity")
    suspend fun deleteAllSongs()
}