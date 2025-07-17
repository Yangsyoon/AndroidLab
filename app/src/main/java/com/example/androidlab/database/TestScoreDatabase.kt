package com.example.androidlab.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.androidlab.database.Converters
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Database(entities = [TestScore::class], version = 1)
@TypeConverters(Converters::class)
abstract class TestScoreDatabase : RoomDatabase() {
    abstract fun TestScoreDAO(): TestScoreDAO

    companion object {
        private val mutex = Mutex()
        private var instance: TestScoreDatabase? = null

        suspend fun getInstance(context: Context): TestScoreDatabase {
            return instance ?: mutex.withLock {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): TestScoreDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                TestScoreDatabase::class.java,
                "app_database"
            ).build()
        }
    }
}
