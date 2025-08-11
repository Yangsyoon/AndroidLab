package com.example.androidlab.ActivityD

import java.time.LocalDateTime

data class TestScore(
    val id: String,
    val date: String, // LocalDateTime 대신 String
    val emotion1Correct: Int,
    val emotion1Wrong: Int,
    val emotion2Correct: Int,
    val emotion2Wrong: Int,
    val emotion3Correct: Int,
    val emotion3Wrong: Int,
    val emotion4Correct: Int,
    val emotion4Wrong: Int,
    val emotion5Correct: Int,
    val emotion5Wrong: Int
)
