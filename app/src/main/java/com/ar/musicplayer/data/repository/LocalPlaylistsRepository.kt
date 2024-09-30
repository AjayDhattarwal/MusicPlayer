package com.ar.musicplayer.data.repository

import android.util.Log
import com.ar.musicplayer.utils.roomdatabase.playlistdb.LocalPlaylistSong
import com.ar.musicplayer.utils.roomdatabase.playlistdb.PlaylistDao
import com.ar.musicplayer.utils.roomdatabase.playlistdb.RoomPlaylist
import javax.inject.Inject

class LocalPlaylistsRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {
    suspend fun insertPlaylist(playlist: RoomPlaylist) = playlistDao.insertPlaylist(playlist)

    suspend fun insertSong(song: LocalPlaylistSong) = playlistDao.insertSong(song)

    fun getAllPlaylists() = playlistDao.getAllPlaylistsWithSongs()

    fun getPlaylistWithOutSongs() = playlistDao.getPlaylistWithOutSongs()

    suspend fun getPlaylistWithSongs(playlistId: String) = playlistDao.getPlaylistWithSongs(playlistId)

    suspend fun getLastSongForPlaylist(playlistId: String) = playlistDao.getLastSongForPlaylist(playlistId)

    suspend fun deletePlaylist(playlistId: String) = playlistDao.deletePlaylist(playlistId)

    suspend fun updatePlaylist(playlistId: String, title: String?, description: String?) {
        val playlist = getPlaylistWithSongs(playlistId)
        val songCount = playlist.songs.size
        val image = if(playlist.playlist.pageUrl.isEmpty()){
            playlist.songs.first().image
        }else{
            null
        }

        playlist.let {
            val updatedPlaylist = RoomPlaylist(
                id = it.playlist.id,
                name = title ?: it.playlist.name,
                description = description?: it.playlist.description,
                songCount = songCount,
                image = image ?: it.playlist.image,
            )
            playlistDao.updatePlaylist(updatedPlaylist)

        }

    }

}