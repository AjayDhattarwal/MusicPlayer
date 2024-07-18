package com.ar.musicplayer.di.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class PermissionViewModelFactory(
    private val permissions: List<PermissionModel>,
    private val askPermission: Boolean
) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PermissionViewModel(permissions = permissions, askPermission = askPermission) as T
    }
}