package com.example.androidlab

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TestScore")
data class TestScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val age: Int
)
