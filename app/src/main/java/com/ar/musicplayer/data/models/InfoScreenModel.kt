package com.ar.musicplayer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class InfoScreenModel(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val image: String,
    val type: String,
    val songCount: Int,
    val isYoutube: Boolean = false,
    val token: String,
)