package com.ar.musicplayer.di.roomdatabase.dbmodels

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
    val cityMod: String
)

@Entity
data class LastSessionDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
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
    val listCount: String,
    val listType: String,
    val list: String,
    val moreInfo: String,
    val name: String,
    val ctr: String,
    val entity: String,
    val role: String
)