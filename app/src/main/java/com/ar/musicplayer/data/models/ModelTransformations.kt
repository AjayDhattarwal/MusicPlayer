package com.ar.musicplayer.data.models

import androidx.media3.exoplayer.ExoPlayer
import com.ar.musicplayer.utils.roomdatabase.dbmodels.HomeDataEntity
import com.ar.musicplayer.utils.roomdatabase.dbmodels.SongDownloadEntity
import com.ar.musicplayer.utils.roomdatabase.playlistdb.LocalPlaylistSong
import com.ar.musicplayer.utils.roomdatabase.playlistdb.PlaylistWithSongs
import com.google.gson.Gson


fun SongResponse.toArtist(): Artist {
    return Artist(
        name = this.name,
        id = this.id,
        image = this.image,
        role = this.role,
        type = this.type,
        permaUrl = this.permaUrl,
        isRadioPresent = this.isRadioPresent,
        ctr = this.ctr,

    )
}

fun SongResponse.toPlaylistResponse(): PlaylistResponse {
    return PlaylistResponse(
        id= this.id,
        title= this.title,
        subtitle =  this.subtitle,
        headerDesc = this.headerDesc,
        type = this.type,
        permaUrl =this.permaUrl,
        image = this.image,
        language = this.language,
        year = this.year,
        playCount = this.playCount,
        explicitContent = this.explicitContent,
        listCount = this.listCount,
        listType = this.listType,
        moreInfo= this.moreInfo,
    )
}

fun SongResponse.toAlbumResponse(): Album {
    return Album(
        id= this.id,
        title= this.title,
        subtitle =  this.subtitle,
        type = this.type,
        permaUrl =this.permaUrl,
        image = this.image,
        explicitContent = this.explicitContent,
        moreInfo= this.moreInfo?.toAlbumMoreInfo(),
    )
}

fun MoreInfoResponse.toAlbumMoreInfo(): AlbumMoreInfo {
    return AlbumMoreInfo(
        music = this.music,
        ctr =  this.ctr,
        year =  this.year,
        isMovie =  this.isMovie,
        language =  this.language,
        songPids =  this.songPids,
    )
}



/// homeData Entity //////


fun HomeData.toHomeDataEntity(): HomeDataEntity {
        return HomeDataEntity(
            history = Gson().toJson(this.history),
            newTrending = Gson().toJson(this.newTrending),
            topPlaylist = Gson().toJson(this.topPlaylist),
            newAlbums = Gson().toJson(this.newAlbums),
            browserDiscover = Gson().toJson(this.browserDiscover),
            charts = Gson().toJson(this.charts),
            radio = Gson().toJson(this.radio),
            artistRecos = Gson().toJson(this.artistRecos),
            cityMod = Gson().toJson(this.cityMod),
            tagMixes = Gson().toJson(this.tagMixes),
            data68 = Gson().toJson(this.data68),
            data76 = Gson().toJson(this.data76),
            data185 = Gson().toJson(this.data185),
            data107 = Gson().toJson(this.data107),
            data113 = Gson().toJson(this.data113),
            data114 = Gson().toJson(this.data114),
            data116 = Gson().toJson(this.data116),
            data144 = Gson().toJson(this.data144),
            data211 = Gson().toJson(this.data211),
            modules = Gson().toJson(this.modules)
        )

}

fun HomeDataEntity.toHomeData(): HomeData {
    return HomeData(
        history = parseJsonArray(history),
        newTrending = parseJsonArray(newTrending),
        topPlaylist = parseJsonArray(topPlaylist),
        newAlbums = parseJsonArray(newAlbums),
        browserDiscover = parseJsonArray(browserDiscover),
        charts = parseJsonArray(charts),
        radio = parseJsonArray(radio),
        artistRecos = parseJsonArray(artistRecos),
        cityMod = parseJsonArray(cityMod),
        tagMixes = parseJsonArray(tagMixes),
        data68 = parseJsonArray(data68),
        data76 = parseJsonArray(data76),
        data185 = parseJsonArray(data185),
        data107 = parseJsonArray(data107),
        data113 = parseJsonArray(data113),
        data114 = parseJsonArray(data114),
        data116 = parseJsonArray(data116),
        data144 = parseJsonArray(data144),
        data211 = parseJsonArray(data211),
        modules = Gson().fromJson(modules, ModulesOfHomeScreen::class.java)

    )
}


fun parseJsonArray(json: String?): List<HomeListItem> {
    return try {
        if (!json.isNullOrEmpty()) {
            Gson().fromJson(json, Array<HomeListItem>::class.java).toList()
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}


///      Screen to  Info Screen //////

fun HomeListItem.toInfoScreenModel(): InfoScreenModel {
    return InfoScreenModel(
        id = this.id.toString(),
        title = this.title.toString(),
        songCount = this.count ?: this.moreInfoHomeList?.songCount ?: 0,
        image = this.image.toString(),
        type = this.type.toString(),
        token = this.permaUrl?.substringAfterLast('/') ?: "",
        isYoutube = this.moreInfoHomeList?.isYoutube ?: false,
        subtitle = this.subtitle.toString()
    )
}


fun Playlist.toInfoScreenModel(): InfoScreenModel {
    return InfoScreenModel(
        id = this.id.toString(),
        title = this.title.toString(),
        songCount = 0,
        image = this.image.toString(),
        type = this.type.toString(),
        token = this.permaUrl?.substringAfterLast('/') ?: ""
    )
}


fun Album.toInfoScreenModel(): InfoScreenModel {
    return InfoScreenModel(
        id = this.id.toString(),
        title = this.title.toString(),
        songCount = 0,
        image = this.image.toString(),
        type = this.type.toString(),
        token = this.permaUrl?.substringAfterLast('/') ?: ""
    )
}


fun PlaylistResponse.toInfoScreenModel(): InfoScreenModel {
    return InfoScreenModel(
        id = this.id.toString(),
        title = this.title.toString(),
        songCount = 0,
        image = this.image.toString(),
        type = this.type.toString(),
        token = this.permaUrl?.substringAfterLast('/') ?: ""
    )
}



// Extension function to generate all permutations of a list
fun <T> List<T>.permutations(): Sequence<List<T>> = sequence {
    if (size == 1) {
        yield(this@permutations)
    } else {
        val sub = this@permutations[0]
        for (perm in this@permutations.drop(1).permutations()) {
            for (i in 0..perm.size) {
                val newPerm = perm.toMutableList()
                newPerm.add(i, sub)
                yield(newPerm)
            }
        }
    }
}


fun String.sanitizeString(): String{
    return this
        .replace("&quot;", "")
        .replace("&amp;", ",")
        .replace("3d","")
        .replace("song", "")
        .replace("&#039;","")
}

fun Song.toSongResponse(): SongResponse{
    return  SongResponse(
        id = this.id,
        title = this.title,
        subtitle = this.subtitle,
        type = this.type,
        image = this.image,
        permaUrl = this.permaUrl
    )
}


fun SongResponse.toSongDownloadEntity(): SongDownloadEntity{
    val artist = this.getArtistsString().toString()

    val album = this.moreInfo?.album ?: ""
    val genre = this.role ?: ""
    val imageUrl = this.image?.replace("150x150", "350x350") ?: ""
    val url = this.moreInfo?.encryptedMediaUrl.toString()
    val is320kbps = this.moreInfo?.kbps320 == true

    return SongDownloadEntity(
        id = this.id.toString(),
        title = this.title.toString(),
        artist = artist,
        album = album,
        genre = genre,
        imageUrl = imageUrl,
        url = url,
        is320kbps = is320kbps
    )
}


fun ArtistMap.getArtistList(): List<Artist> {
    return this.artists
        ?.distinctBy { it.name }
        ?.map { artist ->
            Artist(
                id = artist.id,
                name = artist.name?.sanitizeString(),
                image = artist.image,
                ctr = artist.ctr,
                role = artist.role,
                type = artist.type,
                permaUrl = artist.permaUrl,
                isRadioPresent = artist.isRadioPresent
            )
        }
        ?.filter {
            it.role == "music" || it.role == "singer"
        }
        ?: emptyList()
}


fun SongResponse.getArtistsString(): String? {
    return this.moreInfo?.artistMap?.artists
        ?.distinctBy { it.name }
        ?.joinToString(", ") { it.name.toString() }
            ?.sanitizeString()
}

fun ExoPlayer.getArtistsNameList(): List<String> {
    return this.currentMediaItem?.mediaMetadata?.artist
        ?.split(",", "&amp;", "with", "&quot;")
        ?.map { it.trim() }
        ?: listOf()
}

fun String.toLargeImg(): String {
    return this.replace("150x150", "350x350")
}



fun List<PlaylistWithSongs>.toPlaylistResponse() : List<PlaylistResponse>{
    return this.map {
        PlaylistResponse(
            id = it.playlist.id,
            title = it.playlist.name,
            subtitle = it.playlist.description,
            image = it.playlist.image,
            type = "LocalPlaylist",
            list = it.songs.toSongResponse()

        )
    }
}


fun List<LocalPlaylistSong>.toSongResponse(): List<SongResponse> {
    return this.map { item ->
        SongResponse(
            id = item.songId,
            subtitle = item.artist,
            title = item.title,
            moreInfo = MoreInfoResponse(
                artistMap = ArtistMap(
                    artists = item.artist.toArtist()
                ),
                album = item.album,
                duration = item.duration.toString(),
                encryptedMediaUrl = item.encryptedUrl
            ),

            image = item.image
        )
    }
}

fun String.toArtist(): List<Artist> {
    return this.split(",").map { artistName ->
        Artist(
            name = artistName.trim()
        )
    }
}


fun HomeListItem.toSongResponse(): SongResponse {
    return SongResponse(
        id = id.toString(),
        title = title.toString(),
        image = image,
        type = type,
        permaUrl = permaUrl,
        subtitle = subtitle,
        isYoutube = moreInfoHomeList?.isYoutube

    )
}

fun String.sanitizeFileName(): String {
    // Replace any character that isn't alphanumeric, hyphen, dot, or underscore with an underscore.
    return this.replace("[^a-zA-Z0-9\\-\\._]".toRegex(), "_")
}