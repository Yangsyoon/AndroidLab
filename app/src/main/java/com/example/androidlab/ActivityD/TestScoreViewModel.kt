package com.example.androidlab.ActivityD

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.androidlab.database.MyFaceScore
import com.example.androidlab.database.MyFaceScoreDatabase
import com.example.androidlab.database.TestScore
import com.example.androidlab.database.TestScoreDatabase
import kotlinx.coroutines.flow.Flow


class TestScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = TestScoreDatabase.getInstance(application).testScoreDAO()

    val testScores: Flow<List<TestScore>> = dao.getAllSortedByDate()
}
