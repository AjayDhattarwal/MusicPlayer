package com.ar.musicplayer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class InfoScreenModel(
    val id: String,
    val title: String,
    val image: String,
    val type: String,
    val songCount: Int,
    val token: String,
)