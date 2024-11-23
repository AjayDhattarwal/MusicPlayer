package com.ar.musicplayer.data.models

import android.text.Layout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackResponse(
    @SerialName("track")val track: TrackRecognition? = null
)

@Serializable
data class TrackRecognition(
    val genres: Genres? = null,
    val images: Images? = null,
    @SerialName("relatedtracksurl") val relatedTracksUrl: String? = null,
    val sections: List<Section>? = null,
    val subtitle: String? = null,
    val title: String? = null,
    val type: String? = null,
    val key: String? = null
)

@Serializable
data class Genres(
    val primary: String? = null
)

@Serializable
data class Images(
    val background: String? = null,
    val coverart: String? = null,
    val coverarthq: String? = null,
    val joecolor: String? = null
)

@Serializable
data class Section(
    val metadata: List<Metadata>? = null,
    val tabname: String? = null,
    val type: String? = null,
    val url: String? = null
)

@Serializable
data class Metadata(
    val text: String? = null,
    val title: String? = null
)



@Serializable
data class RelatedTracks(
    @SerialName("tracks") val tracks : List<RelatedTrack>
)


@Serializable
data class RelatedTrack(
    val layout: String? = null,
    val type: String? = null,
    val key: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val images: Images? = null
)