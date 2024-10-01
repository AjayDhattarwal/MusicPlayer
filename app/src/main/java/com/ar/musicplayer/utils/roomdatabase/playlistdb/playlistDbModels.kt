package com.ar.musicplayer.utils.roomdatabase.playlistdb

import android.adservices.topics.EncryptedTopic
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "playlists")
data class RoomPlaylist(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String = "",
    val songCount: Int = 0,
    val image: String,
    val pageUrl: String = ""
)


@Entity(
    tableName = "songs",
    foreignKeys = [ForeignKey(
        entity = RoomPlaylist::class,
        parentColumns = ["id"],
        childColumns = ["playlistId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["playlistId", "songId"], unique = true)]
)
data class LocalPlaylistSong(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playlistId: String,
    val songId: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val image: String,
    val encryptedUrl: String
)


data class PlaylistWithSongs(
    @Embedded val playlist: RoomPlaylist,
    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId"
    )
    val songs: List<LocalPlaylistSong>
)