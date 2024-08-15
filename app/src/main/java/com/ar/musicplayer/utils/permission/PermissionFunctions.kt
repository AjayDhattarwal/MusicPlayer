package com.ar.musicplayer.utils.permission

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat

fun hasPermissions(context: ComponentActivity, vararg permissions: String): Boolean =
    permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }