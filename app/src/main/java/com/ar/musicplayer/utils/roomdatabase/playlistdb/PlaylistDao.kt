package com.ar.musicplayer.utils.roomdatabase.playlistdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: RoomPlaylist): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSong(song: LocalPlaylistSong)

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylist(playlistId: String): RoomPlaylist

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistWithSongs(playlistId: String): PlaylistWithSongs

    @Transaction
    @Query("SELECT * FROM playlists")
    fun getAllPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>

    @Query("SELECT * FROM playlists")
    fun getPlaylistWithOutSongs(): Flow<List<RoomPlaylist>>

    @Update
    suspend fun updatePlaylist(playlist: RoomPlaylist)


    @Query("SELECT * FROM songs WHERE playlistId = :playlistId ORDER BY id DESC LIMIT 1")
    suspend fun getLastSongForPlaylist(playlistId: String): LocalPlaylistSong?

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Delete
    suspend fun deleteSong(song: LocalPlaylistSong)
}