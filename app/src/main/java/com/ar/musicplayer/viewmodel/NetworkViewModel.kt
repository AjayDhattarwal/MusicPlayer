package com.ar.musicplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.ar.musicplayer.utils.helper.NetworkStatusHelper

class NetworkViewModel(application: Application) : AndroidViewModel(application) {
    private val networkStatusHelper = NetworkStatusHelper(application)
    val isConnected: LiveData<Boolean> get() = networkStatusHelper
}
