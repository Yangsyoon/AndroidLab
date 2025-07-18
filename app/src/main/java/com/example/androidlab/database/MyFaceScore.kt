package com.example.androidlab.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "MyFaceScore")
data class MyFaceScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDateTime,
    val emotion1Score: Int,
    val emotion2Score: Int,
    val emotion3Score: Int,
    val emotion4Score: Int

)
