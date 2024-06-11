package com.ar.musicplayer.models

import android.os.Parcelable
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
    @SerializedName("image") val image: String? = " ",
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
    @SerializedName("lyrics_id") val lyricsId: String? = ""
) : Parcelable



@Serializable
@Parcelize
data class RightsResponse(
    @SerializedName("code") val code: String? = "",
    @SerializedName("cacheable") val cacheable: String? = "",
    @SerializedName("delete_cached_object") val deleteCachedObject: String? = "",
    @SerializedName("reason") val reason: String? = ""
) : Parcelable
