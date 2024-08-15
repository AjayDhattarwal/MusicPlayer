package com.ar.musicplayer.data.models

import androidx.lifecycle.viewModelScope
import com.ar.musicplayer.utils.roomdatabase.dbmodels.HomeDataEntity
import com.google.gson.Gson
import kotlinx.coroutines.launch


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
        token = this.permaUrl?.substringAfterLast('/') ?: ""
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

