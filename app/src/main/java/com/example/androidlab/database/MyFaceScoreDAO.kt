package com.example.androidlab.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MyFaceScoreDAO {
    @Insert
    suspend fun insert(user: MyFaceScore)

    @Query("SELECT * FROM MyFaceScore")
    suspend fun getAllUsers(): List<MyFaceScore>

    @Query("SELECT * FROM MyFaceScore ORDER BY date DESC")
    fun getAllSortedByDate(): Flow<List<MyFaceScore>>


    @Delete
    suspend fun delete(user: MyFaceScore)

    @Update
    suspend fun update(user: MyFaceScore)

    @Query("SELECT COUNT(*) FROM MyFaceScore")
    suspend fun getCount(): Int

    @Insert
    suspend fun insertAll(data: List<MyFaceScore>)

}