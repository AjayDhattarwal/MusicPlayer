package com.ar.musicplayer.data.models

import androidx.compose.ui.graphics.Color
import com.ar.musicplayer.di.HomeDataDtoSerializer
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/////             ***************** HomeData ******************
@Serializable
data class HomeData(
    @SerialName("history") val history: List<HomeListItem> = emptyList(),
    @SerialName("new_trending") val newTrending: List<HomeListItem> = emptyList(),
    @SerialName("top_playlists") val topPlaylist: List<HomeListItem> = emptyList(),
    @SerialName("new_albums") val newAlbums: List<HomeListItem> = emptyList(),
    @SerialName("browse_discover") val browserDiscover: List<HomeListItem> = emptyList(),
    @SerialName("charts") val charts: List<HomeListItem> = emptyList(),
    @SerialName("radio") val radio: List<HomeListItem> = emptyList(),
    @SerialName("artist_recos") val artistRecos: List<HomeListItem> = emptyList(),
    @SerialName("city_mod") val cityMod: List<HomeListItem> = emptyList(),
    @SerialName("tag_mixes") val tagMixes: List<HomeListItem> = emptyList(),
    @SerialName("promo:vx:data:68") val data68: List<HomeListItem> = emptyList(),
    @SerialName("promo:vx:data:76") val data76: List<HomeListItem> = emptyList(),
    @SerialName("promo:vx:data:185") val data185: List<HomeListItem> = emptyList(),
    @SerialName("promo:vx:data:107") val data107: List<HomeListItem> = emptyList(),
    @SerialName("promo:vx:data:113") val data113: List<HomeListItem> = emptyList(),
    @SerialName("promo:vx:data:114") val data114: List<HomeListItem> = emptyList(),
    @SerialName("promo:vx:data:116") val data116: List<HomeListItem> = emptyList(),
    @SerialName("promo:vx:data:145") val data144: List<HomeListItem> = emptyList(),
    @SerialName("promo:vx:data:211") val data211: List<HomeListItem> = emptyList(),
    @SerialName("modules") val modules: ModulesOfHomeScreen
)


@Serializable
data class ModulesOfHomeScreen(
    @SerialName("new_trending") val a1: HomeScreenModuleInfo? = null,
    @SerialName("charts") val a2: HomeScreenModuleInfo? = null,
    @SerialName("new_albums") val a3: HomeScreenModuleInfo? = null,
    @SerialName("top_playlists") val a4: HomeScreenModuleInfo? = null,
    @SerialName("promo:vx:data:107") val a5: HomeScreenModuleInfo? = null,
    @SerialName("radio") val a6: HomeScreenModuleInfo? = null,
    @SerialName("artist_recos") val a7: HomeScreenModuleInfo? = null,
    @SerialName("city_mod") val a8: HomeScreenModuleInfo? = null,
    @SerialName("tag_mixes") val a9: HomeScreenModuleInfo? = null,
    @SerialName("promo:vx:data:68") val a10: HomeScreenModuleInfo? = null,
    @SerialName("promo:vx:data:76") val a11: HomeScreenModuleInfo? = null,
    @SerialName("promo:vx:data:185") val a12: HomeScreenModuleInfo? = null,
    @SerialName("promo:vx:data:113") val a13: HomeScreenModuleInfo? = null,
    @SerialName("promo:vx:data:114") val a14: HomeScreenModuleInfo? = null,
    @SerialName("promo:vx:data:116") val a15: HomeScreenModuleInfo? = null,
    @SerialName("promo:vx:data:145") val a16: HomeScreenModuleInfo? = null,
    @SerialName("promo:vx:data:211") val a17: HomeScreenModuleInfo? = null
) 

@Serializable
data class HomeScreenModuleInfo(
    @SerialName("source") val source: String? = "",
    @SerialName("position") val position: String? = "",
    @SerialName("title") val title: String? = ""
)

@Serializable(with = HomeDataDtoSerializer::class)
data class HomeListItem(
    val id: String? = "",
    val title: String? = "",
    val subtitle: String? = "",
    val headerDesc: String? = "",
    val type: String? = "",
    val permaUrl: String? = "",
    var image: String? = "",
    val language: String? = "",
    val year: String? = "",
    val playCount: String? = "",
    val explicitContent: String? = "",
    val listCount: String? = "",
    val listType: String? = "",
    val list: String? = "",
    val moreInfoHomeList: MoreInfoHomeList? = null,
    val count: Int? = null,
)

@Serializable
data class MoreInfoHomeList(
    @SerialName("release_date") val releaseDate: String? = " ",
    @SerialName("song_count") val songCount: Int? = 0,
    @SerialName("artistMap") val artistMap: ArtistMap? = null,
    @SerialName("follower_count") val followerCount: String? = " ",
    @SerialName("firstname") val firstname: String? = " ",
    @SerialName("last_updated") val lastUpdate: String? = " ",
    @SerialName("uid") val uid: String? = " ",
    @SerialName("featured_station_type") val stationType : String? = "",
    @SerialName("query") val query: String? = "",
    @SerialName("language") val language: String? = "",
    @SerialName("viewCount") val viewCount: String? = "",
    @SerialName("isYoutube") val isYoutube: Boolean? = false
)


/////               ***************** Artist ********************




@Serializable
data class ArtistMap(
    @SerialName("primary_artists") val primaryArtists: List<Artist>? = emptyList(),
    @SerialName("featured_artists") val featuredArtists: List<Artist>? = emptyList(),
    @SerialName("artists") val artists: List<Artist>? = emptyList()
)


@Serializable
data class Artist(
    @SerialName("id") val id: String? = " ",
    @SerialName("name") val name: String? = " ",
    @SerialName("ctr") val ctr: Int? = 0,
    @SerialName("role") val role: String? = " ",
    @SerialName("image") val image: String? = " ",
    @SerialName("type") val type: String? = " ",
    @SerialName("perma_url") val permaUrl: String? = " ",
    @SerialName("isRadioPresent") val isRadioPresent: Boolean? = false,
    @SerialName("description") val description: String? = "",
    @SerialName("other") val other: String? = ""

)



///             ***************** Playlist ********************


@Serializable
data class PlaylistResponse(
    @SerializedName("id") val id: String? = "",
    @SerializedName("title") val title: String? = "",
    @SerializedName("subtitle") val subtitle: String? = "",
    @SerializedName("header_desc") val headerDesc: String? = "",
    @SerializedName("type") val type: String? = "",
    @SerializedName("perma_url") val permaUrl: String? = "",
    @SerializedName("image") val image: String? = "",
    @SerializedName("language") val language: String? = "",
    @SerializedName("year") val year: String? = "",
    @SerializedName("play_count") val playCount: String? = "",
    @SerializedName("explicit_content") val explicitContent: String? = "",
    @SerializedName("list_count") val listCount: Int? = 0,
    @SerializedName("list_type") val listType: String? = "",
    @SerializedName("list") val list: List<SongResponse>? = emptyList(),
    @SerializedName("more_info") val moreInfo: MoreInfoResponse? = null,
    @SerializedName("mini_obj") val miniObj: Boolean? = false,
    @SerializedName("description") val description: String? = ""

) 


///             ***************** Song ********************

@Serializable
data class SongResponseList(
    @SerializedName("list") val list: List<SongResponse>? = emptyList()
)


@Serializable
data class SongResponse(
    @SerializedName("id") val id: String? = "",
    @SerializedName("title") val title: String? = "",
    @SerializedName("subtitle") val subtitle: String? = "",
    @SerializedName("header_desc") val headerDesc: String? = "",
    @SerializedName("type") val type: String? = "",
    @SerializedName("perma_url") val permaUrl: String? = "",
    @SerializedName("image") val image: String? = "",
    @SerializedName("language") val language: String? = "",
    @SerializedName("year") val year: String? = "",
    @SerializedName("play_count") val playCount: String? = "",
    @SerializedName("explicit_content") val explicitContent: String? = "",
    @SerializedName("list_count") val listCount: Int? = 0,
    @SerializedName("list_type") val listType: String? = "",
    @SerializedName("list") val list: String? = "",
    @SerializedName("more_info") val moreInfo: MoreInfoResponse? = null,
    @SerializedName("name") val name: String? = "",
    @SerializedName("ctr") val ctr: Int? = 0,
    @SerializedName("entity") val entity: String? = "",
    @SerializedName("role") val role: String? = "",
    @SerializedName("is_followed") val isFollowed: Boolean? = false,
    @SerializedName("uri") val uri: String? = "",
    @SerializedName("isRadioPresent") val isRadioPresent: Boolean? = false,
    @SerializedName("isYoutube") val isYoutube: Boolean? = false,

    ) 




@Serializable
data class MoreInfoResponse(
    @SerializedName("music") val music: String? = "",
    @SerializedName("album_id") val albumId: String? = "",
    @SerializedName("album") val album: String? = "",
    @SerializedName("label") val label: String? = "",
    @SerializedName("origin") val origin: String? = "",
    @SerializedName("is_dolby_content") val isDolbyContent: Boolean? = false,
    @SerializedName("320kbps") val kbps320: Boolean? = false,
    @SerializedName("encrypted_media_url") val encryptedMediaUrl: String? = "",
    @SerializedName("encrypted_cache_url") val encryptedCacheUrl: String? = "",
    @SerializedName("encrypted_drm_cache_url") val encryptedDrmCacheUrl: String? = "",
    @SerializedName("encrypted_drm_media_url") val encryptedDrmMediaUrl: String? = "",
    @SerializedName("album_url") val albumUrl: String? = "",
    @SerializedName("duration") val duration: String? = "",
    @SerializedName("rights") val rights: RightsResponse? = null,
    @SerializedName("cache_state") val cacheState: String? = "",
    @SerializedName("has_lyrics") val hasLyrics: String? = "",
    @SerializedName("lyrics_snippet") val lyricsSnippet: String? = "",
    @SerializedName("starred") val starred: String? = "",
    @SerializedName("copyright_text") val copyrightText: String? = "",
    @SerializedName("artistMap") val artistMap: ArtistMap? = null,
    @SerializedName("release_date") val releaseDate: String? = "",
    @SerializedName("label_url") val labelUrl: String? = "",
    @SerializedName("vcode") val vcode: String? = "",
    @SerializedName("vlink") val vlink: String? = "",
    @SerializedName("triller_available") val trillerAvailable: Boolean? = false,
    @SerializedName("request_jiotune_flag") val requestJiotuneFlag: Boolean? = false,
    @SerializedName("webp") val webp: String? = "",
    @SerializedName("lyrics_id") val lyricsId: String? = "",
    @SerializedName("query") val query: String? = "",
    @SerializedName("text") val text: String? = "",
    @SerializedName("song_count") val songCount: String? = "",
    @SerializedName("ctr") val ctr : Int? = 0,
    @SerializedName("language") val language: String? = "",
    @SerializedName("year") val year: String? = "",
    @SerializedName("is_movie") val isMovie: String? = "",
    @SerializedName("song_pids") val songPids: String? = ""

) 



@Serializable
data class RightsResponse(
    @SerializedName("code") val code: String? = "",
    @SerializedName("cacheable") val cacheable: String? = "",
    @SerializedName("delete_cached_object") val deleteCachedObject: String? = "",
    @SerializedName("reason") val reason: String? = ""
) 



///  ************************** BasicSongInfo ********************

@Serializable
data class BasicSongInfo(
    @SerializedName("id") val id: String? = "",
    @SerializedName("title") val title: String? = "",
    @SerializedName("subtitle") val subtitle: String? = "",
    @SerializedName("type") val type: String? = "",
    @SerializedName("perma_url") val permaUrl: String? = "",
    @SerializedName("image") val image: String? = "",
) 


///  ************************** Searches ********************

@Serializable
data class SearchResults(
    @SerializedName("total") val id: String? = "",
    @SerializedName("start") val title: String? = "",
    @SerializedName("results") val results: List<SongResponse>? = emptyList(),
) 


@Serializable
data class TopSearchResults(
    @SerializedName("albums") val albums: AlbumsData? = null,
    @SerializedName("songs") val songs: SongsData? = null,
    @SerializedName("playlists") val playlists: PlaylistsData? = null,
    @SerializedName("artists") val artists: ArtistsData? = null,
    @SerializedName("topquery") val topQuery: TopQueryData? = null,
    @SerializedName("shows") val shows: ShowsData? = null,
) 

@Serializable

data class AlbumsData(
    @SerializedName("data") val data: List<Album>? = emptyList(),
    @SerializedName("position") val position: String? = "0"
) 


@Serializable

data class Album(
    @SerializedName("id") val id: String? = "",
    @SerializedName("title") val title: String? = "",
    @SerializedName("subtitle") val subtitle: String? = "",
    @SerializedName("type") val type: String? = "",
    @SerializedName("image") val image: String? = "",
    @SerializedName("perma_url") val permaUrl: String? = "",
    @SerializedName("more_info") val moreInfo: AlbumMoreInfo? = null,
    @SerializedName("explicit_content") val explicitContent: String? = "",
    @SerializedName("mini_obj") val miniObj: Boolean? = false,
    @SerializedName("description") val description: String? = ""
) 


@Serializable
data class AlbumMoreInfo(
    @SerializedName("music") val music: String? = "",
    @SerializedName("ctr") val ctr: Int? = 0,
    @SerializedName("year") val year: String? = "",
    @SerializedName("is_movie") val isMovie: String? = "",
    @SerializedName("language") val language: String? = "",
    @SerializedName("song_pids") val songPids: String? = ""
) 


@Serializable
data class SongsData(
    @SerializedName("data") val data: List<Song>? = emptyList(),
    @SerializedName("position") val position: String? = ""
) 

@Serializable
data class Song(
    @SerializedName("id") val id: String? = "",
    @SerializedName("title") val title: String? = "",
    @SerializedName("subtitle") val subtitle: String? = "",
    @SerializedName("type") val type: String? = "",
    @SerializedName("image") val image: String? = "",
    @SerializedName("perma_url") val permaUrl: String? = "",
    @SerializedName("more_info") val moreInfo: SongMoreInfo? = null,
    @SerializedName("explicit_content") val explicitContent: String? = "",
    @SerializedName("mini_obj") val miniObj: Boolean? = false,
    @SerializedName("description") val description: String? = ""
) 


@Serializable
data class SongMoreInfo(
    @SerializedName("album") val album: String? = "",
    @SerializedName("ctr") val ctr: String? = "0",
    @SerializedName("score") val score: String? = "",
    @SerializedName("vcode") val vcode: String? = "",
    @SerializedName("vlink") val vlink: String? = "",
    @SerializedName("primary_artists") val primaryArtists: String? = "",
    @SerializedName("singers") val singers: String? = "",
    @SerializedName("video_available") val videoAvailable: Boolean? = false,
    @SerializedName("triller_available") val trillerAvailable: Boolean? = false,
    @SerializedName("language") val language: String? = ""
) 


@Serializable
data class PlaylistsData(
    @SerializedName("data") val data: List<Playlist>? = emptyList(),
    @SerializedName("position") val position: String? = "0"
) 


@Serializable
data class Playlist(
    @SerializedName("id") val id: String? = "",
    @SerializedName("title") val title: String? = "",
    @SerializedName("subtitle") val subtitle: String? = "",
    @SerializedName("type") val type: String? = "",
    @SerializedName("image") val image: String? = "",
    @SerializedName("perma_url") val permaUrl: String? = "",
    @SerializedName("more_info") val moreInfo: PlaylistMoreInfo? = null,
    @SerializedName("explicit_content") val explicitContent: String? = "",
    @SerializedName("mini_obj") val miniObj: Boolean? = false,
    @SerializedName("description") val description: String? = ""
) 


@Serializable
data class PlaylistMoreInfo(
    @SerializedName("firstname") val firstname: String? = "",
    @SerializedName("artist_name") val artistName: List<String>? = emptyList(),
    @SerializedName("entity_type") val entityType: String? = "",
    @SerializedName("entity_sub_type") val entitySubType: String? = "",
    @SerializedName("video_available") val videoAvailable: Boolean? = false,
    @SerializedName("is_dolby_content") val isDolbyContent: Boolean? = false,
    @SerializedName("sub_types") val subTypes: String? = null,
    @SerializedName("images") val images: String? = null,
    @SerializedName("lastname") val lastname: String? = "",
    @SerializedName("language") val language: String? = "",
    @SerializedName("song_count") val songCount: Int? = 0,
    @SerializedName("uid") val uid: String? = "",

) 

@Serializable
data class ArtistsData(
    @SerializedName("data") val data: List<ArtistResult>? = emptyList(),
    @SerializedName("position") val position: String? = "0"
) 

@Serializable
data class ArtistResult(
    @SerializedName("id") val id: String? = "",
    @SerializedName("title") val title: String? = "",
    @SerializedName("image") val image: String? = "",
    @SerializedName("extra") val extra: String? = "",
    @SerializedName("perma_url") val permaUrl: String? = "",
    @SerializedName("type") val type: String? = "",
    @SerializedName("mini_obj") val miniObj: Boolean? = false,
    @SerializedName("isRadioPresent") val isRadioPresent: Boolean? = false,
    @SerializedName("ctr") val ctr: String? = "0",
    @SerializedName("entity") val entity: String? = "0",
    @SerializedName("description") val description: String? = "",
    @SerializedName("position") val position: String? = "0"
) 

@Serializable
data class TopQueryData(
    @SerializedName("data") val data: List<Artist>? = emptyList(),
    @SerializedName("position") val position: String? = "0"
) 

@Serializable
data class ShowsData(
    @SerializedName("data") val data: List<Show>? = emptyList(),
    @SerializedName("position") val position: String? = "0"
) 

@Serializable
data class Show(
    @SerializedName("id") val id: String? = "",
    @SerializedName("title") val title: String? = "",
    @SerializedName("subtitle") val subtitle: String? = "",
    @SerializedName("type") val type: String? = "",
    @SerializedName("image") val image: String? = "",
    @SerializedName("perma_url") val permaUrl: String? = "",
    @SerializedName("more_info") val moreInfo: ShowMoreInfo? = null,
    @SerializedName("explicit_content") val explicitContent: String? = "",
    @SerializedName("mini_obj") val miniObj: Boolean? = false,
    @SerializedName("description") val description: String? = ""
) 


@Serializable
data class ShowMoreInfo(
    @SerializedName("season_number") val seasonNumber: String? = "0"
) 


//   -------------- Station Id ---------------

@Serializable
data class StationResponse(
    @SerializedName("stationid") val stationId: String
) 


@Serializable
data class RadioSongs(
    @SerializedName("0") val song0: RadioSongItem,
    @SerializedName("1") val song1: RadioSongItem,
    @SerializedName("2") val song2: RadioSongItem,
    @SerializedName("3") val song3: RadioSongItem,
    @SerializedName("4") val song4: RadioSongItem,
    @SerializedName("5") val song5: RadioSongItem,
    @SerializedName("6") val song6: RadioSongItem,
    @SerializedName("7") val song7: RadioSongItem,
    @SerializedName("8") val song8: RadioSongItem,
    @SerializedName("9") val song9: RadioSongItem,
    @SerializedName("10") val song10: RadioSongItem,
    @SerializedName("11") val song11: RadioSongItem,
    @SerializedName("12") val song12: RadioSongItem,
    @SerializedName("13") val song13: RadioSongItem,
    @SerializedName("14") val song14: RadioSongItem,
    @SerializedName("15") val song15: RadioSongItem,
    @SerializedName("16") val song16: RadioSongItem,
    @SerializedName("17") val song17: RadioSongItem,
    @SerializedName("18") val song18: RadioSongItem,
    @SerializedName("19") val song19: RadioSongItem,
)

@Serializable
data class RadioSongItem(
    @SerializedName("song") val song: SongResponse,
)


@Serializable
data class SongDetails(
    @SerializedName("songs") val songs: List<SongResponse>? = emptyList()
) 



/// Artist full response

@Serializable
data class ArtistResponse(
    @SerializedName("artistId") val artistId: String? = "",
    @SerializedName("name") val name: String? = "",
    @SerializedName("subtitle") val subtitle: String? = "",
    @SerializedName("image") val image: String? = "",
    @SerializedName("follower_count") val followerCount: Int,
    @SerializedName("type") val type: String? = "",
    @SerializedName("dominantLanguage") val dominantLanguage: String? = "",
    @SerializedName("dominantType") val dominantType: String? = "",
    @SerializedName("isRadioPresent") val isRadioPresent: String? = "",
    @SerializedName("fan_count") val fanCount: Boolean? = false,
    @SerializedName("topSongs") val topSongs: List<SongResponse>? = emptyList(),
    @SerializedName("topAlbums") val topAlbums: List<Album>? = emptyList(),
    @SerializedName("dedicated_artist_playlist") val dedicatedPlaylist: List<Playlist>? = emptyList(),
    @SerializedName("featured_artist_playlist") val featuredPlaylist: List<Playlist>? = emptyList(),
    @SerializedName("singles") val singles: List<SongResponse>? = emptyList(),
    @SerializedName("latest_release") val latestRelease: List<SongResponse>? = emptyList(),
    @SerializedName("similarArtists") val similarArtist: List<Artist>? = emptyList()
)



@Serializable
data class LyricsResponse(
    @SerializedName("id") val id: Int ? = 0,
    @SerializedName("trackName") val trackName: String ? = "",
    @SerializedName("artistName") val artistName: String ? = "",
    @SerializedName("albumName") val albumName: String ? = "",
    @SerializedName("duration") val duration: Float? = 0.0f,
    @SerializedName("plainLyrics") val plainLyrics: String ? = "",
    @SerializedName("syncedLyrics") val syncedLyrics: String ? = ""
)

@Serializable
data class TranslationResponse(
    @SerializedName("transliterated_text") val text: String ? = ""
)

@Serializable
data class AiResponse(
    val type: String? = "",
    val songs: List<AiSong>? = null,
    val artists: List<Artist>? = null,
    val genre: String? = "",
    val description: String? = "",
    val other: String? = ""
)

@Serializable
data class AiSong(
    val title: String ? = "",
    val artist: String ? = "",
    val album: String ? = "",
    val release_year: Int ?= 0,
    val duration: Float ?= 0f,
    val genre: String ? = "",
    val description: String ? = "",
    val other: String ? = ""
)

data class SongInfo(
    val title: String ? = "",
    val artist: String ? = "",
    val color: Color ? = Color.Black.copy(0.5f)
)

               /////////////////// Import Playlist /////////////////////

data class ImportPlaylistResponse(
    val id : String,
    val items: List<SimpleImportTrack>,
    val image: String = "",
    val name : String = "",
    val description: String = "",
    val pagingInfo: PagingInfo,
    val pageUrl: String = ""
)

data class SimpleImportTrack(
    val name: String,
    val artists: List<String>
)

data class PagingInfo(
    val limit: Int = 100,
    val offset: Int = 0,
    val totalCount: Int
)