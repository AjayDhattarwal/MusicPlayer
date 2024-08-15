package com.ar.musicplayer.utils.roomdatabase.dbmodels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HomeDataEntity(
    @PrimaryKey val id: Int = 1,
    val history: String,
    val newTrending: String,
    val topPlaylist: String,
    val newAlbums: String,
    val browserDiscover: String,
    val charts: String,
    val radio: String,
    val artistRecos: String,
    val cityMod: String,
    val tagMixes: String,
    val data68: String,
    val data76: String,
    val data185: String,
    val data107: String,
    val data113: String,
    val data114: String,
    val data116: String,
    val data144: String,
    val data211: String,
    val modules: String
)

@Entity
data class LastSessionDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val title: String,
    val genres: String,
    val playCount: Int,
    val skipCount: Int,
    val lastSession: String
)

@Entity
data class FavSongResponseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val songId: String,
    val title: String,
    val subtitle: String,
    val type: String,
    val permaUrl: String,
    val image: String,
    val language: String,
    val year: String,
    val playCount: String,
    val explicitContent: String,
    val listCount: Int,
    val listType: String,
    val list: String,
    val moreInfo: String,
    val name: String,
    val ctr: Int,
    val entity: String,
    val role: String
)