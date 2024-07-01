package com.ar.musicplayer.di.roomdatabase.lastsession

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ar.musicplayer.di.roomdatabase.dbmodels.LastSessionDataEntity
import com.ar.musicplayer.di.roomdatabase.homescreendb.HomeDataDao

@Database(
    entities = [LastSessionDataEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LastSessionDatabase: RoomDatabase() {
    abstract val dao: LastSessionDao
}