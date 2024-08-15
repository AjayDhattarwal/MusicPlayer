package com.ar.musicplayer.utils.permission

data class PermissionModel(
    val permission: String,
    val maxSDKVersion: Int,
    val minSDKVersion: Int,
    val rational: String,
)