package com.ar.musicplayer.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.models.Artist
import com.ar.musicplayer.models.ArtistMap
import com.ar.musicplayer.models.MoreInfoResponse
import com.ar.musicplayer.models.Song
import com.ar.musicplayer.models.SongResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalSongsViewModel @Inject constructor(
    application: Application,
    private val contentResolver: ContentResolver
): AndroidViewModel(application) {

    private val _songResponseList = MutableStateFlow<List<SongResponse>>(emptyList())
    val songResponseList: StateFlow<List<SongResponse>> get() = _songResponseList

    init {
        fetchLocalSongs()

    }

    private fun fetchLocalSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            val songList = mutableListOf<SongResponse>()

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION // Add duration to the projection
            )

            // Modify the selection to include duration check and avoid recordings
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
                    "${MediaStore.Audio.Media.DURATION} >= 60000" // Only include songs with duration >= 1 minute
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (it.moveToNext()) {
                    val id = it.getString(idColumn)
                    val title = it.getString(titleColumn)
                    val artist = it.getString(artistColumn)
                    val uri = it.getString(dataColumn)
                    val albumId = it.getString(albumIdColumn)
                    val duration = it.getLong(durationColumn)

                    // Optional: Check the file extension or MIME type to avoid recordings
                    if (uri.endsWith(".3gp") || uri.endsWith(".mp4") || uri.contains("recording")) {
                        continue // Skip call recordings or similar
                    }

                    val albumArtUri = Uri.withAppendedPath(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId.toString()
                    )

                    val song = SongResponse(
                        id = id,
                        title = title,
                        moreInfo = MoreInfoResponse(
                            artistMap = ArtistMap(
                                artists = listOf(Artist(name = artist))
                            )
                        ),
                        uri = uri,
                        image = albumArtUri.toString()
                    )

                    Log.d("Local", "id: ${id} ,,,  title: ${title} ,,,  artist: ${artist} ,,,   uri: ${uri} ,,, ")
                    songList.add(song)
                }
            }
            _songResponseList.value = songList
        }
    }


}