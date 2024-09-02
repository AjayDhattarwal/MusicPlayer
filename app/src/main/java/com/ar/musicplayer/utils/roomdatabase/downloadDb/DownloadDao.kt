package com.ar.musicplayer.utils.roomdatabase.downloadDb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ar.musicplayer.utils.download.DownloadStatus
import com.ar.musicplayer.utils.roomdatabase.dbmodels.SongDownloadEntity

@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads")
    suspend fun getAllDownloads(): List<SongDownloadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongDownloadEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: SongDownloadEntity)

    @Update
    suspend fun update(song: SongDownloadEntity)

    @Delete
    suspend fun delete(song: SongDownloadEntity)

    @Query("DELETE FROM downloads")
    suspend fun clearAll()

    @Query("SELECT * FROM downloads WHERE id = :songId LIMIT 1")
    suspend fun getSongById(songId: String): SongDownloadEntity?

    @Query("UPDATE downloads SET progress = :progress WHERE id = :songId")
    suspend fun updateProgress(songId: String, progress: Int)

    @Query("UPDATE downloads SET status = :status WHERE id = :songId")
    suspend fun updateStatus(songId: String, status: DownloadStatus)
}