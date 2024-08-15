package com.ar.musicplayer.utils.roomdatabase.homescreendb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ar.musicplayer.utils.roomdatabase.dbmodels.HomeDataEntity

@Database(
    entities = [HomeDataEntity::class],
    version = 1,
    exportSchema = false
)

abstract class HomeDatabase: RoomDatabase() {
    abstract val dao: HomeDataDao
}