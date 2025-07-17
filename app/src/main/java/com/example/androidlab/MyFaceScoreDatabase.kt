package com.example.androidlab

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Database(entities = [MyFaceScore::class], version = 1)
@TypeConverters(Converters::class)
abstract class MyFaceScoreDatabase : RoomDatabase() {
    abstract fun MyFaceScoreDAO(): MyFaceScoreDAO

    companion object {
        private val mutex = Mutex()
        private var instance: MyFaceScoreDatabase? = null

        suspend fun getInstance(context: Context): MyFaceScoreDatabase {
            return instance ?: mutex.withLock {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): MyFaceScoreDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MyFaceScoreDatabase::class.java,
                "app_database"
            ).build()
        }
    }
}
