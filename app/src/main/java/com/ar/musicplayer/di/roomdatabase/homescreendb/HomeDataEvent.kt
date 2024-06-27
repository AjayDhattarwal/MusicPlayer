package com.ar.musicplayer.di.roomdatabase.homescreendb

import com.ar.musicplayer.models.HomeData


sealed interface HomeDataEvent {
    object LoadHomeData : HomeDataEvent
    data class InsertHomeData(val homeData: HomeData) : HomeDataEvent
    data class DeleteHomeData(val id: Int) : HomeDataEvent
}
