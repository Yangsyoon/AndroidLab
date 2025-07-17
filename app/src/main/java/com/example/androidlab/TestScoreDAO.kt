package com.example.androidlab

import androidx.room.*

@Dao
interface TestScoreDAO {
    @Insert
    suspend fun insert(user: TestScore)

    @Query("SELECT * FROM TestScore")
    suspend fun getAllUsers(): List<TestScore>

    @Delete
    suspend fun delete(user: TestScore)

    @Update
    suspend fun update(user: TestScore)
}