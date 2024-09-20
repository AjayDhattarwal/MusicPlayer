package com.ar.musicplayer.utils.notification

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent


class AppLifecycleObserver(private val context: Context) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onAppBackgrounded() {

        // App goes to background
        // Stop service or perform cleanup if required
        Intent(context, AudioService::class.java).also {
            it.action = ACTIONS.STOP.toString()
            context.stopService(it)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        // App returns to foreground
    }
}
