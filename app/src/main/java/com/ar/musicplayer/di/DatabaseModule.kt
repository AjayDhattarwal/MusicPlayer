package com.ar.musicplayer.di

import android.content.Context
import androidx.room.Room
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavDao
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteDatabase
import com.ar.musicplayer.di.roomdatabase.favoritedb.FavoriteViewModel
import com.ar.musicplayer.di.roomdatabase.homescreendb.HomeDataDao
import com.ar.musicplayer.di.roomdatabase.homescreendb.HomeDatabase
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionDao
import com.ar.musicplayer.di.roomdatabase.lastsession.LastSessionDatabase
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
    fun provideHomeDataDao(database: HomeDatabase): HomeDataDao {
        return database.dao
    }
    @Provides
    fun provideLastSessionDao(database: LastSessionDatabase): LastSessionDao {
        return  database.dao
    }

    @Provides
    fun provideFavoriteDao(database: FavoriteDatabase): FavDao {
        return database.dao
    }

    @Provides
    @Singleton
    fun provideFavoriteViewModel(
        database: FavoriteDatabase
    ) = FavoriteViewModel(database.dao)


}
