package com.ar.musicplayer.utils.roomdatabase.favoritedb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ar.musicplayer.utils.roomdatabase.dbmodels.FavSongResponseEntity

@Database(
    entities = [FavSongResponseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FavoriteDatabase: RoomDatabase() {
    abstract val dao: FavDao
}