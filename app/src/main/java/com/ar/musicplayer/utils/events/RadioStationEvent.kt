package com.ar.musicplayer.utils.events

sealed interface RadioStationEvent {
    data class LoadRadioStationData(
        val call: String,
        val name: String,
        val query: String,
        val k: String,
        val next: String,
        val radioStationType: String,
        val language: String
    ) : RadioStationEvent

}