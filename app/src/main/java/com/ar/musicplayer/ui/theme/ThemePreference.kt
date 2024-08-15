package com.ar.musicplayer.ui.theme

//import android.content.Context
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.map
//
//private val Context.dataStore by preferencesDataStore("settings")
//
//object ThemePreference {
//    private val DYNAMIC_THEME = booleanPreferencesKey("dynamic_theme")
//
//    fun isDynamicThemeEnabled(context: Context): Flow<Boolean> {
//        return context.dataStore.data.map { preferences ->
//            preferences[DYNAMIC_THEME] ?: false
//        }
//    }
//
//    suspend fun setDynamicThemeEnabled(context: Context, enabled: Boolean) {
//        context.dataStore.edit { preferences ->
//            preferences[DYNAMIC_THEME] = enabled
//        }
//    }
//}