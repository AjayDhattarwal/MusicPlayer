package com.ar.musicplayer.di.roomdatabase.favoritedb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ar.musicplayer.di.roomdatabase.dbmodels.FavSongResponseEntity

@Database(entities = [FavSongResponseEntity::class], version = 1)
abstract class FavoriteDatabase: RoomDatabase() {
    abstract val dao: FavDao
}