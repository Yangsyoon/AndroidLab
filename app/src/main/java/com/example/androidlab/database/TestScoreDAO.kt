package com.example.androidlab.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TestScoreDAO {
    @Insert
    suspend fun insert(user: TestScore)

    @Query("SELECT * FROM TestScore")
    suspend fun getAllUsers(): List<TestScore>

    @Query("SELECT * FROM TestScore ORDER BY date DESC")
    fun getAllSortedByDate(): Flow<List<TestScore>>

    @Delete
    suspend fun delete(user: TestScore)

    @Update
    suspend fun update(user: TestScore)

    @Query("SELECT COUNT(*) FROM TestScore")
    suspend fun getCount(): Int

    @Insert
    suspend fun insertAll(data: List<TestScore>)

}