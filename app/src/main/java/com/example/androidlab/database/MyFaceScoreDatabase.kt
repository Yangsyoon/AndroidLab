package com.example.androidlab.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MyFaceScore::class], version = 1)
@TypeConverters(Converters::class)
abstract class MyFaceScoreDatabase : RoomDatabase() {
    abstract fun myFaceScoreDAO(): MyFaceScoreDAO

    companion object {
        @Volatile
        private var INSTANCE: MyFaceScoreDatabase? = null

        fun getInstance(context: Context): MyFaceScoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyFaceScoreDatabase::class.java,
                    "my_face_score_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
