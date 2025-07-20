package com.example.androidlab.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "MyFaceScore")
data class MyFaceScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDateTime,
    val emotion1Correct: Int,//2개 합쳐서 100 단위로 저장
    val emotion1Wrong: Int,
    val emotion2Correct: Int,
    val emotion2Wrong: Int,
    val emotion3Correct: Int,
    val emotion3Wrong: Int,
    val emotion4Correct: Int,
    val emotion4Wrong: Int,
    val emotion5Correct: Int,
    val emotion5Wrong: Int,
    val emotion6Correct: Int,
    val emotion6Wrong: Int,
    val emotion7Correct: Int,
    val emotion7Wrong: Int


)
