package com.example.androidlab

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "TestScore")
data class TestScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDateTime,
    val emotion1Correct: Int,
    val emotion1Wrong: Int,
    val emotion2Correct: Int,
    val emotion2Wrong: Int,
    val emotion3Correct: Int,
    val emotion3Wrong: Int,
    val emotion4Correct: Int,
    val emotion4Wrong: Int

)
