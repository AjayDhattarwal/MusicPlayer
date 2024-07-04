package com.ar.musicplayer.utils.events

sealed interface RecommendationEvent {
    data class GetRecommendations(val songName: String): RecommendationEvent
}