package com.ar.musicplayer.utils.events

sealed interface RadioStationEvent {
    data class LoadRadioStationData(
        val call: String,
        val name: String,
        val query: String,
        val k: String,
        val next: String
    ) : RadioStationEvent

    data class getStationId(
        val call: String,
        val name: String,
        val query: String
    ) : RadioStationEvent

}