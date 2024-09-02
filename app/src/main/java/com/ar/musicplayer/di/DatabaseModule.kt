package com.ar.musicplayer.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.ar.musicplayer.utils.roomdatabase.downloadDb.DownloadDao
import com.ar.musicplayer.utils.roomdatabase.downloadDb.DownloadDatabase
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavDao
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteDatabase
import com.ar.musicplayer.utils.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.utils.roomdatabase.homescreendb.HomeDataDao
import com.ar.musicplayer.utils.roomdatabase.homescreendb.HomeDatabase
import com.ar.musicplayer.utils.roomdatabase.lastsession.LastSessionDao
import com.ar.musicplayer.utils.roomdatabase.lastsession.LastSessionDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHomeDatabase(@ApplicationContext context: Context): HomeDatabase {
        return Room.databaseBuilder(
            context,
            HomeDatabase::class.java,
            "home_data_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideLastSessionDatabase(@ApplicationContext context: Context): LastSessionDatabase {
        return Room.databaseBuilder(
            context,
            LastSessionDatabase::class.java,
            "lastSession_data_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFavDatabase(@ApplicationContext context: Context): FavoriteDatabase {
        return Room.databaseBuilder(
            context,
            FavoriteDatabase::class.java,
            "fav_data_database"
        ).build()
    }


    @Provides
    @Singleton
    fun provideDownloadDatabase(@ApplicationContext context: Context): DownloadDatabase {
        return DownloadDatabase.getDatabase(context)
    }



    @Provides
    @Singleton
    fun provideHomeDataDao(database: HomeDatabase): HomeDataDao {
        return database.dao
    }
    @Provides
    @Singleton
    fun provideLastSessionDao(database: LastSessionDatabase): LastSessionDao {
        return  database.dao
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(database: FavoriteDatabase): FavDao {
        return database.dao
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: DownloadDatabase): DownloadDao{
        return database.downloadDao()
    }
}
