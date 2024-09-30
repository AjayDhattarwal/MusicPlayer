package com.ar.musicplayer.utils.roomdatabase.playlistdb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RoomPlaylist::class, LocalPlaylistSong::class],
    version = 1,
    exportSchema = false
)
abstract class PlaylistDatabase: RoomDatabase() {
    abstract val playlistDao: PlaylistDao
}