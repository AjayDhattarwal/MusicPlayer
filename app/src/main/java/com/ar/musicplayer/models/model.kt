package com.ar.musicplayer.models

import android.os.Parcelable
import android.provider.MediaStore.Audio.Albums
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

/////             ***************** HomeData ******************
data class HomeData(
    @SerializedName("history") val history: List<HomeListItem>,
    @SerializedName("new_trending") val newTrending: List<HomeListItem>,
    @SerializedName("top_playlists") val topPlaylist: List<HomeListItem>,
    @SerializedName("new_albums") val newAlbums: List<HomeListItem>,
    @SerializedName("browse_discover") val browserDiscover: List<HomeListItem>,
    @SerializedName("charts") val charts: List<HomeListItem>,
    @SerializedName("radio") val radio: List<HomeListItem>,
    @SerializedName("artist_recos") val artistRecos: List<HomeListItem>,
    @SerializedName("city_mod") val cityMod: List<HomeListItem>,
)

@Serializable
@Parcelize
data class HomeListItem(
    @SerializedName("id") val id: String? = " ",
    @SerializedName("title") val title: String? = " ",
    @SerializedName("subtitle") val subtitle: String? = " ",
    @SerializedName("header_desc") val headerDesc: String? = " ",
    @SerializedName("type") val type: String? = " ",
    @SerializedName("perma_url") val permaUrl: String? = " ",
    @SerializedName("image") var image: String? = " ",
    @SerializedName("language") val language: String? = " ",
    @SerializedName("year") val year: String? = " ",
    @SerializedName("play_count") val playCount: String? = " ",
    @SerializedName("explicit_content") val explicitContent: String? = " ",
    @SerializedName("list_count") val listCount: String? = " ",
    @SerializedName("list_type") val listType: String? = " ",
    @SerializedName("list") val list: String? = " ",
    @SerializedName("more_info") val moreInfoHomeList: MoreInfoHomeList?,
//    @SerializedName("modules") val modules: Any?,
//    @SerializedName("button_tooltip_info") val buttonTooltipInfo: List<Any>,
): Parcelable

@Serializable
@Parcelize
data class MoreInfoHomeList(
    @SerializedName("release_date") val releaseDate: String? = " ",
    @SerializedName("song_count") val songCount: String? = " ",
    @SerializedName("artistMap") val artistMap: ArtistMap? = null,
    @SerializedName("follower_count") val followerCount: String? = " ",
    @SerializedName("firstname") val firstname: String? = " ",
    @SerializedName("last_updated") val lastUpdate: String? = " " ,
    @SerializedName("uid") val uid: String? = " "
): Parcelable


/////               ***************** Artist ********************




@Serializable
@Parcelize
data class ArtistMap(
    @SerializedName("primary_artists") val primaryArtists: List<Artist>? = emptyList(),
    @SerializedName("featured_artists") val featuredArtists: List<Artist>? = emptyList(),
    @SerializedName("artists") val artists: List<Artist>? = emptyList()
): Parcelable


@Serializable
@Parcelize
data class Artist(
    @SerializedName("id") val id: String? = " ",
    @SerializedName("name") val name: String? = " ",
    @SerializedName("role") val role: String? = " ",
    @SerializedName("image") val image: String? = " ",
    @SerializedName("type") val type: String? = " ",
    @SerializedName("perma_url") val permaUrl: String? = " "
): Parcelable



///             ***************** Playlist ********************


@Serializable
@Parcelize
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
    @SerializedName("list_count") val listCount: String? = "",
    @SerializedName("list_type") val listType: String? = "",
    @SerializedName("list") val list: List<SongResponse>? = emptyList(),
    @SerializedName("more_info") val moreInfo: MoreInfoResponse? = null,
    @SerializedName("mini_obj") val miniObj: Boolean? = false,
    @SerializedName("description") val description: String? = ""

) : Parcelable


@Serializable
@Parcelize
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
    @SerializedName("list_count") val listCount: String? = "",
    @SerializedName("list_type") val listType: String? = "",
    @SerializedName("list") val list: String? = "",
    @SerializedName("more_info") val moreInfo: MoreInfoResponse? = null,
    @SerializedName("name") val name: String? = "",
    @SerializedName("ctr") val ctr: String? = "",
    @SerializedName("entity") val entity: String? = "",
    @SerializedName("role") val role: String? = "",
    @SerializedName("is_followed") val isFollowed: Boolean? = false,


) : Parcelable



@Serializable
@Parcelize
data class MoreInfoResponse(
    @SerializedName("music") val music: String? = "",
    @SerializedName("album_id") val albumId: String? = "",
    @SerializedName("album") val album: String? = "",
    @SerializedName("label") val label: String? = "",
    @SerializedName("origin") val origin: String? = "",
    @SerializedName("is_dolby_content") val isDolbyContent: Boolean? = false,
    @SerializedName("320kbps") val kbps320: String? = "",
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
    @SerializedName("ctr") val ctr : String? = "0",
    @SerializedName("language") val language: String? = "",

) : Parcelable



@Serializable
@Parcelize
data class RightsResponse(
    @SerializedName("code") val code: String? = "",
    @SerializedName("cacheable") val cacheable: String? = "",
    @SerializedName("delete_cached_object") val deleteCachedObject: String? = "",
    @SerializedName("reason") val reason: String? = ""
) : Parcelable



///  ************************** BasicSongInfo ********************

@Serializable
@Parcelize
data class BasicSongInfo(
    @SerializedName("id") val id: String? = "",
    @SerializedName("title") val title: String? = "",
    @SerializedName("subtitle") val subtitle: String? = "",
    @SerializedName("type") val type: String? = "",
    @SerializedName("perma_url") val permaUrl: String? = "",
    @SerializedName("image") val image: String? = "",
) : Parcelable


///  ************************** Searches ********************

@Serializable
@Parcelize
data class SearchResults(
    @SerializedName("total") val id: String? = "",
    @SerializedName("start") val title: String? = "",
    @SerializedName("results") val results: List<SongResponse>? = emptyList(),
) : Parcelable


@Serializable
@Parcelize
data class TopSearchResults(
    @SerializedName("albums") val albums: AlbumsData? = null,
    @SerializedName("songs") val songs: SongsData? = null,
    @SerializedName("playlists") val playlists: PlaylistsData? = null,
    @SerializedName("artists") val artists: ArtistsData? = null,
    @SerializedName("topquery") val topQuery: TopQueryData? = null,
    @SerializedName("shows") val shows: ShowsData? = null,
) : Parcelable

@Serializable
@Parcelize
data class AlbumsData(
    @SerializedName("data") val data: List<Album>? = emptyList(),
    @SerializedName("position") val position: String? = "0"
) : Parcelable


@Serializable
@Parcelize
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
) : Parcelable


@Serializable
@Parcelize
data class AlbumMoreInfo(
    @SerializedName("music") val music: String? = "",
    @SerializedName("ctr") val ctr: String? = "0",
    @SerializedName("year") val year: String? = "",
    @SerializedName("is_movie") val isMovie: String? = "",
    @SerializedName("language") val language: String? = "",
    @SerializedName("song_pids") val songPids: String? = ""
) : Parcelable


@Serializable
@Parcelize
data class SongsData(
    @SerializedName("data") val data: List<Song>? = emptyList(),
    @SerializedName("position") val position: String? = ""
) : Parcelable

@Serializable
@Parcelize
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
) : Parcelable


@Serializable
@Parcelize
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
) : Parcelable


@Serializable
@Parcelize
data class PlaylistsData(
    @SerializedName("data") val data: List<Playlist>? = emptyList(),
    @SerializedName("position") val position: String? = "0"
) : Parcelable


@Serializable
@Parcelize
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
) : Parcelable


@Serializable
@Parcelize
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
    @SerializedName("language") val language: String? = ""
) : Parcelable

@Serializable
@Parcelize
data class ArtistsData(
    @SerializedName("data") val data: List<ArtistResult>? = emptyList(),
    @SerializedName("position") val position: String? = "0"
) : Parcelable

@Serializable
@Parcelize
data class ArtistResult(
    @SerializedName("id") val id: String? = "",
    @SerializedName("title") val title: String? = "",
    @SerializedName("image") val image: String? = "",
    @SerializedName("extra") val extra: String? = "",
    @SerializedName("type") val type: String? = "",
    @SerializedName("mini_obj") val miniObj: Boolean? = false,
    @SerializedName("isRadioPresent") val isRadioPresent: Boolean? = false,
    @SerializedName("ctr") val ctr: String? = "0",
    @SerializedName("entity") val entity: String? = "0",
    @SerializedName("description") val description: String? = "",
    @SerializedName("position") val position: String? = "0"
) : Parcelable

@Serializable
@Parcelize
data class TopQueryData(
    @SerializedName("data") val data: List<Artist>? = emptyList(),
    @SerializedName("position") val position: String? = "0"
) : Parcelable

@Serializable
@Parcelize
data class ShowsData(
    @SerializedName("data") val data: List<Show>? = emptyList(),
    @SerializedName("position") val position: String? = "0"
) : Parcelable

@Serializable
@Parcelize
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
) : Parcelable


@Serializable
@Parcelize
data class ShowMoreInfo(
    @SerializedName("season_number") val seasonNumber: String? = "0"
) : Parcelable

