package com.example.androidlab.ActivityD

import java.time.LocalDateTime

data class MyFaceScore(
    val id: String = "",
    val date: String = "",
    val angryCorrect: Int = 0,
    val angryWrong: Int = 0,
    val happyCorrect: Int = 0,
    val happyWrong: Int = 0,
    val neutralCorrect: Int = 0,
    val neutralWrong: Int = 0,
    val surprisedCorrect: Int = 0,
    val surprisedWrong: Int = 0,
    val sadCorrect: Int = 0,
    val sadWrong: Int = 0
)
