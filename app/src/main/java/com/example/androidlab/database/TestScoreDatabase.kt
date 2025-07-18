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
    abstract fun testScoreDAO(): TestScoreDAO

    companion object {
        @Volatile
        private var INSTANCE: TestScoreDatabase? = null

        fun getInstance(context: Context): TestScoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TestScoreDatabase::class.java,
                    "test_score_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
