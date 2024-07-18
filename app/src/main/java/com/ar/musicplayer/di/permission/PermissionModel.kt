package com.ar.musicplayer.di.permission

data class PermissionModel(
    val permission: String,
    val maxSDKVersion: Int,
    val minSDKVersion: Int,
    val rational: String,
)