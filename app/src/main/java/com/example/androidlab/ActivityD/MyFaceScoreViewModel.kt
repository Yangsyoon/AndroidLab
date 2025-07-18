package com.example.androidlab.ActivityD

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.androidlab.database.MyFaceScore
import com.example.androidlab.database.MyFaceScoreDatabase
import kotlinx.coroutines.flow.Flow


class MyFaceScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = MyFaceScoreDatabase.getInstance(application).myFaceScoreDAO()

    val faceScores: Flow<List<MyFaceScore>> = dao.getAllSortedByDate()
}
