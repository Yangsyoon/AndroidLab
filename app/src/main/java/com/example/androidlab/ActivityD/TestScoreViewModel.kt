package com.example.androidlab.ActivityD

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TestScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    val testScores: Flow<List<TestScore>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection("testScores")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val dateStr = doc.getString("date") ?: return@mapNotNull null
                        TestScore(
                            id = doc.id,
                            date = dateStr, // 문자열로 바로 저장
                            angryCorrect = (doc.getLong("angryCorrect") ?: 0L).toInt(),
                            angryWrong = (doc.getLong("angryWrong") ?: 0L).toInt(),
                            happyCorrect = (doc.getLong("happyCorrect") ?: 0L).toInt(),
                            happyWrong = (doc.getLong("happyWrong") ?: 0L).toInt(),
                            surprisedCorrect = (doc.getLong("surprisedCorrect") ?: 0L).toInt(),
                            surprisedWrong = (doc.getLong("surprisedWrong") ?: 0L).toInt(),
                            sadCorrect = (doc.getLong("sadCorrect") ?: 0L).toInt(),
                            sadWrong = (doc.getLong("sadWrong") ?: 0L).toInt()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(list)
            }

        awaitClose { listenerRegistration.remove() }
    }
}
